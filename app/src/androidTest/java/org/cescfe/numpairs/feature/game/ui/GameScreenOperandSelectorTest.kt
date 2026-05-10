package org.cescfe.numpairs.feature.game.ui

import androidx.test.ext.junit.runners.AndroidJUnit4
import org.cescfe.numpairs.R
import org.cescfe.numpairs.domain.puzzle.Operator
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class GameScreenOperandSelectorTest : GameScreenTestHost() {
    @Test
    fun selectingAnOperandFromAHiddenSlotUpdatesTheTileAndClosesTheSheet() {
        screen
            .scrollToBoard()
            .tapTileLeftOperand(0)
            .assertOperandSelectorDisplayed()
            .assertOperandOptionDisplayed(entryId = 2)
            .assertOperandOptionDisplayed(entryId = 4)
            .assertOperandOptionDisplayed(entryId = 7)
            .tapOperandOption(entryId = 2)
            .assertLeftOperandDescription(
                0,
                R.string.tile_left_operand_content_description,
                "6"
            )
            .assertOperandSelectorHidden()
    }

    @Test
    fun tappingAFilledOperandReopensTheSheetWithoutSelectedOperandEmphasis() {
        fillTileLeftOperandWithEntryTwo(tileIndex = 0)

        screen
            .tapTileLeftOperand(0)
            .assertOperandSelectorDisplayed()
            .assertNoSelectedNodes()
    }

    @Test
    fun backDismissesTheOperandSelectorWithoutChangingHiddenOrFilledSlots() {
        screen
            .scrollToBoard()
            .tapTileLeftOperand(0)
            .pressBack()
            .assertLeftOperandDescription(
                0,
                R.string.tile_left_operand_hidden_content_description
            )
            .assertOperandSelectorHidden()

        fillTileLeftOperandWithEntryTwo(tileIndex = 0)

        screen
            .tapTileLeftOperand(0)
            .pressBack()
            .assertLeftOperandDescription(
                0,
                R.string.tile_left_operand_content_description,
                "6"
            )
            .assertOperandSelectorHidden()
    }

    @Test
    fun operandSelectorUsageHintsReflectWhetherTheOperatorContextIsKnown() {
        fillTileLeftOperandWithEntryTwo(tileIndex = 0)

        screen
            .tapTileLeftOperand(1)
            .assertOperandUsageHintState(
                entryId = 2,
                operator = Operator.ADDITION,
                stateDescriptionResId = R.string.tile_operand_usage_state_available
            )
            .assertOperandUsageHintState(
                entryId = 2,
                operator = Operator.MULTIPLICATION,
                stateDescriptionResId = R.string.tile_operand_usage_state_available
            )
            .pressBack()
            .tapTileOperator(0)
            .tapOperatorOption(Operator.ADDITION)
            .tapTileLeftOperand(1)
            .assertOperandUsageHintState(
                entryId = 2,
                operator = Operator.ADDITION,
                stateDescriptionResId = R.string.tile_operand_usage_state_used
            )
            .assertOperandUsageHintState(
                entryId = 2,
                operator = Operator.MULTIPLICATION,
                stateDescriptionResId = R.string.tile_operand_usage_state_available
            )
    }

    @Test
    fun selectorRendersBlockedAndExhaustedEntriesWithTheCorrectAvailability() {
        fillTileLeftOperandWithEntryTwo(tileIndex = 0, operator = Operator.ADDITION)

        screen
            .tapTileRightOperand(0)
            .assertOperandOptionDisabled(entryId = 2)
            .pressBack()

        fillTileLeftOperandWithEntryTwo(tileIndex = 1, operator = Operator.MULTIPLICATION)

        screen
            .tapTileLeftOperand(2)
            .assertOperandOptionDisabled(entryId = 2)
            .pressBack()
            .tapTileLeftOperand(0)
            .assertOperandOptionEnabled(entryId = 2)
    }

    private fun fillTileLeftOperandWithEntryTwo(tileIndex: Int, operator: Operator? = null) {
        screen
            .scrollToBoard()
            .tapTileLeftOperand(tileIndex)
            .tapOperandOption(entryId = 2)

        if (operator != null) {
            screen
                .tapTileOperator(tileIndex)
                .tapOperatorOption(operator)
        }
    }
}
