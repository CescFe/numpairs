package org.cescfe.numpairs.domain.puzzle

data class Tile(val expression: Expression, val result: Int) {
    val canReset: Boolean
        get() = expression.leftOperand is Expression.Operand.Known ||
            expression.operator != Operator.Hidden ||
            expression.rightOperand is Expression.Operand.Known

    val resolutionState: TileResolutionState
        get() = when {
            !expression.isFullyKnown -> TileResolutionState.UNRESOLVED
            expression.evaluate() == result -> TileResolutionState.CORRECT
            else -> TileResolutionState.INCORRECT
        }

    fun withLeftOperand(value: Int, stripEntryId: Int? = null): Tile = copy(
        expression = expression.withLeftOperand(value = value, stripEntryId = stripEntryId)
    )

    fun withRightOperand(value: Int, stripEntryId: Int? = null): Tile = copy(
        expression = expression.withRightOperand(value = value, stripEntryId = stripEntryId)
    )

    fun withOperator(operator: Operator): Tile = copy(
        expression = expression.withOperator(operator)
    )

    fun reset(): Tile = copy(
        expression = Expression(
            leftOperand = Expression.Operand.Hidden,
            operator = Operator.Hidden,
            rightOperand = Expression.Operand.Hidden
        )
    )
}

enum class TileResolutionState {
    UNRESOLVED,
    CORRECT,
    INCORRECT
}
