package org.cescfe.numpairs.domain.puzzle

import org.cescfe.numpairs.domain.puzzle.support.TileAssignment
import org.cescfe.numpairs.domain.puzzle.support.defaultKnownStripValues
import org.cescfe.numpairs.domain.puzzle.support.knownPuzzleWithAssignments
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class SumProductPairingTest {
    @Test
    fun matching_sum_and_product_pairs_do_not_report_a_mismatch_when_operand_order_differs() {
        val puzzle = knownPuzzleWithAssignments(
            stripValues = defaultKnownStripValues(),
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
        assertEquals(emptyList<Int>(), puzzle.mismatchedSumProductPairingTileIndexes)
    }

    @Test
    fun mismatched_sum_and_product_pairs_report_an_invalid_pairing_state_even_when_all_tiles_are_locally_correct() {
        val puzzle = knownPuzzleWithAssignments(
            stripValues = defaultKnownStripValues(),
            TileAssignment(leftEntryId = 0, operator = Operator.ADDITION, rightEntryId = 1),
            TileAssignment(leftEntryId = 0, operator = Operator.MULTIPLICATION, rightEntryId = 2),
            TileAssignment(leftEntryId = 2, operator = Operator.ADDITION, rightEntryId = 3),
            TileAssignment(leftEntryId = 1, operator = Operator.MULTIPLICATION, rightEntryId = 3),
            TileAssignment(leftEntryId = 4, operator = Operator.ADDITION, rightEntryId = 5),
            TileAssignment(leftEntryId = 5, operator = Operator.MULTIPLICATION, rightEntryId = 4),
            TileAssignment(leftEntryId = 6, operator = Operator.ADDITION, rightEntryId = 7),
            TileAssignment(leftEntryId = 7, operator = Operator.MULTIPLICATION, rightEntryId = 6)
        )

        assertTrue(puzzle.board.tiles.all { tile -> tile.resolutionState == TileResolutionState.CORRECT })
        assertTrue(puzzle.hasMismatchedSumProductPairings)
        assertEquals(listOf(0, 1, 2, 3), puzzle.mismatchedSumProductPairingTileIndexes)
    }

    @Test
    fun incomplete_puzzles_do_not_report_implicated_pairing_tiles() {
        val puzzle = knownPuzzleWithAssignments(
            stripValues = defaultKnownStripValues(),
            TileAssignment(leftEntryId = 0, operator = Operator.ADDITION, rightEntryId = 1),
            TileAssignment(leftEntryId = 1, operator = Operator.MULTIPLICATION, rightEntryId = 0),
            TileAssignment(leftEntryId = 2, operator = Operator.ADDITION, rightEntryId = 3),
            TileAssignment(leftEntryId = 3, operator = Operator.MULTIPLICATION, rightEntryId = 2),
            TileAssignment(leftEntryId = 4, operator = Operator.ADDITION, rightEntryId = 5),
            TileAssignment(leftEntryId = 5, operator = Operator.MULTIPLICATION, rightEntryId = 4),
            TileAssignment(leftEntryId = 6, operator = Operator.ADDITION, rightEntryId = 7),
            TileAssignment(leftEntryId = 7, operator = Operator.MULTIPLICATION, rightEntryId = 6)
        ).copy(
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

        assertFalse(puzzle.hasMismatchedSumProductPairings)
        assertEquals(emptyList<Int>(), puzzle.mismatchedSumProductPairingTileIndexes)
    }
}
