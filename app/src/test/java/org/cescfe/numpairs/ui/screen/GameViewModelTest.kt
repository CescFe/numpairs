package org.cescfe.numpairs.ui.screen

import org.cescfe.numpairs.domain.puzzle.Operator
import org.cescfe.numpairs.domain.puzzle.PuzzleSamples
import org.cescfe.numpairs.domain.puzzle.Strip
import org.cescfe.numpairs.domain.puzzle.StripEntryRange
import org.cescfe.numpairs.domain.puzzle.StripItem
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class GameViewModelTest {
    @Test
    fun exposes_the_initial_screen_state() {
        val uiState = GameViewModel().uiState.value

        assertEquals(listOf("?", "?", "6", "?", "25", "?", "?", "222"), uiState.stripItems.map { it.label })
        assertEquals(
            listOf(
                StripItemVisualStyle.HIDDEN,
                StripItemVisualStyle.HIDDEN,
                StripItemVisualStyle.KNOWN,
                StripItemVisualStyle.HIDDEN,
                StripItemVisualStyle.KNOWN,
                StripItemVisualStyle.HIDDEN,
                StripItemVisualStyle.HIDDEN,
                StripItemVisualStyle.KNOWN
            ),
            uiState.stripItems.map { it.visualStyle }
        )
        assertEquals(8, uiState.tiles.size)
        assertEquals(TileUiState("?", "?", "?", "223"), uiState.tiles.first())
        assertNull(uiState.stripItemEntryDialog)
        assertNull(uiState.tileOperatorSelectionDialog)
        assertNull(uiState.tileOperandSelectionDialog)
    }

    @Test
    fun tapping_a_hidden_strip_item_opens_the_entry_dialog() {
        val viewModel = GameViewModel()

        viewModel.onStripItemTapped(index = 1)

        assertEquals(
            StripItemEntryDialogUiState(
                stripItemIndex = 1,
                validRange = StripEntryRange(minimumValue = 1, maximumValue = 6),
                mode = StripItemEntryDialogMode.CREATE,
                initialValue = ""
            ),
            viewModel.uiState.value.stripItemEntryDialog
        )
    }

    @Test
    fun tapping_a_hidden_tile_operator_opens_the_selection_dialog() {
        val viewModel = GameViewModel()

        viewModel.onTileOperatorTapped(index = 0)

        assertEquals(
            TileOperatorSelectionDialogUiState(
                tileIndex = 0,
                availableOperators = listOf(
                    Operator.ADDITION,
                    Operator.MULTIPLICATION
                ),
                initialOperator = null
            ),
            viewModel.uiState.value.tileOperatorSelectionDialog
        )
    }

    @Test
    fun tapping_a_hidden_left_tile_operand_opens_the_selection_dialog() {
        val viewModel = GameViewModel()

        viewModel.onTileLeftOperandTapped(index = 0)

        assertEquals(
            TileOperandSelectionDialogUiState(
                tileIndex = 0,
                slot = TileOperandSlot.LEFT,
                availableOperands = listOf(6, 25, 222),
                initialOperand = null
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
                slot = TileOperandSlot.RIGHT,
                availableOperands = listOf(6, 25, 222),
                initialOperand = null
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
                slot = TileOperandSlot.LEFT,
                availableOperands = listOf(2, 6, 25, 222),
                initialOperand = null
            ),
            viewModel.uiState.value.tileOperandSelectionDialog
        )
    }

    @Test
    fun tapping_a_filled_left_tile_operand_reopens_the_selection_dialog_with_the_current_operand() {
        val viewModel = GameViewModel()

        viewModel.onTileLeftOperandTapped(index = 0)
        viewModel.onTileOperandSelectionConfirmed(value = 6)
        viewModel.onTileLeftOperandTapped(index = 0)

        assertEquals(
            TileOperandSelectionDialogUiState(
                tileIndex = 0,
                slot = TileOperandSlot.LEFT,
                availableOperands = listOf(6, 25, 222),
                initialOperand = 6
            ),
            viewModel.uiState.value.tileOperandSelectionDialog
        )
    }

    @Test
    fun tapping_a_filled_right_tile_operand_reopens_the_selection_dialog_with_the_current_operand() {
        val viewModel = GameViewModel()

        viewModel.onTileRightOperandTapped(index = 0)
        viewModel.onTileOperandSelectionConfirmed(value = 6)
        viewModel.onTileRightOperandTapped(index = 0)

        assertEquals(
            TileOperandSelectionDialogUiState(
                tileIndex = 0,
                slot = TileOperandSlot.RIGHT,
                availableOperands = listOf(6, 25, 222),
                initialOperand = 6
            ),
            viewModel.uiState.value.tileOperandSelectionDialog
        )
    }

    @Test
    fun confirming_the_selection_dialog_completes_the_hidden_left_tile_operand() {
        val viewModel = GameViewModel()

        viewModel.onTileLeftOperandTapped(index = 0)
        viewModel.onTileOperandSelectionConfirmed(value = 6)

        val uiState = viewModel.uiState.value

        assertEquals(TileUiState("6", "?", "?", "223"), uiState.tiles.first())
        assertNull(uiState.tileOperandSelectionDialog)
    }

    @Test
    fun confirming_the_selection_dialog_replaces_a_filled_left_tile_operand() {
        val viewModel = GameViewModel()

        viewModel.onTileLeftOperandTapped(index = 0)
        viewModel.onTileOperandSelectionConfirmed(value = 6)
        viewModel.onTileLeftOperandTapped(index = 0)
        viewModel.onTileOperandSelectionConfirmed(value = 25)

        val uiState = viewModel.uiState.value

        assertEquals(TileUiState("25", "?", "?", "223"), uiState.tiles.first())
        assertNull(uiState.tileOperandSelectionDialog)
    }

    @Test
    fun cancelling_the_selection_dialog_leaves_a_filled_tile_operand_unchanged() {
        val viewModel = GameViewModel()

        viewModel.onTileLeftOperandTapped(index = 0)
        viewModel.onTileOperandSelectionConfirmed(value = 6)
        viewModel.onTileLeftOperandTapped(index = 0)
        viewModel.onTileOperandSelectionDismissed()

        val uiState = viewModel.uiState.value

        assertEquals(TileUiState("6", "?", "?", "223"), uiState.tiles.first())
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
    fun confirming_the_selection_dialog_completes_the_hidden_tile_operator() {
        val viewModel = GameViewModel()

        viewModel.onTileOperatorTapped(index = 0)
        viewModel.onTileOperatorSelectionConfirmed(operator = Operator.ADDITION)

        val uiState = viewModel.uiState.value

        assertEquals(TileUiState("?", "+", "?", "223"), uiState.tiles.first())
        assertNull(uiState.tileOperatorSelectionDialog)
    }

    @Test
    fun cancelling_the_selection_dialog_leaves_the_hidden_tile_operator_unchanged() {
        val viewModel = GameViewModel()

        viewModel.onTileOperatorTapped(index = 0)
        viewModel.onTileOperatorSelectionDismissed()

        val uiState = viewModel.uiState.value

        assertEquals(TileUiState("?", "?", "?", "223"), uiState.tiles.first())
        assertNull(uiState.tileOperatorSelectionDialog)
    }

    @Test
    fun tapping_a_filled_tile_operator_reopens_the_selection_dialog_with_the_current_operator() {
        val viewModel = GameViewModel()

        viewModel.onTileOperatorTapped(index = 0)
        viewModel.onTileOperatorSelectionConfirmed(operator = Operator.MULTIPLICATION)
        viewModel.onTileOperatorTapped(index = 0)

        assertEquals(
            TileOperatorSelectionDialogUiState(
                tileIndex = 0,
                availableOperators = listOf(
                    Operator.ADDITION,
                    Operator.MULTIPLICATION
                ),
                initialOperator = Operator.MULTIPLICATION
            ),
            viewModel.uiState.value.tileOperatorSelectionDialog
        )
    }

    @Test
    fun confirming_the_selection_dialog_reassigns_a_filled_tile_operator() {
        val viewModel = GameViewModel()

        viewModel.onTileOperatorTapped(index = 0)
        viewModel.onTileOperatorSelectionConfirmed(operator = Operator.ADDITION)
        viewModel.onTileOperatorTapped(index = 0)
        viewModel.onTileOperatorSelectionConfirmed(operator = Operator.MULTIPLICATION)

        val uiState = viewModel.uiState.value

        assertEquals(TileUiState("?", "×", "?", "223"), uiState.tiles.first())
        assertNull(uiState.tileOperatorSelectionDialog)
    }

    @Test
    fun confirming_the_entry_dialog_completes_the_hidden_strip_item() {
        val viewModel = GameViewModel()

        viewModel.onStripItemTapped(index = 1)
        viewModel.onStripItemEntryConfirmed(value = 2)

        val uiState = viewModel.uiState.value

        assertEquals("2", uiState.stripItems[1].label)
        assertEquals(true, uiState.stripItems[1].isEntryEnabled)
        assertEquals(StripItemVisualStyle.PLAYER_ENTERED, uiState.stripItems[1].visualStyle)
        assertNull(uiState.stripItemEntryDialog)
    }

    @Test
    fun confirming_an_out_of_range_value_keeps_the_dialog_open_and_does_not_change_the_hidden_strip_item() {
        val viewModel = GameViewModel()

        viewModel.onStripItemTapped(index = 1)
        viewModel.onStripItemEntryConfirmed(value = 9)

        val uiState = viewModel.uiState.value

        assertEquals("?", uiState.stripItems[1].label)
        assertEquals(
            StripItemEntryDialogUiState(
                stripItemIndex = 1,
                validRange = StripEntryRange(minimumValue = 1, maximumValue = 6),
                mode = StripItemEntryDialogMode.CREATE,
                initialValue = ""
            ),
            uiState.stripItemEntryDialog
        )
    }

    @Test
    fun tapping_a_player_entered_strip_item_opens_the_entry_dialog_in_edit_mode() {
        val viewModel = GameViewModel()

        viewModel.onStripItemTapped(index = 1)
        viewModel.onStripItemEntryConfirmed(value = 2)
        viewModel.onStripItemTapped(index = 1)

        assertEquals(
            StripItemEntryDialogUiState(
                stripItemIndex = 1,
                validRange = StripEntryRange(minimumValue = 1, maximumValue = 6),
                mode = StripItemEntryDialogMode.EDIT,
                initialValue = "2"
            ),
            viewModel.uiState.value.stripItemEntryDialog
        )
    }

    @Test
    fun confirming_the_entry_dialog_updates_a_player_entered_strip_item() {
        val viewModel = GameViewModel()

        viewModel.onStripItemTapped(index = 1)
        viewModel.onStripItemEntryConfirmed(value = 2)
        viewModel.onStripItemTapped(index = 1)
        viewModel.onStripItemEntryConfirmed(value = 3)

        val uiState = viewModel.uiState.value

        assertEquals("3", uiState.stripItems[1].label)
        assertEquals(true, uiState.stripItems[1].isEntryEnabled)
        assertEquals(StripItemVisualStyle.PLAYER_ENTERED, uiState.stripItems[1].visualStyle)
        assertNull(uiState.stripItemEntryDialog)
    }

    @Test
    fun cancelling_the_entry_dialog_leaves_a_player_entered_strip_item_unchanged() {
        val viewModel = GameViewModel()

        viewModel.onStripItemTapped(index = 1)
        viewModel.onStripItemEntryConfirmed(value = 2)
        viewModel.onStripItemTapped(index = 1)
        viewModel.onStripItemEntryDismissed()

        val uiState = viewModel.uiState.value

        assertEquals("2", uiState.stripItems[1].label)
        assertEquals(true, uiState.stripItems[1].isEntryEnabled)
        assertNull(uiState.stripItemEntryDialog)
    }

    @Test
    fun confirming_an_out_of_range_value_keeps_the_dialog_open_and_does_not_change_the_player_entered_strip_item() {
        val viewModel = GameViewModel()

        viewModel.onStripItemTapped(index = 1)
        viewModel.onStripItemEntryConfirmed(value = 2)
        viewModel.onStripItemTapped(index = 1)
        viewModel.onStripItemEntryConfirmed(value = 9)

        val uiState = viewModel.uiState.value

        assertEquals("2", uiState.stripItems[1].label)
        assertEquals(
            StripItemEntryDialogUiState(
                stripItemIndex = 1,
                validRange = StripEntryRange(minimumValue = 1, maximumValue = 6),
                mode = StripItemEntryDialogMode.EDIT,
                initialValue = "2"
            ),
            uiState.stripItemEntryDialog
        )
    }

    @Test
    fun cancelling_the_entry_dialog_leaves_the_hidden_strip_item_unchanged() {
        val viewModel = GameViewModel()

        viewModel.onStripItemTapped(index = 1)
        viewModel.onStripItemEntryDismissed()

        val uiState = viewModel.uiState.value

        assertEquals("?", uiState.stripItems[1].label)
        assertEquals(true, uiState.stripItems[1].isEntryEnabled)
        assertNull(uiState.stripItemEntryDialog)
    }

    @Test
    fun tapping_a_known_strip_item_does_not_open_the_entry_dialog() {
        val viewModel = GameViewModel()

        viewModel.onStripItemTapped(index = 2)

        assertNull(viewModel.uiState.value.stripItemEntryDialog)
    }

    @Test
    fun tapping_a_hidden_strip_item_without_a_known_value_on_the_left_uses_one_as_the_lower_bound() {
        val viewModel = GameViewModel(
            initialPuzzle = PuzzleSamples.prototype.copy(
                strip = Strip(
                    items = listOf(
                        StripItem.Hidden,
                        StripItem.Hidden,
                        StripItem.Known(3),
                        StripItem.Hidden,
                        StripItem.Known(5),
                        StripItem.Known(6),
                        StripItem.Hidden,
                        StripItem.Known(7)
                    )
                )
            )
        )

        viewModel.onStripItemTapped(index = 0)

        assertEquals(
            StripItemEntryDialogUiState(
                stripItemIndex = 0,
                validRange = StripEntryRange(minimumValue = 1, maximumValue = 3),
                mode = StripItemEntryDialogMode.CREATE,
                initialValue = ""
            ),
            viewModel.uiState.value.stripItemEntryDialog
        )
    }
}
