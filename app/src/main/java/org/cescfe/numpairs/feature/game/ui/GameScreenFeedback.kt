package org.cescfe.numpairs.feature.game.ui

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import org.cescfe.numpairs.R
import org.cescfe.numpairs.domain.puzzle.PuzzleCompletionState
import org.cescfe.numpairs.feature.game.GameCompletionActions
import org.cescfe.numpairs.feature.game.presentation.PuzzleOutcomeUiState
import org.cescfe.numpairs.feature.game.presentation.RuleConflictUiState
import org.cescfe.numpairs.ui.theme.NumPairsComponents

@Composable
internal fun SuccessOverlay(onDismiss: () -> Unit, completionActions: GameCompletionActions? = null) {
    val interactionSource = remember { MutableInteractionSource() }
    val overlayModifier = Modifier
        .fillMaxSize()
        .background(MaterialTheme.colorScheme.scrim.copy(alpha = SUCCESS_OVERLAY_SCRIM_ALPHA))
        .testTag(GameScreenTestTags.SUCCESS_OVERLAY)
    val dismissibleOverlayModifier = if (completionActions == null) {
        overlayModifier.clickable(
            interactionSource = interactionSource,
            indication = null,
            onClick = onDismiss
        )
    } else {
        overlayModifier.clickable(
            interactionSource = interactionSource,
            indication = null,
            onClick = {}
        )
    }

    BackHandler(
        onBack = completionActions?.onReturnToMenuRequested ?: onDismiss
    )

    Box(
        modifier = dismissibleOverlayModifier,
        contentAlignment = Alignment.Center
    ) {
        Surface(
            modifier = Modifier
                .padding(horizontal = 32.dp)
                .widthIn(max = SUCCESS_OVERLAY_CARD_MAX_WIDTH),
            shape = RoundedCornerShape(SUCCESS_OVERLAY_CARD_CORNER_RADIUS),
            color = NumPairsComponents.successContainerColor(),
            contentColor = NumPairsComponents.successContentColor(),
            border = NumPairsComponents.focusBorder()
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(
                        horizontal = SUCCESS_OVERLAY_HORIZONTAL_PADDING,
                        vertical = SUCCESS_OVERLAY_VERTICAL_PADDING
                    ),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Surface(
                    shape = CircleShape,
                    color = MaterialTheme.colorScheme.primary.copy(alpha = 0.16f),
                    contentColor = MaterialTheme.colorScheme.primary
                ) {
                    Box(
                        modifier = Modifier
                            .width(SUCCESS_OVERLAY_BADGE_SIZE)
                            .heightIn(min = SUCCESS_OVERLAY_BADGE_SIZE),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "OK",
                            style = MaterialTheme.typography.titleMedium
                        )
                    }
                }
                Text(
                    text = stringResource(R.string.success_overlay_message),
                    modifier = Modifier.testTag(GameScreenTestTags.SUCCESS_OVERLAY_MESSAGE),
                    style = MaterialTheme.typography.headlineSmall
                )
                Text(
                    text = stringResource(R.string.success_overlay_supporting_text),
                    style = MaterialTheme.typography.bodyMedium
                )
                completionActions?.let { actions ->
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        NumPairsComponents.PrimaryCtaButton(
                            onClick = actions.onNewPuzzleRequested,
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag(GameScreenTestTags.SUCCESS_OVERLAY_NEW_PUZZLE)
                        ) {
                            Text(text = stringResource(R.string.success_overlay_new_puzzle_button))
                        }
                        OutlinedButton(
                            onClick = actions.onReturnToMenuRequested,
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag(GameScreenTestTags.SUCCESS_OVERLAY_RETURN_TO_MENU),
                            shape = NumPairsComponents.MediumShape,
                            colors = NumPairsComponents.secondaryButtonColors(),
                            border = NumPairsComponents.secondaryButtonBorder()
                        ) {
                            Text(text = stringResource(R.string.success_overlay_return_to_menu_button))
                        }
                    }
                }
            }
        }
    }
}

@Composable
internal fun LocalRuleConflictBanner(conflict: RuleConflictUiState, modifier: Modifier = Modifier) {
    val message = conflict.localRuleConflictMessage()

    Surface(
        modifier = modifier
            .testTag(GameScreenTestTags.LOCAL_RULE_CONFLICT)
            .semantics {
                contentDescription = message
            },
        shape = NumPairsComponents.MediumShape,
        color = NumPairsComponents.errorContainerColor(),
        contentColor = NumPairsComponents.errorContentColor(),
        border = NumPairsComponents.errorBorder()
    ) {
        Text(
            text = message,
            modifier = Modifier
                .fillMaxWidth()
                .padding(
                    horizontal = PUZZLE_OUTCOME_HORIZONTAL_PADDING,
                    vertical = 10.dp
                )
                .testTag(GameScreenTestTags.LOCAL_RULE_CONFLICT_MESSAGE),
            style = MaterialTheme.typography.bodyMedium
        )
    }
}

@Composable
internal fun PuzzleOutcomeBanner(puzzleOutcome: PuzzleOutcomeUiState.Invalid, modifier: Modifier = Modifier) {
    Surface(
        modifier = modifier.testTag(GameScreenTestTags.PUZZLE_OUTCOME),
        shape = RoundedCornerShape(PUZZLE_OUTCOME_CORNER_RADIUS),
        color = NumPairsComponents.errorContainerColor(),
        contentColor = NumPairsComponents.errorContentColor(),
        border = NumPairsComponents.errorBorder()
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(
                    horizontal = PUZZLE_OUTCOME_HORIZONTAL_PADDING,
                    vertical = PUZZLE_OUTCOME_VERTICAL_PADDING
                ),
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Text(
                text = stringResource(R.string.puzzle_outcome_invalid_title),
                modifier = Modifier.testTag(GameScreenTestTags.PUZZLE_OUTCOME_TITLE),
                style = MaterialTheme.typography.titleMedium
            )
            Text(
                text = puzzleOutcome.message(),
                modifier = Modifier.testTag(GameScreenTestTags.PUZZLE_OUTCOME_MESSAGE),
                style = MaterialTheme.typography.bodyMedium
            )
        }
    }
}

@Composable
private fun RuleConflictUiState.localRuleConflictMessage(): String = when (this) {
    RuleConflictUiState.DUPLICATE_OPERATOR_USAGE ->
        stringResource(R.string.local_rule_conflict_duplicate_operator_usage_message)
    RuleConflictUiState.MISMATCHED_PAIRING ->
        stringResource(R.string.local_rule_conflict_mismatched_pairing_message)
}

@Composable
internal fun PuzzleOutcomeUiState.Invalid.message(): String = when (completionState) {
    PuzzleCompletionState.INCORRECT_TILES -> stringResource(R.string.puzzle_outcome_invalid_tiles_message)
    PuzzleCompletionState.MISSING_STRIP_ENTRY_IDENTITIES ->
        stringResource(R.string.puzzle_outcome_missing_identities_message)
    PuzzleCompletionState.MISMATCHED_SUM_PRODUCT_PAIRINGS ->
        stringResource(R.string.puzzle_outcome_mismatched_pairings_message)
    PuzzleCompletionState.INVALID_STRIP_ENTRY_USAGE ->
        stringResource(R.string.puzzle_outcome_invalid_usage_message)
    PuzzleCompletionState.INCOMPLETE,
    PuzzleCompletionState.SOLVED -> error("Invalid outcome must represent a completed unsolved puzzle.")
}
