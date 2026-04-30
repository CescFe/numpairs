package org.cescfe.numpairs.domain.puzzle

data class VisibleStripEntry(val entryId: Int, val value: Int)

data class NumberUsageByOperator(
    val additionUsageCount: Int = 0,
    val multiplicationUsageCount: Int = 0,
    val provisionalUsageCount: Int = 0
) {
    init {
        require(additionUsageCount >= 0) {
            "Addition usage count cannot be negative."
        }
        require(multiplicationUsageCount >= 0) {
            "Multiplication usage count cannot be negative."
        }
        require(provisionalUsageCount >= 0) {
            "Provisional usage count cannot be negative."
        }
    }

    val additionUsed: Boolean
        get() = additionUsageCount > 0

    val multiplicationUsed: Boolean
        get() = multiplicationUsageCount > 0

    val totalAssignmentCount: Int
        get() = additionUsageCount + multiplicationUsageCount + provisionalUsageCount
}

data class OperandSelectionHint(
    val stripEntry: VisibleStripEntry,
    val usageByOperator: NumberUsageByOperator,
    val isSelectable: Boolean = usageByOperator.totalAssignmentCount < MAX_ASSIGNMENTS_PER_STRIP_ENTRY
) {
    val totalAssignmentCount: Int
        get() = usageByOperator.totalAssignmentCount
}

fun Puzzle.operandSelectionHintsFor(tileIndex: Int, slot: OperandSlot): List<OperandSelectionHint> {
    val usageByEntryId = board.tiles
        .flatMapIndexed { indexedTile, tile -> tile.assignedStripEntries(tileIndex = indexedTile) }
        .filterNot { assignment -> assignment.tileIndex == tileIndex && assignment.slot == slot }
        .groupBy { assignment -> assignment.stripEntryId }
        .mapValues { (_, assignments) ->
            NumberUsageByOperator(
                additionUsageCount = assignments.count { assignment -> assignment.operator == Operator.ADDITION },
                multiplicationUsageCount = assignments.count { assignment ->
                    assignment.operator ==
                        Operator.MULTIPLICATION
                },
                provisionalUsageCount = assignments.count { assignment -> assignment.operator == Operator.Hidden }
            )
        }
    val oppositeSlotEntryId = board.tiles
        .getOrNull(tileIndex)
        ?.assignedStripEntryId(slot.opposite())

    return strip.visibleEntries().map { visibleEntry ->
        val usage = usageByEntryId.getOrDefault(visibleEntry.entryId, NumberUsageByOperator())

        OperandSelectionHint(
            stripEntry = visibleEntry,
            usageByOperator = usage,
            isSelectable = usage.totalAssignmentCount < MAX_ASSIGNMENTS_PER_STRIP_ENTRY &&
                visibleEntry.entryId != oppositeSlotEntryId
        )
    }
}

private data class AssignedStripEntry(
    val tileIndex: Int,
    val slot: OperandSlot,
    val stripEntryId: Int,
    val operator: Operator
)

private fun Tile.assignedStripEntries(tileIndex: Int): List<AssignedStripEntry> = buildList {
    expression.leftOperand.stripEntryId?.let { stripEntryId ->
        add(
            AssignedStripEntry(
                tileIndex = tileIndex,
                slot = OperandSlot.LEFT,
                stripEntryId = stripEntryId,
                operator = expression.operator
            )
        )
    }
    expression.rightOperand.stripEntryId?.let { stripEntryId ->
        add(
            AssignedStripEntry(
                tileIndex = tileIndex,
                slot = OperandSlot.RIGHT,
                stripEntryId = stripEntryId,
                operator = expression.operator
            )
        )
    }
}

private fun Tile.assignedStripEntryId(slot: OperandSlot): Int? = when (slot) {
    OperandSlot.LEFT -> expression.leftOperand.stripEntryId
    OperandSlot.RIGHT -> expression.rightOperand.stripEntryId
}

private val Expression.Operand.stripEntryId: Int?
    get() = when (this) {
        Expression.Operand.Hidden -> null
        is Expression.Operand.Known -> this.stripEntryId
    }

private fun OperandSlot.opposite(): OperandSlot = when (this) {
    OperandSlot.LEFT -> OperandSlot.RIGHT
    OperandSlot.RIGHT -> OperandSlot.LEFT
}

private const val MAX_ASSIGNMENTS_PER_STRIP_ENTRY = 2
