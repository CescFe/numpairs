package org.cescfe.numpairs.feature.game.ui.screen

import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

internal data class StripGridLayout(val columnCount: Int, val chipWidth: Dp, val entryIndexesByRow: List<List<Int>>)

internal fun calculateStripGridLayout(
    availableWidth: Dp,
    entryCount: Int,
    minimumChipWidth: Dp,
    maximumChipWidth: Dp
): StripGridLayout {
    require(entryCount > 0) {
        "Strip entry count must be positive."
    }
    require(minimumChipWidth > 0.dp) {
        "Minimum strip chip width must be positive."
    }
    require(maximumChipWidth > 0.dp) {
        "Maximum strip chip width must be positive."
    }

    val constrainedAvailableWidth = availableWidth.coerceAtLeast(0.dp)
    val effectiveMinimumChipWidth = minimumChipWidth.coerceAtMost(constrainedAvailableWidth)
    val columnCount = preferredColumnCounts(entryCount).firstOrNull { candidateColumnCount ->
        stripChipWidthFor(
            availableWidth = constrainedAvailableWidth,
            columnCount = candidateColumnCount
        ) >= effectiveMinimumChipWidth
    } ?: 1
    val availableChipWidth = stripChipWidthFor(
        availableWidth = constrainedAvailableWidth,
        columnCount = columnCount
    )

    return StripGridLayout(
        columnCount = columnCount,
        chipWidth = availableChipWidth
            .coerceAtMost(maximumChipWidth)
            .coerceAtLeast(effectiveMinimumChipWidth),
        entryIndexesByRow = (0 until entryCount).toList().chunked(columnCount)
    )
}

private fun preferredColumnCounts(entryCount: Int): List<Int> =
    (minOf(entryCount, STRIP_MAX_VISUAL_COLUMN_COUNT) downTo 1).filter { columnCount ->
        stripRowImbalance(entryCount = entryCount, columnCount = columnCount) <= MAX_STRIP_ROW_IMBALANCE
    }

private fun stripRowImbalance(entryCount: Int, columnCount: Int): Int {
    val rowCount = (entryCount + columnCount - 1) / columnCount
    val lastRowEntryCount = entryCount - (columnCount * (rowCount - 1))

    return columnCount - lastRowEntryCount
}

private fun stripChipWidthFor(availableWidth: Dp, columnCount: Int): Dp {
    val totalSpacing = STRIP_CHIP_SPACING * (columnCount - 1)

    return ((availableWidth - totalSpacing) / columnCount).coerceAtLeast(0.dp)
}

private const val MAX_STRIP_ROW_IMBALANCE = 1
