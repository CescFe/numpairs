package org.cescfe.numpairs.domain.puzzle

sealed interface Operator {
    val symbol: String

    fun apply(leftOperand: Int, rightOperand: Int): Int

    data object Hidden : Operator {
        override val symbol: String = "?"

        override fun apply(leftOperand: Int, rightOperand: Int): Int = error("Hidden operators cannot be applied.")
    }

    data object Addition : Operator {
        override val symbol: String = "+"

        override fun apply(leftOperand: Int, rightOperand: Int): Int = leftOperand + rightOperand
    }

    data object Multiplication : Operator {
        override val symbol: String = "×"

        override fun apply(leftOperand: Int, rightOperand: Int): Int = leftOperand * rightOperand
    }

    companion object {
        val ADDITION: Operator = Addition
        val MULTIPLICATION: Operator = Multiplication
    }
}
