package org.cescfe.numpairs.feature.game.ui.components

import androidx.compose.foundation.BorderStroke
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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import org.cescfe.numpairs.R
import org.cescfe.numpairs.data.puzzle.seed.initialPuzzle
import org.cescfe.numpairs.feature.game.presentation.GameUiState
import org.cescfe.numpairs.feature.game.presentation.TileUiState
import org.cescfe.numpairs.feature.game.presentation.TileVisualState
import org.cescfe.numpairs.feature.game.ui.gameHighlightSemantics
import org.cescfe.numpairs.ui.theme.NumPairsTextStyles
import org.cescfe.numpairs.ui.theme.NumPairsTheme

@Composable
fun PuzzleTile(
    tile: TileUiState,
    modifier: Modifier = Modifier,
    isHighlighted: Boolean = false,
    leftOperandModifier: Modifier = Modifier,
    isLeftOperandHighlighted: Boolean = false,
    leftOperandContentDescription: String? = null,
    onLeftOperandClick: (() -> Unit)? = null,
    operatorModifier: Modifier = Modifier,
    isOperatorHighlighted: Boolean = false,
    operatorContentDescription: String? = null,
    onOperatorClick: (() -> Unit)? = null,
    operatorOverlay: @Composable BoxScope.() -> Unit = {},
    rightOperandModifier: Modifier = Modifier,
    isRightOperandHighlighted: Boolean = false,
    rightOperandContentDescription: String? = null,
    onRightOperandClick: (() -> Unit)? = null,
    resetModifier: Modifier = Modifier,
    onResetClick: (() -> Unit)? = null
) {
    val statePalette = tileStatePalette(tile.visualState)
    val expressionColor = statePalette?.contentColor ?: Color.Unspecified
    val tileBorder = statePalette?.border ?: if (isHighlighted) {
        BorderStroke(width = HIGHLIGHTED_TILE_BORDER_WIDTH, color = MaterialTheme.colorScheme.tertiary)
    } else {
        null
    }
    val tileStateDescription = when (tile.visualState) {
        TileVisualState.INCORRECT -> stringResource(R.string.tile_state_incorrect)
        TileVisualState.MISMATCHED_PAIRING -> stringResource(R.string.tile_state_mismatched_pairing)
        TileVisualState.NORMAL -> null
    }

    Box(
        modifier = modifier
            .semantics {
                if (tileStateDescription != null) {
                    stateDescription = tileStateDescription
                }
            }
            .gameHighlightSemantics(isHighlighted)
    ) {
        Card(
            modifier = Modifier,
            shape = RoundedCornerShape(TILE_CORNER_RADIUS),
            colors = if (statePalette == null) {
                CardDefaults.cardColors()
            } else {
                CardDefaults.cardColors(containerColor = statePalette.containerColor)
            },
            border = tileBorder
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
                    isLeftOperandHighlighted = isLeftOperandHighlighted,
                    leftOperandContentDescription = leftOperandContentDescription,
                    onLeftOperandClick = onLeftOperandClick,
                    operatorModifier = operatorModifier,
                    isOperatorHighlighted = isOperatorHighlighted,
                    operatorContentDescription = operatorContentDescription,
                    onOperatorClick = onOperatorClick,
                    operatorOverlay = operatorOverlay,
                    rightOperandModifier = rightOperandModifier,
                    isRightOperandHighlighted = isRightOperandHighlighted,
                    rightOperandContentDescription = rightOperandContentDescription,
                    onRightOperandClick = onRightOperandClick,
                    textColor = expressionColor,
                    modifier = Modifier.fillMaxWidth()
                )
                Text(
                    text = tile.resultLabel,
                    modifier = Modifier.fillMaxWidth(),
                    style = NumPairsTextStyles.TileResult,
                    color = statePalette?.contentColor ?: MaterialTheme.colorScheme.onSurface,
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
