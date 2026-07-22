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
        assertEquals(
            5,
            state(OnboardingStageCheckpoint.EXPLANATION_COMPLETED).nextRequiredTutorialStepIndex()
        )
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
            6,
            state(OnboardingStageCheckpoint.EXPLANATION_COMPLETED).nextRequiredTutorialStepIndex(
                steps = stepsWithInsertedOrientation
            )
        )
    }

    private fun state(checkpoint: OnboardingStageCheckpoint): OnboardingState = OnboardingState(
        lastCompletedStage = checkpoint
    )
}
