package org.cescfe.numpairs.feature.tutorial

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import org.cescfe.numpairs.R
import org.cescfe.numpairs.feature.game.GameCompletionActions
import org.cescfe.numpairs.feature.game.GameHighlightState
import org.cescfe.numpairs.feature.game.GameInteractionPolicy
import org.cescfe.numpairs.feature.game.GameRoute
import org.cescfe.numpairs.feature.game.presentation.PuzzleOutcomeUiState

@Composable
fun FinalValidationRoute(
    modifier: Modifier = Modifier,
    onValidationSolved: () -> Unit = {},
    onNavigateBack: () -> Unit = {},
    onReturnToMenuRequested: () -> Unit = onNavigateBack
) {
    val scenario = FinalValidationContent.scenario
    var validationSession by rememberSaveable { mutableIntStateOf(0) }
    val solvedReporter = remember(validationSession) { FinalValidationSolvedReporter() }
    val currentOnValidationSolved by rememberUpdatedState(onValidationSolved)

    GameRoute(
        title = stringResource(R.string.final_validation_screen_title),
        initialPuzzle = scenario.initialPuzzle,
        modifier = modifier,
        gameSessionKey = FINAL_VALIDATION_GAME_SESSION_KEY,
        puzzleResetKey = validationSession,
        completionActions = GameCompletionActions(
            onNewPuzzleRequested = {
                validationSession += 1
            },
            onReturnToMenuRequested = onReturnToMenuRequested
        ),
        isRulesHelperEnabled = true,
        interactionPolicy = GameInteractionPolicy.AllowAll,
        highlightState = GameHighlightState.None,
        onGameUiStateChanged = { uiState ->
            if (solvedReporter.shouldReport(uiState.puzzleOutcome)) {
                currentOnValidationSolved()
            }
        },
        onNavigateBack = onNavigateBack
    )
}

internal class FinalValidationSolvedReporter {
    private var hasReportedSolved = false

    fun shouldReport(puzzleOutcome: PuzzleOutcomeUiState?): Boolean {
        if (puzzleOutcome != PuzzleOutcomeUiState.Solved || hasReportedSolved) {
            return false
        }

        hasReportedSolved = true
        return true
    }
}

private const val FINAL_VALIDATION_GAME_SESSION_KEY = "FinalValidationRoute"
