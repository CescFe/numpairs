package org.cescfe.numpairs.domain.puzzle

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertThrows
import org.junit.Assert.assertTrue
import org.junit.Test

class ExpressionTest {
    @Test
    fun known_operands_can_be_evaluated() {
        val expression = Expression(
            leftOperand = 2,
            operator = Operator.MULTIPLICATION,
            rightOperand = 3
        )

        assertEquals(6, expression.evaluate())
        assertTrue(expression.isFullyKnown)
    }

    @Test
    fun hidden_operands_are_supported() {
        val expression = Expression(
            leftOperand = Expression.Operand.Hidden,
            operator = Operator.ADDITION,
            rightOperand = Expression.Operand.Known(3)
        )

        assertEquals(Expression.Operand.Hidden, expression.leftOperand)
        assertFalse(expression.isFullyKnown)
    }

    @Test
    fun hidden_operators_are_supported() {
        val expression = Expression(
            leftOperand = Expression.Operand.Known(2),
            operator = Operator.Hidden,
            rightOperand = Expression.Operand.Known(3)
        )

        assertEquals(Operator.Hidden, expression.operator)
        assertFalse(expression.isFullyKnown)
    }

    @Test
    fun hidden_left_operands_can_be_assigned_a_concrete_value() {
        val expression = Expression(
            leftOperand = Expression.Operand.Hidden,
            operator = Operator.ADDITION,
            rightOperand = Expression.Operand.Known(3)
        )

        val updatedExpression = expression.withLeftOperand(2)

        assertEquals(Expression.Operand.Known(2), updatedExpression.leftOperand)
        assertTrue(updatedExpression.isFullyKnown)
    }

    @Test
    fun hidden_right_operands_can_be_assigned_a_concrete_value() {
        val expression = Expression(
            leftOperand = Expression.Operand.Known(2),
            operator = Operator.ADDITION,
            rightOperand = Expression.Operand.Hidden
        )

        val updatedExpression = expression.withRightOperand(3)

        assertEquals(Expression.Operand.Known(3), updatedExpression.rightOperand)
        assertTrue(updatedExpression.isFullyKnown)
    }

    @Test
    fun concrete_left_operands_can_be_replaced_with_another_concrete_value() {
        val expression = Expression(
            leftOperand = Expression.Operand.Known(2),
            operator = Operator.ADDITION,
            rightOperand = Expression.Operand.Known(3)
        )

        val updatedExpression = expression.withLeftOperand(4)

        assertEquals(Expression.Operand.Known(4), updatedExpression.leftOperand)
        assertTrue(updatedExpression.isFullyKnown)
    }

    @Test
    fun concrete_right_operands_can_be_replaced_with_another_concrete_value() {
        val expression = Expression(
            leftOperand = Expression.Operand.Known(2),
            operator = Operator.ADDITION,
            rightOperand = Expression.Operand.Known(3)
        )

        val updatedExpression = expression.withRightOperand(5)

        assertEquals(Expression.Operand.Known(5), updatedExpression.rightOperand)
        assertTrue(updatedExpression.isFullyKnown)
    }

    @Test
    fun hidden_operators_can_be_assigned_as_addition() {
        val expression = Expression(
            leftOperand = Expression.Operand.Known(2),
            operator = Operator.Hidden,
            rightOperand = Expression.Operand.Known(3)
        )

        val updatedExpression = expression.withOperator(Operator.ADDITION)

        assertEquals(Operator.ADDITION, updatedExpression.operator)
        assertTrue(updatedExpression.isFullyKnown)
    }

    @Test
    fun hidden_operators_can_be_assigned_as_multiplication() {
        val expression = Expression(
            leftOperand = Expression.Operand.Known(2),
            operator = Operator.Hidden,
            rightOperand = Expression.Operand.Known(3)
        )

        val updatedExpression = expression.withOperator(Operator.MULTIPLICATION)

        assertEquals(Operator.MULTIPLICATION, updatedExpression.operator)
        assertTrue(updatedExpression.isFullyKnown)
    }

    @Test
    fun addition_operators_can_be_replaced_with_multiplication() {
        val expression = Expression(
            leftOperand = Expression.Operand.Known(2),
            operator = Operator.ADDITION,
            rightOperand = Expression.Operand.Known(3)
        )

        val updatedExpression = expression.withOperator(Operator.MULTIPLICATION)

        assertEquals(Operator.MULTIPLICATION, updatedExpression.operator)
        assertTrue(updatedExpression.isFullyKnown)
    }

    @Test
    fun multiplication_operators_can_be_replaced_with_addition() {
        val expression = Expression(
            leftOperand = Expression.Operand.Known(2),
            operator = Operator.MULTIPLICATION,
            rightOperand = Expression.Operand.Known(3)
        )

        val updatedExpression = expression.withOperator(Operator.ADDITION)

        assertEquals(Operator.ADDITION, updatedExpression.operator)
        assertTrue(updatedExpression.isFullyKnown)
    }

    @Test
    fun known_operands_require_positive_values() {
        assertThrows(IllegalArgumentException::class.java) {
            Expression.Operand.Known(0)
        }
    }

    @Test
    fun domain_operator_assignment_requires_a_concrete_operator() {
        val expression = Expression(
            leftOperand = Expression.Operand.Known(2),
            operator = Operator.ADDITION,
            rightOperand = Expression.Operand.Known(3)
        )

        assertThrows(IllegalArgumentException::class.java) {
            expression.withOperator(Operator.Hidden)
        }
    }

    @Test
    fun hidden_operands_cannot_be_evaluated() {
        val expression = Expression(
            leftOperand = Expression.Operand.Hidden,
            operator = Operator.ADDITION,
            rightOperand = Expression.Operand.Known(3)
        )

        assertThrows(IllegalStateException::class.java) {
            expression.evaluate()
        }
    }

    @Test
    fun hidden_operators_cannot_be_evaluated() {
        val expression = Expression(
            leftOperand = Expression.Operand.Known(2),
            operator = Operator.Hidden,
            rightOperand = Expression.Operand.Known(3)
        )

        assertThrows(IllegalStateException::class.java) {
            expression.evaluate()
        }
    }
}
