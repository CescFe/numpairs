package org.cescfe.numpairs.ui.screen

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import org.cescfe.numpairs.domain.puzzle.Puzzle
import org.cescfe.numpairs.domain.puzzle.PuzzleSamples
import org.cescfe.numpairs.ui.components.AvailableNumberChip
import org.cescfe.numpairs.ui.components.PuzzleTile
import org.cescfe.numpairs.ui.theme.NumPairsTheme

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GameScreen(puzzle: Puzzle, modifier: Modifier = Modifier.Companion) {
    Scaffold(
        modifier = modifier.fillMaxSize(),
        topBar = {
            TopAppBar(
                title = {
                    Text(text = "NumPairs")
                }
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier.Companion
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(innerPadding)
                .padding(horizontal = 16.dp, vertical = 20.dp),
            verticalArrangement = Arrangement.spacedBy(24.dp)
        ) {
            StripSection(
                availableNumbers = puzzle.strip.numbers,
                modifier = Modifier.Companion.fillMaxWidth()
            )
            SectionTitle(title = "Board")
            BoardSection(
                puzzle = puzzle,
                modifier = Modifier.Companion.fillMaxWidth()
            )
        }
    }
}

@Composable
private fun SectionTitle(title: String) {
    Text(
        text = title,
        style = MaterialTheme.typography.titleLarge,
        fontWeight = FontWeight.Companion.SemiBold
    )
}

@Composable
private fun BoardSection(puzzle: Puzzle, modifier: Modifier = Modifier.Companion) {
    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        puzzle.board.tileRows.forEach { row ->
            Row(
                modifier = Modifier.Companion.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                row.forEach { tile ->
                    PuzzleTile(
                        tile = tile,
                        modifier = Modifier.Companion
                            .weight(1f)
                            .wrapContentHeight()
                    )
                }
            }
        }
    }
}

@Composable
private fun StripSection(availableNumbers: List<Int>, modifier: Modifier = Modifier.Companion) {
    Surface(
        modifier = modifier.semantics {
            contentDescription = "Strip"
        },
        color = MaterialTheme.colorScheme.surfaceVariant,
        shape = RoundedCornerShape(24.dp)
    ) {
        FlowRow(
            modifier = Modifier.Companion
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 18.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            availableNumbers.forEach { availableNumber ->
                AvailableNumberChip(
                    number = availableNumber
                )
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun GameScreenPreview() {
    NumPairsTheme {
        GameScreen(
            puzzle = PuzzleSamples.prototype
        )
    }
}
