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
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import org.cescfe.numpairs.domain.puzzle.model.Puzzle
import org.cescfe.numpairs.feature.game.presentation.GameUiState
import org.cescfe.numpairs.feature.game.presentation.GameViewModel
import org.cescfe.numpairs.feature.game.presentation.TileAssignmentCommit
import org.cescfe.numpairs.feature.game.ui.screen.GameScreen

@Composable
fun GameRoute(
    title: String,
    initialPuzzle: Puzzle,
    modifier: Modifier = Modifier,
    titleContentDescription: String? = null,
    isNavigationIconVisible: Boolean = true,
    gameSessionKey: String = defaultGameSessionKey(title = title, initialPuzzle = initialPuzzle),
    puzzleResetKey: Any = initialPuzzle,
    completionActions: GameCompletionActions? = null,
    isRulesHelperEnabled: Boolean = false,
    isRulesHelperActionDiscoveryDotVisible: Boolean = false,
    onRulesHelperActionTapped: () -> Unit = {},
    onRulesHelperPlayTutorialRequested: (() -> Unit)? = null,
    isSuccessOverlayEnabled: Boolean = true,
    successOverlayContent: GameSuccessOverlayContent? = null,
    successOverlayConfettiCelebrationId: Long? = null,
    onSuccessOverlayConfettiCelebrationStarted: () -> Unit = {},
    isBoardVisible: Boolean = true,
    stripItemEntryGuidance: String? = null,
    isCorrectTileMotionEnabled: Boolean = false,
    isCompletionCelebrationEnabled: Boolean = false,
    compactTileSelectorsEnabled: Boolean = false,
    interactionPolicy: GameInteractionPolicy = GameInteractionPolicy.AllowAll,
    highlightState: GameHighlightState = GameHighlightState.None,
    topBarActions: @Composable RowScope.() -> Unit = {},
    bottomBar: @Composable () -> Unit = {},
    contentBeforePuzzle: @Composable ColumnScope.() -> Unit = {},
    onGameUiStateChanged: (GameUiState) -> Unit = {},
    onPuzzleChanged: (Puzzle) -> Unit = {},
    onPuzzleMutationCommitted: (Puzzle) -> Unit = {},
    onTileAssignmentCommitted: (TileAssignmentCommit) -> Unit = {},
    onNavigateBack: () -> Unit = {}
) {
    val gameViewModel = rememberGameViewModel(
        initialPuzzle = initialPuzzle,
        gameSessionKey = gameSessionKey
    )
    val uiState by gameViewModel.uiState.collectAsState()
    val currentOnGameUiStateChanged by rememberUpdatedState(onGameUiStateChanged)
    val currentOnPuzzleChanged by rememberUpdatedState(onPuzzleChanged)
    val currentOnPuzzleMutationCommitted by rememberUpdatedState(onPuzzleMutationCommitted)
    val currentOnTileAssignmentCommitted by rememberUpdatedState(onTileAssignmentCommitted)
    var nextCorrectTileFeedbackId by remember(gameViewModel) { mutableLongStateOf(0L) }
    var correctTileFeedbackIdsByIndex by remember(gameViewModel, puzzleResetKey) {
        mutableStateOf(emptyMap<Int, Long>())
    }
    var nextCompletionFeedbackId by remember(gameViewModel) { mutableLongStateOf(0L) }
    var completionFeedbackId by remember(gameViewModel, puzzleResetKey) {
        mutableStateOf<Long?>(null)
    }

    fun handleTileAssignmentCommit(commit: TileAssignmentCommit) {
        currentOnTileAssignmentCommitted(commit)

        if (isCorrectTileMotionEnabled && commit.madeTileCorrect) {
            nextCorrectTileFeedbackId += 1
            correctTileFeedbackIdsByIndex = correctTileFeedbackIdsByIndex +
                (commit.tileIndex to nextCorrectTileFeedbackId)
        }
        if (isCompletionCelebrationEnabled && commit.madePuzzleSolved) {
            nextCompletionFeedbackId += 1
            completionFeedbackId = nextCompletionFeedbackId
        }
    }

    fun <T> performPuzzleAction(action: () -> T): T {
        val result = action()
        gameViewModel.consumeCommittedPuzzleMutations().forEach { committedPuzzle ->
            currentOnPuzzleMutationCommitted(committedPuzzle)
        }
        return result
    }

    LaunchedEffect(gameViewModel, puzzleResetKey) {
        gameViewModel.reset(initialPuzzle = initialPuzzle)
        gameViewModel.currentPuzzle.collect { puzzle ->
            currentOnPuzzleChanged(puzzle)
        }
    }

    LaunchedEffect(uiState) {
        currentOnGameUiStateChanged(uiState)
    }

    fun resolveActiveStripItemEntryInputIfAllowed(focusLossSourceIndex: Int? = null): Boolean {
        val input = uiState.stripItemEntryInput ?: return true
        val value = input.draftText.toIntOrNull()
        val isInvalid = input.draftText.isNotBlank() &&
            (value == null || value !in input.validRange)
        val canResolve = input.draftText.isBlank() ||
            isInvalid ||
            value?.let { resolvedValue ->
                interactionPolicy.canConfirmStripItemEntry(input.stripItemIndex, resolvedValue)
            } == true

        if (!canResolve) {
            return false
        }

        performPuzzleAction {
            gameViewModel.onStripItemEntryInputFocusLost(
                stripItemIndex = focusLossSourceIndex ?: input.stripItemIndex
            )
        }

        return input.draftText.isBlank() || !isInvalid
    }

    GameScreen(
        title = title,
        titleContentDescription = titleContentDescription,
        isNavigationIconVisible = isNavigationIconVisible,
        uiState = uiState,
        modifier = modifier,
        onNavigateBack = onNavigateBack,
        onStripItemTapped = { index ->
            if (interactionPolicy.canTapStripItem(index) && resolveActiveStripItemEntryInputIfAllowed()) {
                performPuzzleAction {
                    gameViewModel.onStripItemTapped(index)
                }
            }
        },
        onStripItemEntryInputChanged = gameViewModel::onStripItemEntryInputChanged,
        onStripItemEntryInputConfirmed = { resolveActiveStripItemEntryInputIfAllowed() },
        onStripItemEntryInputCleared = {
            val input = uiState.stripItemEntryInput

            if (input != null && interactionPolicy.canTapStripItem(input.stripItemIndex)) {
                performPuzzleAction(gameViewModel::onStripItemEntryInputCleared)
            }
        },
        onStripItemEntryInputFocusLost = { stripItemIndex ->
            resolveActiveStripItemEntryInputIfAllowed(focusLossSourceIndex = stripItemIndex)
        },
        onTileLeftOperandTapped = { index ->
            if (interactionPolicy.canTapTileLeftOperand(index) && resolveActiveStripItemEntryInputIfAllowed()) {
                performPuzzleAction {
                    gameViewModel.onTileLeftOperandTapped(index)
                }
            }
        },
        onTileRightOperandTapped = { index ->
            if (interactionPolicy.canTapTileRightOperand(index) && resolveActiveStripItemEntryInputIfAllowed()) {
                performPuzzleAction {
                    gameViewModel.onTileRightOperandTapped(index)
                }
            }
        },
        onTileOperandSelectionDismissed = gameViewModel::onTileOperandSelectionDismissed,
        onTileOperandSelectionConfirmed = onTileOperandSelectionConfirmed@{ stripEntryId ->
            val dialog = uiState.tileOperandSelectionDialog ?: return@onTileOperandSelectionConfirmed

            if (interactionPolicy.canConfirmTileOperand(dialog.tileIndex, dialog.slot, stripEntryId)) {
                performPuzzleAction {
                    gameViewModel.onTileOperandSelectionConfirmed(stripEntryId)
                }
                    ?.let(::handleTileAssignmentCommit)
            }
        },
        onTileOperatorTapped = { index ->
            if (interactionPolicy.canTapTileOperator(index) && resolveActiveStripItemEntryInputIfAllowed()) {
                performPuzzleAction {
                    gameViewModel.onTileOperatorTapped(index)
                }
            }
        },
        onTileResetTapped = { index ->
            if (interactionPolicy.canTapTileReset(index) && resolveActiveStripItemEntryInputIfAllowed()) {
                performPuzzleAction {
                    gameViewModel.onTileResetTapped(index)
                }
                correctTileFeedbackIdsByIndex = correctTileFeedbackIdsByIndex - index
                completionFeedbackId = null
            }
        },
        onTileOperatorSelectionDismissed = gameViewModel::onTileOperatorSelectionDismissed,
        onTileOperatorSelectionConfirmed = onTileOperatorSelectionConfirmed@{ operator ->
            val tileIndex = uiState.tileOperatorSelectionDialog?.tileIndex ?: return@onTileOperatorSelectionConfirmed

            if (interactionPolicy.canConfirmTileOperator(tileIndex, operator)) {
                performPuzzleAction {
                    gameViewModel.onTileOperatorSelectionConfirmed(operator)
                }
                    ?.let(::handleTileAssignmentCommit)
            }
        },
        onSuccessOverlayDismissed = gameViewModel::onSuccessOverlayDismissed,
        completionActions = completionActions,
        isRulesHelperEnabled = isRulesHelperEnabled,
        isRulesHelperActionDiscoveryDotVisible = isRulesHelperActionDiscoveryDotVisible,
        onRulesHelperActionTapped = onRulesHelperActionTapped,
        onRulesHelperPlayTutorialRequested = onRulesHelperPlayTutorialRequested,
        isSuccessOverlayEnabled = isSuccessOverlayEnabled,
        successOverlayContent = successOverlayContent,
        successOverlayConfettiCelebrationId = successOverlayConfettiCelebrationId,
        onSuccessOverlayConfettiCelebrationStarted = onSuccessOverlayConfettiCelebrationStarted,
        isBoardVisible = isBoardVisible,
        stripItemEntryGuidance = stripItemEntryGuidance,
        compactTileSelectorsEnabled = compactTileSelectorsEnabled,
        interactionPolicy = interactionPolicy,
        highlightState = highlightState,
        correctTileFeedbackIdsByIndex = correctTileFeedbackIdsByIndex,
        completionFeedbackId = completionFeedbackId,
        topBarActions = topBarActions,
        bottomBar = bottomBar,
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
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        require(modelClass.isAssignableFrom(GameViewModel::class.java)) {
            "Unsupported ViewModel type ${modelClass.name}."
        }

        return requireNotNull(modelClass.cast(GameViewModel(initialPuzzle = initialPuzzle)))
    }
}

private fun defaultGameSessionKey(title: String, initialPuzzle: Puzzle): String =
    "GameRoute:$title:${initialPuzzle.hashCode()}"

private tailrec fun Context.findComponentActivity(): ComponentActivity? = when (this) {
    is ComponentActivity -> this
    is ContextWrapper -> baseContext.findComponentActivity()
    else -> null
}
