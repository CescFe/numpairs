package org.cescfe.numpairs.domain.puzzle

data class Tile(
    val expression: Expression,
    val result: Int
) {
    init {
        require(result == expression.evaluate()) {
            "Tile result must match the expression evaluation."
        }
    }
}
