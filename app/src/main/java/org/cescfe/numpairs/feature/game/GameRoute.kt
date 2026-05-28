package org.cescfe.numpairs.feature.game

import android.content.Context
import android.content.ContextWrapper
import androidx.activity.ComponentActivity
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import org.cescfe.numpairs.domain.puzzle.Puzzle
import org.cescfe.numpairs.feature.game.presentation.GameViewModel
import org.cescfe.numpairs.feature.game.ui.GameScreen

@Composable
fun GameRoute(
    title: String,
    initialPuzzle: Puzzle,
    modifier: Modifier = Modifier,
    gameSessionKey: String = defaultGameSessionKey(title = title, initialPuzzle = initialPuzzle),
    onNavigateBack: () -> Unit = {}
) {
    val gameViewModel = rememberGameViewModel(
        initialPuzzle = initialPuzzle,
        gameSessionKey = gameSessionKey
    )
    val uiState by gameViewModel.uiState.collectAsState()

    GameScreen(
        title = title,
        uiState = uiState,
        modifier = modifier,
        onNavigateBack = onNavigateBack,
        onStripItemTapped = gameViewModel::onStripItemTapped,
        onStripItemEntryDismissed = gameViewModel::onStripItemEntryDismissed,
        onStripItemEntryConfirmed = gameViewModel::onStripItemEntryConfirmed,
        onTileLeftOperandTapped = gameViewModel::onTileLeftOperandTapped,
        onTileRightOperandTapped = gameViewModel::onTileRightOperandTapped,
        onTileOperandSelectionDismissed = gameViewModel::onTileOperandSelectionDismissed,
        onTileOperandSelectionConfirmed = gameViewModel::onTileOperandSelectionConfirmed,
        onTileOperatorTapped = gameViewModel::onTileOperatorTapped,
        onTileResetTapped = gameViewModel::onTileResetTapped,
        onTileOperatorSelectionDismissed = gameViewModel::onTileOperatorSelectionDismissed,
        onTileOperatorSelectionConfirmed = gameViewModel::onTileOperatorSelectionConfirmed,
        onSuccessOverlayDismissed = gameViewModel::onSuccessOverlayDismissed
    )
}

@Composable
private fun rememberGameViewModel(initialPuzzle: Puzzle, gameSessionKey: String): GameViewModel {
    val activity = LocalContext.current.findComponentActivity()
        ?: error("GameRoute requires a ComponentActivity host.")

    return remember(activity, gameSessionKey) {
        ViewModelProvider(
            activity,
            GameViewModelFactory(initialPuzzle = initialPuzzle)
        )[gameSessionKey, GameViewModel::class.java]
    }
}

private class GameViewModelFactory(private val initialPuzzle: Puzzle) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(GameViewModel::class.java)) {
            return GameViewModel(initialPuzzle = initialPuzzle) as T
        }

        error("Unknown ViewModel class ${modelClass.name}")
    }
}

private fun defaultGameSessionKey(title: String, initialPuzzle: Puzzle): String =
    "GameRoute:$title:${initialPuzzle.hashCode()}"

private tailrec fun Context.findComponentActivity(): ComponentActivity? = when (this) {
    is ComponentActivity -> this
    is ContextWrapper -> baseContext.findComponentActivity()
    else -> null
}
