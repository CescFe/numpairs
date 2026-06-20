package org.cescfe.numpairs.domain.puzzle

data class StripEntryUsageByOperator(
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

enum class OperandSelectionAvailability {
    AVAILABLE,
    EXHAUSTED,
    BLOCKED_BY_OPPOSITE_OPERAND
}

data class OperandSelectionChoice(
    val stripEntryId: Int,
    val value: Int,
    val usageByOperator: StripEntryUsageByOperator,
    val availability: OperandSelectionAvailability
) {
    init {
        require(stripEntryId >= 0) {
            "Strip entry id must be non-negative."
        }
        require(value > 0) {
            "Selectable operand values must be positive integers."
        }
    }

    val canBeSelected: Boolean
        get() = availability == OperandSelectionAvailability.AVAILABLE

    val totalAssignmentCount: Int
        get() = usageByOperator.totalAssignmentCount
}

fun Puzzle.operandSelectionChoicesFor(tileIndex: Int, slot: OperandSlot): List<OperandSelectionChoice> {
    val usageByEntryId = board.stripEntryUsageByOperator(
        excludedAssignment = ExcludedAssignedStripEntry(
            tileIndex = tileIndex,
            slot = slot
        )
    )
    val oppositeSlotEntryId = board.tiles
        .getOrNull(tileIndex)
        ?.assignedStripEntryId(slot.opposite())

    return strip.entries.mapNotNull { stripEntry ->
        stripEntry.item.visibleValue?.let { value ->
            val usage = usageByEntryId.getOrDefault(stripEntry.id, StripEntryUsageByOperator())

            OperandSelectionChoice(
                stripEntryId = stripEntry.id,
                value = value,
                usageByOperator = usage,
                availability = when {
                    stripEntry.id == oppositeSlotEntryId -> OperandSelectionAvailability.BLOCKED_BY_OPPOSITE_OPERAND
                    usage.totalAssignmentCount >= MAX_ASSIGNMENTS_PER_STRIP_ENTRY ->
                        OperandSelectionAvailability.EXHAUSTED
                    else -> OperandSelectionAvailability.AVAILABLE
                }
            )
        }
    }
}

fun Puzzle.stripEntryUsageByOperator(): Map<Int, StripEntryUsageByOperator> = board.stripEntryUsageByOperator()

private fun Board.stripEntryUsageByOperator(
    excludedAssignment: ExcludedAssignedStripEntry? = null
): Map<Int, StripEntryUsageByOperator> = tiles
    .flatMapIndexed { indexedTile, tile -> tile.assignedStripEntries(tileIndex = indexedTile) }
    .filterNot { assignment -> excludedAssignment?.matches(assignment) == true }
    .groupBy { assignment -> assignment.stripEntryId }
    .mapValues { (_, assignments) ->
        StripEntryUsageByOperator(
            additionUsageCount = assignments.count { assignment -> assignment.operator == Operator.ADDITION },
            multiplicationUsageCount = assignments.count { assignment ->
                assignment.operator == Operator.MULTIPLICATION
            },
            provisionalUsageCount = assignments.count { assignment -> assignment.operator == Operator.Hidden }
        )
    }

private data class ExcludedAssignedStripEntry(val tileIndex: Int, val slot: OperandSlot) {
    fun matches(assignedStripEntry: AssignedStripEntry): Boolean =
        assignedStripEntry.tileIndex == tileIndex && assignedStripEntry.slot == slot
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

private val StripItem.visibleValue: Int?
    get() = when (this) {
        StripItem.Hidden -> null
        is StripItem.Known -> value
        is StripItem.PlayerEntered -> value
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
