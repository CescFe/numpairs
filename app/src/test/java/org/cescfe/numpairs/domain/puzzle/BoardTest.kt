package org.cescfe.numpairs.domain.puzzle

import org.junit.Assert.assertThrows
import org.junit.Test

class BoardTest {
    @Test
    fun requires_two_rows() {
        assertThrows(IllegalArgumentException::class.java) {
            Board(tileRows = emptyList())
        }
    }

    @Test
    fun requires_four_columns_per_row() {
        assertThrows(IllegalArgumentException::class.java) {
            Board(
                tileRows = listOf(
                    listOf(validTile(1), validTile(2), validTile(3)),
                    listOf(validTile(4), validTile(5), validTile(6), validTile(7))
                )
            )
        }
    }

    private fun validTile(leftOperand: Int): Tile = Tile(
        expression = Expression(
            leftOperand = leftOperand,
            operator = Operator.ADDITION,
            rightOperand = 1
        ),
        result = leftOperand + 1
    )
}
