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
import org.cescfe.numpairs.data.daily.session.DailySessionCompletionResult
import org.cescfe.numpairs.data.daily.session.DailySessionId
import org.cescfe.numpairs.data.daily.session.DailySessionProgressUpdateResult
import org.cescfe.numpairs.data.daily.session.DailySessionReplacementResult
import org.cescfe.numpairs.data.daily.session.DailySessionRepository
import org.cescfe.numpairs.data.daily.session.DailySessionSnapshot
import org.cescfe.numpairs.data.daily.session.requireValidActivePuzzle
import org.cescfe.numpairs.data.daily.session.requireValidSolvedPuzzle
import org.cescfe.numpairs.domain.daily.DailyCompletion
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

internal sealed interface DailyPuzzlePersistenceFailure {
    data object StaleSession : DailyPuzzlePersistenceFailure

    data object InvalidPuzzle : DailyPuzzlePersistenceFailure

    data object InvalidTiming : DailyPuzzlePersistenceFailure

    data object Persistence : DailyPuzzlePersistenceFailure
}

internal sealed interface DailyPuzzleCompletion {
    data class Completed(val completion: DailyCompletion) : DailyPuzzleCompletion

    data class AlreadyCompleted(val completion: DailyCompletion) : DailyPuzzleCompletion
}

internal sealed interface DailyPuzzleUiState {
    data object Idle : DailyPuzzleUiState

    data object Resolving : DailyPuzzleUiState

    data class Loading(val currentDailyChallenge: CurrentDailyChallenge) : DailyPuzzleUiState

    data class Ready(val session: DailyGameSession, val persistenceFailure: DailyPuzzlePersistenceFailure? = null) :
        DailyPuzzleUiState

    data class Completed(val session: DailyGameSession, val completion: DailyPuzzleCompletion) : DailyPuzzleUiState {
        init {
            require(session.currentPuzzle.isSolved) {
                "A completed Daily UI state requires a solved in-memory puzzle."
            }
            val completedIdentity = when (completion) {
                is DailyPuzzleCompletion.Completed -> completion.completion.identity
                is DailyPuzzleCompletion.AlreadyCompleted -> completion.completion.identity
            }
            require(
                completedIdentity.localDate ==
                    session.currentDailyChallenge.identity.localDate
            ) {
                "A completed Daily result must own the captured local date."
            }
        }
    }

    data class CompletedToday(val currentDailyChallenge: CurrentDailyChallenge, val completion: DailyCompletion) :
        DailyPuzzleUiState {
        init {
            require(completion.identity.localDate == currentDailyChallenge.identity.localDate) {
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
    private var sessionWriteJob: Job? = null
    private var persistenceRevision: Int = 0

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
        persistenceRevision++
        _uiState.value = DailyPuzzleUiState.Idle
    }

    fun retry() {
        val failedState = _uiState.value as? DailyPuzzleUiState.Failed ?: return
        startGeneration(currentDailyChallenge = failedState.currentDailyChallenge)
    }

    fun onCommittedPuzzleChanged(expectedSessionId: DailySessionId, puzzle: Puzzle) {
        val state = _uiState.value as? DailyPuzzleUiState.Ready ?: return
        val visibleSession = state.session
        if (
            visibleSession.id != expectedSessionId ||
            visibleSession.currentPuzzle == puzzle ||
            visibleSession.currentPuzzle.isSolved
        ) {
            return
        }

        val updatedSession = try {
            visibleSession.withCurrentPuzzle(puzzle)
        } catch (_: IllegalArgumentException) {
            persistenceRevision++
            publishPersistenceFailure(
                expectedSessionId = expectedSessionId,
                failure = DailyPuzzlePersistenceFailure.InvalidPuzzle
            )
            return
        } catch (_: IllegalStateException) {
            persistenceRevision++
            publishPersistenceFailure(
                expectedSessionId = expectedSessionId,
                failure = DailyPuzzlePersistenceFailure.InvalidPuzzle
            )
            return
        }

        _uiState.value = DailyPuzzleUiState.Ready(session = updatedSession)
        enqueuePersistence(session = updatedSession)
    }

    fun retryPersistence() {
        val readyState = _uiState.value as? DailyPuzzleUiState.Ready ?: return
        if (readyState.persistenceFailure == null) {
            return
        }
        _uiState.value = readyState.copy(persistenceFailure = null)
        enqueuePersistence(session = readyState.session)
    }

    private fun resolveAndPrepare() {
        val token = ++preparationToken
        _uiState.value = DailyPuzzleUiState.Resolving
        val job = viewModelScope.launch(start = CoroutineStart.LAZY) {
            sessionWriteJob?.join()
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

    private fun enqueuePersistence(session: DailyGameSession) {
        val revision = ++persistenceRevision
        val precedingWrite = sessionWriteJob
        val job = viewModelScope.launch(start = CoroutineStart.LAZY) {
            precedingWrite?.join()
            val failure = try {
                if (session.currentPuzzle.isSolved) {
                    persistCompletion(session = session)
                } else {
                    persistProgress(session = session)
                }
            } catch (_: IOException) {
                DailyPuzzlePersistenceFailure.Persistence
            }
            if (
                failure != null &&
                revision == persistenceRevision
            ) {
                publishPersistenceFailure(
                    expectedSessionId = session.id,
                    failure = failure
                )
            }
        }
        sessionWriteJob = job
        job.start()
    }

    private suspend fun persistProgress(session: DailyGameSession): DailyPuzzlePersistenceFailure? = when (
        dailySessionRepository.updateCurrentPuzzle(
            expectedSessionId = session.id,
            puzzle = session.currentPuzzle
        )
    ) {
        DailySessionProgressUpdateResult.Updated -> null

        DailySessionProgressUpdateResult.StaleSession -> {
            DailyPuzzlePersistenceFailure.StaleSession
        }

        DailySessionProgressUpdateResult.InvalidPuzzle -> {
            DailyPuzzlePersistenceFailure.InvalidPuzzle
        }
    }

    private suspend fun persistCompletion(session: DailyGameSession): DailyPuzzlePersistenceFailure? = when (
        val result = dailySessionRepository.complete(
            expectedSessionId = session.id,
            expectedDailyChallengeId = session.currentDailyChallenge.identity,
            solvedPuzzle = session.currentPuzzle
        )
    ) {
        is DailySessionCompletionResult.Completed -> {
            publishCompletion(
                expectedSessionId = session.id,
                completion = DailyPuzzleCompletion.Completed(
                    completion = result.completion
                )
            )
            null
        }

        is DailySessionCompletionResult.AlreadyCompleted -> {
            publishCompletion(
                expectedSessionId = session.id,
                completion = DailyPuzzleCompletion.AlreadyCompleted(
                    completion = result.completion
                )
            )
            null
        }

        DailySessionCompletionResult.StaleSession -> {
            DailyPuzzlePersistenceFailure.StaleSession
        }

        DailySessionCompletionResult.InvalidPuzzle -> {
            DailyPuzzlePersistenceFailure.InvalidPuzzle
        }

        DailySessionCompletionResult.InvalidTiming -> {
            DailyPuzzlePersistenceFailure.InvalidTiming
        }
    }

    private fun publishCompletion(expectedSessionId: DailySessionId, completion: DailyPuzzleCompletion) {
        val readyState = _uiState.value as? DailyPuzzleUiState.Ready ?: return
        if (
            readyState.session.id == expectedSessionId &&
            readyState.session.currentPuzzle.isSolved
        ) {
            _uiState.value = DailyPuzzleUiState.Completed(
                session = readyState.session,
                completion = completion
            )
        }
    }

    private fun publishPersistenceFailure(expectedSessionId: DailySessionId, failure: DailyPuzzlePersistenceFailure) {
        val readyState = _uiState.value as? DailyPuzzleUiState.Ready ?: return
        if (readyState.session.id == expectedSessionId) {
            _uiState.value = readyState.copy(persistenceFailure = failure)
        }
    }
}

internal data class DailyGameSession(
    val currentDailyChallenge: CurrentDailyChallenge,
    val snapshot: DailySessionSnapshot,
    val currentPuzzle: Puzzle = snapshot.currentPuzzle
) {
    init {
        require(snapshot.dailyChallengeId == currentDailyChallenge.identity) {
            "Daily game session identity must match its captured current identity."
        }
        require(snapshot.recipeContract == currentDailyChallenge.recipe.contract) {
            "Daily game session recipe must match its captured current recipe."
        }
        if (currentPuzzle.isSolved) {
            snapshot.requireValidSolvedPuzzle(currentPuzzle)
        } else {
            snapshot.requireValidActivePuzzle(currentPuzzle)
        }
    }

    val id: DailySessionId
        get() = snapshot.sessionId

    val initialPuzzle: Puzzle
        get() = snapshot.initialPuzzle

    fun withCurrentPuzzle(puzzle: Puzzle): DailyGameSession = if (puzzle.isSolved) {
        copy(currentPuzzle = puzzle)
    } else {
        copy(
            snapshot = snapshot.copy(currentPuzzle = puzzle),
            currentPuzzle = puzzle
        )
    }
}
