package org.cescfe.numpairs.ui.screen

import androidx.test.ext.junit.runners.AndroidJUnit4
import org.cescfe.numpairs.R
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class GameScreenAccessibilityTest : GameScreenTestHost() {
    @Test
    fun launchesSuccessfully() {
        screen.assertTitleDisplayed()
    }

    @Test
    fun displaysPuzzleBoard() {
        screen.assertBoardDisplayed()
    }

    @Test
    fun displaysStrip() {
        screen.assertStripDisplayed()
    }

    @Test
    fun hiddenStripItemExposesAnAccessibleChipLabel() {
        screen.assertStripItemDescription(
            1,
            R.string.strip_item_hidden_content_description
        )
    }

    @Test
    fun knownStripItemExposesAnAccessibleChipLabel() {
        screen.assertStripItemDescription(
            2,
            R.string.strip_item_known_content_description,
            "6"
        )
    }

    @Test
    fun hiddenLeftOperandExposesAnAccessibleSlotLabel() {
        screen.assertLeftOperandDescription(
            0,
            R.string.tile_left_operand_hidden_content_description
        )
    }

    @Test
    fun hiddenRightOperandExposesAnAccessibleSlotLabel() {
        screen.assertRightOperandDescription(
            0,
            R.string.tile_right_operand_hidden_content_description
        )
    }

    @Test
    fun hiddenOperatorExposesAnAccessibleSlotLabel() {
        screen.assertOperatorDescription(
            0,
            R.string.tile_operator_hidden_content_description
        )
    }
}
