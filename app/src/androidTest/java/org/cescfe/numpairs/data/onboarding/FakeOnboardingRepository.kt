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
            },
            firstRunTutorialOutcome = if (installationKind == OnboardingInstallationKind.PRE_V6_UPGRADE) {
                FirstRunTutorialOutcome.PRE_V6_UPGRADE
            } else {
                FirstRunTutorialOutcome.UNRESOLVED
            }
        )
    }

    override suspend fun recordStageCompleted(stage: OnboardingStageCheckpoint) {
        onboardingState.value = onboardingState.value.copy(lastCompletedStage = stage)
    }

    override suspend fun selectPostCorePath(path: OnboardingPostCorePath) {
        onboardingState.value = onboardingState.value.copy(postCorePath = path)
    }

    override suspend fun markTutorialCompleted() {
        resolveFirstRun(outcome = FirstRunTutorialOutcome.COMPLETED)
    }

    override suspend fun markTutorialSkipped() {
        resolveFirstRun(outcome = FirstRunTutorialOutcome.SKIPPED)
    }

    private fun resolveFirstRun(outcome: FirstRunTutorialOutcome) {
        if (!onboardingState.value.isRequiredVersionComplete()) {
            onboardingState.value = onboardingState.value.copy(
                completedVersion = REQUIRED_ONBOARDING_VERSION,
                firstRunTutorialOutcome = outcome
            )
        }
    }
}

fun completedOnboardingState(): OnboardingState = OnboardingState(
    isInitialized = true,
    completedVersion = REQUIRED_ONBOARDING_VERSION,
    firstRunTutorialOutcome = FirstRunTutorialOutcome.LEGACY_COMPLETED
)

fun incompleteOnboardingState(
    lastCompletedStage: OnboardingStageCheckpoint = OnboardingStageCheckpoint.NONE,
    postCorePath: OnboardingPostCorePath = OnboardingPostCorePath.UNDECIDED
): OnboardingState = OnboardingState(
    isInitialized = true,
    lastCompletedStage = lastCompletedStage,
    postCorePath = postCorePath
)
