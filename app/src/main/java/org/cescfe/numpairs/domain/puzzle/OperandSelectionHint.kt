package org.cescfe.numpairs.domain.puzzle

data class VisibleStripEntry(
    val entryId: Int,
    val stripIndex: Int,
    val value: Int
)

data class NumberUsageByOperator(
    val additionUsageCount: Int = 0,
    val multiplicationUsageCount: Int = 0,
    val unresolvedUsageCount: Int = 0
) {
    init {
        require(additionUsageCount >= 0) {
            "Addition usage count cannot be negative."
        }
        require(multiplicationUsageCount >= 0) {
            "Multiplication usage count cannot be negative."
        }
        require(unresolvedUsageCount >= 0) {
            "Unresolved usage count cannot be negative."
        }
    }

    val additionUsed: Boolean
        get() = additionUsageCount > 0

    val multiplicationUsed: Boolean
        get() = multiplicationUsageCount > 0

    val hasUnresolvedUsage: Boolean
        get() = unresolvedUsageCount > 0
}

data class OperandSelectionHint(
    val stripEntry: VisibleStripEntry,
    val usageByOperator: NumberUsageByOperator
)

fun Puzzle.operandSelectionHintsFor(tileIndex: Int, slot: OperandSlot): List<OperandSelectionHint> {
    val usageByEntryId = board.tiles
        .flatMapIndexed { indexedTile, tile -> tile.assignedStripEntries(tileIndex = indexedTile) }
        .filterNot { assignment -> assignment.tileIndex == tileIndex && assignment.slot == slot }
        .groupBy { assignment -> assignment.stripEntryId }
        .mapValues { (_, assignments) ->
            NumberUsageByOperator(
                additionUsageCount = assignments.count { assignment -> assignment.operator == Operator.ADDITION },
                multiplicationUsageCount = assignments.count { assignment -> assignment.operator == Operator.MULTIPLICATION },
                unresolvedUsageCount = assignments.count { assignment -> assignment.operator == Operator.Hidden }
            )
        }

    return strip.visibleEntries().map { visibleEntry ->
        OperandSelectionHint(
            stripEntry = visibleEntry,
            usageByOperator = usageByEntryId.getOrDefault(visibleEntry.entryId, NumberUsageByOperator())
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

private val Expression.Operand.stripEntryId: Int?
    get() = when (this) {
        Expression.Operand.Hidden -> null
        is Expression.Operand.Known -> this.stripEntryId
    }
