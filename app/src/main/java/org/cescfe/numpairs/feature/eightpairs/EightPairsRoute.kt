package org.cescfe.numpairs.feature.eightpairs

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import org.cescfe.numpairs.R
import org.cescfe.numpairs.feature.generated.GeneratedModeRoute
import org.cescfe.numpairs.feature.generated.GeneratedPuzzleProvider

@Composable
fun EightPairsRoute(
    modifier: Modifier = Modifier,
    puzzleProvider: EightPairsPuzzleProvider = DefaultEightPairsPuzzleProvider,
    onNavigateBack: () -> Unit = {}
) {
    val generatedPuzzleProvider = remember(puzzleProvider) {
        GeneratedPuzzleProvider {
            puzzleProvider.nextPuzzle()
        }
    }

    GeneratedModeRoute(
        title = stringResource(R.string.eight_pairs_screen_title),
        gameSessionKey = EIGHT_PAIRS_GAME_SESSION_KEY,
        puzzleProvider = generatedPuzzleProvider,
        modifier = modifier,
        onNavigateBack = onNavigateBack
    )
}

private const val EIGHT_PAIRS_GAME_SESSION_KEY = "eight-pairs"
