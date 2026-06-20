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
import androidx.compose.material3.MaterialTheme
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
import org.cescfe.numpairs.R
import org.cescfe.numpairs.data.puzzle.seed.initialPuzzle
import org.cescfe.numpairs.domain.puzzle.OperandSlot
import org.cescfe.numpairs.domain.puzzle.Operator
import org.cescfe.numpairs.feature.game.GameCompletionActions
import org.cescfe.numpairs.feature.game.GameHighlightState
import org.cescfe.numpairs.feature.game.GameInteractionPolicy
import org.cescfe.numpairs.feature.game.presentation.GameUiState
import org.cescfe.numpairs.feature.game.presentation.PuzzleOutcomeUiState
import org.cescfe.numpairs.feature.game.presentation.RuleConflictUiState
import org.cescfe.numpairs.feature.game.presentation.TileOperandOptionUiState
import org.cescfe.numpairs.feature.game.presentation.TileOperandSelectionDialogUiState
import org.cescfe.numpairs.feature.game.presentation.TileOperatorSelectionDialogUiState
import org.cescfe.numpairs.feature.game.presentation.TileUiState
import org.cescfe.numpairs.ui.theme.NumPairsComponents
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
    onRulesHelperPlayTutorialRequested: (() -> Unit)? = null,
    isSuccessOverlayEnabled: Boolean = true,
    interactionPolicy: GameInteractionPolicy = GameInteractionPolicy.AllowAll,
    highlightState: GameHighlightState = GameHighlightState.None,
    topBarActions: @Composable RowScope.() -> Unit = {},
    contentBeforePuzzle: @Composable ColumnScope.() -> Unit = {}
) {
    var isRulesHelperVisible by rememberSaveable { mutableStateOf(false) }
    val localRuleConflict = uiState.localRuleConflict()

    Box(
        modifier = modifier
            .fillMaxSize()
            .testTag(GameScreenTestTags.SCREEN)
    ) {
        Scaffold(
            modifier = Modifier.fillMaxSize(),
            containerColor = MaterialTheme.colorScheme.background,
            contentColor = MaterialTheme.colorScheme.onBackground,
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
                    .padding(
                        horizontal = GAME_SCREEN_HORIZONTAL_PADDING,
                        vertical = GAME_SCREEN_VERTICAL_PADDING
                    ),
                verticalArrangement = Arrangement.spacedBy(GAME_SCREEN_SECTION_SPACING)
            ) {
                contentBeforePuzzle()
                StripSection(
                    stripItems = uiState.stripItems,
                    onStripItemTapped = onStripItemTapped,
                    isStripItemEnabled = interactionPolicy.canTapStripItem,
                    highlightState = highlightState,
                    modifier = Modifier.fillMaxWidth()
                )
                localRuleConflict?.let { conflict ->
                    LocalRuleConflictBanner(
                        conflict = conflict,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
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
                    tileOperatorSelectionDialog = uiState.tileOperatorSelectionDialog.restrictedBy(
                        interactionPolicy = interactionPolicy
                    ),
                    onTileOperatorSelectionDismissed = onTileOperatorSelectionDismissed,
                    onTileOperatorSelectionConfirmed = onTileOperatorSelectionConfirmed,
                    interactionPolicy = interactionPolicy,
                    highlightState = highlightState,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }

        if (
            isSuccessOverlayEnabled &&
            uiState.isSuccessOverlayVisible &&
            uiState.puzzleOutcome == PuzzleOutcomeUiState.Solved
        ) {
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
            onConfirm = onStripItemEntryConfirmed,
            canConfirm = { value ->
                interactionPolicy.canConfirmStripItemEntry(dialogUiState.stripItemIndex, value)
            }
        )
    }
    uiState.tileOperandSelectionDialog?.let { dialogUiState ->
        TileOperandSelectionSheet(
            dialogUiState = dialogUiState.restrictedBy(interactionPolicy = interactionPolicy),
            onDismiss = onTileOperandSelectionDismissed,
            onConfirm = onTileOperandSelectionConfirmed
        )
    }
    if (isRulesHelperEnabled && isRulesHelperVisible) {
        RulesHelperDialog(
            onDismiss = {
                isRulesHelperVisible = false
            },
            onPlayTutorialRequested = onRulesHelperPlayTutorialRequested
        )
    }
}

private fun GameUiState.localRuleConflict(): RuleConflictUiState? {
    if (puzzleOutcome != null) {
        return null
    }

    val conflicts = tiles.flatMap(TileUiState::liveRuleConflicts).toSet()

    return when {
        RuleConflictUiState.DUPLICATE_OPERATOR_USAGE in conflicts -> RuleConflictUiState.DUPLICATE_OPERATOR_USAGE
        RuleConflictUiState.MISMATCHED_PAIRING in conflicts -> RuleConflictUiState.MISMATCHED_PAIRING
        else -> null
    }
}

private fun TileOperatorSelectionDialogUiState?.restrictedBy(
    interactionPolicy: GameInteractionPolicy
): TileOperatorSelectionDialogUiState? = this?.let { dialog ->
    dialog.copy(
        availableOperators = dialog.availableOperators.filter { operator ->
            interactionPolicy.canConfirmTileOperator(dialog.tileIndex, operator)
        }
    ).takeIf { restrictedDialog ->
        restrictedDialog.availableOperators.isNotEmpty()
    }
}

private fun TileOperandSelectionDialogUiState.restrictedBy(
    interactionPolicy: GameInteractionPolicy
): TileOperandSelectionDialogUiState = copy(
    availableOperands = availableOperands.map { operand ->
        operand.restrictedBy(
            tileIndex = tileIndex,
            slot = slot,
            interactionPolicy = interactionPolicy
        )
    }
)

private fun TileOperandOptionUiState.restrictedBy(
    tileIndex: Int,
    slot: OperandSlot,
    interactionPolicy: GameInteractionPolicy
): TileOperandOptionUiState = copy(
    isSelectable = isSelectable &&
        interactionPolicy.canConfirmTileOperand(
            tileIndex,
            slot,
            stripEntryId
        )
)

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
        colors = NumPairsComponents.topAppBarColors(),
        expandedHeight = GAME_TOP_BAR_HEIGHT,
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
            actions()
            if (isRulesHelperEnabled) {
                RulesHelperAction(onClick = onRulesHelperClick)
            }
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
