package org.cescfe.numpairs.domain.puzzle

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class OperandSelectionHintTest {
    @Test
    fun selection_hints_mark_addition_usage_for_the_matching_strip_entry_only() {
        val puzzle = puzzleWithRepeatedSixes()
            .withTile(
                index = 0,
                tile = hiddenTile(result = 12)
                    .withLeftOperand(value = 6, stripEntryId = 0)
                    .withOperator(Operator.ADDITION)
            )

        assertEquals(
            listOf(
                selectionHint(entryId = 0, value = 6, additionUsed = true),
                selectionHint(entryId = 1, value = 6),
                selectionHint(entryId = 4, value = 25),
                selectionHint(entryId = 7, value = 222)
            ),
            puzzle.operandSelectionHintsFor(tileIndex = 1, slot = OperandSlot.LEFT)
        )
    }

    @Test
    fun selection_hints_mark_multiplication_usage_for_the_matching_strip_entry_only() {
        val puzzle = PuzzleSamples.prototype
            .withTile(
                index = 0,
                tile = hiddenTile(result = 12)
                    .withLeftOperand(value = 6, stripEntryId = 2)
                    .withOperator(Operator.MULTIPLICATION)
            )

        assertEquals(
            listOf(
                selectionHint(entryId = 2, value = 6, multiplicationUsed = true),
                selectionHint(entryId = 4, value = 25),
                selectionHint(entryId = 7, value = 222)
            ),
            puzzle.operandSelectionHintsFor(tileIndex = 1, slot = OperandSlot.LEFT)
        )
    }

    @Test
    fun selection_hints_mark_both_operator_families_when_the_same_strip_entry_is_reused() {
        val puzzle = PuzzleSamples.prototype
            .withTile(
                index = 0,
                tile = hiddenTile(result = 12)
                    .withLeftOperand(value = 6, stripEntryId = 2)
                    .withOperator(Operator.ADDITION)
            )
            .withTile(
                index = 1,
                tile = hiddenTile(result = 36)
                    .withLeftOperand(value = 6, stripEntryId = 2)
                    .withOperator(Operator.MULTIPLICATION)
            )

        assertEquals(
            listOf(
                selectionHint(entryId = 2, value = 6, additionUsed = true, multiplicationUsed = true),
                selectionHint(entryId = 4, value = 25),
                selectionHint(entryId = 7, value = 222)
            ),
            puzzle.operandSelectionHintsFor(tileIndex = 2, slot = OperandSlot.LEFT)
        )
    }

    @Test
    fun selection_hints_exclude_the_current_editing_slot_assignment_from_usage() {
        val puzzle = PuzzleSamples.prototype
            .withTile(
                index = 0,
                tile = hiddenTile(result = 12)
                    .withLeftOperand(value = 6, stripEntryId = 2)
                    .withOperator(Operator.ADDITION)
            )

        assertEquals(
            listOf(
                selectionHint(entryId = 2, value = 6),
                selectionHint(entryId = 4, value = 25),
                selectionHint(entryId = 7, value = 222)
            ),
            puzzle.operandSelectionHintsFor(tileIndex = 0, slot = OperandSlot.LEFT)
        )
    }

    @Test
    fun selection_hints_count_assignments_when_the_operator_is_still_hidden() {
        val puzzle = PuzzleSamples.prototype
            .withTile(
                index = 0,
                tile = hiddenTile(result = 12)
                    .withLeftOperand(value = 6, stripEntryId = 2)
            )

        assertEquals(
            listOf(
                selectionHint(entryId = 2, value = 6, provisionalUsed = 1),
                selectionHint(entryId = 4, value = 25),
                selectionHint(entryId = 7, value = 222)
            ),
            puzzle.operandSelectionHintsFor(tileIndex = 1, slot = OperandSlot.LEFT)
        )
    }

    @Test
    fun selection_hints_mark_entries_as_unavailable_when_they_are_already_assigned_twice_elsewhere() {
        val puzzle = puzzleWithRepeatedSixes()
            .withTile(
                index = 0,
                tile = hiddenTile(result = 12)
                    .withLeftOperand(value = 6, stripEntryId = 0)
                    .withOperator(Operator.ADDITION)
            )
            .withTile(
                index = 1,
                tile = hiddenTile(result = 36)
                    .withLeftOperand(value = 6, stripEntryId = 0)
                    .withOperator(Operator.MULTIPLICATION)
            )

        val hints = puzzle.operandSelectionHintsFor(tileIndex = 2, slot = OperandSlot.LEFT)
        val exhaustedHint = hints.first { hint -> hint.stripEntry.entryId == 0 }
        val stillAvailableRepeatedValueHint = hints.first { hint -> hint.stripEntry.entryId == 1 }

        assertEquals(2, exhaustedHint.totalAssignmentCount)
        assertFalse(exhaustedHint.isSelectable)
        assertEquals(0, stillAvailableRepeatedValueHint.totalAssignmentCount)
        assertTrue(stillAvailableRepeatedValueHint.isSelectable)
    }

    @Test
    fun selection_hints_mark_entries_as_unavailable_when_they_are_already_assigned_twice_provisionally() {
        val puzzle = puzzleWithRepeatedSixes()
            .withTile(
                index = 0,
                tile = hiddenTile(result = 12)
                    .withLeftOperand(value = 6, stripEntryId = 0)
            )
            .withTile(
                index = 1,
                tile = hiddenTile(result = 36)
                    .withLeftOperand(value = 6, stripEntryId = 0)
            )

        val hints = puzzle.operandSelectionHintsFor(tileIndex = 2, slot = OperandSlot.LEFT)
        val exhaustedHint = hints.first { hint -> hint.stripEntry.entryId == 0 }
        val stillAvailableRepeatedValueHint = hints.first { hint -> hint.stripEntry.entryId == 1 }

        assertEquals(2, exhaustedHint.totalAssignmentCount)
        assertFalse(exhaustedHint.isSelectable)
        assertEquals(0, exhaustedHint.usageByOperator.additionUsageCount)
        assertEquals(0, exhaustedHint.usageByOperator.multiplicationUsageCount)
        assertEquals(0, stillAvailableRepeatedValueHint.totalAssignmentCount)
        assertTrue(stillAvailableRepeatedValueHint.isSelectable)
    }

    @Test
    fun selection_hints_exclude_the_current_slot_assignment_when_reopening_an_already_filled_operand() {
        val puzzle = puzzleWithRepeatedSixes()
            .withTile(
                index = 0,
                tile = hiddenTile(result = 12)
                    .withLeftOperand(value = 6, stripEntryId = 0)
                    .withOperator(Operator.ADDITION)
            )
            .withTile(
                index = 1,
                tile = hiddenTile(result = 36)
                    .withLeftOperand(value = 6, stripEntryId = 0)
                    .withOperator(Operator.MULTIPLICATION)
            )

        val reopenedHint = puzzle.operandSelectionHintsFor(tileIndex = 0, slot = OperandSlot.LEFT)
            .first { hint -> hint.stripEntry.entryId == 0 }

        assertEquals(1, reopenedHint.usageByOperator.multiplicationUsageCount)
        assertEquals(1, reopenedHint.totalAssignmentCount)
        assertTrue(reopenedHint.isSelectable)
    }

    @Test
    fun selection_hints_exclude_the_current_slot_assignment_when_reopening_a_provisionally_exhausted_operand() {
        val puzzle = puzzleWithRepeatedSixes()
            .withTile(
                index = 0,
                tile = hiddenTile(result = 12)
                    .withLeftOperand(value = 6, stripEntryId = 0)
            )
            .withTile(
                index = 1,
                tile = hiddenTile(result = 36)
                    .withLeftOperand(value = 6, stripEntryId = 0)
            )

        val reopenedHint = puzzle.operandSelectionHintsFor(tileIndex = 0, slot = OperandSlot.LEFT)
            .first { hint -> hint.stripEntry.entryId == 0 }

        assertEquals(0, reopenedHint.usageByOperator.additionUsageCount)
        assertEquals(0, reopenedHint.usageByOperator.multiplicationUsageCount)
        assertEquals(1, reopenedHint.totalAssignmentCount)
        assertTrue(reopenedHint.isSelectable)
    }

    @Test
    fun selection_hints_mark_the_opposite_slot_strip_entry_as_unavailable_for_the_current_tile() {
        val puzzle = PuzzleSamples.prototype
            .withTile(
                index = 0,
                tile = hiddenTile(result = 12)
                    .withLeftOperand(value = 6, stripEntryId = 2)
            )

        val blockedHint = puzzle.operandSelectionHintsFor(tileIndex = 0, slot = OperandSlot.RIGHT)
            .first { hint -> hint.stripEntry.entryId == 2 }

        assertEquals(1, blockedHint.totalAssignmentCount)
        assertFalse(blockedHint.isSelectable)
    }

    @Test
    fun selection_hints_keep_the_current_slot_assignment_selectable_when_the_opposite_slot_uses_another_entry() {
        val puzzle = PuzzleSamples.prototype
            .withTile(
                index = 0,
                tile = hiddenTile(result = 31)
                    .withLeftOperand(value = 6, stripEntryId = 2)
                    .withRightOperand(value = 25, stripEntryId = 4)
            )

        val hints = puzzle.operandSelectionHintsFor(tileIndex = 0, slot = OperandSlot.LEFT)
        val currentSlotHint = hints.first { hint -> hint.stripEntry.entryId == 2 }
        val oppositeSlotHint = hints.first { hint -> hint.stripEntry.entryId == 4 }

        assertTrue(currentSlotHint.isSelectable)
        assertFalse(oppositeSlotHint.isSelectable)
    }
}

private fun selectionHint(
    entryId: Int,
    value: Int,
    additionUsed: Boolean = false,
    multiplicationUsed: Boolean = false,
    provisionalUsed: Int = 0,
    isSelectable: Boolean? = null
): OperandSelectionHint = OperandSelectionHint(
    stripEntry = VisibleStripEntry(
        entryId = entryId,
        value = value
    ),
    usageByOperator = NumberUsageByOperator(
        additionUsageCount = if (additionUsed) 1 else 0,
        multiplicationUsageCount = if (multiplicationUsed) 1 else 0,
        provisionalUsageCount = provisionalUsed
    ),
    isSelectable = isSelectable ?: (listOf(additionUsed, multiplicationUsed).count { used -> used } + provisionalUsed < 2)
)

private fun puzzleWithRepeatedSixes(): Puzzle = PuzzleSamples.prototype.copy(
    strip = Strip.fromItems(
        items = listOf(
            StripItem.Known(6),
            StripItem.Known(6),
            StripItem.Hidden,
            StripItem.Hidden,
            StripItem.Known(25),
            StripItem.Hidden,
            StripItem.Hidden,
            StripItem.Known(222)
        )
    )
)

private fun Puzzle.withTile(index: Int, tile: Tile): Puzzle = copy(
    board = Board(
        tiles = board.tiles.toMutableList().apply {
            set(index, tile)
        }
    )
)

private fun hiddenTile(result: Int): Tile = Tile(
    expression = Expression(
        leftOperand = Expression.Operand.Hidden,
        operator = Operator.Hidden,
        rightOperand = Expression.Operand.Hidden
    ),
    result = result
)
