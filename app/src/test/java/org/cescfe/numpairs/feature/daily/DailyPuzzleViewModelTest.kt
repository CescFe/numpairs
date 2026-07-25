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
import org.cescfe.numpairs.data.daily.session.generatedDailyFixture
import org.cescfe.numpairs.domain.daily.DailyChallengeId
import org.cescfe.numpairs.domain.daily.DeviceLocalDateSource
import org.cescfe.numpairs.domain.generated.generation.GeneratedPairsPuzzleGenerationFailureReason
import org.cescfe.numpairs.domain.generated.generation.GeneratedPairsPuzzleGenerationOutcome
import org.cescfe.numpairs.domain.generated.generation.GeneratedPuzzleGenerationRequest
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
    private val replaceFailures: ArrayDeque<IOException> = ArrayDeque()
) : DailySessionRepository {
    private val mutableState = MutableStateFlow(initialState)
    override val state = mutableState

    val currentState: DailyState
        get() = mutableState.value

    val replaceAttempts = mutableListOf<DailySessionSnapshot>()

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
    ): DailySessionProgressUpdateResult = DailySessionProgressUpdateResult.StaleSession

    override suspend fun clearSession(expectedSessionId: DailySessionId): DailySessionClearResult =
        DailySessionClearResult.StaleSession

    override suspend fun complete(
        expectedSessionId: DailySessionId,
        expectedDailyChallengeId: DailyChallengeId,
        solvedPuzzle: Puzzle
    ): DailySessionCompletionResult = DailySessionCompletionResult.StaleSession
}
