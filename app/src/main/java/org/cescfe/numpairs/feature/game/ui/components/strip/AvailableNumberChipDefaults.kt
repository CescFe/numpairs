package org.cescfe.numpairs.feature.game.ui.components.strip

import androidx.compose.foundation.BorderStroke
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import org.cescfe.numpairs.ui.theme.NumPairsComponents
import org.cescfe.numpairs.ui.theme.numPairsSemanticColors

internal val CHIP_MIN_HEIGHT = 48.dp
internal val CHIP_CONTENT_HORIZONTAL_PADDING = 4.dp
internal val CHIP_LABEL_HORIZONTAL_PADDING = 2.dp
internal val CHIP_HORIZONTAL_CONTENT_INSET = (CHIP_CONTENT_HORIZONTAL_PADDING + CHIP_LABEL_HORIZONTAL_PADDING) * 2
internal val CHIP_USAGE_INDICATOR_RESERVED_HEIGHT = 5.dp
internal val CHIP_USAGE_INDICATOR_SPACING = 1.dp
internal val CHIP_USAGE_INDICATOR_HORIZONTAL_PADDING = 3.dp
internal val CHIP_USAGE_INDICATOR_VERTICAL_PADDING = 0.dp
internal val MODIFIABLE_CHIP_BORDER_WIDTH = 1.5.dp
internal val HIGHLIGHTED_CHIP_BORDER_WIDTH = 3.dp

@Composable
internal fun chipColorsFor(style: AvailableNumberChipStyle): AvailableNumberChipColors {
    val colorScheme = MaterialTheme.colorScheme
    val semanticColors = MaterialTheme.numPairsSemanticColors

    return when (style) {
        AvailableNumberChipStyle.KNOWN -> AvailableNumberChipColors(
            containerColor = colorScheme.surface,
            contentColor = colorScheme.onSurface,
            border = NumPairsComponents.subtleBorder()
        )

        AvailableNumberChipStyle.HIDDEN -> AvailableNumberChipColors(
            containerColor = semanticColors.hiddenContainer,
            contentColor = semanticColors.onHiddenContainer,
            border = BorderStroke(
                width = MODIFIABLE_CHIP_BORDER_WIDTH,
                color = semanticColors.hiddenBorder
            )
        )

        AvailableNumberChipStyle.PLAYER_ENTERED -> AvailableNumberChipColors(
            containerColor = semanticColors.selectionContainer,
            contentColor = semanticColors.onSelectionContainer,
            border = BorderStroke(
                width = MODIFIABLE_CHIP_BORDER_WIDTH,
                color = semanticColors.selection
            )
        )
    }
}

internal data class AvailableNumberChipColors(
    val containerColor: Color,
    val contentColor: Color,
    val border: BorderStroke
)
