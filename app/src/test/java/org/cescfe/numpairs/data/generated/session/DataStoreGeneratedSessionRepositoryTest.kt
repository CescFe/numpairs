package org.cescfe.numpairs.data.generated.session

import androidx.datastore.core.handlers.ReplaceFileCorruptionHandler
import androidx.datastore.preferences.core.PreferenceDataStoreFactory
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.byteArrayPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.emptyPreferences
import java.io.File
import java.util.UUID
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancelAndJoin
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.cescfe.numpairs.data.puzzle.seed.samplePuzzle
import org.cescfe.numpairs.domain.generated.GeneratedElapsedTime
import org.cescfe.numpairs.domain.generated.GeneratedPersonalBestCategory
import org.cescfe.numpairs.domain.generated.GeneratedPersonalBestOutcome
import org.cescfe.numpairs.domain.generated.GeneratedPersonalBestResult
import org.cescfe.numpairs.domain.generated.GeneratedTimingStartInstant
import org.cescfe.numpairs.domain.puzzle.PuzzleCorrectionCount
import org.cescfe.numpairs.domain.puzzle.model.Expression
import org.cescfe.numpairs.domain.puzzle.model.Puzzle
import org.cescfe.numpairs.feature.game.presentation.support.solvedPuzzleWithKnownStripAndAssignments
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TemporaryFolder

class DataStoreGeneratedSessionRepositoryTest {
    @get:Rule
    val temporaryFolder = TemporaryFolder()

    private val dataStoreJobs = mutableListOf<Job>()

    @After
    fun tearDown() {
        dataStoreJobs.forEach(Job::cancel)
    }

    @Test
    fun `session is empty before a snapshot is stored`() = runBlocking {
        val fixture = createRepository()

        assertNull(fixture.repository.session.first())
    }

    @Test
    fun `replaces the single session atomically`() = runBlocking {
        val fixture = createRepository()
        val firstSnapshot = snapshot(sessionId = "first")
        val replacement = snapshot(sessionId = "replacement", modeId = "eight-pairs")

        fixture.repository.replace(firstSnapshot)
        fixture.repository.replace(replacement)

        assertEquals(replacement, fixture.repository.session.first())
    }

    @Test
    fun `updates the current puzzle for the owning session`() = runBlocking {
        val fixture = createRepository()
        val snapshot = snapshot()
        val updatedPuzzle = updatedPuzzle()
        fixture.repository.replace(snapshot)

        val wasUpdated = fixture.repository.updateCurrentPuzzle(
            expectedSessionId = snapshot.sessionId,
            puzzle = updatedPuzzle,
            correctionCount = PuzzleCorrectionCount.ZERO
        )

        assertTrue(wasUpdated)
        assertEquals(
            snapshot.copy(currentPuzzle = updatedPuzzle),
            fixture.repository.session.first()
        )
    }

    @Test
    fun `timing start is identity guarded and can only be established once`() = runBlocking {
        val fixture = createRepository()
        val snapshot = snapshot()
        fixture.repository.replace(snapshot)

        assertEquals(
            GeneratedSessionTimingStartResult.StaleSession,
            fixture.repository.startTiming(
                GeneratedSessionId("stale"),
                GeneratedTimingStartInstant(1_000)
            )
        )
        assertEquals(
            GeneratedSessionTimingStartResult.Started(GeneratedTimingStartInstant(2_000)),
            fixture.repository.startTiming(snapshot.sessionId, GeneratedTimingStartInstant(2_000))
        )
        assertEquals(
            GeneratedSessionTimingStartResult.AlreadyStarted(GeneratedTimingStartInstant(2_000)),
            fixture.repository.startTiming(snapshot.sessionId, GeneratedTimingStartInstant(9_000))
        )
        assertEquals(
            2_000L,
            requireNotNull(fixture.repository.session.first()?.timingStartInstant).epochMilliseconds
        )
    }

    @Test
    fun `stale update cannot overwrite a replacement session`() = runBlocking {
        val fixture = createRepository()
        val staleSnapshot = snapshot(sessionId = "stale")
        val replacement = snapshot(sessionId = "replacement")
        fixture.repository.replace(staleSnapshot)
        fixture.repository.replace(replacement)

        val wasUpdated = fixture.repository.updateCurrentPuzzle(
            expectedSessionId = staleSnapshot.sessionId,
            puzzle = updatedPuzzle(),
            correctionCount = PuzzleCorrectionCount.ZERO
        )

        assertFalse(wasUpdated)
        assertEquals(replacement, fixture.repository.session.first())
    }

    @Test
    fun `correction progress accepts retries and forward counts but rejects regressions and unknown mismatches`() =
        runBlocking {
            val fixture = createRepository()
            val snapshot = snapshot()
            val updatedPuzzle = updatedPuzzle()
            fixture.repository.replace(snapshot)

            assertTrue(
                fixture.repository.updateCurrentPuzzle(
                    expectedSessionId = snapshot.sessionId,
                    puzzle = updatedPuzzle,
                    correctionCount = PuzzleCorrectionCount(3)
                )
            )
            assertTrue(
                fixture.repository.updateCurrentPuzzle(
                    expectedSessionId = snapshot.sessionId,
                    puzzle = updatedPuzzle,
                    correctionCount = PuzzleCorrectionCount(3)
                )
            )
            assertFalse(
                fixture.repository.updateCurrentPuzzle(
                    expectedSessionId = snapshot.sessionId,
                    puzzle = updatedPuzzle,
                    correctionCount = PuzzleCorrectionCount(2)
                )
            )
            assertFalse(
                fixture.repository.updateCurrentPuzzle(
                    expectedSessionId = snapshot.sessionId,
                    puzzle = updatedPuzzle,
                    correctionCount = null
                )
            )

            assertEquals(
                3L,
                requireNotNull(fixture.repository.session.first()?.correctionCount).value
            )
        }

    @Test
    fun `legacy unknown correction count remains unknown through progress`() = runBlocking {
        val fixture = createRepository()
        val legacySnapshot = snapshot().copy(correctionCount = null)
        fixture.dataStore.edit { preferences ->
            preferences[byteArrayPreferencesKey(GENERATED_SESSION_SNAPSHOT_PREFERENCE_KEY_NAME)] =
                GeneratedSessionSnapshotCodec().encode(legacySnapshot)
        }

        assertTrue(
            fixture.repository.updateCurrentPuzzle(
                expectedSessionId = legacySnapshot.sessionId,
                puzzle = updatedPuzzle(),
                correctionCount = null
            )
        )
        assertFalse(
            fixture.repository.updateCurrentPuzzle(
                expectedSessionId = legacySnapshot.sessionId,
                puzzle = updatedPuzzle(),
                correctionCount = PuzzleCorrectionCount.ZERO
            )
        )
        assertNull(fixture.repository.session.first()?.correctionCount)
    }

    @Test
    fun `clear only removes the owning session`() = runBlocking {
        val fixture = createRepository()
        val snapshot = snapshot()
        fixture.repository.replace(snapshot)

        assertFalse(fixture.repository.clear(GeneratedSessionId("stale")))
        assertEquals(snapshot, fixture.repository.session.first())
        assertTrue(fixture.repository.clear(snapshot.sessionId))
        assertNull(fixture.repository.session.first())
    }

    @Test
    fun `completion atomically clears the owning session and establishes its category baseline`() = runBlocking {
        val fixture = createRepository()
        val snapshot = timedCompletableSnapshot()
        val elapsedTime = GeneratedElapsedTime(65_432)
        val personalBestResult = GeneratedPersonalBestResult.classify(
            category = GeneratedPersonalBestCategory.FOUR_PAIRS_LOW,
            currentElapsedTime = elapsedTime,
            previousBestElapsedTime = null
        )
        fixture.repository.replace(snapshot)
        fixture.repository.startTiming(snapshot.sessionId, GeneratedTimingStartInstant(1_700_000_000_000))

        assertEquals(
            GeneratedSessionCompletionResult.Completed,
            fixture.repository.complete(
                expectedSessionId = snapshot.sessionId,
                solvedPuzzle = solvedPuzzleWithKnownStripAndAssignments(),
                correctionCount = PuzzleCorrectionCount(2),
                personalBestResult = personalBestResult
            )
        )

        val state = fixture.repository.state.first()
        assertNull(state.activeSession)
        assertEquals(
            mapOf(GeneratedPersonalBestCategory.FOUR_PAIRS_LOW to elapsedTime),
            state.personalBests
        )
        assertEquals(GeneratedPersonalBestOutcome.BASELINE, personalBestResult.outcome)
    }

    @Test
    fun `solved progress cannot bypass the atomic completion operation`() = runBlocking {
        val fixture = createRepository()
        val snapshot = timedCompletableSnapshot()
        fixture.repository.replace(snapshot)

        assertFalse(
            fixture.repository.updateCurrentPuzzle(
                expectedSessionId = snapshot.sessionId,
                puzzle = solvedPuzzleWithKnownStripAndAssignments(),
                correctionCount = snapshot.correctionCount
            )
        )
        assertEquals(snapshot, fixture.repository.state.first().activeSession)
        assertTrue(fixture.repository.state.first().personalBests.isEmpty())
    }

    @Test
    fun `completion updates only the exact category and preserves the other five bests`() = runBlocking {
        val fixture = createRepository()
        val existingBests = GeneratedPersonalBestCategory.entries.associateWith { category ->
            GeneratedElapsedTime(80_000L + category.ordinal)
        }
        fixture.dataStore.edit { preferences ->
            preferences[byteArrayPreferencesKey(GENERATED_SESSION_SNAPSHOT_PREFERENCE_KEY_NAME)] =
                GeneratedAggregateCodec().encode(GeneratedAggregate(personalBests = existingBests))
        }
        val snapshot = timedCompletableSnapshot()
        fixture.repository.replace(snapshot)
        fixture.repository.startTiming(snapshot.sessionId, GeneratedTimingStartInstant(1_700_000_000_000))
        val category = GeneratedPersonalBestCategory.FOUR_PAIRS_LOW
        val improved = GeneratedElapsedTime(70_000)

        assertEquals(
            GeneratedSessionCompletionResult.Completed,
            fixture.repository.complete(
                expectedSessionId = snapshot.sessionId,
                solvedPuzzle = solvedPuzzleWithKnownStripAndAssignments(),
                correctionCount = snapshot.correctionCount,
                personalBestResult = GeneratedPersonalBestResult.classify(
                    category = category,
                    currentElapsedTime = improved,
                    previousBestElapsedTime = existingBests.getValue(category)
                )
            )
        )

        assertEquals(existingBests + (category to improved), fixture.repository.state.first().personalBests)
    }

    @Test
    fun `stale identity or stale previous best changes neither session nor bests`() = runBlocking {
        val fixture = createRepository()
        val snapshot = timedCompletableSnapshot()
        fixture.repository.replace(snapshot)
        fixture.repository.startTiming(snapshot.sessionId, GeneratedTimingStartInstant(1_700_000_000_000))
        val frozenResult = GeneratedPersonalBestResult.classify(
            category = GeneratedPersonalBestCategory.FOUR_PAIRS_LOW,
            currentElapsedTime = GeneratedElapsedTime(50_000),
            previousBestElapsedTime = GeneratedElapsedTime(60_000)
        )

        assertEquals(
            GeneratedSessionCompletionResult.StaleSession,
            fixture.repository.complete(
                expectedSessionId = GeneratedSessionId("stale"),
                solvedPuzzle = solvedPuzzleWithKnownStripAndAssignments(),
                correctionCount = snapshot.correctionCount,
                personalBestResult = frozenResult
            )
        )
        assertEquals(
            GeneratedSessionCompletionResult.StalePersonalBest,
            fixture.repository.complete(
                expectedSessionId = snapshot.sessionId,
                solvedPuzzle = solvedPuzzleWithKnownStripAndAssignments(),
                correctionCount = snapshot.correctionCount,
                personalBestResult = frozenResult
            )
        )
        assertEquals(
            snapshot.copy(timingStartInstant = GeneratedTimingStartInstant(1_700_000_000_000)),
            fixture.repository.state.first().activeSession
        )
        assertTrue(fixture.repository.state.first().personalBests.isEmpty())
    }

    @Test
    fun `untimed completion clears the session without establishing or changing a best`() = runBlocking {
        val fixture = createRepository()
        val category = GeneratedPersonalBestCategory.FOUR_PAIRS_LOW
        val existingBest = GeneratedElapsedTime(45_000)
        fixture.dataStore.edit { preferences ->
            preferences[byteArrayPreferencesKey(GENERATED_SESSION_SNAPSHOT_PREFERENCE_KEY_NAME)] =
                GeneratedAggregateCodec().encode(
                    GeneratedAggregate(personalBests = mapOf(category to existingBest))
                )
        }
        val snapshot = timedCompletableSnapshot()
        fixture.repository.replace(snapshot)

        assertEquals(
            GeneratedSessionCompletionResult.Completed,
            fixture.repository.complete(
                expectedSessionId = snapshot.sessionId,
                solvedPuzzle = solvedPuzzleWithKnownStripAndAssignments(),
                correctionCount = snapshot.correctionCount,
                personalBestResult = GeneratedPersonalBestResult.classify(
                    category = category,
                    currentElapsedTime = null,
                    previousBestElapsedTime = existingBest
                )
            )
        )

        val state = fixture.repository.state.first()
        assertNull(state.activeSession)
        assertEquals(mapOf(category to existingBest), state.personalBests)
    }

    @Test
    fun `session persists across repository and data store recreation`() = runBlocking {
        val dataStoreFile = createDataStoreFile()
        val snapshot = snapshot()
        val firstFixture = createRepository(dataStoreFile)
        firstFixture.repository.replace(snapshot)
        firstFixture.close()

        val secondFixture = createRepository(dataStoreFile)

        assertEquals(snapshot, secondFixture.repository.session.first())
    }

    @Test
    fun `personal best persists across repository and data store recreation`() = runBlocking {
        val dataStoreFile = createDataStoreFile()
        val snapshot = timedCompletableSnapshot()
        val elapsedTime = GeneratedElapsedTime(49_876)
        val firstFixture = createRepository(dataStoreFile)
        firstFixture.repository.replace(snapshot)
        firstFixture.repository.startTiming(snapshot.sessionId, GeneratedTimingStartInstant(1_700_000_000_000))
        firstFixture.repository.complete(
            expectedSessionId = snapshot.sessionId,
            solvedPuzzle = solvedPuzzleWithKnownStripAndAssignments(),
            correctionCount = snapshot.correctionCount,
            personalBestResult = GeneratedPersonalBestResult.classify(
                category = GeneratedPersonalBestCategory.FOUR_PAIRS_LOW,
                currentElapsedTime = elapsedTime,
                previousBestElapsedTime = null
            )
        )
        firstFixture.close()

        val secondFixture = createRepository(dataStoreFile)

        assertNull(secondFixture.repository.state.first().activeSession)
        assertEquals(
            elapsedTime.milliseconds,
            requireNotNull(
                secondFixture.repository.state.first()
                    .personalBests[GeneratedPersonalBestCategory.FOUR_PAIRS_LOW]
            ).milliseconds
        )
    }

    @Test
    fun `invalid encoded session recovers as empty`() = runBlocking {
        val fixture = createRepository()
        fixture.dataStore.edit { preferences ->
            preferences[byteArrayPreferencesKey(GENERATED_SESSION_SNAPSHOT_PREFERENCE_KEY_NAME)] =
                byteArrayOf(1, 2, 3)
        }

        assertNull(fixture.repository.session.first())

        val replacement = snapshot()
        fixture.repository.replace(replacement)
        assertEquals(replacement, fixture.repository.session.first())
    }

    private fun createRepository(dataStoreFile: File = createDataStoreFile()): RepositoryFixture {
        val job = SupervisorJob()
        dataStoreJobs += job
        val dataStore = PreferenceDataStoreFactory.create(
            corruptionHandler = ReplaceFileCorruptionHandler {
                emptyPreferences()
            },
            scope = CoroutineScope(job + Dispatchers.IO),
            produceFile = { dataStoreFile }
        )

        return RepositoryFixture(
            repository = DataStoreGeneratedSessionRepository(dataStore),
            dataStore = dataStore,
            job = job
        )
    }

    private fun createDataStoreFile(): File = File(
        temporaryFolder.root,
        "${UUID.randomUUID()}.preferences_pb"
    )

    private fun snapshot(sessionId: String = "session-208", modeId: String = "four-pairs"): GeneratedSessionSnapshot =
        GeneratedSessionSnapshot(
            sessionId = GeneratedSessionId(sessionId),
            modeId = modeId,
            profileId = if (modeId == "four-pairs") {
                "4-pairs-low"
            } else {
                "8-pairs-medium"
            },
            seed = 208,
            initialPuzzle = samplePuzzle,
            currentPuzzle = samplePuzzle
        )

    private fun updatedPuzzle(): Puzzle = samplePuzzle.copy(
        strip = samplePuzzle.strip.withUpdatedEntry(
            index = 1,
            value = 1
        )
    )

    private fun timedCompletableSnapshot(): GeneratedSessionSnapshot {
        val solvedPuzzle = solvedPuzzleWithKnownStripAndAssignments()
        val initialPuzzle = solvedPuzzle.copy(
            board = solvedPuzzle.board.copy(
                tiles = solvedPuzzle.board.tiles.map { tile ->
                    tile.copy(
                        expression = tile.expression.copy(
                            leftOperand = Expression.Operand.Hidden,
                            operator = org.cescfe.numpairs.domain.puzzle.model.Operator.Hidden,
                            rightOperand = Expression.Operand.Hidden
                        )
                    )
                }
            )
        )
        return GeneratedSessionSnapshot(
            sessionId = GeneratedSessionId("timed-completion"),
            modeId = "four-pairs",
            profileId = "4-pairs-low",
            seed = 717,
            initialPuzzle = initialPuzzle,
            currentPuzzle = initialPuzzle,
            correctionCount = PuzzleCorrectionCount.ZERO
        )
    }

    private data class RepositoryFixture(
        val repository: GeneratedSessionRepository,
        val dataStore: androidx.datastore.core.DataStore<Preferences>,
        private val job: Job
    ) {
        suspend fun close() {
            job.cancelAndJoin()
        }
    }
}
