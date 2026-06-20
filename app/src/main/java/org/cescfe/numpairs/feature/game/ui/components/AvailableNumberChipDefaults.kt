package org.cescfe.numpairs.feature.game.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import org.cescfe.numpairs.ui.theme.NumPairsComponents

internal val CHIP_MIN_HEIGHT = 48.dp
internal val CHIP_CONTENT_HORIZONTAL_PADDING = 2.dp
internal val CHIP_CONTENT_VERTICAL_PADDING = 3.dp
internal val CHIP_USAGE_INDICATOR_SPACING = 1.dp
internal val CHIP_USAGE_INDICATOR_HORIZONTAL_PADDING = 3.dp
internal val CHIP_USAGE_INDICATOR_VERTICAL_PADDING = 0.dp
internal val MODIFIABLE_CHIP_BORDER_WIDTH = 1.5.dp
internal val HIGHLIGHTED_CHIP_BORDER_WIDTH = 3.dp

@Composable
internal fun chipColorsFor(style: AvailableNumberChipStyle): AvailableNumberChipColors {
    val colorScheme = MaterialTheme.colorScheme

    return when (style) {
        AvailableNumberChipStyle.KNOWN -> AvailableNumberChipColors(
            containerColor = colorScheme.surface,
            contentColor = colorScheme.onSurface,
            border = NumPairsComponents.subtleBorder()
        )

        AvailableNumberChipStyle.HIDDEN -> AvailableNumberChipColors(
            containerColor = NumPairsComponents.hiddenContainerColor(),
            contentColor = NumPairsComponents.hiddenContentColor(),
            border = BorderStroke(
                width = MODIFIABLE_CHIP_BORDER_WIDTH,
                color = colorScheme.onSurfaceVariant
            )
        )

        AvailableNumberChipStyle.PLAYER_ENTERED -> AvailableNumberChipColors(
            containerColor = colorScheme.secondaryContainer,
            contentColor = colorScheme.onSecondaryContainer,
            border = BorderStroke(
                width = MODIFIABLE_CHIP_BORDER_WIDTH,
                color = colorScheme.secondary
            )
        )
    }
}

internal data class AvailableNumberChipColors(
    val containerColor: Color,
    val contentColor: Color,
    val border: BorderStroke
)

@Composable
internal fun chipUsageIndicatorColors(used: Boolean): AvailableNumberChipUsageIndicatorColors = if (used) {
    AvailableNumberChipUsageIndicatorColors(
        containerColor = MaterialTheme.colorScheme.primaryContainer,
        contentColor = MaterialTheme.colorScheme.onPrimaryContainer,
        border = BorderStroke(
            width = NumPairsComponents.ThinBorderWidth,
            color = MaterialTheme.colorScheme.primary
        )
    )
} else {
    AvailableNumberChipUsageIndicatorColors(
        containerColor = Color.Transparent,
        contentColor = MaterialTheme.colorScheme.onSurfaceVariant,
        border = NumPairsComponents.subtleBorder()
    )
}

internal data class AvailableNumberChipUsageIndicatorColors(
    val containerColor: Color,
    val contentColor: Color,
    val border: BorderStroke
)
