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
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import org.cescfe.numpairs.R
import org.cescfe.numpairs.domain.puzzle.Puzzle
import org.cescfe.numpairs.domain.puzzle.PuzzleSamples
import org.cescfe.numpairs.ui.components.AvailableNumberChip
import org.cescfe.numpairs.ui.components.PuzzleTile
import org.cescfe.numpairs.ui.theme.NumPairsTheme

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GameScreen(puzzle: Puzzle, modifier: Modifier = Modifier) {
    Scaffold(
        modifier = modifier.fillMaxSize(),
        topBar = {
            TopAppBar(
                title = {
                    Text(text = stringResource(R.string.app_name))
                }
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(innerPadding)
                .padding(horizontal = 16.dp, vertical = 20.dp),
            verticalArrangement = Arrangement.spacedBy(24.dp)
        ) {
            StripSection(
                availableNumbers = puzzle.strip.numbers,
                modifier = Modifier.fillMaxWidth()
            )
            BoardSection(
                puzzle = puzzle,
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}

@Composable
private fun BoardSection(puzzle: Puzzle, modifier: Modifier = Modifier) {
    val boardContentDescription = stringResource(R.string.board_content_description)

    Column(
        modifier = modifier.semantics {
            contentDescription = boardContentDescription
        },
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        puzzle.board.tileRows.forEach { row ->
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                row.forEach { tile ->
                    PuzzleTile(
                        tile = tile,
                        modifier = Modifier
                            .weight(1f)
                            .wrapContentHeight()
                    )
                }
            }
        }
    }
}

@Composable
private fun StripSection(availableNumbers: List<Int>, modifier: Modifier = Modifier) {
    val stripContentDescription = stringResource(R.string.strip_content_description)

    Surface(
        modifier = modifier.semantics {
            contentDescription = stripContentDescription
        },
        color = MaterialTheme.colorScheme.surfaceVariant,
        shape = RoundedCornerShape(24.dp)
    ) {
        FlowRow(
            modifier = Modifier
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
