package org.cescfe.numpairs.domain.puzzle

data class Tile(val expression: Expression, val result: Int) {
    val resolutionState: TileResolutionState
        get() = when {
            !expression.isFullyKnown -> TileResolutionState.UNRESOLVED
            expression.evaluate() == result -> TileResolutionState.CORRECT
            else -> TileResolutionState.INCORRECT
        }

    fun withLeftOperand(value: Int): Tile = copy(
        expression = expression.withLeftOperand(value)
    )

    fun withRightOperand(value: Int): Tile = copy(
        expression = expression.withRightOperand(value)
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
