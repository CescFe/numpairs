package org.cescfe.numpairs.feature.daily

import java.io.IOException
import java.time.LocalDate
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.setMain
import org.cescfe.numpairs.data.daily.session.DailySessionClearResult
import org.cescfe.numpairs.data.daily.session.DailySessionCompletionResult
import org.cescfe.numpairs.data.daily.session.DailySessionId
import org.cescfe.numpairs.data.daily.session.DailySessionProgressUpdateResult
import org.cescfe.numpairs.data.daily.session.DailySessionReplacementResult
import org.cescfe.numpairs.data.daily.session.DailySessionRepository
import org.cescfe.numpairs.data.daily.session.DailySessionSnapshot
import org.cescfe.numpairs.data.daily.session.DailySessionTimingStartResult
import org.cescfe.numpairs.data.daily.session.DailyState
import org.cescfe.numpairs.data.daily.session.GeneratedDailyFixture
import org.cescfe.numpairs.data.daily.session.dailyCompletion
import org.cescfe.numpairs.data.daily.session.generatedDailyFixture
import org.cescfe.numpairs.data.daily.session.requireValidSolvedPuzzle
import org.cescfe.numpairs.domain.daily.DailyChallengeId
import org.cescfe.numpairs.domain.daily.DailyCompletion
import org.cescfe.numpairs.domain.daily.DailyElapsedTime
import org.cescfe.numpairs.domain.daily.DailyMovementCount
import org.cescfe.numpairs.domain.daily.DailyPersonalBestOutcome
import org.cescfe.numpairs.domain.daily.DailyTimingStartInstant
import org.cescfe.numpairs.domain.daily.DeviceLocalDateSource
import org.cescfe.numpairs.domain.generated.generation.GeneratedPairsPuzzleGenerationFailureReason
import org.cescfe.numpairs.domain.generated.generation.GeneratedPairsPuzzleGenerationOutcome
import org.cescfe.numpairs.domain.generated.generation.GeneratedPuzzleGenerationRequest
import org.cescfe.numpairs.domain.puzzle.model.Board
import org.cescfe.numpairs.domain.puzzle.model.Puzzle
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertSame
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class DailyPuzzleViewModelTest {
    private val dispatcher = StandardTestDispatcher()

    @Before
    fun setUp() {
        Dispatchers.setMain(dispatcher)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun exact_session_is_ready_without_generation_id_creation_or_writes() {
        val fixture = generatedDailyFixture()
        val snapshot = fixture.snapshot(currentPuzzle = fixture.progressPuzzle())
        val repository = RecordingDailySessionRepository(
            initialState = DailyState(snapshot, emptyList())
        )
        val generator = RecordingDailyPuzzleGenerator()
        val idSource = QueueDailySessionIdSource("unexpected")
        val viewModel = viewModel(
            date = fixture.identity.localDate,
            repository = repository,
            generator = generator,
            idSource = idSource
        )

        viewModel.onRouteEntered()
        dispatcher.scheduler.advanceUntilIdle()

        val ready = viewModel.uiState.value as DailyPuzzleUiState.Ready
        assertSame(snapshot, ready.session.snapshot)
        assertEquals(0, generator.requestCount)
        assertEquals(0, idSource.requestCount)
        assertTrue(repository.replaceAttempts.isEmpty())
    }

    @Test
    fun same_date_completion_is_published_without_generation_or_writes() {
        val date = LocalDate.of(2027, 4, 18)
        val completion = dailyCompletion(DailyRecipes.FOUR_PAIRS_LOW_V1.identityFor(date))
        val repository = RecordingDailySessionRepository(
            initialState = DailyState(
                activeSession = null,
                completions = listOf(completion)
            )
        )
        val generator = RecordingDailyPuzzleGenerator()
        val viewModel = viewModel(
            date = date,
            repository = repository,
            generator = generator
        )

        viewModel.onRouteEntered()
        dispatcher.scheduler.advanceUntilIdle()

        val completed = viewModel.uiState.value as DailyPuzzleUiState.CompletedToday
        assertEquals(completion, completed.completion)
        assertEquals(0, generator.requestCount)
        assertTrue(repository.replaceAttempts.isEmpty())
    }

    @Test
    fun completed_today_recreates_the_historical_personal_record_without_reading_the_clock() {
        val date = LocalDate.of(2027, 4, 18)
        val currentCompletion = dailyCompletion(
            identity = DailyRecipes.FOUR_PAIRS_LOW_V1.identityFor(date),
            elapsedMilliseconds = 4_000
        )
        val repository = RecordingDailySessionRepository(
            initialState = DailyState(
                activeSession = null,
                completions = listOf(
                    dailyCompletion(
                        identity = DailyRecipes.FOUR_PAIRS_LOW_V1.identityFor(date.minusDays(1)),
                        elapsedMilliseconds = 5_000
                    ),
                    currentCompletion
                )
            )
        )
        val timeSource = MutableDailyTimeSource()
        val viewModel = viewModel(
            date = date,
            repository = repository,
            generator = RecordingDailyPuzzleGenerator(),
            timeSource = timeSource
        )

        viewModel.onRouteEntered()
        dispatcher.scheduler.advanceUntilIdle()

        val completed = viewModel.uiState.value as DailyPuzzleUiState.CompletedToday
        assertEquals(currentCompletion, completed.completion)
        assertEquals(DailyPersonalBestOutcome.PERSONAL_RECORD, completed.personalBestResult.outcome)
        assertEquals(DailyElapsedTime(5_000), completed.personalBestResult.previousBestElapsedTime)
        assertEquals(DailyElapsedTime(4_000), completed.personalBestResult.bestElapsedTime)
        assertEquals(0, timeSource.readCount)
    }

    @Test
    fun generated_successor_is_stored_with_identical_puzzles_before_readiness() {
        val fixture = generatedDailyFixture()
        val writeGate = CompletableDeferred<Unit>()
        val repository = RecordingDailySessionRepository(
            initialState = DailyState(activeSession = null, completions = emptyList()),
            nextReplaceGate = writeGate
        )
        val generator = RecordingDailyPuzzleGenerator(
            generatedResult(fixture.identity.localDate, fixture.generatedPuzzle.initialPuzzle)
        )
        val timeSource = MutableDailyTimeSource(25_000, 900)
        val viewModel = viewModel(
            date = fixture.identity.localDate,
            repository = repository,
            generator = generator,
            idSource = QueueDailySessionIdSource("daily-stable"),
            timeSource = timeSource
        )

        viewModel.onRouteEntered()
        dispatcher.scheduler.runCurrent()

        assertTrue(viewModel.uiState.value is DailyPuzzleUiState.Loading)
        val attemptedSnapshot = repository.replaceAttempts.single()
        assertEquals(DailySessionId("daily-stable"), attemptedSnapshot.sessionId)
        assertEquals(attemptedSnapshot.initialPuzzle, attemptedSnapshot.currentPuzzle)
        assertEquals(fixture.identity, attemptedSnapshot.dailyChallengeId)
        assertSame(null, repository.currentState.activeSession)

        writeGate.complete(Unit)
        dispatcher.scheduler.advanceUntilIdle()

        val ready = viewModel.uiState.value as DailyPuzzleUiState.Ready
        assertEquals(attemptedSnapshot, repository.currentState.activeSession)
        assertEquals(attemptedSnapshot, ready.session.snapshot)
        assertEquals(0, timeSource.readCount)

        viewModel.onPuzzlePresented(ready.session.id)
        dispatcher.scheduler.advanceUntilIdle()

        assertEquals(
            25_000L,
            requireNotNull(repository.currentState.activeSession?.timingStartInstant).epochMilliseconds
        )
        assertEquals(1, repository.timingStartAttempts.size)
    }

    @Test
    fun stale_predecessor_remains_during_failure_then_is_replaced_after_retry_storage() {
        val currentDate = LocalDate.of(2027, 4, 18)
        val staleSnapshot = generatedDailyFixture(
            date = currentDate.minusDays(1)
        ).snapshot()
        val repository = RecordingDailySessionRepository(
            initialState = DailyState(staleSnapshot, emptyList()),
            replaceFailures = ArrayDeque(listOf(IOException("storage unavailable")))
        )
        val generated = generatedResult(
            date = currentDate,
            initialPuzzle = generatedDailyFixture(date = currentDate).generatedPuzzle.initialPuzzle
        )
        val generator = RecordingDailyPuzzleGenerator(generated, generated)
        val idSource = QueueDailySessionIdSource("stable-across-retry")
        val viewModel = viewModel(
            date = currentDate,
            repository = repository,
            generator = generator,
            idSource = idSource
        )

        viewModel.onRouteEntered()
        dispatcher.scheduler.advanceUntilIdle()

        val failed = viewModel.uiState.value as DailyPuzzleUiState.Failed
        assertEquals(DailyPuzzlePreparationFailure.Persistence, failed.failure)
        assertSame(staleSnapshot, repository.currentState.activeSession)

        viewModel.retry()
        viewModel.retry()
        dispatcher.scheduler.advanceUntilIdle()

        val ready = viewModel.uiState.value as DailyPuzzleUiState.Ready
        assertEquals(DailySessionId("stable-across-retry"), ready.session.id)
        assertEquals(1, idSource.requestCount)
        assertEquals(2, generator.requestCount)
        assertEquals(2, repository.replaceAttempts.size)
        assertEquals(ready.session.snapshot, repository.currentState.activeSession)
    }

    @Test
    fun duplicate_entry_does_not_duplicate_generation() {
        val fixture = generatedDailyFixture()
        val generationGate = CompletableDeferred<DailyPuzzleGenerationResult>()
        val generator = RecordingDailyPuzzleGenerator(generationGate)
        val repository = RecordingDailySessionRepository(
            initialState = DailyState(null, emptyList())
        )
        val viewModel = viewModel(
            date = fixture.identity.localDate,
            repository = repository,
            generator = generator
        )

        viewModel.onRouteEntered()
        viewModel.onRouteEntered()
        dispatcher.scheduler.runCurrent()

        assertEquals(1, generator.requestCount)

        generationGate.complete(
            generatedResult(
                date = fixture.identity.localDate,
                initialPuzzle = fixture.generatedPuzzle.initialPuzzle
            )
        )
        dispatcher.scheduler.advanceUntilIdle()
        assertTrue(viewModel.uiState.value is DailyPuzzleUiState.Ready)
    }

    @Test
    fun exhaustion_and_typed_cancellation_keep_the_previous_slot() {
        val date = LocalDate.of(2027, 4, 18)
        val staleSnapshot = generatedDailyFixture(date = date.minusDays(1)).snapshot()
        val repository = RecordingDailySessionRepository(
            initialState = DailyState(staleSnapshot, emptyList())
        )
        val current = currentDailyChallenge(date)
        val generator = RecordingDailyPuzzleGenerator(
            exhaustedResult(current),
            cancelledResult(current)
        )
        val viewModel = viewModel(
            date = date,
            repository = repository,
            generator = generator
        )

        viewModel.onRouteEntered()
        dispatcher.scheduler.advanceUntilIdle()

        val exhausted = viewModel.uiState.value as DailyPuzzleUiState.Failed
        assertTrue(exhausted.failure is DailyPuzzlePreparationFailure.GenerationExhausted)
        assertSame(staleSnapshot, repository.currentState.activeSession)
        assertTrue(repository.replaceAttempts.isEmpty())

        viewModel.retry()
        dispatcher.scheduler.advanceUntilIdle()

        val cancelled = viewModel.uiState.value as DailyPuzzleUiState.Failed
        assertTrue(cancelled.failure is DailyPuzzlePreparationFailure.GenerationCancelled)
        assertSame(staleSnapshot, repository.currentState.activeSession)
        assertTrue(repository.replaceAttempts.isEmpty())
    }

    @Test
    fun route_exit_ignores_a_late_generation_result_and_reentry_captures_the_new_date() {
        var date = LocalDate.of(2027, 4, 18)
        val firstGate = CompletableDeferred<DailyPuzzleGenerationResult>()
        val secondFixture = generatedDailyFixture(date = date.plusDays(1))
        val generator = RecordingDailyPuzzleGenerator(
            firstGate,
            generatedResult(
                date = secondFixture.identity.localDate,
                initialPuzzle = secondFixture.generatedPuzzle.initialPuzzle
            )
        )
        val repository = RecordingDailySessionRepository(
            initialState = DailyState(null, emptyList())
        )
        val viewModel = viewModel(
            dateSource = { date },
            repository = repository,
            generator = generator
        )

        viewModel.onRouteEntered()
        dispatcher.scheduler.runCurrent()
        viewModel.onRouteExited()
        date = date.plusDays(1)
        firstGate.complete(
            generatedResult(
                date = date.minusDays(1),
                initialPuzzle = generatedDailyFixture(date = date.minusDays(1)).generatedPuzzle.initialPuzzle
            )
        )
        dispatcher.scheduler.advanceUntilIdle()

        assertTrue(viewModel.uiState.value is DailyPuzzleUiState.Idle)
        assertTrue(repository.replaceAttempts.isEmpty())

        viewModel.onRouteEntered()
        dispatcher.scheduler.advanceUntilIdle()

        val ready = viewModel.uiState.value as DailyPuzzleUiState.Ready
        assertEquals(date, ready.session.currentDailyChallenge.identity.localDate)
        assertEquals(2, generator.requestCount)
    }

    @Test
    fun route_exit_cancels_pending_successor_storage_before_rollover_reentry() {
        var date = LocalDate.of(2027, 4, 18)
        val staleSnapshot = generatedDailyFixture(date = date.minusDays(1)).snapshot()
        val firstSuccessor = generatedDailyFixture(date = date)
        val secondSuccessor = generatedDailyFixture(date = date.plusDays(1))
        val storageGate = CompletableDeferred<Unit>()
        val repository = RecordingDailySessionRepository(
            initialState = DailyState(staleSnapshot, emptyList()),
            nextReplaceGate = storageGate
        )
        val generator = RecordingDailyPuzzleGenerator(
            generatedResult(
                date = firstSuccessor.identity.localDate,
                initialPuzzle = firstSuccessor.generatedPuzzle.initialPuzzle
            ),
            generatedResult(
                date = secondSuccessor.identity.localDate,
                initialPuzzle = secondSuccessor.generatedPuzzle.initialPuzzle
            )
        )
        val viewModel = viewModel(
            dateSource = { date },
            repository = repository,
            generator = generator,
            idSource = QueueDailySessionIdSource(
                "cancelled-successor",
                "rollover-successor"
            )
        )

        viewModel.onRouteEntered()
        dispatcher.scheduler.runCurrent()

        assertTrue(viewModel.uiState.value is DailyPuzzleUiState.Loading)
        assertEquals(firstSuccessor.identity, repository.replaceAttempts.single().dailyChallengeId)
        assertSame(staleSnapshot, repository.currentState.activeSession)

        viewModel.onRouteExited()
        date = date.plusDays(1)
        storageGate.complete(Unit)
        dispatcher.scheduler.advanceUntilIdle()

        assertTrue(viewModel.uiState.value is DailyPuzzleUiState.Idle)
        assertSame(staleSnapshot, repository.currentState.activeSession)

        viewModel.onRouteEntered()
        dispatcher.scheduler.advanceUntilIdle()

        val ready = viewModel.uiState.value as DailyPuzzleUiState.Ready
        assertEquals(secondSuccessor.identity, ready.session.currentDailyChallenge.identity)
        assertEquals(
            listOf(firstSuccessor.identity, secondSuccessor.identity),
            repository.replaceAttempts.map(DailySessionSnapshot::dailyChallengeId)
        )
        assertEquals(ready.session.snapshot, repository.currentState.activeSession)
    }

    @Test
    fun committed_progress_updates_memory_immediately_and_persists_in_callback_order() {
        val fixture = generatedDailyFixture()
        val firstWriteGate = CompletableDeferred<Unit>()
        val repository = RecordingDailySessionRepository(
            initialState = DailyState(fixture.snapshot(), emptyList()),
            firstUpdateGate = firstWriteGate
        )
        val viewModel = viewModel(
            date = fixture.identity.localDate,
            repository = repository,
            generator = RecordingDailyPuzzleGenerator()
        )
        viewModel.onRouteEntered()
        dispatcher.scheduler.advanceUntilIdle()
        val sessionId = (viewModel.uiState.value as DailyPuzzleUiState.Ready).session.id
        val firstProgress = fixture.progressPuzzle()
        val latestProgress = fixture.laterProgressPuzzle()

        viewModel.onPuzzleMutationCommitted(sessionId, firstProgress)
        dispatcher.scheduler.runCurrent()
        viewModel.onPuzzleMutationCommitted(sessionId, latestProgress)
        viewModel.onPuzzleMutationCommitted(sessionId, latestProgress)
        dispatcher.scheduler.runCurrent()

        val visible = (viewModel.uiState.value as DailyPuzzleUiState.Ready).session
        assertEquals(latestProgress, visible.currentPuzzle)
        assertEquals(2L, requireNotNull(visible.snapshot.movementCount).value)
        assertEquals(fixture.generatedPuzzle.initialPuzzle, repository.currentState.activeSession?.currentPuzzle)
        assertEquals(listOf(firstProgress), repository.updateAttempts.map { attempt -> attempt.puzzle })
        assertEquals(
            listOf(1L),
            repository.updateAttempts.map { attempt -> requireNotNull(attempt.movementCount).value }
        )

        firstWriteGate.complete(Unit)
        dispatcher.scheduler.advanceUntilIdle()

        assertEquals(
            listOf(firstProgress, latestProgress),
            repository.updateAttempts.map { attempt -> attempt.puzzle }
        )
        assertEquals(
            listOf(1L, 2L),
            repository.updateAttempts.map { attempt -> requireNotNull(attempt.movementCount).value }
        )
        assertEquals(latestProgress, repository.currentState.activeSession?.currentPuzzle)
        assertEquals(2L, requireNotNull(repository.currentState.activeSession?.movementCount).value)
    }

    @Test
    fun progress_persistence_retry_reuses_the_accepted_movement_without_incrementing_again() {
        val fixture = generatedDailyFixture()
        val repository = RecordingDailySessionRepository(
            initialState = DailyState(fixture.snapshot(), emptyList()),
            updateFailures = ArrayDeque(listOf(IOException("storage unavailable")))
        )
        val viewModel = viewModel(
            date = fixture.identity.localDate,
            repository = repository,
            generator = RecordingDailyPuzzleGenerator()
        )
        viewModel.onRouteEntered()
        dispatcher.scheduler.advanceUntilIdle()
        val sessionId = (viewModel.uiState.value as DailyPuzzleUiState.Ready).session.id

        viewModel.onPuzzleMutationCommitted(sessionId, fixture.progressPuzzle())
        dispatcher.scheduler.advanceUntilIdle()
        assertEquals(
            DailyPuzzlePersistenceFailure.Persistence,
            (viewModel.uiState.value as DailyPuzzleUiState.Ready).persistenceFailure
        )

        viewModel.retryPersistence()
        dispatcher.scheduler.advanceUntilIdle()

        assertEquals(
            listOf(1L, 1L),
            repository.updateAttempts.map { attempt -> requireNotNull(attempt.movementCount).value }
        )
        val ready = viewModel.uiState.value as DailyPuzzleUiState.Ready
        assertEquals(1L, requireNotNull(ready.session.snapshot.movementCount).value)
        assertEquals(1L, requireNotNull(repository.currentState.activeSession?.movementCount).value)
    }

    @Test
    fun solved_commit_waits_for_prior_progress_and_remains_visible_after_atomic_completion() {
        val fixture = generatedDailyFixture()
        val progressWriteGate = CompletableDeferred<Unit>()
        val repository = RecordingDailySessionRepository(
            initialState = DailyState(fixture.snapshot(), emptyList()),
            firstUpdateGate = progressWriteGate
        )
        val viewModel = viewModel(
            date = fixture.identity.localDate,
            repository = repository,
            generator = RecordingDailyPuzzleGenerator()
        )
        viewModel.onRouteEntered()
        dispatcher.scheduler.advanceUntilIdle()
        val sessionId = (viewModel.uiState.value as DailyPuzzleUiState.Ready).session.id
        val progress = fixture.progressPuzzle()
        val solved = fixture.solvedProgressPuzzle()

        viewModel.onPuzzleMutationCommitted(sessionId, progress)
        dispatcher.scheduler.runCurrent()
        viewModel.onPuzzleMutationCommitted(sessionId, solved)
        dispatcher.scheduler.runCurrent()

        val solving = viewModel.uiState.value as DailyPuzzleUiState.Ready
        assertEquals(solved, solving.session.currentPuzzle)
        assertTrue(repository.completionAttempts.isEmpty())
        assertEquals(fixture.snapshot().dailyChallengeId, solving.session.currentDailyChallenge.identity)

        progressWriteGate.complete(Unit)
        dispatcher.scheduler.advanceUntilIdle()

        val completed = viewModel.uiState.value as DailyPuzzleUiState.Completed
        assertEquals(
            dailyCompletion(
                identity = fixture.identity,
                movementCount = 2
            ),
            (completed.completion as DailyPuzzleCompletion.Completed).completion
        )
        assertEquals(solved, completed.session.currentPuzzle)
        assertEquals(fixture.identity, repository.currentState.completedChallengeIds.single())
        assertSame(null, repository.currentState.activeSession)
        assertEquals(1L, requireNotNull(repository.updateAttempts.single().movementCount).value)
        assertEquals(sessionId, repository.completionAttempts.single().sessionId)
        assertEquals(fixture.identity, repository.completionAttempts.single().identity)
        assertEquals(2L, requireNotNull(repository.completionAttempts.single().movementCount).value)
    }

    @Test
    fun already_completed_response_keeps_the_solved_session_and_exposes_the_existing_identity() {
        val fixture = generatedDailyFixture()
        val repository = RecordingDailySessionRepository(
            initialState = DailyState(fixture.snapshot(), emptyList()),
            completionResults = ArrayDeque(
                listOf(DailySessionCompletionResult.AlreadyCompleted(dailyCompletion(fixture.identity)))
            )
        )
        val viewModel = viewModel(
            date = fixture.identity.localDate,
            repository = repository,
            generator = RecordingDailyPuzzleGenerator()
        )
        viewModel.onRouteEntered()
        dispatcher.scheduler.advanceUntilIdle()
        val sessionId = (viewModel.uiState.value as DailyPuzzleUiState.Ready).session.id

        viewModel.onPuzzleMutationCommitted(sessionId, fixture.solvedProgressPuzzle())
        dispatcher.scheduler.advanceUntilIdle()

        val completed = viewModel.uiState.value as DailyPuzzleUiState.Completed
        assertEquals(
            dailyCompletion(fixture.identity),
            (completed.completion as DailyPuzzleCompletion.AlreadyCompleted).completion
        )
        assertTrue(completed.session.currentPuzzle.isSolved)
    }

    @Test
    fun stale_invalid_and_storage_failures_remain_typed_and_recoverable() {
        val fixture = generatedDailyFixture()
        val repository = RecordingDailySessionRepository(
            initialState = DailyState(fixture.snapshot(), emptyList()),
            updateResults = ArrayDeque(
                listOf(DailySessionProgressUpdateResult.StaleSession)
            ),
            completionResults = ArrayDeque(
                listOf(DailySessionCompletionResult.InvalidPuzzle)
            ),
            completionFailures = ArrayDeque(listOf(IOException("storage unavailable")))
        )
        val viewModel = viewModel(
            date = fixture.identity.localDate,
            repository = repository,
            generator = RecordingDailyPuzzleGenerator()
        )
        viewModel.onRouteEntered()
        dispatcher.scheduler.advanceUntilIdle()
        val sessionId = (viewModel.uiState.value as DailyPuzzleUiState.Ready).session.id

        viewModel.onPuzzleMutationCommitted(sessionId, fixture.progressPuzzle())
        dispatcher.scheduler.advanceUntilIdle()
        assertEquals(
            DailyPuzzlePersistenceFailure.StaleSession,
            (viewModel.uiState.value as DailyPuzzleUiState.Ready).persistenceFailure
        )

        viewModel.onPuzzleMutationCommitted(sessionId, fixture.solvedProgressPuzzle())
        dispatcher.scheduler.advanceUntilIdle()
        assertEquals(
            DailyPuzzlePersistenceFailure.Persistence,
            (viewModel.uiState.value as DailyPuzzleUiState.Ready).persistenceFailure
        )

        viewModel.retryPersistence()
        dispatcher.scheduler.advanceUntilIdle()
        assertEquals(
            DailyPuzzlePersistenceFailure.InvalidPuzzle,
            (viewModel.uiState.value as DailyPuzzleUiState.Ready).persistenceFailure
        )
    }

    @Test
    fun invalid_or_stale_callbacks_cannot_mutate_the_active_daily_session() {
        val fixture = generatedDailyFixture()
        val repository = RecordingDailySessionRepository(
            initialState = DailyState(fixture.snapshot(), emptyList())
        )
        val viewModel = viewModel(
            date = fixture.identity.localDate,
            repository = repository,
            generator = RecordingDailyPuzzleGenerator()
        )
        viewModel.onRouteEntered()
        dispatcher.scheduler.advanceUntilIdle()
        val ready = viewModel.uiState.value as DailyPuzzleUiState.Ready

        viewModel.onPuzzleMutationCommitted(
            expectedSessionId = DailySessionId("stale-callback"),
            puzzle = fixture.progressPuzzle()
        )
        viewModel.onPuzzleMutationCommitted(
            expectedSessionId = ready.session.id,
            puzzle = org.cescfe.numpairs.data.puzzle.seed.samplePuzzle
        )
        dispatcher.scheduler.advanceUntilIdle()

        val unchanged = viewModel.uiState.value as DailyPuzzleUiState.Ready
        assertEquals(fixture.generatedPuzzle.initialPuzzle, unchanged.session.currentPuzzle)
        assertEquals(0L, requireNotNull(unchanged.session.snapshot.movementCount).value)
        assertEquals(DailyPuzzlePersistenceFailure.InvalidPuzzle, unchanged.persistenceFailure)
        assertTrue(repository.updateAttempts.isEmpty())
        assertTrue(repository.completionAttempts.isEmpty())
    }

    @Test
    fun late_progress_and_completion_cannot_mutate_or_complete_a_replacement_session() {
        val fixture = generatedDailyFixture()
        val originalSnapshot = fixture.snapshot(sessionId = "original")
        val replacementSnapshot = fixture.snapshot(sessionId = "replacement")
        val repository = RecordingDailySessionRepository(
            initialState = DailyState(originalSnapshot, emptyList())
        )
        val viewModel = viewModel(
            date = fixture.identity.localDate,
            repository = repository,
            generator = RecordingDailyPuzzleGenerator()
        )
        viewModel.onRouteEntered()
        dispatcher.scheduler.advanceUntilIdle()
        repository.simulateExternalReplacement(replacementSnapshot)

        viewModel.onPuzzleMutationCommitted(
            expectedSessionId = originalSnapshot.sessionId,
            puzzle = fixture.progressPuzzle()
        )
        viewModel.onPuzzleMutationCommitted(
            expectedSessionId = originalSnapshot.sessionId,
            puzzle = fixture.solvedProgressPuzzle()
        )
        dispatcher.scheduler.advanceUntilIdle()

        assertEquals(replacementSnapshot, repository.currentState.activeSession)
        assertEquals(0L, requireNotNull(repository.currentState.activeSession?.movementCount).value)
        assertTrue(repository.currentState.completedChallengeIds.isEmpty())
        assertEquals(
            DailyPuzzlePersistenceFailure.StaleSession,
            (viewModel.uiState.value as DailyPuzzleUiState.Ready).persistenceFailure
        )
        assertEquals(1, repository.updateAttempts.size)
        assertEquals(1, repository.completionAttempts.size)
    }

    @Test
    fun persisted_progress_is_restored_exactly_after_feature_recreation() {
        val fixture = generatedDailyFixture()
        val repository = RecordingDailySessionRepository(
            initialState = DailyState(fixture.snapshot(), emptyList())
        )
        val firstOwner = viewModel(
            date = fixture.identity.localDate,
            repository = repository,
            generator = RecordingDailyPuzzleGenerator()
        )
        firstOwner.onRouteEntered()
        dispatcher.scheduler.advanceUntilIdle()
        val sessionId = (firstOwner.uiState.value as DailyPuzzleUiState.Ready).session.id
        val progress = fixture.progressPuzzle()
        firstOwner.onPuzzleMutationCommitted(sessionId, progress)
        dispatcher.scheduler.advanceUntilIdle()

        val recreatedOwner = viewModel(
            date = fixture.identity.localDate,
            repository = repository,
            generator = RecordingDailyPuzzleGenerator()
        )
        recreatedOwner.onRouteEntered()
        dispatcher.scheduler.advanceUntilIdle()

        val restored = recreatedOwner.uiState.value as DailyPuzzleUiState.Ready
        assertEquals(progress, restored.session.currentPuzzle)
        assertEquals(1L, requireNotNull(restored.session.snapshot.movementCount).value)
        assertTrue(repository.replaceAttempts.isEmpty())
    }

    @Test
    fun migrated_unknown_movement_count_remains_unknown_through_progress_and_completion() {
        val fixture = generatedDailyFixture()
        val repository = RecordingDailySessionRepository(
            initialState = DailyState(
                activeSession = fixture.snapshot(movementCount = null),
                completions = emptyList()
            )
        )
        val viewModel = viewModel(
            date = fixture.identity.localDate,
            repository = repository,
            generator = RecordingDailyPuzzleGenerator()
        )
        viewModel.onRouteEntered()
        dispatcher.scheduler.advanceUntilIdle()
        val sessionId = (viewModel.uiState.value as DailyPuzzleUiState.Ready).session.id

        viewModel.onPuzzleMutationCommitted(sessionId, fixture.progressPuzzle())
        viewModel.onPuzzleMutationCommitted(sessionId, fixture.solvedProgressPuzzle())
        dispatcher.scheduler.advanceUntilIdle()

        assertNull(repository.updateAttempts.single().movementCount)
        assertNull(repository.completionAttempts.single().movementCount)
        val completed = viewModel.uiState.value as DailyPuzzleUiState.Completed
        assertNull(completed.session.snapshot.movementCount)
        assertNull(
            (completed.completion as DailyPuzzleCompletion.Completed)
                .completion
                .movementCount
        )
    }

    @Test
    fun movement_count_overflow_is_reported_without_mutating_or_persisting_progress() {
        val fixture = generatedDailyFixture()
        val repository = RecordingDailySessionRepository(
            initialState = DailyState(
                activeSession = fixture.snapshot(
                    movementCount = DailyMovementCount(Long.MAX_VALUE)
                ),
                completions = emptyList()
            )
        )
        val viewModel = viewModel(
            date = fixture.identity.localDate,
            repository = repository,
            generator = RecordingDailyPuzzleGenerator()
        )
        viewModel.onRouteEntered()
        dispatcher.scheduler.advanceUntilIdle()
        val ready = viewModel.uiState.value as DailyPuzzleUiState.Ready

        viewModel.onPuzzleMutationCommitted(ready.session.id, fixture.progressPuzzle())
        dispatcher.scheduler.advanceUntilIdle()

        val unchanged = viewModel.uiState.value as DailyPuzzleUiState.Ready
        assertEquals(fixture.generatedPuzzle.initialPuzzle, unchanged.session.currentPuzzle)
        assertEquals(Long.MAX_VALUE, requireNotNull(unchanged.session.snapshot.movementCount).value)
        assertEquals(DailyPuzzlePersistenceFailure.InvalidMovement, unchanged.persistenceFailure)
        assertTrue(repository.updateAttempts.isEmpty())
    }

    @Test
    fun first_playable_presentation_starts_and_persists_timing_exactly_once() {
        val fixture = generatedDailyFixture()
        val repository = RecordingDailySessionRepository(
            initialState = DailyState(fixture.snapshot(), emptyList())
        )
        val timeSource = MutableDailyTimeSource(
            epochMilliseconds = 10_000,
            monotonicMilliseconds = 500
        )
        val viewModel = viewModel(
            date = fixture.identity.localDate,
            repository = repository,
            generator = RecordingDailyPuzzleGenerator(),
            timeSource = timeSource
        )
        viewModel.onRouteEntered()
        dispatcher.scheduler.advanceUntilIdle()
        val sessionId = (viewModel.uiState.value as DailyPuzzleUiState.Ready).session.id

        assertTrue(repository.timingStartAttempts.isEmpty())

        viewModel.onPuzzlePresented(sessionId)
        viewModel.onPuzzlePresented(sessionId)
        dispatcher.scheduler.advanceUntilIdle()

        assertEquals(
            listOf(TimingStartAttempt(sessionId, DailyTimingStartInstant(10_000))),
            repository.timingStartAttempts
        )
        assertEquals(
            10_000L,
            requireNotNull(repository.currentState.activeSession?.timingStartInstant).epochMilliseconds
        )
        assertEquals(
            0L,
            requireNotNull((viewModel.uiState.value as DailyPuzzleUiState.Ready).elapsedTime).milliseconds
        )
        assertEquals(0L, requireNotNull(repository.currentState.activeSession?.movementCount).value)
        assertTrue(repository.updateAttempts.isEmpty())
    }

    @Test
    fun restored_timing_uses_wall_clock_once_then_never_moves_backwards_while_visible() {
        val fixture = generatedDailyFixture()
        val snapshot = fixture.snapshot().copy(
            timingStartInstant = DailyTimingStartInstant(1_000)
        )
        val repository = RecordingDailySessionRepository(
            initialState = DailyState(snapshot, emptyList())
        )
        val timeSource = MutableDailyTimeSource(
            epochMilliseconds = 6_000,
            monotonicMilliseconds = 100
        )
        val viewModel = viewModel(
            date = fixture.identity.localDate,
            repository = repository,
            generator = RecordingDailyPuzzleGenerator(),
            timeSource = timeSource
        )
        viewModel.onRouteEntered()
        dispatcher.scheduler.advanceUntilIdle()
        val sessionId = (viewModel.uiState.value as DailyPuzzleUiState.Ready).session.id

        viewModel.onPuzzlePresented(sessionId)
        assertEquals(
            5_000L,
            requireNotNull((viewModel.uiState.value as DailyPuzzleUiState.Ready).elapsedTime).milliseconds
        )

        timeSource.set(epochMilliseconds = 500, monotonicMilliseconds = 2_100)
        viewModel.onTimerRefresh(sessionId)
        assertEquals(
            7_000L,
            requireNotNull((viewModel.uiState.value as DailyPuzzleUiState.Ready).elapsedTime).milliseconds
        )

        timeSource.set(epochMilliseconds = 500, monotonicMilliseconds = 1_000)
        viewModel.onTimerRefresh(sessionId)
        assertEquals(
            7_000L,
            requireNotNull((viewModel.uiState.value as DailyPuzzleUiState.Ready).elapsedTime).milliseconds
        )
        assertTrue(repository.timingStartAttempts.isEmpty())
    }

    @Test
    fun process_recreation_restores_elapsed_time_from_the_persisted_start() {
        val fixture = generatedDailyFixture()
        val repository = RecordingDailySessionRepository(
            initialState = DailyState(
                fixture.snapshot().copy(
                    timingStartInstant = DailyTimingStartInstant(1_000)
                ),
                emptyList()
            )
        )
        val firstOwner = viewModel(
            date = fixture.identity.localDate,
            repository = repository,
            generator = RecordingDailyPuzzleGenerator(),
            timeSource = MutableDailyTimeSource(4_000, 800)
        )
        firstOwner.onRouteEntered()
        dispatcher.scheduler.advanceUntilIdle()
        val firstSessionId = (firstOwner.uiState.value as DailyPuzzleUiState.Ready).session.id
        firstOwner.onPuzzlePresented(firstSessionId)
        assertEquals(
            3_000L,
            requireNotNull((firstOwner.uiState.value as DailyPuzzleUiState.Ready).elapsedTime).milliseconds
        )

        val recreatedOwner = viewModel(
            date = fixture.identity.localDate,
            repository = repository,
            generator = RecordingDailyPuzzleGenerator(),
            timeSource = MutableDailyTimeSource(9_000, 100)
        )
        recreatedOwner.onRouteEntered()
        dispatcher.scheduler.advanceUntilIdle()
        val recreatedSessionId = (recreatedOwner.uiState.value as DailyPuzzleUiState.Ready).session.id
        recreatedOwner.onPuzzlePresented(recreatedSessionId)

        assertEquals(
            8_000L,
            requireNotNull((recreatedOwner.uiState.value as DailyPuzzleUiState.Ready).elapsedTime).milliseconds
        )
        assertTrue(repository.timingStartAttempts.isEmpty())
    }

    @Test
    fun route_exit_and_reentry_continue_the_same_timer_without_another_start() {
        val fixture = generatedDailyFixture()
        val repository = RecordingDailySessionRepository(
            initialState = DailyState(fixture.snapshot(), emptyList())
        )
        val timeSource = MutableDailyTimeSource(10_000, 1_000)
        val viewModel = viewModel(
            date = fixture.identity.localDate,
            repository = repository,
            generator = RecordingDailyPuzzleGenerator(),
            timeSource = timeSource
        )
        viewModel.onRouteEntered()
        dispatcher.scheduler.advanceUntilIdle()
        val sessionId = (viewModel.uiState.value as DailyPuzzleUiState.Ready).session.id
        viewModel.onPuzzlePresented(sessionId)
        dispatcher.scheduler.advanceUntilIdle()

        timeSource.set(epochMilliseconds = 15_000, monotonicMilliseconds = 6_000)
        viewModel.onTimerRefresh(sessionId)
        assertEquals(
            5_000L,
            requireNotNull((viewModel.uiState.value as DailyPuzzleUiState.Ready).elapsedTime).milliseconds
        )

        viewModel.onRouteExited()
        timeSource.set(epochMilliseconds = 20_000, monotonicMilliseconds = 11_000)
        viewModel.onRouteEntered()
        dispatcher.scheduler.advanceUntilIdle()
        val restoredSessionId = (viewModel.uiState.value as DailyPuzzleUiState.Ready).session.id
        viewModel.onPuzzlePresented(restoredSessionId)

        assertEquals(sessionId, restoredSessionId)
        assertEquals(
            10_000L,
            requireNotNull((viewModel.uiState.value as DailyPuzzleUiState.Ready).elapsedTime).milliseconds
        )
        assertEquals(1, repository.timingStartAttempts.size)
    }

    @Test
    fun failed_timing_start_retry_reuses_the_original_presentation_instant() {
        val fixture = generatedDailyFixture()
        val repository = RecordingDailySessionRepository(
            initialState = DailyState(fixture.snapshot(), emptyList()),
            startTimingFailures = ArrayDeque(listOf(IOException("storage unavailable")))
        )
        val timeSource = MutableDailyTimeSource(10_000, 500)
        val viewModel = viewModel(
            date = fixture.identity.localDate,
            repository = repository,
            generator = RecordingDailyPuzzleGenerator(),
            timeSource = timeSource
        )
        viewModel.onRouteEntered()
        dispatcher.scheduler.advanceUntilIdle()
        val sessionId = (viewModel.uiState.value as DailyPuzzleUiState.Ready).session.id

        viewModel.onPuzzlePresented(sessionId)
        dispatcher.scheduler.advanceUntilIdle()
        assertEquals(
            DailyPuzzlePersistenceFailure.Persistence,
            (viewModel.uiState.value as DailyPuzzleUiState.Ready).persistenceFailure
        )

        timeSource.set(epochMilliseconds = 20_000, monotonicMilliseconds = 10_500)
        viewModel.retryPersistence()
        dispatcher.scheduler.advanceUntilIdle()

        assertEquals(
            listOf(
                TimingStartAttempt(sessionId, DailyTimingStartInstant(10_000)),
                TimingStartAttempt(sessionId, DailyTimingStartInstant(10_000))
            ),
            repository.timingStartAttempts
        )
        assertEquals(
            10_000L,
            requireNotNull(repository.currentState.activeSession?.timingStartInstant).epochMilliseconds
        )
    }

    @Test
    fun solved_transition_freezes_elapsed_time_before_completion_io() {
        val fixture = generatedDailyFixture()
        val completionGate = CompletableDeferred<Unit>()
        val repository = RecordingDailySessionRepository(
            initialState = DailyState(fixture.snapshot(), emptyList()),
            nextCompletionGate = completionGate
        )
        val timeSource = MutableDailyTimeSource(10_000, 500)
        val viewModel = viewModel(
            date = fixture.identity.localDate,
            repository = repository,
            generator = RecordingDailyPuzzleGenerator(),
            timeSource = timeSource
        )
        viewModel.onRouteEntered()
        dispatcher.scheduler.advanceUntilIdle()
        val sessionId = (viewModel.uiState.value as DailyPuzzleUiState.Ready).session.id
        viewModel.onPuzzlePresented(sessionId)
        dispatcher.scheduler.advanceUntilIdle()

        timeSource.set(epochMilliseconds = 11_234, monotonicMilliseconds = 1_734)
        viewModel.onPuzzleMutationCommitted(sessionId, fixture.solvedProgressPuzzle())
        timeSource.set(epochMilliseconds = 15_000, monotonicMilliseconds = 5_500)
        dispatcher.scheduler.runCurrent()

        assertEquals(
            1_234L,
            requireNotNull((viewModel.uiState.value as DailyPuzzleUiState.Ready).elapsedTime).milliseconds
        )
        assertEquals(1_234L, requireNotNull(repository.completionAttempts.single().elapsedTime).milliseconds)
        assertEquals(1L, requireNotNull(repository.completionAttempts.single().movementCount).value)
        val frozenPersonalBest = requireNotNull(
            (viewModel.uiState.value as DailyPuzzleUiState.Ready).personalBestResult
        )
        assertEquals(DailyPersonalBestOutcome.BASELINE, frozenPersonalBest.outcome)
        assertEquals(DailyElapsedTime(1_234), frozenPersonalBest.bestElapsedTime)

        timeSource.set(epochMilliseconds = 99_000, monotonicMilliseconds = 90_000)
        completionGate.complete(Unit)
        dispatcher.scheduler.advanceUntilIdle()

        val completed = viewModel.uiState.value as DailyPuzzleUiState.Completed
        val completedResult = completed.completion as DailyPuzzleCompletion.Completed
        assertEquals(
            1_234L,
            requireNotNull(completedResult.completion.elapsedTime).milliseconds
        )
        assertEquals(
            frozenPersonalBest,
            completedResult.personalBestResult
        )
    }

    @Test
    fun completion_persistence_retry_reuses_the_frozen_elapsed_time() {
        val fixture = generatedDailyFixture()
        val repository = RecordingDailySessionRepository(
            initialState = DailyState(
                activeSession = fixture.snapshot(),
                completions = listOf(
                    dailyCompletion(
                        identity = DailyRecipes.FOUR_PAIRS_LOW_V1.identityFor(
                            fixture.identity.localDate.minusDays(1)
                        ),
                        elapsedMilliseconds = 5_000
                    )
                )
            ),
            completionFailures = ArrayDeque(listOf(IOException("storage unavailable")))
        )
        val timeSource = MutableDailyTimeSource(20_000, 1_000)
        val viewModel = viewModel(
            date = fixture.identity.localDate,
            repository = repository,
            generator = RecordingDailyPuzzleGenerator(),
            timeSource = timeSource
        )
        viewModel.onRouteEntered()
        dispatcher.scheduler.advanceUntilIdle()
        val sessionId = (viewModel.uiState.value as DailyPuzzleUiState.Ready).session.id
        viewModel.onPuzzlePresented(sessionId)
        dispatcher.scheduler.advanceUntilIdle()

        timeSource.set(epochMilliseconds = 23_456, monotonicMilliseconds = 4_456)
        viewModel.onPuzzleMutationCommitted(sessionId, fixture.solvedProgressPuzzle())
        dispatcher.scheduler.advanceUntilIdle()
        assertEquals(
            DailyPuzzlePersistenceFailure.Persistence,
            (viewModel.uiState.value as DailyPuzzleUiState.Ready).persistenceFailure
        )
        val frozenPersonalBest = requireNotNull(
            (viewModel.uiState.value as DailyPuzzleUiState.Ready).personalBestResult
        )
        assertEquals(DailyPersonalBestOutcome.PERSONAL_RECORD, frozenPersonalBest.outcome)
        assertEquals(DailyElapsedTime(5_000), frozenPersonalBest.previousBestElapsedTime)
        assertEquals(DailyElapsedTime(3_456), frozenPersonalBest.bestElapsedTime)

        timeSource.set(epochMilliseconds = 40_000, monotonicMilliseconds = 21_000)
        viewModel.retryPersistence()
        dispatcher.scheduler.advanceUntilIdle()

        assertEquals(
            listOf(DailyElapsedTime(3_456), DailyElapsedTime(3_456)),
            repository.completionAttempts.map(CompletionAttempt::elapsedTime)
        )
        assertEquals(
            listOf(1L, 1L),
            repository.completionAttempts.map { attempt -> requireNotNull(attempt.movementCount).value }
        )
        val completed = viewModel.uiState.value as DailyPuzzleUiState.Completed
        val completedResult = completed.completion as DailyPuzzleCompletion.Completed
        assertEquals(
            3_456L,
            requireNotNull(completedResult.completion.elapsedTime).milliseconds
        )
        assertEquals(
            frozenPersonalBest,
            completedResult.personalBestResult
        )
    }

    @Test
    fun a_session_opened_before_midnight_completes_its_captured_daily_identity() {
        var currentDate = LocalDate.of(2027, 4, 18)
        val fixture = generatedDailyFixture(date = currentDate)
        val repository = RecordingDailySessionRepository(
            initialState = DailyState(fixture.snapshot(), emptyList())
        )
        val viewModel = viewModel(
            dateSource = { currentDate },
            repository = repository,
            generator = RecordingDailyPuzzleGenerator()
        )
        viewModel.onRouteEntered()
        dispatcher.scheduler.advanceUntilIdle()
        val session = (viewModel.uiState.value as DailyPuzzleUiState.Ready).session
        currentDate = currentDate.plusDays(1)

        viewModel.onPuzzleMutationCommitted(session.id, fixture.solvedProgressPuzzle())
        dispatcher.scheduler.advanceUntilIdle()

        assertEquals(fixture.identity, repository.completionAttempts.single().identity)
        assertEquals(
            fixture.identity,
            (viewModel.uiState.value as DailyPuzzleUiState.Completed)
                .session
                .currentDailyChallenge
                .identity
        )
    }
}

private fun viewModel(
    date: LocalDate,
    repository: DailySessionRepository,
    generator: DailyPuzzleGenerator,
    idSource: DailySessionIdSource = QueueDailySessionIdSource("daily-session"),
    timeSource: DailyTimeSource = MutableDailyTimeSource()
): DailyPuzzleViewModel = viewModel(
    dateSource = { date },
    repository = repository,
    generator = generator,
    idSource = idSource,
    timeSource = timeSource
)

private fun viewModel(
    dateSource: DeviceLocalDateSource,
    repository: DailySessionRepository,
    generator: DailyPuzzleGenerator,
    idSource: DailySessionIdSource = QueueDailySessionIdSource("daily-session"),
    timeSource: DailyTimeSource = MutableDailyTimeSource()
): DailyPuzzleViewModel = DailyPuzzleViewModel(
    availabilityResolver = CurrentDailyAvailabilityResolver(
        currentDailyChallengeResolver = CurrentDailyChallengeResolver(
            localDateSource = dateSource,
            activeRecipeVersion = DailyRecipes.FOUR_PAIRS_LOW_V1.version
        ),
        dailySessionRepository = repository
    ),
    puzzleGenerator = generator,
    dailySessionRepository = repository,
    sessionIdSource = idSource,
    timeSource = timeSource
)

private fun currentDailyChallenge(date: LocalDate): CurrentDailyChallenge = CurrentDailyChallenge(
    identity = DailyRecipes.FOUR_PAIRS_LOW_V1.identityFor(date),
    recipe = DailyRecipes.FOUR_PAIRS_LOW_V1
)

private fun generatedResult(date: LocalDate, initialPuzzle: Puzzle): DailyPuzzleGenerationResult.Generated {
    val current = currentDailyChallenge(date)
    val candidateIndex = current.recipe.candidateIndices.first()
    return DailyPuzzleGenerationResult.Generated(
        currentDailyChallenge = current,
        candidateIndex = candidateIndex,
        seed = current.recipe.seedFor(current.identity, candidateIndex),
        initialPuzzle = initialPuzzle
    )
}

private fun GeneratedDailyFixture.laterProgressPuzzle(): Puzzle {
    val firstProgress = progressPuzzle()
    return firstProgress.copy(
        board = Board(
            tiles = firstProgress.board.tiles.mapIndexed { index, tile ->
                if (index == 1) generatedPuzzle.solvedPuzzle.board.tiles[index] else tile
            }
        )
    )
}

private fun exhaustedResult(current: CurrentDailyChallenge): DailyPuzzleGenerationResult.Exhausted =
    DailyPuzzleGenerationResult.Exhausted(
        currentDailyChallenge = current,
        attemptedFailures = current.recipe.candidateIndices.map { candidateIndex ->
            candidateFailure(
                current = current,
                candidateIndex = candidateIndex,
                reason = GeneratedPairsPuzzleGenerationFailureReason.AttemptsExhausted
            )
        }
    )

private fun cancelledResult(current: CurrentDailyChallenge): DailyPuzzleGenerationResult.Cancelled =
    DailyPuzzleGenerationResult.Cancelled(
        currentDailyChallenge = current,
        attemptedFailures = listOf(
            candidateFailure(
                current = current,
                candidateIndex = current.recipe.candidateIndices.first(),
                reason = GeneratedPairsPuzzleGenerationFailureReason.Cancelled
            )
        )
    )

private fun candidateFailure(
    current: CurrentDailyChallenge,
    candidateIndex: org.cescfe.numpairs.domain.daily.DailyCandidateIndex,
    reason: GeneratedPairsPuzzleGenerationFailureReason
): DailyCandidateGenerationFailure {
    val seed = current.recipe.seedFor(current.identity, candidateIndex)
    val request = GeneratedPuzzleGenerationRequest(
        profile = current.challenge.profile,
        seed = seed
    )
    return DailyCandidateGenerationFailure(
        candidateIndex = candidateIndex,
        seed = seed,
        failure = GeneratedPairsPuzzleGenerationOutcome.Failed(
            request = request,
            attemptsUsed = 1,
            searchWorkConsumed = 1,
            reason = reason,
            candidateRejections = emptyList()
        )
    )
}

private class QueueDailySessionIdSource(vararg ids: String) : DailySessionIdSource {
    private val remainingIds = ArrayDeque(ids.toList())

    var requestCount: Int = 0
        private set

    override fun nextId(): DailySessionId {
        requestCount += 1
        return DailySessionId(remainingIds.removeFirst())
    }
}

private class MutableDailyTimeSource(epochMilliseconds: Long = 1_000, monotonicMilliseconds: Long = 1_000) :
    DailyTimeSource {
    private var reading = DailyTimeReading(
        epochMilliseconds = epochMilliseconds,
        monotonicMilliseconds = monotonicMilliseconds
    )

    var readCount: Int = 0
        private set

    override fun read(): DailyTimeReading {
        readCount += 1
        return reading
    }

    fun set(epochMilliseconds: Long, monotonicMilliseconds: Long) {
        reading = DailyTimeReading(
            epochMilliseconds = epochMilliseconds,
            monotonicMilliseconds = monotonicMilliseconds
        )
    }
}

private class RecordingDailyPuzzleGenerator(vararg outcomes: Any) : DailyPuzzleGenerator {
    private val remainingOutcomes = ArrayDeque(outcomes.toList())

    var requestCount: Int = 0
        private set

    override suspend fun generate(currentDailyChallenge: CurrentDailyChallenge): DailyPuzzleGenerationResult {
        requestCount += 1
        return when (val outcome = remainingOutcomes.removeFirst()) {
            is DailyPuzzleGenerationResult -> outcome

            is CompletableDeferred<*> -> {
                @Suppress("UNCHECKED_CAST")
                (outcome as CompletableDeferred<DailyPuzzleGenerationResult>).await()
            }

            else -> error("Unsupported Daily generation test outcome.")
        }
    }
}

private class RecordingDailySessionRepository(
    initialState: DailyState,
    var nextReplaceGate: CompletableDeferred<Unit>? = null,
    private val replaceFailures: ArrayDeque<IOException> = ArrayDeque(),
    firstUpdateGate: CompletableDeferred<Unit>? = null,
    private val updateResults: ArrayDeque<DailySessionProgressUpdateResult> = ArrayDeque(),
    private val updateFailures: ArrayDeque<IOException> = ArrayDeque(),
    private val startTimingFailures: ArrayDeque<IOException> = ArrayDeque(),
    private val completionResults: ArrayDeque<DailySessionCompletionResult> = ArrayDeque(),
    private val completionFailures: ArrayDeque<IOException> = ArrayDeque(),
    var nextCompletionGate: CompletableDeferred<Unit>? = null
) : DailySessionRepository {
    private val mutableState = MutableStateFlow(initialState)
    private var pendingUpdateGate = firstUpdateGate
    override val state = mutableState

    val currentState: DailyState
        get() = mutableState.value

    val replaceAttempts = mutableListOf<DailySessionSnapshot>()
    val timingStartAttempts = mutableListOf<TimingStartAttempt>()
    val updateAttempts = mutableListOf<ProgressAttempt>()
    val completionAttempts = mutableListOf<CompletionAttempt>()

    fun simulateExternalReplacement(snapshot: DailySessionSnapshot) {
        mutableState.value = mutableState.value.copy(activeSession = snapshot)
    }

    override suspend fun replaceSession(snapshot: DailySessionSnapshot): DailySessionReplacementResult {
        replaceAttempts += snapshot
        nextReplaceGate?.also { gate ->
            nextReplaceGate = null
            gate.await()
        }
        if (replaceFailures.isNotEmpty()) {
            throw replaceFailures.removeFirst()
        }
        val completion = mutableState.value.completions.singleOrNull { completed ->
            completed.identity.localDate == snapshot.dailyChallengeId.localDate
        }
        if (completion != null) {
            return DailySessionReplacementResult.DateAlreadyCompleted(completion)
        }
        mutableState.value = mutableState.value.copy(activeSession = snapshot)
        return DailySessionReplacementResult.Replaced
    }

    override suspend fun updateCurrentPuzzle(
        expectedSessionId: DailySessionId,
        puzzle: Puzzle,
        movementCount: DailyMovementCount?
    ): DailySessionProgressUpdateResult {
        updateAttempts += ProgressAttempt(
            sessionId = expectedSessionId,
            puzzle = puzzle,
            movementCount = movementCount
        )
        pendingUpdateGate?.also { gate ->
            pendingUpdateGate = null
            gate.await()
        }
        if (updateFailures.isNotEmpty()) {
            throw updateFailures.removeFirst()
        }
        if (updateResults.isNotEmpty()) {
            return updateResults.removeFirst()
        }
        val activeSession = mutableState.value.activeSession
        if (activeSession?.sessionId != expectedSessionId) {
            return DailySessionProgressUpdateResult.StaleSession
        }
        val updatedSession = try {
            activeSession.copy(
                currentPuzzle = puzzle,
                movementCount = movementCount
            )
        } catch (_: IllegalArgumentException) {
            return DailySessionProgressUpdateResult.InvalidPuzzle
        } catch (_: IllegalStateException) {
            return DailySessionProgressUpdateResult.InvalidPuzzle
        }
        mutableState.value = mutableState.value.copy(activeSession = updatedSession)
        return DailySessionProgressUpdateResult.Updated
    }

    override suspend fun startTiming(
        expectedSessionId: DailySessionId,
        startInstant: DailyTimingStartInstant
    ): DailySessionTimingStartResult {
        timingStartAttempts += TimingStartAttempt(expectedSessionId, startInstant)
        if (startTimingFailures.isNotEmpty()) {
            throw startTimingFailures.removeFirst()
        }
        val activeSession = mutableState.value.activeSession
        if (activeSession?.sessionId != expectedSessionId) {
            return DailySessionTimingStartResult.StaleSession
        }
        val existingStart = activeSession.timingStartInstant
        if (existingStart != null) {
            return DailySessionTimingStartResult.AlreadyStarted(existingStart)
        }
        mutableState.value = mutableState.value.copy(
            activeSession = activeSession.copy(timingStartInstant = startInstant)
        )
        return DailySessionTimingStartResult.Started(startInstant)
    }

    override suspend fun clearSession(expectedSessionId: DailySessionId): DailySessionClearResult =
        DailySessionClearResult.StaleSession

    override suspend fun complete(
        expectedSessionId: DailySessionId,
        expectedDailyChallengeId: DailyChallengeId,
        solvedPuzzle: Puzzle,
        movementCount: DailyMovementCount?,
        elapsedTime: DailyElapsedTime?
    ): DailySessionCompletionResult {
        completionAttempts += CompletionAttempt(
            sessionId = expectedSessionId,
            identity = expectedDailyChallengeId,
            solvedPuzzle = solvedPuzzle,
            movementCount = movementCount,
            elapsedTime = elapsedTime
        )
        nextCompletionGate?.also { gate ->
            nextCompletionGate = null
            gate.await()
        }
        if (completionFailures.isNotEmpty()) {
            throw completionFailures.removeFirst()
        }
        if (completionResults.isNotEmpty()) {
            return completionResults.removeFirst()
        }
        val completion = mutableState.value.completions.singleOrNull { completed ->
            completed.identity.localDate == expectedDailyChallengeId.localDate
        }
        if (completion != null) {
            return DailySessionCompletionResult.AlreadyCompleted(completion)
        }
        val activeSession = mutableState.value.activeSession
        if (
            activeSession?.sessionId != expectedSessionId ||
            activeSession.dailyChallengeId != expectedDailyChallengeId
        ) {
            return DailySessionCompletionResult.StaleSession
        }
        try {
            activeSession.requireValidSolvedPuzzle(solvedPuzzle)
        } catch (_: IllegalArgumentException) {
            return DailySessionCompletionResult.InvalidPuzzle
        } catch (_: IllegalStateException) {
            return DailySessionCompletionResult.InvalidPuzzle
        }
        if ((activeSession.timingStartInstant == null) != (elapsedTime == null)) {
            return DailySessionCompletionResult.InvalidTiming
        }
        val completed = DailyCompletion(
            identity = expectedDailyChallengeId,
            elapsedTime = elapsedTime,
            movementCount = movementCount
        )
        mutableState.value = DailyState(
            activeSession = null,
            completions = mutableState.value.completions + completed
        )
        return DailySessionCompletionResult.Completed(completed)
    }
}

private data class ProgressAttempt(
    val sessionId: DailySessionId,
    val puzzle: Puzzle,
    val movementCount: DailyMovementCount?
)

private data class TimingStartAttempt(val sessionId: DailySessionId, val startInstant: DailyTimingStartInstant)

private data class CompletionAttempt(
    val sessionId: DailySessionId,
    val identity: DailyChallengeId,
    val solvedPuzzle: Puzzle,
    val movementCount: DailyMovementCount?,
    val elapsedTime: DailyElapsedTime?
)
