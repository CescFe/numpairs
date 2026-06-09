package org.cescfe.numpairs.feature.game.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import org.cescfe.numpairs.R
import org.cescfe.numpairs.data.puzzle.seed.initialPuzzle
import org.cescfe.numpairs.domain.puzzle.Operator
import org.cescfe.numpairs.feature.game.GameCompletionActions
import org.cescfe.numpairs.feature.game.presentation.GameUiState
import org.cescfe.numpairs.feature.game.presentation.PuzzleOutcomeUiState
import org.cescfe.numpairs.ui.theme.NumPairsTheme

@Composable
fun GameScreen(
    title: String,
    uiState: GameUiState,
    modifier: Modifier = Modifier,
    onNavigateBack: () -> Unit = {},
    onStripItemTapped: (Int) -> Unit = {},
    onStripItemEntryDismissed: () -> Unit = {},
    onStripItemEntryConfirmed: (Int) -> Unit = {},
    onTileLeftOperandTapped: (Int) -> Unit = {},
    onTileRightOperandTapped: (Int) -> Unit = {},
    onTileOperandSelectionDismissed: () -> Unit = {},
    onTileOperandSelectionConfirmed: (Int) -> Unit = {},
    onTileOperatorTapped: (Int) -> Unit = {},
    onTileResetTapped: (Int) -> Unit = {},
    onTileOperatorSelectionDismissed: () -> Unit = {},
    onTileOperatorSelectionConfirmed: (Operator) -> Unit = {},
    onSuccessOverlayDismissed: () -> Unit = {},
    completionActions: GameCompletionActions? = null,
    isRulesHelperEnabled: Boolean = false,
    topBarActions: @Composable RowScope.() -> Unit = {},
    contentBeforePuzzle: @Composable ColumnScope.() -> Unit = {}
) {
    var isRulesHelperVisible by rememberSaveable { mutableStateOf(false) }

    Box(
        modifier = modifier
            .fillMaxSize()
            .testTag(GameScreenTestTags.SCREEN)
    ) {
        Scaffold(
            modifier = Modifier.fillMaxSize(),
            topBar = {
                GameScreenTopBar(
                    title = title,
                    onNavigateBack = onNavigateBack,
                    onRulesHelperClick = {
                        isRulesHelperVisible = true
                    },
                    isRulesHelperEnabled = isRulesHelperEnabled,
                    actions = topBarActions
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
                contentBeforePuzzle()
                StripSection(
                    stripItems = uiState.stripItems,
                    onStripItemTapped = onStripItemTapped,
                    modifier = Modifier.fillMaxWidth()
                )
                (uiState.puzzleOutcome as? PuzzleOutcomeUiState.Invalid)?.let { puzzleOutcome ->
                    PuzzleOutcomeBanner(
                        puzzleOutcome = puzzleOutcome,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
                BoardSection(
                    tiles = uiState.tiles,
                    onTileLeftOperandTapped = onTileLeftOperandTapped,
                    onTileRightOperandTapped = onTileRightOperandTapped,
                    onTileOperatorTapped = onTileOperatorTapped,
                    onTileResetTapped = onTileResetTapped,
                    tileOperatorSelectionDialog = uiState.tileOperatorSelectionDialog,
                    onTileOperatorSelectionDismissed = onTileOperatorSelectionDismissed,
                    onTileOperatorSelectionConfirmed = onTileOperatorSelectionConfirmed,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }

        if (uiState.isSuccessOverlayVisible && uiState.puzzleOutcome == PuzzleOutcomeUiState.Solved) {
            SuccessOverlay(
                onDismiss = onSuccessOverlayDismissed,
                completionActions = completionActions
            )
        }
    }

    uiState.stripItemEntryDialog?.let { dialogUiState ->
        StripItemEntryDialog(
            dialogUiState = dialogUiState,
            onDismiss = onStripItemEntryDismissed,
            onConfirm = onStripItemEntryConfirmed
        )
    }
    uiState.tileOperandSelectionDialog?.let { dialogUiState ->
        TileOperandSelectionSheet(
            dialogUiState = dialogUiState,
            onDismiss = onTileOperandSelectionDismissed,
            onConfirm = onTileOperandSelectionConfirmed
        )
    }
    if (isRulesHelperEnabled && isRulesHelperVisible) {
        RulesHelperDialog(
            onDismiss = {
                isRulesHelperVisible = false
            }
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun GameScreenTopBar(
    title: String,
    onNavigateBack: () -> Unit,
    onRulesHelperClick: () -> Unit,
    isRulesHelperEnabled: Boolean,
    actions: @Composable RowScope.() -> Unit
) {
    val backButtonContentDescription = stringResource(R.string.back_button_content_description)

    TopAppBar(
        navigationIcon = {
            IconButton(
                onClick = onNavigateBack,
                modifier = Modifier.testTag(GameScreenTestTags.BACK_BUTTON)
            ) {
                Icon(
                    painter = painterResource(R.drawable.ic_chevron_left),
                    contentDescription = backButtonContentDescription
                )
            }
        },
        title = {
            Text(text = title)
        },
        actions = {
            if (isRulesHelperEnabled) {
                RulesHelperAction(onClick = onRulesHelperClick)
            }
            actions()
        }
    )
}

@Composable
private fun RulesHelperAction(onClick: () -> Unit) {
    IconButton(
        onClick = onClick,
        modifier = Modifier.testTag(GameScreenTestTags.RULES_HELPER_ACTION)
    ) {
        Icon(
            painter = painterResource(R.drawable.ic_help),
            contentDescription = stringResource(R.string.rules_helper_action_content_description)
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun GameScreenPreview() {
    NumPairsTheme {
        GameScreen(
            title = stringResource(R.string.tutorial_screen_title),
            uiState = GameUiState.from(initialPuzzle)
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun GameScreenWithTopBarActionPreview() {
    NumPairsTheme {
        GameScreen(
            title = stringResource(R.string.tutorial_screen_title),
            uiState = GameUiState.from(initialPuzzle),
            topBarActions = {
                IconButton(onClick = {}) {
                    Text(text = "A")
                }
            }
        )
    }
}
