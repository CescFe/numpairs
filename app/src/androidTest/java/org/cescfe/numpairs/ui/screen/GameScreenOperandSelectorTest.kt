package org.cescfe.numpairs.feature.game.ui

import androidx.test.ext.junit.runners.AndroidJUnit4
import org.cescfe.numpairs.R
import org.cescfe.numpairs.domain.puzzle.Operator
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class GameScreenOperandSelectorTest : GameScreenTestHost() {
    @Test
    fun tappingHiddenLeftTileOperandOpensBottomSheetSelector() {
        screen
            .scrollToBoard()
            .tapTileLeftOperand(0)
            .assertOperandSelectorDisplayed()
            .assertOperandOptionDisplayed(entryId = 2)
            .assertOperandOptionDisplayed(entryId = 4)
            .assertOperandOptionDisplayed(entryId = 7)
    }

    @Test
    fun tappingHiddenRightTileOperandOpensBottomSheetSelector() {
        screen
            .scrollToBoard()
            .tapTileRightOperand(0)
            .assertOperandSelectorDisplayed()
    }

    @Test
    fun playerEnteredStripItemAppearsAsSelectableOperandValue() {
        completeSecondStripItemWithPlayerValueTwo()

        screen
            .scrollToBoard()
            .tapTileLeftOperand(0)
            .assertOperandOptionDisplayed(entryId = 1)
    }

    @Test
    fun selectingAnOperandOptionCompletesTheHiddenTileOperandImmediately() {
        screen
            .scrollToBoard()
            .tapTileLeftOperand(0)
            .tapOperandOption(entryId = 2)
            .assertLeftOperandDescription(
                0,
                R.string.tile_left_operand_content_description,
                "6"
            )
            .assertOperandSelectorHidden()
    }

    @Test
    fun tappingFilledLeftTileOperandReopensBottomSheetSelectorWithoutSelectedOperandEmphasis() {
        fillTileLeftOperandWithEntryTwo(tileIndex = 0)

        screen
            .tapTileLeftOperand(0)
            .assertOperandSelectorDisplayed()
            .assertNoSelectedNodes()
    }

    @Test
    fun tappingFilledRightTileOperandReopensBottomSheetSelector() {
        screen
            .scrollToBoard()
            .tapTileRightOperand(0)
            .tapOperandOption(entryId = 2)
            .tapTileRightOperand(0)
            .assertOperandSelectorDisplayed()
    }

    @Test
    fun selectingAnOperandOptionReplacesTheFilledTileOperandImmediately() {
        fillTileLeftOperandWithEntryTwo(tileIndex = 0)

        screen
            .tapTileLeftOperand(0)
            .tapOperandOption(entryId = 4)
            .assertLeftOperandDescription(
                0,
                R.string.tile_left_operand_content_description,
                "25"
            )
    }

    @Test
    fun the_current_tile_entry_is_rendered_disabled_for_the_opposite_operand_slot() {
        fillTileLeftOperandWithEntryTwo(tileIndex = 0)

        screen
            .tapTileRightOperand(0)
            .assertOperandOptionDisabled(entryId = 2)
    }

    @Test
    fun dismissingTheOperandSelectorLeavesTheHiddenTileOperandUnchanged() {
        screen
            .scrollToBoard()
            .tapTileLeftOperand(0)
            .pressBack()
            .assertLeftOperandDescription(
                0,
                R.string.tile_left_operand_hidden_content_description
            )
            .assertOperandSelectorHidden()
    }

    @Test
    fun dismissingTheOperandSelectorLeavesTheFilledTileOperandUnchanged() {
        fillTileLeftOperandWithEntryTwo(tileIndex = 0)

        screen
            .tapTileLeftOperand(0)
            .pressBack()
            .assertLeftOperandDescription(
                0,
                R.string.tile_left_operand_content_description,
                "6"
            )
    }

    @Test
    fun operandSelectorShowsOperatorSpecificUsageHintsWithoutVerboseText() {
        fillTileLeftOperandWithEntryTwo(tileIndex = 0)

        screen
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
    fun operandSelectorDoesNotMarkUsageHintsUntilTheTileOperatorIsKnown() {
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
    }

    @Test
    fun exhausted_operand_options_are_rendered_disabled_in_the_picker() {
        fillTileLeftOperandWithEntryTwo(tileIndex = 0, operator = Operator.ADDITION)
        fillTileLeftOperandWithEntryTwo(tileIndex = 1, operator = Operator.MULTIPLICATION)

        screen
            .tapTileLeftOperand(2)
            .assertOperandOptionDisabled(entryId = 2)
    }

    @Test
    fun reopening_a_slot_keeps_its_current_exhausted_operand_enabled() {
        fillTileLeftOperandWithEntryTwo(tileIndex = 0, operator = Operator.ADDITION)
        fillTileLeftOperandWithEntryTwo(tileIndex = 1, operator = Operator.MULTIPLICATION)

        screen
            .tapTileLeftOperand(0)
            .assertOperandOptionEnabled(entryId = 2)
    }

    private fun completeSecondStripItemWithPlayerValueTwo() {
        screen
            .tapStripItem(1)
            .enterStripValue("2")
            .confirmStripEntry()
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
