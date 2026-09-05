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
import org.cescfe.numpairs.data.generated.session.GeneratedSessionCompletionResult
import org.cescfe.numpairs.data.generated.session.GeneratedSessionId
import org.cescfe.numpairs.data.generated.session.GeneratedSessionRepository
import org.cescfe.numpairs.data.generated.session.GeneratedSessionSnapshot
import org.cescfe.numpairs.data.generated.session.GeneratedSessionState
import org.cescfe.numpairs.data.generated.session.GeneratedSessionTimingStartResult
import org.cescfe.numpairs.data.puzzle.seed.samplePuzzle
import org.cescfe.numpairs.domain.generated.GeneratedElapsedTime
import org.cescfe.numpairs.domain.generated.GeneratedPersonalBestCategory
import org.cescfe.numpairs.domain.generated.GeneratedPersonalBestOutcome
import org.cescfe.numpairs.domain.generated.GeneratedPersonalBestResult
import org.cescfe.numpairs.domain.generated.GeneratedTimingStartInstant
import org.cescfe.numpairs.domain.generated.generation.GeneratedPairsPuzzleGenerationFailureReason
import org.cescfe.numpairs.domain.generated.generation.GeneratedPairsPuzzleGenerationOutcome
import org.cescfe.numpairs.domain.generated.generation.GeneratedPuzzleGenerationRequest
import org.cescfe.numpairs.domain.puzzle.PuzzleCorrectionCount
import org.cescfe.numpairs.domain.puzzle.model.Expression
import org.cescfe.numpairs.domain.puzzle.model.Operator
import org.cescfe.numpairs.domain.puzzle.model.Puzzle
import org.cescfe.numpairs.feature.game.presentation.CommittedPuzzleMutation
import org.cescfe.numpairs.feature.game.presentation.support.solvedPuzzleWithKnownStripAndAssignments
import org.cescfe.numpairs.feature.time.ElapsedTimeReading
import org.cescfe.numpairs.feature.time.ElapsedTimeSource
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertThrows
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
            challenge = GeneratedModes.FOUR_PAIRS_LOW,
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
        assertEquals(GeneratedModes.FOUR_PAIRS_LOW.profile.id.value, attemptedSnapshot.profileId)
        assertEquals(17, attemptedSnapshot.seed)
        assertEquals(samplePuzzle, attemptedSnapshot.initialPuzzle)
        assertEquals(samplePuzzle, attemptedSnapshot.currentPuzzle)
        assertEquals(0L, requireNotNull(attemptedSnapshot.correctionCount).value)
        assertNull(repository.session.value)

        writeGate.complete(Unit)
        dispatcher.scheduler.advanceUntilIdle()

        val ready = viewModel.uiState.value as GeneratedPuzzleGenerationUiState.Ready
        assertEquals(attemptedSnapshot, repository.session.value)
        assertEquals(attemptedSnapshot, ready.session.snapshot)
        assertEquals(GeneratedModes.FOUR_PAIRS_LOW, ready.session.challenge)
        assertNull(ready.replacementTransition)
    }

    @Test
    fun hard_replay_keeps_the_exact_mode_and_profile_for_the_successor_session() {
        val requests = mutableListOf<GeneratedPuzzleGenerationRequest>()
        val repository = RecordingGeneratedSessionRepository()
        val viewModel = GeneratedPuzzleViewModel(
            challenge = GeneratedModes.EIGHT_PAIRS_HARD,
            generationUseCase = { request ->
                requests += request
                GeneratedPuzzleGenerationResult.Generated(
                    request = request,
                    initialPuzzle = samplePuzzle
                )
            },
            generatedSessionRepository = repository,
            seedSource = QueueGeneratedPuzzleSeedSource(71, 73),
            sessionIdSource = QueueGeneratedSessionIdSource("hard-first", "hard-replay")
        )

        viewModel.onRouteEntered()
        dispatcher.scheduler.advanceUntilIdle()
        viewModel.onNewPuzzleRequested()
        dispatcher.scheduler.advanceUntilIdle()

        val ready = viewModel.uiState.value as GeneratedPuzzleGenerationUiState.Ready
        assertEquals(listOf(71, 73), requests.map(GeneratedPuzzleGenerationRequest::seed))
        assertTrue(requests.all { request -> request.profileId == GeneratedModes.EIGHT_PAIRS_HARD.profile.id })
        assertEquals(GeneratedSessionId("hard-replay"), ready.session.id)
        assertEquals(GeneratedModes.EIGHT_PAIRS_HARD, ready.session.challenge)
        assertEquals(GeneratedModes.EIGHT_PAIRS.id.value, ready.session.snapshot.modeId)
        assertEquals(GeneratedModes.EIGHT_PAIRS_HARD.profile.id.value, ready.session.snapshot.profileId)
        assertEquals(ready.session.snapshot, repository.session.value)
    }

    @Test
    fun quick_replay_adopts_one_fresh_exact_challenge_while_the_previous_session_remains_visible() {
        val replacementPuzzle = CompletableDeferred<Puzzle>()
        val replacementRequests = mutableListOf<GeneratedPuzzleGenerationRequest>()
        val repository = RecordingGeneratedSessionRepository()
        val viewModel = GeneratedPuzzleViewModel(
            challenge = GeneratedModes.FOUR_PAIRS_LOW,
            generationUseCase = generatedPuzzleUseCase(),
            generatedSessionRepository = repository,
            seedSource = QueueGeneratedPuzzleSeedSource(79, 83),
            sessionIdSource = QueueGeneratedSessionIdSource("quick-four", "quick-three")
        )
        viewModel.onRouteEntered()
        dispatcher.scheduler.advanceUntilIdle()
        val previousSession = (viewModel.uiState.value as GeneratedPuzzleGenerationUiState.Ready).session
        var replacementSelections = 0

        val replacementDefinition = {
            replacementSelections += 1
            GeneratedPuzzleGenerationDefinition(
                challenge = GeneratedModes.THREE_PAIRS_LOW,
                generationUseCase = { request ->
                    replacementRequests += request
                    GeneratedPuzzleGenerationResult.Generated(
                        request = request,
                        initialPuzzle = replacementPuzzle.await()
                    )
                }
            )
        }
        viewModel.onNewPuzzleRequested(replacementDefinition)
        viewModel.onNewPuzzleRequested(replacementDefinition)
        dispatcher.scheduler.runCurrent()

        val loading = viewModel.uiState.value as GeneratedPuzzleGenerationUiState.Loading
        assertEquals(1, replacementSelections)
        assertEquals(previousSession, loading.previousSession)
        assertEquals(previousSession.snapshot, repository.session.value)

        replacementPuzzle.complete(samplePuzzle)
        dispatcher.scheduler.advanceUntilIdle()

        val ready = viewModel.uiState.value as GeneratedPuzzleGenerationUiState.Ready
        assertEquals(listOf(83), replacementRequests.map(GeneratedPuzzleGenerationRequest::seed))
        assertEquals(GeneratedModes.THREE_PAIRS.id.value, ready.session.snapshot.modeId)
        assertEquals(GeneratedModes.THREE_PAIRS_LOW.profile.id.value, ready.session.snapshot.profileId)
        assertEquals(GeneratedModes.THREE_PAIRS_LOW, ready.session.challenge)
        assertEquals(GeneratedSessionId("quick-three"), ready.session.id)
        assertEquals(ready.session.snapshot, repository.session.value)
    }

    @Test
    fun retry_reuses_the_exact_weighted_quick_challenge_without_selecting_again() {
        var replacementSelections = 0
        var replacementAttempts = 0
        val repository = RecordingGeneratedSessionRepository()
        val viewModel = GeneratedPuzzleViewModel(
            challenge = GeneratedModes.FOUR_PAIRS_MEDIUM,
            generationUseCase = generatedPuzzleUseCase(),
            generatedSessionRepository = repository,
            seedSource = QueueGeneratedPuzzleSeedSource(89, 97, 101),
            sessionIdSource = QueueGeneratedSessionIdSource("quick-initial", "quick-retry")
        )
        viewModel.onRouteEntered()
        dispatcher.scheduler.advanceUntilIdle()

        viewModel.onNewPuzzleRequested {
            replacementSelections += 1
            GeneratedPuzzleGenerationDefinition(
                challenge = GeneratedModes.THREE_PAIRS_MEDIUM,
                generationUseCase = { request ->
                    replacementAttempts += 1
                    if (replacementAttempts == 1) {
                        GeneratedPuzzleGenerationResult.Failed(
                            GeneratedPairsPuzzleGenerationOutcome.Failed(
                                request = request,
                                attemptsUsed = 1,
                                searchWorkConsumed = 12,
                                reason = GeneratedPairsPuzzleGenerationFailureReason.AttemptsExhausted,
                                candidateRejections = emptyList()
                            )
                        )
                    } else {
                        GeneratedPuzzleGenerationResult.Generated(
                            request = request,
                            initialPuzzle = samplePuzzle
                        )
                    }
                }
            )
        }
        dispatcher.scheduler.advanceUntilIdle()

        val failed = viewModel.uiState.value as GeneratedPuzzleGenerationUiState.Failed
        assertEquals(GeneratedModes.THREE_PAIRS_MEDIUM, failed.definition.challenge)
        viewModel.retry()
        dispatcher.scheduler.advanceUntilIdle()

        val ready = viewModel.uiState.value as GeneratedPuzzleGenerationUiState.Ready
        assertEquals(1, replacementSelections)
        assertEquals(2, replacementAttempts)
        assertEquals(GeneratedModes.THREE_PAIRS.id.value, ready.session.snapshot.modeId)
        assertEquals(GeneratedModes.THREE_PAIRS_MEDIUM.profile.id.value, ready.session.snapshot.profileId)
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
            challenge = GeneratedModes.FOUR_PAIRS_LOW,
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
        assertEquals(
            GeneratedPuzzleReplacementTransition(
                predecessorSessionId = initialSession.id,
                successorSessionId = ready.session.id
            ),
            ready.replacementTransition
        )
        viewModel.onReplacementTransitionConsumed(requireNotNull(ready.replacementTransition))
        assertNull(
            (viewModel.uiState.value as GeneratedPuzzleGenerationUiState.Ready).replacementTransition
        )
    }

    @Test
    fun replacement_readiness_and_transition_metadata_wait_for_successful_adoption() {
        val replacementWriteGate = CompletableDeferred<Unit>()
        val repository = RecordingGeneratedSessionRepository()
        val viewModel = GeneratedPuzzleViewModel(
            challenge = GeneratedModes.FOUR_PAIRS_LOW,
            generationUseCase = QueueGeneratedPuzzleUseCase(
                CompletableDeferred(samplePuzzle),
                CompletableDeferred(samplePuzzle)
            ),
            generatedSessionRepository = repository,
            seedSource = QueueGeneratedPuzzleSeedSource(101, 202),
            sessionIdSource = QueueGeneratedSessionIdSource("previous", "successor")
        )
        viewModel.onRouteEntered()
        dispatcher.scheduler.advanceUntilIdle()
        val previousSession = (viewModel.uiState.value as GeneratedPuzzleGenerationUiState.Ready).session
        repository.nextReplaceGate = replacementWriteGate

        viewModel.onNewPuzzleRequested()
        dispatcher.scheduler.runCurrent()

        val loading = viewModel.uiState.value as GeneratedPuzzleGenerationUiState.Loading
        assertEquals(previousSession, loading.previousSession)
        assertEquals(previousSession.snapshot, repository.session.value)
        assertEquals(GeneratedSessionId("successor"), repository.replaceAttempts.last().sessionId)

        replacementWriteGate.complete(Unit)
        dispatcher.scheduler.advanceUntilIdle()

        val ready = viewModel.uiState.value as GeneratedPuzzleGenerationUiState.Ready
        assertEquals(GeneratedSessionId("successor"), ready.session.id)
        assertEquals(ready.session.snapshot, repository.session.value)
        assertEquals(
            GeneratedPuzzleReplacementTransition(
                predecessorSessionId = previousSession.id,
                successorSessionId = ready.session.id
            ),
            ready.replacementTransition
        )
    }

    @Test
    fun replacement_transition_requires_distinct_ids_and_the_ready_successor() {
        val readySnapshot = generatedSessionSnapshot(sessionId = "ready")
        val readySession = GeneratedModeGameSession(
            challenge = GeneratedModes.FOUR_PAIRS_LOW,
            snapshot = readySnapshot,
            request = GeneratedPuzzleGenerationRequest(
                profile = GeneratedModes.FOUR_PAIRS_LOW.profile,
                seed = readySnapshot.seed
            )
        )

        assertThrows(IllegalArgumentException::class.java) {
            GeneratedPuzzleReplacementTransition(
                predecessorSessionId = readySession.id,
                successorSessionId = readySession.id
            )
        }
        assertThrows(IllegalArgumentException::class.java) {
            GeneratedPuzzleGenerationUiState.Ready(
                session = readySession,
                replacementTransition = GeneratedPuzzleReplacementTransition(
                    predecessorSessionId = GeneratedSessionId("previous"),
                    successorSessionId = GeneratedSessionId("different-successor")
                )
            )
        }
    }

    @Test
    fun generation_cancellation_does_not_replace_the_stored_session() {
        val generatedPuzzle = CompletableDeferred<Puzzle>()
        val existingSnapshot = generatedSessionSnapshot(sessionId = "existing")
        val repository = RecordingGeneratedSessionRepository(initialSession = existingSnapshot)
        val viewModel = GeneratedPuzzleViewModel(
            challenge = GeneratedModes.FOUR_PAIRS_LOW,
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
            challenge = GeneratedModes.FOUR_PAIRS_LOW,
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
            challenge = GeneratedModes.FOUR_PAIRS_LOW,
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
        assertEquals(GeneratedModes.FOUR_PAIRS_LOW, ready.session.challenge)
        assertEquals(snapshot.seed, ready.session.request.seed)
        assertEquals(snapshot.profileId, ready.session.request.profileId.value)
        assertEquals(0, generationUseCase.requestCount)
        assertTrue(repository.replaceAttempts.isEmpty())
        assertNull(ready.replacementTransition)
    }

    @Test
    fun resume_rejects_missing_stale_mismatched_and_solved_sessions() {
        val expectedSessionId = GeneratedSessionId("expected")
        val solvedPuzzle = solvedPuzzleWithKnownStripAndAssignments()
        val solvedInitialPuzzle = initialPuzzleFor(solvedPuzzle)
        val unavailableSnapshots = listOf(
            null,
            generatedSessionSnapshot(sessionId = "stale"),
            generatedSessionSnapshot(
                sessionId = expectedSessionId.value,
                modeId = GeneratedModes.EIGHT_PAIRS.id.value
            ),
            generatedSessionSnapshot(
                sessionId = expectedSessionId.value,
                profileId = GeneratedModes.EIGHT_PAIRS_MEDIUM.profile.id.value
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
                challenge = GeneratedModes.FOUR_PAIRS_LOW,
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
            assertFalse(viewModel.claimPersonalRecordCelebration())
        }
    }

    @Test
    fun committed_puzzle_changes_update_the_visible_and_durable_session_in_order() {
        val firstWriteGate = CompletableDeferred<Unit>()
        val repository = RecordingGeneratedSessionRepository(firstUpdateGate = firstWriteGate)
        val viewModel = GeneratedPuzzleViewModel(
            challenge = GeneratedModes.FOUR_PAIRS_LOW,
            generationUseCase = generatedPuzzleUseCase(),
            generatedSessionRepository = repository,
            seedSource = QueueGeneratedPuzzleSeedSource(41),
            sessionIdSource = QueueGeneratedSessionIdSource("active")
        )
        viewModel.onRouteEntered()
        dispatcher.scheduler.advanceUntilIdle()
        val sessionId = (viewModel.uiState.value as GeneratedPuzzleGenerationUiState.Ready).session.id
        val firstPuzzle = samplePuzzle.copy(
            strip = samplePuzzle.strip.withUpdatedEntry(index = 1, value = 1)
        )
        val latestPuzzle = samplePuzzle.copy(
            strip = samplePuzzle.strip.withUpdatedEntry(index = 1, value = 2)
        )

        viewModel.onPuzzleMutationCommitted(
            sessionId,
            firstPuzzle.asCommittedMutation(isCorrection = true)
        )
        dispatcher.scheduler.runCurrent()
        viewModel.onPuzzleMutationCommitted(sessionId, latestPuzzle.asCommittedMutation())
        dispatcher.scheduler.runCurrent()

        val visibleSession = (viewModel.uiState.value as GeneratedPuzzleGenerationUiState.Ready).session
        assertEquals(latestPuzzle, visibleSession.currentPuzzle)
        assertEquals(1L, requireNotNull(visibleSession.snapshot.correctionCount).value)
        assertEquals(samplePuzzle, repository.session.value?.currentPuzzle)

        firstWriteGate.complete(Unit)
        dispatcher.scheduler.advanceUntilIdle()

        assertEquals(listOf(firstPuzzle, latestPuzzle), repository.updateAttempts)
        assertEquals(latestPuzzle, repository.session.value?.currentPuzzle)
        assertEquals(1L, requireNotNull(repository.session.value?.correctionCount).value)
    }

    @Test
    fun legacy_generated_session_keeps_corrections_unknown_after_resume_and_progress() {
        val currentPuzzle = samplePuzzle.copy(
            strip = samplePuzzle.strip.withUpdatedEntry(index = 1, value = 1)
        )
        val latestPuzzle = samplePuzzle.copy(
            strip = samplePuzzle.strip.withUpdatedEntry(index = 1, value = 2)
        )
        val snapshot = generatedSessionSnapshot(
            sessionId = "legacy",
            currentPuzzle = currentPuzzle,
            correctionCount = null
        )
        val repository = RecordingGeneratedSessionRepository(initialSession = snapshot)
        val viewModel = GeneratedPuzzleViewModel(
            challenge = GeneratedModes.FOUR_PAIRS_LOW,
            generationUseCase = UnexpectedGeneratedPuzzleUseCase(),
            generatedSessionRepository = repository
        )
        viewModel.onRouteEntered(
            GeneratedModeLaunchIntent.ResumeSession(expectedSessionId = snapshot.sessionId)
        )
        dispatcher.scheduler.advanceUntilIdle()

        viewModel.onPuzzleMutationCommitted(
            snapshot.sessionId,
            latestPuzzle.asCommittedMutation(isCorrection = true)
        )
        dispatcher.scheduler.advanceUntilIdle()

        val visibleSession = (viewModel.uiState.value as GeneratedPuzzleGenerationUiState.Ready).session
        assertNull(visibleSession.snapshot.correctionCount)
        assertNull(repository.session.value?.correctionCount)
    }

    @Test
    fun solved_puzzle_clears_resumability_while_remaining_visible() {
        val solvedPuzzle = solvedPuzzleWithKnownStripAndAssignments()
        val initialPuzzle = initialPuzzleFor(solvedPuzzle)
        val repository = RecordingGeneratedSessionRepository()
        val viewModel = GeneratedPuzzleViewModel(
            challenge = GeneratedModes.FOUR_PAIRS_LOW,
            generationUseCase = generatedPuzzleUseCase(CompletableDeferred(initialPuzzle)),
            generatedSessionRepository = repository,
            seedSource = QueueGeneratedPuzzleSeedSource(43),
            sessionIdSource = QueueGeneratedSessionIdSource("solved")
        )
        viewModel.onRouteEntered()
        dispatcher.scheduler.advanceUntilIdle()
        val sessionId = (viewModel.uiState.value as GeneratedPuzzleGenerationUiState.Ready).session.id

        viewModel.onPuzzleMutationCommitted(sessionId, solvedPuzzle.asCommittedMutation())
        dispatcher.scheduler.advanceUntilIdle()

        val visibleSession = (viewModel.uiState.value as GeneratedPuzzleGenerationUiState.Ready).session
        assertEquals(solvedPuzzle, visibleSession.currentPuzzle)
        assertEquals(listOf(sessionId), repository.completionAttempts)
        assertNull(repository.session.value)
        assertEquals(
            GeneratedPersonalBestOutcome.NOT_RECORD,
            (viewModel.uiState.value as GeneratedPuzzleGenerationUiState.Ready)
                .personalBestResult?.outcome
        )
        assertFalse(viewModel.claimPersonalRecordCelebration())
        assertTrue(repository.state.value.personalBests.isEmpty())
    }

    @Test
    fun exact_quick_challenge_best_classifies_a_strict_improvement_independently() {
        val solvedPuzzle = solvedPuzzleWithKnownStripAndAssignments()
        val initialPuzzle = initialPuzzleFor(solvedPuzzle)
        val category = GeneratedPersonalBestCategory.FOUR_PAIRS_LOW
        val otherQuickCategory = GeneratedPersonalBestCategory.THREE_PAIRS_LOW
        val repository = RecordingGeneratedSessionRepository(
            initialPersonalBests = mapOf(
                category to GeneratedElapsedTime(1_001),
                otherQuickCategory to GeneratedElapsedTime(400)
            )
        )
        val timeSource = MutableElapsedTimeSource(epochMilliseconds = 20_000, monotonicMilliseconds = 2_000)
        val viewModel = GeneratedPuzzleViewModel(
            challenge = GeneratedModes.FOUR_PAIRS_LOW,
            generationUseCase = generatedPuzzleUseCase(CompletableDeferred(initialPuzzle)),
            generatedSessionRepository = repository,
            seedSource = QueueGeneratedPuzzleSeedSource(999),
            sessionIdSource = QueueGeneratedSessionIdSource("four-low-record"),
            timeSource = timeSource
        )
        viewModel.onRouteEntered()
        dispatcher.scheduler.advanceUntilIdle()
        val sessionId = (viewModel.uiState.value as GeneratedPuzzleGenerationUiState.Ready).session.id
        viewModel.onPuzzlePresented(sessionId)
        dispatcher.scheduler.advanceUntilIdle()
        timeSource.advance(epochMilliseconds = 1_000, monotonicMilliseconds = 1_000)

        viewModel.onPuzzleMutationCommitted(
            sessionId,
            solvedPuzzle.asCommittedMutation()
        )
        val frozen = (viewModel.uiState.value as GeneratedPuzzleGenerationUiState.Ready).personalBestResult
        assertEquals(
            0L,
            requireNotNull(
                (viewModel.uiState.value as GeneratedPuzzleGenerationUiState.Ready)
                    .session.snapshot.correctionCount
            ).value
        )
        assertTrue(viewModel.claimPersonalRecordCelebration())
        assertFalse(viewModel.claimPersonalRecordCelebration())
        dispatcher.scheduler.advanceUntilIdle()

        assertEquals(
            GeneratedPersonalBestResult(
                category = category,
                currentElapsedTime = GeneratedElapsedTime(1_000),
                previousBestElapsedTime = GeneratedElapsedTime(1_001),
                bestElapsedTime = GeneratedElapsedTime(1_000),
                outcome = GeneratedPersonalBestOutcome.PERSONAL_RECORD
            ),
            frozen
        )
        assertEquals(
            1_000L,
            requireNotNull(repository.state.value.personalBests[category]).milliseconds
        )
        assertEquals(
            400L,
            requireNotNull(repository.state.value.personalBests[otherQuickCategory]).milliseconds
        )
    }

    @Test
    fun `exact tie is not a record and keeps the resulting best`() {
        val solvedPuzzle = solvedPuzzleWithKnownStripAndAssignments()
        val initialPuzzle = initialPuzzleFor(solvedPuzzle)
        val category = GeneratedPersonalBestCategory.EIGHT_PAIRS_HARD
        val repository = RecordingGeneratedSessionRepository(
            initialPersonalBests = mapOf(category to GeneratedElapsedTime(2_500))
        )
        val timeSource = MutableElapsedTimeSource(epochMilliseconds = 30_000, monotonicMilliseconds = 3_000)
        val viewModel = GeneratedPuzzleViewModel(
            challenge = GeneratedModes.EIGHT_PAIRS_HARD,
            generationUseCase = generatedPuzzleUseCase(CompletableDeferred(initialPuzzle)),
            generatedSessionRepository = repository,
            seedSource = QueueGeneratedPuzzleSeedSource(123),
            sessionIdSource = QueueGeneratedSessionIdSource("hard-tie"),
            timeSource = timeSource
        )
        viewModel.onRouteEntered()
        dispatcher.scheduler.advanceUntilIdle()
        val sessionId = (viewModel.uiState.value as GeneratedPuzzleGenerationUiState.Ready).session.id
        viewModel.onPuzzlePresented(sessionId)
        dispatcher.scheduler.advanceUntilIdle()
        timeSource.advance(epochMilliseconds = 2_500, monotonicMilliseconds = 2_500)

        viewModel.onPuzzleMutationCommitted(sessionId, solvedPuzzle.asCommittedMutation())
        dispatcher.scheduler.advanceUntilIdle()

        val result = (viewModel.uiState.value as GeneratedPuzzleGenerationUiState.Ready).personalBestResult
        assertEquals(GeneratedPersonalBestOutcome.NOT_RECORD, result?.outcome)
        assertFalse(viewModel.claimPersonalRecordCelebration())
        assertEquals(2_500L, requireNotNull(result?.previousBestElapsedTime).milliseconds)
        assertEquals(2_500L, requireNotNull(result.bestElapsedTime).milliseconds)
    }

    @Test
    fun `timed completion with an unconfigured exact challenge cannot change a best`() {
        val solvedPuzzle = solvedPuzzleWithKnownStripAndAssignments()
        val initialPuzzle = initialPuzzleFor(solvedPuzzle)
        val unsupportedChallenge = GeneratedChallenge(
            id = GeneratedChallengeId("unsupported-low"),
            modeId = GeneratedModeId("unsupported"),
            difficulty = GeneratedModes.FOUR_PAIRS_LOW.difficulty,
            profile = GeneratedModes.FOUR_PAIRS_LOW.profile
        )
        val existingBests = mapOf(
            GeneratedPersonalBestCategory.FOUR_PAIRS_LOW to GeneratedElapsedTime(5_000)
        )
        val repository = RecordingGeneratedSessionRepository(initialPersonalBests = existingBests)
        val timeSource = MutableElapsedTimeSource(epochMilliseconds = 40_000, monotonicMilliseconds = 4_000)
        val viewModel = GeneratedPuzzleViewModel(
            challenge = unsupportedChallenge,
            generationUseCase = generatedPuzzleUseCase(CompletableDeferred(initialPuzzle)),
            generatedSessionRepository = repository,
            seedSource = QueueGeneratedPuzzleSeedSource(717),
            sessionIdSource = QueueGeneratedSessionIdSource("unsupported"),
            timeSource = timeSource
        )
        viewModel.onRouteEntered()
        dispatcher.scheduler.advanceUntilIdle()
        val sessionId = (viewModel.uiState.value as GeneratedPuzzleGenerationUiState.Ready).session.id
        viewModel.onPuzzlePresented(sessionId)
        dispatcher.scheduler.advanceUntilIdle()
        timeSource.advance(epochMilliseconds = 100, monotonicMilliseconds = 100)

        viewModel.onPuzzleMutationCommitted(sessionId, solvedPuzzle.asCommittedMutation())
        dispatcher.scheduler.advanceUntilIdle()

        val result = (viewModel.uiState.value as GeneratedPuzzleGenerationUiState.Ready).personalBestResult
        assertEquals(GeneratedPersonalBestOutcome.NOT_RECORD, result?.outcome)
        assertNull(result?.category)
        assertNull(result?.bestElapsedTime)
        assertFalse(viewModel.claimPersonalRecordCelebration())
        assertEquals(existingBests, repository.state.value.personalBests)
    }

    @Test
    fun stale_puzzle_callback_cannot_change_a_replacement_session() {
        val replacementPuzzle = CompletableDeferred<Puzzle>()
        val repository = RecordingGeneratedSessionRepository()
        val viewModel = GeneratedPuzzleViewModel(
            challenge = GeneratedModes.FOUR_PAIRS_LOW,
            generationUseCase = QueueGeneratedPuzzleUseCase(
                CompletableDeferred(samplePuzzle),
                replacementPuzzle
            ),
            generatedSessionRepository = repository,
            seedSource = QueueGeneratedPuzzleSeedSource(47, 53),
            sessionIdSource = QueueGeneratedSessionIdSource("previous", "replacement")
        )
        viewModel.onRouteEntered()
        dispatcher.scheduler.advanceUntilIdle()
        val previousSession = (viewModel.uiState.value as GeneratedPuzzleGenerationUiState.Ready).session

        viewModel.onNewPuzzleRequested()
        dispatcher.scheduler.runCurrent()
        replacementPuzzle.complete(samplePuzzle)
        dispatcher.scheduler.advanceUntilIdle()
        val replacementSession = (viewModel.uiState.value as GeneratedPuzzleGenerationUiState.Ready).session
        val stalePuzzle = samplePuzzle.copy(
            strip = samplePuzzle.strip.withUpdatedEntry(index = 1, value = 2)
        )

        viewModel.onPuzzleMutationCommitted(previousSession.id, stalePuzzle.asCommittedMutation())
        viewModel.onPuzzleMutationCommitted(
            previousSession.id,
            solvedPuzzleWithKnownStripAndAssignments().asCommittedMutation()
        )
        dispatcher.scheduler.advanceUntilIdle()

        assertEquals(GeneratedSessionId("replacement"), replacementSession.id)
        assertEquals(replacementSession.snapshot, repository.session.value)
        assertEquals(
            GeneratedPuzzleReplacementTransition(
                predecessorSessionId = previousSession.id,
                successorSessionId = replacementSession.id
            ),
            (viewModel.uiState.value as GeneratedPuzzleGenerationUiState.Ready).replacementTransition
        )
        assertTrue(repository.updateAttempts.isEmpty())
        assertTrue(repository.clearAttempts.isEmpty())
    }

    @Test
    fun cancelling_replacement_keeps_the_previous_session_visible_and_stored() {
        val replacementPuzzle = CompletableDeferred<Puzzle>()
        val repository = RecordingGeneratedSessionRepository()
        val viewModel = GeneratedPuzzleViewModel(
            challenge = GeneratedModes.FOUR_PAIRS_LOW,
            generationUseCase = QueueGeneratedPuzzleUseCase(
                CompletableDeferred(samplePuzzle),
                replacementPuzzle
            ),
            generatedSessionRepository = repository,
            seedSource = QueueGeneratedPuzzleSeedSource(59, 61),
            sessionIdSource = QueueGeneratedSessionIdSource("previous", "cancelled")
        )
        viewModel.onRouteEntered()
        dispatcher.scheduler.advanceUntilIdle()
        val previousSession = (viewModel.uiState.value as GeneratedPuzzleGenerationUiState.Ready).session

        viewModel.onNewPuzzleRequested()
        dispatcher.scheduler.runCurrent()
        viewModel.onRouteExited()
        replacementPuzzle.complete(samplePuzzle)
        dispatcher.scheduler.advanceUntilIdle()

        val loading = viewModel.uiState.value as GeneratedPuzzleGenerationUiState.Loading
        assertEquals(previousSession, loading.previousSession)
        assertEquals(previousSession.snapshot, repository.session.value)
        assertEquals(1, repository.replaceAttempts.size)
    }

    @Test
    fun timing_starts_once_on_first_playable_presentation_and_uses_monotonic_continuation() {
        val repository = RecordingGeneratedSessionRepository()
        val timeSource = MutableElapsedTimeSource(epochMilliseconds = 10_000, monotonicMilliseconds = 500)
        val viewModel = GeneratedPuzzleViewModel(
            challenge = GeneratedModes.FOUR_PAIRS_LOW,
            generationUseCase = generatedPuzzleUseCase(),
            generatedSessionRepository = repository,
            seedSource = QueueGeneratedPuzzleSeedSource(101),
            sessionIdSource = QueueGeneratedSessionIdSource("timed"),
            timeSource = timeSource
        )
        viewModel.onRouteEntered()
        dispatcher.scheduler.advanceUntilIdle()
        val sessionId = (viewModel.uiState.value as GeneratedPuzzleGenerationUiState.Ready).session.id

        assertNull(repository.session.value?.timingStartInstant)
        viewModel.onPuzzlePresented(sessionId)
        dispatcher.scheduler.advanceUntilIdle()
        timeSource.advance(epochMilliseconds = 1_234, monotonicMilliseconds = 1_234)
        viewModel.onPuzzlePresented(sessionId)
        viewModel.onTimerRefresh(sessionId)

        val state = viewModel.uiState.value as GeneratedPuzzleGenerationUiState.Ready
        assertEquals(
            10_000L,
            requireNotNull(state.session.snapshot.timingStartInstant).epochMilliseconds
        )
        assertEquals(1_234L, requireNotNull(state.elapsedTime).milliseconds)
        assertEquals(listOf(GeneratedTimingStartInstant(10_000)), repository.startTimingAttempts)
        assertEquals(
            10_000L,
            requireNotNull(repository.session.value?.timingStartInstant).epochMilliseconds
        )
    }

    @Test
    fun migrated_active_session_gets_no_earlier_time_and_starts_on_first_presentation_after_resume() {
        val snapshot = generatedSessionSnapshot(sessionId = "migrated-untimed")
        val repository = RecordingGeneratedSessionRepository(initialSession = snapshot)
        val timeSource = MutableElapsedTimeSource(epochMilliseconds = 50_000, monotonicMilliseconds = 7_000)
        val viewModel = GeneratedPuzzleViewModel(
            challenge = GeneratedModes.FOUR_PAIRS_LOW,
            generationUseCase = UnexpectedGeneratedPuzzleUseCase(),
            generatedSessionRepository = repository,
            timeSource = timeSource
        )

        viewModel.onRouteEntered(GeneratedModeLaunchIntent.ResumeSession(snapshot.sessionId))
        dispatcher.scheduler.advanceUntilIdle()
        val restored = viewModel.uiState.value as GeneratedPuzzleGenerationUiState.Ready
        assertNull(restored.elapsedTime)
        assertNull(restored.session.snapshot.timingStartInstant)
        assertTrue(repository.startTimingAttempts.isEmpty())

        viewModel.onPuzzlePresented(snapshot.sessionId)
        dispatcher.scheduler.advanceUntilIdle()

        val presented = viewModel.uiState.value as GeneratedPuzzleGenerationUiState.Ready
        assertEquals(0L, requireNotNull(presented.elapsedTime).milliseconds)
        assertEquals(
            50_000L,
            requireNotNull(presented.session.snapshot.timingStartInstant).epochMilliseconds
        )
        assertEquals(listOf(GeneratedTimingStartInstant(50_000)), repository.startTimingAttempts)
    }

    @Test
    fun a_pending_start_is_persisted_before_a_successor_can_replace_the_session() {
        val startWriteGate = CompletableDeferred<Unit>()
        val repository = RecordingGeneratedSessionRepository(startTimingGate = startWriteGate)
        val viewModel = GeneratedPuzzleViewModel(
            challenge = GeneratedModes.FOUR_PAIRS_LOW,
            generationUseCase = QueueGeneratedPuzzleUseCase(
                CompletableDeferred(samplePuzzle),
                CompletableDeferred(samplePuzzle)
            ),
            generatedSessionRepository = repository,
            seedSource = QueueGeneratedPuzzleSeedSource(107, 109),
            sessionIdSource = QueueGeneratedSessionIdSource("timing-owner", "successor"),
            timeSource = MutableElapsedTimeSource(epochMilliseconds = 80_000, monotonicMilliseconds = 8_000)
        )
        viewModel.onRouteEntered()
        dispatcher.scheduler.advanceUntilIdle()
        val firstSession = (viewModel.uiState.value as GeneratedPuzzleGenerationUiState.Ready).session
        viewModel.onPuzzlePresented(firstSession.id)
        dispatcher.scheduler.runCurrent()

        viewModel.onNewPuzzleRequested()
        dispatcher.scheduler.runCurrent()

        assertTrue(viewModel.uiState.value is GeneratedPuzzleGenerationUiState.Loading)
        assertEquals(listOf(firstSession.snapshot), repository.replaceAttempts)
        startWriteGate.complete(Unit)
        dispatcher.scheduler.advanceUntilIdle()

        assertEquals(2, repository.replaceAttempts.size)
        assertEquals(
            GeneratedSessionId("successor"),
            (viewModel.uiState.value as GeneratedPuzzleGenerationUiState.Ready).session.id
        )
    }

    @Test
    fun timing_persistence_retry_reuses_the_original_start_instead_of_remeasuring() {
        val repository = RecordingGeneratedSessionRepository(startTimingFailuresRemaining = 1)
        val timeSource = MutableElapsedTimeSource(epochMilliseconds = 90_000, monotonicMilliseconds = 9_000)
        val viewModel = GeneratedPuzzleViewModel(
            challenge = GeneratedModes.FOUR_PAIRS_LOW,
            generationUseCase = generatedPuzzleUseCase(),
            generatedSessionRepository = repository,
            seedSource = QueueGeneratedPuzzleSeedSource(113),
            sessionIdSource = QueueGeneratedSessionIdSource("start-retry"),
            timeSource = timeSource
        )
        viewModel.onRouteEntered()
        dispatcher.scheduler.advanceUntilIdle()
        val sessionId = (viewModel.uiState.value as GeneratedPuzzleGenerationUiState.Ready).session.id

        viewModel.onPuzzlePresented(sessionId)
        dispatcher.scheduler.advanceUntilIdle()
        assertTrue((viewModel.uiState.value as GeneratedPuzzleGenerationUiState.Ready).hasPersistenceFailure)
        timeSource.advance(epochMilliseconds = 5_000, monotonicMilliseconds = 5_000)
        viewModel.retryPersistence()
        dispatcher.scheduler.advanceUntilIdle()

        assertEquals(
            listOf(GeneratedTimingStartInstant(90_000), GeneratedTimingStartInstant(90_000)),
            repository.startTimingAttempts
        )
        assertEquals(
            90_000L,
            requireNotNull(repository.session.value?.timingStartInstant).epochMilliseconds
        )
        assertTrue(!(viewModel.uiState.value as GeneratedPuzzleGenerationUiState.Ready).hasPersistenceFailure)
    }

    @Test
    fun restoration_uses_wall_clock_then_never_moves_backwards_in_the_visible_session() {
        val snapshot = generatedSessionSnapshot(sessionId = "restored").copy(
            timingStartInstant = GeneratedTimingStartInstant(5_000)
        )
        val repository = RecordingGeneratedSessionRepository(initialSession = snapshot)
        val timeSource = MutableElapsedTimeSource(epochMilliseconds = 10_000, monotonicMilliseconds = 1_000)
        val viewModel = GeneratedPuzzleViewModel(
            challenge = GeneratedModes.FOUR_PAIRS_LOW,
            generationUseCase = UnexpectedGeneratedPuzzleUseCase(),
            generatedSessionRepository = repository,
            timeSource = timeSource
        )
        viewModel.onRouteEntered(GeneratedModeLaunchIntent.ResumeSession(snapshot.sessionId))
        dispatcher.scheduler.advanceUntilIdle()

        viewModel.onPuzzlePresented(snapshot.sessionId)
        assertEquals(
            5_000L,
            requireNotNull(
                (viewModel.uiState.value as GeneratedPuzzleGenerationUiState.Ready).elapsedTime
            ).milliseconds
        )
        timeSource.set(epochMilliseconds = 4_000, monotonicMilliseconds = 1_500)
        viewModel.onTimerRefresh(snapshot.sessionId)
        assertEquals(
            5_500L,
            requireNotNull(
                (viewModel.uiState.value as GeneratedPuzzleGenerationUiState.Ready).elapsedTime
            ).milliseconds
        )
        timeSource.set(epochMilliseconds = 3_000, monotonicMilliseconds = 900)
        viewModel.onTimerRefresh(snapshot.sessionId)
        assertEquals(
            5_500L,
            requireNotNull(
                (viewModel.uiState.value as GeneratedPuzzleGenerationUiState.Ready).elapsedTime
            ).milliseconds
        )
        assertTrue(repository.startTimingAttempts.isEmpty())
    }

    @Test
    fun solved_transition_freezes_before_io_and_retry_reuses_the_same_millisecond_duration() {
        val solvedPuzzle = solvedPuzzleWithKnownStripAndAssignments()
        val initialPuzzle = initialPuzzleFor(solvedPuzzle)
        val repository = RecordingGeneratedSessionRepository(
            initialPersonalBests = mapOf(
                GeneratedPersonalBestCategory.FOUR_PAIRS_LOW to GeneratedElapsedTime(2_346)
            ),
            updateFailuresRemaining = 1
        )
        val timeSource = MutableElapsedTimeSource(epochMilliseconds = 20_000, monotonicMilliseconds = 2_000)
        val viewModel = GeneratedPuzzleViewModel(
            challenge = GeneratedModes.FOUR_PAIRS_LOW,
            generationUseCase = generatedPuzzleUseCase(CompletableDeferred(initialPuzzle)),
            generatedSessionRepository = repository,
            seedSource = QueueGeneratedPuzzleSeedSource(103),
            sessionIdSource = QueueGeneratedSessionIdSource("completion-retry"),
            timeSource = timeSource
        )
        viewModel.onRouteEntered()
        dispatcher.scheduler.advanceUntilIdle()
        val sessionId = (viewModel.uiState.value as GeneratedPuzzleGenerationUiState.Ready).session.id
        viewModel.onPuzzlePresented(sessionId)
        dispatcher.scheduler.advanceUntilIdle()
        timeSource.advance(epochMilliseconds = 2_345, monotonicMilliseconds = 2_345)

        viewModel.onPuzzleMutationCommitted(sessionId, solvedPuzzle.asCommittedMutation())
        val frozenState = viewModel.uiState.value as GeneratedPuzzleGenerationUiState.Ready
        assertEquals(
            2_345L,
            requireNotNull(frozenState.session.snapshot.completionElapsedTime).milliseconds
        )
        assertTrue(viewModel.claimPersonalRecordCelebration())
        assertFalse(viewModel.claimPersonalRecordCelebration())
        timeSource.advance(epochMilliseconds = 9_000, monotonicMilliseconds = 9_000)
        dispatcher.scheduler.advanceUntilIdle()
        assertTrue((viewModel.uiState.value as GeneratedPuzzleGenerationUiState.Ready).hasPersistenceFailure)

        viewModel.retryPersistence()
        timeSource.advance(epochMilliseconds = 4_000, monotonicMilliseconds = 4_000)
        dispatcher.scheduler.advanceUntilIdle()

        assertEquals(
            listOf(GeneratedElapsedTime(2_345), GeneratedElapsedTime(2_345)),
            repository.completionElapsedTimeAttempts
        )
        assertEquals(2, repository.personalBestResultAttempts.size)
        assertEquals(
            repository.personalBestResultAttempts.first(),
            repository.personalBestResultAttempts.last()
        )
        assertEquals(
            GeneratedPersonalBestOutcome.PERSONAL_RECORD,
            repository.personalBestResultAttempts.first().outcome
        )
        assertFalse(viewModel.claimPersonalRecordCelebration())
        assertEquals(
            2_345L,
            requireNotNull(
                repository.state.value.personalBests[GeneratedPersonalBestCategory.FOUR_PAIRS_LOW]
            ).milliseconds
        )
        assertNull(repository.session.value)
        assertEquals(
            2_345L,
            requireNotNull(
                (viewModel.uiState.value as GeneratedPuzzleGenerationUiState.Ready)
                    .session.snapshot.completionElapsedTime
            ).milliseconds
        )
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

private class QueueGeneratedPuzzleUseCase(vararg puzzles: CompletableDeferred<Puzzle>) :
    GeneratedPuzzleGenerationUseCase {
    private val remainingPuzzles = ArrayDeque(puzzles.toList())

    override suspend fun generate(request: GeneratedPuzzleGenerationRequest): GeneratedPuzzleGenerationResult =
        GeneratedPuzzleGenerationResult.Generated(
            request = request,
            initialPuzzle = remainingPuzzles.removeFirst().await()
        )
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
    initialPersonalBests: Map<GeneratedPersonalBestCategory, GeneratedElapsedTime> = emptyMap(),
    writeGate: CompletableDeferred<Unit>? = null,
    private val replaceFailure: IOException? = null,
    firstUpdateGate: CompletableDeferred<Unit>? = null,
    private var updateFailuresRemaining: Int = 0,
    private var startTimingFailuresRemaining: Int = 0,
    private val startTimingGate: CompletableDeferred<Unit>? = null
) : GeneratedSessionRepository {
    private val mutableSession = MutableStateFlow(initialSession)
    private val mutableState = MutableStateFlow(
        GeneratedSessionState(
            activeSession = initialSession,
            personalBests = initialPersonalBests
        )
    )
    private var pendingUpdateGate = firstUpdateGate
    override val state: StateFlow<GeneratedSessionState> = mutableState.asStateFlow()
    override val session: StateFlow<GeneratedSessionSnapshot?> = mutableSession.asStateFlow()
    val replaceAttempts = mutableListOf<GeneratedSessionSnapshot>()
    val updateAttempts = mutableListOf<Puzzle>()
    val clearAttempts = mutableListOf<GeneratedSessionId>()
    val startTimingAttempts = mutableListOf<GeneratedTimingStartInstant>()
    val completionElapsedTimeAttempts = mutableListOf<GeneratedElapsedTime?>()
    val personalBestResultAttempts = mutableListOf<GeneratedPersonalBestResult>()
    val completionAttempts = mutableListOf<GeneratedSessionId>()
    var nextReplaceGate: CompletableDeferred<Unit>? = writeGate

    override suspend fun replace(snapshot: GeneratedSessionSnapshot) {
        replaceAttempts += snapshot
        nextReplaceGate?.let { gate ->
            nextReplaceGate = null
            gate.await()
        }
        replaceFailure?.let { failure ->
            throw failure
        }
        updateSession(snapshot)
    }

    override suspend fun startTiming(
        expectedSessionId: GeneratedSessionId,
        startInstant: GeneratedTimingStartInstant
    ): GeneratedSessionTimingStartResult {
        startTimingAttempts += startInstant
        if (startTimingFailuresRemaining > 0) {
            startTimingFailuresRemaining--
            throw IOException("Controlled generated-session timing failure.")
        }
        startTimingGate?.await()
        val snapshot = mutableSession.value
        if (snapshot?.sessionId != expectedSessionId) {
            return GeneratedSessionTimingStartResult.StaleSession
        }
        snapshot.timingStartInstant?.let { existing ->
            return GeneratedSessionTimingStartResult.AlreadyStarted(existing)
        }
        updateSession(snapshot.copy(timingStartInstant = startInstant))
        return GeneratedSessionTimingStartResult.Started(startInstant)
    }

    override suspend fun updateCurrentPuzzle(
        expectedSessionId: GeneratedSessionId,
        puzzle: Puzzle,
        correctionCount: PuzzleCorrectionCount?,
        completionElapsedTime: GeneratedElapsedTime?
    ): Boolean {
        updateAttempts += puzzle
        completionElapsedTimeAttempts += completionElapsedTime
        if (updateFailuresRemaining > 0) {
            updateFailuresRemaining--
            throw IOException("Controlled generated-session update failure.")
        }
        pendingUpdateGate?.let { gate ->
            pendingUpdateGate = null
            gate.await()
        }
        val snapshot = mutableSession.value
        if (snapshot?.sessionId != expectedSessionId) {
            return false
        }

        updateSession(
            snapshot.copy(
                currentPuzzle = puzzle,
                correctionCount = correctionCount,
                completionElapsedTime = completionElapsedTime
            )
        )
        return true
    }

    override suspend fun complete(
        expectedSessionId: GeneratedSessionId,
        solvedPuzzle: Puzzle,
        correctionCount: PuzzleCorrectionCount?,
        personalBestResult: GeneratedPersonalBestResult
    ): GeneratedSessionCompletionResult {
        completionAttempts += expectedSessionId
        completionElapsedTimeAttempts += personalBestResult.currentElapsedTime
        personalBestResultAttempts += personalBestResult
        if (updateFailuresRemaining > 0) {
            updateFailuresRemaining--
            throw IOException("Controlled generated-session completion failure.")
        }
        val snapshot = mutableSession.value
        if (snapshot?.sessionId != expectedSessionId) {
            return GeneratedSessionCompletionResult.StaleSession
        }
        val category = personalBestResult.category
        if (
            category != null &&
            mutableState.value.personalBests[category] != personalBestResult.previousBestElapsedTime
        ) {
            return GeneratedSessionCompletionResult.StalePersonalBest
        }
        val resultingBests = if (category != null && personalBestResult.bestElapsedTime != null) {
            mutableState.value.personalBests + (category to personalBestResult.bestElapsedTime)
        } else {
            mutableState.value.personalBests
        }
        mutableSession.value = null
        mutableState.value = GeneratedSessionState(
            activeSession = null,
            personalBests = resultingBests
        )
        return GeneratedSessionCompletionResult.Completed
    }

    override suspend fun clear(expectedSessionId: GeneratedSessionId): Boolean {
        clearAttempts += expectedSessionId
        if (mutableSession.value?.sessionId != expectedSessionId) {
            return false
        }

        updateSession(null)
        return true
    }

    private fun updateSession(snapshot: GeneratedSessionSnapshot?) {
        mutableSession.value = snapshot
        mutableState.value = mutableState.value.copy(activeSession = snapshot)
    }
}

private fun generatedSessionSnapshot(
    sessionId: String,
    modeId: String = GeneratedModes.FOUR_PAIRS.id.value,
    profileId: String = GeneratedModes.FOUR_PAIRS_LOW.profile.id.value,
    initialPuzzle: Puzzle = samplePuzzle,
    currentPuzzle: Puzzle = samplePuzzle,
    correctionCount: PuzzleCorrectionCount? = PuzzleCorrectionCount.ZERO
): GeneratedSessionSnapshot = GeneratedSessionSnapshot(
    sessionId = GeneratedSessionId(sessionId),
    modeId = modeId,
    profileId = profileId,
    seed = 7,
    initialPuzzle = initialPuzzle,
    currentPuzzle = currentPuzzle,
    correctionCount = correctionCount
)

private fun Puzzle.asCommittedMutation(isCorrection: Boolean = false): CommittedPuzzleMutation =
    CommittedPuzzleMutation(
        puzzle = this,
        isCorrection = isCorrection
    )

private class MutableElapsedTimeSource(epochMilliseconds: Long, monotonicMilliseconds: Long) : ElapsedTimeSource {
    private var reading = ElapsedTimeReading(epochMilliseconds, monotonicMilliseconds)

    override fun read(): ElapsedTimeReading = reading

    fun advance(epochMilliseconds: Long, monotonicMilliseconds: Long) {
        set(
            epochMilliseconds = reading.epochMilliseconds + epochMilliseconds,
            monotonicMilliseconds = reading.monotonicMilliseconds + monotonicMilliseconds
        )
    }

    fun set(epochMilliseconds: Long, monotonicMilliseconds: Long) {
        reading = ElapsedTimeReading(epochMilliseconds, monotonicMilliseconds)
    }
}

private fun initialPuzzleFor(solvedPuzzle: Puzzle): Puzzle = solvedPuzzle.copy(
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
