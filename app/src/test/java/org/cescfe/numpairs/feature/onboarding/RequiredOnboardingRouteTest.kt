package org.cescfe.numpairs.feature.onboarding

import org.cescfe.numpairs.data.onboarding.OnboardingPostCorePath
import org.cescfe.numpairs.data.onboarding.OnboardingStageCheckpoint
import org.cescfe.numpairs.data.onboarding.OnboardingState
import org.cescfe.numpairs.feature.tutorial.GuidedOnboardingStage
import org.junit.Assert.assertEquals
import org.junit.Test

class RequiredOnboardingRouteTest {
    @Test
    fun `early checkpoints resume at the next required guided stage`() {
        assertEquals(
            RequiredOnboardingStep.GuidedStage(GuidedOnboardingStage.NUMBER_PLACEMENT),
            state(OnboardingStageCheckpoint.NONE).nextRequiredStep()
        )
        assertEquals(
            RequiredOnboardingStep.GuidedStage(GuidedOnboardingStage.COMPLEMENTARY_PAIR),
            state(OnboardingStageCheckpoint.STAGE_ONE).nextRequiredStep()
        )
    }

    @Test
    fun `stage two requires a path choice and resumes the selected path`() {
        assertEquals(
            RequiredOnboardingStep.PostCoreChoice,
            state(OnboardingStageCheckpoint.STAGE_TWO).nextRequiredStep()
        )
        assertEquals(
            RequiredOnboardingStep.GuidedStage(GuidedOnboardingStage.HIDDEN_STRIP_VALUE),
            state(
                checkpoint = OnboardingStageCheckpoint.STAGE_TWO,
                path = OnboardingPostCorePath.CONTINUE_GUIDED
            ).nextRequiredStep()
        )
        assertEquals(
            RequiredOnboardingStep.FinalValidation,
            state(
                checkpoint = OnboardingStageCheckpoint.STAGE_TWO,
                path = OnboardingPostCorePath.EARLY_VALIDATION
            ).nextRequiredStep()
        )
    }

    @Test
    fun `stage three resumes final validation`() {
        assertEquals(
            RequiredOnboardingStep.FinalValidation,
            state(OnboardingStageCheckpoint.STAGE_THREE).nextRequiredStep()
        )
    }

    private fun state(
        checkpoint: OnboardingStageCheckpoint,
        path: OnboardingPostCorePath = OnboardingPostCorePath.UNDECIDED
    ): OnboardingState = OnboardingState(
        isInitialized = true,
        lastCompletedStage = checkpoint,
        postCorePath = path
    )
}
