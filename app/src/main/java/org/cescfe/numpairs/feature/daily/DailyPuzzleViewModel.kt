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
import org.cescfe.numpairs.data.daily.session.DailySessionTimingStartResult
import org.cescfe.numpairs.data.daily.session.requireValidActivePuzzle
import org.cescfe.numpairs.data.daily.session.requireValidSolvedPuzzle
import org.cescfe.numpairs.domain.daily.DailyCompletion
import org.cescfe.numpairs.domain.daily.DailyElapsedTime
import org.cescfe.numpairs.domain.daily.DailyMovementCount
import org.cescfe.numpairs.domain.daily.DailyPersonalBestHistory
import org.cescfe.numpairs.domain.daily.DailyPersonalBestResult
import org.cescfe.numpairs.domain.daily.DailyTimingStartInstant
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

    data object InvalidMovement : DailyPuzzlePersistenceFailure

    data object Persistence : DailyPuzzlePersistenceFailure
}

internal sealed interface DailyPuzzleCompletion {
    val completion: DailyCompletion
    val personalBestResult: DailyPersonalBestResult

    data class Completed(
        override val completion: DailyCompletion,
        override val personalBestResult: DailyPersonalBestResult
    ) : DailyPuzzleCompletion {
        init {
            require(personalBestResult.currentElapsedTime == completion.elapsedTime) {
                "A persisted Daily completion must preserve its frozen personal-best duration."
            }
        }
    }

    data class AlreadyCompleted(
        override val completion: DailyCompletion,
        override val personalBestResult: DailyPersonalBestResult
    ) : DailyPuzzleCompletion {
        init {
            require(personalBestResult.currentElapsedTime == completion.elapsedTime) {
                "An existing Daily completion must preserve its derived personal-best duration."
            }
        }
    }
}

internal sealed interface DailyPuzzleUiState {
    data object Idle : DailyPuzzleUiState

    data object Resolving : DailyPuzzleUiState

    data class Loading(val currentDailyChallenge: CurrentDailyChallenge) : DailyPuzzleUiState

    data class Ready(
        val session: DailyGameSession,
        val elapsedTime: DailyElapsedTime? = null,
        val personalBestResult: DailyPersonalBestResult? = null,
        val persistenceFailure: DailyPuzzlePersistenceFailure? = null
    ) : DailyPuzzleUiState {
        init {
            require(session.currentPuzzle.isSolved == (personalBestResult != null)) {
                "A solved Daily session must freeze one personal-best result."
            }
            require(personalBestResult == null || personalBestResult.currentElapsedTime == elapsedTime) {
                "A frozen Daily personal-best result must use the visible completion duration."
            }
        }
    }

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
            require(completion.personalBestResult.currentElapsedTime == completion.completion.elapsedTime) {
                "A completed Daily result must preserve its authoritative duration."
            }
        }
    }

    data class CompletedToday(
        val currentDailyChallenge: CurrentDailyChallenge,
        val completion: DailyCompletion,
        val personalBestResult: DailyPersonalBestResult
    ) : DailyPuzzleUiState {
        init {
            require(completion.identity.localDate == currentDailyChallenge.identity.localDate) {
                "A completed Daily UI state must own the captured local date."
            }
            require(personalBestResult.currentElapsedTime == completion.elapsedTime) {
                "A completed-today Daily result must preserve its authoritative duration."
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
    private val sessionIdSource: DailySessionIdSource = UuidDailySessionIdSource,
    private val timeSource: DailyTimeSource = SystemDailyTimeSource
) : ViewModel() {
    private val _uiState = MutableStateFlow<DailyPuzzleUiState>(DailyPuzzleUiState.Idle)
    val uiState: StateFlow<DailyPuzzleUiState> = _uiState.asStateFlow()

    private var preparationJob: Job? = null
    private var preparationToken: Int = 0
    private var pendingSessionId: DailySessionId? = null
    private var sessionWriteJob: Job? = null
    private var persistenceRevision: Int = 0
    private var visibleTimer: VisibleDailyTimer? = null
    private var pendingTimingStart: PendingDailyTimingStart? = null
    private val elapsedHighWaterBySessionId = mutableMapOf<DailySessionId, DailyElapsedTime>()
    private var personalBestHistory: DailyPersonalBestHistory? = null

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
        visibleTimer = null
        _uiState.value = DailyPuzzleUiState.Idle
    }

    fun onPuzzlePresented(expectedSessionId: DailySessionId) {
        val state = _uiState.value as? DailyPuzzleUiState.Ready ?: return
        if (
            state.session.id != expectedSessionId ||
            state.session.currentPuzzle.isSolved ||
            visibleTimer?.sessionId == expectedSessionId
        ) {
            return
        }

        val reading = timeSource.read()
        val persistedStart = state.session.snapshot.timingStartInstant
        val pendingStart = pendingTimingStart
            ?.takeIf { pending -> pending.sessionId == expectedSessionId }
            ?.startInstant
        val startInstant = persistedStart
            ?: pendingStart
            ?: DailyTimingStartInstant(reading.epochMilliseconds)
        val session = state.session.withTimingStart(startInstant)
        val restoredElapsed = DailyElapsedTime(
            nonNegativeDifference(
                current = reading.epochMilliseconds,
                earlier = startInstant.epochMilliseconds
            )
        )
        val elapsedAtAnchor = maxElapsedTime(
            restoredElapsed,
            elapsedHighWaterBySessionId[expectedSessionId]
        )
        visibleTimer = VisibleDailyTimer(
            sessionId = expectedSessionId,
            anchorMonotonicMilliseconds = reading.monotonicMilliseconds,
            elapsedAtAnchor = elapsedAtAnchor,
            highWater = elapsedAtAnchor
        )
        elapsedHighWaterBySessionId[expectedSessionId] = elapsedAtAnchor
        _uiState.value = state.copy(
            session = session,
            elapsedTime = elapsedAtAnchor
        )

        if (persistedStart == null) {
            pendingTimingStart = PendingDailyTimingStart(
                sessionId = expectedSessionId,
                startInstant = startInstant
            )
            enqueuePersistence(
                session = session,
                elapsedTime = elapsedAtAnchor,
                timingStartOnly = true
            )
        }
    }

    fun onTimerRefresh(expectedSessionId: DailySessionId) {
        val state = _uiState.value as? DailyPuzzleUiState.Ready ?: return
        val timer = visibleTimer ?: return
        if (
            state.session.id != expectedSessionId ||
            state.session.currentPuzzle.isSolved ||
            timer.sessionId != expectedSessionId
        ) {
            return
        }

        val elapsedTime = timer.readElapsed(timeSource.read())
        elapsedHighWaterBySessionId[expectedSessionId] = elapsedTime
        if (elapsedTime != state.elapsedTime) {
            _uiState.value = state.copy(elapsedTime = elapsedTime)
        }
    }

    fun retry() {
        val failedState = _uiState.value as? DailyPuzzleUiState.Failed ?: return
        startGeneration(currentDailyChallenge = failedState.currentDailyChallenge)
    }

    fun onPuzzleMutationCommitted(expectedSessionId: DailySessionId, puzzle: Puzzle) {
        val state = _uiState.value as? DailyPuzzleUiState.Ready ?: return
        val visibleSession = state.session
        if (
            visibleSession.id != expectedSessionId ||
            visibleSession.currentPuzzle == puzzle ||
            visibleSession.currentPuzzle.isSolved
        ) {
            return
        }

        val movementCount = try {
            visibleSession.snapshot.movementCount?.incremented()
        } catch (_: IllegalArgumentException) {
            persistenceRevision++
            publishPersistenceFailure(
                expectedSessionId = expectedSessionId,
                failure = DailyPuzzlePersistenceFailure.InvalidMovement
            )
            return
        }
        val updatedSession = try {
            visibleSession.withCommittedPuzzleMutation(
                puzzle = puzzle,
                movementCount = movementCount
            )
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

        val elapsedTime = if (puzzle.isSolved) {
            captureCompletionElapsedTime(visibleSession)
        } else {
            state.elapsedTime
        }
        val personalBestResult = if (puzzle.isSolved) {
            requirePersonalBestHistory().resultFor(
                DailyCompletion(
                    identity = updatedSession.currentDailyChallenge.identity,
                    elapsedTime = elapsedTime,
                    movementCount = updatedSession.snapshot.movementCount
                )
            )
        } else {
            null
        }
        _uiState.value = DailyPuzzleUiState.Ready(
            session = updatedSession,
            elapsedTime = elapsedTime,
            personalBestResult = personalBestResult
        )
        enqueuePersistence(
            session = updatedSession,
            elapsedTime = elapsedTime,
            personalBestResult = personalBestResult
        )
    }

    fun retryPersistence() {
        val readyState = _uiState.value as? DailyPuzzleUiState.Ready ?: return
        if (readyState.persistenceFailure == null) {
            return
        }
        _uiState.value = readyState.copy(persistenceFailure = null)
        enqueuePersistence(
            session = readyState.session,
            elapsedTime = readyState.elapsedTime,
            personalBestResult = readyState.personalBestResult
        )
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
            personalBestHistory = availability.personalBestHistory
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
                        completion = availability.completion,
                        personalBestResult = availability.personalBestHistory.resultFor(
                            availability.completion
                        )
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
                        completion = replacement.completion,
                        personalBestResult = requirePersonalBestHistory().resultFor(
                            replacement.completion
                        )
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

    private fun enqueuePersistence(
        session: DailyGameSession,
        elapsedTime: DailyElapsedTime?,
        personalBestResult: DailyPersonalBestResult? = null,
        timingStartOnly: Boolean = false
    ) {
        val revision = ++persistenceRevision
        val precedingWrite = sessionWriteJob
        val job = viewModelScope.launch(start = CoroutineStart.LAZY) {
            precedingWrite?.join()
            val failure = try {
                val timingFailure = persistPendingTimingStart(session)
                if (timingFailure != null || timingStartOnly) {
                    timingFailure
                } else if (session.currentPuzzle.isSolved) {
                    persistCompletion(
                        session = session,
                        personalBestResult = requireNotNull(personalBestResult)
                    )
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

    private suspend fun persistPendingTimingStart(session: DailyGameSession): DailyPuzzlePersistenceFailure? {
        val pending = pendingTimingStart
            ?.takeIf { pendingStart -> pendingStart.sessionId == session.id }
            ?: return null
        return when (
            val result = dailySessionRepository.startTiming(
                expectedSessionId = pending.sessionId,
                startInstant = pending.startInstant
            )
        ) {
            is DailySessionTimingStartResult.Started -> {
                acceptPersistedTimingStart(
                    sessionId = pending.sessionId,
                    startInstant = result.startInstant
                )
                null
            }

            is DailySessionTimingStartResult.AlreadyStarted -> {
                acceptPersistedTimingStart(
                    sessionId = pending.sessionId,
                    startInstant = result.startInstant
                )
                null
            }

            DailySessionTimingStartResult.StaleSession -> {
                DailyPuzzlePersistenceFailure.StaleSession
            }
        }
    }

    private suspend fun persistProgress(session: DailyGameSession): DailyPuzzlePersistenceFailure? = when (
        dailySessionRepository.updateCurrentPuzzle(
            expectedSessionId = session.id,
            puzzle = session.currentPuzzle,
            movementCount = session.snapshot.movementCount
        )
    ) {
        DailySessionProgressUpdateResult.Updated -> null

        DailySessionProgressUpdateResult.StaleSession -> {
            DailyPuzzlePersistenceFailure.StaleSession
        }

        DailySessionProgressUpdateResult.InvalidPuzzle -> {
            DailyPuzzlePersistenceFailure.InvalidPuzzle
        }

        DailySessionProgressUpdateResult.InvalidMovement -> {
            DailyPuzzlePersistenceFailure.InvalidMovement
        }
    }

    private suspend fun persistCompletion(
        session: DailyGameSession,
        personalBestResult: DailyPersonalBestResult
    ): DailyPuzzlePersistenceFailure? {
        val frozenCompletion = DailyCompletion(
            identity = session.currentDailyChallenge.identity,
            elapsedTime = personalBestResult.currentElapsedTime,
            movementCount = session.snapshot.movementCount
        )
        return when (
            val result = dailySessionRepository.complete(
                expectedSessionId = session.id,
                expectedDailyChallengeId = frozenCompletion.identity,
                solvedPuzzle = session.currentPuzzle,
                movementCount = frozenCompletion.movementCount,
                elapsedTime = frozenCompletion.elapsedTime
            )
        ) {
            is DailySessionCompletionResult.Completed -> {
                check(result.completion == frozenCompletion) {
                    "Daily completion persistence must preserve the frozen result."
                }
                publishCompletion(
                    expectedSessionId = session.id,
                    completion = DailyPuzzleCompletion.Completed(
                        completion = result.completion,
                        personalBestResult = personalBestResult
                    )
                )
                null
            }

            is DailySessionCompletionResult.AlreadyCompleted -> {
                val authoritativePersonalBestResult = if (
                    result.completion == frozenCompletion
                ) {
                    personalBestResult
                } else {
                    requirePersonalBestHistory().resultFor(result.completion)
                }
                publishCompletion(
                    expectedSessionId = session.id,
                    completion = DailyPuzzleCompletion.AlreadyCompleted(
                        completion = result.completion,
                        personalBestResult = authoritativePersonalBestResult
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

            DailySessionCompletionResult.InvalidMovement -> {
                DailyPuzzlePersistenceFailure.InvalidMovement
            }
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

    private fun captureCompletionElapsedTime(session: DailyGameSession): DailyElapsedTime? {
        val timer = visibleTimer
        val elapsedTime = when {
            timer?.sessionId == session.id -> timer.readElapsed(timeSource.read())

            session.snapshot.timingStartInstant != null -> {
                val reading = timeSource.read()
                DailyElapsedTime(
                    nonNegativeDifference(
                        current = reading.epochMilliseconds,
                        earlier = session.snapshot.timingStartInstant.epochMilliseconds
                    )
                )
            }

            else -> null
        }
        if (elapsedTime != null) {
            val frozenElapsedTime = maxElapsedTime(
                elapsedTime,
                elapsedHighWaterBySessionId[session.id]
            )
            elapsedHighWaterBySessionId[session.id] = frozenElapsedTime
            visibleTimer = null
            return frozenElapsedTime
        }
        visibleTimer = null
        return null
    }

    private fun acceptPersistedTimingStart(sessionId: DailySessionId, startInstant: DailyTimingStartInstant) {
        val pending = pendingTimingStart ?: return
        if (pending.sessionId != sessionId) {
            return
        }
        pendingTimingStart = null

        val state = _uiState.value as? DailyPuzzleUiState.Ready ?: return
        if (state.session.id != sessionId) {
            return
        }
        val authoritativeSession = state.session.withTimingStart(
            startInstant = startInstant,
            replaceExisting = true
        )
        if (state.session.snapshot.timingStartInstant == startInstant) {
            return
        }
        if (authoritativeSession.currentPuzzle.isSolved) {
            _uiState.value = state.copy(session = authoritativeSession)
            return
        }

        val reading = timeSource.read()
        val restoredElapsed = DailyElapsedTime(
            nonNegativeDifference(
                current = reading.epochMilliseconds,
                earlier = startInstant.epochMilliseconds
            )
        )
        val elapsedTime = maxElapsedTime(
            restoredElapsed,
            state.elapsedTime,
            elapsedHighWaterBySessionId[sessionId]
        )
        elapsedHighWaterBySessionId[sessionId] = elapsedTime
        visibleTimer = VisibleDailyTimer(
            sessionId = sessionId,
            anchorMonotonicMilliseconds = reading.monotonicMilliseconds,
            elapsedAtAnchor = elapsedTime,
            highWater = elapsedTime
        )
        _uiState.value = state.copy(
            session = authoritativeSession,
            elapsedTime = elapsedTime
        )
    }

    private fun publishPersistenceFailure(expectedSessionId: DailySessionId, failure: DailyPuzzlePersistenceFailure) {
        val readyState = _uiState.value as? DailyPuzzleUiState.Ready ?: return
        if (readyState.session.id == expectedSessionId) {
            _uiState.value = readyState.copy(persistenceFailure = failure)
        }
    }

    private fun requirePersonalBestHistory(): DailyPersonalBestHistory = requireNotNull(personalBestHistory) {
        "Daily personal-best history must be resolved before gameplay starts."
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

    fun withCommittedPuzzleMutation(puzzle: Puzzle, movementCount: DailyMovementCount?): DailyGameSession =
        if (puzzle.isSolved) {
            copy(
                snapshot = snapshot.copy(movementCount = movementCount),
                currentPuzzle = puzzle
            )
        } else {
            copy(
                snapshot = snapshot.copy(
                    currentPuzzle = puzzle,
                    movementCount = movementCount
                ),
                currentPuzzle = puzzle
            )
        }

    fun withTimingStart(startInstant: DailyTimingStartInstant, replaceExisting: Boolean = false): DailyGameSession {
        val timingStartInstant = if (replaceExisting) {
            startInstant
        } else {
            snapshot.timingStartInstant ?: startInstant
        }
        return copy(
            snapshot = snapshot.copy(timingStartInstant = timingStartInstant)
        )
    }
}

private data class PendingDailyTimingStart(val sessionId: DailySessionId, val startInstant: DailyTimingStartInstant)

private data class VisibleDailyTimer(
    val sessionId: DailySessionId,
    val anchorMonotonicMilliseconds: Long,
    val elapsedAtAnchor: DailyElapsedTime,
    var highWater: DailyElapsedTime
) {
    fun readElapsed(reading: DailyTimeReading): DailyElapsedTime {
        val monotonicDelta = nonNegativeDifference(
            current = reading.monotonicMilliseconds,
            earlier = anchorMonotonicMilliseconds
        )
        val measuredElapsed = DailyElapsedTime(
            saturatedSum(elapsedAtAnchor.milliseconds, monotonicDelta)
        )
        highWater = maxElapsedTime(highWater, measuredElapsed)
        return highWater
    }
}

private fun nonNegativeDifference(current: Long, earlier: Long): Long = if (current >= earlier) {
    current - earlier
} else {
    0L
}

private fun saturatedSum(first: Long, second: Long): Long = if (Long.MAX_VALUE - first < second) {
    Long.MAX_VALUE
} else {
    first + second
}

private fun maxElapsedTime(first: DailyElapsedTime, second: DailyElapsedTime?): DailyElapsedTime =
    if (second != null && second.milliseconds > first.milliseconds) {
        second
    } else {
        first
    }

private fun maxElapsedTime(
    first: DailyElapsedTime,
    second: DailyElapsedTime?,
    third: DailyElapsedTime?
): DailyElapsedTime = maxElapsedTime(maxElapsedTime(first, second), third)
