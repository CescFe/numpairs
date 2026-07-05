package org.cescfe.numpairs.domain.puzzle.model

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
    fun expressions_support_hidden_operands_and_hidden_operators() {
        val hiddenOperandExpression = Expression(
            leftOperand = Expression.Operand.Hidden,
            operator = Operator.ADDITION,
            rightOperand = Expression.Operand.Known(3)
        )
        val hiddenOperatorExpression = Expression(
            leftOperand = Expression.Operand.Known(2),
            operator = Operator.Hidden,
            rightOperand = Expression.Operand.Known(3)
        )

        assertEquals(Expression.Operand.Hidden, hiddenOperandExpression.leftOperand)
        assertFalse(hiddenOperandExpression.isFullyKnown)
        assertEquals(Operator.Hidden, hiddenOperatorExpression.operator)
        assertFalse(hiddenOperatorExpression.isFullyKnown)
    }

    @Test
    fun left_operands_can_be_assigned_and_reassigned() {
        val hiddenOperandExpression = Expression(
            leftOperand = Expression.Operand.Hidden,
            operator = Operator.ADDITION,
            rightOperand = Expression.Operand.Known(3)
        )
        val fullyKnownExpression = Expression(
            leftOperand = Expression.Operand.Known(2),
            operator = Operator.ADDITION,
            rightOperand = Expression.Operand.Known(3)
        )

        val assignedExpression = hiddenOperandExpression.withLeftOperand(2)
        val reassignedExpression = fullyKnownExpression.withLeftOperand(4)

        assertEquals(Expression.Operand.Known(2), assignedExpression.leftOperand)
        assertTrue(assignedExpression.isFullyKnown)
        assertEquals(Expression.Operand.Known(4), reassignedExpression.leftOperand)
        assertTrue(reassignedExpression.isFullyKnown)
    }

    @Test
    fun right_operands_can_be_assigned_and_reassigned() {
        val hiddenOperandExpression = Expression(
            leftOperand = Expression.Operand.Known(2),
            operator = Operator.ADDITION,
            rightOperand = Expression.Operand.Hidden
        )
        val fullyKnownExpression = Expression(
            leftOperand = Expression.Operand.Known(2),
            operator = Operator.ADDITION,
            rightOperand = Expression.Operand.Known(3)
        )

        val assignedExpression = hiddenOperandExpression.withRightOperand(3)
        val reassignedExpression = fullyKnownExpression.withRightOperand(5)

        assertEquals(Expression.Operand.Known(3), assignedExpression.rightOperand)
        assertTrue(assignedExpression.isFullyKnown)
        assertEquals(Expression.Operand.Known(5), reassignedExpression.rightOperand)
        assertTrue(reassignedExpression.isFullyKnown)
    }

    @Test
    fun operators_can_be_assigned_and_reassigned_to_concrete_values() {
        val hiddenOperatorExpression = Expression(
            leftOperand = Expression.Operand.Known(2),
            operator = Operator.Hidden,
            rightOperand = Expression.Operand.Known(3)
        )
        val additionExpression = Expression(
            leftOperand = Expression.Operand.Known(2),
            operator = Operator.ADDITION,
            rightOperand = Expression.Operand.Known(3)
        )
        val multiplicationExpression = Expression(
            leftOperand = Expression.Operand.Known(2),
            operator = Operator.MULTIPLICATION,
            rightOperand = Expression.Operand.Known(3)
        )

        assertEquals(Operator.ADDITION, hiddenOperatorExpression.withOperator(Operator.ADDITION).operator)
        assertEquals(
            Operator.MULTIPLICATION,
            hiddenOperatorExpression.withOperator(Operator.MULTIPLICATION).operator
        )
        assertEquals(Operator.MULTIPLICATION, additionExpression.withOperator(Operator.MULTIPLICATION).operator)
        assertEquals(Operator.ADDITION, multiplicationExpression.withOperator(Operator.ADDITION).operator)
    }

    @Test
    fun known_operands_require_positive_values() {
        assertThrows(IllegalArgumentException::class.java) {
            Expression.Operand.Known(0)
        }
    }

    @Test
    fun known_operands_require_non_negative_strip_entry_ids_when_present() {
        assertThrows(IllegalArgumentException::class.java) {
            Expression.Operand.Known(
                value = 1,
                stripEntryId = -1
            )
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
    fun evaluate_requires_a_fully_known_expression() {
        val hiddenOperandExpression = Expression(
            leftOperand = Expression.Operand.Hidden,
            operator = Operator.ADDITION,
            rightOperand = Expression.Operand.Known(3)
        )
        val hiddenOperatorExpression = Expression(
            leftOperand = Expression.Operand.Known(2),
            operator = Operator.Hidden,
            rightOperand = Expression.Operand.Known(3)
        )

        assertThrows(IllegalStateException::class.java) {
            hiddenOperandExpression.evaluate()
        }
        assertThrows(IllegalStateException::class.java) {
            hiddenOperatorExpression.evaluate()
        }
    }
}
