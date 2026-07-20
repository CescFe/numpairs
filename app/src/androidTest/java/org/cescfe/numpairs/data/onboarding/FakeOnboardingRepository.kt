package org.cescfe.numpairs.data.onboarding

import kotlinx.coroutines.flow.MutableStateFlow

class FakeOnboardingRepository(initialState: OnboardingState = completedOnboardingState()) : OnboardingRepository {
    override val onboardingState = MutableStateFlow(initialState)

    override suspend fun recordStageCompleted(stage: OnboardingStageCheckpoint) {
        onboardingState.value = onboardingState.value.copy(lastCompletedStage = stage)
    }

    override suspend fun markTutorialCompleted() {
        resolveFirstRun(outcome = FirstRunTutorialOutcome.COMPLETED)
    }

    override suspend fun markTutorialSkipped() {
        resolveFirstRun(outcome = FirstRunTutorialOutcome.SKIPPED)
    }

    private fun resolveFirstRun(outcome: FirstRunTutorialOutcome) {
        if (!onboardingState.value.firstRunTutorialOutcome.isResolved) {
            onboardingState.value = onboardingState.value.copy(
                firstRunTutorialOutcome = outcome
            )
        }
    }
}

fun completedOnboardingState(): OnboardingState = OnboardingState(
    firstRunTutorialOutcome = FirstRunTutorialOutcome.COMPLETED
)

fun incompleteOnboardingState(
    lastCompletedStage: OnboardingStageCheckpoint = OnboardingStageCheckpoint.NONE
): OnboardingState = OnboardingState(
    lastCompletedStage = lastCompletedStage
)
