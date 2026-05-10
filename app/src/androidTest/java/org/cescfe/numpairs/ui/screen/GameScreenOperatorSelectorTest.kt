package org.cescfe.numpairs.feature.game

import androidx.test.ext.junit.runners.AndroidJUnit4
import org.cescfe.numpairs.R
import org.cescfe.numpairs.domain.puzzle.Operator
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class GameScreenOperatorSelectorTest : GameScreenTestHost() {
    @Test
    fun tappingHiddenTileOperatorOpensContextualSelector() {
        screen
            .scrollToBoard()
            .tapTileOperator(0)
            .assertOperatorSelectorDisplayed()
            .assertOperatorOptionDisplayed(Operator.ADDITION)
            .assertOperatorOptionDisplayed(Operator.MULTIPLICATION)
    }

    @Test
    fun selectingAnOperatorOptionCompletesTheHiddenTileOperatorImmediately() {
        screen
            .scrollToBoard()
            .tapTileOperator(0)
            .tapOperatorOption(Operator.ADDITION)
            .assertOperatorDescription(
                0,
                R.string.tile_operator_content_description,
                composeTestRule.activity.getString(R.string.tile_operator_option_addition)
            )
            .assertOperatorSelectorHidden()
    }

    @Test
    fun dismissingTheOperatorSelectorLeavesTheHiddenTileOperatorUnchanged() {
        screen
            .scrollToBoard()
            .tapTileOperator(0)
            .pressBack()
            .assertOperatorDescription(
                0,
                R.string.tile_operator_hidden_content_description
            )
            .assertOperatorSelectorHidden()
    }

    @Test
    fun tappingFilledTileOperatorReopensTheSelectorWithTheCurrentOptionSelectedAndAllowsReassignment() {
        screen
            .scrollToBoard()
            .tapTileOperator(0)
            .tapOperatorOption(Operator.ADDITION)
            .tapTileOperator(0)
            .assertOperatorSelectorDisplayed()
            .assertOperatorOptionSelected(Operator.ADDITION)
            .tapOperatorOption(Operator.MULTIPLICATION)
            .assertOperatorDescription(
                0,
                R.string.tile_operator_content_description,
                composeTestRule.activity.getString(R.string.tile_operator_option_multiplication)
            )
    }
}
