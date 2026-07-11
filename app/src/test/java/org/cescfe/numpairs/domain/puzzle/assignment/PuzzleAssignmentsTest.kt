package org.cescfe.numpairs.domain.puzzle.assignment

import org.cescfe.numpairs.domain.puzzle.model.Operator
import org.junit.Assert.assertEquals
import org.junit.Assert.assertThrows
import org.junit.Test

class PuzzleAssignmentsTest {
    @Test
    fun unordered_strip_entry_pairs_have_a_canonical_identity() {
        assertEquals(
            UnorderedStripEntryPair.of(StripEntryId(2), StripEntryId(5)),
            UnorderedStripEntryPair.of(StripEntryId(5), StripEntryId(2))
        )
    }

    @Test
    fun resolved_operand_assignments_require_non_negative_entry_ids_and_positive_values() {
        assertThrows(IllegalArgumentException::class.java) {
            StripEntryId(-1)
        }

        assertThrows(IllegalArgumentException::class.java) {
            ResolvedOperandAssignment(
                stripEntryId = StripEntryId(0),
                value = 0
            )
        }
    }

    @Test
    fun resolved_tile_assignments_require_a_concrete_operator() {
        assertThrows(IllegalArgumentException::class.java) {
            ResolvedTileAssignment(
                leftOperand = ResolvedOperandAssignment(stripEntryId = StripEntryId(0), value = 1),
                operator = Operator.Hidden,
                rightOperand = ResolvedOperandAssignment(stripEntryId = StripEntryId(1), value = 2)
            )
        }
    }

    @Test
    fun indexed_resolved_tile_assignments_require_non_negative_indexes_and_concrete_operators() {
        val assignment = ResolvedTileAssignment(
            leftOperand = ResolvedOperandAssignment(stripEntryId = StripEntryId(0), value = 1),
            operator = Operator.ADDITION,
            rightOperand = ResolvedOperandAssignment(stripEntryId = StripEntryId(1), value = 2)
        )

        assertThrows(IllegalArgumentException::class.java) {
            IndexedResolvedTileAssignment(
                tileIndex = -1,
                assignment = assignment
            )
        }

        assertThrows(IllegalArgumentException::class.java) {
            IndexedResolvedTileAssignment(
                tileIndex = 0,
                leftOperand = assignment.leftOperand,
                operator = Operator.Hidden,
                rightOperand = assignment.rightOperand
            )
        }
    }
}
