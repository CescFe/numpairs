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
import org.cescfe.numpairs.data.generated.session.GeneratedSessionTimingStartResult
import org.cescfe.numpairs.domain.generated.GeneratedElapsedTime
import org.cescfe.numpairs.domain.generated.GeneratedTimingStartInstant
import org.cescfe.numpairs.domain.generated.generation.GeneratedPuzzleGenerationRequest
import org.cescfe.numpairs.domain.puzzle.model.Puzzle
import org.cescfe.numpairs.feature.game.presentation.CommittedPuzzleMutation
import org.cescfe.numpairs.feature.time.ElapsedTimeReading
import org.cescfe.numpairs.feature.time.ElapsedTimeSource
import org.cescfe.numpairs.feature.time.SystemElapsedTimeSource

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
        val replacementTransition: GeneratedPuzzleReplacementTransition? = null,
        val elapsedTime: GeneratedElapsedTime? = session.snapshot.completionElapsedTime,
        val hasPersistenceFailure: Boolean = false
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
    private val sessionIdSource: GeneratedSessionIdSource = UuidGeneratedSessionIdSource,
    private val timeSource: ElapsedTimeSource = SystemElapsedTimeSource
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
    private var visibleTimer: VisibleGeneratedTimer? = null
    private var pendingTimingStart: PendingGeneratedTimingStart? = null
    private val elapsedHighWaterBySessionId = mutableMapOf<GeneratedSessionId, GeneratedElapsedTime>()

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
        if (state.hasPersistenceFailure && state.session.currentPuzzle.isSolved) {
            return
        }
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

    fun onPuzzlePresented(expectedSessionId: GeneratedSessionId) {
        val state = _uiState.value as? GeneratedPuzzleGenerationUiState.Ready ?: return
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
            ?: GeneratedTimingStartInstant(reading.epochMilliseconds)
        val restoredElapsed = GeneratedElapsedTime(
            nonNegativeDifference(
                current = reading.epochMilliseconds,
                earlier = startInstant.epochMilliseconds
            )
        )
        val elapsedAtAnchor = maxElapsedTime(
            restoredElapsed,
            elapsedHighWaterBySessionId[expectedSessionId]
        )
        val session = state.session.withTimingStart(startInstant)
        visibleTimer = VisibleGeneratedTimer(
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
            pendingTimingStart = PendingGeneratedTimingStart(
                sessionId = expectedSessionId,
                startInstant = startInstant
            )
            enqueueSessionPersistence(session = session, timingStartOnly = true)
        }
    }

    fun onTimerRefresh(expectedSessionId: GeneratedSessionId) {
        val state = _uiState.value as? GeneratedPuzzleGenerationUiState.Ready ?: return
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

    fun retryPersistence() {
        val state = _uiState.value as? GeneratedPuzzleGenerationUiState.Ready ?: return
        if (!state.hasPersistenceFailure) {
            return
        }
        _uiState.value = state.copy(hasPersistenceFailure = false)
        enqueueSessionPersistence(session = state.session, timingStartOnly = false)
    }

    fun onPuzzleMutationCommitted(expectedSessionId: GeneratedSessionId, mutation: CommittedPuzzleMutation) {
        val completionElapsedTime = if (mutation.puzzle.isSolved) {
            captureCompletionElapsedTime(expectedSessionId)
        } else {
            null
        }
        val updatedSession = try {
            updateVisibleSession(
                expectedSessionId = expectedSessionId,
                mutation = mutation,
                completionElapsedTime = completionElapsedTime
            )
        } catch (_: IllegalArgumentException) {
            null
        } ?: run {
            return
        }

        enqueueSessionPersistence(session = updatedSession, timingStartOnly = false)
    }

    private fun updateVisibleSession(
        expectedSessionId: GeneratedSessionId,
        mutation: CommittedPuzzleMutation,
        completionElapsedTime: GeneratedElapsedTime?
    ): GeneratedModeGameSession? {
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
            visibleSession.currentPuzzle == mutation.puzzle ||
            visibleSession.currentPuzzle.isSolved
        ) {
            return null
        }

        val correctionCount = if (mutation.isCorrection) {
            visibleSession.snapshot.correctionCount?.incremented()
        } else {
            visibleSession.snapshot.correctionCount
        }
        val updatedSession = visibleSession.copy(
            snapshot = visibleSession.snapshot.copy(
                currentPuzzle = mutation.puzzle,
                correctionCount = correctionCount,
                completionElapsedTime = completionElapsedTime
            )
        )
        _uiState.value = state.withVisibleSession(
            session = updatedSession,
            elapsedTime = completionElapsedTime
                ?: (state as? GeneratedPuzzleGenerationUiState.Ready)?.elapsedTime
        )
        return updatedSession
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
            sessionWriteJob?.join()
            if (previousSession != null && !persistPendingTimingStart(previousSession.id)) {
                return GeneratedPuzzleGenerationUiState.Failed(
                    definition = definition,
                    request = outcome.request,
                    failure = GeneratedPuzzlePreparationFailure.Persistence,
                    previousSession = previousSession
                )
            }
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

    private fun enqueueSessionPersistence(session: GeneratedModeGameSession, timingStartOnly: Boolean) {
        val precedingWrite = sessionWriteJob
        sessionWriteJob = viewModelScope.launch {
            precedingWrite?.join()
            try {
                if (!persistPendingTimingStart(session.id)) {
                    publishPersistenceFailure(session.id)
                    return@launch
                }
                if (!timingStartOnly) {
                    val wasUpdated = generatedSessionRepository.updateCurrentPuzzle(
                        expectedSessionId = session.id,
                        puzzle = session.currentPuzzle,
                        correctionCount = session.snapshot.correctionCount,
                        completionElapsedTime = session.snapshot.completionElapsedTime
                    )
                    if (!wasUpdated) {
                        publishPersistenceFailure(session.id)
                        return@launch
                    }
                    if (session.currentPuzzle.isSolved && !generatedSessionRepository.clear(session.id)) {
                        publishPersistenceFailure(session.id)
                        return@launch
                    }
                }
                clearPersistenceFailure(session.id)
            } catch (_: IOException) {
                publishPersistenceFailure(session.id)
            }
        }
    }

    private suspend fun persistPendingTimingStart(expectedSessionId: GeneratedSessionId): Boolean {
        val pending = pendingTimingStart
            ?.takeIf { pendingStart -> pendingStart.sessionId == expectedSessionId }
            ?: return true
        return when (
            val result = generatedSessionRepository.startTiming(
                expectedSessionId = expectedSessionId,
                startInstant = pending.startInstant
            )
        ) {
            is GeneratedSessionTimingStartResult.Started -> {
                acceptPersistedTimingStart(expectedSessionId, result.startInstant)
                true
            }

            is GeneratedSessionTimingStartResult.AlreadyStarted -> {
                acceptPersistedTimingStart(expectedSessionId, result.startInstant)
                true
            }

            GeneratedSessionTimingStartResult.StaleSession -> false
        }
    }

    private fun acceptPersistedTimingStart(sessionId: GeneratedSessionId, startInstant: GeneratedTimingStartInstant) {
        val pending = pendingTimingStart ?: return
        if (pending.sessionId != sessionId) {
            return
        }
        pendingTimingStart = null
        val state = _uiState.value as? GeneratedPuzzleGenerationUiState.Ready ?: return
        if (state.session.id != sessionId || state.session.snapshot.timingStartInstant == startInstant) {
            return
        }

        val session = state.session.withTimingStart(startInstant, replaceExisting = true)
        if (session.currentPuzzle.isSolved) {
            _uiState.value = state.copy(session = session)
            return
        }
        val reading = timeSource.read()
        val elapsedTime = maxElapsedTime(
            GeneratedElapsedTime(
                nonNegativeDifference(reading.epochMilliseconds, startInstant.epochMilliseconds)
            ),
            state.elapsedTime,
            elapsedHighWaterBySessionId[sessionId]
        )
        elapsedHighWaterBySessionId[sessionId] = elapsedTime
        visibleTimer = VisibleGeneratedTimer(
            sessionId = sessionId,
            anchorMonotonicMilliseconds = reading.monotonicMilliseconds,
            elapsedAtAnchor = elapsedTime,
            highWater = elapsedTime
        )
        _uiState.value = state.copy(session = session, elapsedTime = elapsedTime)
    }

    private fun captureCompletionElapsedTime(sessionId: GeneratedSessionId): GeneratedElapsedTime? {
        val state = _uiState.value as? GeneratedPuzzleGenerationUiState.Ready ?: return null
        if (state.session.id != sessionId || state.session.currentPuzzle.isSolved) {
            return state.session.snapshot.completionElapsedTime
        }
        val timer = visibleTimer
        val measured = when {
            timer?.sessionId == sessionId -> timer.readElapsed(timeSource.read())

            state.session.snapshot.timingStartInstant != null -> {
                val reading = timeSource.read()
                GeneratedElapsedTime(
                    nonNegativeDifference(
                        reading.epochMilliseconds,
                        state.session.snapshot.timingStartInstant.epochMilliseconds
                    )
                )
            }

            else -> null
        }
        visibleTimer = null
        return measured?.let { elapsedTime ->
            maxElapsedTime(
                elapsedTime,
                state.elapsedTime,
                elapsedHighWaterBySessionId[sessionId]
            ).also { frozen ->
                elapsedHighWaterBySessionId[sessionId] = frozen
            }
        }
    }

    private fun publishPersistenceFailure(expectedSessionId: GeneratedSessionId) {
        val state = _uiState.value as? GeneratedPuzzleGenerationUiState.Ready ?: return
        if (state.session.id == expectedSessionId) {
            _uiState.value = state.copy(hasPersistenceFailure = true)
        }
    }

    private fun clearPersistenceFailure(expectedSessionId: GeneratedSessionId) {
        val state = _uiState.value as? GeneratedPuzzleGenerationUiState.Ready ?: return
        if (state.session.id == expectedSessionId && state.hasPersistenceFailure) {
            _uiState.value = state.copy(hasPersistenceFailure = false)
        }
    }
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

    fun withTimingStart(
        startInstant: GeneratedTimingStartInstant,
        replaceExisting: Boolean = false
    ): GeneratedModeGameSession = copy(
        snapshot = snapshot.copy(
            timingStartInstant = if (replaceExisting) {
                startInstant
            } else {
                snapshot.timingStartInstant ?: startInstant
            }
        )
    )
}

private fun GeneratedPuzzleGenerationUiState.withVisibleSession(
    session: GeneratedModeGameSession,
    elapsedTime: GeneratedElapsedTime?
): GeneratedPuzzleGenerationUiState = when (this) {
    is GeneratedPuzzleGenerationUiState.Ready -> copy(session = session, elapsedTime = elapsedTime)

    is GeneratedPuzzleGenerationUiState.Loading -> copy(previousSession = session)

    is GeneratedPuzzleGenerationUiState.Failed -> copy(previousSession = session)

    GeneratedPuzzleGenerationUiState.Idle,
    is GeneratedPuzzleGenerationUiState.Restoring,
    is GeneratedPuzzleGenerationUiState.ResumeUnavailable -> error(
        "Only a generated puzzle state with a visible session can replace that session."
    )
}

private data class PendingGeneratedTimingStart(
    val sessionId: GeneratedSessionId,
    val startInstant: GeneratedTimingStartInstant
)

private data class VisibleGeneratedTimer(
    val sessionId: GeneratedSessionId,
    val anchorMonotonicMilliseconds: Long,
    val elapsedAtAnchor: GeneratedElapsedTime,
    var highWater: GeneratedElapsedTime
) {
    fun readElapsed(reading: ElapsedTimeReading): GeneratedElapsedTime {
        val monotonicDelta = nonNegativeDifference(
            current = reading.monotonicMilliseconds,
            earlier = anchorMonotonicMilliseconds
        )
        val measuredElapsed = GeneratedElapsedTime(
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

private fun maxElapsedTime(
    first: GeneratedElapsedTime,
    second: GeneratedElapsedTime?,
    third: GeneratedElapsedTime? = null
): GeneratedElapsedTime = listOf(second, third).fold(first) { maximum, candidate ->
    if (candidate != null && candidate.milliseconds > maximum.milliseconds) candidate else maximum
}
