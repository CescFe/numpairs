package org.cescfe.numpairs.domain.puzzle

import org.junit.Assert.assertEquals
import org.junit.Assert.assertThrows
import org.junit.Test

class PuzzleCorrectionCountTest {
    @Test
    fun `count is non-negative and increments without losing precision`() {
        assertEquals(0L, PuzzleCorrectionCount.ZERO.value)
        assertEquals(24L, PuzzleCorrectionCount(23).incremented().value)
        assertThrows(IllegalArgumentException::class.java) {
            PuzzleCorrectionCount(-1)
        }
    }

    @Test
    fun `count rejects overflow`() {
        assertThrows(IllegalArgumentException::class.java) {
            PuzzleCorrectionCount(Long.MAX_VALUE).incremented()
        }
    }
}
