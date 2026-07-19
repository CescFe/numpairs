package org.cescfe.numpairs.feature.tutorial

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class TutorialSuccessOverlayPolicyTest {
    @Test
    fun `solving tips waits for its own scenario state before enabling success`() {
        val finalScenarioId = TutorialScenarioId.SOLVING_TIPS_PRACTICE

        assertFalse(
            isTutorialSuccessOverlayEnabled(
                mode = TutorialMode.SOLVING_TIPS_PRACTICE,
                isFinalStep = true,
                currentScenarioId = finalScenarioId,
                observedScenarioId = TutorialScenarioId.STRIP_AND_TILES_INTRODUCTION,
                isRequiredPlayback = false
            )
        )
        assertTrue(
            isTutorialSuccessOverlayEnabled(
                mode = TutorialMode.SOLVING_TIPS_PRACTICE,
                isFinalStep = true,
                currentScenarioId = finalScenarioId,
                observedScenarioId = finalScenarioId,
                isRequiredPlayback = false
            )
        )
    }

    @Test
    fun `learn basics never enables the success overlay`() {
        val finalScenarioId = TutorialScenarioId.REPEATED_VALUE_PRACTICE

        assertFalse(
            isTutorialSuccessOverlayEnabled(
                mode = TutorialMode.LEARN_BASICS,
                isFinalStep = true,
                currentScenarioId = finalScenarioId,
                observedScenarioId = finalScenarioId,
                isRequiredPlayback = false
            )
        )
    }

    @Test
    fun `required playback never enables the success overlay`() {
        val finalScenarioId = TutorialScenarioId.SOLVING_TIPS_PRACTICE

        assertFalse(
            isTutorialSuccessOverlayEnabled(
                mode = TutorialMode.SOLVING_TIPS_PRACTICE,
                isFinalStep = true,
                currentScenarioId = finalScenarioId,
                observedScenarioId = finalScenarioId,
                isRequiredPlayback = true
            )
        )
    }
}
