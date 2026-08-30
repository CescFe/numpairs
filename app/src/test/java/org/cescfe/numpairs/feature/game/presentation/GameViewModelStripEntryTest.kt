package org.cescfe.numpairs.feature.game.presentation

import org.cescfe.numpairs.data.puzzle.seed.samplePuzzle
import org.cescfe.numpairs.domain.puzzle.model.Expression
import org.cescfe.numpairs.domain.puzzle.model.Operator
import org.cescfe.numpairs.domain.puzzle.model.Strip
import org.cescfe.numpairs.domain.puzzle.model.StripEntryRange
import org.cescfe.numpairs.domain.puzzle.model.StripItem
import org.cescfe.numpairs.feature.game.presentation.support.enterStripValue
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class GameViewModelStripEntryTest {
    @Test
    fun tapping_a_hidden_strip_item_starts_inline_entry_with_an_empty_draft() {
        val viewModel = GameViewModel()

        viewModel.onStripItemTapped(index = 1)

        assertEquals(
            StripItemEntryInputUiState(
                stripItemIndex = 1,
                draftText = "",
                validRange = StripEntryRange(minimumValue = 1, maximumValue = 6),
                isInvalid = false
            ),
            viewModel.uiState.value.stripItemEntryInput
        )
    }

    @Test
    fun tapping_a_player_entered_strip_item_starts_inline_entry_with_the_current_value_as_draft() {
        val viewModel = GameViewModel()

        viewModel.enterStripValue(index = 1, value = "2")
        viewModel.onStripItemTapped(index = 1)

        assertEquals(
            StripItemEntryInputUiState(
                stripItemIndex = 1,
                draftText = "2",
                validRange = StripEntryRange(minimumValue = 1, maximumValue = 6),
                isInvalid = false
            ),
            viewModel.uiState.value.stripItemEntryInput
        )
    }

    @Test
    fun changing_the_inline_entry_draft_does_not_update_the_puzzle() {
        val viewModel = GameViewModel()

        viewModel.onStripItemTapped(index = 1)
        viewModel.onStripItemEntryInputChanged(draftText = "2")

        val uiState = viewModel.uiState.value

        assertEquals("?", uiState.stripItems[1].label)
        assertEquals(
            StripItemEntryInputUiState(
                stripItemIndex = 1,
                draftText = "2",
                validRange = StripEntryRange(minimumValue = 1, maximumValue = 6),
                isInvalid = false
            ),
            uiState.stripItemEntryInput
        )
    }

    @Test
    fun changing_the_inline_entry_draft_to_an_out_of_range_value_marks_the_input_invalid_immediately() {
        val viewModel = GameViewModel()

        viewModel.onStripItemTapped(index = 1)
        viewModel.onStripItemEntryInputChanged(draftText = "9")

        val uiState = viewModel.uiState.value

        assertEquals("?", uiState.stripItems[1].label)
        assertEquals(
            StripItemEntryInputUiState(
                stripItemIndex = 1,
                draftText = "9",
                validRange = StripEntryRange(minimumValue = 1, maximumValue = 6),
                isInvalid = true
            ),
            uiState.stripItemEntryInput
        )
    }

    @Test
    fun confirming_a_valid_inline_entry_completes_the_hidden_strip_item_and_exits_editing() {
        val viewModel = GameViewModel()

        viewModel.enterStripValue(index = 1, value = "2")

        val uiState = viewModel.uiState.value

        assertEquals("2", uiState.stripItems[1].label)
        assertEquals(true, uiState.stripItems[1].isEntryEnabled)
        assertEquals(StripItemVisualStyle.PLAYER_ENTERED, uiState.stripItems[1].visualStyle)
        assertNull(uiState.stripItemEntryInput)
    }

    @Test
    fun confirming_adjacent_hidden_strip_entries_surfaces_the_reordered_domain_result_in_ui_state() {
        val viewModel = GameViewModel()

        viewModel.enterStripValue(index = 0, value = "5")
        viewModel.enterStripValue(index = 1, value = "2")

        assertEquals(
            listOf("2", "5", "6", "?", "25", "?", "?", "222"),
            viewModel.uiState.value.stripItems.map { stripItem -> stripItem.label }
        )
        assertNull(viewModel.uiState.value.stripItemEntryInput)
    }

    @Test
    fun confirming_an_out_of_range_inline_entry_marks_the_input_invalid_and_preserves_hidden_item() {
        val viewModel = GameViewModel()

        viewModel.onStripItemTapped(index = 1)
        viewModel.onStripItemEntryInputChanged(draftText = "9")
        viewModel.onStripItemEntryInputConfirmed()

        val uiState = viewModel.uiState.value

        assertEquals("?", uiState.stripItems[1].label)
        assertEquals(
            StripItemEntryInputUiState(
                stripItemIndex = 1,
                draftText = "9",
                validRange = StripEntryRange(minimumValue = 1, maximumValue = 6),
                isInvalid = true
            ),
            uiState.stripItemEntryInput
        )
    }

    @Test
    fun losing_focus_with_a_valid_inline_entry_completes_the_hidden_strip_item_and_exits_editing() {
        val viewModel = GameViewModel()

        viewModel.onStripItemTapped(index = 1)
        viewModel.onStripItemEntryInputChanged(draftText = "2")
        viewModel.onStripItemEntryInputFocusLost(stripItemIndex = 1)

        val uiState = viewModel.uiState.value

        assertEquals("2", uiState.stripItems[1].label)
        assertNull(uiState.stripItemEntryInput)
    }

    @Test
    fun late_focus_loss_from_previous_inline_entry_does_not_resolve_active_entry() {
        val viewModel = GameViewModel()

        viewModel.onStripItemTapped(index = 1)
        viewModel.onStripItemEntryInputChanged(draftText = "2")
        viewModel.onStripItemTapped(index = 3)
        viewModel.onStripItemEntryInputFocusLost(stripItemIndex = 1)

        val uiState = viewModel.uiState.value

        assertEquals("2", uiState.stripItems[1].label)
        assertEquals(3, uiState.stripItemEntryInput?.stripItemIndex)
        assertEquals("", uiState.stripItemEntryInput?.draftText)
    }

    @Test
    fun losing_focus_with_an_invalid_inline_entry_keeps_the_input_active_and_preserves_the_item() {
        val viewModel = GameViewModel()

        viewModel.onStripItemTapped(index = 1)
        viewModel.onStripItemEntryInputChanged(draftText = "9")
        viewModel.onStripItemEntryInputFocusLost(stripItemIndex = 1)

        val uiState = viewModel.uiState.value

        assertEquals("?", uiState.stripItems[1].label)
        assertEquals(
            StripItemEntryInputUiState(
                stripItemIndex = 1,
                draftText = "9",
                validRange = StripEntryRange(minimumValue = 1, maximumValue = 6),
                isInvalid = true
            ),
            uiState.stripItemEntryInput
        )
    }

    @Test
    fun losing_focus_with_an_empty_inline_entry_draft_exits_editing_without_changing_the_item() {
        val viewModel = GameViewModel()

        viewModel.onStripItemTapped(index = 1)
        viewModel.onStripItemEntryInputFocusLost(stripItemIndex = 1)

        val uiState = viewModel.uiState.value

        assertEquals("?", uiState.stripItems[1].label)
        assertNull(uiState.stripItemEntryInput)
    }

    @Test
    fun updating_an_invalid_inline_entry_draft_clears_the_invalid_state_without_updating_the_puzzle() {
        val viewModel = GameViewModel()

        viewModel.onStripItemTapped(index = 1)
        viewModel.onStripItemEntryInputChanged(draftText = "9")
        viewModel.onStripItemEntryInputConfirmed()
        viewModel.onStripItemEntryInputChanged(draftText = "3")

        val uiState = viewModel.uiState.value

        assertEquals("?", uiState.stripItems[1].label)
        assertEquals(
            StripItemEntryInputUiState(
                stripItemIndex = 1,
                draftText = "3",
                validRange = StripEntryRange(minimumValue = 1, maximumValue = 6),
                isInvalid = false
            ),
            uiState.stripItemEntryInput
        )
    }

    @Test
    fun confirming_a_valid_inline_entry_updates_a_player_entered_strip_item_and_exits_editing() {
        val viewModel = GameViewModel()

        viewModel.enterStripValue(index = 1, value = "2")
        viewModel.enterStripValue(index = 1, value = "3")

        val uiState = viewModel.uiState.value

        assertEquals("3", uiState.stripItems[1].label)
        assertEquals(true, uiState.stripItems[1].isEntryEnabled)
        assertEquals(StripItemVisualStyle.PLAYER_ENTERED, uiState.stripItems[1].visualStyle)
        assertNull(uiState.stripItemEntryInput)
    }

    @Test
    fun confirming_an_empty_player_entered_draft_clears_the_item_and_exits_editing() {
        val viewModel = GameViewModel()
        viewModel.enterStripValue(index = 1, value = "2")
        viewModel.onStripItemTapped(index = 1)

        viewModel.onStripItemEntryInputChanged(draftText = "")
        viewModel.onStripItemEntryInputConfirmed()

        val uiState = viewModel.uiState.value

        assertEquals("?", uiState.stripItems[1].label)
        assertEquals(StripItemVisualStyle.HIDDEN, uiState.stripItems[1].visualStyle)
        assertEquals(StripItem.Hidden, viewModel.currentPuzzle.value.strip.items[1])
        assertNull(uiState.stripItemEntryInput)
    }

    @Test
    fun losing_focus_with_an_empty_player_entered_draft_clears_the_item() {
        val viewModel = GameViewModel()
        viewModel.enterStripValue(index = 1, value = "2")
        viewModel.onStripItemTapped(index = 1)
        viewModel.onStripItemEntryInputChanged(draftText = "")

        viewModel.onStripItemEntryInputFocusLost(stripItemIndex = 1)

        assertEquals(StripItem.Hidden, viewModel.currentPuzzle.value.strip.items[1])
        assertNull(viewModel.uiState.value.stripItemEntryInput)
    }

    @Test
    fun explicit_clear_discards_an_invalid_draft_and_clears_the_player_entered_item() {
        val viewModel = GameViewModel()
        viewModel.enterStripValue(index = 1, value = "2")
        viewModel.onStripItemTapped(index = 1)
        viewModel.onStripItemEntryInputChanged(draftText = "9")

        viewModel.onStripItemEntryInputCleared()

        assertEquals(StripItem.Hidden, viewModel.currentPuzzle.value.strip.items[1])
        assertNull(viewModel.uiState.value.stripItemEntryInput)
    }

    @Test
    fun explicit_clear_ignores_hidden_known_and_missing_active_inputs() {
        val viewModel = GameViewModel()

        viewModel.onStripItemEntryInputCleared()
        viewModel.onStripItemTapped(index = 1)
        viewModel.onStripItemEntryInputCleared()

        assertEquals(StripItem.Hidden, viewModel.currentPuzzle.value.strip.items[1])
        assertEquals(1, viewModel.uiState.value.stripItemEntryInput?.stripItemIndex)

        viewModel.onStripItemEntryInputCancelled()
        viewModel.onStripItemTapped(index = 2)
        viewModel.onStripItemEntryInputCleared()

        assertEquals(StripItem.Known(6), viewModel.currentPuzzle.value.strip.items[2])
        assertNull(viewModel.uiState.value.stripItemEntryInput)
    }

    @Test
    fun late_focus_loss_after_clearing_does_not_resolve_a_new_active_entry() {
        val viewModel = GameViewModel()
        viewModel.enterStripValue(index = 1, value = "2")
        viewModel.onStripItemTapped(index = 1)
        viewModel.onStripItemEntryInputCleared()
        viewModel.onStripItemTapped(index = 3)

        viewModel.onStripItemEntryInputFocusLost(stripItemIndex = 1)

        assertEquals(3, viewModel.uiState.value.stripItemEntryInput?.stripItemIndex)
        assertEquals("", viewModel.uiState.value.stripItemEntryInput?.draftText)
    }

    @Test
    fun editing_a_player_entered_strip_item_updates_assigned_operands_after_reordering() {
        val viewModel = GameViewModel()
        viewModel.enterStripValue(index = 0, value = "5")
        viewModel.enterStripValue(index = 1, value = "2")
        viewModel.onTileLeftOperandTapped(index = 0)
        viewModel.onTileOperandSelectionConfirmed(stripEntryId = 0)
        viewModel.onTileOperatorSelectionConfirmed(operator = Operator.ADDITION)
        viewModel.onTileOperandSelectionDismissed()
        viewModel.onTileRightOperandTapped(index = 1)
        viewModel.onTileOperandSelectionConfirmed(stripEntryId = 0)
        viewModel.onTileOperatorSelectionConfirmed(operator = Operator.MULTIPLICATION)
        viewModel.onTileOperandSelectionDismissed()

        viewModel.enterStripValue(index = 1, value = "1")

        val currentPuzzle = viewModel.currentPuzzle.value

        assertEquals(listOf(0, 1), currentPuzzle.strip.entries.take(2).map { entry -> entry.id })
        assertEquals(
            Expression.Operand.Known(value = 1, stripEntryId = 0),
            currentPuzzle.board.tiles[0].expression.leftOperand
        )
        assertEquals(
            Expression.Operand.Known(value = 1, stripEntryId = 0),
            currentPuzzle.board.tiles[1].expression.rightOperand
        )
        assertEquals("1", viewModel.uiState.value.tiles[0].leftOperandLabel)
        assertEquals("1", viewModel.uiState.value.tiles[1].rightOperandLabel)
    }

    @Test
    fun clearing_a_player_entered_item_hides_assigned_operands_and_releases_its_usage() {
        val viewModel = GameViewModel()
        viewModel.enterStripValue(index = 0, value = "5")
        viewModel.enterStripValue(index = 1, value = "2")
        viewModel.onTileLeftOperandTapped(index = 0)
        viewModel.onTileOperandSelectionConfirmed(stripEntryId = 0)
        viewModel.onTileOperatorSelectionConfirmed(operator = Operator.ADDITION)
        viewModel.onTileOperandSelectionDismissed()
        viewModel.onTileRightOperandTapped(index = 1)
        viewModel.onTileOperandSelectionConfirmed(stripEntryId = 0)
        viewModel.onTileOperatorSelectionConfirmed(operator = Operator.MULTIPLICATION)
        viewModel.onTileOperandSelectionDismissed()
        viewModel.onStripItemTapped(index = 1)

        viewModel.onStripItemEntryInputCleared()

        val currentPuzzle = viewModel.currentPuzzle.value
        val uiState = viewModel.uiState.value

        assertEquals(Expression.Operand.Hidden, currentPuzzle.board.tiles[0].expression.leftOperand)
        assertEquals(Expression.Operand.Hidden, currentPuzzle.board.tiles[1].expression.rightOperand)
        assertEquals("?", uiState.tiles[0].leftOperandLabel)
        assertEquals("?", uiState.tiles[1].rightOperandLabel)
        assertEquals(StripItemVisualStyle.HIDDEN, uiState.stripItems[1].visualStyle)
        assertEquals(false, uiState.stripItems[1].additionUsed)
        assertEquals(false, uiState.stripItems[1].multiplicationUsed)
    }

    @Test
    fun confirming_an_out_of_range_inline_entry_marks_the_input_invalid_and_preserves_player_entered_item() {
        val viewModel = GameViewModel()

        viewModel.enterStripValue(index = 1, value = "2")
        viewModel.onStripItemTapped(index = 1)
        viewModel.onStripItemEntryInputChanged(draftText = "9")
        viewModel.onStripItemEntryInputConfirmed()

        val uiState = viewModel.uiState.value

        assertEquals("2", uiState.stripItems[1].label)
        assertEquals(
            StripItemEntryInputUiState(
                stripItemIndex = 1,
                draftText = "9",
                validRange = StripEntryRange(minimumValue = 1, maximumValue = 6),
                isInvalid = true
            ),
            uiState.stripItemEntryInput
        )
    }

    @Test
    fun cancelling_inline_entry_leaves_hidden_and_player_entered_strip_items_unchanged() {
        val hiddenItemViewModel = GameViewModel()

        hiddenItemViewModel.onStripItemTapped(index = 1)
        hiddenItemViewModel.onStripItemEntryInputChanged(draftText = "2")
        hiddenItemViewModel.onStripItemEntryInputCancelled()

        var uiState = hiddenItemViewModel.uiState.value
        assertEquals("?", uiState.stripItems[1].label)
        assertEquals(true, uiState.stripItems[1].isEntryEnabled)
        assertNull(uiState.stripItemEntryInput)

        val playerEnteredViewModel = GameViewModel()

        playerEnteredViewModel.enterStripValue(index = 1, value = "2")
        playerEnteredViewModel.onStripItemTapped(index = 1)
        playerEnteredViewModel.onStripItemEntryInputChanged(draftText = "3")
        playerEnteredViewModel.onStripItemEntryInputCancelled()

        uiState = playerEnteredViewModel.uiState.value
        assertEquals("2", uiState.stripItems[1].label)
        assertEquals(true, uiState.stripItems[1].isEntryEnabled)
        assertNull(uiState.stripItemEntryInput)
    }

    @Test
    fun confirming_an_empty_inline_entry_draft_exits_editing_without_changing_the_item() {
        val viewModel = GameViewModel()

        viewModel.onStripItemTapped(index = 1)
        viewModel.onStripItemEntryInputConfirmed()

        val uiState = viewModel.uiState.value

        assertEquals("?", uiState.stripItems[1].label)
        assertNull(uiState.stripItemEntryInput)
    }

    @Test
    fun tapping_a_known_strip_item_does_not_start_inline_entry() {
        val viewModel = GameViewModel()

        viewModel.onStripItemTapped(index = 2)

        assertNull(viewModel.uiState.value.stripItemEntryInput)
    }

    @Test
    fun tapping_a_hidden_strip_item_without_a_known_value_on_the_left_uses_one_as_the_lower_bound() {
        val viewModel = GameViewModel(
            initialPuzzle = samplePuzzle.copy(
                strip = Strip.fromItems(
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
            StripItemEntryInputUiState(
                stripItemIndex = 0,
                draftText = "",
                validRange = StripEntryRange(minimumValue = 1, maximumValue = 3),
                isInvalid = false
            ),
            viewModel.uiState.value.stripItemEntryInput
        )
    }
}
