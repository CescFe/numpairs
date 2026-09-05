package org.cescfe.numpairs.feature.game

import org.junit.Assert.assertThrows
import org.junit.Test

class GameSuccessOverlayContentTest {
    @Test
    fun standard_completion_copy_requires_an_indivisible_nonblank_pair() {
        assertThrows(IllegalArgumentException::class.java) {
            GameSuccessOverlayCopy(
                message = "",
                supportingText = "Supporting"
            )
        }
        assertThrows(IllegalArgumentException::class.java) {
            GameSuccessOverlayCopy(
                message = "Completed",
                supportingText = " "
            )
        }
    }

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
        assertThrows(IllegalArgumentException::class.java) {
            GameSuccessOverlayCopy(
                message = "Completed",
                supportingText = "Supporting",
                contextContentDescription = "Best time: 02:05"
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
