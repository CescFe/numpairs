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
    fun known_operands_require_positive_values() {
        assertThrows(IllegalArgumentException::class.java) {
            Expression.Operand.Known(0)
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
