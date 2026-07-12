package org.cescfe.numpairs.feature.generated

import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.setMain
import org.cescfe.numpairs.data.puzzle.seed.samplePuzzle
import org.cescfe.numpairs.domain.generated.generation.GeneratedPairsPuzzleGenerationFailureReason
import org.cescfe.numpairs.domain.generated.generation.GeneratedPairsPuzzleGenerationOutcome
import org.cescfe.numpairs.domain.generated.generation.GeneratedPuzzleGenerationRequest
import org.cescfe.numpairs.domain.puzzle.model.Puzzle
import org.junit.After
import org.junit.Assert.assertEquals
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
    fun replay_is_deduplicated_and_a_failure_keeps_the_completed_session_until_retry_succeeds() {
        val firstPuzzle = CompletableDeferred(samplePuzzle)
        val replayFailure = CompletableDeferred(true)
        val retryPuzzle = CompletableDeferred(samplePuzzle)
        val useCase = ControlledGeneratedPuzzleUseCase(
            firstPuzzle = firstPuzzle,
            replayFailure = replayFailure,
            retryPuzzle = retryPuzzle
        )
        val viewModel = GeneratedPuzzleViewModel(
            mode = GeneratedModes.FOUR_PAIRS,
            generationUseCase = useCase,
            seedSource = QueueGeneratedPuzzleSeedSource(11, 22, 33)
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
        assertEquals(GeneratedPairsPuzzleGenerationFailureReason.AttemptsExhausted, failed.failure.failure.reason)

        viewModel.retry()
        dispatcher.scheduler.advanceUntilIdle()

        val ready = viewModel.uiState.value as GeneratedPuzzleGenerationUiState.Ready
        assertEquals(1, ready.session.id)
        assertEquals(3, useCase.requests.size)
        assertEquals(listOf(11, 22, 33), useCase.requests.map(GeneratedPuzzleGenerationRequest::seed))
    }
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

private class QueueGeneratedPuzzleSeedSource(vararg seeds: Int) : GeneratedPuzzleSeedSource {
    private val values = ArrayDeque(seeds.toList())

    override fun nextSeed(): Int = values.removeFirst()
}
