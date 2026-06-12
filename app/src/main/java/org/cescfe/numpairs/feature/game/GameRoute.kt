package org.cescfe.numpairs.feature.game

import android.content.Context
import android.content.ContextWrapper
import androidx.activity.ComponentActivity
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.RowScope
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import org.cescfe.numpairs.domain.puzzle.Puzzle
import org.cescfe.numpairs.feature.game.presentation.GameUiState
import org.cescfe.numpairs.feature.game.presentation.GameViewModel
import org.cescfe.numpairs.feature.game.ui.GameScreen

@Composable
fun GameRoute(
    title: String,
    initialPuzzle: Puzzle,
    modifier: Modifier = Modifier,
    gameSessionKey: String = defaultGameSessionKey(title = title, initialPuzzle = initialPuzzle),
    puzzleResetKey: Any = initialPuzzle,
    completionActions: GameCompletionActions? = null,
    isRulesHelperEnabled: Boolean = false,
    onRulesHelperPlayTutorialRequested: (() -> Unit)? = null,
    isSuccessOverlayEnabled: Boolean = true,
    interactionPolicy: GameInteractionPolicy = GameInteractionPolicy.AllowAll,
    highlightState: GameHighlightState = GameHighlightState.None,
    topBarActions: @Composable RowScope.() -> Unit = {},
    contentBeforePuzzle: @Composable ColumnScope.() -> Unit = {},
    onGameUiStateChanged: (GameUiState) -> Unit = {},
    onNavigateBack: () -> Unit = {}
) {
    val gameViewModel = rememberGameViewModel(
        initialPuzzle = initialPuzzle,
        gameSessionKey = gameSessionKey
    )
    val uiState by gameViewModel.uiState.collectAsState()
    val currentOnGameUiStateChanged by rememberUpdatedState(onGameUiStateChanged)

    LaunchedEffect(gameViewModel, puzzleResetKey) {
        gameViewModel.reset(initialPuzzle = initialPuzzle)
    }

    LaunchedEffect(uiState) {
        currentOnGameUiStateChanged(uiState)
    }

    GameScreen(
        title = title,
        uiState = uiState,
        modifier = modifier,
        onNavigateBack = onNavigateBack,
        onStripItemTapped = { index ->
            if (interactionPolicy.canTapStripItem(index)) {
                gameViewModel.onStripItemTapped(index)
            }
        },
        onStripItemEntryDismissed = gameViewModel::onStripItemEntryDismissed,
        onStripItemEntryConfirmed = onStripItemEntryConfirmed@{ value ->
            val stripItemIndex = uiState.stripItemEntryDialog?.stripItemIndex ?: return@onStripItemEntryConfirmed

            if (interactionPolicy.canConfirmStripItemEntry(stripItemIndex, value)) {
                gameViewModel.onStripItemEntryConfirmed(value)
            }
        },
        onTileLeftOperandTapped = { index ->
            if (interactionPolicy.canTapTileLeftOperand(index)) {
                gameViewModel.onTileLeftOperandTapped(index)
            }
        },
        onTileRightOperandTapped = { index ->
            if (interactionPolicy.canTapTileRightOperand(index)) {
                gameViewModel.onTileRightOperandTapped(index)
            }
        },
        onTileOperandSelectionDismissed = gameViewModel::onTileOperandSelectionDismissed,
        onTileOperandSelectionConfirmed = onTileOperandSelectionConfirmed@{ stripEntryId ->
            val dialog = uiState.tileOperandSelectionDialog ?: return@onTileOperandSelectionConfirmed

            if (interactionPolicy.canConfirmTileOperand(dialog.tileIndex, dialog.slot, stripEntryId)) {
                gameViewModel.onTileOperandSelectionConfirmed(stripEntryId)
            }
        },
        onTileOperatorTapped = { index ->
            if (interactionPolicy.canTapTileOperator(index)) {
                gameViewModel.onTileOperatorTapped(index)
            }
        },
        onTileResetTapped = { index ->
            if (interactionPolicy.canTapTileReset(index)) {
                gameViewModel.onTileResetTapped(index)
            }
        },
        onTileOperatorSelectionDismissed = gameViewModel::onTileOperatorSelectionDismissed,
        onTileOperatorSelectionConfirmed = onTileOperatorSelectionConfirmed@{ operator ->
            val tileIndex = uiState.tileOperatorSelectionDialog?.tileIndex ?: return@onTileOperatorSelectionConfirmed

            if (interactionPolicy.canConfirmTileOperator(tileIndex, operator)) {
                gameViewModel.onTileOperatorSelectionConfirmed(operator)
            }
        },
        onSuccessOverlayDismissed = gameViewModel::onSuccessOverlayDismissed,
        completionActions = completionActions,
        isRulesHelperEnabled = isRulesHelperEnabled,
        onRulesHelperPlayTutorialRequested = onRulesHelperPlayTutorialRequested,
        isSuccessOverlayEnabled = isSuccessOverlayEnabled,
        interactionPolicy = interactionPolicy,
        highlightState = highlightState,
        topBarActions = topBarActions,
        contentBeforePuzzle = contentBeforePuzzle
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
