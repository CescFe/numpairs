package org.cescfe.numpairs.feature.generated

import androidx.compose.foundation.layout.RowScope
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import org.cescfe.numpairs.domain.puzzle.model.Puzzle
import org.cescfe.numpairs.feature.game.GameCompletionActions
import org.cescfe.numpairs.feature.game.GameRoute

@Composable
fun GeneratedModeRoute(
    title: String,
    gameSessionKey: String,
    puzzleProvider: GeneratedPuzzleProvider,
    modifier: Modifier = Modifier,
    isRulesHelperEnabled: Boolean = false,
    isRulesHelperActionDiscoveryDotVisible: Boolean = false,
    onRulesHelperActionTapped: () -> Unit = {},
    onRulesHelperPlayTutorialRequested: (() -> Unit)? = null,
    topBarActions: @Composable RowScope.() -> Unit = {},
    onNavigateBack: () -> Unit = {}
) {
    val gameSessionFactory = remember(puzzleProvider) {
        GeneratedModeGameSessionFactory(puzzleProvider = puzzleProvider)
    }
    var gameSession by remember(gameSessionFactory) {
        mutableStateOf(gameSessionFactory.create())
    }

    GameRoute(
        title = title,
        initialPuzzle = gameSession.initialPuzzle,
        modifier = modifier,
        gameSessionKey = gameSessionKey,
        puzzleResetKey = gameSession.id,
        completionActions = GameCompletionActions(
            onNewPuzzleRequested = {
                gameSession = gameSessionFactory.create()
            },
            onReturnToMenuRequested = onNavigateBack
        ),
        isRulesHelperEnabled = isRulesHelperEnabled,
        isRulesHelperActionDiscoveryDotVisible = isRulesHelperActionDiscoveryDotVisible,
        onRulesHelperActionTapped = onRulesHelperActionTapped,
        onRulesHelperPlayTutorialRequested = onRulesHelperPlayTutorialRequested,
        topBarActions = topBarActions,
        onNavigateBack = onNavigateBack
    )
}

internal data class GeneratedModeGameSession(val id: Int, val initialPuzzle: Puzzle)

internal class GeneratedModeGameSessionFactory(private val puzzleProvider: GeneratedPuzzleProvider) {
    private var nextGameSessionId = 0

    fun create(): GeneratedModeGameSession = GeneratedModeGameSession(
        id = nextGameSessionId++,
        initialPuzzle = puzzleProvider.nextPuzzle()
    )
}
