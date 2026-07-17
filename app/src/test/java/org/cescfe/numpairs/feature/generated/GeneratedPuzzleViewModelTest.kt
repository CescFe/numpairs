package org.cescfe.numpairs.feature.generated

import java.io.IOException
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.setMain
import org.cescfe.numpairs.data.generated.session.GeneratedSessionId
import org.cescfe.numpairs.data.generated.session.GeneratedSessionRepository
import org.cescfe.numpairs.data.generated.session.GeneratedSessionSnapshot
import org.cescfe.numpairs.data.puzzle.seed.samplePuzzle
import org.cescfe.numpairs.domain.generated.generation.GeneratedPairsPuzzleGenerationFailureReason
import org.cescfe.numpairs.domain.generated.generation.GeneratedPairsPuzzleGenerationOutcome
import org.cescfe.numpairs.domain.generated.generation.GeneratedPuzzleGenerationRequest
import org.cescfe.numpairs.domain.puzzle.model.Expression
import org.cescfe.numpairs.domain.puzzle.model.Operator
import org.cescfe.numpairs.domain.puzzle.model.Puzzle
import org.cescfe.numpairs.feature.game.presentation.support.solvedPuzzleWithKnownStripAndAssignments
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class GeneratedPuzzleViewModelTest {
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
    fun successful_generation_is_persisted_before_readiness() {
        val writeGate = CompletableDeferred<Unit>()
        val repository = RecordingGeneratedSessionRepository(writeGate = writeGate)
        val viewModel = GeneratedPuzzleViewModel(
            mode = GeneratedModes.FOUR_PAIRS,
            generationUseCase = generatedPuzzleUseCase(),
            generatedSessionRepository = repository,
            seedSource = QueueGeneratedPuzzleSeedSource(17),
            sessionIdSource = QueueGeneratedSessionIdSource("stable-session")
        )

        viewModel.onRouteEntered()
        dispatcher.scheduler.runCurrent()

        assertTrue(viewModel.uiState.value is GeneratedPuzzleGenerationUiState.Loading)
        val attemptedSnapshot = repository.replaceAttempts.single()
        assertEquals(GeneratedSessionId("stable-session"), attemptedSnapshot.sessionId)
        assertEquals(GeneratedModes.FOUR_PAIRS.id.value, attemptedSnapshot.modeId)
        assertEquals(GeneratedModes.FOUR_PAIRS.profile.id.value, attemptedSnapshot.profileId)
        assertEquals(17, attemptedSnapshot.seed)
        assertEquals(samplePuzzle, attemptedSnapshot.initialPuzzle)
        assertEquals(samplePuzzle, attemptedSnapshot.currentPuzzle)
        assertNull(repository.session.value)

        writeGate.complete(Unit)
        dispatcher.scheduler.advanceUntilIdle()

        val ready = viewModel.uiState.value as GeneratedPuzzleGenerationUiState.Ready
        assertEquals(attemptedSnapshot, repository.session.value)
        assertEquals(attemptedSnapshot, ready.session.snapshot)
    }

    @Test
    fun replay_is_deduplicated_and_a_failure_keeps_the_completed_session_until_retry_succeeds() {
        val firstPuzzle = CompletableDeferred(samplePuzzle)
        val replayFailure = CompletableDeferred(true)
        val retryPuzzle = CompletableDeferred(samplePuzzle)
        val useCase = ControlledGeneratedPuzzleUseCase(
            firstPuzzle = firstPuzzle,
            replayFailure = replayFailure,
            retryPuzzle = retryPuzzle
        )
        val repository = RecordingGeneratedSessionRepository()
        val viewModel = GeneratedPuzzleViewModel(
            mode = GeneratedModes.FOUR_PAIRS,
            generationUseCase = useCase,
            generatedSessionRepository = repository,
            seedSource = QueueGeneratedPuzzleSeedSource(11, 22, 33),
            sessionIdSource = QueueGeneratedSessionIdSource("first", "retry")
        )

        viewModel.onRouteEntered()
        dispatcher.scheduler.advanceUntilIdle()
        assertTrue(viewModel.uiState.value is GeneratedPuzzleGenerationUiState.Ready)

        val initialSession = (viewModel.uiState.value as GeneratedPuzzleGenerationUiState.Ready).session
        viewModel.onNewPuzzleRequested()
        viewModel.onNewPuzzleRequested()
        dispatcher.scheduler.advanceUntilIdle()
        assertEquals(2, useCase.requests.size)
        val failed = viewModel.uiState.value as GeneratedPuzzleGenerationUiState.Failed
        assertEquals(initialSession, failed.previousSession)
        val generationFailure = failed.failure as GeneratedPuzzlePreparationFailure.Generation
        assertEquals(
            GeneratedPairsPuzzleGenerationFailureReason.AttemptsExhausted,
            generationFailure.result.failure.reason
        )
        assertEquals(1, repository.replaceAttempts.size)

        viewModel.retry()
        dispatcher.scheduler.advanceUntilIdle()

        val ready = viewModel.uiState.value as GeneratedPuzzleGenerationUiState.Ready
        assertEquals(GeneratedSessionId("retry"), ready.session.id)
        assertEquals(3, useCase.requests.size)
        assertEquals(listOf(11, 22, 33), useCase.requests.map(GeneratedPuzzleGenerationRequest::seed))
        assertEquals(2, repository.replaceAttempts.size)
        assertEquals(ready.session.snapshot, repository.session.value)
    }

    @Test
    fun generation_cancellation_does_not_replace_the_stored_session() {
        val generatedPuzzle = CompletableDeferred<Puzzle>()
        val existingSnapshot = generatedSessionSnapshot(sessionId = "existing")
        val repository = RecordingGeneratedSessionRepository(initialSession = existingSnapshot)
        val viewModel = GeneratedPuzzleViewModel(
            mode = GeneratedModes.FOUR_PAIRS,
            generationUseCase = generatedPuzzleUseCase(generatedPuzzle),
            generatedSessionRepository = repository,
            seedSource = QueueGeneratedPuzzleSeedSource(29),
            sessionIdSource = QueueGeneratedSessionIdSource("cancelled")
        )

        viewModel.onRouteEntered()
        dispatcher.scheduler.runCurrent()
        viewModel.onRouteExited()
        generatedPuzzle.complete(samplePuzzle)
        dispatcher.scheduler.advanceUntilIdle()

        assertTrue(repository.replaceAttempts.isEmpty())
        assertEquals(existingSnapshot, repository.session.value)
    }

    @Test
    fun persistence_failure_keeps_the_previous_stored_session_and_is_recoverable() {
        val existingSnapshot = generatedSessionSnapshot(sessionId = "existing")
        val repository = RecordingGeneratedSessionRepository(
            initialSession = existingSnapshot,
            replaceFailure = IOException("storage unavailable")
        )
        val viewModel = GeneratedPuzzleViewModel(
            mode = GeneratedModes.FOUR_PAIRS,
            generationUseCase = generatedPuzzleUseCase(),
            generatedSessionRepository = repository,
            seedSource = QueueGeneratedPuzzleSeedSource(31),
            sessionIdSource = QueueGeneratedSessionIdSource("not-stored")
        )

        viewModel.onRouteEntered()
        dispatcher.scheduler.advanceUntilIdle()

        val failed = viewModel.uiState.value as GeneratedPuzzleGenerationUiState.Failed
        assertEquals(GeneratedPuzzlePreparationFailure.Persistence, failed.failure)
        assertNull(failed.previousSession)
        assertEquals(existingSnapshot, repository.session.value)
        assertEquals(1, repository.replaceAttempts.size)
    }

    @Test
    fun resume_restores_the_exact_current_puzzle_and_metadata_without_generation_or_writes() {
        val currentPuzzle = samplePuzzle.copy(
            strip = samplePuzzle.strip.withUpdatedEntry(index = 1, value = 1)
        )
        val snapshot = generatedSessionSnapshot(
            sessionId = "resume-me",
            currentPuzzle = currentPuzzle
        )
        val repository = RecordingGeneratedSessionRepository(initialSession = snapshot)
        val generationUseCase = UnexpectedGeneratedPuzzleUseCase()
        val viewModel = GeneratedPuzzleViewModel(
            mode = GeneratedModes.FOUR_PAIRS,
            generationUseCase = generationUseCase,
            generatedSessionRepository = repository
        )

        viewModel.onRouteEntered(
            GeneratedModeLaunchIntent.ResumeSession(
                expectedSessionId = snapshot.sessionId
            )
        )
        dispatcher.scheduler.advanceUntilIdle()

        val ready = viewModel.uiState.value as GeneratedPuzzleGenerationUiState.Ready
        assertEquals(snapshot, ready.session.snapshot)
        assertEquals(currentPuzzle, ready.session.currentPuzzle)
        assertEquals(snapshot.seed, ready.session.request.seed)
        assertEquals(snapshot.profileId, ready.session.request.profileId.value)
        assertEquals(0, generationUseCase.requestCount)
        assertTrue(repository.replaceAttempts.isEmpty())
    }

    @Test
    fun resume_rejects_missing_stale_mismatched_and_solved_sessions() {
        val expectedSessionId = GeneratedSessionId("expected")
        val solvedPuzzle = solvedPuzzleWithKnownStripAndAssignments()
        val solvedInitialPuzzle = solvedPuzzle.copy(
            board = solvedPuzzle.board.copy(
                tiles = solvedPuzzle.board.tiles.map { tile ->
                    tile.copy(
                        expression = tile.expression.copy(
                            leftOperand = Expression.Operand.Hidden,
                            operator = Operator.Hidden,
                            rightOperand = Expression.Operand.Hidden
                        )
                    )
                }
            )
        )
        val unavailableSnapshots = listOf(
            null,
            generatedSessionSnapshot(sessionId = "stale"),
            generatedSessionSnapshot(
                sessionId = expectedSessionId.value,
                modeId = GeneratedModes.EIGHT_PAIRS.id.value
            ),
            generatedSessionSnapshot(
                sessionId = expectedSessionId.value,
                profileId = GeneratedModes.EIGHT_PAIRS.profile.id.value
            ),
            generatedSessionSnapshot(
                sessionId = expectedSessionId.value,
                initialPuzzle = solvedInitialPuzzle,
                currentPuzzle = solvedPuzzle
            )
        )

        unavailableSnapshots.forEach { storedSnapshot ->
            val repository = RecordingGeneratedSessionRepository(initialSession = storedSnapshot)
            val generationUseCase = UnexpectedGeneratedPuzzleUseCase()
            val viewModel = GeneratedPuzzleViewModel(
                mode = GeneratedModes.FOUR_PAIRS,
                generationUseCase = generationUseCase,
                generatedSessionRepository = repository
            )

            viewModel.onRouteEntered(
                GeneratedModeLaunchIntent.ResumeSession(
                    expectedSessionId = expectedSessionId
                )
            )
            dispatcher.scheduler.advanceUntilIdle()

            assertEquals(
                GeneratedPuzzleGenerationUiState.ResumeUnavailable(expectedSessionId),
                viewModel.uiState.value
            )
            assertEquals(0, generationUseCase.requestCount)
            assertTrue(repository.replaceAttempts.isEmpty())
        }
    }
}

private fun generatedPuzzleUseCase(
    puzzle: CompletableDeferred<Puzzle> = CompletableDeferred(samplePuzzle)
): GeneratedPuzzleGenerationUseCase = GeneratedPuzzleGenerationUseCase { request ->
    GeneratedPuzzleGenerationResult.Generated(
        request = request,
        initialPuzzle = puzzle.await()
    )
}

private class ControlledGeneratedPuzzleUseCase(
    private val firstPuzzle: CompletableDeferred<Puzzle>,
    private val replayFailure: CompletableDeferred<Boolean>,
    private val retryPuzzle: CompletableDeferred<Puzzle>
) : GeneratedPuzzleGenerationUseCase {
    val requests = mutableListOf<GeneratedPuzzleGenerationRequest>()

    override suspend fun generate(request: GeneratedPuzzleGenerationRequest): GeneratedPuzzleGenerationResult {
        requests += request

        return when (requests.size) {
            1 -> GeneratedPuzzleGenerationResult.Generated(
                request = request,
                initialPuzzle = firstPuzzle.await()
            )

            2 -> {
                replayFailure.await()
                GeneratedPuzzleGenerationResult.Failed(
                    GeneratedPairsPuzzleGenerationOutcome.Failed(
                        request = request,
                        attemptsUsed = 1,
                        searchWorkConsumed = 12,
                        reason = GeneratedPairsPuzzleGenerationFailureReason.AttemptsExhausted,
                        candidateRejections = emptyList()
                    )
                )
            }

            3 -> GeneratedPuzzleGenerationResult.Generated(
                request = request,
                initialPuzzle = retryPuzzle.await()
            )

            else -> error("Unexpected generated puzzle request.")
        }
    }
}

private class UnexpectedGeneratedPuzzleUseCase : GeneratedPuzzleGenerationUseCase {
    var requestCount: Int = 0

    override suspend fun generate(request: GeneratedPuzzleGenerationRequest): GeneratedPuzzleGenerationResult {
        requestCount++
        error("Resume must not invoke generated puzzle creation.")
    }
}

private class QueueGeneratedPuzzleSeedSource(vararg seeds: Int) : GeneratedPuzzleSeedSource {
    private val values = ArrayDeque(seeds.toList())

    override fun nextSeed(): Int = values.removeFirst()
}

private class QueueGeneratedSessionIdSource(vararg ids: String) : GeneratedSessionIdSource {
    private val values = ArrayDeque(ids.toList())

    override fun nextId(): GeneratedSessionId = GeneratedSessionId(values.removeFirst())
}

private class RecordingGeneratedSessionRepository(
    initialSession: GeneratedSessionSnapshot? = null,
    private val writeGate: CompletableDeferred<Unit>? = null,
    private val replaceFailure: IOException? = null
) : GeneratedSessionRepository {
    private val mutableSession = MutableStateFlow(initialSession)
    override val session: StateFlow<GeneratedSessionSnapshot?> = mutableSession.asStateFlow()
    val replaceAttempts = mutableListOf<GeneratedSessionSnapshot>()

    override suspend fun replace(snapshot: GeneratedSessionSnapshot) {
        replaceAttempts += snapshot
        writeGate?.await()
        replaceFailure?.let { failure ->
            throw failure
        }
        mutableSession.value = snapshot
    }

    override suspend fun updateCurrentPuzzle(expectedSessionId: GeneratedSessionId, puzzle: Puzzle): Boolean {
        val snapshot = mutableSession.value
        if (snapshot?.sessionId != expectedSessionId) {
            return false
        }

        mutableSession.value = snapshot.copy(currentPuzzle = puzzle)
        return true
    }

    override suspend fun clear(expectedSessionId: GeneratedSessionId): Boolean {
        if (mutableSession.value?.sessionId != expectedSessionId) {
            return false
        }

        mutableSession.value = null
        return true
    }
}

private fun generatedSessionSnapshot(
    sessionId: String,
    modeId: String = GeneratedModes.FOUR_PAIRS.id.value,
    profileId: String = GeneratedModes.FOUR_PAIRS.profile.id.value,
    initialPuzzle: Puzzle = samplePuzzle,
    currentPuzzle: Puzzle = samplePuzzle
): GeneratedSessionSnapshot = GeneratedSessionSnapshot(
    sessionId = GeneratedSessionId(sessionId),
    modeId = modeId,
    profileId = profileId,
    seed = 7,
    initialPuzzle = initialPuzzle,
    currentPuzzle = currentPuzzle
)
