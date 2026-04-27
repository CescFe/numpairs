package org.cescfe.numpairs.domain.puzzle

import org.junit.Assert.assertEquals
import org.junit.Test

class TileTest {
    @Test
    fun fully_known_correct_expressions_are_reported_as_correct() {
        val tile = Tile(
            expression = Expression(
                leftOperand = 2,
                operator = Operator.MULTIPLICATION,
                rightOperand = 3
            ),
            result = 6
        )

        assertEquals(TileResolutionState.CORRECT, tile.resolutionState)
    }

    @Test
    fun fully_known_incorrect_expressions_are_reported_as_incorrect() {
        val tile = Tile(
            expression = Expression(
                leftOperand = 2,
                operator = Operator.MULTIPLICATION,
                rightOperand = 3
            ),
            result = 5
        )

        assertEquals(TileResolutionState.INCORRECT, tile.resolutionState)
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
        assertEquals(TileResolutionState.UNRESOLVED, tile.resolutionState)
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
        assertEquals(TileResolutionState.UNRESOLVED, tile.resolutionState)
    }

    @Test
    fun assigning_a_left_tile_operand_can_transition_from_unresolved_to_correct() {
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
        assertEquals(TileResolutionState.CORRECT, updatedTile.resolutionState)
    }

    @Test
    fun assigning_a_left_tile_operand_can_transition_from_correct_to_incorrect() {
        val tile = Tile(
            expression = Expression(
                leftOperand = Expression.Operand.Hidden,
                operator = Operator.ADDITION,
                rightOperand = Expression.Operand.Known(3)
            ),
            result = 5
        ).withLeftOperand(2)

        val updatedTile = tile.withLeftOperand(4)

        assertEquals(Expression.Operand.Known(4), updatedTile.expression.leftOperand)
        assertEquals(TileResolutionState.INCORRECT, updatedTile.resolutionState)
    }

    @Test
    fun assigning_a_right_tile_operand_can_transition_from_unresolved_to_correct() {
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
        assertEquals(TileResolutionState.CORRECT, updatedTile.resolutionState)
    }

    @Test
    fun assigning_a_right_tile_operand_can_transition_from_correct_to_incorrect() {
        val tile = Tile(
            expression = Expression(
                leftOperand = Expression.Operand.Known(2),
                operator = Operator.ADDITION,
                rightOperand = Expression.Operand.Hidden
            ),
            result = 5
        ).withRightOperand(3)

        val updatedTile = tile.withRightOperand(4)

        assertEquals(Expression.Operand.Known(4), updatedTile.expression.rightOperand)
        assertEquals(TileResolutionState.INCORRECT, updatedTile.resolutionState)
    }

    @Test
    fun assigning_a_tile_operator_can_transition_from_unresolved_to_correct() {
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
        assertEquals(TileResolutionState.CORRECT, updatedTile.resolutionState)
    }

    @Test
    fun assigning_a_tile_operator_can_transition_from_incorrect_to_correct() {
        val tile = Tile(
            expression = Expression(
                leftOperand = Expression.Operand.Known(2),
                operator = Operator.Hidden,
                rightOperand = Expression.Operand.Known(3)
            ),
            result = 6
        ).withOperator(Operator.ADDITION)

        assertEquals(TileResolutionState.INCORRECT, tile.resolutionState)

        val updatedTile = tile.withOperator(Operator.MULTIPLICATION)

        assertEquals(Operator.MULTIPLICATION, updatedTile.expression.operator)
        assertEquals(TileResolutionState.CORRECT, updatedTile.resolutionState)
    }
}
