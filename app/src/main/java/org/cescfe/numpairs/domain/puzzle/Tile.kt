package org.cescfe.numpairs.domain.puzzle

data class Tile(val expression: Expression, val result: Int) {
    init {
        require(!expression.isFullyKnown || result == expression.evaluate()) {
            "Tile result must match the expression evaluation when all operands are known."
        }
    }
}
