package org.cescfe.numpairs.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.sizeIn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import org.cescfe.numpairs.domain.puzzle.Expression
import org.cescfe.numpairs.domain.puzzle.PuzzleSamples
import org.cescfe.numpairs.domain.puzzle.Tile
import org.cescfe.numpairs.ui.theme.NumPairsTheme

@Composable
fun PuzzleTile(
    tile: Tile,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(20.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            TileExpressionRow(
                expression = tile.expression,
                modifier = Modifier.fillMaxWidth()
            )
            Text(
                text = tile.result.toString(),
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.primary,
                fontWeight = FontWeight.SemiBold
            )
        }
    }
}

@Composable
private fun TileExpressionRow(
    expression: Expression,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.SpaceEvenly,
        verticalAlignment = Alignment.CenterVertically
    ) {
        TileExpressionItem(text = expression.leftOperand.toString())
        TileExpressionItem(text = expression.operator.symbol)
        TileExpressionItem(text = expression.rightOperand.toString())
    }
}

@Composable
private fun TileExpressionItem(
    text: String,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier.sizeIn(minWidth = 24.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = text,
            style = MaterialTheme.typography.titleMedium,
            textAlign = TextAlign.Center
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun PuzzleTilePreview() {
    NumPairsTheme {
        PuzzleTile(
            tile = PuzzleSamples.prototype.board.tileAt(rowIndex = 0, columnIndex = 0)
        )
    }
}
