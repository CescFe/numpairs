package org.cescfe.numpairs.feature.game.ui.screen

import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.unit.dp
import androidx.test.ext.junit.runners.AndroidJUnit4
import org.cescfe.numpairs.R
import org.cescfe.numpairs.domain.puzzle.model.Operator
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class GameScreenOperatorSelectorTest : GameScreenTestHost() {
    @Test
    fun tappingAHiddenTileOperatorOpensTheSelectorAndSelectionUpdatesTheTile() {
        screen
            .scrollToBoard()
            .tapTileOperator(0)
            .assertOperatorSelectorDisplayed()
            .assertOperatorSelectorTitle(R.string.tile_operator_dialog_title)
            .assertOperatorSelectorExpression(
                R.string.tile_operator_dialog_expression_preview,
                "?",
                "?",
                "223"
            )
            .assertOperatorOptionDisplayed(Operator.ADDITION)
            .assertOperatorOptionDisplayed(Operator.MULTIPLICATION)
            .tapOperatorOption(Operator.ADDITION)
            .assertOperatorDescription(
                0,
                R.string.tile_operator_content_description,
                composeTestRule.activity.getString(R.string.tile_operator_option_addition)
            )
            .assertOperatorSelectorHidden()
    }

    @Test
    fun operatorOptionsAreCompactAndTheirSymbolsAreCentered() {
        screen
            .scrollToBoard()
            .tapTileOperator(0)

        val maximumOptionWidth = with(composeTestRule.density) {
            TILE_SELECTION_SHEET_OPTION_CARD_MAX_WIDTH.toPx()
        }
        val centerTolerance = with(composeTestRule.density) { 0.5.dp.toPx() }

        listOf(Operator.ADDITION, Operator.MULTIPLICATION).forEach { operator ->
            val optionBounds = composeTestRule
                .onNodeWithTag(GameScreenTestTags.tileOperatorOption(operator), useUnmergedTree = true)
                .fetchSemanticsNode()
                .boundsInRoot
            val symbolBounds = composeTestRule
                .onNodeWithTag(GameScreenTestTags.tileOperatorOptionSymbol(operator), useUnmergedTree = true)
                .fetchSemanticsNode()
                .boundsInRoot

            assertTrue(optionBounds.width <= maximumOptionWidth + centerTolerance)
            assertEquals(optionBounds.center.x, symbolBounds.center.x, centerTolerance)
            assertEquals(optionBounds.center.y, symbolBounds.center.y, centerTolerance)
        }
    }

    @Test
    fun backDismissesTheHiddenOperatorSelectorWithoutChangingTheTile() {
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
    fun tappingAFilledTileOperatorReopensTheSelectorWithTheCurrentOptionSelectedAndAllowsReassignment() {
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
