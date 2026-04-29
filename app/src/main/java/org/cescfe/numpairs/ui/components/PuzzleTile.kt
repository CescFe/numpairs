package org.cescfe.numpairs.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
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
import org.cescfe.numpairs.ui.theme.NumPairsTheme

private val TILE_CORNER_RADIUS = 20.dp
private val TILE_HORIZONTAL_PADDING = 12.dp
private val TILE_VERTICAL_PADDING = 16.dp
private val TILE_EXPRESSION_ITEM_SPACING = 4.dp
private val TILE_EXPRESSION_ITEM_MIN_WIDTH = 24.dp
private val TILE_EXPRESSION_ITEM_MIN_HEIGHT = 40.dp
private val TILE_OPERAND_TEXT_PADDING = 2.dp
private val TILE_OPERATOR_SLOT_WIDTH = 28.dp
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
    onRightOperandClick: (() -> Unit)? = null
) {
    val expressionColor = if (tile.isInvalid) {
        MaterialTheme.colorScheme.error
    } else {
        Color.Unspecified
    }
    val incorrectStateDescription = stringResource(R.string.tile_state_incorrect)

    Card(
        modifier = modifier.semantics {
            if (tile.isInvalid) {
                stateDescription = incorrectStateDescription
            }
        },
        shape = RoundedCornerShape(TILE_CORNER_RADIUS),
        colors = if (tile.isInvalid) {
            CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.error.copy(alpha = 0.08f)
            )
        } else {
            CardDefaults.cardColors()
        },
        border = if (tile.isInvalid) {
            BorderStroke(1.5.dp, MaterialTheme.colorScheme.error)
        } else {
            null
        }
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
