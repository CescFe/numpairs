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
import org.cescfe.numpairs.data.generated.session.GeneratedSessionCompletionResult
import org.cescfe.numpairs.data.generated.session.GeneratedSessionId
import org.cescfe.numpairs.data.generated.session.GeneratedSessionRepository
import org.cescfe.numpairs.data.generated.session.GeneratedSessionSnapshot
import org.cescfe.numpairs.data.generated.session.GeneratedSessionTimingStartResult
import org.cescfe.numpairs.domain.generated.GeneratedElapsedTime
import org.cescfe.numpairs.domain.generated.GeneratedPersonalBestCategory
import org.cescfe.numpairs.domain.generated.GeneratedPersonalBestCategoryResolver
import org.cescfe.numpairs.domain.generated.GeneratedPersonalBestOutcome
import org.cescfe.numpairs.domain.generated.GeneratedPersonalBestResult
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
        val previousSession: GeneratedModeGameSession?,
        val previousPersonalBestResult: GeneratedPersonalBestResult? = null
    ) : GeneratedPuzzleGenerationUiState

    data class Ready(
        val session: GeneratedModeGameSession,
        val replacementTransition: GeneratedPuzzleReplacementTransition? = null,
        val elapsedTime: GeneratedElapsedTime? = session.snapshot.completionElapsedTime,
        val personalBests: Map<GeneratedPersonalBestCategory, GeneratedElapsedTime> = emptyMap(),
        val personalBestResult: GeneratedPersonalBestResult? = null,
        val hasPersistenceFailure: Boolean = false
    ) : GeneratedPuzzleGenerationUiState {
        init {
            require(replacementTransition == null || replacementTransition.successorSessionId == session.id) {
                "A replacement transition must target the ready session."
            }
            require(personalBestResult == null || session.currentPuzzle.isSolved) {
                "A generated personal-best result requires a solved visible session."
            }
            require(
                personalBestResult?.currentElapsedTime == null ||
                    personalBestResult.currentElapsedTime == session.snapshot.completionElapsedTime
            ) {
                "A generated personal-best result must use the visible frozen duration."
            }
        }
    }

    data class Failed(
        val definition: GeneratedPuzzleGenerationDefinition,
        val request: GeneratedPuzzleGenerationRequest,
        val failure: GeneratedPuzzlePreparationFailure,
        val previousSession: GeneratedModeGameSession?,
        val previousPersonalBestResult: GeneratedPersonalBestResult? = null
    ) : GeneratedPuzzleGenerationUiState

    data class ResumeUnavailable(val expectedSessionId: GeneratedSessionId) : GeneratedPuzzleGenerationUiState
}

internal class GeneratedPuzzleViewModel(
    private val challenge: GeneratedChallenge,
    private val generationUseCase: GeneratedPuzzleGenerationUseCase,
    private val generatedSessionRepository: GeneratedSessionRepository,
    private val seedSource: GeneratedPuzzleSeedSource = ThreadLocalGeneratedPuzzleSeedSource,
    private val sessionIdSource: GeneratedSessionIdSource = UuidGeneratedSessionIdSource,
    private val timeSource: ElapsedTimeSource = SystemElapsedTimeSource,
    private val personalBestCategoryResolver: GeneratedPersonalBestCategoryResolver =
        GeneratedChallengePersonalBestCategoryResolver()
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
    private var pendingPersonalRecordCelebrationSessionId: GeneratedSessionId? = null

    fun onRouteEntered(launchIntent: GeneratedModeLaunchIntent = GeneratedModeLaunchIntent.DefaultNewPuzzle) {
        if (launchIntent != activeLaunchIntent) {
            generationToken++
            generationJob?.cancel()
            generationJob = null
            activeLaunchIntent = launchIntent

            when (launchIntent) {
                is GeneratedModeLaunchIntent.NewPuzzle -> {
                    val ready = _uiState.value as? GeneratedPuzzleGenerationUiState.Ready
                    startGeneration(
                        definition = initialDefinition,
                        request = nextRequest(initialDefinition.challenge),
                        previousSession = ready?.session,
                        previousPersonalBestResult = ready?.personalBestResult
                    )
                }

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
                previousSession = state.previousSession,
                previousPersonalBestResult = state.previousPersonalBestResult
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
        pendingPersonalRecordCelebrationSessionId = null
    }

    fun retry() {
        val state = _uiState.value as? GeneratedPuzzleGenerationUiState.Failed ?: return
        startGeneration(
            definition = state.definition,
            request = nextRequest(state.definition.challenge),
            previousSession = state.previousSession,
            previousPersonalBestResult = state.previousPersonalBestResult
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
            previousSession = state.session,
            previousPersonalBestResult = state.personalBestResult
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
        enqueueSessionPersistence(
            session = state.session,
            timingStartOnly = false,
            personalBestResult = state.personalBestResult
        )
    }

    fun claimPersonalRecordCelebration(): Boolean {
        val expectedSessionId = pendingPersonalRecordCelebrationSessionId ?: return false
        val state = _uiState.value
        val (visibleSession, personalBestResult) = when (state) {
            is GeneratedPuzzleGenerationUiState.Ready -> state.session to state.personalBestResult

            is GeneratedPuzzleGenerationUiState.Loading -> state.previousSession to state.previousPersonalBestResult

            is GeneratedPuzzleGenerationUiState.Failed -> state.previousSession to state.previousPersonalBestResult

            GeneratedPuzzleGenerationUiState.Idle,
            is GeneratedPuzzleGenerationUiState.Restoring,
            is GeneratedPuzzleGenerationUiState.ResumeUnavailable -> null to null
        }
        if (
            visibleSession?.id != expectedSessionId ||
            !visibleSession.currentPuzzle.isSolved ||
            personalBestResult?.outcome != GeneratedPersonalBestOutcome.PERSONAL_RECORD
        ) {
            return false
        }

        pendingPersonalRecordCelebrationSessionId = null
        return true
    }

    fun onPuzzleMutationCommitted(expectedSessionId: GeneratedSessionId, mutation: CommittedPuzzleMutation) {
        val completionElapsedTime = if (mutation.puzzle.isSolved) {
            captureCompletionElapsedTime(expectedSessionId)
        } else {
            null
        }
        val personalBestResult = if (mutation.puzzle.isSolved) {
            freezePersonalBestResult(
                expectedSessionId = expectedSessionId,
                completionElapsedTime = completionElapsedTime
            )
        } else {
            null
        }
        val updatedSession = try {
            updateVisibleSession(
                expectedSessionId = expectedSessionId,
                mutation = mutation,
                completionElapsedTime = completionElapsedTime,
                personalBestResult = personalBestResult
            )
        } catch (_: IllegalArgumentException) {
            null
        } ?: run {
            return
        }

        if (personalBestResult?.outcome == GeneratedPersonalBestOutcome.PERSONAL_RECORD) {
            pendingPersonalRecordCelebrationSessionId = updatedSession.id
        }

        enqueueSessionPersistence(
            session = updatedSession,
            timingStartOnly = false,
            personalBestResult = personalBestResult
        )
    }

    private fun updateVisibleSession(
        expectedSessionId: GeneratedSessionId,
        mutation: CommittedPuzzleMutation,
        completionElapsedTime: GeneratedElapsedTime?,
        personalBestResult: GeneratedPersonalBestResult?
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
                ?: (state as? GeneratedPuzzleGenerationUiState.Ready)?.elapsedTime,
            personalBestResult = personalBestResult
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
            val repositoryState = generatedSessionRepository.state.first()
            val snapshot = repositoryState.activeSession
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
                    ),
                    personalBests = repositoryState.personalBests
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
        previousSession: GeneratedModeGameSession?,
        previousPersonalBestResult: GeneratedPersonalBestResult? = null
    ) {
        require(definition.challenge.profile.id == request.profileId) {
            "Generated puzzle definition must match its request profile."
        }
        if (generationJob != null) {
            return
        }
        pendingPersonalRecordCelebrationSessionId = null

        val token = ++generationToken
        _uiState.value = GeneratedPuzzleGenerationUiState.Loading(
            definition = definition,
            request = request,
            previousSession = previousSession,
            previousPersonalBestResult = previousPersonalBestResult
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
                        previousSession = previousSession,
                        previousPersonalBestResult = previousPersonalBestResult
                    )
                }

                is GeneratedPuzzleGenerationResult.Failed -> {
                    GeneratedPuzzleGenerationUiState.Failed(
                        definition = definition,
                        request = outcome.request,
                        failure = GeneratedPuzzlePreparationFailure.Generation(outcome),
                        previousSession = previousSession,
                        previousPersonalBestResult = previousPersonalBestResult
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
        previousSession: GeneratedModeGameSession?,
        previousPersonalBestResult: GeneratedPersonalBestResult?
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
                    previousSession = previousSession,
                    previousPersonalBestResult = previousPersonalBestResult
                )
            }
            generatedSessionRepository.replace(snapshot)
            val repositoryState = generatedSessionRepository.state.first()
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
                },
                personalBests = repositoryState.personalBests
            )
        } catch (_: IOException) {
            GeneratedPuzzleGenerationUiState.Failed(
                definition = definition,
                request = outcome.request,
                failure = GeneratedPuzzlePreparationFailure.Persistence,
                previousSession = previousSession,
                previousPersonalBestResult = previousPersonalBestResult
            )
        }
    }

    private fun nextRequest(challenge: GeneratedChallenge): GeneratedPuzzleGenerationRequest =
        GeneratedPuzzleGenerationRequest(
            profile = challenge.profile,
            seed = seedSource.nextSeed()
        )

    private fun enqueueSessionPersistence(
        session: GeneratedModeGameSession,
        timingStartOnly: Boolean,
        personalBestResult: GeneratedPersonalBestResult? = null
    ) {
        val precedingWrite = sessionWriteJob
        sessionWriteJob = viewModelScope.launch {
            precedingWrite?.join()
            try {
                if (!persistPendingTimingStart(session.id)) {
                    publishPersistenceFailure(session.id)
                    return@launch
                }
                if (!timingStartOnly) {
                    if (session.currentPuzzle.isSolved) {
                        val completionResult = generatedSessionRepository.complete(
                            expectedSessionId = session.id,
                            solvedPuzzle = session.currentPuzzle,
                            correctionCount = session.snapshot.correctionCount,
                            personalBestResult = requireNotNull(personalBestResult) {
                                "A solved generated session requires a frozen personal-best result."
                            }
                        )
                        if (completionResult != GeneratedSessionCompletionResult.Completed) {
                            publishPersistenceFailure(session.id)
                            return@launch
                        }
                    } else {
                        val wasUpdated = generatedSessionRepository.updateCurrentPuzzle(
                            expectedSessionId = session.id,
                            puzzle = session.currentPuzzle,
                            correctionCount = session.snapshot.correctionCount,
                            completionElapsedTime = null
                        )
                        if (!wasUpdated) {
                            publishPersistenceFailure(session.id)
                            return@launch
                        }
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

    private fun freezePersonalBestResult(
        expectedSessionId: GeneratedSessionId,
        completionElapsedTime: GeneratedElapsedTime?
    ): GeneratedPersonalBestResult? {
        val state = _uiState.value as? GeneratedPuzzleGenerationUiState.Ready ?: return null
        if (state.session.id != expectedSessionId || state.session.currentPuzzle.isSolved) {
            return state.personalBestResult
        }
        val snapshot = state.session.snapshot
        val category = personalBestCategoryResolver.categoryFor(
            modeId = snapshot.modeId,
            profileId = snapshot.profileId
        )
        return GeneratedPersonalBestResult.classify(
            category = category,
            currentElapsedTime = completionElapsedTime,
            previousBestElapsedTime = category?.let(state.personalBests::get)
        )
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
    elapsedTime: GeneratedElapsedTime?,
    personalBestResult: GeneratedPersonalBestResult?
): GeneratedPuzzleGenerationUiState = when (this) {
    is GeneratedPuzzleGenerationUiState.Ready -> copy(
        session = session,
        elapsedTime = elapsedTime,
        personalBestResult = personalBestResult
    )

    is GeneratedPuzzleGenerationUiState.Loading -> copy(
        previousSession = session,
        previousPersonalBestResult = personalBestResult ?: previousPersonalBestResult
    )

    is GeneratedPuzzleGenerationUiState.Failed -> copy(
        previousSession = session,
        previousPersonalBestResult = personalBestResult ?: previousPersonalBestResult
    )

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
