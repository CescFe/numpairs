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
    fun confirming_adjacent_hidden_strip_entries_reorders_player_entered_values_to_keep_the_strip_ascending() {
        val viewModel = GameViewModel()

        viewModel.onStripItemTapped(index = 0)
        viewModel.onStripItemEntryConfirmed(value = 5)
        viewModel.onStripItemTapped(index = 1)
        viewModel.onStripItemEntryConfirmed(value = 2)

        assertEquals(
            listOf("2", "5", "6", "?", "25", "?", "?", "222"),
            viewModel.uiState.value.stripItems.map { it.label }
        )
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
    fun editing_adjacent_player_entered_strip_items_reorders_them_to_keep_the_strip_ascending() {
        val viewModel = GameViewModel()

        viewModel.onStripItemTapped(index = 0)
        viewModel.onStripItemEntryConfirmed(value = 2)
        viewModel.onStripItemTapped(index = 1)
        viewModel.onStripItemEntryConfirmed(value = 5)
        viewModel.onStripItemTapped(index = 1)
        viewModel.onStripItemEntryConfirmed(value = 1)

        assertEquals(
            listOf("1", "2", "6", "?", "25", "?", "?", "222"),
            viewModel.uiState.value.stripItems.map { it.label }
        )
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
