package org.cescfe.numpairs.feature.game.presentation

import org.cescfe.numpairs.domain.puzzle.PuzzleCompletionState
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class GameViewModelCompletionAndResetTest {
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
        assertTrue(uiState.tiles.none { tile -> tile.isPairingMismatchHighlighted })
        assertNull(uiState.puzzleOutcome)
        assertFalse(uiState.isSuccessOverlayVisible)
        assertNull(uiState.stripItemEntryDialog)
        assertNull(uiState.tileOperatorSelectionDialog)
        assertNull(uiState.tileOperandSelectionDialog)
    }

    @Test
    fun partially_filled_tiles_expose_the_reset_action_in_ui_state() {
        val viewModel = GameViewModel()

        assertFalse(viewModel.uiState.value.tiles.first().canReset)

        viewModel.onTileLeftOperandTapped(index = 0)
        viewModel.onTileOperandSelectionConfirmed(stripEntryId = 2)

        assertTrue(viewModel.uiState.value.tiles.first().canReset)
    }

    @Test
    fun resetting_a_partially_filled_tile_restores_its_initial_state_without_touching_strip_or_other_tiles() {
        val viewModel = GameViewModel()

        viewModel.onTileLeftOperandTapped(index = 0)
        viewModel.onTileOperandSelectionConfirmed(stripEntryId = 2)
        viewModel.onTileOperatorTapped(index = 1)
        viewModel.onTileOperatorSelectionConfirmed(operator = org.cescfe.numpairs.domain.puzzle.Operator.ADDITION)

        val stripLabelsBeforeReset = viewModel.uiState.value.stripItems.map { it.label }
        val otherTileBeforeReset = viewModel.uiState.value.tiles[1]

        viewModel.onTileResetTapped(index = 0)

        val uiState = viewModel.uiState.value

        assertEquals(TileUiState("?", "?", "?", "223"), uiState.tiles.first())
        assertFalse(uiState.tiles.first().canReset)
        assertEquals(stripLabelsBeforeReset, uiState.stripItems.map { it.label })
        assertEquals(otherTileBeforeReset, uiState.tiles[1])
    }

    @Test
    fun resetting_an_incorrect_tile_clears_invalid_styling_and_restores_the_initial_expression() {
        val viewModel = GameViewModel()

        viewModel.onStripItemTapped(index = 1)
        viewModel.onStripItemEntryConfirmed(value = 1)
        viewModel.onTileLeftOperandTapped(index = 0)
        viewModel.onTileOperandSelectionConfirmed(stripEntryId = 1)
        viewModel.onTileOperatorTapped(index = 0)
        viewModel.onTileOperatorSelectionConfirmed(operator = org.cescfe.numpairs.domain.puzzle.Operator.MULTIPLICATION)
        viewModel.onTileRightOperandTapped(index = 0)
        viewModel.onTileOperandSelectionConfirmed(stripEntryId = 7)

        assertTrue(viewModel.uiState.value.tiles.first().isInvalid)

        viewModel.onTileResetTapped(index = 0)

        val uiState = viewModel.uiState.value

        assertEquals(TileUiState("?", "?", "?", "223"), uiState.tiles.first())
        assertFalse(uiState.tiles.first().isInvalid)
        assertFalse(uiState.tiles.first().canReset)
    }

    @Test
    fun resetting_a_correct_tile_clears_solved_feedback_when_needed() {
        val viewModel = GameViewModel(
            initialPuzzle = solvedPuzzleWithKnownStripAndAssignments()
        )

        assertEquals(PuzzleOutcomeUiState.Solved, viewModel.uiState.value.puzzleOutcome)
        assertTrue(viewModel.uiState.value.isSuccessOverlayVisible)

        viewModel.onSuccessOverlayDismissed()
        viewModel.onTileResetTapped(index = 0)

        val uiState = viewModel.uiState.value

        assertNull(uiState.puzzleOutcome)
        assertFalse(uiState.isSuccessOverlayVisible)
        assertEquals(TileUiState("?", "?", "?", "3"), uiState.tiles.first())
    }

    @Test
    fun solved_puzzle_emits_a_success_completion_outcome() {
        val viewModel = GameViewModel(
            initialPuzzle = solvedPuzzleWithKnownStripAndAssignments()
        )

        val uiState = viewModel.uiState.value

        assertEquals(PuzzleOutcomeUiState.Solved, uiState.puzzleOutcome)
        assertTrue(uiState.isSuccessOverlayVisible)
        assertTrue(uiState.tiles.none { tile -> tile.isPairingMismatchHighlighted })
    }

    @Test
    fun dismissing_the_success_overlay_hides_it_without_clearing_the_solved_outcome() {
        val viewModel = GameViewModel(
            initialPuzzle = solvedPuzzleWithKnownStripAndAssignments()
        )

        viewModel.onSuccessOverlayDismissed()

        assertEquals(PuzzleOutcomeUiState.Solved, viewModel.uiState.value.puzzleOutcome)
        assertFalse(viewModel.uiState.value.isSuccessOverlayVisible)
    }

    @Test
    fun completing_the_board_successfully_shows_the_success_overlay() {
        val viewModel = GameViewModel(
            initialPuzzle = incompletePuzzleOneOperatorSelectionAwayFromSolvedCompletion()
        )

        assertNull(viewModel.uiState.value.puzzleOutcome)
        assertFalse(viewModel.uiState.value.isSuccessOverlayVisible)

        viewModel.onTileOperatorTapped(index = 1)
        viewModel.onTileOperatorSelectionConfirmed(operator = org.cescfe.numpairs.domain.puzzle.Operator.MULTIPLICATION)

        assertEquals(PuzzleOutcomeUiState.Solved, viewModel.uiState.value.puzzleOutcome)
        assertTrue(viewModel.uiState.value.isSuccessOverlayVisible)
    }

    @Test
    fun completing_the_board_with_mismatched_pairings_emits_an_invalid_outcome_instead_of_success() {
        val viewModel = GameViewModel(
            initialPuzzle = incompletePuzzleOneOperatorSelectionAwayFromMismatchedCompletion()
        )

        assertNull(viewModel.uiState.value.puzzleOutcome)
        assertTrue(viewModel.uiState.value.tiles.none { tile -> tile.isPairingMismatchHighlighted })

        viewModel.onTileOperatorTapped(index = 1)
        viewModel.onTileOperatorSelectionConfirmed(operator = org.cescfe.numpairs.domain.puzzle.Operator.MULTIPLICATION)

        val uiState = viewModel.uiState.value
        val highlightedTileIndexes = uiState.tiles.mapIndexedNotNull { index, tile ->
            index.takeIf { tile.isPairingMismatchHighlighted }
        }

        assertEquals(
            PuzzleOutcomeUiState.Invalid(PuzzleCompletionState.MISMATCHED_SUM_PRODUCT_PAIRINGS),
            uiState.puzzleOutcome
        )
        assertEquals(listOf(0, 1, 2, 3), highlightedTileIndexes)
        assertFalse(uiState.tiles[0].isInvalid)
        assertFalse(uiState.isSuccessOverlayVisible)
        assertFalse(uiState.puzzleOutcome == PuzzleOutcomeUiState.Solved)
    }
}
