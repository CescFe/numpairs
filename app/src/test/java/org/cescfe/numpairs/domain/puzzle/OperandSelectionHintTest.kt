package org.cescfe.numpairs.domain.puzzle

import org.junit.Assert.assertEquals
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
    fun selection_hints_track_assignments_with_hidden_operators_as_unresolved_usage() {
        val puzzle = PuzzleSamples.prototype
            .withTile(
                index = 0,
                tile = hiddenTile(result = 12)
                    .withLeftOperand(value = 6, stripEntryId = 2)
            )

        assertEquals(
            listOf(
                selectionHint(entryId = 2, value = 6, hasUnresolvedUsage = true),
                selectionHint(entryId = 4, value = 25),
                selectionHint(entryId = 7, value = 222)
            ),
            puzzle.operandSelectionHintsFor(tileIndex = 1, slot = OperandSlot.LEFT)
        )
    }
}

private fun selectionHint(
    entryId: Int,
    value: Int,
    additionUsed: Boolean = false,
    multiplicationUsed: Boolean = false,
    hasUnresolvedUsage: Boolean = false
): OperandSelectionHint = OperandSelectionHint(
    stripEntry = VisibleStripEntry(
        entryId = entryId,
        stripIndex = entryId,
        value = value
    ),
    usageByOperator = NumberUsageByOperator(
        additionUsageCount = if (additionUsed) 1 else 0,
        multiplicationUsageCount = if (multiplicationUsed) 1 else 0,
        unresolvedUsageCount = if (hasUnresolvedUsage) 1 else 0
    )
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
