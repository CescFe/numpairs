package org.cescfe.numpairs.domain.puzzle

data class Tile(val expression: Expression, val result: Int) {
    init {
        require(!expression.isFullyKnown || result == expression.evaluate()) {
            "Tile result must match the expression evaluation when the expression is fully known."
        }
    }

    fun withOperator(operator: Operator): Tile = copy(
        expression = expression.withOperator(operator)
    )
}
