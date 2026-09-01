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
import org.cescfe.numpairs.domain.daily.DailyCompletion
import org.cescfe.numpairs.domain.daily.DailyElapsedTime
import org.cescfe.numpairs.domain.daily.DailyMovementCount
import org.cescfe.numpairs.domain.daily.DailyRecipeVersion
import org.cescfe.numpairs.domain.daily.DailyTimingStartInstant
import org.cescfe.numpairs.domain.puzzle.model.Board
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertSame
import org.junit.Assert.assertThrows
import org.junit.Assert.assertTrue
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
            DailyState(activeSession = null, completions = emptyList()),
            fixture.repository.state.first()
        )
    }

    @Test
    fun replacement_atomically_adopts_one_daily_session() = runBlocking {
        val fixture = createRepository()
        val first = generatedDailyFixture(date = LocalDate.of(2027, 4, 18)).snapshot(sessionId = "first")
        val replacement = generatedDailyFixture(date = LocalDate.of(2027, 4, 19))
            .snapshot(sessionId = "replacement")

        assertEquals(DailyMovementCount.ZERO, first.movementCount)
        assertEquals(DailyMovementCount.ZERO, replacement.movementCount)

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
    fun replacement_rejects_a_new_session_that_does_not_start_at_zero_movements() {
        val fixture = createRepository()

        assertThrows(IllegalArgumentException::class.java) {
            runBlocking {
                fixture.repository.replaceSession(
                    generatedDailyFixture().snapshot(movementCount = null)
                )
            }
        }
        assertThrows(IllegalArgumentException::class.java) {
            runBlocking {
                fixture.repository.replaceSession(
                    generatedDailyFixture().snapshot(movementCount = DailyMovementCount(1))
                )
            }
        }
    }

    @Test
    fun replacement_for_a_completed_date_is_rejected_without_changing_the_previous_session() = runBlocking {
        val fixture = createRepository()
        val previous = generatedDailyFixture(date = LocalDate.of(2027, 4, 17)).snapshot()
        val completedIdentity = dailyChallengeId(LocalDate.of(2027, 4, 18))
        val completion = untimedCompletion(completedIdentity)
        fixture.storeAggregate(
            DailyAggregate(
                activeSession = previous,
                completions = listOf(completion)
            )
        )
        val rejected = generatedDailyFixture(date = completedIdentity.localDate).snapshot()

        assertEquals(
            DailySessionReplacementResult.DateAlreadyCompleted(completion),
            fixture.repository.replaceSession(rejected)
        )
        assertEquals(previous, fixture.repository.state.first().activeSession)
        assertEquals(listOf(completedIdentity), fixture.repository.state.first().completedChallengeIds)
    }

    @Test
    fun timing_start_is_identity_guarded_persisted_and_cannot_be_reset() = runBlocking {
        val fixture = createRepository()
        val snapshot = generatedDailyFixture().snapshot()
        val firstStart = DailyTimingStartInstant(1_798_761_600_123)
        val laterStart = DailyTimingStartInstant(1_798_761_605_987)
        fixture.repository.replaceSession(snapshot)

        assertEquals(
            DailySessionTimingStartResult.StaleSession,
            fixture.repository.startTiming(
                expectedSessionId = DailySessionId("stale"),
                startInstant = firstStart
            )
        )
        assertEquals(
            DailySessionTimingStartResult.Started(firstStart),
            fixture.repository.startTiming(
                expectedSessionId = snapshot.sessionId,
                startInstant = firstStart
            )
        )
        assertEquals(
            DailySessionTimingStartResult.AlreadyStarted(firstStart),
            fixture.repository.startTiming(
                expectedSessionId = snapshot.sessionId,
                startInstant = laterStart
            )
        )
        assertEquals(
            snapshot.copy(timingStartInstant = firstStart),
            fixture.repository.state.first().activeSession
        )
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
                puzzle = generatedFixture.progressPuzzle(),
                movementCount = DailyMovementCount(1)
            )
        )
        assertEquals(
            snapshot.copy(
                currentPuzzle = generatedFixture.progressPuzzle(),
                movementCount = DailyMovementCount(1)
            ),
            fixture.repository.state.first().activeSession
        )
    }

    @Test
    fun progress_accepts_idempotent_and_forward_counts_but_rejects_regressions_and_unknown_mismatches() = runBlocking {
        val fixture = createRepository()
        val generatedFixture = generatedDailyFixture()
        val snapshot = generatedFixture.snapshot()
        val progressPuzzle = generatedFixture.progressPuzzle()
        fixture.repository.replaceSession(snapshot)

        assertEquals(
            DailySessionProgressUpdateResult.Updated,
            fixture.repository.updateCurrentPuzzle(
                expectedSessionId = snapshot.sessionId,
                puzzle = progressPuzzle,
                movementCount = DailyMovementCount(3)
            )
        )
        assertEquals(
            DailySessionProgressUpdateResult.Updated,
            fixture.repository.updateCurrentPuzzle(
                expectedSessionId = snapshot.sessionId,
                puzzle = progressPuzzle,
                movementCount = DailyMovementCount(3)
            )
        )
        assertEquals(
            DailySessionProgressUpdateResult.InvalidMovement,
            fixture.repository.updateCurrentPuzzle(
                expectedSessionId = snapshot.sessionId,
                puzzle = progressPuzzle,
                movementCount = DailyMovementCount(2)
            )
        )
        assertEquals(
            DailySessionProgressUpdateResult.InvalidMovement,
            fixture.repository.updateCurrentPuzzle(
                expectedSessionId = snapshot.sessionId,
                puzzle = progressPuzzle,
                movementCount = null
            )
        )
        assertEquals(
            DailyMovementCount(3),
            fixture.repository.state.first().activeSession?.movementCount
        )
    }

    @Test
    fun migrated_unknown_progress_remains_unknown_and_rejects_enabling_tracking_mid_session() = runBlocking {
        val fixture = createRepository()
        val generatedFixture = generatedDailyFixture()
        val snapshot = generatedFixture.snapshot(movementCount = null)
        fixture.storeAggregate(DailyAggregate(activeSession = snapshot))

        assertEquals(
            DailySessionProgressUpdateResult.Updated,
            fixture.repository.updateCurrentPuzzle(
                expectedSessionId = snapshot.sessionId,
                puzzle = generatedFixture.progressPuzzle(),
                movementCount = null
            )
        )
        assertEquals(
            DailySessionProgressUpdateResult.InvalidMovement,
            fixture.repository.updateCurrentPuzzle(
                expectedSessionId = snapshot.sessionId,
                puzzle = generatedFixture.progressPuzzle(),
                movementCount = DailyMovementCount.ZERO
            )
        )
        assertNull(fixture.repository.state.first().activeSession?.movementCount)
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
                puzzle = generatedFixture.progressPuzzle(),
                movementCount = DailyMovementCount(1)
            )
        )
        assertEquals(
            DailySessionProgressUpdateResult.InvalidPuzzle,
            fixture.repository.updateCurrentPuzzle(
                expectedSessionId = snapshot.sessionId,
                puzzle = changedResultPuzzle,
                movementCount = DailyMovementCount(1)
            )
        )
        assertEquals(
            DailySessionProgressUpdateResult.InvalidPuzzle,
            fixture.repository.updateCurrentPuzzle(
                expectedSessionId = snapshot.sessionId,
                puzzle = generatedFixture.generatedPuzzle.solvedPuzzle,
                movementCount = DailyMovementCount(1)
            )
        )
        assertEquals(snapshot, fixture.repository.state.first().activeSession)
    }

    @Test
    fun clear_removes_only_the_owning_daily_session_and_preserves_completions() = runBlocking {
        val fixture = createRepository()
        val snapshot = generatedDailyFixture().snapshot()
        val completion = untimedCompletion(dailyChallengeId(LocalDate.of(2027, 4, 17)))
        fixture.storeAggregate(
            DailyAggregate(
                activeSession = snapshot,
                completions = listOf(completion)
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
        assertEquals(listOf(completion), fixture.repository.state.first().completions)
    }

    @Test
    fun exact_daily_state_survives_repository_and_data_store_recreation() = runBlocking {
        val dataStoreFile = createDataStoreFile()
        val snapshot = generatedDailyFixture().snapshot()
        val completion = untimedCompletion(dailyChallengeId(LocalDate.of(2027, 4, 17)))
        val firstFixture = createRepository(dataStoreFile)
        firstFixture.storeAggregate(
            DailyAggregate(
                activeSession = snapshot,
                completions = listOf(completion)
            )
        )
        firstFixture.close()

        val secondFixture = createRepository(dataStoreFile)

        assertEquals(
            DailyState(
                activeSession = snapshot,
                completions = listOf(completion)
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
        val completion = untimedCompletion(
            identity = snapshot.dailyChallengeId,
            movementCount = DailyMovementCount(1)
        )

        assertEquals(
            DailySessionCompletionResult.Completed(completion),
            fixture.repository.complete(
                expectedSessionId = snapshot.sessionId,
                expectedDailyChallengeId = snapshot.dailyChallengeId,
                solvedPuzzle = generatedFixture.solvedProgressPuzzle(),
                movementCount = DailyMovementCount(1)
            )
        )
        assertEquals(
            DailyState(
                activeSession = null,
                completions = listOf(completion)
            ),
            fixture.repository.state.first()
        )
    }

    @Test
    fun timed_completion_atomically_records_the_exact_elapsed_time_and_is_idempotent() = runBlocking {
        val fixture = createRepository()
        val generatedFixture = generatedDailyFixture()
        val startInstant = DailyTimingStartInstant(1_798_761_600_123)
        val elapsedTime = DailyElapsedTime(91_234)
        val snapshot = generatedFixture.snapshot(timingStartInstant = startInstant)
        val completion = DailyCompletion(
            identity = snapshot.dailyChallengeId,
            elapsedTime = elapsedTime,
            movementCount = DailyMovementCount(7)
        )
        fixture.repository.replaceSession(snapshot)

        assertEquals(
            DailySessionCompletionResult.Completed(completion),
            fixture.repository.complete(
                expectedSessionId = snapshot.sessionId,
                expectedDailyChallengeId = snapshot.dailyChallengeId,
                solvedPuzzle = generatedFixture.solvedProgressPuzzle(),
                movementCount = DailyMovementCount(7),
                elapsedTime = elapsedTime
            )
        )
        assertEquals(
            DailyState(activeSession = null, completions = listOf(completion)),
            fixture.repository.state.first()
        )
        assertEquals(
            DailySessionCompletionResult.AlreadyCompleted(completion),
            fixture.repository.complete(
                expectedSessionId = snapshot.sessionId,
                expectedDailyChallengeId = snapshot.dailyChallengeId,
                solvedPuzzle = generatedFixture.solvedProgressPuzzle(),
                movementCount = DailyMovementCount(999),
                elapsedTime = DailyElapsedTime(999_999)
            )
        )
        assertEquals(listOf(completion), fixture.repository.state.first().completions)
    }

    @Test
    fun completion_rejects_regressed_or_unknown_movement_counts_and_accepts_a_forward_jump() = runBlocking {
        val fixture = createRepository()
        val generatedFixture = generatedDailyFixture()
        val initialSnapshot = generatedFixture.snapshot()
        fixture.repository.replaceSession(initialSnapshot)
        fixture.repository.updateCurrentPuzzle(
            expectedSessionId = initialSnapshot.sessionId,
            puzzle = generatedFixture.progressPuzzle(),
            movementCount = DailyMovementCount(5)
        )

        assertEquals(
            DailySessionCompletionResult.InvalidMovement,
            fixture.repository.complete(
                expectedSessionId = initialSnapshot.sessionId,
                expectedDailyChallengeId = initialSnapshot.dailyChallengeId,
                solvedPuzzle = generatedFixture.solvedProgressPuzzle(),
                movementCount = DailyMovementCount(4)
            )
        )
        assertEquals(
            DailySessionCompletionResult.InvalidMovement,
            fixture.repository.complete(
                expectedSessionId = initialSnapshot.sessionId,
                expectedDailyChallengeId = initialSnapshot.dailyChallengeId,
                solvedPuzzle = generatedFixture.solvedProgressPuzzle(),
                movementCount = null
            )
        )

        val completion = untimedCompletion(
            identity = initialSnapshot.dailyChallengeId,
            movementCount = DailyMovementCount(8)
        )
        assertEquals(
            DailySessionCompletionResult.Completed(completion),
            fixture.repository.complete(
                expectedSessionId = initialSnapshot.sessionId,
                expectedDailyChallengeId = initialSnapshot.dailyChallengeId,
                solvedPuzzle = generatedFixture.solvedProgressPuzzle(),
                movementCount = DailyMovementCount(8)
            )
        )
        assertEquals(listOf(completion), fixture.repository.state.first().completions)
    }

    @Test
    fun migrated_unknown_completion_remains_unknown() = runBlocking {
        val fixture = createRepository()
        val generatedFixture = generatedDailyFixture()
        val snapshot = generatedFixture.snapshot(movementCount = null)
        fixture.storeAggregate(DailyAggregate(activeSession = snapshot))
        val completion = untimedCompletion(snapshot.dailyChallengeId)

        assertEquals(
            DailySessionCompletionResult.Completed(completion),
            fixture.repository.complete(
                expectedSessionId = snapshot.sessionId,
                expectedDailyChallengeId = snapshot.dailyChallengeId,
                solvedPuzzle = generatedFixture.solvedProgressPuzzle(),
                movementCount = null
            )
        )
        assertEquals(listOf(completion), fixture.repository.state.first().completions)
    }

    @Test
    fun completion_rejects_missing_or_unowned_timing_without_mutating_the_session() = runBlocking {
        val fixture = createRepository()
        val generatedFixture = generatedDailyFixture()
        val startedSnapshot = generatedFixture.snapshot(
            timingStartInstant = DailyTimingStartInstant(1_798_761_600_123)
        )
        fixture.repository.replaceSession(startedSnapshot)

        assertEquals(
            DailySessionCompletionResult.InvalidTiming,
            fixture.repository.complete(
                expectedSessionId = startedSnapshot.sessionId,
                expectedDailyChallengeId = startedSnapshot.dailyChallengeId,
                solvedPuzzle = generatedFixture.solvedProgressPuzzle(),
                movementCount = DailyMovementCount(1)
            )
        )
        assertEquals(startedSnapshot, fixture.repository.state.first().activeSession)

        val untimedSnapshot = generatedFixture.snapshot(sessionId = "untimed")
        fixture.repository.replaceSession(untimedSnapshot)
        assertEquals(
            DailySessionCompletionResult.InvalidTiming,
            fixture.repository.complete(
                expectedSessionId = untimedSnapshot.sessionId,
                expectedDailyChallengeId = untimedSnapshot.dailyChallengeId,
                solvedPuzzle = generatedFixture.solvedProgressPuzzle(),
                movementCount = DailyMovementCount(1),
                elapsedTime = DailyElapsedTime(91_234)
            )
        )
        assertEquals(untimedSnapshot, fixture.repository.state.first().activeSession)
        assertTrue(fixture.repository.state.first().completions.isEmpty())
    }

    @Test
    fun exact_repeated_completion_is_idempotent() = runBlocking {
        val fixture = createRepository()
        val generatedFixture = generatedDailyFixture()
        val snapshot = generatedFixture.snapshot()
        val solvedPuzzle = generatedFixture.solvedProgressPuzzle()
        val completion = untimedCompletion(
            identity = snapshot.dailyChallengeId,
            movementCount = DailyMovementCount(1)
        )
        fixture.repository.replaceSession(snapshot)
        fixture.repository.complete(
            expectedSessionId = snapshot.sessionId,
            expectedDailyChallengeId = snapshot.dailyChallengeId,
            solvedPuzzle = solvedPuzzle,
            movementCount = DailyMovementCount(1)
        )

        assertEquals(
            DailySessionCompletionResult.AlreadyCompleted(completion),
            fixture.repository.complete(
                expectedSessionId = snapshot.sessionId,
                expectedDailyChallengeId = snapshot.dailyChallengeId,
                solvedPuzzle = solvedPuzzle,
                movementCount = DailyMovementCount(1)
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
        val existingCompletion = untimedCompletion(
            dailyChallengeId(
                date = LocalDate.of(2027, 4, 18),
                recipeVersion = DailyRecipeVersion("retired-recipe")
            )
        )
        fixture.storeAggregate(
            DailyAggregate(completions = listOf(existingCompletion))
        )
        val currentFixture = generatedDailyFixture(date = existingCompletion.identity.localDate)

        assertEquals(
            DailySessionCompletionResult.AlreadyCompleted(existingCompletion),
            fixture.repository.complete(
                expectedSessionId = DailySessionId("missing"),
                expectedDailyChallengeId = currentFixture.identity,
                solvedPuzzle = currentFixture.solvedProgressPuzzle(),
                movementCount = DailyMovementCount.ZERO
            )
        )
        assertEquals(
            listOf(existingCompletion),
            fixture.repository.state.first().completions
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
                solvedPuzzle = predecessorFixture.solvedProgressPuzzle(),
                movementCount = DailyMovementCount(1)
            )
        )
        assertEquals(
            DailySessionCompletionResult.StaleSession,
            fixture.repository.complete(
                expectedSessionId = successor.sessionId,
                expectedDailyChallengeId = predecessor.dailyChallengeId,
                solvedPuzzle = successorFixture.solvedProgressPuzzle(),
                movementCount = DailyMovementCount(1)
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
                solvedPuzzle = snapshot.currentPuzzle,
                movementCount = DailyMovementCount(1)
            )
        )
        assertEquals(
            DailySessionCompletionResult.InvalidPuzzle,
            fixture.repository.complete(
                expectedSessionId = snapshot.sessionId,
                expectedDailyChallengeId = snapshot.dailyChallengeId,
                solvedPuzzle = inconsistentPuzzle,
                movementCount = DailyMovementCount(1)
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
            solvedPuzzle = generatedFixture.solvedProgressPuzzle(),
            movementCount = DailyMovementCount(1)
        )
        firstFixture.close()

        val secondFixture = createRepository(dataStoreFile)

        assertEquals(
            DailyState(
                activeSession = null,
                completions = listOf(
                    untimedCompletion(
                        identity = snapshot.dailyChallengeId,
                        movementCount = DailyMovementCount(1)
                    )
                )
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
            DailyState(activeSession = null, completions = emptyList()),
            fixture.repository.state.first()
        )

        val replacement = generatedDailyFixture().snapshot()
        assertEquals(
            DailySessionCompletionResult.StaleSession,
            fixture.repository.complete(
                expectedSessionId = replacement.sessionId,
                expectedDailyChallengeId = replacement.dailyChallengeId,
                solvedPuzzle = generatedDailyFixture().solvedProgressPuzzle(),
                movementCount = DailyMovementCount(1)
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
    fun failed_completion_write_keeps_the_active_session_and_exact_retry_publishes_history() {
        val generatedFixture = generatedDailyFixture()
        val activeSession = generatedFixture.snapshot()
        val aggregateKey = byteArrayPreferencesKey(DAILY_AGGREGATE_PREFERENCE_KEY_NAME)
        val storedPreferences = mutablePreferencesOf(
            aggregateKey to DailyAggregateCodec().encode(DailyAggregate(activeSession = activeSession))
        )
        val expectedFailure = IOException("Synthetic Daily completion write failure.")
        val storedState = MutableStateFlow<Preferences>(storedPreferences)
        var failNextWrite = true
        val repository = DataStoreDailySessionRepository(
            dataStore = object : DataStore<Preferences> {
                override val data = storedState

                override suspend fun updateData(transform: suspend (Preferences) -> Preferences): Preferences {
                    if (failNextWrite) {
                        failNextWrite = false
                        throw expectedFailure
                    }
                    return transform(storedState.value).also { updatedPreferences ->
                        storedState.value = updatedPreferences
                    }
                }
            }
        )

        val actualFailure = assertThrows(IOException::class.java) {
            runBlocking {
                repository.complete(
                    expectedSessionId = activeSession.sessionId,
                    expectedDailyChallengeId = activeSession.dailyChallengeId,
                    solvedPuzzle = generatedFixture.solvedProgressPuzzle(),
                    movementCount = DailyMovementCount(1)
                )
            }
        }

        assertSame(expectedFailure, actualFailure)
        assertEquals(
            DailyState(activeSession = activeSession, completions = emptyList()),
            runBlocking { repository.state.first() }
        )

        val completion = untimedCompletion(
            identity = activeSession.dailyChallengeId,
            movementCount = DailyMovementCount(1)
        )
        assertEquals(
            DailySessionCompletionResult.Completed(completion),
            runBlocking {
                repository.complete(
                    expectedSessionId = activeSession.sessionId,
                    expectedDailyChallengeId = activeSession.dailyChallengeId,
                    solvedPuzzle = generatedFixture.solvedProgressPuzzle(),
                    movementCount = DailyMovementCount(1)
                )
            }
        )
        assertEquals(
            DailyState(activeSession = null, completions = listOf(completion)),
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
            solvedPuzzle = generatedDailyFixture().solvedProgressPuzzle(),
            movementCount = DailyMovementCount(1)
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

private fun untimedCompletion(identity: DailyChallengeId, movementCount: DailyMovementCount? = null): DailyCompletion =
    DailyCompletion(
        identity = identity,
        elapsedTime = null,
        movementCount = movementCount
    )
