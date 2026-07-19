package org.cescfe.numpairs.data.onboarding

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class OnboardingInitializerTest {
    @Test
    fun `missing upgrade marker initializes a fresh first run`() = runBlocking {
        val repository = FakeOnboardingRepository()
        val marker = FakePreV6UpgradeMarker(isMarked = false)

        OnboardingInitializer(repository, marker).initialize()

        assertEquals(OnboardingInstallationKind.FRESH_INSTALL, repository.initializationKind)
        assertFalse(repository.onboardingState.first().isRequiredVersionComplete())
        assertEquals(
            FirstRunTutorialOutcome.UNRESOLVED,
            repository.onboardingState.first().firstRunTutorialOutcome
        )
        assertFalse(marker.wasCleared)
    }

    @Test
    fun `upgrade marker initializes completion and is consumed after persistence`() = runBlocking {
        val repository = FakeOnboardingRepository()
        val marker = FakePreV6UpgradeMarker(isMarked = true)

        OnboardingInitializer(repository, marker).initialize()

        assertEquals(OnboardingInstallationKind.PRE_V6_UPGRADE, repository.initializationKind)
        assertTrue(repository.onboardingState.first().isRequiredVersionComplete())
        assertEquals(
            FirstRunTutorialOutcome.PRE_V6_UPGRADE,
            repository.onboardingState.first().firstRunTutorialOutcome
        )
        assertTrue(marker.wasCleared)
    }

    @Test
    fun `missing local state after data clearing initializes a new first run`() = runBlocking {
        val marker = FakePreV6UpgradeMarker(isMarked = true)
        OnboardingInitializer(FakeOnboardingRepository(), marker).initialize()
        val repositoryAfterDataClear = FakeOnboardingRepository()

        OnboardingInitializer(repositoryAfterDataClear, marker).initialize()

        assertEquals(OnboardingInstallationKind.FRESH_INSTALL, repositoryAfterDataClear.initializationKind)
        assertFalse(repositoryAfterDataClear.onboardingState.first().isRequiredVersionComplete())
    }

    private class FakeOnboardingRepository : OnboardingRepository {
        override val onboardingState = MutableStateFlow(OnboardingState())
        var initializationKind: OnboardingInstallationKind? = null
            private set

        override suspend fun initialize(installationKind: OnboardingInstallationKind) {
            initializationKind = installationKind
            onboardingState.value = OnboardingState(
                isInitialized = true,
                completedVersion = if (installationKind == OnboardingInstallationKind.PRE_V6_UPGRADE) {
                    REQUIRED_ONBOARDING_VERSION
                } else {
                    0
                },
                firstRunTutorialOutcome = if (installationKind == OnboardingInstallationKind.PRE_V6_UPGRADE) {
                    FirstRunTutorialOutcome.PRE_V6_UPGRADE
                } else {
                    FirstRunTutorialOutcome.UNRESOLVED
                }
            )
        }

        override suspend fun recordStageCompleted(stage: OnboardingStageCheckpoint) = Unit

        override suspend fun selectPostCorePath(path: OnboardingPostCorePath) = Unit

        override suspend fun markTutorialCompleted() = Unit

        override suspend fun markTutorialSkipped() = Unit
    }

    private class FakePreV6UpgradeMarker(isMarked: Boolean) : PreV6UpgradeMarker {
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
