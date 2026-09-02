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

    data class Loading(
        val definition: GeneratedPuzzleGenerationDefinition,
        val request: GeneratedPuzzleGenerationRequest,
        val previousSession: GeneratedModeGameSession?
    ) : GeneratedPuzzleGenerationUiState

    data class Ready(
        val session: GeneratedModeGameSession,
        val replacementTransition: GeneratedPuzzleReplacementTransition? = null
    ) : GeneratedPuzzleGenerationUiState {
        init {
            require(replacementTransition == null || replacementTransition.successorSessionId == session.id) {
                "A replacement transition must target the ready session."
            }
        }
    }

    data class Failed(
        val definition: GeneratedPuzzleGenerationDefinition,
        val request: GeneratedPuzzleGenerationRequest,
        val failure: GeneratedPuzzlePreparationFailure,
        val previousSession: GeneratedModeGameSession?
    ) : GeneratedPuzzleGenerationUiState

    data class ResumeUnavailable(val expectedSessionId: GeneratedSessionId) : GeneratedPuzzleGenerationUiState
}

internal class GeneratedPuzzleViewModel(
    private val challenge: GeneratedChallenge,
    private val generationUseCase: GeneratedPuzzleGenerationUseCase,
    private val generatedSessionRepository: GeneratedSessionRepository,
    private val seedSource: GeneratedPuzzleSeedSource = ThreadLocalGeneratedPuzzleSeedSource,
    private val sessionIdSource: GeneratedSessionIdSource = UuidGeneratedSessionIdSource
) : ViewModel() {
    private val initialDefinition = GeneratedPuzzleGenerationDefinition(
        challenge = challenge,
        generationUseCase = generationUseCase
    )
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
                    definition = initialDefinition,
                    request = nextRequest(initialDefinition.challenge),
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
                definition = initialDefinition,
                request = nextRequest(initialDefinition.challenge),
                previousSession = null
            )

            is GeneratedPuzzleGenerationUiState.Loading -> startGeneration(
                definition = state.definition,
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
            definition = state.definition,
            request = nextRequest(state.definition.challenge),
            previousSession = state.previousSession
        )
    }

    fun onNewPuzzleRequested() {
        onNewPuzzleRequested {
            initialDefinition
        }
    }

    fun onNewPuzzleRequested(definitionProvider: () -> GeneratedPuzzleGenerationDefinition) {
        val state = _uiState.value as? GeneratedPuzzleGenerationUiState.Ready ?: return
        val definition = definitionProvider()
        startGeneration(
            definition = definition,
            request = nextRequest(definition.challenge),
            previousSession = state.session
        )
    }

    fun onReplacementTransitionConsumed(transition: GeneratedPuzzleReplacementTransition) {
        val state = _uiState.value as? GeneratedPuzzleGenerationUiState.Ready ?: return
        if (
            state.session.id == transition.successorSessionId &&
            state.replacementTransition == transition
        ) {
            _uiState.value = state.copy(replacementTransition = null)
        }
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
        _uiState.value = state.withVisibleSession(updatedSession)
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
                        storedSnapshot.modeId == challenge.modeId.value &&
                        storedSnapshot.profileId == challenge.profile.id.value &&
                        !storedSnapshot.currentPuzzle.isSolved
                }
            if (token != generationToken) {
                return@launch
            }

            generationJob = null
            _uiState.value = snapshot?.let { resumableSnapshot ->
                GeneratedPuzzleGenerationUiState.Ready(
                    session = GeneratedModeGameSession(
                        challenge = challenge,
                        snapshot = resumableSnapshot,
                        request = GeneratedPuzzleGenerationRequest(
                            profile = challenge.profile,
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

    private fun startGeneration(
        definition: GeneratedPuzzleGenerationDefinition,
        request: GeneratedPuzzleGenerationRequest,
        previousSession: GeneratedModeGameSession?
    ) {
        require(definition.challenge.profile.id == request.profileId) {
            "Generated puzzle definition must match its request profile."
        }
        if (generationJob != null) {
            return
        }

        val token = ++generationToken
        _uiState.value = GeneratedPuzzleGenerationUiState.Loading(
            definition = definition,
            request = request,
            previousSession = previousSession
        )
        val job = viewModelScope.launch(start = CoroutineStart.LAZY) {
            val outcome = definition.generationUseCase.generate(request = request)
            if (token != generationToken) {
                return@launch
            }

            val nextState = when (outcome) {
                is GeneratedPuzzleGenerationResult.Generated -> {
                    prepareGeneratedSession(
                        definition = definition,
                        outcome = outcome,
                        previousSession = previousSession
                    )
                }

                is GeneratedPuzzleGenerationResult.Failed -> {
                    GeneratedPuzzleGenerationUiState.Failed(
                        definition = definition,
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
        definition: GeneratedPuzzleGenerationDefinition,
        outcome: GeneratedPuzzleGenerationResult.Generated,
        previousSession: GeneratedModeGameSession?
    ): GeneratedPuzzleGenerationUiState {
        val sessionId = sessionIdSource.nextId()
        val snapshot = GeneratedSessionSnapshot(
            sessionId = sessionId,
            modeId = definition.challenge.modeId.value,
            profileId = outcome.request.profileId.value,
            seed = outcome.request.seed,
            initialPuzzle = outcome.initialPuzzle,
            currentPuzzle = outcome.initialPuzzle
        )

        return try {
            generatedSessionRepository.replace(snapshot)
            val session = GeneratedModeGameSession(
                challenge = definition.challenge,
                snapshot = snapshot,
                request = outcome.request
            )
            GeneratedPuzzleGenerationUiState.Ready(
                session = session,
                replacementTransition = previousSession?.let { predecessor ->
                    GeneratedPuzzleReplacementTransition(
                        predecessorSessionId = predecessor.id,
                        successorSessionId = session.id
                    )
                }
            )
        } catch (_: IOException) {
            GeneratedPuzzleGenerationUiState.Failed(
                definition = definition,
                request = outcome.request,
                failure = GeneratedPuzzlePreparationFailure.Persistence,
                previousSession = previousSession
            )
        }
    }

    private fun nextRequest(challenge: GeneratedChallenge): GeneratedPuzzleGenerationRequest =
        GeneratedPuzzleGenerationRequest(
            profile = challenge.profile,
            seed = seedSource.nextSeed()
        )
}

internal data class GeneratedPuzzleGenerationDefinition(
    val challenge: GeneratedChallenge,
    val generationUseCase: GeneratedPuzzleGenerationUseCase
)

internal data class GeneratedPuzzleReplacementTransition(
    val predecessorSessionId: GeneratedSessionId,
    val successorSessionId: GeneratedSessionId
) {
    init {
        require(predecessorSessionId != successorSessionId) {
            "A replacement transition requires distinct predecessor and successor sessions."
        }
    }
}

internal data class GeneratedModeGameSession(
    val challenge: GeneratedChallenge,
    val snapshot: GeneratedSessionSnapshot,
    val request: GeneratedPuzzleGenerationRequest
) {
    init {
        require(snapshot.modeId == challenge.modeId.value) {
            "Generated game session mode must match its challenge."
        }
        require(snapshot.profileId == challenge.profile.id.value) {
            "Generated game session profile must match its challenge."
        }
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

private fun GeneratedPuzzleGenerationUiState.withVisibleSession(
    session: GeneratedModeGameSession
): GeneratedPuzzleGenerationUiState = when (this) {
    is GeneratedPuzzleGenerationUiState.Ready -> copy(session = session)

    is GeneratedPuzzleGenerationUiState.Loading -> copy(previousSession = session)

    is GeneratedPuzzleGenerationUiState.Failed -> copy(previousSession = session)

    GeneratedPuzzleGenerationUiState.Idle,
    is GeneratedPuzzleGenerationUiState.Restoring,
    is GeneratedPuzzleGenerationUiState.ResumeUnavailable -> error(
        "Only a generated puzzle state with a visible session can replace that session."
    )
}
