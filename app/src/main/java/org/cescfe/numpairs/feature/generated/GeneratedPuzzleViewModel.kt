package org.cescfe.numpairs.feature.generated

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import java.io.IOException
import java.util.UUID
import java.util.concurrent.ThreadLocalRandom
import kotlinx.coroutines.CoroutineStart
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import org.cescfe.numpairs.data.generated.session.GeneratedSessionId
import org.cescfe.numpairs.data.generated.session.GeneratedSessionRepository
import org.cescfe.numpairs.data.generated.session.GeneratedSessionSnapshot
import org.cescfe.numpairs.domain.generated.generation.GeneratedPuzzleGenerationRequest
import org.cescfe.numpairs.domain.puzzle.model.Puzzle

fun interface GeneratedPuzzleSeedSource {
    fun nextSeed(): Int
}

internal object ThreadLocalGeneratedPuzzleSeedSource : GeneratedPuzzleSeedSource {
    override fun nextSeed(): Int = ThreadLocalRandom.current().nextInt()
}

fun interface GeneratedSessionIdSource {
    fun nextId(): GeneratedSessionId
}

internal object UuidGeneratedSessionIdSource : GeneratedSessionIdSource {
    override fun nextId(): GeneratedSessionId = GeneratedSessionId(UUID.randomUUID().toString())
}

internal sealed interface GeneratedPuzzlePreparationFailure {
    data class Generation(val result: GeneratedPuzzleGenerationResult.Failed) : GeneratedPuzzlePreparationFailure

    data object Persistence : GeneratedPuzzlePreparationFailure
}

internal sealed interface GeneratedPuzzleGenerationUiState {
    data object Idle : GeneratedPuzzleGenerationUiState

    data class Restoring(val expectedSessionId: GeneratedSessionId) : GeneratedPuzzleGenerationUiState

    data class Loading(val request: GeneratedPuzzleGenerationRequest, val previousSession: GeneratedModeGameSession?) :
        GeneratedPuzzleGenerationUiState

    data class Ready(val session: GeneratedModeGameSession) : GeneratedPuzzleGenerationUiState

    data class Failed(
        val request: GeneratedPuzzleGenerationRequest,
        val failure: GeneratedPuzzlePreparationFailure,
        val previousSession: GeneratedModeGameSession?
    ) : GeneratedPuzzleGenerationUiState

    data class ResumeUnavailable(val expectedSessionId: GeneratedSessionId) : GeneratedPuzzleGenerationUiState
}

internal class GeneratedPuzzleViewModel(
    private val mode: GeneratedModeConfiguration,
    private val generationUseCase: GeneratedPuzzleGenerationUseCase,
    private val generatedSessionRepository: GeneratedSessionRepository,
    private val seedSource: GeneratedPuzzleSeedSource = ThreadLocalGeneratedPuzzleSeedSource,
    private val sessionIdSource: GeneratedSessionIdSource = UuidGeneratedSessionIdSource
) : ViewModel() {
    private val _uiState = MutableStateFlow<GeneratedPuzzleGenerationUiState>(GeneratedPuzzleGenerationUiState.Idle)
    val uiState: StateFlow<GeneratedPuzzleGenerationUiState> = _uiState.asStateFlow()

    private var generationJob: Job? = null
    private var generationToken = 0
    private var activeLaunchIntent: GeneratedModeLaunchIntent? = null
    private var sessionWriteJob: Job? = null

    fun onRouteEntered(launchIntent: GeneratedModeLaunchIntent = GeneratedModeLaunchIntent.DefaultNewPuzzle) {
        if (launchIntent != activeLaunchIntent) {
            generationToken++
            generationJob?.cancel()
            generationJob = null
            activeLaunchIntent = launchIntent

            when (launchIntent) {
                is GeneratedModeLaunchIntent.NewPuzzle -> startGeneration(
                    request = nextRequest(),
                    previousSession = (_uiState.value as? GeneratedPuzzleGenerationUiState.Ready)?.session
                )

                is GeneratedModeLaunchIntent.ResumeSession -> startResume(launchIntent)
            }
            return
        }

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

            is GeneratedPuzzleGenerationUiState.Restoring -> startResume(
                GeneratedModeLaunchIntent.ResumeSession(
                    expectedSessionId = state.expectedSessionId
                )
            )

            is GeneratedPuzzleGenerationUiState.Ready,
            is GeneratedPuzzleGenerationUiState.Failed,
            is GeneratedPuzzleGenerationUiState.ResumeUnavailable -> Unit
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

    fun onPuzzleChanged(expectedSessionId: GeneratedSessionId, puzzle: Puzzle) {
        if (!updateVisibleSession(expectedSessionId = expectedSessionId, puzzle = puzzle)) {
            return
        }

        val precedingWrite = sessionWriteJob
        sessionWriteJob = viewModelScope.launch {
            precedingWrite?.join()
            try {
                if (puzzle.isSolved) {
                    generatedSessionRepository.clear(expectedSessionId = expectedSessionId)
                } else {
                    generatedSessionRepository.updateCurrentPuzzle(
                        expectedSessionId = expectedSessionId,
                        puzzle = puzzle
                    )
                }
            } catch (_: IOException) {
                // Keep the playable in-memory session when local persistence is temporarily unavailable.
            }
        }
    }

    private fun updateVisibleSession(expectedSessionId: GeneratedSessionId, puzzle: Puzzle): Boolean {
        val state = _uiState.value
        val visibleSession = when (state) {
            is GeneratedPuzzleGenerationUiState.Ready -> state.session
            is GeneratedPuzzleGenerationUiState.Loading -> state.previousSession
            is GeneratedPuzzleGenerationUiState.Failed -> state.previousSession
            GeneratedPuzzleGenerationUiState.Idle,
            is GeneratedPuzzleGenerationUiState.Restoring,
            is GeneratedPuzzleGenerationUiState.ResumeUnavailable -> null
        }
        if (
            visibleSession?.id != expectedSessionId ||
            visibleSession.currentPuzzle == puzzle
        ) {
            return false
        }

        val updatedSession = visibleSession.copy(
            snapshot = visibleSession.snapshot.copy(currentPuzzle = puzzle)
        )
        _uiState.value = when (state) {
            is GeneratedPuzzleGenerationUiState.Ready -> state.copy(session = updatedSession)
            is GeneratedPuzzleGenerationUiState.Loading -> state.copy(previousSession = updatedSession)
            is GeneratedPuzzleGenerationUiState.Failed -> state.copy(previousSession = updatedSession)
            GeneratedPuzzleGenerationUiState.Idle,
            is GeneratedPuzzleGenerationUiState.Restoring,
            is GeneratedPuzzleGenerationUiState.ResumeUnavailable -> state
        }
        return true
    }

    private fun startResume(launchIntent: GeneratedModeLaunchIntent.ResumeSession) {
        if (generationJob != null) {
            return
        }

        val token = ++generationToken
        _uiState.value = GeneratedPuzzleGenerationUiState.Restoring(
            expectedSessionId = launchIntent.expectedSessionId
        )
        val job = viewModelScope.launch(start = CoroutineStart.LAZY) {
            val snapshot = generatedSessionRepository.session.first()
                ?.takeIf { storedSnapshot ->
                    storedSnapshot.sessionId == launchIntent.expectedSessionId &&
                        storedSnapshot.modeId == mode.id.value &&
                        storedSnapshot.profileId == mode.profile.id.value &&
                        !storedSnapshot.currentPuzzle.isSolved
                }
            if (token != generationToken) {
                return@launch
            }

            generationJob = null
            _uiState.value = snapshot?.let { resumableSnapshot ->
                GeneratedPuzzleGenerationUiState.Ready(
                    session = GeneratedModeGameSession(
                        snapshot = resumableSnapshot,
                        request = GeneratedPuzzleGenerationRequest(
                            profile = mode.profile,
                            seed = resumableSnapshot.seed
                        )
                    )
                )
            } ?: GeneratedPuzzleGenerationUiState.ResumeUnavailable(
                expectedSessionId = launchIntent.expectedSessionId
            )
        }
        generationJob = job
        job.start()
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

            val nextState = when (outcome) {
                is GeneratedPuzzleGenerationResult.Generated -> {
                    prepareGeneratedSession(
                        outcome = outcome,
                        previousSession = previousSession
                    )
                }

                is GeneratedPuzzleGenerationResult.Failed -> {
                    GeneratedPuzzleGenerationUiState.Failed(
                        request = outcome.request,
                        failure = GeneratedPuzzlePreparationFailure.Generation(outcome),
                        previousSession = previousSession
                    )
                }
            }
            if (token != generationToken) {
                return@launch
            }

            generationJob = null
            _uiState.value = nextState
        }
        generationJob = job
        job.start()
    }

    private suspend fun prepareGeneratedSession(
        outcome: GeneratedPuzzleGenerationResult.Generated,
        previousSession: GeneratedModeGameSession?
    ): GeneratedPuzzleGenerationUiState {
        val sessionId = sessionIdSource.nextId()
        val snapshot = GeneratedSessionSnapshot(
            sessionId = sessionId,
            modeId = mode.id.value,
            profileId = outcome.request.profileId.value,
            seed = outcome.request.seed,
            initialPuzzle = outcome.initialPuzzle,
            currentPuzzle = outcome.initialPuzzle
        )

        return try {
            generatedSessionRepository.replace(snapshot)
            GeneratedPuzzleGenerationUiState.Ready(
                session = GeneratedModeGameSession(
                    snapshot = snapshot,
                    request = outcome.request
                )
            )
        } catch (_: IOException) {
            GeneratedPuzzleGenerationUiState.Failed(
                request = outcome.request,
                failure = GeneratedPuzzlePreparationFailure.Persistence,
                previousSession = previousSession
            )
        }
    }

    private fun nextRequest(): GeneratedPuzzleGenerationRequest = GeneratedPuzzleGenerationRequest(
        profile = mode.profile,
        seed = seedSource.nextSeed()
    )
}

internal data class GeneratedModeGameSession(
    val snapshot: GeneratedSessionSnapshot,
    val request: GeneratedPuzzleGenerationRequest
) {
    init {
        require(snapshot.profileId == request.profileId.value) {
            "Generated game session profile must match its generation request."
        }
        require(snapshot.seed == request.seed) {
            "Generated game session seed must match its generation request."
        }
    }

    val id: GeneratedSessionId
        get() = snapshot.sessionId

    val initialPuzzle: Puzzle
        get() = snapshot.initialPuzzle

    val currentPuzzle: Puzzle
        get() = snapshot.currentPuzzle
}
