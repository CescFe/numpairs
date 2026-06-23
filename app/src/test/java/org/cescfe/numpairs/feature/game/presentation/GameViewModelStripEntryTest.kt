package org.cescfe.numpairs.feature.game.presentation

import org.cescfe.numpairs.data.puzzle.seed.initialPuzzle
import org.cescfe.numpairs.domain.puzzle.Strip
import org.cescfe.numpairs.domain.puzzle.StripEntryRange
import org.cescfe.numpairs.domain.puzzle.StripItem
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
        viewModel.onStripItemEntryInputFocusLost()

        val uiState = viewModel.uiState.value

        assertEquals("2", uiState.stripItems[1].label)
        assertNull(uiState.stripItemEntryInput)
    }

    @Test
    fun losing_focus_with_an_invalid_inline_entry_keeps_the_input_active_and_preserves_the_item() {
        val viewModel = GameViewModel()

        viewModel.onStripItemTapped(index = 1)
        viewModel.onStripItemEntryInputChanged(draftText = "9")
        viewModel.onStripItemEntryInputFocusLost()

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
        viewModel.onStripItemEntryInputFocusLost()

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
            initialPuzzle = initialPuzzle.copy(
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
