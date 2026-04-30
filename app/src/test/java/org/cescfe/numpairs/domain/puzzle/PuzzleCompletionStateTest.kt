package org.cescfe.numpairs.domain.puzzle

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class PuzzleCompletionStateTest {
    @Test
    fun solved_puzzle_reports_solved_completion_state() {
        val puzzle = puzzleWithKnownStripAndAssignments(
            stripValues = listOf(1, 2, 3, 4, 5, 6, 7, 8),
            CompletionTileAssignment(leftEntryId = 0, operator = Operator.ADDITION, rightEntryId = 1),
            CompletionTileAssignment(leftEntryId = 1, operator = Operator.MULTIPLICATION, rightEntryId = 0),
            CompletionTileAssignment(leftEntryId = 2, operator = Operator.ADDITION, rightEntryId = 3),
            CompletionTileAssignment(leftEntryId = 3, operator = Operator.MULTIPLICATION, rightEntryId = 2),
            CompletionTileAssignment(leftEntryId = 4, operator = Operator.ADDITION, rightEntryId = 5),
            CompletionTileAssignment(leftEntryId = 5, operator = Operator.MULTIPLICATION, rightEntryId = 4),
            CompletionTileAssignment(leftEntryId = 6, operator = Operator.ADDITION, rightEntryId = 7),
            CompletionTileAssignment(leftEntryId = 7, operator = Operator.MULTIPLICATION, rightEntryId = 6)
        )

        assertEquals(PuzzleCompletionState.SOLVED, puzzle.completionState)
        assertTrue(puzzle.isSolved)
    }

    @Test
    fun incomplete_puzzle_reports_incomplete_completion_state() {
        val puzzle = Puzzle(
            board = solvedBoardWithAssignments(),
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

        assertEquals(PuzzleCompletionState.INCOMPLETE, puzzle.completionState)
        assertFalse(puzzle.isSolved)
    }

    @Test
    fun puzzle_with_incorrect_tiles_reports_incorrect_tiles_completion_state() {
        val puzzle = puzzleWithKnownStripAndAssignments(
            stripValues = listOf(1, 2, 3, 4, 5, 6, 7, 8),
            CompletionTileAssignment(leftEntryId = 0, operator = Operator.ADDITION, rightEntryId = 1, result = 999),
            CompletionTileAssignment(leftEntryId = 1, operator = Operator.MULTIPLICATION, rightEntryId = 0),
            CompletionTileAssignment(leftEntryId = 2, operator = Operator.ADDITION, rightEntryId = 3),
            CompletionTileAssignment(leftEntryId = 3, operator = Operator.MULTIPLICATION, rightEntryId = 2),
            CompletionTileAssignment(leftEntryId = 4, operator = Operator.ADDITION, rightEntryId = 5),
            CompletionTileAssignment(leftEntryId = 5, operator = Operator.MULTIPLICATION, rightEntryId = 4),
            CompletionTileAssignment(leftEntryId = 6, operator = Operator.ADDITION, rightEntryId = 7),
            CompletionTileAssignment(leftEntryId = 7, operator = Operator.MULTIPLICATION, rightEntryId = 6)
        )

        assertEquals(PuzzleCompletionState.INCORRECT_TILES, puzzle.completionState)
        assertFalse(puzzle.isSolved)
    }

    @Test
    fun puzzle_without_strip_entry_identity_for_all_tiles_is_not_reported_as_solved() {
        val puzzle = Puzzle(
            board = Board(
                tiles = listOf(
                    tileWithoutStripIdentity(),
                    tileWithAssignment(
                        leftEntryId = 1,
                        leftValue = 2,
                        operator = Operator.MULTIPLICATION,
                        rightEntryId = 0,
                        rightValue = 1
                    ),
                    tileWithAssignment(
                        leftEntryId = 2,
                        leftValue = 3,
                        operator = Operator.ADDITION,
                        rightEntryId = 3,
                        rightValue = 4
                    ),
                    tileWithAssignment(
                        leftEntryId = 3,
                        leftValue = 4,
                        operator = Operator.MULTIPLICATION,
                        rightEntryId = 2,
                        rightValue = 3
                    ),
                    tileWithAssignment(
                        leftEntryId = 4,
                        leftValue = 5,
                        operator = Operator.ADDITION,
                        rightEntryId = 5,
                        rightValue = 6
                    ),
                    tileWithAssignment(
                        leftEntryId = 5,
                        leftValue = 6,
                        operator = Operator.MULTIPLICATION,
                        rightEntryId = 4,
                        rightValue = 5
                    ),
                    tileWithAssignment(
                        leftEntryId = 6,
                        leftValue = 7,
                        operator = Operator.ADDITION,
                        rightEntryId = 7,
                        rightValue = 8
                    ),
                    tileWithAssignment(
                        leftEntryId = 7,
                        leftValue = 8,
                        operator = Operator.MULTIPLICATION,
                        rightEntryId = 6,
                        rightValue = 7
                    )
                )
            ),
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

        assertEquals(PuzzleCompletionState.MISSING_STRIP_ENTRY_IDENTITIES, puzzle.completionState)
        assertFalse(puzzle.isSolved)
    }

    @Test
    fun mismatched_pairings_report_mismatched_pairings_completion_state() {
        val puzzle = puzzleWithKnownStripAndAssignments(
            stripValues = listOf(1, 2, 3, 4, 5, 6, 7, 8),
            CompletionTileAssignment(leftEntryId = 0, operator = Operator.ADDITION, rightEntryId = 1),
            CompletionTileAssignment(leftEntryId = 0, operator = Operator.MULTIPLICATION, rightEntryId = 2),
            CompletionTileAssignment(leftEntryId = 2, operator = Operator.ADDITION, rightEntryId = 3),
            CompletionTileAssignment(leftEntryId = 1, operator = Operator.MULTIPLICATION, rightEntryId = 3),
            CompletionTileAssignment(leftEntryId = 4, operator = Operator.ADDITION, rightEntryId = 5),
            CompletionTileAssignment(leftEntryId = 5, operator = Operator.MULTIPLICATION, rightEntryId = 4),
            CompletionTileAssignment(leftEntryId = 6, operator = Operator.ADDITION, rightEntryId = 7),
            CompletionTileAssignment(leftEntryId = 7, operator = Operator.MULTIPLICATION, rightEntryId = 6)
        )

        assertEquals(PuzzleCompletionState.MISMATCHED_SUM_PRODUCT_PAIRINGS, puzzle.completionState)
        assertFalse(puzzle.isSolved)
    }

    @Test
    fun invalid_strip_entry_usage_reports_invalid_strip_entry_usage_completion_state() {
        val puzzle = puzzleWithKnownStripAndAssignments(
            stripValues = listOf(2, 2, 2, 2, 5, 5, 7, 7),
            CompletionTileAssignment(leftEntryId = 0, operator = Operator.ADDITION, rightEntryId = 1),
            CompletionTileAssignment(leftEntryId = 1, operator = Operator.MULTIPLICATION, rightEntryId = 0),
            CompletionTileAssignment(leftEntryId = 0, operator = Operator.ADDITION, rightEntryId = 1),
            CompletionTileAssignment(leftEntryId = 1, operator = Operator.MULTIPLICATION, rightEntryId = 0),
            CompletionTileAssignment(leftEntryId = 4, operator = Operator.ADDITION, rightEntryId = 5),
            CompletionTileAssignment(leftEntryId = 5, operator = Operator.MULTIPLICATION, rightEntryId = 4),
            CompletionTileAssignment(leftEntryId = 6, operator = Operator.ADDITION, rightEntryId = 7),
            CompletionTileAssignment(leftEntryId = 7, operator = Operator.MULTIPLICATION, rightEntryId = 6)
        )

        assertEquals(PuzzleCompletionState.INVALID_STRIP_ENTRY_USAGE, puzzle.completionState)
        assertFalse(puzzle.isSolved)
    }
}

private data class CompletionTileAssignment(
    val leftEntryId: Int,
    val operator: Operator,
    val rightEntryId: Int,
    val result: Int? = null
)

private fun solvedBoardWithAssignments(): Board = puzzleWithKnownStripAndAssignments(
    stripValues = listOf(1, 2, 3, 4, 5, 6, 7, 8),
    CompletionTileAssignment(leftEntryId = 0, operator = Operator.ADDITION, rightEntryId = 1),
    CompletionTileAssignment(leftEntryId = 1, operator = Operator.MULTIPLICATION, rightEntryId = 0),
    CompletionTileAssignment(leftEntryId = 2, operator = Operator.ADDITION, rightEntryId = 3),
    CompletionTileAssignment(leftEntryId = 3, operator = Operator.MULTIPLICATION, rightEntryId = 2),
    CompletionTileAssignment(leftEntryId = 4, operator = Operator.ADDITION, rightEntryId = 5),
    CompletionTileAssignment(leftEntryId = 5, operator = Operator.MULTIPLICATION, rightEntryId = 4),
    CompletionTileAssignment(leftEntryId = 6, operator = Operator.ADDITION, rightEntryId = 7),
    CompletionTileAssignment(leftEntryId = 7, operator = Operator.MULTIPLICATION, rightEntryId = 6)
).board

private fun puzzleWithKnownStripAndAssignments(
    stripValues: List<Int>,
    vararg tileAssignments: CompletionTileAssignment
): Puzzle = Puzzle(
    board = Board(
        tiles = tileAssignments.map { assignment ->
            tileWithAssignment(
                leftEntryId = assignment.leftEntryId,
                leftValue = stripValues[assignment.leftEntryId],
                operator = assignment.operator,
                rightEntryId = assignment.rightEntryId,
                rightValue = stripValues[assignment.rightEntryId],
                result = assignment.result
            )
        }
    ),
    strip = Strip.fromItems(items = stripValues.map(StripItem::Known))
)

private fun tileWithAssignment(
    leftEntryId: Int,
    leftValue: Int,
    operator: Operator,
    rightEntryId: Int,
    rightValue: Int,
    result: Int? = null
): Tile = Tile(
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
    result = result ?: operator.apply(leftValue, rightValue)
)

private fun tileWithoutStripIdentity(): Tile = Tile(
    expression = Expression(
        leftOperand = Expression.Operand.Known(value = 1),
        operator = Operator.ADDITION,
        rightOperand = Expression.Operand.Known(value = 2)
    ),
    result = Operator.ADDITION.apply(leftOperand = 1, rightOperand = 2)
)
