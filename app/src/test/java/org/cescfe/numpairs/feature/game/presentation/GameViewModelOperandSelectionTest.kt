package org.cescfe.numpairs.feature.game.presentation

import org.cescfe.numpairs.domain.puzzle.OperandSlot
import org.cescfe.numpairs.domain.puzzle.Operator
import org.cescfe.numpairs.domain.puzzle.support.puzzleWithRepeatedSixes
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class GameViewModelOperandSelectionTest {
    @Test
    fun tapping_hidden_tile_operands_opens_the_selection_dialog_for_the_requested_slot() {
        val leftOperandViewModel = GameViewModel()

        leftOperandViewModel.onTileLeftOperandTapped(index = 0)

        assertSelectionDialogTargetForFirstTile(
            viewModel = leftOperandViewModel,
            slot = OperandSlot.LEFT
        )

        val rightOperandViewModel = GameViewModel()

        rightOperandViewModel.onTileRightOperandTapped(index = 0)

        assertSelectionDialogTargetForFirstTile(
            viewModel = rightOperandViewModel,
            slot = OperandSlot.RIGHT
        )
    }

    @Test
    fun player_entered_strip_items_are_included_in_hidden_tile_operand_selection() {
        val viewModel = GameViewModel()

        viewModel.onStripItemTapped(index = 1)
        viewModel.onStripItemEntryConfirmed(value = 2)
        viewModel.onTileLeftOperandTapped(index = 0)

        val dialog = requireNotNull(viewModel.uiState.value.tileOperandSelectionDialog)

        assertEquals(OperandSlot.LEFT, dialog.slot)
        assertTrue(
            dialog.availableOperands.any { operand ->
                operand.stripEntryId == 1 && operand.value == 2 && operand.isSelectable
            }
        )
    }

    @Test
    fun reopening_a_filled_operand_dialog_keeps_the_current_entry_selectable_for_that_slot() {
        val leftOperandViewModel = GameViewModel()

        leftOperandViewModel.onTileLeftOperandTapped(index = 0)
        leftOperandViewModel.onTileOperandSelectionConfirmed(stripEntryId = 2)
        leftOperandViewModel.onTileLeftOperandTapped(index = 0)

        var dialog = requireNotNull(leftOperandViewModel.uiState.value.tileOperandSelectionDialog)
        assertEquals(OperandSlot.LEFT, dialog.slot)
        assertTrue(
            dialog.availableOperands.any { operand ->
                operand.stripEntryId == 2 && operand.isSelectable
            }
        )

        val rightOperandViewModel = GameViewModel()

        rightOperandViewModel.onTileRightOperandTapped(index = 0)
        rightOperandViewModel.onTileOperandSelectionConfirmed(stripEntryId = 2)
        rightOperandViewModel.onTileRightOperandTapped(index = 0)

        dialog = requireNotNull(rightOperandViewModel.uiState.value.tileOperandSelectionDialog)
        assertEquals(OperandSlot.RIGHT, dialog.slot)
        assertTrue(
            dialog.availableOperands.any { operand ->
                operand.stripEntryId == 2 && operand.isSelectable
            }
        )
    }

    @Test
    fun confirming_the_selection_dialog_completes_the_hidden_left_tile_operand() {
        val viewModel = GameViewModel()

        viewModel.onTileLeftOperandTapped(index = 0)
        viewModel.onTileOperandSelectionConfirmed(stripEntryId = 2)

        val uiState = viewModel.uiState.value

        assertEquals(TileUiState("6", "?", "?", "223", canReset = true), uiState.tiles.first())
        assertNull(uiState.tileOperandSelectionDialog)
    }

    @Test
    fun confirming_the_selection_dialog_replaces_a_filled_left_tile_operand() {
        val viewModel = GameViewModel()

        viewModel.onTileLeftOperandTapped(index = 0)
        viewModel.onTileOperandSelectionConfirmed(stripEntryId = 2)
        viewModel.onTileLeftOperandTapped(index = 0)
        viewModel.onTileOperandSelectionConfirmed(stripEntryId = 4)

        val uiState = viewModel.uiState.value

        assertEquals(TileUiState("25", "?", "?", "223", canReset = true), uiState.tiles.first())
        assertNull(uiState.tileOperandSelectionDialog)
    }

    @Test
    fun confirming_the_opposite_slot_current_entry_is_rejected_without_mutating_the_tile() {
        val viewModel = GameViewModel()

        viewModel.onTileLeftOperandTapped(index = 0)
        viewModel.onTileOperandSelectionConfirmed(stripEntryId = 2)
        viewModel.onTileRightOperandTapped(index = 0)

        val beforeAttempt = viewModel.uiState.value

        viewModel.onTileOperandSelectionConfirmed(stripEntryId = 2)

        assertEquals(beforeAttempt.tileOperandSelectionDialog, viewModel.uiState.value.tileOperandSelectionDialog)
        assertEquals(beforeAttempt.tiles.first(), viewModel.uiState.value.tiles.first())
    }

    @Test
    fun cancelling_the_selection_dialog_leaves_hidden_and_filled_tile_operands_unchanged() {
        val hiddenOperandViewModel = GameViewModel()

        hiddenOperandViewModel.onTileLeftOperandTapped(index = 0)
        hiddenOperandViewModel.onTileOperandSelectionDismissed()

        var uiState = hiddenOperandViewModel.uiState.value
        assertEquals(TileUiState("?", "?", "?", "223"), uiState.tiles.first())
        assertNull(uiState.tileOperandSelectionDialog)

        val filledOperandViewModel = GameViewModel()

        filledOperandViewModel.onTileLeftOperandTapped(index = 0)
        filledOperandViewModel.onTileOperandSelectionConfirmed(stripEntryId = 2)
        filledOperandViewModel.onTileLeftOperandTapped(index = 0)
        filledOperandViewModel.onTileOperandSelectionDismissed()

        uiState = filledOperandViewModel.uiState.value
        assertEquals(TileUiState("6", "?", "?", "223", canReset = true), uiState.tiles.first())
        assertNull(uiState.tileOperandSelectionDialog)
    }

    @Test
    fun confirming_an_exhausted_operand_does_not_mutate_the_puzzle_or_close_the_dialog() {
        val viewModel = GameViewModel()

        assignEntryTwoToLeftOperand(index = 0, operator = Operator.ADDITION, viewModel = viewModel)
        assignEntryTwoToLeftOperand(index = 1, operator = Operator.MULTIPLICATION, viewModel = viewModel)
        viewModel.onTileLeftOperandTapped(index = 2)

        val beforeAttempt = viewModel.uiState.value

        viewModel.onTileOperandSelectionConfirmed(stripEntryId = 2)

        assertEquals(beforeAttempt.tileOperandSelectionDialog, viewModel.uiState.value.tileOperandSelectionDialog)
        assertEquals(beforeAttempt.tiles[2], viewModel.uiState.value.tiles[2])
    }

    @Test
    fun repeated_numeric_values_survive_the_view_model_flow_with_distinct_ids() {
        val viewModel = GameViewModel(
            initialPuzzle = puzzleWithRepeatedSixes()
        )

        viewModel.onTileLeftOperandTapped(index = 0)
        viewModel.onTileOperandSelectionConfirmed(stripEntryId = 0)
        viewModel.onTileOperatorTapped(index = 0)
        viewModel.onTileOperatorSelectionConfirmed(operator = Operator.ADDITION)
        viewModel.onTileLeftOperandTapped(index = 1)

        val dialog = requireNotNull(viewModel.uiState.value.tileOperandSelectionDialog)
        val repeatedValueChoices = dialog.availableOperands.filter { operand -> operand.value == 6 }

        assertEquals(listOf(0, 1), repeatedValueChoices.map { operand -> operand.stripEntryId })
        assertFalse(repeatedValueChoices.first { operand -> operand.stripEntryId == 0 }.multiplicationUsed)
        assertTrue(repeatedValueChoices.first { operand -> operand.stripEntryId == 0 }.additionUsed)
        assertFalse(repeatedValueChoices.first { operand -> operand.stripEntryId == 1 }.additionUsed)
    }
}

private fun assertSelectionDialogTargetForFirstTile(viewModel: GameViewModel, slot: OperandSlot) {
    val dialog = requireNotNull(viewModel.uiState.value.tileOperandSelectionDialog)

    assertEquals(0, dialog.tileIndex)
    assertEquals(slot, dialog.slot)
    assertTrue(dialog.availableOperands.isNotEmpty())
}

private fun assignEntryTwoToLeftOperand(index: Int, operator: Operator, viewModel: GameViewModel) {
    viewModel.onTileLeftOperandTapped(index = index)
    viewModel.onTileOperandSelectionConfirmed(stripEntryId = 2)
    viewModel.onTileOperatorTapped(index = index)
    viewModel.onTileOperatorSelectionConfirmed(operator = operator)
}
