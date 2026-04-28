package org.cescfe.numpairs.domain.puzzle

data class Tile(val expression: Expression, val result: Int) {
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
}

enum class TileResolutionState {
    UNRESOLVED,
    CORRECT,
    INCORRECT
}
