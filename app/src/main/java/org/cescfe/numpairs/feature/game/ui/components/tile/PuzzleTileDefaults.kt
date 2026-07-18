package org.cescfe.numpairs.feature.game.ui.components.tile

import androidx.compose.foundation.BorderStroke
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import org.cescfe.numpairs.feature.game.presentation.TileVisualState
import org.cescfe.numpairs.ui.theme.NumPairsComponents
import org.cescfe.numpairs.ui.theme.numPairsSemanticColors

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
internal val HIGHLIGHTED_TILE_BORDER_WIDTH = 3.dp
internal val HIGHLIGHTED_TILE_EXPRESSION_SLOT_BORDER_WIDTH = 2.dp
internal val HIGHLIGHTED_TILE_EXPRESSION_SLOT_CORNER_RADIUS = 8.dp
internal const val CORRECT_TILE_FEEDBACK_SCALE = 1.04f
internal const val CORRECT_TILE_FEEDBACK_SCALE_UP_DURATION_MILLIS = 90
internal const val CORRECT_TILE_FEEDBACK_SCALE_DOWN_DURATION_MILLIS = 130

@Composable
internal fun tileStatePalette(visualState: TileVisualState): TileStatePalette {
    val colorScheme = MaterialTheme.colorScheme
    val semanticColors = MaterialTheme.numPairsSemanticColors

    return when (visualState) {
        TileVisualState.NORMAL -> TileStatePalette(
            containerColor = NumPairsComponents.raisedSurfaceColor(),
            expressionContentColor = colorScheme.onSurfaceVariant,
            resultContentColor = colorScheme.onSurface,
            border = NumPairsComponents.subtleBorder()
        )

        TileVisualState.INCORRECT -> TileStatePalette(
            containerColor = semanticColors.errorContainer,
            expressionContentColor = semanticColors.onErrorContainer,
            resultContentColor = colorScheme.onSurface,
            border = NumPairsComponents.errorBorder()
        )

        TileVisualState.MISMATCHED_PAIRING -> TileStatePalette(
            containerColor = NumPairsComponents.raisedSurfaceColor(),
            expressionContentColor = colorScheme.onSurfaceVariant,
            resultContentColor = colorScheme.onSurface,
            border = BorderStroke(2.dp, semanticColors.error)
        )

        TileVisualState.LIVE_RULE_CONFLICT -> TileStatePalette(
            containerColor = semanticColors.errorContainer,
            expressionContentColor = semanticColors.onErrorContainer,
            resultContentColor = semanticColors.onErrorContainer,
            border = NumPairsComponents.errorBorder()
        )
    }
}

internal data class TileStatePalette(
    val containerColor: Color,
    val expressionContentColor: Color,
    val resultContentColor: Color,
    val border: BorderStroke
)
