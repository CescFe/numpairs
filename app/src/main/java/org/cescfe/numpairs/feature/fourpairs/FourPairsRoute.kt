package org.cescfe.numpairs.feature.fourpairs

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import java.util.concurrent.atomic.AtomicInteger
import org.cescfe.numpairs.R
import org.cescfe.numpairs.domain.puzzle.Puzzle
import org.cescfe.numpairs.feature.game.GameRoute

@Composable
fun FourPairsRoute(
    modifier: Modifier = Modifier,
    puzzleProvider: FourPairsPuzzleProvider = DefaultFourPairsPuzzleProvider,
    onNavigateBack: () -> Unit = {}
) {
    val gameSession = remember(puzzleProvider) {
        GeneratedFourPairsGameSession(
            initialPuzzle = puzzleProvider.nextPuzzle(),
            gameSessionKey = "four-pairs-${nextFourPairsGameSessionId.incrementAndGet()}"
        )
    }

    GameRoute(
        title = stringResource(R.string.four_pairs_screen_title),
        initialPuzzle = gameSession.initialPuzzle,
        modifier = modifier,
        gameSessionKey = gameSession.gameSessionKey,
        onNavigateBack = onNavigateBack
    )
}

private data class GeneratedFourPairsGameSession(val initialPuzzle: Puzzle, val gameSessionKey: String)

private val nextFourPairsGameSessionId = AtomicInteger()
