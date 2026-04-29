package org.cescfe.numpairs.domain.puzzle

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class SumProductPairingTest {
    @Test
    fun matching_sum_and_product_pairs_do_not_report_a_mismatch_when_operand_order_differs() {
        val puzzle = solvedPuzzleWithTiles(
            tile(
                leftEntryId = 0,
                leftValue = 1,
                operator = Operator.ADDITION,
                rightEntryId = 1,
                rightValue = 2
            ),
            tile(
                leftEntryId = 1,
                leftValue = 2,
                operator = Operator.MULTIPLICATION,
                rightEntryId = 0,
                rightValue = 1
            ),
            tile(
                leftEntryId = 2,
                leftValue = 3,
                operator = Operator.ADDITION,
                rightEntryId = 3,
                rightValue = 4
            ),
            tile(
                leftEntryId = 3,
                leftValue = 4,
                operator = Operator.MULTIPLICATION,
                rightEntryId = 2,
                rightValue = 3
            ),
            tile(
                leftEntryId = 4,
                leftValue = 5,
                operator = Operator.ADDITION,
                rightEntryId = 5,
                rightValue = 6
            ),
            tile(
                leftEntryId = 5,
                leftValue = 6,
                operator = Operator.MULTIPLICATION,
                rightEntryId = 4,
                rightValue = 5
            ),
            tile(
                leftEntryId = 6,
                leftValue = 7,
                operator = Operator.ADDITION,
                rightEntryId = 7,
                rightValue = 8
            ),
            tile(
                leftEntryId = 7,
                leftValue = 8,
                operator = Operator.MULTIPLICATION,
                rightEntryId = 6,
                rightValue = 7
            )
        )

        assertFalse(puzzle.hasMismatchedSumProductPairings)
    }

    @Test
    fun mismatched_sum_and_product_pairs_report_an_invalid_pairing_state_even_when_all_tiles_are_locally_correct() {
        val puzzle = solvedPuzzleWithTiles(
            tile(
                leftEntryId = 0,
                leftValue = 1,
                operator = Operator.ADDITION,
                rightEntryId = 1,
                rightValue = 2
            ),
            tile(
                leftEntryId = 0,
                leftValue = 1,
                operator = Operator.MULTIPLICATION,
                rightEntryId = 2,
                rightValue = 3
            ),
            tile(
                leftEntryId = 2,
                leftValue = 3,
                operator = Operator.ADDITION,
                rightEntryId = 3,
                rightValue = 4
            ),
            tile(
                leftEntryId = 1,
                leftValue = 2,
                operator = Operator.MULTIPLICATION,
                rightEntryId = 3,
                rightValue = 4
            ),
            tile(
                leftEntryId = 4,
                leftValue = 5,
                operator = Operator.ADDITION,
                rightEntryId = 5,
                rightValue = 6
            ),
            tile(
                leftEntryId = 5,
                leftValue = 6,
                operator = Operator.MULTIPLICATION,
                rightEntryId = 4,
                rightValue = 5
            ),
            tile(
                leftEntryId = 6,
                leftValue = 7,
                operator = Operator.ADDITION,
                rightEntryId = 7,
                rightValue = 8
            ),
            tile(
                leftEntryId = 7,
                leftValue = 8,
                operator = Operator.MULTIPLICATION,
                rightEntryId = 6,
                rightValue = 7
            )
        )

        assertTrue(puzzle.board.tiles.all { tile -> tile.resolutionState == TileResolutionState.CORRECT })
        assertTrue(puzzle.hasMismatchedSumProductPairings)
    }
}

private fun solvedPuzzleWithTiles(vararg tiles: Tile): Puzzle = Puzzle(
    board = Board(tiles = tiles.toList()),
    strip = Strip.fromItems(
        items = listOf(
            StripItem.Known(1),
            StripItem.Known(2),
            StripItem.Known(3),
            StripItem.Known(4),
            StripItem.Known(5),
            StripItem.Known(6),
            StripItem.Known(7),
            StripItem.Known(8)
        )
    )
)

private fun tile(leftEntryId: Int, leftValue: Int, operator: Operator, rightEntryId: Int, rightValue: Int): Tile = Tile(
    expression = Expression(
        leftOperand = Expression.Operand.Known(
            value = leftValue,
            stripEntryId = leftEntryId
        ),
        operator = operator,
        rightOperand = Expression.Operand.Known(
            value = rightValue,
            stripEntryId = rightEntryId
        )
    ),
    result = operator.apply(leftValue, rightValue)
)
