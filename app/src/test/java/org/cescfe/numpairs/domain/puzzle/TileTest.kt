package org.cescfe.numpairs.domain.puzzle

import org.junit.Assert.assertThrows
import org.junit.Test

class TileTest {
    @Test
    fun requires_result_to_match_expression_evaluation() {
        assertThrows(IllegalArgumentException::class.java) {
            Tile(
                expression = Expression(
                    leftOperand = 2,
                    operator = Operator.MULTIPLICATION,
                    rightOperand = 3
                ),
                result = 5
            )
        }
    }
}
