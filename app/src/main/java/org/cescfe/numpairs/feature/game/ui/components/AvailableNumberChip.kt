package org.cescfe.numpairs.feature.game.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.onClick as semanticOnClick
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.stateDescription
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import org.cescfe.numpairs.R
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
    additionUsed: Boolean = false,
    multiplicationUsed: Boolean = false,
    additionUsageIndicatorTestTag: String? = null,
    multiplicationUsageIndicatorTestTag: String? = null,
    isHighlighted: Boolean = false,
    onClick: (() -> Unit)? = null
) {
    val chipColors = chipColorsFor(style)
    val chipBorder = if (isHighlighted) {
        BorderStroke(width = HIGHLIGHTED_CHIP_BORDER_WIDTH, color = MaterialTheme.colorScheme.tertiary)
    } else {
        chipColors.border
    }
    val clickAction = onClick
    val chipModifier = modifier
        .semantics(mergeDescendants = true) {
            contentDescription?.let { this.contentDescription = it }
            clickAction?.let { action ->
                semanticOnClick(action = {
                    action()
                    true
                })
            }
        }
        .gameHighlightSemantics(isHighlighted)

    Box(
        modifier = chipModifier,
        contentAlignment = Alignment.TopCenter
    ) {
        AvailableNumberChipSurface(
            label = label,
            chipColors = chipColors,
            chipBorder = chipBorder,
            onClick = onClick
        )
        if (style != AvailableNumberChipStyle.HIDDEN) {
            Row(
                modifier = Modifier.offset(y = -CHIP_USAGE_INDICATOR_OVERLAY_LIFT),
                horizontalArrangement = Arrangement.spacedBy(CHIP_USAGE_INDICATOR_SPACING),
                verticalAlignment = Alignment.CenterVertically
            ) {
                StripUsageIndicator(
                    label = "+",
                    used = additionUsed,
                    testTag = additionUsageIndicatorTestTag
                )
                StripUsageIndicator(
                    label = "×",
                    used = multiplicationUsed,
                    testTag = multiplicationUsageIndicatorTestTag
                )
            }
        }
    }
}

@Composable
private fun AvailableNumberChipSurface(
    label: String,
    chipColors: AvailableNumberChipColors,
    chipBorder: BorderStroke,
    onClick: (() -> Unit)?
) {
    if (onClick == null) {
        Surface(
            modifier = Modifier.fillMaxWidth(),
            shape = NumPairsComponents.MediumShape,
            color = chipColors.containerColor,
            contentColor = chipColors.contentColor,
            border = chipBorder,
            tonalElevation = 1.dp
        ) {
            AvailableNumberChipContent(label = label)
        }
    } else {
        Surface(
            modifier = Modifier.fillMaxWidth(),
            onClick = onClick,
            shape = NumPairsComponents.MediumShape,
            color = chipColors.containerColor,
            contentColor = chipColors.contentColor,
            border = chipBorder,
            tonalElevation = 1.dp
        ) {
            AvailableNumberChipContent(label = label)
        }
    }
}

@Composable
private fun AvailableNumberChipContent(label: String) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .defaultMinSize(minHeight = CHIP_MIN_HEIGHT)
            .padding(horizontal = 4.dp),
        contentAlignment = Alignment.Center
    ) {
        AvailableNumberChipLabel(label = label)
    }
}

@Composable
private fun AvailableNumberChipLabel(label: String) {
    Text(
        text = label,
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 2.dp),
        style = NumPairsTextStyles.StripValue,
        textAlign = TextAlign.Center,
        maxLines = 1,
        overflow = TextOverflow.Clip
    )
}

@Composable
private fun StripUsageIndicator(label: String, used: Boolean, testTag: String?) {
    val colors = chipUsageIndicatorColors(used = used)
    val usageStateDescription = stringResource(
        if (used) {
            R.string.tile_operand_usage_state_used
        } else {
            R.string.tile_operand_usage_state_available
        }
    )
    val tagModifier = testTag?.let { tag -> Modifier.testTag(tag) } ?: Modifier

    Surface(
        modifier = tagModifier.semantics {
            stateDescription = usageStateDescription
        },
        shape = RoundedCornerShape(999.dp),
        color = colors.containerColor,
        contentColor = colors.contentColor,
        border = colors.border
    ) {
        Text(
            text = label,
            modifier = Modifier.padding(
                horizontal = CHIP_USAGE_INDICATOR_HORIZONTAL_PADDING,
                vertical = CHIP_USAGE_INDICATOR_VERTICAL_PADDING
            ),
            style = NumPairsTextStyles.PuzzleLabel,
            maxLines = 1
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

@Preview(showBackground = true, widthDp = 48)
@Composable
private fun AvailableNumberChipUsedPreview() {
    NumPairsTheme {
        AvailableNumberChip(
            label = "222",
            style = AvailableNumberChipStyle.KNOWN,
            additionUsed = true,
            multiplicationUsed = false
        )
    }
}
