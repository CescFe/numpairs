package org.cescfe.numpairs.ui.screen

import org.junit.Assert.assertEquals
import org.junit.Test

class GameViewModelTest {
    @Test
    fun exposes_the_initial_screen_state() {
        val uiState = GameViewModel().uiState.value

        assertEquals(listOf("1", "?", "3", "?", "25", "6", "?", "888"), uiState.stripItems.map { it.label })
        assertEquals(8, uiState.tiles.size)
        assertEquals(TileUiState("1", "+", "2", "3"), uiState.tiles.first())
    }
}
