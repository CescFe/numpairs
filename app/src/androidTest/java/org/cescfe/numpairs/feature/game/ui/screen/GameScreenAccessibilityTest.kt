package org.cescfe.numpairs.feature.game.ui.screen

import androidx.test.ext.junit.runners.AndroidJUnit4
import org.cescfe.numpairs.R
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class GameScreenAccessibilityTest : GameScreenTestHost() {
    @Test
    fun gameScreenDisplaysItsCoreRegions() {
        screen
            .assertTitleDisplayed()
            .assertStripDisplayed()
            .assertBoardDisplayed()
    }

    @Test
    fun stripItemsAndHiddenTileSlotsExposeAccessibleDescriptions() {
        screen
            .assertStripItemDescription(
                1,
                R.string.strip_item_hidden_content_description
            )
            .assertStripItemDescription(
                2,
                R.string.strip_item_known_content_description,
                "6"
            )
            .assertLeftOperandDescription(
                0,
                R.string.tile_left_operand_hidden_content_description
            )
            .assertRightOperandDescription(
                0,
                R.string.tile_right_operand_hidden_content_description
            )
            .assertOperatorDescription(
                0,
                R.string.tile_operator_hidden_content_description
            )
    }
}
