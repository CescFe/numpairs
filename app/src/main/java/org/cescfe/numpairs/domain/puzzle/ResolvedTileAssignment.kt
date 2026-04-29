package org.cescfe.numpairs.domain.puzzle

data class ResolvedOperandAssignment(val stripEntryId: Int, val value: Int) {
    init {
        require(stripEntryId >= 0) {
            "Strip entry id must be non-negative."
        }
        require(value > 0) {
            "Resolved operand value must be a positive integer."
        }
    }
}

data class ResolvedTileAssignment(
    val leftOperand: ResolvedOperandAssignment,
    val operator: Operator,
    val rightOperand: ResolvedOperandAssignment
) {
    init {
        require(operator != Operator.Hidden) {
            "Resolved tile assignments require a concrete operator."
        }
    }
}

data class IndexedResolvedTileAssignment(
    val tileIndex: Int,
    val leftOperand: ResolvedOperandAssignment,
    val operator: Operator,
    val rightOperand: ResolvedOperandAssignment
) {
    init {
        require(tileIndex >= 0) {
            "Tile index must be non-negative."
        }
        require(operator != Operator.Hidden) {
            "Indexed resolved tile assignments require a concrete operator."
        }
    }

    constructor(tileIndex: Int, assignment: ResolvedTileAssignment) : this(
        tileIndex = tileIndex,
        leftOperand = assignment.leftOperand,
        operator = assignment.operator,
        rightOperand = assignment.rightOperand
    )
}

fun Tile.resolvedStripEntryAssignment(): ResolvedTileAssignment? {
    val leftOperandAssignment = expression.leftOperand.resolvedOperandAssignment ?: return null
    val rightOperandAssignment = expression.rightOperand.resolvedOperandAssignment ?: return null
    val concreteOperator = expression.operator.takeUnless { it == Operator.Hidden } ?: return null

    return ResolvedTileAssignment(
        leftOperand = leftOperandAssignment,
        operator = concreteOperator,
        rightOperand = rightOperandAssignment
    )
}

fun Board.resolvedTileAssignments(): List<IndexedResolvedTileAssignment> = tiles.mapIndexedNotNull { tileIndex, tile ->
    tile.resolvedStripEntryAssignment()?.let { assignment ->
        IndexedResolvedTileAssignment(
            tileIndex = tileIndex,
            assignment = assignment
        )
    }
}

fun Puzzle.resolvedTileAssignments(): List<IndexedResolvedTileAssignment> = board.resolvedTileAssignments()

private val Expression.Operand.resolvedOperandAssignment: ResolvedOperandAssignment?
    get() = when (this) {
        Expression.Operand.Hidden -> null
        is Expression.Operand.Known -> stripEntryId?.let { resolvedStripEntryId ->
            ResolvedOperandAssignment(
                stripEntryId = resolvedStripEntryId,
                value = value
            )
        }
    }
