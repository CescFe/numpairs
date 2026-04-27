package org.cescfe.numpairs.domain.puzzle

data class Expression(val leftOperand: Operand, val operator: Operator, val rightOperand: Operand) {
    constructor(leftOperand: Int, operator: Operator, rightOperand: Int) : this(
        leftOperand = Operand.Known(leftOperand),
        operator = operator,
        rightOperand = Operand.Known(rightOperand)
    )

    val isFullyKnown: Boolean
        get() = leftOperand is Operand.Known && operator != Operator.Hidden && rightOperand is Operand.Known

    fun withOperator(operator: Operator): Expression {
        require(operator != Operator.Hidden) {
            "Expression operators can only be assigned concrete operators."
        }

        return copy(operator = operator)
    }

    fun evaluate(): Int = operator.apply(
        leftOperand = leftOperand.requireKnownValue(),
        rightOperand = rightOperand.requireKnownValue()
    )

    sealed interface Operand {
        data object Hidden : Operand

        data class Known(val value: Int) : Operand {
            init {
                require(value > 0) {
                    "Expression operand value must be a positive integer."
                }
            }
        }
    }
}

private fun Expression.Operand.requireKnownValue(): Int = when (this) {
    Expression.Operand.Hidden -> error("Hidden operands cannot be evaluated.")
    is Expression.Operand.Known -> value
}
