package org.cescfe.numpairs.domain.puzzle

import org.junit.Assert.assertEquals
import org.junit.Assert.assertThrows
import org.junit.Test

class StripTest {
    @Test
    fun supports_variable_item_counts_and_assigns_stable_entry_ids() {
        val strip = Strip.fromItems(
            items = listOf(
                StripItem.Known(1),
                StripItem.Hidden
            )
        )

        assertEquals(listOf(0, 1), strip.entries.map(StripEntry::id))
    }

    @Test
    fun requires_unique_strip_entry_ids() {
        assertThrows(IllegalArgumentException::class.java) {
            Strip.fromEntries(
                entries = listOf(
                    StripEntry(0, StripItem.Hidden),
                    StripEntry(0, StripItem.Hidden),
                    StripEntry(2, StripItem.Known(6)),
                    StripEntry(3, StripItem.Hidden),
                    StripEntry(4, StripItem.Known(25)),
                    StripEntry(5, StripItem.Hidden),
                    StripEntry(6, StripItem.Hidden),
                    StripEntry(7, StripItem.Known(222))
                )
            )
        }
    }

    @Test
    fun requires_visible_values_to_be_non_decreasing() {
        assertThrows(IllegalArgumentException::class.java) {
            Strip.fromItems(
                items = listOf(
                    StripItem.Known(1),
                    StripItem.PlayerEntered(5),
                    StripItem.Hidden,
                    StripItem.PlayerEntered(2),
                    StripItem.Known(6),
                    StripItem.Known(7),
                    StripItem.Known(8),
                    StripItem.Known(9)
                )
            )
        }
    }

    @Test
    fun valid_entry_range_requires_an_editable_index_within_bounds() {
        val strip = Strip.fromItems(
            items = listOf(
                StripItem.Hidden,
                StripItem.Hidden,
                StripItem.Known(6),
                StripItem.Hidden,
                StripItem.Known(25),
                StripItem.Hidden,
                StripItem.Hidden,
                StripItem.Known(222)
            )
        )

        listOf(-1, strip.entries.size).forEach { invalidIndex ->
            assertThrows(IllegalArgumentException::class.java) {
                strip.validEntryRangeFor(index = invalidIndex)
            }
        }
    }

    @Test
    fun known_entries_are_not_editable() {
        val strip = Strip.fromItems(
            items = listOf(
                StripItem.Known(1),
                StripItem.Hidden,
                StripItem.Known(3),
                StripItem.Hidden,
                StripItem.Known(5),
                StripItem.Hidden,
                StripItem.Known(7),
                StripItem.Known(9)
            )
        )

        assertThrows(IllegalArgumentException::class.java) {
            strip.validEntryRangeFor(index = 0)
        }

        assertThrows(IllegalArgumentException::class.java) {
            strip.withUpdatedEntry(
                index = 0,
                value = 2
            )
        }
    }

    @Test
    fun valid_entry_range_uses_the_nearest_known_values_on_both_sides() {
        val strip = Strip.fromItems(
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
        val strip = Strip.fromItems(
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
        val strip = Strip.fromItems(
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

    @Test
    fun updating_an_entry_requires_an_index_within_the_strip_bounds() {
        val strip = Strip.fromItems(
            items = listOf(
                StripItem.Hidden,
                StripItem.Hidden,
                StripItem.Known(6),
                StripItem.Hidden,
                StripItem.Known(25),
                StripItem.Hidden,
                StripItem.Hidden,
                StripItem.Known(222)
            )
        )

        listOf(-1, strip.entries.size).forEach { invalidIndex ->
            assertThrows(IllegalArgumentException::class.java) {
                strip.withUpdatedEntry(
                    index = invalidIndex,
                    value = 1
                )
            }
        }
    }

    @Test
    fun updating_an_adjacent_hidden_entry_reorders_player_entered_values_within_the_editable_run() {
        val strip = Strip.fromItems(
            items = listOf(
                StripItem.PlayerEntered(5),
                StripItem.Hidden,
                StripItem.Known(6),
                StripItem.Hidden,
                StripItem.Known(8),
                StripItem.Known(10),
                StripItem.Known(12),
                StripItem.Known(14)
            )
        )

        assertEquals(
            listOf(
                StripItem.PlayerEntered(2),
                StripItem.PlayerEntered(5),
                StripItem.Known(6),
                StripItem.Hidden,
                StripItem.Known(8),
                StripItem.Known(10),
                StripItem.Known(12),
                StripItem.Known(14)
            ),
            strip.withUpdatedEntry(index = 1, value = 2).items
        )
    }

    @Test
    fun editing_an_entry_reorders_only_player_entered_values_and_keeps_hidden_positions_in_place() {
        val strip = Strip.fromItems(
            items = listOf(
                StripItem.Known(1),
                StripItem.PlayerEntered(2),
                StripItem.Hidden,
                StripItem.PlayerEntered(5),
                StripItem.Known(6),
                StripItem.Known(7),
                StripItem.Known(8),
                StripItem.Known(9)
            )
        )

        assertEquals(
            listOf(
                StripItem.Known(1),
                StripItem.PlayerEntered(1),
                StripItem.Hidden,
                StripItem.PlayerEntered(2),
                StripItem.Known(6),
                StripItem.Known(7),
                StripItem.Known(8),
                StripItem.Known(9)
            ),
            strip.withUpdatedEntry(index = 3, value = 1).items
        )
    }

    @Test
    fun reordering_player_entered_values_moves_their_entry_ids_with_them() {
        val strip = Strip.fromEntries(
            entries = listOf(
                StripEntry(10, StripItem.PlayerEntered(5)),
                StripEntry(11, StripItem.Hidden),
                StripEntry(12, StripItem.Known(6)),
                StripEntry(13, StripItem.Hidden),
                StripEntry(14, StripItem.Known(8)),
                StripEntry(15, StripItem.Known(10)),
                StripEntry(16, StripItem.Known(12)),
                StripEntry(17, StripItem.Known(14))
            )
        )

        assertEquals(
            listOf(11, 10, 12, 13, 14, 15, 16, 17),
            strip.withUpdatedEntry(index = 1, value = 2).entries.map(StripEntry::id)
        )
    }

    @Test
    fun updating_an_entry_requires_a_value_within_the_valid_range() {
        val strip = Strip.fromItems(
            items = listOf(
                StripItem.Known(3),
                StripItem.Hidden,
                StripItem.Known(5),
                StripItem.Hidden,
                StripItem.Known(7),
                StripItem.Known(8),
                StripItem.Known(9),
                StripItem.Known(10)
            )
        )

        assertThrows(IllegalArgumentException::class.java) {
            strip.withUpdatedEntry(index = 1, value = 2)
        }
    }
}
