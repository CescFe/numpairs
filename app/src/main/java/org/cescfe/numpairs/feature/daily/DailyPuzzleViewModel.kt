package org.cescfe.numpairs.feature.daily

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import java.io.IOException
import java.util.UUID
import kotlinx.coroutines.CoroutineStart
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import org.cescfe.numpairs.data.daily.session.DailySessionId
import org.cescfe.numpairs.data.daily.session.DailySessionReplacementResult
import org.cescfe.numpairs.data.daily.session.DailySessionRepository
import org.cescfe.numpairs.data.daily.session.DailySessionSnapshot
import org.cescfe.numpairs.domain.daily.DailyChallengeId
import org.cescfe.numpairs.domain.puzzle.model.Puzzle

fun interface DailySessionIdSource {
    fun nextId(): DailySessionId
}

internal object UuidDailySessionIdSource : DailySessionIdSource {
    override fun nextId(): DailySessionId = DailySessionId(UUID.randomUUID().toString())
}

internal sealed interface DailyPuzzlePreparationFailure {
    data class GenerationExhausted(val result: DailyPuzzleGenerationResult.Exhausted) : DailyPuzzlePreparationFailure

    data class GenerationCancelled(val result: DailyPuzzleGenerationResult.Cancelled) : DailyPuzzlePreparationFailure

    data object InvalidGeneratedPuzzle : DailyPuzzlePreparationFailure

    data object Persistence : DailyPuzzlePreparationFailure
}

internal sealed interface DailyPuzzleUiState {
    data object Idle : DailyPuzzleUiState

    data object Resolving : DailyPuzzleUiState

    data class Loading(val currentDailyChallenge: CurrentDailyChallenge) : DailyPuzzleUiState

    data class Ready(val session: DailyGameSession) : DailyPuzzleUiState

    data class CompletedToday(val currentDailyChallenge: CurrentDailyChallenge, val completion: DailyChallengeId) :
        DailyPuzzleUiState {
        init {
            require(completion.localDate == currentDailyChallenge.identity.localDate) {
                "A completed Daily UI state must own the captured local date."
            }
        }
    }

    data class Failed(val currentDailyChallenge: CurrentDailyChallenge, val failure: DailyPuzzlePreparationFailure) :
        DailyPuzzleUiState
}

internal class DailyPuzzleViewModel(
    private val availabilityResolver: CurrentDailyAvailabilityResolver,
    private val puzzleGenerator: DailyPuzzleGenerator,
    private val dailySessionRepository: DailySessionRepository,
    private val sessionIdSource: DailySessionIdSource = UuidDailySessionIdSource
) : ViewModel() {
    private val _uiState = MutableStateFlow<DailyPuzzleUiState>(DailyPuzzleUiState.Idle)
    val uiState: StateFlow<DailyPuzzleUiState> = _uiState.asStateFlow()

    private var preparationJob: Job? = null
    private var preparationToken: Int = 0
    private var pendingSessionId: DailySessionId? = null

    fun onRouteEntered() {
        if (preparationJob != null || _uiState.value != DailyPuzzleUiState.Idle) {
            return
        }
        resolveAndPrepare()
    }

    fun onRouteExited() {
        preparationToken++
        preparationJob?.cancel()
        preparationJob = null
        pendingSessionId = null
        _uiState.value = DailyPuzzleUiState.Idle
    }

    fun retry() {
        val failedState = _uiState.value as? DailyPuzzleUiState.Failed ?: return
        startGeneration(currentDailyChallenge = failedState.currentDailyChallenge)
    }

    private fun resolveAndPrepare() {
        val token = ++preparationToken
        _uiState.value = DailyPuzzleUiState.Resolving
        val job = viewModelScope.launch(start = CoroutineStart.LAZY) {
            val availability = availabilityResolver.resolve()
            if (token != preparationToken) {
                return@launch
            }

            preparationJob = null
            when (availability) {
                is CurrentDailyAvailability.StartToday -> {
                    startGeneration(
                        currentDailyChallenge = availability.currentDailyChallenge
                    )
                }

                is CurrentDailyAvailability.ContinueToday -> {
                    _uiState.value = DailyPuzzleUiState.Ready(
                        session = DailyGameSession(
                            currentDailyChallenge = availability.currentDailyChallenge,
                            snapshot = availability.snapshot
                        )
                    )
                }

                is CurrentDailyAvailability.CompletedToday -> {
                    _uiState.value = DailyPuzzleUiState.CompletedToday(
                        currentDailyChallenge = availability.currentDailyChallenge,
                        completion = availability.completion
                    )
                }
            }
        }
        preparationJob = job
        job.start()
    }

    private fun startGeneration(currentDailyChallenge: CurrentDailyChallenge) {
        if (preparationJob != null) {
            return
        }

        val token = ++preparationToken
        _uiState.value = DailyPuzzleUiState.Loading(
            currentDailyChallenge = currentDailyChallenge
        )
        val job = viewModelScope.launch(start = CoroutineStart.LAZY) {
            val outcome = puzzleGenerator.generate(currentDailyChallenge)
            if (token != preparationToken) {
                return@launch
            }

            val nextState = when (outcome) {
                is DailyPuzzleGenerationResult.Generated -> {
                    prepareGeneratedSession(
                        expectedCurrentDailyChallenge = currentDailyChallenge,
                        outcome = outcome
                    )
                }

                is DailyPuzzleGenerationResult.Exhausted -> {
                    require(outcome.currentDailyChallenge == currentDailyChallenge) {
                        "Daily generation outcome must match the captured current identity."
                    }
                    DailyPuzzleUiState.Failed(
                        currentDailyChallenge = currentDailyChallenge,
                        failure = DailyPuzzlePreparationFailure.GenerationExhausted(outcome)
                    )
                }

                is DailyPuzzleGenerationResult.Cancelled -> {
                    require(outcome.currentDailyChallenge == currentDailyChallenge) {
                        "Daily generation outcome must match the captured current identity."
                    }
                    DailyPuzzleUiState.Failed(
                        currentDailyChallenge = currentDailyChallenge,
                        failure = DailyPuzzlePreparationFailure.GenerationCancelled(outcome)
                    )
                }
            }
            if (token != preparationToken) {
                return@launch
            }

            preparationJob = null
            _uiState.value = nextState
        }
        preparationJob = job
        job.start()
    }

    private suspend fun prepareGeneratedSession(
        expectedCurrentDailyChallenge: CurrentDailyChallenge,
        outcome: DailyPuzzleGenerationResult.Generated
    ): DailyPuzzleUiState {
        require(outcome.currentDailyChallenge == expectedCurrentDailyChallenge) {
            "Daily generation outcome must match the captured current identity."
        }
        val snapshot = try {
            DailySessionSnapshot(
                sessionId = pendingSessionId ?: sessionIdSource.nextId().also { generatedId ->
                    pendingSessionId = generatedId
                },
                dailyChallengeId = outcome.identity,
                candidateIndex = outcome.candidateIndex,
                seed = outcome.seed,
                initialPuzzle = outcome.initialPuzzle,
                currentPuzzle = outcome.initialPuzzle
            )
        } catch (_: IllegalArgumentException) {
            return DailyPuzzleUiState.Failed(
                currentDailyChallenge = expectedCurrentDailyChallenge,
                failure = DailyPuzzlePreparationFailure.InvalidGeneratedPuzzle
            )
        } catch (_: IllegalStateException) {
            return DailyPuzzleUiState.Failed(
                currentDailyChallenge = expectedCurrentDailyChallenge,
                failure = DailyPuzzlePreparationFailure.InvalidGeneratedPuzzle
            )
        }

        return try {
            when (val replacement = dailySessionRepository.replaceSession(snapshot)) {
                DailySessionReplacementResult.Replaced -> {
                    pendingSessionId = null
                    DailyPuzzleUiState.Ready(
                        session = DailyGameSession(
                            currentDailyChallenge = expectedCurrentDailyChallenge,
                            snapshot = snapshot
                        )
                    )
                }

                is DailySessionReplacementResult.DateAlreadyCompleted -> {
                    pendingSessionId = null
                    DailyPuzzleUiState.CompletedToday(
                        currentDailyChallenge = expectedCurrentDailyChallenge,
                        completion = replacement.completion
                    )
                }
            }
        } catch (_: IOException) {
            DailyPuzzleUiState.Failed(
                currentDailyChallenge = expectedCurrentDailyChallenge,
                failure = DailyPuzzlePreparationFailure.Persistence
            )
        }
    }
}

internal data class DailyGameSession(
    val currentDailyChallenge: CurrentDailyChallenge,
    val snapshot: DailySessionSnapshot
) {
    init {
        require(snapshot.dailyChallengeId == currentDailyChallenge.identity) {
            "Daily game session identity must match its captured current identity."
        }
        require(snapshot.recipeContract == currentDailyChallenge.recipe.contract) {
            "Daily game session recipe must match its captured current recipe."
        }
    }

    val id: DailySessionId
        get() = snapshot.sessionId

    val initialPuzzle: Puzzle
        get() = snapshot.initialPuzzle

    val currentPuzzle: Puzzle
        get() = snapshot.currentPuzzle
}
