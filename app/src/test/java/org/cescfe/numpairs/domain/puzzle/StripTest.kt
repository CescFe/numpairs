package org.cescfe.numpairs.domain.puzzle

import org.junit.Assert.assertThrows
import org.junit.Test

class StripTest {
    @Test
    fun requires_eight_numbers() {
        assertThrows(IllegalArgumentException::class.java) {
            Strip(numbers = listOf(1, 2, 3))
        }
    }
}
