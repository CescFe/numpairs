package org.cescfe.numpairs.feature.onboarding

import org.cescfe.numpairs.data.onboarding.OnboardingStageCheckpoint
import org.cescfe.numpairs.data.onboarding.OnboardingState
import org.junit.Assert.assertEquals
import org.junit.Test

class RequiredOnboardingRouteTest {
    @Test
    fun `checkpoints resume after the last completed Tutorial step`() {
        assertEquals(0, state(OnboardingStageCheckpoint.NONE).nextRequiredTutorialStepIndex())
        assertEquals(1, state(OnboardingStageCheckpoint.STAGE_ONE).nextRequiredTutorialStepIndex())
        assertEquals(2, state(OnboardingStageCheckpoint.STAGE_TWO).nextRequiredTutorialStepIndex())
        assertEquals(3, state(OnboardingStageCheckpoint.STAGE_THREE).nextRequiredTutorialStepIndex())
    }

    private fun state(checkpoint: OnboardingStageCheckpoint): OnboardingState = OnboardingState(
        lastCompletedStage = checkpoint
    )
}
