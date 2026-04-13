package org.cescfe.numpairs.domain.puzzle

import org.junit.Assert.assertThrows
import org.junit.Test

class BoardTest {
    @Test
    fun requires_two_rows_and_four_columns() {
        assertThrows(IllegalArgumentException::class.java) {
            Board(tileRows = emptyList())
        }
    }
}
