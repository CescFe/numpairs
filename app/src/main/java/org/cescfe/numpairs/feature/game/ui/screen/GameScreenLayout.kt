package org.cescfe.numpairs.feature.game.ui.screen

import androidx.compose.ui.unit.Dp

internal fun calculateBoardColumnCount(availableWidth: Dp): Int {
    val columnsThatFit = (
        (availableWidth.value + BOARD_TILE_SPACING.value) /
            (BOARD_TILE_MIN_WIDTH.value + BOARD_TILE_SPACING.value)
        ).toInt()

    return columnsThatFit.coerceIn(1, BOARD_MAX_VISUAL_COLUMN_COUNT)
}

internal fun calculateBoardTileWidth(availableWidth: Dp, visualColumnCount: Int): Dp {
    val totalSpacing = BOARD_TILE_SPACING * (visualColumnCount - 1)
    val availableTileWidth = (availableWidth - totalSpacing) / visualColumnCount

    return availableTileWidth.coerceIn(BOARD_TILE_MIN_WIDTH, BOARD_TILE_MAX_WIDTH)
}
