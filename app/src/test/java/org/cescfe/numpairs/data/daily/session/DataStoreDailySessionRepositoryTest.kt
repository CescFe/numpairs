package org.cescfe.numpairs.data.daily.session

import androidx.datastore.core.DataStore
import androidx.datastore.core.handlers.ReplaceFileCorruptionHandler
import androidx.datastore.preferences.core.PreferenceDataStoreFactory
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.byteArrayPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.emptyPreferences
import androidx.datastore.preferences.core.mutablePreferencesOf
import java.io.File
import java.io.IOException
import java.time.LocalDate
import java.util.UUID
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancelAndJoin
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.cescfe.numpairs.data.generated.session.DataStoreGeneratedSessionRepository
import org.cescfe.numpairs.data.generated.session.GeneratedSessionId
import org.cescfe.numpairs.data.generated.session.GeneratedSessionSnapshot
import org.cescfe.numpairs.data.puzzle.seed.samplePuzzle
import org.cescfe.numpairs.domain.daily.DailyChallengeId
import org.cescfe.numpairs.domain.daily.DailyRecipeVersion
import org.cescfe.numpairs.domain.puzzle.model.Board
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertSame
import org.junit.Assert.assertThrows
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TemporaryFolder

class DataStoreDailySessionRepositoryTest {
    @get:Rule
    val temporaryFolder = TemporaryFolder()

    private val dataStoreJobs = mutableListOf<Job>()

    @After
    fun tearDown() {
        dataStoreJobs.forEach(Job::cancel)
    }

    @Test
    fun empty_application_data_exposes_empty_daily_state() = runBlocking {
        val fixture = createRepository()

        assertEquals(
            DailyState(activeSession = null, completedChallengeIds = emptyList()),
            fixture.repository.state.first()
        )
    }

    @Test
    fun replacement_atomically_adopts_one_daily_session() = runBlocking {
        val fixture = createRepository()
        val first = generatedDailyFixture(date = LocalDate.of(2027, 4, 18)).snapshot(sessionId = "first")
        val replacement = generatedDailyFixture(date = LocalDate.of(2027, 4, 19))
            .snapshot(sessionId = "replacement")

        assertEquals(
            DailySessionReplacementResult.Replaced,
            fixture.repository.replaceSession(first)
        )
        assertEquals(
            DailySessionReplacementResult.Replaced,
            fixture.repository.replaceSession(replacement)
        )
        assertEquals(replacement, fixture.repository.state.first().activeSession)
    }

    @Test
    fun replacement_for_a_completed_date_is_rejected_without_changing_the_previous_session() = runBlocking {
        val fixture = createRepository()
        val previous = generatedDailyFixture(date = LocalDate.of(2027, 4, 17)).snapshot()
        val completedIdentity = dailyChallengeId(LocalDate.of(2027, 4, 18))
        fixture.storeAggregate(
            DailyAggregate(
                activeSession = previous,
                completedChallengeIds = listOf(completedIdentity)
            )
        )
        val rejected = generatedDailyFixture(date = completedIdentity.localDate).snapshot()

        assertEquals(
            DailySessionReplacementResult.DateAlreadyCompleted(completedIdentity),
            fixture.repository.replaceSession(rejected)
        )
        assertEquals(previous, fixture.repository.state.first().activeSession)
        assertEquals(listOf(completedIdentity), fixture.repository.state.first().completedChallengeIds)
    }

    @Test
    fun incomplete_progress_updates_only_the_owning_daily_session() = runBlocking {
        val fixture = createRepository()
        val generatedFixture = generatedDailyFixture()
        val snapshot = generatedFixture.snapshot()
        fixture.repository.replaceSession(snapshot)

        assertEquals(
            DailySessionProgressUpdateResult.Updated,
            fixture.repository.updateCurrentPuzzle(
                expectedSessionId = snapshot.sessionId,
                puzzle = generatedFixture.progressPuzzle()
            )
        )
        assertEquals(
            snapshot.copy(currentPuzzle = generatedFixture.progressPuzzle()),
            fixture.repository.state.first().activeSession
        )
    }

    @Test
    fun stale_solved_and_inconsistent_progress_cannot_change_storage() = runBlocking {
        val fixture = createRepository()
        val generatedFixture = generatedDailyFixture()
        val snapshot = generatedFixture.snapshot()
        fixture.repository.replaceSession(snapshot)
        val changedResultPuzzle = snapshot.currentPuzzle.copy(
            board = Board(
                tiles = snapshot.currentPuzzle.board.tiles.mapIndexed { index, tile ->
                    if (index == 0) tile.copy(result = tile.result + 1) else tile
                }
            )
        )

        assertEquals(
            DailySessionProgressUpdateResult.StaleSession,
            fixture.repository.updateCurrentPuzzle(
                expectedSessionId = DailySessionId("stale"),
                puzzle = generatedFixture.progressPuzzle()
            )
        )
        assertEquals(
            DailySessionProgressUpdateResult.InvalidPuzzle,
            fixture.repository.updateCurrentPuzzle(
                expectedSessionId = snapshot.sessionId,
                puzzle = changedResultPuzzle
            )
        )
        assertEquals(
            DailySessionProgressUpdateResult.InvalidPuzzle,
            fixture.repository.updateCurrentPuzzle(
                expectedSessionId = snapshot.sessionId,
                puzzle = generatedFixture.generatedPuzzle.solvedPuzzle
            )
        )
        assertEquals(snapshot, fixture.repository.state.first().activeSession)
    }

    @Test
    fun clear_removes_only_the_owning_daily_session_and_preserves_completions() = runBlocking {
        val fixture = createRepository()
        val snapshot = generatedDailyFixture().snapshot()
        val completion = dailyChallengeId(LocalDate.of(2027, 4, 17))
        fixture.storeAggregate(
            DailyAggregate(
                activeSession = snapshot,
                completedChallengeIds = listOf(completion)
            )
        )

        assertEquals(
            DailySessionClearResult.StaleSession,
            fixture.repository.clearSession(DailySessionId("stale"))
        )
        assertEquals(snapshot, fixture.repository.state.first().activeSession)
        assertEquals(
            DailySessionClearResult.Cleared,
            fixture.repository.clearSession(snapshot.sessionId)
        )
        assertNull(fixture.repository.state.first().activeSession)
        assertEquals(listOf(completion), fixture.repository.state.first().completedChallengeIds)
    }

    @Test
    fun exact_daily_state_survives_repository_and_data_store_recreation() = runBlocking {
        val dataStoreFile = createDataStoreFile()
        val snapshot = generatedDailyFixture().snapshot()
        val completion = dailyChallengeId(LocalDate.of(2027, 4, 17))
        val firstFixture = createRepository(dataStoreFile)
        firstFixture.storeAggregate(
            DailyAggregate(
                activeSession = snapshot,
                completedChallengeIds = listOf(completion)
            )
        )
        firstFixture.close()

        val secondFixture = createRepository(dataStoreFile)

        assertEquals(
            DailyState(
                activeSession = snapshot,
                completedChallengeIds = listOf(completion)
            ),
            secondFixture.repository.state.first()
        )
    }

    @Test
    fun solved_completion_records_identity_and_removes_active_session_atomically() = runBlocking {
        val fixture = createRepository()
        val generatedFixture = generatedDailyFixture()
        val snapshot = generatedFixture.snapshot()
        fixture.repository.replaceSession(snapshot)

        assertEquals(
            DailySessionCompletionResult.Completed,
            fixture.repository.complete(
                expectedSessionId = snapshot.sessionId,
                expectedDailyChallengeId = snapshot.dailyChallengeId,
                solvedPuzzle = generatedFixture.solvedProgressPuzzle()
            )
        )
        assertEquals(
            DailyState(
                activeSession = null,
                completedChallengeIds = listOf(snapshot.dailyChallengeId)
            ),
            fixture.repository.state.first()
        )
    }

    @Test
    fun exact_repeated_completion_is_idempotent() = runBlocking {
        val fixture = createRepository()
        val generatedFixture = generatedDailyFixture()
        val snapshot = generatedFixture.snapshot()
        val solvedPuzzle = generatedFixture.solvedProgressPuzzle()
        fixture.repository.replaceSession(snapshot)
        fixture.repository.complete(
            expectedSessionId = snapshot.sessionId,
            expectedDailyChallengeId = snapshot.dailyChallengeId,
            solvedPuzzle = solvedPuzzle
        )

        assertEquals(
            DailySessionCompletionResult.AlreadyCompleted(snapshot.dailyChallengeId),
            fixture.repository.complete(
                expectedSessionId = snapshot.sessionId,
                expectedDailyChallengeId = snapshot.dailyChallengeId,
                solvedPuzzle = solvedPuzzle
            )
        )
        assertEquals(
            listOf(snapshot.dailyChallengeId),
            fixture.repository.state.first().completedChallengeIds
        )
    }

    @Test
    fun another_recipe_cannot_add_a_second_completion_for_the_same_date() = runBlocking {
        val fixture = createRepository()
        val existingCompletion = dailyChallengeId(
            date = LocalDate.of(2027, 4, 18),
            recipeVersion = DailyRecipeVersion("retired-recipe")
        )
        fixture.storeAggregate(
            DailyAggregate(completedChallengeIds = listOf(existingCompletion))
        )
        val currentFixture = generatedDailyFixture(date = existingCompletion.localDate)

        assertEquals(
            DailySessionCompletionResult.AlreadyCompleted(existingCompletion),
            fixture.repository.complete(
                expectedSessionId = DailySessionId("missing"),
                expectedDailyChallengeId = currentFixture.identity,
                solvedPuzzle = currentFixture.solvedProgressPuzzle()
            )
        )
        assertEquals(
            listOf(existingCompletion),
            fixture.repository.state.first().completedChallengeIds
        )
    }

    @Test
    fun stale_session_or_challenge_identity_cannot_complete_a_successor() = runBlocking {
        val fixture = createRepository()
        val predecessorFixture = generatedDailyFixture(date = LocalDate.of(2027, 4, 18))
        val predecessor = predecessorFixture.snapshot(sessionId = "predecessor")
        fixture.repository.replaceSession(predecessor)
        val successorFixture = generatedDailyFixture(date = LocalDate.of(2027, 4, 19))
        val successor = successorFixture.snapshot(sessionId = "successor")
        fixture.repository.replaceSession(successor)

        assertEquals(
            DailySessionCompletionResult.StaleSession,
            fixture.repository.complete(
                expectedSessionId = predecessor.sessionId,
                expectedDailyChallengeId = predecessor.dailyChallengeId,
                solvedPuzzle = predecessorFixture.solvedProgressPuzzle()
            )
        )
        assertEquals(
            DailySessionCompletionResult.StaleSession,
            fixture.repository.complete(
                expectedSessionId = successor.sessionId,
                expectedDailyChallengeId = predecessor.dailyChallengeId,
                solvedPuzzle = successorFixture.solvedProgressPuzzle()
            )
        )
        assertEquals(successor, fixture.repository.state.first().activeSession)
        assertEquals(emptyList<DailyChallengeId>(), fixture.repository.state.first().completedChallengeIds)
    }

    @Test
    fun unsolved_or_inconsistent_puzzle_cannot_complete_daily() = runBlocking {
        val fixture = createRepository()
        val generatedFixture = generatedDailyFixture()
        val snapshot = generatedFixture.snapshot()
        fixture.repository.replaceSession(snapshot)
        val solvedPuzzle = generatedFixture.solvedProgressPuzzle()
        val inconsistentPuzzle = solvedPuzzle.copy(
            board = Board(
                tiles = solvedPuzzle.board.tiles.mapIndexed { index, tile ->
                    if (index == 0) tile.copy(result = tile.result + 1) else tile
                }
            )
        )

        assertEquals(
            DailySessionCompletionResult.InvalidPuzzle,
            fixture.repository.complete(
                expectedSessionId = snapshot.sessionId,
                expectedDailyChallengeId = snapshot.dailyChallengeId,
                solvedPuzzle = snapshot.currentPuzzle
            )
        )
        assertEquals(
            DailySessionCompletionResult.InvalidPuzzle,
            fixture.repository.complete(
                expectedSessionId = snapshot.sessionId,
                expectedDailyChallengeId = snapshot.dailyChallengeId,
                solvedPuzzle = inconsistentPuzzle
            )
        )
        assertEquals(snapshot, fixture.repository.state.first().activeSession)
        assertEquals(emptyList<DailyChallengeId>(), fixture.repository.state.first().completedChallengeIds)
    }

    @Test
    fun completion_history_survives_repository_recreation() = runBlocking {
        val dataStoreFile = createDataStoreFile()
        val firstFixture = createRepository(dataStoreFile)
        val generatedFixture = generatedDailyFixture()
        val snapshot = generatedFixture.snapshot()
        firstFixture.repository.replaceSession(snapshot)
        firstFixture.repository.complete(
            expectedSessionId = snapshot.sessionId,
            expectedDailyChallengeId = snapshot.dailyChallengeId,
            solvedPuzzle = generatedFixture.solvedProgressPuzzle()
        )
        firstFixture.close()

        val secondFixture = createRepository(dataStoreFile)

        assertEquals(
            DailyState(
                activeSession = null,
                completedChallengeIds = listOf(snapshot.dailyChallengeId)
            ),
            secondFixture.repository.state.first()
        )
    }

    @Test
    fun invalid_or_unsupported_aggregate_recovers_as_empty_and_accepts_a_successor() = runBlocking {
        val fixture = createRepository()
        fixture.dataStore.edit { preferences ->
            preferences[byteArrayPreferencesKey(DAILY_AGGREGATE_PREFERENCE_KEY_NAME)] =
                byteArrayOf(1, 2, 3)
        }

        assertEquals(
            DailyState(activeSession = null, completedChallengeIds = emptyList()),
            fixture.repository.state.first()
        )

        val replacement = generatedDailyFixture().snapshot()
        assertEquals(
            DailySessionCompletionResult.StaleSession,
            fixture.repository.complete(
                expectedSessionId = replacement.sessionId,
                expectedDailyChallengeId = replacement.dailyChallengeId,
                solvedPuzzle = generatedDailyFixture().solvedProgressPuzzle()
            )
        )
        assertEquals(emptyList<DailyChallengeId>(), fixture.repository.state.first().completedChallengeIds)
        assertEquals(
            DailySessionReplacementResult.Replaced,
            fixture.repository.replaceSession(replacement)
        )
        assertEquals(replacement, fixture.repository.state.first().activeSession)
    }

    @Test
    fun failed_replacement_write_leaves_the_previous_daily_state_intact() {
        val previous = generatedDailyFixture(date = LocalDate.of(2027, 4, 17)).snapshot()
        val aggregateKey = byteArrayPreferencesKey(DAILY_AGGREGATE_PREFERENCE_KEY_NAME)
        val storedPreferences = mutablePreferencesOf(
            aggregateKey to DailyAggregateCodec().encode(DailyAggregate(activeSession = previous))
        )
        val expectedFailure = IOException("Synthetic Daily write failure.")
        val storedState = MutableStateFlow<Preferences>(storedPreferences)
        val repository = DataStoreDailySessionRepository(
            dataStore = object : DataStore<Preferences> {
                override val data = storedState

                override suspend fun updateData(transform: suspend (Preferences) -> Preferences): Preferences =
                    throw expectedFailure
            }
        )

        val actualFailure = assertThrows(IOException::class.java) {
            runBlocking {
                repository.replaceSession(
                    generatedDailyFixture(date = LocalDate.of(2027, 4, 18)).snapshot()
                )
            }
        }

        assertSame(expectedFailure, actualFailure)
        assertEquals(previous, runBlocking { repository.state.first().activeSession })
    }

    @Test
    fun failed_completion_write_keeps_the_active_session_and_does_not_publish_history() {
        val generatedFixture = generatedDailyFixture()
        val activeSession = generatedFixture.snapshot()
        val aggregateKey = byteArrayPreferencesKey(DAILY_AGGREGATE_PREFERENCE_KEY_NAME)
        val storedPreferences = mutablePreferencesOf(
            aggregateKey to DailyAggregateCodec().encode(DailyAggregate(activeSession = activeSession))
        )
        val expectedFailure = IOException("Synthetic Daily completion write failure.")
        val storedState = MutableStateFlow<Preferences>(storedPreferences)
        val repository = DataStoreDailySessionRepository(
            dataStore = object : DataStore<Preferences> {
                override val data = storedState

                override suspend fun updateData(transform: suspend (Preferences) -> Preferences): Preferences =
                    throw expectedFailure
            }
        )

        val actualFailure = assertThrows(IOException::class.java) {
            runBlocking {
                repository.complete(
                    expectedSessionId = activeSession.sessionId,
                    expectedDailyChallengeId = activeSession.dailyChallengeId,
                    solvedPuzzle = generatedFixture.solvedProgressPuzzle()
                )
            }
        }

        assertSame(expectedFailure, actualFailure)
        assertEquals(
            DailyState(activeSession = activeSession, completedChallengeIds = emptyList()),
            runBlocking { repository.state.first() }
        )
    }

    @Test
    fun daily_and_normal_generated_repositories_do_not_replace_each_other() = runBlocking {
        val dailyFixture = createRepository()
        val normalDataStoreFixture = createRepository()
        val normalRepository = DataStoreGeneratedSessionRepository(normalDataStoreFixture.dataStore)
        val normalSnapshot = GeneratedSessionSnapshot(
            sessionId = GeneratedSessionId("normal-session"),
            modeId = "four-pairs",
            profileId = "4-pairs-low",
            seed = 42,
            initialPuzzle = samplePuzzle,
            currentPuzzle = samplePuzzle
        )
        normalRepository.replace(normalSnapshot)
        val dailySnapshot = generatedDailyFixture().snapshot()

        dailyFixture.repository.replaceSession(dailySnapshot)
        dailyFixture.repository.complete(
            expectedSessionId = dailySnapshot.sessionId,
            expectedDailyChallengeId = dailySnapshot.dailyChallengeId,
            solvedPuzzle = generatedDailyFixture().solvedProgressPuzzle()
        )

        assertEquals(normalSnapshot, normalRepository.session.first())
        assertEquals(
            listOf(dailySnapshot.dailyChallengeId),
            dailyFixture.repository.state.first().completedChallengeIds
        )
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
            repository = DataStoreDailySessionRepository(dataStore),
            dataStore = dataStore,
            job = job
        )
    }

    private fun createDataStoreFile(): File = File(
        temporaryFolder.root,
        "${UUID.randomUUID()}.preferences_pb"
    )

    private data class RepositoryFixture(
        val repository: DataStoreDailySessionRepository,
        val dataStore: DataStore<Preferences>,
        private val job: Job
    ) {
        suspend fun storeAggregate(aggregate: DailyAggregate) {
            dataStore.edit { preferences ->
                preferences[byteArrayPreferencesKey(DAILY_AGGREGATE_PREFERENCE_KEY_NAME)] =
                    DailyAggregateCodec().encode(aggregate)
            }
        }

        suspend fun close() {
            job.cancelAndJoin()
        }
    }
}
