package org.cescfe.numpairs.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.stateDescription
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import org.cescfe.numpairs.R
import org.cescfe.numpairs.domain.puzzle.PuzzleSamples
import org.cescfe.numpairs.ui.screen.GameUiState
import org.cescfe.numpairs.ui.screen.TileUiState
import org.cescfe.numpairs.ui.screen.TileVisualState
import org.cescfe.numpairs.ui.theme.NumPairsTheme

private val TILE_CORNER_RADIUS = 20.dp
private val TILE_HORIZONTAL_PADDING = 10.dp
private val TILE_VERTICAL_PADDING = 16.dp
private val TILE_EXPRESSION_ITEM_SPACING = 2.dp
private val TILE_EXPRESSION_ITEM_MIN_WIDTH = 24.dp
private val TILE_EXPRESSION_ITEM_MIN_HEIGHT = 40.dp
private val TILE_OPERAND_TEXT_PADDING = 0.dp
private val TILE_OPERATOR_SLOT_WIDTH = 28.dp
private val TILE_RESET_ACTION_CONTAINER_SIZE = 28.dp
private val TILE_RESET_ACTION_ICON_SIZE = 16.dp
private val TILE_RESET_ACTION_CORNER_OVERLAP = TILE_RESET_ACTION_CONTAINER_SIZE / 2
private const val LARGE_OPERAND_CHARACTER_COUNT = 3

@Composable
fun PuzzleTile(
    tile: TileUiState,
    modifier: Modifier = Modifier,
    leftOperandModifier: Modifier = Modifier,
    onLeftOperandClick: (() -> Unit)? = null,
    operatorModifier: Modifier = Modifier,
    onOperatorClick: (() -> Unit)? = null,
    operatorOverlay: @Composable BoxScope.() -> Unit = {},
    rightOperandModifier: Modifier = Modifier,
    onRightOperandClick: (() -> Unit)? = null,
    resetModifier: Modifier = Modifier,
    onResetClick: (() -> Unit)? = null
) {
    val expressionColor = when (tile.visualState) {
        TileVisualState.INCORRECT -> MaterialTheme.colorScheme.error
        TileVisualState.MISMATCHED_PAIRING,
        TileVisualState.NORMAL -> Color.Unspecified
    }
    val tileStateDescription = when (tile.visualState) {
        TileVisualState.INCORRECT -> stringResource(R.string.tile_state_incorrect)
        TileVisualState.MISMATCHED_PAIRING -> stringResource(R.string.tile_state_mismatched_pairing)
        TileVisualState.NORMAL -> null
    }
    val containerColor = when (tile.visualState) {
        TileVisualState.INCORRECT -> MaterialTheme.colorScheme.error.copy(alpha = 0.08f)
        TileVisualState.MISMATCHED_PAIRING -> MaterialTheme.colorScheme.tertiaryContainer.copy(alpha = 0.72f)
        TileVisualState.NORMAL -> Color.Unspecified
    }
    val border = when (tile.visualState) {
        TileVisualState.INCORRECT -> BorderStroke(1.5.dp, MaterialTheme.colorScheme.error)
        TileVisualState.MISMATCHED_PAIRING -> BorderStroke(1.5.dp, MaterialTheme.colorScheme.tertiary)
        TileVisualState.NORMAL -> null
    }

    Box(
        modifier = modifier.semantics {
            if (tileStateDescription != null) {
                stateDescription = tileStateDescription
            }
        }
    ) {
        Card(
            modifier = Modifier,
            shape = RoundedCornerShape(TILE_CORNER_RADIUS),
            colors = if (containerColor == Color.Unspecified) {
                CardDefaults.cardColors()
            } else {
                CardDefaults.cardColors(containerColor = containerColor)
            },
            border = border
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = TILE_HORIZONTAL_PADDING, vertical = TILE_VERTICAL_PADDING),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                TileExpressionRow(
                    tile = tile,
                    leftOperandModifier = leftOperandModifier,
                    onLeftOperandClick = onLeftOperandClick,
                    operatorModifier = operatorModifier,
                    onOperatorClick = onOperatorClick,
                    operatorOverlay = operatorOverlay,
                    rightOperandModifier = rightOperandModifier,
                    onRightOperandClick = onRightOperandClick,
                    textColor = expressionColor,
                    modifier = Modifier.fillMaxWidth()
                )
                Text(
                    text = tile.resultLabel,
                    modifier = Modifier.fillMaxWidth(),
                    style = MaterialTheme.typography.bodyLarge.copy(
                        fontSize = MaterialTheme.typography.bodyLarge.fontSize * 2,
                        lineHeight = MaterialTheme.typography.bodyLarge.lineHeight * 2
                    ),
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.SemiBold,
                    textAlign = TextAlign.Center
                )
            }
        }

        if (tile.canReset && onResetClick != null) {
            val resetContentDescription = stringResource(R.string.tile_reset_content_description)

            Surface(
                onClick = onResetClick,
                modifier = resetModifier
                    .align(Alignment.TopEnd)
                    .offset(
                        x = TILE_RESET_ACTION_CORNER_OVERLAP,
                        y = -TILE_RESET_ACTION_CORNER_OVERLAP
                    )
                    .size(TILE_RESET_ACTION_CONTAINER_SIZE),
                shape = RoundedCornerShape(999.dp),
                color = MaterialTheme.colorScheme.surface,
                contentColor = MaterialTheme.colorScheme.primary,
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .semantics {
                            stateDescription = resetContentDescription
                        },
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        painter = painterResource(R.drawable.ic_replay),
                        contentDescription = resetContentDescription,
                        modifier = Modifier.size(TILE_RESET_ACTION_ICON_SIZE),
                        tint = MaterialTheme.colorScheme.primary
                    )
                }
            }
        }
    }
}

@Composable
private fun TileExpressionRow(
    tile: TileUiState,
    modifier: Modifier = Modifier,
    leftOperandModifier: Modifier = Modifier,
    onLeftOperandClick: (() -> Unit)? = null,
    operatorModifier: Modifier = Modifier,
    onOperatorClick: (() -> Unit)? = null,
    operatorOverlay: @Composable BoxScope.() -> Unit = {},
    rightOperandModifier: Modifier = Modifier,
    onRightOperandClick: (() -> Unit)? = null,
    textColor: Color = Color.Unspecified
) {
    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(TILE_EXPRESSION_ITEM_SPACING),
        verticalAlignment = Alignment.CenterVertically
    ) {
        TileExpressionItem(
            text = tile.leftOperandLabel,
            modifier = Modifier
                .weight(1f)
                .then(leftOperandModifier),
            onClick = onLeftOperandClick,
            textColor = textColor,
            isOperand = true,
            horizontalTextPadding = TILE_OPERAND_TEXT_PADDING
        )
        TileExpressionItem(
            text = tile.operatorLabel,
            modifier = Modifier
                .width(TILE_OPERATOR_SLOT_WIDTH)
                .then(operatorModifier),
            onClick = onOperatorClick,
            textColor = textColor,
            overlayContent = operatorOverlay
        )
        TileExpressionItem(
            text = tile.rightOperandLabel,
            modifier = Modifier
                .weight(1f)
                .then(rightOperandModifier),
            onClick = onRightOperandClick,
            textColor = textColor,
            isOperand = true,
            horizontalTextPadding = TILE_OPERAND_TEXT_PADDING
        )
    }
}

@Composable
private fun TileExpressionItem(
    text: String,
    modifier: Modifier = Modifier,
    onClick: (() -> Unit)? = null,
    textColor: Color = Color.Unspecified,
    isOperand: Boolean = false,
    horizontalTextPadding: Dp = 0.dp,
    overlayContent: @Composable BoxScope.() -> Unit = {}
) {
    Box(
        modifier = modifier
            .widthIn(min = TILE_EXPRESSION_ITEM_MIN_WIDTH)
            .defaultMinSize(minHeight = TILE_EXPRESSION_ITEM_MIN_HEIGHT)
            .let { currentModifier ->
                if (onClick == null) {
                    currentModifier
                } else {
                    currentModifier.clickable(onClick = onClick)
                }
            },
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = text,
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = horizontalTextPadding),
            style = expressionTextStyle(text = text, isOperand = isOperand),
            color = textColor,
            textAlign = TextAlign.Center,
            maxLines = 1,
            overflow = TextOverflow.Clip
        )
        overlayContent()
    }
}

@Composable
private fun expressionTextStyle(text: String, isOperand: Boolean) = when {
    isOperand && text.length >= LARGE_OPERAND_CHARACTER_COUNT -> MaterialTheme.typography.titleSmall
    else -> MaterialTheme.typography.titleMedium
}

@Preview(showBackground = true)
@Composable
private fun PuzzleTilePreview() {
    NumPairsTheme {
        PuzzleTile(
            tile = GameUiState.from(PuzzleSamples.prototype).tiles.first()
        )
    }
}

@Preview(showBackground = true, widthDp = 128)
@Composable
private fun PuzzleTileLargeOperandsPreview() {
    NumPairsTheme {
        PuzzleTile(
            tile = TileUiState(
                leftOperandLabel = "1",
                operatorLabel = "×",
                rightOperandLabel = "222",
                resultLabel = "222"
            ),
            modifier = Modifier.width(112.dp)
        )
    }
}
