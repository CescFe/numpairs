package org.cescfe.numpairs.domain.puzzle.assignment

import org.cescfe.numpairs.domain.puzzle.model.Board
import org.cescfe.numpairs.domain.puzzle.model.Operator
import org.cescfe.numpairs.domain.puzzle.support.assignedTile
import org.cescfe.numpairs.domain.puzzle.support.hiddenTile
import org.cescfe.numpairs.domain.puzzle.support.puzzleWithRepeatedSixes
import org.cescfe.numpairs.domain.puzzle.support.withTile
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class ResolvedTileAssignmentTest {
    @Test
    fun tile_exposes_resolved_strip_entry_assignment_with_operator_and_stable_entry_ids() {
        val tile = hiddenTile(result = 31)
            .withLeftOperand(value = 6, stripEntryId = 2)
            .withOperator(Operator.ADDITION)
            .withRightOperand(value = 25, stripEntryId = 4)

        assertEquals(
            ResolvedTileAssignment(
                leftOperand = ResolvedOperandAssignment(stripEntryId = 2, value = 6),
                operator = Operator.ADDITION,
                rightOperand = ResolvedOperandAssignment(stripEntryId = 4, value = 25)
            ),
            tile.resolvedStripEntryAssignment()
        )
    }

    @Test
    fun tile_does_not_expose_a_resolved_assignment_when_it_is_unresolved_or_lacks_strip_identity() {
        val unresolvedTile = hiddenTile(result = 31)
            .withLeftOperand(value = 6, stripEntryId = 2)
            .withOperator(Operator.ADDITION)
        val identityLessTile = hiddenTile(result = 31)
            .withLeftOperand(value = 6, stripEntryId = 2)
            .withOperator(Operator.ADDITION)
            .withRightOperand(value = 25)

        assertNull(unresolvedTile.resolvedStripEntryAssignment())
        assertNull(identityLessTile.resolvedStripEntryAssignment())
    }

    @Test
    fun board_exposes_indexed_resolved_assignments_only_for_resolved_tiles() {
        val board = Board(
            tiles = listOf(
                assignedTile(
                    leftEntryId = 2,
                    leftValue = 6,
                    operator = Operator.ADDITION,
                    rightEntryId = 4,
                    rightValue = 25,
                    result = 31
                ),
                hiddenTile(result = 223),
                hiddenTile(result = 222),
                hiddenTile(result = 52),
                hiddenTile(result = 100),
                assignedTile(
                    leftEntryId = 2,
                    leftValue = 6,
                    operator = Operator.MULTIPLICATION,
                    rightEntryId = 4,
                    rightValue = 25,
                    result = 150
                ),
                hiddenTile(result = 35),
                hiddenTile(result = 250)
            )
        )

        assertEquals(
            listOf(
                IndexedResolvedTileAssignment(
                    tileIndex = 0,
                    leftOperand = ResolvedOperandAssignment(stripEntryId = 2, value = 6),
                    operator = Operator.ADDITION,
                    rightOperand = ResolvedOperandAssignment(stripEntryId = 4, value = 25)
                ),
                IndexedResolvedTileAssignment(
                    tileIndex = 5,
                    leftOperand = ResolvedOperandAssignment(stripEntryId = 2, value = 6),
                    operator = Operator.MULTIPLICATION,
                    rightOperand = ResolvedOperandAssignment(stripEntryId = 4, value = 25)
                )
            ),
            board.resolvedTileAssignments()
        )
    }

    @Test
    fun puzzle_resolved_assignments_preserve_distinct_entry_ids_for_repeated_values() {
        val puzzle = puzzleWithRepeatedSixes()
            .withTile(
                index = 0,
                tile = hiddenTile(result = 12)
                    .withLeftOperand(value = 6, stripEntryId = 0)
                    .withOperator(Operator.ADDITION)
                    .withRightOperand(value = 6, stripEntryId = 1)
            )
            .withTile(
                index = 1,
                tile = hiddenTile(result = 36)
                    .withLeftOperand(value = 6, stripEntryId = 1)
                    .withOperator(Operator.MULTIPLICATION)
                    .withRightOperand(value = 6, stripEntryId = 0)
            )

        assertEquals(
            listOf(
                IndexedResolvedTileAssignment(
                    tileIndex = 0,
                    leftOperand = ResolvedOperandAssignment(stripEntryId = 0, value = 6),
                    operator = Operator.ADDITION,
                    rightOperand = ResolvedOperandAssignment(stripEntryId = 1, value = 6)
                ),
                IndexedResolvedTileAssignment(
                    tileIndex = 1,
                    leftOperand = ResolvedOperandAssignment(stripEntryId = 1, value = 6),
                    operator = Operator.MULTIPLICATION,
                    rightOperand = ResolvedOperandAssignment(stripEntryId = 0, value = 6)
                )
            ),
            puzzle.resolvedTileAssignments()
        )
    }
}
