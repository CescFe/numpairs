package org.cescfe.numpairs.feature.game.presentation

import org.cescfe.numpairs.data.puzzle.seed.samplePuzzle
import org.cescfe.numpairs.domain.puzzle.model.Expression
import org.cescfe.numpairs.domain.puzzle.model.Operator
import org.cescfe.numpairs.domain.puzzle.model.StripItem
import org.junit.Assert.assertEquals
import org.junit.Assert.assertSame
import org.junit.Test

class GameViewModelPuzzleStateTest {
    @Test
    fun `starts with the exact initial puzzle`() {
        val viewModel = GameViewModel(initialPuzzle = samplePuzzle)

        assertSame(samplePuzzle, viewModel.currentPuzzle.value)
    }

    @Test
    fun `transient strip drafts and selectors do not change the current puzzle`() {
        val viewModel = GameViewModel(initialPuzzle = samplePuzzle)

        viewModel.onStripItemTapped(index = 0)
        viewModel.onStripItemEntryInputChanged(draftText = "1")
        assertSame(samplePuzzle, viewModel.currentPuzzle.value)

        viewModel.onStripItemEntryInputCancelled()
        viewModel.onTileLeftOperandTapped(index = 0)
        assertSame(samplePuzzle, viewModel.currentPuzzle.value)

        viewModel.onTileOperandSelectionDismissed()
        viewModel.onTileOperatorTapped(index = 0)
        assertSame(samplePuzzle, viewModel.currentPuzzle.value)
    }

    @Test
    fun `confirmed strip operand and operator changes update the current puzzle`() {
        val viewModel = GameViewModel(initialPuzzle = samplePuzzle)

        viewModel.onStripItemTapped(index = 0)
        viewModel.onStripItemEntryInputChanged(draftText = "1")
        viewModel.onStripItemEntryInputConfirmed()
        assertEquals(StripItem.PlayerEntered(1), viewModel.currentPuzzle.value.strip.items[0])

        viewModel.onTileLeftOperandTapped(index = 0)
        viewModel.onTileOperandSelectionConfirmed(stripEntryId = 0)
        assertEquals(
            Expression.Operand.Known(value = 1, stripEntryId = 0),
            viewModel.currentPuzzle.value.board.tiles[0].expression.leftOperand
        )

        viewModel.onTileOperatorTapped(index = 0)
        viewModel.onTileOperatorSelectionConfirmed(operator = Operator.ADDITION)
        assertEquals(
            Operator.ADDITION,
            viewModel.currentPuzzle.value.board.tiles[0].expression.operator
        )
    }

    @Test
    fun `tile and full puzzle reset update the current puzzle`() {
        val viewModel = GameViewModel(initialPuzzle = samplePuzzle)
        viewModel.onTileLeftOperandTapped(index = 0)
        viewModel.onTileOperandSelectionConfirmed(stripEntryId = 2)

        viewModel.onTileResetTapped(index = 0)

        assertEquals(
            Expression.Operand.Hidden,
            viewModel.currentPuzzle.value.board.tiles[0].expression.leftOperand
        )

        val replacement = samplePuzzle.copy(
            strip = samplePuzzle.strip.withUpdatedEntry(
                index = 1,
                value = 1
            )
        )
        viewModel.reset(initialPuzzle = replacement)

        assertSame(replacement, viewModel.currentPuzzle.value)
    }
}
