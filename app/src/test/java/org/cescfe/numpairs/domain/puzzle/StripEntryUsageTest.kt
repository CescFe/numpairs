package org.cescfe.numpairs.domain.puzzle

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import org.cescfe.numpairs.domain.puzzle.support.knownPuzzleWithAssignments
import org.cescfe.numpairs.domain.puzzle.support.tileAssignment

class StripEntryUsageTest {
    @Test
    fun distinct_entry_ids_with_repeated_values_do_not_report_invalid_usage_when_used_once_per_operator_family() {
        val puzzle = knownPuzzleWithAssignments(
            stripValues = listOf(2, 2, 2, 2, 5, 5, 7, 7),
            tileAssignment(leftEntryId = 0, operator = Operator.ADDITION, rightEntryId = 1),
            tileAssignment(leftEntryId = 1, operator = Operator.MULTIPLICATION, rightEntryId = 0),
            tileAssignment(leftEntryId = 2, operator = Operator.ADDITION, rightEntryId = 3),
            tileAssignment(leftEntryId = 3, operator = Operator.MULTIPLICATION, rightEntryId = 2),
            tileAssignment(leftEntryId = 4, operator = Operator.ADDITION, rightEntryId = 5),
            tileAssignment(leftEntryId = 5, operator = Operator.MULTIPLICATION, rightEntryId = 4),
            tileAssignment(leftEntryId = 6, operator = Operator.ADDITION, rightEntryId = 7),
            tileAssignment(leftEntryId = 7, operator = Operator.MULTIPLICATION, rightEntryId = 6)
        )

        assertFalse(puzzle.hasMismatchedSumProductPairings)
        assertFalse(puzzle.hasInvalidStripEntryUsage)
    }

    @Test
    fun repeating_the_same_repeated_value_pair_reports_invalid_usage_even_when_sum_and_product_pairings_still_match() {
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

        assertTrue(puzzle.board.tiles.all { tile -> tile.resolutionState == TileResolutionState.CORRECT })
        assertFalse(puzzle.hasMismatchedSumProductPairings)
        assertTrue(puzzle.hasInvalidStripEntryUsage)
    }

    @Test
    fun leaving_a_strip_entry_unused_in_multiplication_reports_invalid_usage() {
        val puzzle = knownPuzzleWithAssignments(
            stripValues = listOf(1, 2, 3, 4, 5, 6, 7, 8),
            tileAssignment(leftEntryId = 0, operator = Operator.ADDITION, rightEntryId = 1),
            tileAssignment(leftEntryId = 1, operator = Operator.MULTIPLICATION, rightEntryId = 0),
            tileAssignment(leftEntryId = 2, operator = Operator.ADDITION, rightEntryId = 3),
            tileAssignment(leftEntryId = 3, operator = Operator.MULTIPLICATION, rightEntryId = 2),
            tileAssignment(leftEntryId = 4, operator = Operator.ADDITION, rightEntryId = 5),
            tileAssignment(leftEntryId = 5, operator = Operator.MULTIPLICATION, rightEntryId = 4),
            tileAssignment(leftEntryId = 6, operator = Operator.ADDITION, rightEntryId = 7),
            tileAssignment(leftEntryId = 5, operator = Operator.MULTIPLICATION, rightEntryId = 6)
        )

        assertTrue(puzzle.board.tiles.all { tile -> tile.resolutionState == TileResolutionState.CORRECT })
        assertTrue(puzzle.hasInvalidStripEntryUsage)
    }
}
