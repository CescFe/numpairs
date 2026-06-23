package org.cescfe.numpairs.feature.game.ui

import androidx.test.ext.junit.runners.AndroidJUnit4
import org.cescfe.numpairs.R
import org.cescfe.numpairs.domain.puzzle.model.Operator
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class GameScreenTileStateTest : GameScreenTestHost() {
    @Test
    fun fillingATileShowsTheResetActionAndResetRestoresTheInitialExpression() {
        screen
            .scrollToBoard()
            .assertResetHidden(tileIndex = 0)
            .tapTileLeftOperand(0)
            .tapOperandOption(entryId = 2)
            .assertResetVisible(tileIndex = 0)
            .tapTileReset(0)
            .assertLeftOperandDescription(
                0,
                R.string.tile_left_operand_hidden_content_description
            )
            .assertOperatorDescription(
                0,
                R.string.tile_operator_hidden_content_description
            )
            .assertRightOperandDescription(
                0,
                R.string.tile_right_operand_hidden_content_description
            )
            .assertResetHidden(tileIndex = 0)
    }

    @Test
    fun incorrectTilesExposeInvalidStateAndCanBeCorrectedFromTheUi() {
        buildIncorrectFirstTile()

        screen
            .assertTileStateDescription(
                tileIndex = 0,
                stringResId = R.string.tile_state_incorrect
            )
            .tapTileOperator(0)
            .assertOperatorSelectorDisplayed()
            .tapOperatorOption(Operator.ADDITION)
            .assertTileHasNoStateDescription(tileIndex = 0)
    }

    private fun buildIncorrectFirstTile() {
        screen
            .tapStripItem(1)
            .enterStripValue("1")
            .submitStripEntryInput()
            .scrollToBoard()
            .tapTileLeftOperand(0)
            .tapOperandOption(entryId = 1)
            .tapTileOperator(0)
            .tapOperatorOption(Operator.MULTIPLICATION)
            .tapTileRightOperand(0)
            .tapOperandOption(entryId = 7)
    }
}
