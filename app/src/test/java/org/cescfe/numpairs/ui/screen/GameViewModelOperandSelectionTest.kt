package org.cescfe.numpairs.feature.game

import org.cescfe.numpairs.domain.puzzle.OperandSlot
import org.cescfe.numpairs.domain.puzzle.Operator
import org.cescfe.numpairs.domain.puzzle.support.puzzleWithRepeatedSixes
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Test

class GameViewModelOperandSelectionTest {
    @Test
    fun tapping_a_hidden_left_tile_operand_opens_the_selection_dialog() {
        val viewModel = GameViewModel()

        viewModel.onTileLeftOperandTapped(index = 0)

        assertEquals(
            TileOperandSelectionDialogUiState(
                tileIndex = 0,
                slot = OperandSlot.LEFT,
                availableOperands = listOf(
                    TileOperandOptionUiState(
                        2,
                        6,
                        additionUsed = false,
                        multiplicationUsed = false,
                        isSelectable = true
                    ),
                    TileOperandOptionUiState(
                        4,
                        25,
                        additionUsed = false,
                        multiplicationUsed = false,
                        isSelectable = true
                    ),
                    TileOperandOptionUiState(
                        7,
                        222,
                        additionUsed = false,
                        multiplicationUsed = false,
                        isSelectable = true
                    )
                )
            ),
            viewModel.uiState.value.tileOperandSelectionDialog
        )
    }

    @Test
    fun tapping_a_hidden_right_tile_operand_opens_the_selection_dialog() {
        val viewModel = GameViewModel()

        viewModel.onTileRightOperandTapped(index = 0)

        assertEquals(
            TileOperandSelectionDialogUiState(
                tileIndex = 0,
                slot = OperandSlot.RIGHT,
                availableOperands = listOf(
                    TileOperandOptionUiState(
                        2,
                        6,
                        additionUsed = false,
                        multiplicationUsed = false,
                        isSelectable = true
                    ),
                    TileOperandOptionUiState(
                        4,
                        25,
                        additionUsed = false,
                        multiplicationUsed = false,
                        isSelectable = true
                    ),
                    TileOperandOptionUiState(
                        7,
                        222,
                        additionUsed = false,
                        multiplicationUsed = false,
                        isSelectable = true
                    )
                )
            ),
            viewModel.uiState.value.tileOperandSelectionDialog
        )
    }

    @Test
    fun player_entered_strip_items_are_included_in_hidden_tile_operand_selection() {
        val viewModel = GameViewModel()

        viewModel.onStripItemTapped(index = 1)
        viewModel.onStripItemEntryConfirmed(value = 2)
        viewModel.onTileLeftOperandTapped(index = 0)

        assertEquals(
            TileOperandSelectionDialogUiState(
                tileIndex = 0,
                slot = OperandSlot.LEFT,
                availableOperands = listOf(
                    TileOperandOptionUiState(
                        1,
                        2,
                        additionUsed = false,
                        multiplicationUsed = false,
                        isSelectable = true
                    ),
                    TileOperandOptionUiState(
                        2,
                        6,
                        additionUsed = false,
                        multiplicationUsed = false,
                        isSelectable = true
                    ),
                    TileOperandOptionUiState(
                        4,
                        25,
                        additionUsed = false,
                        multiplicationUsed = false,
                        isSelectable = true
                    ),
                    TileOperandOptionUiState(
                        7,
                        222,
                        additionUsed = false,
                        multiplicationUsed = false,
                        isSelectable = true
                    )
                )
            ),
            viewModel.uiState.value.tileOperandSelectionDialog
        )
    }

    @Test
    fun tapping_a_filled_left_tile_operand_reopens_the_selection_dialog_with_the_current_operand_still_available() {
        val viewModel = GameViewModel()

        viewModel.onTileLeftOperandTapped(index = 0)
        viewModel.onTileOperandSelectionConfirmed(stripEntryId = 2)
        viewModel.onTileLeftOperandTapped(index = 0)

        assertEquals(
            TileOperandSelectionDialogUiState(
                tileIndex = 0,
                slot = OperandSlot.LEFT,
                availableOperands = listOf(
                    TileOperandOptionUiState(
                        2,
                        6,
                        additionUsed = false,
                        multiplicationUsed = false,
                        isSelectable = true
                    ),
                    TileOperandOptionUiState(
                        4,
                        25,
                        additionUsed = false,
                        multiplicationUsed = false,
                        isSelectable = true
                    ),
                    TileOperandOptionUiState(
                        7,
                        222,
                        additionUsed = false,
                        multiplicationUsed = false,
                        isSelectable = true
                    )
                )
            ),
            viewModel.uiState.value.tileOperandSelectionDialog
        )
    }

    @Test
    fun tapping_a_filled_right_tile_operand_reopens_the_selection_dialog_with_the_current_operand_still_available() {
        val viewModel = GameViewModel()

        viewModel.onTileRightOperandTapped(index = 0)
        viewModel.onTileOperandSelectionConfirmed(stripEntryId = 2)
        viewModel.onTileRightOperandTapped(index = 0)

        assertEquals(
            TileOperandSelectionDialogUiState(
                tileIndex = 0,
                slot = OperandSlot.RIGHT,
                availableOperands = listOf(
                    TileOperandOptionUiState(
                        2,
                        6,
                        additionUsed = false,
                        multiplicationUsed = false,
                        isSelectable = true
                    ),
                    TileOperandOptionUiState(
                        4,
                        25,
                        additionUsed = false,
                        multiplicationUsed = false,
                        isSelectable = true
                    ),
                    TileOperandOptionUiState(
                        7,
                        222,
                        additionUsed = false,
                        multiplicationUsed = false,
                        isSelectable = true
                    )
                )
            ),
            viewModel.uiState.value.tileOperandSelectionDialog
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
    fun partially_filled_tiles_are_not_marked_invalid_in_ui_state() {
        val viewModel = GameViewModel()

        viewModel.onTileLeftOperandTapped(index = 0)
        viewModel.onTileOperandSelectionConfirmed(stripEntryId = 2)

        assertFalse(viewModel.uiState.value.tiles.first().isInvalid)
    }

    @Test
    fun selecting_the_opposite_operand_slot_surfaces_the_current_tile_entry_as_unavailable() {
        val viewModel = GameViewModel()

        viewModel.onTileLeftOperandTapped(index = 0)
        viewModel.onTileOperandSelectionConfirmed(stripEntryId = 2)
        viewModel.onTileRightOperandTapped(index = 0)

        assertEquals(
            TileOperandSelectionDialogUiState(
                tileIndex = 0,
                slot = OperandSlot.RIGHT,
                availableOperands = listOf(
                    TileOperandOptionUiState(
                        2,
                        6,
                        additionUsed = false,
                        multiplicationUsed = false,
                        isSelectable = false
                    ),
                    TileOperandOptionUiState(
                        4,
                        25,
                        additionUsed = false,
                        multiplicationUsed = false,
                        isSelectable = true
                    ),
                    TileOperandOptionUiState(
                        7,
                        222,
                        additionUsed = false,
                        multiplicationUsed = false,
                        isSelectable = true
                    )
                )
            ),
            viewModel.uiState.value.tileOperandSelectionDialog
        )
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
    fun reopening_a_slot_keeps_its_current_operand_selectable_while_blocking_the_opposite_slot_entry() {
        val viewModel = GameViewModel()

        viewModel.onTileLeftOperandTapped(index = 0)
        viewModel.onTileOperandSelectionConfirmed(stripEntryId = 2)
        viewModel.onTileRightOperandTapped(index = 0)
        viewModel.onTileOperandSelectionConfirmed(stripEntryId = 4)
        viewModel.onTileLeftOperandTapped(index = 0)

        assertEquals(
            TileOperandSelectionDialogUiState(
                tileIndex = 0,
                slot = OperandSlot.LEFT,
                availableOperands = listOf(
                    TileOperandOptionUiState(
                        2,
                        6,
                        additionUsed = false,
                        multiplicationUsed = false,
                        isSelectable = true
                    ),
                    TileOperandOptionUiState(
                        4,
                        25,
                        additionUsed = false,
                        multiplicationUsed = false,
                        isSelectable = false
                    ),
                    TileOperandOptionUiState(
                        7,
                        222,
                        additionUsed = false,
                        multiplicationUsed = false,
                        isSelectable = true
                    )
                )
            ),
            viewModel.uiState.value.tileOperandSelectionDialog
        )
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
    fun cancelling_the_selection_dialog_leaves_a_filled_tile_operand_unchanged() {
        val viewModel = GameViewModel()

        viewModel.onTileLeftOperandTapped(index = 0)
        viewModel.onTileOperandSelectionConfirmed(stripEntryId = 2)
        viewModel.onTileLeftOperandTapped(index = 0)
        viewModel.onTileOperandSelectionDismissed()

        val uiState = viewModel.uiState.value

        assertEquals(TileUiState("6", "?", "?", "223", canReset = true), uiState.tiles.first())
        assertNull(uiState.tileOperandSelectionDialog)
    }

    @Test
    fun cancelling_the_selection_dialog_leaves_the_hidden_tile_operand_unchanged() {
        val viewModel = GameViewModel()

        viewModel.onTileLeftOperandTapped(index = 0)
        viewModel.onTileOperandSelectionDismissed()

        val uiState = viewModel.uiState.value

        assertEquals(TileUiState("?", "?", "?", "223"), uiState.tiles.first())
        assertNull(uiState.tileOperandSelectionDialog)
    }

    @Test
    fun repeated_numeric_values_remain_distinct_operand_options_with_separate_usage_hints() {
        val viewModel = GameViewModel(
            initialPuzzle = puzzleWithRepeatedSixes()
        )

        viewModel.onTileLeftOperandTapped(index = 0)
        viewModel.onTileOperandSelectionConfirmed(stripEntryId = 0)
        viewModel.onTileOperatorTapped(index = 0)
        viewModel.onTileOperatorSelectionConfirmed(operator = Operator.ADDITION)
        viewModel.onTileLeftOperandTapped(index = 1)

        assertEquals(
            TileOperandSelectionDialogUiState(
                tileIndex = 1,
                slot = OperandSlot.LEFT,
                availableOperands = listOf(
                    TileOperandOptionUiState(
                        0,
                        6,
                        additionUsed = true,
                        multiplicationUsed = false,
                        isSelectable = true
                    ),
                    TileOperandOptionUiState(
                        1,
                        6,
                        additionUsed = false,
                        multiplicationUsed = false,
                        isSelectable = true
                    ),
                    TileOperandOptionUiState(
                        4,
                        25,
                        additionUsed = false,
                        multiplicationUsed = false,
                        isSelectable = true
                    ),
                    TileOperandOptionUiState(
                        7,
                        222,
                        additionUsed = false,
                        multiplicationUsed = false,
                        isSelectable = true
                    )
                )
            ),
            viewModel.uiState.value.tileOperandSelectionDialog
        )
    }

    @Test
    fun exhausted_operand_options_are_exposed_as_unavailable_in_ui_state() {
        val viewModel = GameViewModel()

        assignEntryTwoToLeftOperand(index = 0, operator = Operator.ADDITION, viewModel = viewModel)
        assignEntryTwoToLeftOperand(index = 1, operator = Operator.MULTIPLICATION, viewModel = viewModel)
        viewModel.onTileLeftOperandTapped(index = 2)

        assertEquals(
            TileOperandSelectionDialogUiState(
                tileIndex = 2,
                slot = OperandSlot.LEFT,
                availableOperands = listOf(
                    TileOperandOptionUiState(
                        2,
                        6,
                        additionUsed = true,
                        multiplicationUsed = true,
                        isSelectable = false
                    ),
                    TileOperandOptionUiState(
                        4,
                        25,
                        additionUsed = false,
                        multiplicationUsed = false,
                        isSelectable = true
                    ),
                    TileOperandOptionUiState(
                        7,
                        222,
                        additionUsed = false,
                        multiplicationUsed = false,
                        isSelectable = true
                    )
                )
            ),
            viewModel.uiState.value.tileOperandSelectionDialog
        )
    }

    @Test
    fun provisionally_exhausted_operand_options_are_exposed_as_unavailable_in_ui_state() {
        val viewModel = GameViewModel()

        viewModel.onTileLeftOperandTapped(index = 0)
        viewModel.onTileOperandSelectionConfirmed(stripEntryId = 2)
        viewModel.onTileLeftOperandTapped(index = 1)
        viewModel.onTileOperandSelectionConfirmed(stripEntryId = 2)
        viewModel.onTileLeftOperandTapped(index = 2)

        assertEquals(
            TileOperandSelectionDialogUiState(
                tileIndex = 2,
                slot = OperandSlot.LEFT,
                availableOperands = listOf(
                    TileOperandOptionUiState(
                        2,
                        6,
                        additionUsed = false,
                        multiplicationUsed = false,
                        isSelectable = false
                    ),
                    TileOperandOptionUiState(
                        4,
                        25,
                        additionUsed = false,
                        multiplicationUsed = false,
                        isSelectable = true
                    ),
                    TileOperandOptionUiState(
                        7,
                        222,
                        additionUsed = false,
                        multiplicationUsed = false,
                        isSelectable = true
                    )
                )
            ),
            viewModel.uiState.value.tileOperandSelectionDialog
        )
    }

    @Test
    fun reopening_a_slot_keeps_its_current_exhausted_operand_selectable_for_that_same_slot() {
        val viewModel = GameViewModel()

        assignEntryTwoToLeftOperand(index = 0, operator = Operator.ADDITION, viewModel = viewModel)
        assignEntryTwoToLeftOperand(index = 1, operator = Operator.MULTIPLICATION, viewModel = viewModel)
        viewModel.onTileLeftOperandTapped(index = 0)

        assertEquals(
            TileOperandSelectionDialogUiState(
                tileIndex = 0,
                slot = OperandSlot.LEFT,
                availableOperands = listOf(
                    TileOperandOptionUiState(
                        2,
                        6,
                        additionUsed = false,
                        multiplicationUsed = true,
                        isSelectable = true
                    ),
                    TileOperandOptionUiState(
                        4,
                        25,
                        additionUsed = false,
                        multiplicationUsed = false,
                        isSelectable = true
                    ),
                    TileOperandOptionUiState(
                        7,
                        222,
                        additionUsed = false,
                        multiplicationUsed = false,
                        isSelectable = true
                    )
                )
            ),
            viewModel.uiState.value.tileOperandSelectionDialog
        )
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
}

private fun assignEntryTwoToLeftOperand(index: Int, operator: Operator, viewModel: GameViewModel) {
    viewModel.onTileLeftOperandTapped(index = index)
    viewModel.onTileOperandSelectionConfirmed(stripEntryId = 2)
    viewModel.onTileOperatorTapped(index = index)
    viewModel.onTileOperatorSelectionConfirmed(operator = operator)
}
