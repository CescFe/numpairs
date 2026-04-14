package org.cescfe.numpairs.domain.puzzle

data class Expression(val leftOperand: Int, val operator: Operator, val rightOperand: Int) {
    fun evaluate(): Int = operator.apply(leftOperand, rightOperand)
}
