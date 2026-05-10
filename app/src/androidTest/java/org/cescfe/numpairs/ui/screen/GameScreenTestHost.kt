package org.cescfe.numpairs.ui.screen

import androidx.activity.ComponentActivity
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.test.junit4.v2.createAndroidComposeRule
import org.cescfe.numpairs.domain.puzzle.Operator
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
    private var useSolvedOverlayFixture by mutableStateOf(false)
    private var isSolvedOverlayVisible by mutableStateOf(false)

    @Before
    fun setUpGameScreenHost() {
        viewModel = GameViewModel()
        uiStateOverride = null
        onTileOperatorTappedOverride = null
        onSuccessOverlayDismissedOverride = null
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
                    uiState = uiState,
                    onStripItemTapped = currentViewModel::onStripItemTapped,
                    onStripItemEntryDismissed = currentViewModel::onStripItemEntryDismissed,
                    onStripItemEntryConfirmed = currentViewModel::onStripItemEntryConfirmed,
                    onTileLeftOperandTapped = currentViewModel::onTileLeftOperandTapped,
                    onTileRightOperandTapped = currentViewModel::onTileRightOperandTapped,
                    onTileOperandSelectionDismissed = currentViewModel::onTileOperandSelectionDismissed,
                    onTileOperandSelectionConfirmed = currentViewModel::onTileOperandSelectionConfirmed,
                    onTileOperatorTapped = onTileOperatorTapped,
                    onTileResetTapped = currentViewModel::onTileResetTapped,
                    onTileOperatorSelectionDismissed = currentViewModel::onTileOperatorSelectionDismissed,
                    onTileOperatorSelectionConfirmed = currentViewModel::onTileOperatorSelectionConfirmed,
                    onSuccessOverlayDismissed = onSuccessOverlayDismissed
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
}
