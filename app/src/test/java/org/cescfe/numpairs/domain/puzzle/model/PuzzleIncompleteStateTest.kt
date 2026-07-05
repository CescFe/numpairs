package org.cescfe.numpairs.domain.puzzle.model

import org.cescfe.numpairs.domain.puzzle.support.additionTile
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
        additionTile(leftOperand = 1, rightOperand = 1, result = 2),
        additionTile(leftOperand = 1, rightOperand = 2, result = 3),
        additionTile(leftOperand = 2, rightOperand = 2, result = 4),
        additionTile(leftOperand = 2, rightOperand = 3, result = 5),
        additionTile(leftOperand = 3, rightOperand = 3, result = 6),
        additionTile(leftOperand = 3, rightOperand = 4, result = 7),
        additionTile(leftOperand = 4, rightOperand = 4, result = 8),
        additionTile(leftOperand = 4, rightOperand = 5, result = 9)
    )
)
