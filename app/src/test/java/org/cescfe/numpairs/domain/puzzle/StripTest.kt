package org.cescfe.numpairs.domain.puzzle

import org.junit.Assert.assertEquals
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

    @Test
    fun valid_entry_range_uses_the_nearest_known_values_on_both_sides() {
        val strip = Strip(
            items = listOf(
                StripItem.Known(1),
                StripItem.Hidden,
                StripItem.PlayerEntered(2),
                StripItem.Hidden,
                StripItem.Known(5),
                StripItem.Hidden,
                StripItem.Known(7),
                StripItem.Known(9)
            )
        )

        assertEquals(
            StripEntryRange(minimumValue = 1, maximumValue = 5),
            strip.validEntryRangeFor(index = 3)
        )
    }

    @Test
    fun valid_entry_range_uses_one_when_there_is_no_known_value_on_the_left() {
        val strip = Strip(
            items = listOf(
                StripItem.Hidden,
                StripItem.Hidden,
                StripItem.Known(3),
                StripItem.Hidden,
                StripItem.Known(5),
                StripItem.Hidden,
                StripItem.Known(7),
                StripItem.Known(9)
            )
        )

        assertEquals(
            StripEntryRange(minimumValue = 1, maximumValue = 3),
            strip.validEntryRangeFor(index = 0)
        )
    }

    @Test
    fun valid_entry_range_has_no_upper_bound_when_there_is_no_known_value_on_the_right() {
        val strip = Strip(
            items = listOf(
                StripItem.Known(1),
                StripItem.Hidden,
                StripItem.Known(3),
                StripItem.Hidden,
                StripItem.Hidden,
                StripItem.PlayerEntered(6),
                StripItem.Hidden,
                StripItem.Hidden
            )
        )

        assertEquals(
            StripEntryRange(minimumValue = 3, maximumValue = null),
            strip.validEntryRangeFor(index = 6)
        )
    }
}
