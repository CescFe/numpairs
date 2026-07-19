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
import org.cescfe.numpairs.data.onboarding.OnboardingInitializer
import org.cescfe.numpairs.data.onboarding.OnboardingInstallationKind
import org.cescfe.numpairs.data.onboarding.OnboardingRepository
import org.cescfe.numpairs.data.onboarding.OnboardingStageCheckpoint
import org.cescfe.numpairs.data.onboarding.OnboardingState
import org.cescfe.numpairs.data.onboarding.PreV6UpgradeMarker
import org.cescfe.numpairs.data.onboarding.REQUIRED_ONBOARDING_VERSION
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class OnboardingStartupCoordinatorTest {
    @Test
    fun `fresh startup initializes before publishing required Tutorial state`() = runTest {
        val repository = StartupOnboardingRepository()

        val state = startupStates(repository = repository).first()

        val ready = state as OnboardingStartupState.Ready
        assertTrue(ready.onboardingState.isInitialized)
        assertFalse(ready.onboardingState.isRequiredVersionComplete())
        assertEquals(FirstRunTutorialOutcome.UNRESOLVED, ready.onboardingState.firstRunTutorialOutcome)
        assertEquals(listOf(OnboardingInstallationKind.FRESH_INSTALL), repository.initializationKinds)
    }

    @Test
    fun `resolved first-run outcomes remain unlocked on startup`() = runTest {
        FirstRunTutorialOutcome.entries
            .filter(FirstRunTutorialOutcome::isResolved)
            .forEach { outcome ->
                val expectedState = completedState(outcome)
                val repository = StartupOnboardingRepository(initialState = expectedState)

                val state = startupStates(repository = repository).first()

                assertEquals(expectedState, (state as OnboardingStartupState.Ready).onboardingState)
            }
    }

    @Test
    fun `pre-v6 marker initializes upgraded users before publishing readiness`() = runTest {
        val repository = StartupOnboardingRepository()
        val marker = StartupUpgradeMarker(isMarked = true)

        val state = startupStates(repository = repository, marker = marker).first()

        val ready = state as OnboardingStartupState.Ready
        assertTrue(ready.onboardingState.isRequiredVersionComplete())
        assertEquals(FirstRunTutorialOutcome.PRE_V6_UPGRADE, ready.onboardingState.firstRunTutorialOutcome)
        assertEquals(listOf(OnboardingInstallationKind.PRE_V6_UPGRADE), repository.initializationKinds)
        assertTrue(marker.wasCleared)
    }

    @Test
    fun `transient storage read failure is retried without process restart`() = runTest {
        val repository = StartupOnboardingRepository(readFailuresRemaining = 1)

        val state = startupStates(repository = repository).first()

        assertTrue(state is OnboardingStartupState.Ready)
        assertEquals(2, repository.initializationKinds.size)
        assertEquals(2, repository.readAttempts)
    }

    @Test
    fun `persistent storage read failure reaches recoverable failure state`() = runTest {
        val repository = StartupOnboardingRepository(readFailuresRemaining = Int.MAX_VALUE)

        val state = startupStates(repository = repository).first()

        val failure = state as OnboardingStartupState.Failure
        assertTrue(failure.cause is IOException)
        assertFalse(failure.isRetrying)
        assertEquals(3, repository.initializationKinds.size)
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

    private fun startupStates(
        repository: StartupOnboardingRepository,
        marker: StartupUpgradeMarker = StartupUpgradeMarker()
    ): Flow<OnboardingStartupState> = onboardingStartupStates(
        initializer = OnboardingInitializer(repository, marker),
        repository = repository
    )

    private fun coordinator(
        repository: StartupOnboardingRepository,
        coroutineScope: CoroutineScope
    ): OnboardingStartupCoordinator = OnboardingStartupCoordinator(
        initializer = OnboardingInitializer(repository, StartupUpgradeMarker()),
        repository = repository,
        coroutineScope = coroutineScope
    )

    private fun completedState(outcome: FirstRunTutorialOutcome): OnboardingState = OnboardingState(
        isInitialized = true,
        completedVersion = REQUIRED_ONBOARDING_VERSION,
        lastCompletedStage = OnboardingStageCheckpoint.STAGE_THREE,
        firstRunTutorialOutcome = outcome
    )

    private class StartupOnboardingRepository(
        initialState: OnboardingState = OnboardingState(),
        var readFailuresRemaining: Int = 0
    ) : OnboardingRepository {
        private val persistedState = MutableStateFlow(initialState)

        val initializationKinds = mutableListOf<OnboardingInstallationKind>()
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

        override suspend fun initialize(installationKind: OnboardingInstallationKind) {
            initializationKinds += installationKind
            if (persistedState.value.isInitialized) {
                return
            }

            persistedState.value = when (installationKind) {
                OnboardingInstallationKind.FRESH_INSTALL -> OnboardingState(isInitialized = true)
                OnboardingInstallationKind.PRE_V6_UPGRADE -> OnboardingState(
                    isInitialized = true,
                    completedVersion = REQUIRED_ONBOARDING_VERSION,
                    lastCompletedStage = OnboardingStageCheckpoint.STAGE_THREE,
                    firstRunTutorialOutcome = FirstRunTutorialOutcome.PRE_V6_UPGRADE
                )
            }
        }

        override suspend fun recordStageCompleted(stage: OnboardingStageCheckpoint) = Unit

        override suspend fun markTutorialCompleted() = Unit

        override suspend fun markTutorialSkipped() = Unit
    }

    private class StartupUpgradeMarker(isMarked: Boolean = false) : PreV6UpgradeMarker {
        private var marked = isMarked
        var wasCleared = false
            private set

        override fun isMarked(): Boolean = marked

        override fun mark() {
            marked = true
        }

        override fun clear() {
            marked = false
            wasCleared = true
        }
    }
}
