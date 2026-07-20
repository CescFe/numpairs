package org.cescfe.numpairs.feature.onboarding

import java.io.IOException
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.emitAll
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.test.runTest
import org.cescfe.numpairs.data.onboarding.FirstRunTutorialOutcome
import org.cescfe.numpairs.data.onboarding.OnboardingRepository
import org.cescfe.numpairs.data.onboarding.OnboardingStageCheckpoint
import org.cescfe.numpairs.data.onboarding.OnboardingState
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class OnboardingStartupCoordinatorTest {
    @Test
    fun `empty application data publishes unresolved first-run state`() = runTest {
        val state = onboardingStartupStates(StartupOnboardingRepository()).first()

        val ready = state as OnboardingStartupState.Ready
        assertEquals(OnboardingState(), ready.onboardingState)
        assertFalse(ready.onboardingState.firstRunTutorialOutcome.isResolved)
    }

    @Test
    fun `resolved first-run outcomes remain unlocked on startup`() = runTest {
        FirstRunTutorialOutcome.entries
            .filter(FirstRunTutorialOutcome::isResolved)
            .forEach { outcome ->
                val expectedState = completedState(outcome)

                val state = onboardingStartupStates(
                    StartupOnboardingRepository(initialState = expectedState)
                ).first()

                assertEquals(expectedState, (state as OnboardingStartupState.Ready).onboardingState)
            }
    }

    @Test
    fun `transient storage read failure is retried without process restart`() = runTest {
        val repository = StartupOnboardingRepository(readFailuresRemaining = 1)

        val state = onboardingStartupStates(repository).first()

        assertTrue(state is OnboardingStartupState.Ready)
        assertEquals(2, repository.readAttempts)
    }

    @Test
    fun `persistent storage read failure reaches recoverable failure state`() = runTest {
        val repository = StartupOnboardingRepository(readFailuresRemaining = Int.MAX_VALUE)

        val state = onboardingStartupStates(repository).first()

        val failure = state as OnboardingStartupState.Failure
        assertTrue(failure.cause is IOException)
        assertFalse(failure.isRetrying)
        assertEquals(3, repository.readAttempts)
    }

    @Test
    fun `explicit retry recovers after automatic storage retries are exhausted`() = runTest {
        val repository = StartupOnboardingRepository(readFailuresRemaining = Int.MAX_VALUE)
        val coordinator = coordinator(repository = repository, coroutineScope = backgroundScope)
        testScheduler.runCurrent()
        assertTrue(coordinator.state.value is OnboardingStartupState.Failure)

        repository.readFailuresRemaining = 0
        coordinator.retry()

        val retrying = coordinator.state.value as OnboardingStartupState.Failure
        assertTrue(retrying.isRetrying)
        testScheduler.runCurrent()
        assertTrue(coordinator.state.value is OnboardingStartupState.Ready)
    }

    @Test
    fun `initial local resolution remains behind startup loading state`() = runTest {
        val coordinator = coordinator(
            repository = StartupOnboardingRepository(),
            coroutineScope = backgroundScope
        )

        assertEquals(OnboardingStartupState.Loading, coordinator.state.value)

        testScheduler.runCurrent()

        assertTrue(coordinator.state.value is OnboardingStartupState.Ready)
    }

    private fun coordinator(
        repository: StartupOnboardingRepository,
        coroutineScope: CoroutineScope
    ): OnboardingStartupCoordinator = OnboardingStartupCoordinator(
        repository = repository,
        coroutineScope = coroutineScope
    )

    private fun completedState(outcome: FirstRunTutorialOutcome): OnboardingState = OnboardingState(
        lastCompletedStage = OnboardingStageCheckpoint.STAGE_THREE,
        firstRunTutorialOutcome = outcome
    )

    private class StartupOnboardingRepository(
        initialState: OnboardingState = OnboardingState(),
        var readFailuresRemaining: Int = 0
    ) : OnboardingRepository {
        private val persistedState = MutableStateFlow(initialState)

        var readAttempts = 0
            private set

        override val onboardingState: Flow<OnboardingState> = flow {
            readAttempts += 1
            if (readFailuresRemaining > 0) {
                readFailuresRemaining -= 1
                throw IOException("Synthetic onboarding read failure.")
            }
            emitAll(persistedState)
        }

        override suspend fun recordStageCompleted(stage: OnboardingStageCheckpoint) = Unit

        override suspend fun markTutorialCompleted() = Unit

        override suspend fun markTutorialSkipped() = Unit
    }
}
