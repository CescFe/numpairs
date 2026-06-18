package org.cescfe.numpairs.feature.game.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import org.cescfe.numpairs.feature.game.ui.gameHighlightSemantics
import org.cescfe.numpairs.ui.theme.NumPairsComponents
import org.cescfe.numpairs.ui.theme.NumPairsTextStyles
import org.cescfe.numpairs.ui.theme.NumPairsTheme

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
    isHighlighted: Boolean = false,
    onClick: (() -> Unit)? = null
) {
    val chipColors = chipColorsFor(style)
    val chipBorder = if (isHighlighted) {
        BorderStroke(width = HIGHLIGHTED_CHIP_BORDER_WIDTH, color = MaterialTheme.colorScheme.tertiary)
    } else {
        chipColors.border
    }
    val chipModifier = if (contentDescription == null) {
        modifier
    } else {
        modifier.semantics(mergeDescendants = true) {
            this.contentDescription = contentDescription
        }
    }.gameHighlightSemantics(isHighlighted)

    if (onClick == null) {
        Surface(
            modifier = chipModifier,
            shape = NumPairsComponents.MediumShape,
            color = chipColors.containerColor,
            contentColor = chipColors.contentColor,
            border = chipBorder,
            tonalElevation = 1.dp
        ) {
            AvailableNumberChipLabel(label = label)
        }
    } else {
        Surface(
            modifier = chipModifier,
            onClick = onClick,
            shape = NumPairsComponents.MediumShape,
            color = chipColors.containerColor,
            contentColor = chipColors.contentColor,
            border = chipBorder,
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
            style = NumPairsTextStyles.StripValue,
            textAlign = TextAlign.Center
        )
    }
}

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
