package org.cescfe.numpairs.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
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
import org.cescfe.numpairs.data.puzzle.seed.initialPuzzle
import org.cescfe.numpairs.ui.screen.GameUiState
import org.cescfe.numpairs.ui.screen.TileUiState
import org.cescfe.numpairs.ui.screen.TileVisualState
import org.cescfe.numpairs.ui.theme.NumPairsTheme

@Composable
fun PuzzleTile(
    tile: TileUiState,
    modifier: Modifier = Modifier,
    leftOperandModifier: Modifier = Modifier,
    leftOperandContentDescription: String? = null,
    onLeftOperandClick: (() -> Unit)? = null,
    operatorModifier: Modifier = Modifier,
    operatorContentDescription: String? = null,
    onOperatorClick: (() -> Unit)? = null,
    operatorOverlay: @Composable BoxScope.() -> Unit = {},
    rightOperandModifier: Modifier = Modifier,
    rightOperandContentDescription: String? = null,
    onRightOperandClick: (() -> Unit)? = null,
    resetModifier: Modifier = Modifier,
    onResetClick: (() -> Unit)? = null
) {
    val statePalette = tileStatePalette(tile.visualState)
    val expressionColor = statePalette?.contentColor ?: Color.Unspecified
    val tileStateDescription = when (tile.visualState) {
        TileVisualState.INCORRECT -> stringResource(R.string.tile_state_incorrect)
        TileVisualState.MISMATCHED_PAIRING -> stringResource(R.string.tile_state_mismatched_pairing)
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
            colors = if (statePalette == null) {
                CardDefaults.cardColors()
            } else {
                CardDefaults.cardColors(containerColor = statePalette.containerColor)
            },
            border = statePalette?.border
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
                    leftOperandContentDescription = leftOperandContentDescription,
                    onLeftOperandClick = onLeftOperandClick,
                    operatorModifier = operatorModifier,
                    operatorContentDescription = operatorContentDescription,
                    onOperatorClick = onOperatorClick,
                    operatorOverlay = operatorOverlay,
                    rightOperandModifier = rightOperandModifier,
                    rightOperandContentDescription = rightOperandContentDescription,
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
                    color = statePalette?.contentColor ?: MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.SemiBold,
                    textAlign = TextAlign.Center
                )
            }
        }

        if (tile.canReset && onResetClick != null) {
            TileResetAction(
                modifier = resetModifier
                    .align(Alignment.TopEnd)
                    .offset(
                        x = TILE_RESET_ACTION_CORNER_OFFSET,
                        y = -TILE_RESET_ACTION_CORNER_OFFSET
                    )
                    .size(TILE_RESET_ACTION_CONTAINER_SIZE),
                onClick = onResetClick
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun PuzzleTilePreview() {
    NumPairsTheme {
        PuzzleTile(
            tile = GameUiState.from(initialPuzzle).tiles.first()
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
