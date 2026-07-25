package org.cescfe.numpairs.feature.game

import org.junit.Assert.assertThrows
import org.junit.Test

class GameSuccessOverlayContentTest {
    @Test
    fun optional_completion_actions_require_a_label_and_callback_together() {
        assertThrows(IllegalArgumentException::class.java) {
            GameSuccessOverlayContent(
                message = "Completed",
                supportingText = "Supporting",
                primaryActionLabel = "Share",
                onPrimaryAction = {},
                secondaryActionLabel = "Calendar"
            )
        }
        assertThrows(IllegalArgumentException::class.java) {
            GameSuccessOverlayContent(
                message = "Completed",
                supportingText = "Supporting",
                primaryActionLabel = "Share",
                onPrimaryAction = {},
                onTertiaryAction = {}
            )
        }
    }
}
