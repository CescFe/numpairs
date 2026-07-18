package org.cescfe.numpairs.feature.game.presentation

import org.cescfe.numpairs.data.puzzle.seed.samplePuzzle
import org.cescfe.numpairs.domain.puzzle.model.Expression
import org.cescfe.numpairs.domain.puzzle.model.Operator
import org.cescfe.numpairs.domain.puzzle.model.TileResolutionState
import org.cescfe.numpairs.feature.game.presentation.support.enterStripValue
import org.cescfe.numpairs.feature.game.presentation.support.incompletePuzzleOneOperatorSelectionAwayFromSolvedCompletion
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class GameViewModelTileAssignmentCommitTest {
    @Test
    fun `accepted operand change returns one commit after publishing the puzzle change`() {
        val viewModel = GameViewModel(initialPuzzle = samplePuzzle)
        viewModel.enterStripValue(index = 0, value = "1")

        viewModel.onTileLeftOperandTapped(index = 0)
        val commit = viewModel.onTileOperandSelectionConfirmed(stripEntryId = 0)

        assertEquals(
            Expression.Operand.Known(value = 1, stripEntryId = 0),
            viewModel.currentPuzzle.value.board.tiles[0].expression.leftOperand
        )
        assertEquals(TileAssignmentCommit(tileIndex = 0, madeTileCorrect = false), commit)
    }

    @Test
    fun `accepted operator change reports when that action makes its tile correct`() {
        val viewModel = GameViewModel(
            initialPuzzle = incompletePuzzleOneOperatorSelectionAwayFromSolvedCompletion()
        )

        viewModel.onTileOperatorTapped(index = 1)
        val commit = viewModel.onTileOperatorSelectionConfirmed(operator = Operator.MULTIPLICATION)

        assertEquals(
            TileResolutionState.CORRECT,
            viewModel.currentPuzzle.value.board.tiles[1].resolutionState
        )
        assertEquals(TileAssignmentCommit(tileIndex = 1, madeTileCorrect = true), commit)
    }

    @Test
    fun `invalid unavailable and unchanged selections return no commit`() {
        val viewModel = GameViewModel(
            initialPuzzle = incompletePuzzleOneOperatorSelectionAwayFromSolvedCompletion()
        )

        assertNull(viewModel.onTileOperatorSelectionConfirmed(operator = Operator.ADDITION))

        viewModel.onTileLeftOperandTapped(index = 1)
        assertNull(viewModel.onTileOperandSelectionConfirmed(stripEntryId = Int.MAX_VALUE))

        viewModel.onTileOperandSelectionDismissed()
        viewModel.onTileOperatorTapped(index = 0)
        assertNull(viewModel.onTileOperatorSelectionConfirmed(operator = Operator.ADDITION))
        assertNull(viewModel.uiState.value.tileOperatorSelectionDialog)
    }

    @Test
    fun `a tile can report a new correct transition after it is reset`() {
        val viewModel = GameViewModel(
            initialPuzzle = incompletePuzzleOneOperatorSelectionAwayFromSolvedCompletion()
        )
        viewModel.onTileOperatorTapped(index = 1)
        viewModel.onTileOperatorSelectionConfirmed(operator = Operator.MULTIPLICATION)
        viewModel.onSuccessOverlayDismissed()

        viewModel.onTileResetTapped(index = 1)
        viewModel.onTileLeftOperandTapped(index = 1)
        val leftOperandCommit = viewModel.onTileOperandSelectionConfirmed(stripEntryId = 1)
        viewModel.onTileOperatorTapped(index = 1)
        val operatorCommit = viewModel.onTileOperatorSelectionConfirmed(operator = Operator.MULTIPLICATION)
        viewModel.onTileRightOperandTapped(index = 1)
        val rightOperandCommit = viewModel.onTileOperandSelectionConfirmed(stripEntryId = 0)

        assertEquals(TileAssignmentCommit(tileIndex = 1, madeTileCorrect = false), leftOperandCommit)
        assertEquals(TileAssignmentCommit(tileIndex = 1, madeTileCorrect = false), operatorCommit)
        assertEquals(TileAssignmentCommit(tileIndex = 1, madeTileCorrect = true), rightOperandCommit)
    }
}
