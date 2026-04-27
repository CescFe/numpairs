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

    @Test
    fun allows_hidden_operators_in_tile_expressions() {
        val tile = Tile(
            expression = Expression(
                leftOperand = Expression.Operand.Known(2),
                operator = Operator.Hidden,
                rightOperand = Expression.Operand.Known(3)
            ),
            result = 999
        )

        assertEquals(Operator.Hidden, tile.expression.operator)
        assertEquals(999, tile.result)
    }

    @Test
    fun assigning_a_left_tile_operand_keeps_result_validation_when_the_expression_becomes_fully_known() {
        val tile = Tile(
            expression = Expression(
                leftOperand = Expression.Operand.Hidden,
                operator = Operator.ADDITION,
                rightOperand = Expression.Operand.Known(3)
            ),
            result = 5
        )

        val updatedTile = tile.withLeftOperand(2)

        assertEquals(Expression.Operand.Known(2), updatedTile.expression.leftOperand)
        assertEquals(5, updatedTile.result)

        assertThrows(IllegalArgumentException::class.java) {
            updatedTile.withLeftOperand(4)
        }
    }

    @Test
    fun assigning_a_right_tile_operand_keeps_result_validation_when_the_expression_becomes_fully_known() {
        val tile = Tile(
            expression = Expression(
                leftOperand = Expression.Operand.Known(2),
                operator = Operator.ADDITION,
                rightOperand = Expression.Operand.Hidden
            ),
            result = 5
        )

        val updatedTile = tile.withRightOperand(3)

        assertEquals(Expression.Operand.Known(3), updatedTile.expression.rightOperand)
        assertEquals(5, updatedTile.result)

        assertThrows(IllegalArgumentException::class.java) {
            updatedTile.withRightOperand(4)
        }
    }

    @Test
    fun assigning_a_tile_operator_keeps_result_validation_when_the_expression_becomes_fully_known() {
        val tile = Tile(
            expression = Expression(
                leftOperand = Expression.Operand.Known(2),
                operator = Operator.Hidden,
                rightOperand = Expression.Operand.Known(3)
            ),
            result = 5
        )

        val updatedTile = tile.withOperator(Operator.ADDITION)

        assertEquals(Operator.ADDITION, updatedTile.expression.operator)
        assertEquals(5, updatedTile.result)

        assertThrows(IllegalArgumentException::class.java) {
            updatedTile.withOperator(Operator.MULTIPLICATION)
        }
    }
}
