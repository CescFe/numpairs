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
import org.cescfe.numpairs.data.daily.session.DailyState
import org.cescfe.numpairs.data.daily.session.GeneratedDailyFixture
import org.cescfe.numpairs.data.daily.session.generatedDailyFixture
import org.cescfe.numpairs.data.daily.session.requireValidSolvedPuzzle
import org.cescfe.numpairs.domain.daily.DailyChallengeId
import org.cescfe.numpairs.domain.daily.DeviceLocalDateSource
import org.cescfe.numpairs.domain.generated.generation.GeneratedPairsPuzzleGenerationFailureReason
import org.cescfe.numpairs.domain.generated.generation.GeneratedPairsPuzzleGenerationOutcome
import org.cescfe.numpairs.domain.generated.generation.GeneratedPuzzleGenerationRequest
import org.cescfe.numpairs.domain.puzzle.model.Board
import org.cescfe.numpairs.domain.puzzle.model.Puzzle
import org.junit.After
import org.junit.Assert.assertEquals
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
        val completion = DailyRecipes.FOUR_PAIRS_LOW_V1.identityFor(date)
        val repository = RecordingDailySessionRepository(
            initialState = DailyState(
                activeSession = null,
                completedChallengeIds = listOf(completion)
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
    fun generated_successor_is_stored_with_identical_puzzles_before_readiness() {
        val fixture = generatedDailyFixture()
        val writeGate = CompletableDeferred<Unit>()
        val repository = RecordingDailySessionRepository(
            initialState = DailyState(activeSession = null, completedChallengeIds = emptyList()),
            nextReplaceGate = writeGate
        )
        val generator = RecordingDailyPuzzleGenerator(
            generatedResult(fixture.identity.localDate, fixture.generatedPuzzle.initialPuzzle)
        )
        val viewModel = viewModel(
            date = fixture.identity.localDate,
            repository = repository,
            generator = generator,
            idSource = QueueDailySessionIdSource("daily-stable")
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
            dateSource = DeviceLocalDateSource { date },
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
            dateSource = DeviceLocalDateSource { date },
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

        viewModel.onCommittedPuzzleChanged(sessionId, firstProgress)
        dispatcher.scheduler.runCurrent()
        viewModel.onCommittedPuzzleChanged(sessionId, latestProgress)
        viewModel.onCommittedPuzzleChanged(sessionId, latestProgress)
        dispatcher.scheduler.runCurrent()

        val visible = (viewModel.uiState.value as DailyPuzzleUiState.Ready).session
        assertEquals(latestProgress, visible.currentPuzzle)
        assertEquals(fixture.generatedPuzzle.initialPuzzle, repository.currentState.activeSession?.currentPuzzle)
        assertEquals(listOf(firstProgress), repository.updateAttempts.map { attempt -> attempt.puzzle })

        firstWriteGate.complete(Unit)
        dispatcher.scheduler.advanceUntilIdle()

        assertEquals(
            listOf(firstProgress, latestProgress),
            repository.updateAttempts.map { attempt -> attempt.puzzle }
        )
        assertEquals(latestProgress, repository.currentState.activeSession?.currentPuzzle)
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

        viewModel.onCommittedPuzzleChanged(sessionId, progress)
        dispatcher.scheduler.runCurrent()
        viewModel.onCommittedPuzzleChanged(sessionId, solved)
        dispatcher.scheduler.runCurrent()

        val solving = viewModel.uiState.value as DailyPuzzleUiState.Ready
        assertEquals(solved, solving.session.currentPuzzle)
        assertTrue(repository.completionAttempts.isEmpty())
        assertEquals(fixture.snapshot().dailyChallengeId, solving.session.currentDailyChallenge.identity)

        progressWriteGate.complete(Unit)
        dispatcher.scheduler.advanceUntilIdle()

        val completed = viewModel.uiState.value as DailyPuzzleUiState.Completed
        assertEquals(DailyPuzzleCompletion.Completed, completed.completion)
        assertEquals(solved, completed.session.currentPuzzle)
        assertEquals(fixture.identity, repository.currentState.completedChallengeIds.single())
        assertSame(null, repository.currentState.activeSession)
        assertEquals(sessionId, repository.completionAttempts.single().sessionId)
        assertEquals(fixture.identity, repository.completionAttempts.single().identity)
    }

    @Test
    fun already_completed_response_keeps_the_solved_session_and_exposes_the_existing_identity() {
        val fixture = generatedDailyFixture()
        val repository = RecordingDailySessionRepository(
            initialState = DailyState(fixture.snapshot(), emptyList()),
            completionResults = ArrayDeque(
                listOf(DailySessionCompletionResult.AlreadyCompleted(fixture.identity))
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

        viewModel.onCommittedPuzzleChanged(sessionId, fixture.solvedProgressPuzzle())
        dispatcher.scheduler.advanceUntilIdle()

        val completed = viewModel.uiState.value as DailyPuzzleUiState.Completed
        assertEquals(
            DailyPuzzleCompletion.AlreadyCompleted(fixture.identity),
            completed.completion
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

        viewModel.onCommittedPuzzleChanged(sessionId, fixture.progressPuzzle())
        dispatcher.scheduler.advanceUntilIdle()
        assertEquals(
            DailyPuzzlePersistenceFailure.StaleSession,
            (viewModel.uiState.value as DailyPuzzleUiState.Ready).persistenceFailure
        )

        viewModel.onCommittedPuzzleChanged(sessionId, fixture.solvedProgressPuzzle())
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

        viewModel.onCommittedPuzzleChanged(
            expectedSessionId = DailySessionId("stale-callback"),
            puzzle = fixture.progressPuzzle()
        )
        viewModel.onCommittedPuzzleChanged(
            expectedSessionId = ready.session.id,
            puzzle = org.cescfe.numpairs.data.puzzle.seed.samplePuzzle
        )
        dispatcher.scheduler.advanceUntilIdle()

        val unchanged = viewModel.uiState.value as DailyPuzzleUiState.Ready
        assertEquals(fixture.generatedPuzzle.initialPuzzle, unchanged.session.currentPuzzle)
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

        viewModel.onCommittedPuzzleChanged(
            expectedSessionId = originalSnapshot.sessionId,
            puzzle = fixture.progressPuzzle()
        )
        viewModel.onCommittedPuzzleChanged(
            expectedSessionId = originalSnapshot.sessionId,
            puzzle = fixture.solvedProgressPuzzle()
        )
        dispatcher.scheduler.advanceUntilIdle()

        assertEquals(replacementSnapshot, repository.currentState.activeSession)
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
        firstOwner.onCommittedPuzzleChanged(sessionId, progress)
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
        assertTrue(repository.replaceAttempts.isEmpty())
    }

    @Test
    fun a_session_opened_before_midnight_completes_its_captured_daily_identity() {
        var currentDate = LocalDate.of(2027, 4, 18)
        val fixture = generatedDailyFixture(date = currentDate)
        val repository = RecordingDailySessionRepository(
            initialState = DailyState(fixture.snapshot(), emptyList())
        )
        val viewModel = viewModel(
            dateSource = DeviceLocalDateSource { currentDate },
            repository = repository,
            generator = RecordingDailyPuzzleGenerator()
        )
        viewModel.onRouteEntered()
        dispatcher.scheduler.advanceUntilIdle()
        val session = (viewModel.uiState.value as DailyPuzzleUiState.Ready).session
        currentDate = currentDate.plusDays(1)

        viewModel.onCommittedPuzzleChanged(session.id, fixture.solvedProgressPuzzle())
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
    idSource: DailySessionIdSource = QueueDailySessionIdSource("daily-session")
): DailyPuzzleViewModel = viewModel(
    dateSource = DeviceLocalDateSource { date },
    repository = repository,
    generator = generator,
    idSource = idSource
)

private fun viewModel(
    dateSource: DeviceLocalDateSource,
    repository: DailySessionRepository,
    generator: DailyPuzzleGenerator,
    idSource: DailySessionIdSource = QueueDailySessionIdSource("daily-session")
): DailyPuzzleViewModel = DailyPuzzleViewModel(
    availabilityResolver = CurrentDailyAvailabilityResolver(
        currentDailyChallengeResolver = CurrentDailyChallengeResolver(
            localDateSource = dateSource
        ),
        dailySessionRepository = repository
    ),
    puzzleGenerator = generator,
    dailySessionRepository = repository,
    sessionIdSource = idSource
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
        profile = current.recipe.challenge.profile,
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
    private val completionResults: ArrayDeque<DailySessionCompletionResult> = ArrayDeque(),
    private val completionFailures: ArrayDeque<IOException> = ArrayDeque()
) : DailySessionRepository {
    private val mutableState = MutableStateFlow(initialState)
    private var pendingUpdateGate = firstUpdateGate
    override val state = mutableState

    val currentState: DailyState
        get() = mutableState.value

    val replaceAttempts = mutableListOf<DailySessionSnapshot>()
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
        val completion = mutableState.value.completedChallengeIds.singleOrNull { completedIdentity ->
            completedIdentity.localDate == snapshot.dailyChallengeId.localDate
        }
        if (completion != null) {
            return DailySessionReplacementResult.DateAlreadyCompleted(completion)
        }
        mutableState.value = mutableState.value.copy(activeSession = snapshot)
        return DailySessionReplacementResult.Replaced
    }

    override suspend fun updateCurrentPuzzle(
        expectedSessionId: DailySessionId,
        puzzle: Puzzle
    ): DailySessionProgressUpdateResult {
        updateAttempts += ProgressAttempt(
            sessionId = expectedSessionId,
            puzzle = puzzle
        )
        pendingUpdateGate?.also { gate ->
            pendingUpdateGate = null
            gate.await()
        }
        if (updateResults.isNotEmpty()) {
            return updateResults.removeFirst()
        }
        val activeSession = mutableState.value.activeSession
        if (activeSession?.sessionId != expectedSessionId) {
            return DailySessionProgressUpdateResult.StaleSession
        }
        val updatedSession = try {
            activeSession.copy(currentPuzzle = puzzle)
        } catch (_: IllegalArgumentException) {
            return DailySessionProgressUpdateResult.InvalidPuzzle
        } catch (_: IllegalStateException) {
            return DailySessionProgressUpdateResult.InvalidPuzzle
        }
        mutableState.value = mutableState.value.copy(activeSession = updatedSession)
        return DailySessionProgressUpdateResult.Updated
    }

    override suspend fun clearSession(expectedSessionId: DailySessionId): DailySessionClearResult =
        DailySessionClearResult.StaleSession

    override suspend fun complete(
        expectedSessionId: DailySessionId,
        expectedDailyChallengeId: DailyChallengeId,
        solvedPuzzle: Puzzle
    ): DailySessionCompletionResult {
        completionAttempts += CompletionAttempt(
            sessionId = expectedSessionId,
            identity = expectedDailyChallengeId,
            solvedPuzzle = solvedPuzzle
        )
        if (completionFailures.isNotEmpty()) {
            throw completionFailures.removeFirst()
        }
        if (completionResults.isNotEmpty()) {
            return completionResults.removeFirst()
        }
        val completion = mutableState.value.completedChallengeIds.singleOrNull { identity ->
            identity.localDate == expectedDailyChallengeId.localDate
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
        mutableState.value = DailyState(
            activeSession = null,
            completedChallengeIds = mutableState.value.completedChallengeIds + expectedDailyChallengeId
        )
        return DailySessionCompletionResult.Completed
    }
}

private data class ProgressAttempt(val sessionId: DailySessionId, val puzzle: Puzzle)

private data class CompletionAttempt(
    val sessionId: DailySessionId,
    val identity: DailyChallengeId,
    val solvedPuzzle: Puzzle
)
