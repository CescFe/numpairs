package org.cescfe.numpairs.feature.game

import org.cescfe.numpairs.domain.puzzle.OperandSlot
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class GameViewModelPresentationStateTest {
    @Test
    fun opening_an_operator_selector_closes_the_strip_entry_dialog() {
        val viewModel = GameViewModel()

        viewModel.onStripItemTapped(index = 1)
        viewModel.onTileOperatorTapped(index = 0)

        val uiState = viewModel.uiState.value

        assertNull(uiState.stripItemEntryDialog)
        assertEquals(0, uiState.tileOperatorSelectionDialog?.tileIndex)
    }

    @Test
    fun opening_an_operand_selector_closes_the_operator_selector() {
        val viewModel = GameViewModel()

        viewModel.onTileOperatorTapped(index = 0)
        viewModel.onTileRightOperandTapped(index = 1)

        val uiState = viewModel.uiState.value

        assertNull(uiState.tileOperatorSelectionDialog)
        assertEquals(1, uiState.tileOperandSelectionDialog?.tileIndex)
        assertEquals(OperandSlot.RIGHT, uiState.tileOperandSelectionDialog?.slot)
    }

    @Test
    fun solved_overlay_blocks_new_modals_until_it_is_dismissed() {
        val viewModel = GameViewModel(
            initialPuzzle = solvedPuzzleWithKnownStripAndAssignments()
        )

        viewModel.onTileOperatorTapped(index = 0)

        assertTrue(viewModel.uiState.value.isSuccessOverlayVisible)
        assertNull(viewModel.uiState.value.tileOperatorSelectionDialog)

        viewModel.onSuccessOverlayDismissed()
        viewModel.onTileOperatorTapped(index = 0)

        assertEquals(0, viewModel.uiState.value.tileOperatorSelectionDialog?.tileIndex)
    }
}
