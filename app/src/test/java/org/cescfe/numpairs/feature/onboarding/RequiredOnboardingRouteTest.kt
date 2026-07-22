package org.cescfe.numpairs.feature.onboarding

import org.cescfe.numpairs.data.onboarding.OnboardingStageCheckpoint
import org.cescfe.numpairs.data.onboarding.OnboardingState
import org.cescfe.numpairs.feature.tutorial.TutorialContent
import org.junit.Assert.assertEquals
import org.junit.Test

class RequiredOnboardingRouteTest {
    @Test
    fun `checkpoints resume after the last completed Tutorial step`() {
        assertEquals(0, state(OnboardingStageCheckpoint.NONE).nextRequiredTutorialStepIndex())
        assertEquals(1, state(OnboardingStageCheckpoint.STAGE_ONE).nextRequiredTutorialStepIndex())
        assertEquals(2, state(OnboardingStageCheckpoint.STAGE_TWO).nextRequiredTutorialStepIndex())
    }

    @Test
    fun `resume meaning survives a non-checkpoint step insertion`() {
        val stepsWithInsertedOrientation = TutorialContent.learnBasicsSteps.toMutableList().apply {
            add(
                index = 0,
                element = first().copy(progressCheckpoint = null)
            )
        }

        assertEquals(
            2,
            state(OnboardingStageCheckpoint.STAGE_ONE).nextRequiredTutorialStepIndex(
                steps = stepsWithInsertedOrientation
            )
        )
        assertEquals(
            3,
            state(OnboardingStageCheckpoint.STAGE_TWO).nextRequiredTutorialStepIndex(
                steps = stepsWithInsertedOrientation
            )
        )
    }

    private fun state(checkpoint: OnboardingStageCheckpoint): OnboardingState = OnboardingState(
        lastCompletedStage = checkpoint
    )
}
