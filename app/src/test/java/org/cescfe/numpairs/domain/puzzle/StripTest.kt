package org.cescfe.numpairs.domain.puzzle

import org.junit.Assert.assertThrows
import org.junit.Test

class StripTest {
    @Test
    fun requires_eight_items() {
        assertThrows(IllegalArgumentException::class.java) {
            Strip(
                items = listOf(
                    StripItem.Known(1),
                    StripItem.Known(2),
                    StripItem.Known(3)
                )
            )
        }
    }
}
