package org.cescfe.numpairs.feature.game

import org.junit.Assert.assertThrows
import org.junit.Test

class GameSuccessOverlayContentTest {
    @Test
    fun highlight_accessibility_requires_visible_highlight_text() {
        assertThrows(IllegalArgumentException::class.java) {
            GameSuccessOverlayContent(
                message = "Completed",
                supportingText = "Supporting",
                highlightContentDescription = "Elapsed time: 02:05",
                primaryActionLabel = "Share",
                onPrimaryAction = {}
            )
        }
    }

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
