package org.cescfe.numpairs.domain.puzzle

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import org.cescfe.numpairs.domain.puzzle.support.assignedTile
import org.cescfe.numpairs.domain.puzzle.support.defaultKnownStripValues
import org.cescfe.numpairs.domain.puzzle.support.knownPuzzleWithAssignments
import org.cescfe.numpairs.domain.puzzle.support.stripOf
import org.cescfe.numpairs.domain.puzzle.support.tileAssignment
import org.cescfe.numpairs.domain.puzzle.support.tileWithoutStripIdentity

class PuzzleCompletionStateTest {
    @Test
    fun solved_puzzle_reports_solved_completion_state() {
        val puzzle = knownPuzzleWithAssignments(
            stripValues = defaultKnownStripValues(),
            tileAssignment(leftEntryId = 0, operator = Operator.ADDITION, rightEntryId = 1),
            tileAssignment(leftEntryId = 1, operator = Operator.MULTIPLICATION, rightEntryId = 0),
            tileAssignment(leftEntryId = 2, operator = Operator.ADDITION, rightEntryId = 3),
            tileAssignment(leftEntryId = 3, operator = Operator.MULTIPLICATION, rightEntryId = 2),
            tileAssignment(leftEntryId = 4, operator = Operator.ADDITION, rightEntryId = 5),
            tileAssignment(leftEntryId = 5, operator = Operator.MULTIPLICATION, rightEntryId = 4),
            tileAssignment(leftEntryId = 6, operator = Operator.ADDITION, rightEntryId = 7),
            tileAssignment(leftEntryId = 7, operator = Operator.MULTIPLICATION, rightEntryId = 6)
        )

        assertEquals(PuzzleCompletionState.SOLVED, puzzle.completionState)
        assertTrue(puzzle.isSolved)
    }

    @Test
    fun incomplete_puzzle_reports_incomplete_completion_state() {
        val puzzle = Puzzle(
            board = solvedBoardWithAssignments(),
            strip = stripOf(
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

        assertEquals(PuzzleCompletionState.INCOMPLETE, puzzle.completionState)
        assertFalse(puzzle.isSolved)
    }

    @Test
    fun puzzle_with_incorrect_tiles_reports_incorrect_tiles_completion_state() {
        val puzzle = knownPuzzleWithAssignments(
            stripValues = defaultKnownStripValues(),
            tileAssignment(leftEntryId = 0, operator = Operator.ADDITION, rightEntryId = 1, result = 999),
            tileAssignment(leftEntryId = 1, operator = Operator.MULTIPLICATION, rightEntryId = 0),
            tileAssignment(leftEntryId = 2, operator = Operator.ADDITION, rightEntryId = 3),
            tileAssignment(leftEntryId = 3, operator = Operator.MULTIPLICATION, rightEntryId = 2),
            tileAssignment(leftEntryId = 4, operator = Operator.ADDITION, rightEntryId = 5),
            tileAssignment(leftEntryId = 5, operator = Operator.MULTIPLICATION, rightEntryId = 4),
            tileAssignment(leftEntryId = 6, operator = Operator.ADDITION, rightEntryId = 7),
            tileAssignment(leftEntryId = 7, operator = Operator.MULTIPLICATION, rightEntryId = 6)
        )

        assertEquals(PuzzleCompletionState.INCORRECT_TILES, puzzle.completionState)
        assertFalse(puzzle.isSolved)
    }

    @Test
    fun puzzle_without_strip_entry_identity_for_all_tiles_is_not_reported_as_solved() {
        val puzzle = Puzzle(
            board = Board(
                tiles = listOf(
                    tileWithoutStripIdentity(
                        leftOperand = 1,
                        operator = Operator.ADDITION,
                        rightOperand = 2
                    ),
                    assignedTile(leftEntryId = 1, leftValue = 2, operator = Operator.MULTIPLICATION, rightEntryId = 0, rightValue = 1),
                    assignedTile(leftEntryId = 2, leftValue = 3, operator = Operator.ADDITION, rightEntryId = 3, rightValue = 4),
                    assignedTile(leftEntryId = 3, leftValue = 4, operator = Operator.MULTIPLICATION, rightEntryId = 2, rightValue = 3),
                    assignedTile(leftEntryId = 4, leftValue = 5, operator = Operator.ADDITION, rightEntryId = 5, rightValue = 6),
                    assignedTile(leftEntryId = 5, leftValue = 6, operator = Operator.MULTIPLICATION, rightEntryId = 4, rightValue = 5),
                    assignedTile(leftEntryId = 6, leftValue = 7, operator = Operator.ADDITION, rightEntryId = 7, rightValue = 8),
                    assignedTile(leftEntryId = 7, leftValue = 8, operator = Operator.MULTIPLICATION, rightEntryId = 6, rightValue = 7)
                )
            ),
            strip = stripOf(
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

        assertEquals(PuzzleCompletionState.MISSING_STRIP_ENTRY_IDENTITIES, puzzle.completionState)
        assertFalse(puzzle.isSolved)
    }

    @Test
    fun mismatched_pairings_report_mismatched_pairings_completion_state() {
        val puzzle = knownPuzzleWithAssignments(
            stripValues = defaultKnownStripValues(),
            tileAssignment(leftEntryId = 0, operator = Operator.ADDITION, rightEntryId = 1),
            tileAssignment(leftEntryId = 0, operator = Operator.MULTIPLICATION, rightEntryId = 2),
            tileAssignment(leftEntryId = 2, operator = Operator.ADDITION, rightEntryId = 3),
            tileAssignment(leftEntryId = 1, operator = Operator.MULTIPLICATION, rightEntryId = 3),
            tileAssignment(leftEntryId = 4, operator = Operator.ADDITION, rightEntryId = 5),
            tileAssignment(leftEntryId = 5, operator = Operator.MULTIPLICATION, rightEntryId = 4),
            tileAssignment(leftEntryId = 6, operator = Operator.ADDITION, rightEntryId = 7),
            tileAssignment(leftEntryId = 7, operator = Operator.MULTIPLICATION, rightEntryId = 6)
        )

        assertEquals(PuzzleCompletionState.MISMATCHED_SUM_PRODUCT_PAIRINGS, puzzle.completionState)
        assertFalse(puzzle.isSolved)
    }

    @Test
    fun invalid_strip_entry_usage_reports_invalid_strip_entry_usage_completion_state() {
        val puzzle = knownPuzzleWithAssignments(
            stripValues = listOf(2, 2, 2, 2, 5, 5, 7, 7),
            tileAssignment(leftEntryId = 0, operator = Operator.ADDITION, rightEntryId = 1),
            tileAssignment(leftEntryId = 1, operator = Operator.MULTIPLICATION, rightEntryId = 0),
            tileAssignment(leftEntryId = 0, operator = Operator.ADDITION, rightEntryId = 1),
            tileAssignment(leftEntryId = 1, operator = Operator.MULTIPLICATION, rightEntryId = 0),
            tileAssignment(leftEntryId = 4, operator = Operator.ADDITION, rightEntryId = 5),
            tileAssignment(leftEntryId = 5, operator = Operator.MULTIPLICATION, rightEntryId = 4),
            tileAssignment(leftEntryId = 6, operator = Operator.ADDITION, rightEntryId = 7),
            tileAssignment(leftEntryId = 7, operator = Operator.MULTIPLICATION, rightEntryId = 6)
        )

        assertEquals(PuzzleCompletionState.INVALID_STRIP_ENTRY_USAGE, puzzle.completionState)
        assertFalse(puzzle.isSolved)
    }
}

private fun solvedBoardWithAssignments(): Board = knownPuzzleWithAssignments(
    stripValues = defaultKnownStripValues(),
    tileAssignment(leftEntryId = 0, operator = Operator.ADDITION, rightEntryId = 1),
    tileAssignment(leftEntryId = 1, operator = Operator.MULTIPLICATION, rightEntryId = 0),
    tileAssignment(leftEntryId = 2, operator = Operator.ADDITION, rightEntryId = 3),
    tileAssignment(leftEntryId = 3, operator = Operator.MULTIPLICATION, rightEntryId = 2),
    tileAssignment(leftEntryId = 4, operator = Operator.ADDITION, rightEntryId = 5),
    tileAssignment(leftEntryId = 5, operator = Operator.MULTIPLICATION, rightEntryId = 4),
    tileAssignment(leftEntryId = 6, operator = Operator.ADDITION, rightEntryId = 7),
    tileAssignment(leftEntryId = 7, operator = Operator.MULTIPLICATION, rightEntryId = 6)
).board
