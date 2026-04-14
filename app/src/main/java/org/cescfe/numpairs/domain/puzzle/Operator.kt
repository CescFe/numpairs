package org.cescfe.numpairs.domain.puzzle

enum class Operator(val symbol: String) {
    ADDITION("+") {
        override fun apply(leftOperand: Int, rightOperand: Int): Int = leftOperand + rightOperand
    },
    MULTIPLICATION("×") {
        override fun apply(leftOperand: Int, rightOperand: Int): Int = leftOperand * rightOperand
    };

    abstract fun apply(leftOperand: Int, rightOperand: Int): Int
}
