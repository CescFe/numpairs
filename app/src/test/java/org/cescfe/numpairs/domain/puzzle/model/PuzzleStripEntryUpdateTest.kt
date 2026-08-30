package org.cescfe.numpairs.domain.puzzle.model

import org.junit.Assert.assertEquals
import org.junit.Test

class PuzzleStripEntryUpdateTest {
    @Test
    fun updating_a_strip_entry_updates_every_referencing_operand_by_stable_identity() {
        val puzzle = Puzzle(
            board = Board(
                tiles = listOf(
                    Tile(
                        expression = Expression(
                            leftOperand = Expression.Operand.Known(value = 5, stripEntryId = 10),
                            operator = Operator.ADDITION,
                            rightOperand = Expression.Operand.Known(value = 2, stripEntryId = 11)
                        ),
                        result = 7
                    ),
                    Tile(
                        expression = Expression(
                            leftOperand = Expression.Operand.Known(value = 2, stripEntryId = 11),
                            operator = Operator.MULTIPLICATION,
                            rightOperand = Expression.Operand.Known(value = 5, stripEntryId = 10)
                        ),
                        result = 10
                    )
                )
            ),
            strip = Strip.fromEntries(
                entries = listOf(
                    StripEntry(id = 11, item = StripItem.PlayerEntered(2)),
                    StripEntry(id = 10, item = StripItem.PlayerEntered(5))
                )
            )
        )

        val updatedPuzzle = puzzle.withUpdatedStripEntry(index = 1, value = 1)

        assertEquals(listOf(10, 11), updatedPuzzle.strip.entries.map(StripEntry::id))
        assertEquals(
            Expression.Operand.Known(value = 1, stripEntryId = 10),
            updatedPuzzle.board.tiles[0].expression.leftOperand
        )
        assertEquals(
            Expression.Operand.Known(value = 2, stripEntryId = 11),
            updatedPuzzle.board.tiles[0].expression.rightOperand
        )
        assertEquals(
            Expression.Operand.Known(value = 2, stripEntryId = 11),
            updatedPuzzle.board.tiles[1].expression.leftOperand
        )
        assertEquals(
            Expression.Operand.Known(value = 1, stripEntryId = 10),
            updatedPuzzle.board.tiles[1].expression.rightOperand
        )
        assertEquals(PuzzleCompletionState.INCORRECT_TILES, updatedPuzzle.completionState)
    }
}
