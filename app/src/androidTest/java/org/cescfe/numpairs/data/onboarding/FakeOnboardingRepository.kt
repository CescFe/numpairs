package org.cescfe.numpairs.data.onboarding

import kotlinx.coroutines.flow.MutableStateFlow

class FakeOnboardingRepository(initialState: OnboardingState = completedOnboardingState()) : OnboardingRepository {
    override val onboardingState = MutableStateFlow(initialState)

    override suspend fun initialize(installationKind: OnboardingInstallationKind) {
        if (onboardingState.value.isInitialized) {
            return
        }

        onboardingState.value = OnboardingState(
            isInitialized = true,
            completedVersion = if (installationKind == OnboardingInstallationKind.PRE_V6_UPGRADE) {
                REQUIRED_ONBOARDING_VERSION
            } else {
                0
            }
        )
    }

    override suspend fun recordStageCompleted(stage: OnboardingStageCheckpoint) {
        onboardingState.value = onboardingState.value.copy(lastCompletedStage = stage)
    }

    override suspend fun selectPostCorePath(path: OnboardingPostCorePath) {
        onboardingState.value = onboardingState.value.copy(postCorePath = path)
    }

    override suspend fun markRequiredVersionCompleted() {
        onboardingState.value = onboardingState.value.copy(completedVersion = REQUIRED_ONBOARDING_VERSION)
    }
}

fun completedOnboardingState(): OnboardingState = OnboardingState(
    isInitialized = true,
    completedVersion = REQUIRED_ONBOARDING_VERSION
)

fun incompleteOnboardingState(
    lastCompletedStage: OnboardingStageCheckpoint = OnboardingStageCheckpoint.NONE,
    postCorePath: OnboardingPostCorePath = OnboardingPostCorePath.UNDECIDED
): OnboardingState = OnboardingState(
    isInitialized = true,
    lastCompletedStage = lastCompletedStage,
    postCorePath = postCorePath
)
