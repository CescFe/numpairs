package org.cescfe.numpairs.domain.puzzle

import org.cescfe.numpairs.domain.puzzle.model.StripEntryRange
import org.junit.Assert.assertFalse
import org.junit.Assert.assertThrows
import org.junit.Assert.assertTrue
import org.junit.Test

class StripEntryRangeTest {
    @Test
    fun requires_a_positive_minimum_value() {
        listOf(0, -1).forEach { invalidMinimumValue ->
            assertThrows(IllegalArgumentException::class.java) {
                StripEntryRange(minimumValue = invalidMinimumValue)
            }
        }
    }

    @Test
    fun requires_the_maximum_value_to_be_greater_than_or_equal_to_the_minimum_value() {
        assertThrows(IllegalArgumentException::class.java) {
            StripEntryRange(
                minimumValue = 3,
                maximumValue = 2
            )
        }
    }

    @Test
    fun contains_respects_bounded_and_unbounded_ranges() {
        val boundedRange = StripEntryRange(
            minimumValue = 3,
            maximumValue = 5
        )
        val unboundedRange = StripEntryRange(minimumValue = 4)

        assertFalse(2 in boundedRange)
        assertTrue(3 in boundedRange)
        assertTrue(5 in boundedRange)
        assertFalse(6 in boundedRange)

        assertFalse(3 in unboundedRange)
        assertTrue(4 in unboundedRange)
        assertTrue(999 in unboundedRange)
    }
}
