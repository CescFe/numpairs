package org.cescfe.numpairs.feature.game.presentation

import org.cescfe.numpairs.domain.puzzle.model.Operator
import org.cescfe.numpairs.feature.game.presentation.support.enterStripValue
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class GameViewModelOperatorAndTileStateTest {
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
    fun confirming_the_selection_dialog_completes_the_hidden_tile_operator() {
        val viewModel = GameViewModel()

        viewModel.onTileOperatorTapped(index = 0)
        viewModel.onTileOperatorSelectionConfirmed(operator = Operator.ADDITION)

        val uiState = viewModel.uiState.value

        assertEquals(TileUiState("?", "+", "?", "223", canReset = true), uiState.tiles.first())
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

        assertEquals(TileUiState("?", "×", "?", "223", canReset = true), uiState.tiles.first())
        assertNull(uiState.tileOperatorSelectionDialog)
    }

    @Test
    fun completing_a_tile_with_an_incorrect_expression_marks_it_invalid_without_crashing() {
        val viewModel = GameViewModel()

        viewModel.enterStripValue(index = 1, value = "1")
        viewModel.onTileLeftOperandTapped(index = 0)
        viewModel.onTileOperandSelectionConfirmed(stripEntryId = 1)
        viewModel.onTileOperatorTapped(index = 0)
        viewModel.onTileOperatorSelectionConfirmed(operator = Operator.MULTIPLICATION)
        viewModel.onTileRightOperandTapped(index = 0)
        viewModel.onTileOperandSelectionConfirmed(stripEntryId = 7)

        val uiState = viewModel.uiState.value

        assertEquals(
            TileUiState("1", "×", "222", "223", visualState = TileVisualState.INCORRECT, canReset = true),
            uiState.tiles.first()
        )
        assertTrue(uiState.tiles.first().isInvalid)
        assertNull(uiState.tileOperandSelectionDialog)
        assertNull(uiState.tileOperatorSelectionDialog)
    }

    @Test
    fun correcting_an_incorrect_tile_clears_its_invalid_ui_state() {
        val viewModel = GameViewModel()

        viewModel.enterStripValue(index = 1, value = "1")
        viewModel.onTileLeftOperandTapped(index = 0)
        viewModel.onTileOperandSelectionConfirmed(stripEntryId = 1)
        viewModel.onTileOperatorTapped(index = 0)
        viewModel.onTileOperatorSelectionConfirmed(operator = Operator.MULTIPLICATION)
        viewModel.onTileRightOperandTapped(index = 0)
        viewModel.onTileOperandSelectionConfirmed(stripEntryId = 7)
        viewModel.onTileOperatorTapped(index = 0)
        viewModel.onTileOperatorSelectionConfirmed(operator = Operator.ADDITION)

        assertFalse(viewModel.uiState.value.tiles.first().isInvalid)
    }
}
