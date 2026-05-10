package org.cescfe.numpairs.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import org.cescfe.numpairs.ui.screen.TileVisualState

internal val TILE_CORNER_RADIUS = 20.dp
internal val TILE_HORIZONTAL_PADDING = 10.dp
internal val TILE_VERTICAL_PADDING = 16.dp
internal val TILE_EXPRESSION_ITEM_SPACING = 2.dp
internal val TILE_EXPRESSION_ITEM_MIN_WIDTH = 24.dp
internal val TILE_EXPRESSION_ITEM_MIN_HEIGHT = 40.dp
internal val TILE_OPERAND_TEXT_PADDING = 0.dp
internal val TILE_OPERATOR_SLOT_WIDTH = 28.dp
internal val TILE_RESET_ACTION_CONTAINER_SIZE = 28.dp
internal val TILE_RESET_ACTION_ICON_SIZE = 16.dp
internal val TILE_RESET_ACTION_CORNER_OVERLAP = TILE_RESET_ACTION_CONTAINER_SIZE / 2
internal val TILE_RESET_ACTION_CORNER_ADJUSTMENT = 4.dp
internal val TILE_RESET_ACTION_CORNER_OFFSET =
    TILE_RESET_ACTION_CORNER_OVERLAP - TILE_RESET_ACTION_CORNER_ADJUSTMENT
internal const val LARGE_OPERAND_CHARACTER_COUNT = 3

@Composable
internal fun tileStatePalette(visualState: TileVisualState): TileStatePalette? {
    val colorScheme = MaterialTheme.colorScheme

    return when (visualState) {
        TileVisualState.NORMAL -> null
        TileVisualState.INCORRECT -> TileStatePalette(
            containerColor = colorScheme.errorContainer,
            contentColor = colorScheme.onErrorContainer,
            border = BorderStroke(2.dp, colorScheme.error)
        )

        TileVisualState.MISMATCHED_PAIRING -> TileStatePalette(
            containerColor = colorScheme.tertiaryContainer,
            contentColor = colorScheme.onTertiaryContainer,
            border = BorderStroke(2.dp, colorScheme.tertiary)
        )
    }
}

internal data class TileStatePalette(
    val containerColor: Color,
    val contentColor: Color,
    val border: BorderStroke
)
