package org.cescfe.numpairs.domain.puzzle

import org.cescfe.numpairs.domain.puzzle.support.hiddenTile
import org.cescfe.numpairs.domain.puzzle.support.puzzleWithRepeatedSixes
import org.cescfe.numpairs.domain.puzzle.support.withTile
import org.cescfe.numpairs.data.puzzle.seed.initialPuzzle
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class OperandSelectionChoiceTest {
    @Test
    fun selection_choices_mark_addition_usage_for_the_matching_strip_entry_only() {
        val puzzle = puzzleWithRepeatedSixes()
            .withTile(
                index = 0,
                tile = hiddenTile(result = 12)
                    .withLeftOperand(value = 6, stripEntryId = 0)
                    .withOperator(Operator.ADDITION)
            )

        assertEquals(
            listOf(
                selectionChoice(entryId = 0, value = 6, additionUsed = true),
                selectionChoice(entryId = 1, value = 6),
                selectionChoice(entryId = 4, value = 25),
                selectionChoice(entryId = 7, value = 222)
            ),
            puzzle.operandSelectionChoicesFor(tileIndex = 1, slot = OperandSlot.LEFT)
        )
    }

    @Test
    fun selection_choices_mark_multiplication_usage_for_the_matching_strip_entry_only() {
        val puzzle = initialPuzzle
            .withTile(
                index = 0,
                tile = hiddenTile(result = 12)
                    .withLeftOperand(value = 6, stripEntryId = 2)
                    .withOperator(Operator.MULTIPLICATION)
            )

        assertEquals(
            listOf(
                selectionChoice(entryId = 2, value = 6, multiplicationUsed = true),
                selectionChoice(entryId = 4, value = 25),
                selectionChoice(entryId = 7, value = 222)
            ),
            puzzle.operandSelectionChoicesFor(tileIndex = 1, slot = OperandSlot.LEFT)
        )
    }

    @Test
    fun selection_choices_mark_both_operator_families_when_the_same_strip_entry_is_reused() {
        val puzzle = initialPuzzle
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
                selectionChoice(entryId = 2, value = 6, additionUsed = true, multiplicationUsed = true),
                selectionChoice(entryId = 4, value = 25),
                selectionChoice(entryId = 7, value = 222)
            ),
            puzzle.operandSelectionChoicesFor(tileIndex = 2, slot = OperandSlot.LEFT)
        )
    }

    @Test
    fun selection_choices_exclude_the_current_editing_slot_assignment_from_usage() {
        val puzzle = initialPuzzle
            .withTile(
                index = 0,
                tile = hiddenTile(result = 12)
                    .withLeftOperand(value = 6, stripEntryId = 2)
                    .withOperator(Operator.ADDITION)
            )

        assertEquals(
            listOf(
                selectionChoice(entryId = 2, value = 6),
                selectionChoice(entryId = 4, value = 25),
                selectionChoice(entryId = 7, value = 222)
            ),
            puzzle.operandSelectionChoicesFor(tileIndex = 0, slot = OperandSlot.LEFT)
        )
    }

    @Test
    fun selection_choices_count_assignments_when_the_operator_is_still_hidden() {
        val puzzle = initialPuzzle
            .withTile(
                index = 0,
                tile = hiddenTile(result = 12)
                    .withLeftOperand(value = 6, stripEntryId = 2)
            )

        assertEquals(
            listOf(
                selectionChoice(entryId = 2, value = 6, provisionalUsed = 1),
                selectionChoice(entryId = 4, value = 25),
                selectionChoice(entryId = 7, value = 222)
            ),
            puzzle.operandSelectionChoicesFor(tileIndex = 1, slot = OperandSlot.LEFT)
        )
    }

    @Test
    fun selection_choices_mark_entries_as_exhausted_when_they_are_already_assigned_twice_elsewhere() {
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

        val choices = puzzle.operandSelectionChoicesFor(tileIndex = 2, slot = OperandSlot.LEFT)
        val exhaustedChoice = choices.first { choice -> choice.stripEntryId == 0 }
        val stillAvailableRepeatedValueChoice = choices.first { choice -> choice.stripEntryId == 1 }

        assertEquals(2, exhaustedChoice.totalAssignmentCount)
        assertEquals(OperandSelectionAvailability.EXHAUSTED, exhaustedChoice.availability)
        assertFalse(exhaustedChoice.canBeSelected)
        assertEquals(0, stillAvailableRepeatedValueChoice.totalAssignmentCount)
        assertEquals(OperandSelectionAvailability.AVAILABLE, stillAvailableRepeatedValueChoice.availability)
        assertTrue(stillAvailableRepeatedValueChoice.canBeSelected)
    }

    @Test
    fun selection_choices_mark_entries_as_exhausted_when_they_are_already_assigned_twice_provisionally() {
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

        val choices = puzzle.operandSelectionChoicesFor(tileIndex = 2, slot = OperandSlot.LEFT)
        val exhaustedChoice = choices.first { choice -> choice.stripEntryId == 0 }
        val stillAvailableRepeatedValueChoice = choices.first { choice -> choice.stripEntryId == 1 }

        assertEquals(2, exhaustedChoice.totalAssignmentCount)
        assertEquals(OperandSelectionAvailability.EXHAUSTED, exhaustedChoice.availability)
        assertFalse(exhaustedChoice.canBeSelected)
        assertEquals(0, exhaustedChoice.usageByOperator.additionUsageCount)
        assertEquals(0, exhaustedChoice.usageByOperator.multiplicationUsageCount)
        assertEquals(0, stillAvailableRepeatedValueChoice.totalAssignmentCount)
        assertEquals(OperandSelectionAvailability.AVAILABLE, stillAvailableRepeatedValueChoice.availability)
        assertTrue(stillAvailableRepeatedValueChoice.canBeSelected)
    }

    @Test
    fun selection_choices_exclude_the_current_slot_assignment_when_reopening_an_already_filled_operand() {
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

        val reopenedChoice = puzzle.operandSelectionChoicesFor(tileIndex = 0, slot = OperandSlot.LEFT)
            .first { choice -> choice.stripEntryId == 0 }

        assertEquals(1, reopenedChoice.usageByOperator.multiplicationUsageCount)
        assertEquals(1, reopenedChoice.totalAssignmentCount)
        assertEquals(OperandSelectionAvailability.AVAILABLE, reopenedChoice.availability)
        assertTrue(reopenedChoice.canBeSelected)
    }

    @Test
    fun selection_choices_exclude_the_current_slot_assignment_when_reopening_a_provisionally_exhausted_operand() {
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

        val reopenedChoice = puzzle.operandSelectionChoicesFor(tileIndex = 0, slot = OperandSlot.LEFT)
            .first { choice -> choice.stripEntryId == 0 }

        assertEquals(0, reopenedChoice.usageByOperator.additionUsageCount)
        assertEquals(0, reopenedChoice.usageByOperator.multiplicationUsageCount)
        assertEquals(1, reopenedChoice.totalAssignmentCount)
        assertEquals(OperandSelectionAvailability.AVAILABLE, reopenedChoice.availability)
        assertTrue(reopenedChoice.canBeSelected)
    }

    @Test
    fun selection_choices_mark_the_opposite_slot_strip_entry_as_blocked_for_the_current_tile() {
        val puzzle = initialPuzzle
            .withTile(
                index = 0,
                tile = hiddenTile(result = 12)
                    .withLeftOperand(value = 6, stripEntryId = 2)
            )

        val blockedChoice = puzzle.operandSelectionChoicesFor(tileIndex = 0, slot = OperandSlot.RIGHT)
            .first { choice -> choice.stripEntryId == 2 }

        assertEquals(1, blockedChoice.totalAssignmentCount)
        assertEquals(OperandSelectionAvailability.BLOCKED_BY_OPPOSITE_OPERAND, blockedChoice.availability)
        assertFalse(blockedChoice.canBeSelected)
    }

    @Test
    fun selection_choices_keep_the_current_slot_assignment_available_when_the_opposite_slot_uses_another_entry() {
        val puzzle = initialPuzzle
            .withTile(
                index = 0,
                tile = hiddenTile(result = 31)
                    .withLeftOperand(value = 6, stripEntryId = 2)
                    .withRightOperand(value = 25, stripEntryId = 4)
            )

        val choices = puzzle.operandSelectionChoicesFor(tileIndex = 0, slot = OperandSlot.LEFT)
        val currentSlotChoice = choices.first { choice -> choice.stripEntryId == 2 }
        val oppositeSlotChoice = choices.first { choice -> choice.stripEntryId == 4 }

        assertEquals(OperandSelectionAvailability.AVAILABLE, currentSlotChoice.availability)
        assertTrue(currentSlotChoice.canBeSelected)
        assertEquals(OperandSelectionAvailability.BLOCKED_BY_OPPOSITE_OPERAND, oppositeSlotChoice.availability)
        assertFalse(oppositeSlotChoice.canBeSelected)
    }
}

private fun selectionChoice(
    entryId: Int,
    value: Int,
    additionUsed: Boolean = false,
    multiplicationUsed: Boolean = false,
    provisionalUsed: Int = 0,
    availability: OperandSelectionAvailability? = null
): OperandSelectionChoice = OperandSelectionChoice(
    stripEntryId = entryId,
    value = value,
    usageByOperator = StripEntryUsageByOperator(
        additionUsageCount = if (additionUsed) 1 else 0,
        multiplicationUsageCount = if (multiplicationUsed) 1 else 0,
        provisionalUsageCount = provisionalUsed
    ),
    availability = availability ?: if (
        listOf(additionUsed, multiplicationUsed).count { used -> used } + provisionalUsed < 2
    ) {
        OperandSelectionAvailability.AVAILABLE
    } else {
        OperandSelectionAvailability.EXHAUSTED
    }
)
