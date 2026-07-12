package org.cescfe.numpairs.feature.generated

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import java.util.concurrent.ThreadLocalRandom
import kotlinx.coroutines.CoroutineStart
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import org.cescfe.numpairs.domain.generated.generation.GeneratedPuzzleGenerationRequest
import org.cescfe.numpairs.domain.puzzle.model.Puzzle

fun interface GeneratedPuzzleSeedSource {
    fun nextSeed(): Int
}

internal object ThreadLocalGeneratedPuzzleSeedSource : GeneratedPuzzleSeedSource {
    override fun nextSeed(): Int = ThreadLocalRandom.current().nextInt()
}

internal sealed interface GeneratedPuzzleGenerationUiState {
    data object Idle : GeneratedPuzzleGenerationUiState

    data class Loading(val request: GeneratedPuzzleGenerationRequest, val previousSession: GeneratedModeGameSession?) :
        GeneratedPuzzleGenerationUiState

    data class Ready(val session: GeneratedModeGameSession) : GeneratedPuzzleGenerationUiState

    data class Failed(
        val request: GeneratedPuzzleGenerationRequest,
        val failure: GeneratedPuzzleGenerationResult.Failed,
        val previousSession: GeneratedModeGameSession?
    ) : GeneratedPuzzleGenerationUiState
}

internal class GeneratedPuzzleViewModel(
    private val mode: GeneratedModeConfiguration,
    private val generationUseCase: GeneratedPuzzleGenerationUseCase,
    private val seedSource: GeneratedPuzzleSeedSource = ThreadLocalGeneratedPuzzleSeedSource
) : ViewModel() {
    private val _uiState = MutableStateFlow<GeneratedPuzzleGenerationUiState>(GeneratedPuzzleGenerationUiState.Idle)
    val uiState: StateFlow<GeneratedPuzzleGenerationUiState> = _uiState.asStateFlow()

    private var generationJob: Job? = null
    private var generationToken = 0
    private var nextSessionId = 0

    fun onRouteEntered() {
        if (generationJob != null) {
            return
        }

        when (val state = _uiState.value) {
            GeneratedPuzzleGenerationUiState.Idle -> startGeneration(
                request = nextRequest(),
                previousSession = null
            )

            is GeneratedPuzzleGenerationUiState.Loading -> startGeneration(
                request = state.request,
                previousSession = state.previousSession
            )

            is GeneratedPuzzleGenerationUiState.Ready,
            is GeneratedPuzzleGenerationUiState.Failed -> Unit
        }
    }

    fun onRouteExited() {
        generationToken++
        generationJob?.cancel()
        generationJob = null
    }

    fun retry() {
        val state = _uiState.value as? GeneratedPuzzleGenerationUiState.Failed ?: return
        startGeneration(
            request = nextRequest(),
            previousSession = state.previousSession
        )
    }

    fun onNewPuzzleRequested() {
        val state = _uiState.value as? GeneratedPuzzleGenerationUiState.Ready ?: return
        startGeneration(
            request = nextRequest(),
            previousSession = state.session
        )
    }

    fun dismissFailure() {
        val state = _uiState.value as? GeneratedPuzzleGenerationUiState.Failed ?: return
        state.previousSession?.let { previousSession ->
            _uiState.value = GeneratedPuzzleGenerationUiState.Ready(session = previousSession)
        }
    }

    private fun startGeneration(request: GeneratedPuzzleGenerationRequest, previousSession: GeneratedModeGameSession?) {
        if (generationJob != null) {
            return
        }

        val token = ++generationToken
        _uiState.value = GeneratedPuzzleGenerationUiState.Loading(
            request = request,
            previousSession = previousSession
        )
        val job = viewModelScope.launch(start = CoroutineStart.LAZY) {
            val outcome = generationUseCase.generate(request = request)
            if (token != generationToken) {
                return@launch
            }

            generationJob = null
            _uiState.value = when (outcome) {
                is GeneratedPuzzleGenerationResult.Generated -> {
                    GeneratedPuzzleGenerationUiState.Ready(
                        session = GeneratedModeGameSession(
                            id = nextSessionId++,
                            initialPuzzle = outcome.initialPuzzle,
                            request = outcome.request
                        )
                    )
                }

                is GeneratedPuzzleGenerationResult.Failed -> {
                    GeneratedPuzzleGenerationUiState.Failed(
                        request = outcome.request,
                        failure = outcome,
                        previousSession = previousSession
                    )
                }
            }
        }
        generationJob = job
        job.start()
    }

    private fun nextRequest(): GeneratedPuzzleGenerationRequest = GeneratedPuzzleGenerationRequest(
        profile = mode.profile,
        seed = seedSource.nextSeed()
    )
}

internal data class GeneratedModeGameSession(
    val id: Int,
    val initialPuzzle: Puzzle,
    val request: GeneratedPuzzleGenerationRequest
)
