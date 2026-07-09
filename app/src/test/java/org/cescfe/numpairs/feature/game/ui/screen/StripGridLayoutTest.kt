package org.cescfe.numpairs.feature.game.ui.screen

import androidx.compose.ui.unit.dp
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class StripGridLayoutTest {

    @Test
    fun keeps_eight_entries_in_one_row_when_their_minimum_width_fits() {
        val layout = calculateStripGridLayout(
            availableWidth = 320.dp,
            entryCount = 8,
            minimumChipWidth = 32.dp,
            maximumChipWidth = 56.dp
        )

        assertEquals(8, layout.columnCount)
        assertEquals(36.5.dp, layout.chipWidth)
        assertEquals(listOf((0..7).toList()), layout.entryIndexesByRow)
    }

    @Test
    fun arranges_sixteen_entries_in_two_rows_of_eight_when_their_minimum_width_fits() {
        val layout = calculateStripGridLayout(
            availableWidth = 320.dp,
            entryCount = 16,
            minimumChipWidth = 32.dp,
            maximumChipWidth = 56.dp
        )

        assertEquals(8, layout.columnCount)
        assertEquals(36.5.dp, layout.chipWidth)
        assertEquals(
            listOf(
                (0..7).toList(),
                (8..15).toList()
            ),
            layout.entryIndexesByRow
        )
    }

    @Test
    fun reduces_to_balanced_rows_before_allowing_chips_to_become_too_narrow() {
        val layout = calculateStripGridLayout(
            availableWidth = 250.dp,
            entryCount = 16,
            minimumChipWidth = 32.dp,
            maximumChipWidth = 56.dp
        )

        assertEquals(4, layout.columnCount)
        assertEquals(56.dp, layout.chipWidth)
        assertEquals(
            listOf(
                (0..3).toList(),
                (4..7).toList(),
                (8..11).toList(),
                (12..15).toList()
            ),
            layout.entryIndexesByRow
        )
    }

    @Test
    fun accounts_for_a_larger_text_requirement_by_reducing_the_column_count() {
        val layout = calculateStripGridLayout(
            availableWidth = 320.dp,
            entryCount = 8,
            minimumChipWidth = 40.dp,
            maximumChipWidth = 56.dp
        )

        assertEquals(4, layout.columnCount)
        assertTrue(layout.chipWidth >= 40.dp)
        assertEquals(
            listOf(
                (0..3).toList(),
                (4..7).toList()
            ),
            layout.entryIndexesByRow
        )
    }

    @Test
    fun prefers_balanced_rows_for_a_non_divisible_entry_count() {
        val layout = calculateStripGridLayout(
            availableWidth = 320.dp,
            entryCount = 11,
            minimumChipWidth = 32.dp,
            maximumChipWidth = 56.dp
        )

        assertEquals(6, layout.columnCount)
        assertEquals(
            listOf(
                (0..5).toList(),
                (6..10).toList()
            ),
            layout.entryIndexesByRow
        )
    }

    @Test
    fun caps_chip_width_on_wide_screens_without_reordering_entries() {
        val layout = calculateStripGridLayout(
            availableWidth = 800.dp,
            entryCount = 16,
            minimumChipWidth = 32.dp,
            maximumChipWidth = 56.dp
        )

        assertEquals(8, layout.columnCount)
        assertEquals(56.dp, layout.chipWidth)
        assertEquals((0..15).toList(), layout.entryIndexesByRow.flatten())
    }
}
