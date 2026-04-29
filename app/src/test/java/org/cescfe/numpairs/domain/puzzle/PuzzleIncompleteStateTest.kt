package org.cescfe.numpairs.domain.puzzle

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class PuzzleIncompleteStateTest {
    @Test
    fun puzzle_is_incomplete_when_the_strip_still_contains_hidden_entries() {
        val puzzle = Puzzle(
            board = resolvedBoard(),
            strip = Strip.fromItems(
                items = listOf(
                    StripItem.Known(1),
                    StripItem.Hidden,
                    StripItem.Known(3),
                    StripItem.Known(4),
                    StripItem.Known(5),
                    StripItem.Known(6),
                    StripItem.Known(7),
                    StripItem.Known(8)
                )
            )
        )

        assertTrue(puzzle.strip.hasHiddenEntries)
        assertTrue(puzzle.isIncomplete)
    }

    @Test
    fun puzzle_is_incomplete_when_one_or_more_tiles_are_unresolved() {
        val puzzle = Puzzle(
            board = Board(
                tiles = resolvedBoard().tiles.toMutableList().apply {
                    set(
                        0,
                        Tile(
                            expression = Expression(
                                leftOperand = Expression.Operand.Hidden,
                                operator = Operator.ADDITION,
                                rightOperand = Expression.Operand.Known(1)
                            ),
                            result = 2
                        )
                    )
                }
            ),
            strip = fullyVisibleStrip()
        )

        assertTrue(puzzle.board.hasUnresolvedTiles)
        assertTrue(puzzle.isIncomplete)
    }

    @Test
    fun puzzle_is_not_incomplete_when_all_strip_entries_are_visible_and_all_tiles_are_fully_known() {
        val puzzle = Puzzle(
            board = resolvedBoard(),
            strip = fullyVisibleStrip()
        )

        assertFalse(puzzle.strip.hasHiddenEntries)
        assertFalse(puzzle.board.hasUnresolvedTiles)
        assertFalse(puzzle.isIncomplete)
    }
}

private fun fullyVisibleStrip(): Strip = Strip.fromItems(
    items = listOf(
        StripItem.Known(1),
        StripItem.PlayerEntered(2),
        StripItem.Known(3),
        StripItem.PlayerEntered(4),
        StripItem.Known(5),
        StripItem.PlayerEntered(6),
        StripItem.Known(7),
        StripItem.PlayerEntered(8)
    )
)

private fun resolvedBoard(): Board = Board(
    tiles = listOf(
        resolvedTile(result = 2, leftOperand = 1, rightOperand = 1),
        resolvedTile(result = 3, leftOperand = 1, rightOperand = 2),
        resolvedTile(result = 4, leftOperand = 2, rightOperand = 2),
        resolvedTile(result = 5, leftOperand = 2, rightOperand = 3),
        resolvedTile(result = 6, leftOperand = 3, rightOperand = 3),
        resolvedTile(result = 7, leftOperand = 3, rightOperand = 4),
        resolvedTile(result = 8, leftOperand = 4, rightOperand = 4),
        resolvedTile(result = 9, leftOperand = 4, rightOperand = 5)
    )
)

private fun resolvedTile(result: Int, leftOperand: Int, rightOperand: Int): Tile = Tile(
    expression = Expression(
        leftOperand = leftOperand,
        operator = Operator.ADDITION,
        rightOperand = rightOperand
    ),
    result = result
)
