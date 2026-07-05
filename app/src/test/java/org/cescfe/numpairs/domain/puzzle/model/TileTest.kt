package org.cescfe.numpairs.domain.puzzle.model

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class TileTest {
    @Test
    fun fully_known_expressions_report_correct_and_incorrect_resolution_states() {
        val correctTile = Tile(
            expression = Expression(
                leftOperand = 2,
                operator = Operator.MULTIPLICATION,
                rightOperand = 3
            ),
            result = 6
        )
        val incorrectTile = correctTile.copy(result = 5)

        assertEquals(TileResolutionState.CORRECT, correctTile.resolutionState)
        assertEquals(TileResolutionState.INCORRECT, incorrectTile.resolutionState)
    }

    @Test
    fun tile_expressions_support_hidden_operands_and_hidden_operators() {
        val hiddenOperandTile = Tile(
            expression = Expression(
                leftOperand = Expression.Operand.Hidden,
                operator = Operator.MULTIPLICATION,
                rightOperand = Expression.Operand.Known(3)
            ),
            result = 6
        )
        val hiddenOperatorTile = Tile(
            expression = Expression(
                leftOperand = Expression.Operand.Known(2),
                operator = Operator.Hidden,
                rightOperand = Expression.Operand.Known(3)
            ),
            result = 999
        )

        assertEquals(Expression.Operand.Hidden, hiddenOperandTile.expression.leftOperand)
        assertEquals(TileResolutionState.UNRESOLVED, hiddenOperandTile.resolutionState)
        assertEquals(Operator.Hidden, hiddenOperatorTile.expression.operator)
        assertEquals(TileResolutionState.UNRESOLVED, hiddenOperatorTile.resolutionState)
    }

    @Test
    fun assigning_a_left_tile_operand_recomputes_resolution_state() {
        val tile = Tile(
            expression = Expression(
                leftOperand = Expression.Operand.Hidden,
                operator = Operator.ADDITION,
                rightOperand = Expression.Operand.Known(3)
            ),
            result = 5
        )

        val correctTile = tile.withLeftOperand(2)
        val incorrectTile = correctTile.withLeftOperand(4)

        assertEquals(Expression.Operand.Known(2), correctTile.expression.leftOperand)
        assertEquals(TileResolutionState.CORRECT, correctTile.resolutionState)
        assertEquals(Expression.Operand.Known(4), incorrectTile.expression.leftOperand)
        assertEquals(TileResolutionState.INCORRECT, incorrectTile.resolutionState)
    }

    @Test
    fun assigning_a_right_tile_operand_recomputes_resolution_state() {
        val tile = Tile(
            expression = Expression(
                leftOperand = Expression.Operand.Known(2),
                operator = Operator.ADDITION,
                rightOperand = Expression.Operand.Hidden
            ),
            result = 5
        )

        val correctTile = tile.withRightOperand(3)
        val incorrectTile = correctTile.withRightOperand(4)

        assertEquals(Expression.Operand.Known(3), correctTile.expression.rightOperand)
        assertEquals(TileResolutionState.CORRECT, correctTile.resolutionState)
        assertEquals(Expression.Operand.Known(4), incorrectTile.expression.rightOperand)
        assertEquals(TileResolutionState.INCORRECT, incorrectTile.resolutionState)
    }

    @Test
    fun assigning_a_tile_operator_recomputes_resolution_state() {
        val unresolvedTile = Tile(
            expression = Expression(
                leftOperand = Expression.Operand.Known(2),
                operator = Operator.Hidden,
                rightOperand = Expression.Operand.Known(3)
            ),
            result = 5
        )
        val incorrectTile = unresolvedTile.withOperator(Operator.ADDITION).copy(result = 6)

        assertEquals(TileResolutionState.CORRECT, unresolvedTile.withOperator(Operator.ADDITION).resolutionState)
        assertEquals(TileResolutionState.INCORRECT, incorrectTile.resolutionState)
        assertEquals(
            TileResolutionState.CORRECT,
            incorrectTile.withOperator(Operator.MULTIPLICATION).resolutionState
        )
    }

    @Test
    fun initial_tiles_do_not_expose_a_reset_action() {
        val tile = Tile(
            expression = Expression(
                leftOperand = Expression.Operand.Hidden,
                operator = Operator.Hidden,
                rightOperand = Expression.Operand.Hidden
            ),
            result = 42
        )

        assertFalse(tile.canReset)
    }

    @Test
    fun partially_or_fully_filled_tiles_can_be_reset_back_to_the_initial_expression_state() {
        val tile = Tile(
            expression = Expression(
                leftOperand = Expression.Operand.Known(value = 2, stripEntryId = 1),
                operator = Operator.MULTIPLICATION,
                rightOperand = Expression.Operand.Known(value = 3, stripEntryId = 7)
            ),
            result = 6
        )

        val resetTile = tile.reset()

        assertTrue(tile.canReset)
        assertEquals(Expression.Operand.Hidden, resetTile.expression.leftOperand)
        assertEquals(Operator.Hidden, resetTile.expression.operator)
        assertEquals(Expression.Operand.Hidden, resetTile.expression.rightOperand)
        assertEquals(6, resetTile.result)
        assertEquals(TileResolutionState.UNRESOLVED, resetTile.resolutionState)
        assertFalse(resetTile.canReset)
    }
}
