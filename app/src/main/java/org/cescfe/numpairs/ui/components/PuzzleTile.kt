package org.cescfe.numpairs.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
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
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import org.cescfe.numpairs.R
import org.cescfe.numpairs.domain.puzzle.PuzzleSamples
import org.cescfe.numpairs.ui.screen.GameUiState
import org.cescfe.numpairs.ui.screen.TileUiState
import org.cescfe.numpairs.ui.theme.NumPairsTheme

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
        shape = RoundedCornerShape(20.dp),
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
                .padding(horizontal = 12.dp, vertical = 16.dp),
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
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        TileExpressionItem(
            text = tile.leftOperandLabel,
            modifier = Modifier
                .weight(1f)
                .then(leftOperandModifier),
            onClick = onLeftOperandClick,
            textColor = textColor
        )
        TileExpressionItem(
            text = tile.operatorLabel,
            modifier = Modifier
                .weight(1f)
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
            textColor = textColor
        )
    }
}

@Composable
private fun TileExpressionItem(
    text: String,
    modifier: Modifier = Modifier,
    onClick: (() -> Unit)? = null,
    textColor: Color = Color.Unspecified,
    overlayContent: @Composable BoxScope.() -> Unit = {}
) {
    Box(
        modifier = modifier
            .widthIn(min = 24.dp)
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
            modifier = Modifier.fillMaxWidth(),
            style = MaterialTheme.typography.titleMedium,
            color = textColor,
            textAlign = TextAlign.Center
        )
        overlayContent()
    }
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
