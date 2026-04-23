package org.cescfe.numpairs.ui.screen

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class GameViewModelTest {
    @Test
    fun exposes_the_initial_screen_state() {
        val uiState = GameViewModel().uiState.value

        assertEquals(listOf("1", "?", "3", "?", "25", "6", "?", "888"), uiState.stripItems.map { it.label })
        assertEquals(8, uiState.tiles.size)
        assertEquals(TileUiState("1", "+", "2", "3"), uiState.tiles.first())
        assertNull(uiState.stripItemEntryDialog)
    }

    @Test
    fun tapping_a_hidden_strip_item_opens_the_entry_dialog() {
        val viewModel = GameViewModel()

        viewModel.onStripItemTapped(index = 1)

        assertEquals(StripItemEntryDialogUiState(stripItemIndex = 1), viewModel.uiState.value.stripItemEntryDialog)
    }

    @Test
    fun confirming_the_entry_dialog_completes_the_hidden_strip_item() {
        val viewModel = GameViewModel()

        viewModel.onStripItemTapped(index = 1)
        viewModel.onStripItemEntryConfirmed(value = 9)

        val uiState = viewModel.uiState.value

        assertEquals("9", uiState.stripItems[1].label)
        assertEquals(false, uiState.stripItems[1].isEntryEnabled)
        assertNull(uiState.stripItemEntryDialog)
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

        viewModel.onStripItemTapped(index = 0)

        assertNull(viewModel.uiState.value.stripItemEntryDialog)
    }
}
