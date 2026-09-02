package org.cescfe.numpairs.feature.game.ui.screen

import androidx.activity.compose.BackHandler
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
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
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import org.cescfe.numpairs.R
import org.cescfe.numpairs.domain.puzzle.model.PuzzleCompletionState
import org.cescfe.numpairs.feature.game.GameCompletionActions
import org.cescfe.numpairs.feature.game.GameSuccessOverlayContent
import org.cescfe.numpairs.feature.game.GameSuccessOverlayCopy
import org.cescfe.numpairs.feature.game.GameSuccessOverlayStandardBadge
import org.cescfe.numpairs.feature.game.GameSuccessOverlayVisualStyle
import org.cescfe.numpairs.feature.game.presentation.PuzzleOutcomeUiState
import org.cescfe.numpairs.feature.game.presentation.RuleConflictUiState
import org.cescfe.numpairs.feature.game.ui.semantics.completionFeedbackSemantics
import org.cescfe.numpairs.ui.theme.NumPairsComponents
import org.cescfe.numpairs.ui.theme.numPairsSemanticColors

@Composable
internal fun SuccessOverlay(
    onDismiss: () -> Unit,
    completionActions: GameCompletionActions? = null,
    content: GameSuccessOverlayContent? = null,
    celebrationCopy: GameSuccessOverlayCopy? = null,
    completionFeedbackId: Long? = null,
    confettiCelebrationId: Long? = null,
    onConfettiCelebrationStarted: () -> Unit = {},
    isConfettiAnimationEnabled: Boolean = true
) {
    require(content == null || completionActions == null) {
        "A success overlay cannot combine custom content with generated-puzzle completion actions."
    }
    require(content == null || celebrationCopy == null) {
        "A success overlay cannot combine custom content with separate celebration copy."
    }

    val interactionSource = remember { MutableInteractionSource() }
    val isPersonalRecord = content?.visualStyle == GameSuccessOverlayVisualStyle.PERSONAL_RECORD
    val usesCompletionCheck = !isPersonalRecord &&
        (celebrationCopy != null || content?.standardBadge == GameSuccessOverlayStandardBadge.CHECK)
    val standardBadgeContentDescription = if (usesCompletionCheck) {
        stringResource(R.string.success_overlay_badge_content_description)
    } else {
        null
    }
    val currentOnConfettiCelebrationStarted by rememberUpdatedState(onConfettiCelebrationStarted)
    var activeConfettiCelebrationId by remember { mutableStateOf<Long?>(null) }
    val entranceProgress = remember {
        Animatable(if (completionFeedbackId == null) 1f else 0f)
    }

    LaunchedEffect(completionFeedbackId) {
        if (completionFeedbackId == null) {
            entranceProgress.snapTo(1f)
        } else {
            entranceProgress.snapTo(0f)
            entranceProgress.animateTo(
                targetValue = 1f,
                animationSpec = tween(
                    durationMillis = COMPLETION_OVERLAY_ENTRANCE_DURATION_MILLIS,
                    easing = FastOutSlowInEasing
                )
            )
        }
    }

    LaunchedEffect(confettiCelebrationId, isPersonalRecord, isConfettiAnimationEnabled) {
        if (
            confettiCelebrationId != null &&
            isPersonalRecord &&
            activeConfettiCelebrationId == null
        ) {
            if (isConfettiAnimationEnabled) {
                activeConfettiCelebrationId = confettiCelebrationId
            }
            currentOnConfettiCelebrationStarted()
        }
    }

    val overlayModifier = Modifier
        .fillMaxSize()
        .background(
            MaterialTheme.colorScheme.scrim.copy(
                alpha = SUCCESS_OVERLAY_SCRIM_ALPHA * entranceProgress.value
            )
        )
        .testTag(GameScreenTestTags.SUCCESS_OVERLAY)
        .completionFeedbackSemantics(completionFeedbackId)
    val dismissibleOverlayModifier = if (completionActions == null && content == null) {
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
        onBack = content?.onBackRequested
            ?: content?.onPrimaryAction
            ?: completionActions?.onReturnToMenuRequested
            ?: onDismiss
    )

    Box(
        modifier = dismissibleOverlayModifier,
        contentAlignment = Alignment.Center
    ) {
        PersonalRecordConfetti(
            celebrationId = activeConfettiCelebrationId,
            animationEnabled = isConfettiAnimationEnabled
        )
        Surface(
            modifier = Modifier
                .padding(horizontal = 32.dp)
                .widthIn(max = SUCCESS_OVERLAY_CARD_MAX_WIDTH)
                .graphicsLayer {
                    val progress = entranceProgress.value
                    val scale = COMPLETION_OVERLAY_INITIAL_SCALE +
                        ((1f - COMPLETION_OVERLAY_INITIAL_SCALE) * progress)
                    scaleX = scale
                    scaleY = scale
                    alpha = progress
                },
            shape = RoundedCornerShape(SUCCESS_OVERLAY_CARD_CORNER_RADIUS),
            color = if (isPersonalRecord) {
                NumPairsComponents.recordContainerColor()
            } else {
                NumPairsComponents.successContainerColor()
            },
            contentColor = if (isPersonalRecord) {
                NumPairsComponents.recordContentColor()
            } else {
                NumPairsComponents.successContentColor()
            },
            border = if (isPersonalRecord) {
                NumPairsComponents.recordBorder()
            } else {
                NumPairsComponents.successBorder()
            }
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState())
                    .padding(
                        horizontal = SUCCESS_OVERLAY_HORIZONTAL_PADDING,
                        vertical = SUCCESS_OVERLAY_VERTICAL_PADDING
                    ),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Surface(
                    modifier = Modifier
                        .testTag(GameScreenTestTags.SUCCESS_OVERLAY_BADGE)
                        .let { modifier ->
                            (content?.badgeContentDescription ?: standardBadgeContentDescription)?.let { description ->
                                modifier.semantics {
                                    contentDescription = description
                                }
                            } ?: modifier
                        },
                    shape = CircleShape,
                    color = if (isPersonalRecord) {
                        MaterialTheme.numPairsSemanticColors.record.copy(alpha = 0.18f)
                    } else {
                        MaterialTheme.numPairsSemanticColors.success.copy(alpha = 0.16f)
                    },
                    contentColor = if (isPersonalRecord) {
                        MaterialTheme.numPairsSemanticColors.record
                    } else {
                        MaterialTheme.numPairsSemanticColors.success
                    }
                ) {
                    Box(
                        modifier = Modifier
                            .width(SUCCESS_OVERLAY_BADGE_SIZE)
                            .heightIn(min = SUCCESS_OVERLAY_BADGE_SIZE),
                        contentAlignment = Alignment.Center
                    ) {
                        if (isPersonalRecord) {
                            Icon(
                                painter = painterResource(R.drawable.ic_personal_record_star),
                                contentDescription = null,
                                modifier = Modifier.size(SUCCESS_OVERLAY_BADGE_ICON_SIZE)
                            )
                        } else if (usesCompletionCheck) {
                            Icon(
                                painter = painterResource(R.drawable.ic_completion_check),
                                contentDescription = null,
                                modifier = Modifier.size(SUCCESS_OVERLAY_BADGE_ICON_SIZE)
                            )
                        } else {
                            Text(
                                text = stringResource(R.string.success_overlay_badge_text),
                                style = MaterialTheme.typography.titleMedium
                            )
                        }
                    }
                }
                Text(
                    text = content?.message
                        ?: celebrationCopy?.message
                        ?: stringResource(R.string.success_overlay_message),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag(GameScreenTestTags.SUCCESS_OVERLAY_MESSAGE),
                    style = MaterialTheme.typography.headlineSmall,
                    textAlign = TextAlign.Center
                )
                content?.highlightText?.let { highlightText ->
                    val highlightModifier = Modifier
                        .fillMaxWidth()
                        .testTag(GameScreenTestTags.SUCCESS_OVERLAY_HIGHLIGHT)
                        .let { modifier ->
                            content.highlightContentDescription?.let { description ->
                                modifier.semantics {
                                    contentDescription = description
                                }
                            } ?: modifier
                        }
                    Text(
                        text = highlightText,
                        modifier = highlightModifier,
                        style = MaterialTheme.typography.headlineMedium.copy(
                            fontFamily = FontFamily.Monospace,
                            fontFeatureSettings = "tnum",
                            fontWeight = FontWeight.Bold
                        ),
                        textAlign = TextAlign.Center
                    )
                }
                content?.contextText?.let { contextText ->
                    val contextModifier = Modifier
                        .fillMaxWidth()
                        .testTag(GameScreenTestTags.SUCCESS_OVERLAY_CONTEXT)
                        .let { modifier ->
                            content.contextContentDescription?.let { description ->
                                modifier.semantics {
                                    contentDescription = description
                                }
                            } ?: modifier
                        }
                    Text(
                        text = contextText,
                        modifier = contextModifier,
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.SemiBold
                        ),
                        textAlign = TextAlign.Center
                    )
                }
                Text(
                    text = content?.supportingText
                        ?: celebrationCopy?.supportingText
                        ?: stringResource(R.string.success_overlay_supporting_text),
                    modifier = Modifier.fillMaxWidth(),
                    style = MaterialTheme.typography.bodyMedium,
                    textAlign = TextAlign.Center
                )
                if (content != null) {
                    NumPairsComponents.PrimaryCtaButton(
                        onClick = content.onPrimaryAction,
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag(GameScreenTestTags.SUCCESS_OVERLAY_PRIMARY_ACTION)
                    ) {
                        Text(
                            text = content.primaryActionLabel,
                            style = MaterialTheme.typography.labelLarge.copy(
                                fontWeight = FontWeight.Bold
                            )
                        )
                    }
                    content.secondaryActionLabel?.let { label ->
                        OutlinedButton(
                            onClick = requireNotNull(content.onSecondaryAction),
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag(GameScreenTestTags.SUCCESS_OVERLAY_SECONDARY_ACTION),
                            shape = NumPairsComponents.MediumShape,
                            colors = NumPairsComponents.secondaryButtonColors(),
                            border = NumPairsComponents.secondaryButtonBorder()
                        ) {
                            Text(text = label)
                        }
                    }
                    content.tertiaryActionLabel?.let { label ->
                        TextButton(
                            onClick = requireNotNull(content.onTertiaryAction),
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag(GameScreenTestTags.SUCCESS_OVERLAY_TERTIARY_ACTION)
                        ) {
                            Text(text = label)
                        }
                    }
                } else {
                    completionActions?.let { actions ->
                        Column(
                            modifier = Modifier
                                .fillMaxWidth(),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            NumPairsComponents.PrimaryCtaButton(
                                onClick = actions.onNewPuzzleRequested,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .testTag(GameScreenTestTags.SUCCESS_OVERLAY_NEW_PUZZLE)
                            ) {
                                Text(
                                    text = stringResource(R.string.success_overlay_new_puzzle_button),
                                    style = MaterialTheme.typography.labelLarge.copy(
                                        fontWeight = FontWeight.Bold
                                    )
                                )
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
