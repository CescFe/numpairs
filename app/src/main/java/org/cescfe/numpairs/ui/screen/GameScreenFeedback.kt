package org.cescfe.numpairs.ui.screen

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.BorderStroke
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
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import org.cescfe.numpairs.R
import org.cescfe.numpairs.domain.puzzle.PuzzleCompletionState

@Composable
internal fun SuccessOverlay(onDismiss: () -> Unit) {
    val interactionSource = remember { MutableInteractionSource() }

    BackHandler(onBack = onDismiss)

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.scrim.copy(alpha = SUCCESS_OVERLAY_SCRIM_ALPHA))
            .clickable(
                interactionSource = interactionSource,
                indication = null,
                onClick = onDismiss
            )
            .testTag(GameScreenTestTags.SUCCESS_OVERLAY),
        contentAlignment = Alignment.Center
    ) {
        Surface(
            modifier = Modifier
                .padding(horizontal = 32.dp)
                .widthIn(max = SUCCESS_OVERLAY_CARD_MAX_WIDTH),
            shape = RoundedCornerShape(SUCCESS_OVERLAY_CARD_CORNER_RADIUS),
            color = SUCCESS_OVERLAY_SUCCESS_GREEN_SOFT,
            contentColor = SUCCESS_OVERLAY_SUCCESS_GREEN,
            border = BorderStroke(width = 1.dp, color = SUCCESS_OVERLAY_SUCCESS_GREEN)
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
                    color = SUCCESS_OVERLAY_SUCCESS_GREEN.copy(alpha = 0.12f),
                    contentColor = SUCCESS_OVERLAY_SUCCESS_GREEN
                ) {
                    Box(
                        modifier = Modifier
                            .width(SUCCESS_OVERLAY_BADGE_SIZE)
                            .heightIn(min = SUCCESS_OVERLAY_BADGE_SIZE),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "OK",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
                Text(
                    text = stringResource(R.string.success_overlay_message),
                    modifier = Modifier.testTag(GameScreenTestTags.SUCCESS_OVERLAY_MESSAGE),
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.SemiBold
                )
                Text(
                    text = stringResource(R.string.success_overlay_supporting_text),
                    style = MaterialTheme.typography.bodyMedium
                )
            }
        }
    }
}

@Composable
internal fun PuzzleOutcomeBanner(puzzleOutcome: PuzzleOutcomeUiState.Invalid, modifier: Modifier = Modifier) {
    val colorScheme = MaterialTheme.colorScheme

    Surface(
        modifier = modifier.testTag(GameScreenTestTags.PUZZLE_OUTCOME),
        shape = RoundedCornerShape(PUZZLE_OUTCOME_CORNER_RADIUS),
        color = colorScheme.errorContainer,
        contentColor = colorScheme.onErrorContainer,
        border = BorderStroke(width = 1.dp, color = colorScheme.error)
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
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold
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
