package org.cescfe.numpairs.feature.game.presentation

import org.cescfe.numpairs.data.puzzle.seed.samplePuzzle
import org.cescfe.numpairs.domain.puzzle.model.Board
import org.cescfe.numpairs.domain.puzzle.model.Expression
import org.cescfe.numpairs.domain.puzzle.model.Operator
import org.cescfe.numpairs.domain.puzzle.model.Puzzle
import org.cescfe.numpairs.domain.puzzle.model.Strip
import org.cescfe.numpairs.domain.puzzle.model.StripEntry
import org.cescfe.numpairs.domain.puzzle.model.StripItem
import org.cescfe.numpairs.domain.puzzle.model.Tile
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertSame
import org.junit.Assert.assertTrue
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

    @Test
    fun `committed puzzle mutations remain distinct when one action changes the strip and resets a tile`() {
        val viewModel = GameViewModel(initialPuzzle = samplePuzzle)
        viewModel.onTileOperatorTapped(index = 0)
        viewModel.onTileOperatorSelectionConfirmed(operator = Operator.ADDITION)
        assertEquals(1, viewModel.consumeCommittedPuzzleMutations().size)
        viewModel.onStripItemTapped(index = 5)
        viewModel.onStripItemEntryInputChanged(draftText = "100")

        viewModel.onTileResetTapped(index = 0)

        val mutations = viewModel.consumeCommittedPuzzleMutations()
        assertEquals(2, mutations.size)
        assertEquals(StripItem.PlayerEntered(100), mutations[0].puzzle.strip.items[5])
        assertEquals(Operator.ADDITION, mutations[0].puzzle.board.tiles[0].expression.operator)
        assertEquals(StripItem.PlayerEntered(100), mutations[1].puzzle.strip.items[5])
        assertEquals(samplePuzzle.board.tiles[0], mutations[1].puzzle.board.tiles[0])
    }

    @Test
    fun `initial strip operand and operator assignments are not corrections`() {
        val viewModel = GameViewModel(initialPuzzle = samplePuzzle)

        viewModel.onStripItemTapped(index = 5)
        viewModel.onStripItemEntryInputChanged(draftText = "100")
        viewModel.onStripItemEntryInputConfirmed()
        viewModel.onTileLeftOperandTapped(index = 0)
        viewModel.onTileOperandSelectionConfirmed(stripEntryId = 2)
        viewModel.onTileOperatorSelectionConfirmed(operator = Operator.ADDITION)

        val mutations = viewModel.consumeCommittedPuzzleMutations()
        assertEquals(3, mutations.size)
        assertTrue(mutations.none(CommittedPuzzleMutation::isCorrection))
    }

    @Test
    fun `changing and clearing an entered strip value are separate corrections`() {
        val viewModel = GameViewModel(initialPuzzle = samplePuzzle)
        viewModel.onStripItemTapped(index = 5)
        viewModel.onStripItemEntryInputChanged(draftText = "100")
        viewModel.onStripItemEntryInputConfirmed()
        viewModel.consumeCommittedPuzzleMutations()

        viewModel.onStripItemTapped(index = 5)
        viewModel.onStripItemEntryInputChanged(draftText = "101")
        viewModel.onStripItemEntryInputConfirmed()
        viewModel.onStripItemTapped(index = 5)
        viewModel.onStripItemEntryInputCleared()

        val corrections = viewModel.consumeCommittedPuzzleMutations()
        assertEquals(2, corrections.size)
        assertTrue(corrections.all(CommittedPuzzleMutation::isCorrection))
    }

    @Test
    fun `operand operator and non-pristine reset changes are corrections`() {
        val operandViewModel = GameViewModel(initialPuzzle = samplePuzzle)
        operandViewModel.onTileLeftOperandTapped(index = 0)
        operandViewModel.onTileOperandSelectionConfirmed(stripEntryId = 2)
        operandViewModel.onTileOperatorSelectionDismissed()
        operandViewModel.consumeCommittedPuzzleMutations()
        operandViewModel.onTileLeftOperandTapped(index = 0)
        operandViewModel.onTileOperandSelectionConfirmed(stripEntryId = 4)
        assertTrue(operandViewModel.consumeCommittedPuzzleMutations().single().isCorrection)

        val operatorViewModel = GameViewModel(initialPuzzle = samplePuzzle)
        operatorViewModel.onTileOperatorTapped(index = 0)
        operatorViewModel.onTileOperatorSelectionConfirmed(operator = Operator.ADDITION)
        operatorViewModel.consumeCommittedPuzzleMutations()
        operatorViewModel.onTileOperatorTapped(index = 0)
        operatorViewModel.onTileOperatorSelectionConfirmed(operator = Operator.MULTIPLICATION)
        assertTrue(operatorViewModel.consumeCommittedPuzzleMutations().single().isCorrection)

        val resetViewModel = GameViewModel(initialPuzzle = samplePuzzle)
        resetViewModel.onTileLeftOperandTapped(index = 0)
        resetViewModel.onTileOperandSelectionConfirmed(stripEntryId = 2)
        resetViewModel.consumeCommittedPuzzleMutations()
        resetViewModel.onTileResetTapped(index = 0)
        assertTrue(resetViewModel.consumeCommittedPuzzleMutations().single().isCorrection)
    }

    @Test
    fun `operand correction follows strip identity even when the visible value is unchanged`() {
        val puzzle = Puzzle(
            board = Board(
                tiles = listOf(
                    Tile(
                        expression = Expression(
                            leftOperand = Expression.Operand.Hidden,
                            operator = Operator.Hidden,
                            rightOperand = Expression.Operand.Hidden
                        ),
                        result = 4
                    ),
                    Tile(
                        expression = Expression(
                            leftOperand = Expression.Operand.Hidden,
                            operator = Operator.Hidden,
                            rightOperand = Expression.Operand.Hidden
                        ),
                        result = 4
                    )
                )
            ),
            strip = Strip.fromEntries(
                listOf(
                    StripEntry(id = 0, item = StripItem.Known(2)),
                    StripEntry(id = 1, item = StripItem.Known(2))
                )
            )
        )
        val viewModel = GameViewModel(initialPuzzle = puzzle)
        viewModel.onTileLeftOperandTapped(index = 0)
        viewModel.onTileOperandSelectionConfirmed(stripEntryId = 0)
        viewModel.onTileOperatorSelectionDismissed()
        viewModel.consumeCommittedPuzzleMutations()

        viewModel.onTileLeftOperandTapped(index = 0)
        viewModel.onTileOperandSelectionConfirmed(stripEntryId = 1)

        val correction = viewModel.consumeCommittedPuzzleMutations().single()
        val assignedOperand = correction.puzzle.board.tiles.first().expression.leftOperand as Expression.Operand.Known
        assertTrue(correction.isCorrection)
        assertEquals(2, assignedOperand.value)
        assertEquals(1, assignedOperand.stripEntryId)
    }

    @Test
    fun `unchanged confirmations and pristine reset do not emit corrections`() {
        val viewModel = GameViewModel(initialPuzzle = samplePuzzle)
        viewModel.onTileResetTapped(index = 0)
        assertTrue(viewModel.consumeCommittedPuzzleMutations().isEmpty())

        viewModel.onStripItemTapped(index = 5)
        viewModel.onStripItemEntryInputChanged(draftText = "100")
        viewModel.onStripItemEntryInputConfirmed()
        viewModel.consumeCommittedPuzzleMutations()
        viewModel.onStripItemTapped(index = 5)
        viewModel.onStripItemEntryInputConfirmed()

        assertFalse(viewModel.consumeCommittedPuzzleMutations().any(CommittedPuzzleMutation::isCorrection))
    }
}
