package org.cescfe.numpairs.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import org.cescfe.numpairs.ui.theme.NumPairsTheme

private val CHIP_MIN_HEIGHT = 48.dp
private val CHIP_CORNER_RADIUS = 14.dp
private val KNOWN_CHIP_BORDER_WIDTH = 1.dp
private val MODIFIABLE_CHIP_BORDER_WIDTH = 1.5.dp

enum class AvailableNumberChipStyle {
    KNOWN,
    HIDDEN,
    PLAYER_ENTERED
}

@Composable
fun AvailableNumberChip(
    label: String,
    modifier: Modifier = Modifier,
    style: AvailableNumberChipStyle = AvailableNumberChipStyle.KNOWN,
    contentDescription: String? = null,
    onClick: (() -> Unit)? = null
) {
    val chipColors = chipColorsFor(style)
    val chipModifier = if (contentDescription == null) {
        modifier
    } else {
        modifier.semantics(mergeDescendants = true) {
            this.contentDescription = contentDescription
        }
    }

    if (onClick == null) {
        Surface(
            modifier = chipModifier,
            shape = RoundedCornerShape(CHIP_CORNER_RADIUS),
            color = chipColors.containerColor,
            contentColor = chipColors.contentColor,
            border = chipColors.border,
            tonalElevation = 1.dp
        ) {
            AvailableNumberChipLabel(label = label)
        }
    } else {
        Surface(
            modifier = chipModifier,
            onClick = onClick,
            shape = RoundedCornerShape(CHIP_CORNER_RADIUS),
            color = chipColors.containerColor,
            contentColor = chipColors.contentColor,
            border = chipColors.border,
            tonalElevation = 1.dp
        ) {
            AvailableNumberChipLabel(label = label)
        }
    }
}

@Composable
private fun AvailableNumberChipLabel(label: String) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .defaultMinSize(minHeight = CHIP_MIN_HEIGHT)
            .padding(horizontal = 4.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.titleSmall,
            textAlign = TextAlign.Center
        )
    }
}

@Composable
private fun chipColorsFor(style: AvailableNumberChipStyle): AvailableNumberChipColors {
    val colorScheme = MaterialTheme.colorScheme

    return when (style) {
        AvailableNumberChipStyle.KNOWN -> AvailableNumberChipColors(
            containerColor = colorScheme.surface,
            contentColor = colorScheme.onSurface,
            border = BorderStroke(
                width = KNOWN_CHIP_BORDER_WIDTH,
                color = colorScheme.outline
            )
        )

        AvailableNumberChipStyle.HIDDEN -> AvailableNumberChipColors(
            containerColor = colorScheme.primaryContainer,
            contentColor = colorScheme.onPrimaryContainer,
            border = BorderStroke(
                width = MODIFIABLE_CHIP_BORDER_WIDTH,
                color = colorScheme.primary
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

private data class AvailableNumberChipColors(
    val containerColor: Color,
    val contentColor: Color,
    val border: BorderStroke?
)

@Preview(showBackground = true)
@Composable
private fun AvailableNumberChipPreview() {
    NumPairsTheme {
        AvailableNumberChip(label = "4", style = AvailableNumberChipStyle.KNOWN)
    }
}

@Preview(showBackground = true)
@Composable
private fun AvailableNumberChipHiddenPreview() {
    NumPairsTheme {
        AvailableNumberChip(label = "?", style = AvailableNumberChipStyle.HIDDEN)
    }
}

@Preview(showBackground = true)
@Composable
private fun AvailableNumberChipPlayerEnteredPreview() {
    NumPairsTheme {
        AvailableNumberChip(label = "4", style = AvailableNumberChipStyle.PLAYER_ENTERED)
    }
}
