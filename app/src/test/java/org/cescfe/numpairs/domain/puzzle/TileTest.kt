package org.cescfe.numpairs.domain.puzzle

import org.junit.Assert.assertEquals
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

    @Test
    fun allows_hidden_operands_in_tile_expressions() {
        val tile = Tile(
            expression = Expression(
                leftOperand = Expression.Operand.Hidden,
                operator = Operator.MULTIPLICATION,
                rightOperand = Expression.Operand.Known(3)
            ),
            result = 6
        )

        assertEquals(Expression.Operand.Hidden, tile.expression.leftOperand)
        assertEquals(6, tile.result)
    }
}
