package org.cescfe.numpairs.feature.game.ui

import androidx.activity.ComponentActivity
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.test.junit4.v2.createAndroidComposeRule
import org.cescfe.numpairs.R
import org.cescfe.numpairs.domain.puzzle.model.Operator
import org.cescfe.numpairs.domain.puzzle.model.PuzzleCompletionState
import org.cescfe.numpairs.feature.game.GameHighlightState
import org.cescfe.numpairs.feature.game.presentation.GameUiState
import org.cescfe.numpairs.feature.game.presentation.GameViewModel
import org.cescfe.numpairs.feature.game.presentation.TileOperatorSelectionDialogUiState
import org.cescfe.numpairs.feature.game.ui.screen.GameScreen
import org.cescfe.numpairs.ui.theme.NumPairsTheme
import org.junit.Before
import org.junit.Rule

abstract class GameScreenTestHost {
    @get:Rule
    val composeTestRule = createAndroidComposeRule<ComponentActivity>()

    protected lateinit var screen: GameScreenRobot

    private var viewModel by mutableStateOf(GameViewModel())
    private var uiStateOverride: GameUiState? by mutableStateOf(null)
    private var onTileOperatorTappedOverride: ((Int) -> Unit)? by mutableStateOf(null)
    private var onSuccessOverlayDismissedOverride: (() -> Unit)? by mutableStateOf(null)
    private var highlightState by mutableStateOf(GameHighlightState.None)
    private var isSuccessOverlayEnabled by mutableStateOf(true)
    private var useSolvedOverlayFixture by mutableStateOf(false)
    private var isSolvedOverlayVisible by mutableStateOf(false)

    @Before
    fun setUpGameScreenHost() {
        viewModel = GameViewModel()
        uiStateOverride = null
        onTileOperatorTappedOverride = null
        onSuccessOverlayDismissedOverride = null
        highlightState = GameHighlightState.None
        isSuccessOverlayEnabled = true
        useSolvedOverlayFixture = false
        isSolvedOverlayVisible = false

        composeTestRule.setContent {
            val currentViewModel = viewModel
            val viewModelUiState by currentViewModel.uiState.collectAsState()
            val uiState = uiStateOverride ?: if (useSolvedOverlayFixture) {
                solvedOverlayUiState(isSuccessOverlayVisible = isSolvedOverlayVisible)
            } else {
                viewModelUiState
            }
            val onSuccessOverlayDismissed = onSuccessOverlayDismissedOverride ?: if (useSolvedOverlayFixture) {
                { isSolvedOverlayVisible = false }
            } else {
                currentViewModel::onSuccessOverlayDismissed
            }
            val onTileOperatorTapped = onTileOperatorTappedOverride ?: currentViewModel::onTileOperatorTapped

            NumPairsTheme {
                GameScreen(
                    title = stringResource(R.string.tutorial_screen_title),
                    uiState = uiState,
                    onStripItemTapped = currentViewModel::onStripItemTapped,
                    onStripItemEntryInputChanged = currentViewModel::onStripItemEntryInputChanged,
                    onStripItemEntryInputConfirmed = currentViewModel::onStripItemEntryInputConfirmed,
                    onStripItemEntryInputFocusLost = currentViewModel::onStripItemEntryInputFocusLost,
                    onTileLeftOperandTapped = currentViewModel::onTileLeftOperandTapped,
                    onTileRightOperandTapped = currentViewModel::onTileRightOperandTapped,
                    onTileOperandSelectionDismissed = currentViewModel::onTileOperandSelectionDismissed,
                    onTileOperandSelectionConfirmed = currentViewModel::onTileOperandSelectionConfirmed,
                    onTileOperatorTapped = onTileOperatorTapped,
                    onTileResetTapped = currentViewModel::onTileResetTapped,
                    onTileOperatorSelectionDismissed = currentViewModel::onTileOperatorSelectionDismissed,
                    onTileOperatorSelectionConfirmed = currentViewModel::onTileOperatorSelectionConfirmed,
                    onSuccessOverlayDismissed = onSuccessOverlayDismissed,
                    isSuccessOverlayEnabled = isSuccessOverlayEnabled,
                    highlightState = highlightState
                )
            }
        }

        screen = GameScreenRobot(
            activity = composeTestRule.activity,
            interactions = composeTestRule
        )
    }

    protected fun showSolvedOverlayFixture() {
        composeTestRule.runOnIdle {
            useSolvedOverlayFixture = true
            isSolvedOverlayVisible = true
        }
    }

    protected fun showInteractiveSuccessOverlayFixture() {
        composeTestRule.runOnIdle {
            uiStateOverride = solvedOverlayUiState(isSuccessOverlayVisible = true)
            onTileOperatorTappedOverride = { tileIndex ->
                uiStateOverride = uiStateOverride?.copy(
                    tileOperatorSelectionDialog = TileOperatorSelectionDialogUiState(
                        tileIndex = tileIndex,
                        availableOperators = listOf(
                            Operator.ADDITION,
                            Operator.MULTIPLICATION
                        )
                    )
                )
            }
            onSuccessOverlayDismissedOverride = {
                uiStateOverride = uiStateOverride?.copy(isSuccessOverlayVisible = false)
            }
        }
    }

    protected fun showInvalidOutcomeFixture(completionState: PuzzleCompletionState) {
        composeTestRule.runOnIdle {
            uiStateOverride = invalidOutcomeUiState(completionState = completionState)
        }
    }

    protected fun showInteractiveMismatchedPairingFixture() {
        composeTestRule.runOnIdle {
            uiStateOverride = mismatchedPairingUiState()
            onTileOperatorTappedOverride = { tileIndex ->
                uiStateOverride = uiStateOverride?.copy(
                    tileOperatorSelectionDialog = TileOperatorSelectionDialogUiState(
                        tileIndex = tileIndex,
                        availableOperators = listOf(
                            Operator.ADDITION,
                            Operator.MULTIPLICATION
                        )
                    )
                )
            }
        }
    }

    protected fun showUiStateFixture(uiState: GameUiState) {
        composeTestRule.runOnIdle {
            uiStateOverride = uiState
        }
    }

    protected fun showHighlightState(highlightState: GameHighlightState) {
        composeTestRule.runOnIdle {
            this.highlightState = highlightState
        }
    }

    protected fun disableSuccessOverlay() {
        composeTestRule.runOnIdle {
            isSuccessOverlayEnabled = false
        }
    }
}
