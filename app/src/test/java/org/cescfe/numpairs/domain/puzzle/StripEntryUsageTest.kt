package org.cescfe.numpairs.domain.puzzle

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class StripEntryUsageTest {
    @Test
    fun distinct_entry_ids_with_repeated_values_do_not_report_invalid_usage_when_used_once_per_operator_family() {
        val puzzle = puzzleWithStripValuesAndTileAssignments(
            stripValues = listOf(2, 2, 2, 2, 5, 5, 7, 7),
            TileAssignment(leftEntryId = 0, operator = Operator.ADDITION, rightEntryId = 1),
            TileAssignment(leftEntryId = 1, operator = Operator.MULTIPLICATION, rightEntryId = 0),
            TileAssignment(leftEntryId = 2, operator = Operator.ADDITION, rightEntryId = 3),
            TileAssignment(leftEntryId = 3, operator = Operator.MULTIPLICATION, rightEntryId = 2),
            TileAssignment(leftEntryId = 4, operator = Operator.ADDITION, rightEntryId = 5),
            TileAssignment(leftEntryId = 5, operator = Operator.MULTIPLICATION, rightEntryId = 4),
            TileAssignment(leftEntryId = 6, operator = Operator.ADDITION, rightEntryId = 7),
            TileAssignment(leftEntryId = 7, operator = Operator.MULTIPLICATION, rightEntryId = 6)
        )

        assertFalse(puzzle.hasMismatchedSumProductPairings)
        assertFalse(puzzle.hasInvalidStripEntryUsage)
    }

    @Test
    fun repeating_the_same_repeated_value_pair_reports_invalid_usage_even_when_sum_and_product_pairings_still_match() {
        val puzzle = puzzleWithStripValuesAndTileAssignments(
            stripValues = listOf(2, 2, 2, 2, 5, 5, 7, 7),
            TileAssignment(leftEntryId = 0, operator = Operator.ADDITION, rightEntryId = 1),
            TileAssignment(leftEntryId = 1, operator = Operator.MULTIPLICATION, rightEntryId = 0),
            TileAssignment(leftEntryId = 0, operator = Operator.ADDITION, rightEntryId = 1),
            TileAssignment(leftEntryId = 1, operator = Operator.MULTIPLICATION, rightEntryId = 0),
            TileAssignment(leftEntryId = 4, operator = Operator.ADDITION, rightEntryId = 5),
            TileAssignment(leftEntryId = 5, operator = Operator.MULTIPLICATION, rightEntryId = 4),
            TileAssignment(leftEntryId = 6, operator = Operator.ADDITION, rightEntryId = 7),
            TileAssignment(leftEntryId = 7, operator = Operator.MULTIPLICATION, rightEntryId = 6)
        )

        assertTrue(puzzle.board.tiles.all { tile -> tile.resolutionState == TileResolutionState.CORRECT })
        assertFalse(puzzle.hasMismatchedSumProductPairings)
        assertTrue(puzzle.hasInvalidStripEntryUsage)
    }

    @Test
    fun leaving_a_strip_entry_unused_in_multiplication_reports_invalid_usage() {
        val puzzle = puzzleWithStripValuesAndTileAssignments(
            stripValues = listOf(1, 2, 3, 4, 5, 6, 7, 8),
            TileAssignment(leftEntryId = 0, operator = Operator.ADDITION, rightEntryId = 1),
            TileAssignment(leftEntryId = 1, operator = Operator.MULTIPLICATION, rightEntryId = 0),
            TileAssignment(leftEntryId = 2, operator = Operator.ADDITION, rightEntryId = 3),
            TileAssignment(leftEntryId = 3, operator = Operator.MULTIPLICATION, rightEntryId = 2),
            TileAssignment(leftEntryId = 4, operator = Operator.ADDITION, rightEntryId = 5),
            TileAssignment(leftEntryId = 5, operator = Operator.MULTIPLICATION, rightEntryId = 4),
            TileAssignment(leftEntryId = 6, operator = Operator.ADDITION, rightEntryId = 7),
            TileAssignment(leftEntryId = 5, operator = Operator.MULTIPLICATION, rightEntryId = 6)
        )

        assertTrue(puzzle.board.tiles.all { tile -> tile.resolutionState == TileResolutionState.CORRECT })
        assertTrue(puzzle.hasInvalidStripEntryUsage)
    }
}

private data class TileAssignment(val leftEntryId: Int, val operator: Operator, val rightEntryId: Int)

private fun puzzleWithStripValuesAndTileAssignments(
    stripValues: List<Int>,
    vararg tileAssignments: TileAssignment
): Puzzle = Puzzle(
    board = Board(
        tiles = tileAssignments.map { assignment ->
            tile(
                stripValues = stripValues,
                leftEntryId = assignment.leftEntryId,
                operator = assignment.operator,
                rightEntryId = assignment.rightEntryId
            )
        }
    ),
    strip = Strip.fromItems(
        items = stripValues.map(StripItem::Known)
    )
)

private fun tile(stripValues: List<Int>, leftEntryId: Int, operator: Operator, rightEntryId: Int): Tile {
    val leftValue = stripValues[leftEntryId]
    val rightValue = stripValues[rightEntryId]

    return Tile(
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
}
