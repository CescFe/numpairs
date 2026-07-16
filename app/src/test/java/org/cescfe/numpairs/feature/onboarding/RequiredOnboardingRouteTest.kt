package org.cescfe.numpairs.feature.onboarding

import org.cescfe.numpairs.data.onboarding.OnboardingStageCheckpoint
import org.cescfe.numpairs.feature.tutorial.GuidedOnboardingStage
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class RequiredOnboardingRouteTest {
    @Test
    fun `checkpoint resumes at the next required guided stage`() {
        assertEquals(
            GuidedOnboardingStage.NUMBER_PLACEMENT,
            OnboardingStageCheckpoint.NONE.nextRequiredGuidedStage()
        )
        assertEquals(
            GuidedOnboardingStage.COMPLEMENTARY_PAIR,
            OnboardingStageCheckpoint.STAGE_ONE.nextRequiredGuidedStage()
        )
        assertEquals(
            GuidedOnboardingStage.HIDDEN_STRIP_VALUE,
            OnboardingStageCheckpoint.STAGE_TWO.nextRequiredGuidedStage()
        )
        assertNull(OnboardingStageCheckpoint.STAGE_THREE.nextRequiredGuidedStage())
    }
}
