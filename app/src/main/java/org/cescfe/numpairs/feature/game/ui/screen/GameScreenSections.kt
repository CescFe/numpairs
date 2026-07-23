package org.cescfe.numpairs.feature.game.ui.screen

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.error
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.stateDescription
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.unit.Dp
import org.cescfe.numpairs.R
import org.cescfe.numpairs.domain.puzzle.model.Operator
import org.cescfe.numpairs.domain.puzzle.model.StripEntryRange
import org.cescfe.numpairs.feature.game.GameHighlightState
import org.cescfe.numpairs.feature.game.GameInteractionPolicy
import org.cescfe.numpairs.feature.game.GameTileExpressionSlot
import org.cescfe.numpairs.feature.game.presentation.StripItemEntryInputUiState
import org.cescfe.numpairs.feature.game.presentation.StripItemUiState
import org.cescfe.numpairs.feature.game.presentation.StripItemVisualStyle
import org.cescfe.numpairs.feature.game.presentation.TileOperatorSelectionDialogUiState
import org.cescfe.numpairs.feature.game.presentation.TileUiState
import org.cescfe.numpairs.feature.game.ui.components.strip.AvailableNumberChip
import org.cescfe.numpairs.feature.game.ui.components.strip.AvailableNumberChipStyle
import org.cescfe.numpairs.feature.game.ui.components.strip.AvailableNumberInputChip
import org.cescfe.numpairs.feature.game.ui.components.strip.CHIP_HORIZONTAL_CONTENT_INSET
import org.cescfe.numpairs.feature.game.ui.components.tile.PuzzleTile
import org.cescfe.numpairs.feature.game.ui.semantics.completionFeedbackSemantics
import org.cescfe.numpairs.feature.game.ui.semantics.gameHighlightSemantics
import org.cescfe.numpairs.ui.theme.NumPairsComponents
import org.cescfe.numpairs.ui.theme.NumPairsTextStyles
import org.cescfe.numpairs.ui.theme.numPairsSemanticColors

@Composable
internal fun BoardSection(
    modifier: Modifier = Modifier,
    tiles: List<TileUiState>,
    onTileLeftOperandTapped: (Int) -> Unit,
    onTileRightOperandTapped: (Int) -> Unit,
    onTileOperatorTapped: (Int) -> Unit,
    onTileResetTapped: (Int) -> Unit,
    tileOperatorSelectionDialog: TileOperatorSelectionDialogUiState?,
    onTileOperatorSelectionDismissed: () -> Unit,
    onTileOperatorSelectionConfirmed: (Operator) -> Unit,
    interactionPolicy: GameInteractionPolicy = GameInteractionPolicy.AllowAll,
    highlightState: GameHighlightState = GameHighlightState.None,
    correctTileFeedbackIdsByIndex: Map<Int, Long> = emptyMap(),
    completionFeedbackId: Long? = null
) {
    val boardContentDescription = stringResource(R.string.board_content_description)
    val completionScale = remember { Animatable(1f) }

    LaunchedEffect(completionFeedbackId) {
        completionScale.snapTo(1f)
        if (completionFeedbackId != null) {
            completionScale.animateTo(
                targetValue = COMPLETION_BOARD_FEEDBACK_SCALE,
                animationSpec = tween(
                    durationMillis = COMPLETION_BOARD_SCALE_DOWN_DURATION_MILLIS,
                    easing = FastOutSlowInEasing
                )
            )
            completionScale.animateTo(
                targetValue = 1f,
                animationSpec = tween(
                    durationMillis = COMPLETION_BOARD_SCALE_UP_DURATION_MILLIS,
                    easing = FastOutSlowInEasing
                )
            )
        }
    }

    BoxWithConstraints(
        modifier = modifier
            .graphicsLayer {
                scaleX = completionScale.value
                scaleY = completionScale.value
            }
            .testTag(GameScreenTestTags.BOARD)
            .completionFeedbackSemantics(completionFeedbackId)
            .semantics {
                contentDescription = boardContentDescription
            }
    ) {
        val visualColumnCount = calculateBoardColumnCount(maxWidth)
        val tileWidth = calculateBoardTileWidth(
            availableWidth = maxWidth,
            visualColumnCount = visualColumnCount
        )
        val visualRows = tiles.withIndex().toList().chunked(visualColumnCount)

        Column(
            verticalArrangement = Arrangement.spacedBy(BOARD_TILE_SPACING)
        ) {
            visualRows.forEach { row ->
                Box(
                    modifier = Modifier.fillMaxWidth(),
                    contentAlignment = Alignment.Center
                ) {
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(BOARD_TILE_SPACING)
                    ) {
                        row.forEach { indexedTile ->
                            val tileIndex = indexedTile.index
                            val tile = indexedTile.value
                            val tileOperatorSelectionUiState = tileOperatorSelectionDialog
                                ?.takeIf { dialogUiState -> dialogUiState.tileIndex == tileIndex }

                            PuzzleTile(
                                tile = tile,
                                isHighlighted = highlightState.isTileHighlighted(tileIndex),
                                correctFeedbackId = correctTileFeedbackIdsByIndex[tileIndex],
                                modifier = Modifier
                                    .testTag(GameScreenTestTags.tile(tileIndex))
                                    .width(tileWidth)
                                    .wrapContentHeight(),
                                isLeftOperandHighlighted = highlightState.isTileExpressionSlotHighlighted(
                                    tileIndex = tileIndex,
                                    slot = GameTileExpressionSlot.LEFT_OPERAND
                                ),
                                leftOperandModifier = Modifier.testTag(GameScreenTestTags.tileLeftOperand(tileIndex)),
                                leftOperandContentDescription = tileLeftOperandContentDescription(tile),
                                onLeftOperandClick = if (interactionPolicy.canTapTileLeftOperand(tileIndex)) {
                                    { onTileLeftOperandTapped(tileIndex) }
                                } else {
                                    null
                                },
                                isOperatorHighlighted = highlightState.isTileExpressionSlotHighlighted(
                                    tileIndex = tileIndex,
                                    slot = GameTileExpressionSlot.OPERATOR
                                ),
                                operatorModifier = Modifier.testTag(GameScreenTestTags.tileOperator(tileIndex)),
                                operatorContentDescription = tileOperatorContentDescription(tile),
                                onOperatorClick = if (interactionPolicy.canTapTileOperator(tileIndex)) {
                                    { onTileOperatorTapped(tileIndex) }
                                } else {
                                    null
                                },
                                operatorOverlay = {
                                    tileOperatorSelectionUiState?.let { dialogUiState ->
                                        TileOperatorSelectionMenu(
                                            dialogUiState = dialogUiState,
                                            onDismiss = onTileOperatorSelectionDismissed,
                                            onConfirm = onTileOperatorSelectionConfirmed
                                        )
                                    }
                                },
                                isRightOperandHighlighted = highlightState.isTileExpressionSlotHighlighted(
                                    tileIndex = tileIndex,
                                    slot = GameTileExpressionSlot.RIGHT_OPERAND
                                ),
                                rightOperandModifier = Modifier.testTag(GameScreenTestTags.tileRightOperand(tileIndex)),
                                rightOperandContentDescription = tileRightOperandContentDescription(tile),
                                onRightOperandClick = if (interactionPolicy.canTapTileRightOperand(tileIndex)) {
                                    { onTileRightOperandTapped(tileIndex) }
                                } else {
                                    null
                                },
                                resetModifier = Modifier.testTag(GameScreenTestTags.tileReset(tileIndex)),
                                onResetClick = if (interactionPolicy.canTapTileReset(tileIndex)) {
                                    { onTileResetTapped(tileIndex) }
                                } else {
                                    null
                                }
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
internal fun StripSection(
    modifier: Modifier = Modifier,
    stripItems: List<StripItemUiState>,
    stripItemEntryInput: StripItemEntryInputUiState? = null,
    onStripItemTapped: (Int) -> Unit,
    onStripItemEntryInputChanged: (String) -> Unit = {},
    onStripItemEntryInputConfirmed: () -> Unit = {},
    onStripItemEntryInputFocusLost: () -> Unit = {},
    stripItemEntryGuidance: String? = null,
    isStripItemEnabled: (Int) -> Boolean = { true },
    highlightState: GameHighlightState = GameHighlightState.None
) {
    val stripContentDescription = stringResource(R.string.strip_content_description)
    val stripEntryFeedbackText = stripItemEntryInput?.feedbackText(
        entryGuidance = stripItemEntryGuidance
    )
    val minimumChipWidth = requiredStripChipWidth(
        stripItems = stripItems,
        stripItemEntryInput = stripItemEntryInput
    )
    val stripBorder = if (highlightState.isStripHighlighted) {
        BorderStroke(
            width = STRIP_HIGHLIGHTED_BORDER_WIDTH,
            color = MaterialTheme.numPairsSemanticColors.tutorialHighlight
        )
    } else {
        NumPairsComponents.subtleBorder()
    }

    Surface(
        modifier = modifier
            .testTag(GameScreenTestTags.STRIP)
            .gameHighlightSemantics(highlightState.isStripHighlighted)
            .semantics {
                contentDescription = stripContentDescription
                stripEntryFeedbackText?.let { feedbackText ->
                    stateDescription = feedbackText
                }
            },
        color = NumPairsComponents.subtleSurfaceColor(),
        shape = NumPairsComponents.LargeShape,
        border = stripBorder
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(
                    horizontal = STRIP_HORIZONTAL_PADDING,
                    vertical = STRIP_VERTICAL_PADDING
                ),
            verticalArrangement = Arrangement.spacedBy(STRIP_ENTRY_FEEDBACK_TOP_SPACING)
        ) {
            BoxWithConstraints(modifier = Modifier.fillMaxWidth()) {
                val gridLayout = calculateStripGridLayout(
                    availableWidth = maxWidth,
                    entryCount = stripItems.size,
                    minimumChipWidth = minimumChipWidth,
                    maximumChipWidth = STRIP_CHIP_MAX_WIDTH
                )

                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(STRIP_ROW_SPACING)
                ) {
                    gridLayout.entryIndexesByRow.forEach { entryIndexes ->
                        Box(
                            modifier = Modifier.fillMaxWidth(),
                            contentAlignment = Alignment.Center
                        ) {
                            Row(
                                modifier = Modifier.wrapContentWidth(),
                                horizontalArrangement = Arrangement.spacedBy(STRIP_CHIP_SPACING)
                            ) {
                                entryIndexes.forEach { index ->
                                    StripEntryChip(
                                        stripItemIndex = index,
                                        stripItem = stripItems[index],
                                        chipWidth = gridLayout.chipWidth,
                                        stripItemEntryInput = stripItemEntryInput,
                                        stripEntryFeedbackText = stripEntryFeedbackText,
                                        onStripItemTapped = onStripItemTapped,
                                        onStripItemEntryInputChanged = onStripItemEntryInputChanged,
                                        onStripItemEntryInputConfirmed = onStripItemEntryInputConfirmed,
                                        onStripItemEntryInputFocusLost = onStripItemEntryInputFocusLost,
                                        isStripItemEnabled = isStripItemEnabled,
                                        isHighlighted = highlightState.isStripEntryHighlighted(index)
                                    )
                                }
                            }
                        }
                    }
                }
            }
            if (stripItemEntryInput != null && stripEntryFeedbackText != null) {
                StripEntryFeedback(
                    message = stripEntryFeedbackText,
                    isError = stripItemEntryInput.isInvalid,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag(GameScreenTestTags.STRIP_ENTRY_RANGE)
                )
            }
        }
    }
}

@Composable
private fun StripEntryChip(
    stripItemIndex: Int,
    stripItem: StripItemUiState,
    chipWidth: Dp,
    stripItemEntryInput: StripItemEntryInputUiState?,
    stripEntryFeedbackText: String?,
    onStripItemTapped: (Int) -> Unit,
    onStripItemEntryInputChanged: (String) -> Unit,
    onStripItemEntryInputConfirmed: () -> Unit,
    onStripItemEntryInputFocusLost: () -> Unit,
    isStripItemEnabled: (Int) -> Boolean,
    isHighlighted: Boolean
) {
    val chipStyle = when (stripItem.visualStyle) {
        StripItemVisualStyle.KNOWN -> AvailableNumberChipStyle.KNOWN
        StripItemVisualStyle.HIDDEN -> AvailableNumberChipStyle.HIDDEN
        StripItemVisualStyle.PLAYER_ENTERED -> AvailableNumberChipStyle.PLAYER_ENTERED
    }
    val activeInput = stripItemEntryInput?.takeIf { input -> input.stripItemIndex == stripItemIndex }
    val chipModifier = Modifier
        .width(chipWidth)
        .testTag(GameScreenTestTags.stripItem(stripItemIndex))
    val additionUsageIndicatorTestTag = GameScreenTestTags.stripUsageIndicator(
        index = stripItemIndex,
        operator = Operator.ADDITION
    )
    val multiplicationUsageIndicatorTestTag = GameScreenTestTags.stripUsageIndicator(
        index = stripItemIndex,
        operator = Operator.MULTIPLICATION
    )

    if (activeInput != null) {
        AvailableNumberInputChip(
            value = activeInput.draftText,
            onValueChange = onStripItemEntryInputChanged,
            onDone = onStripItemEntryInputConfirmed,
            onFocusLost = onStripItemEntryInputFocusLost,
            modifier = chipModifier,
            contentDescription = stripItemContentDescription(stripItem),
            additionUsed = stripItem.additionUsed,
            multiplicationUsed = stripItem.multiplicationUsed,
            inputTestTag = GameScreenTestTags.STRIP_ENTRY_INPUT,
            additionUsageIndicatorTestTag = additionUsageIndicatorTestTag,
            multiplicationUsageIndicatorTestTag = multiplicationUsageIndicatorTestTag,
            isHighlighted = isHighlighted,
            style = chipStyle,
            isInvalid = activeInput.isInvalid,
            errorMessage = stripEntryFeedbackText?.takeIf { activeInput.isInvalid }
        )
    } else {
        AvailableNumberChip(
            label = stripItem.label,
            modifier = chipModifier,
            contentDescription = stripItemContentDescription(stripItem),
            additionUsed = stripItem.additionUsed,
            multiplicationUsed = stripItem.multiplicationUsed,
            additionUsageIndicatorTestTag = additionUsageIndicatorTestTag,
            multiplicationUsageIndicatorTestTag = multiplicationUsageIndicatorTestTag,
            isHighlighted = isHighlighted,
            style = chipStyle,
            onClick = if (stripItem.isEntryEnabled && isStripItemEnabled(stripItemIndex)) {
                { onStripItemTapped(stripItemIndex) }
            } else {
                null
            }
        )
    }
}

@Composable
private fun requiredStripChipWidth(
    stripItems: List<StripItemUiState>,
    stripItemEntryInput: StripItemEntryInputUiState?
): Dp {
    val density = LocalDensity.current
    val textMeasurer = rememberTextMeasurer()
    val labels = buildList {
        addAll(stripItems.map(StripItemUiState::label))
        stripItemEntryInput?.let { input ->
            add(input.draftText.ifEmpty { "?" })
            input.validRange.maximumValue?.let { maximumValue ->
                add(maximumValue.toString())
            }
        }
    }
    val widestLabelWidth = labels.maxOfOrNull { label ->
        textMeasurer.measure(
            text = AnnotatedString(label),
            style = NumPairsTextStyles.StripValue
        ).size.width
    } ?: 0

    return with(density) {
        widestLabelWidth.toDp() + CHIP_HORIZONTAL_CONTENT_INSET
    }.coerceAtLeast(STRIP_CHIP_MIN_WIDTH)
}

@Composable
private fun StripEntryFeedback(message: String, isError: Boolean, modifier: Modifier = Modifier) {
    Text(
        text = message,
        modifier = modifier.semantics {
            contentDescription = message
            if (isError) {
                error(message)
            }
        },
        color = if (isError) {
            MaterialTheme.numPairsSemanticColors.error
        } else {
            MaterialTheme.colorScheme.onSurfaceVariant
        },
        style = MaterialTheme.typography.bodySmall
    )
}

@Composable
private fun StripItemEntryInputUiState.feedbackText(entryGuidance: String?): String = if (isInvalid) {
    validRange.invalidRangeText()
} else {
    entryGuidance ?: validRange.validRangeText()
}

@Composable
private fun StripEntryRange.validRangeText(): String = maximumValue?.let { maximum ->
    stringResource(
        R.string.strip_entry_valid_range_bounded,
        minimumValue,
        maximum
    )
} ?: stringResource(
    R.string.strip_entry_valid_range_unbounded,
    minimumValue
)

@Composable
private fun StripEntryRange.invalidRangeText(): String = maximumValue?.let { maximum ->
    stringResource(
        R.string.strip_entry_invalid_range_bounded,
        minimumValue,
        maximum
    )
} ?: stringResource(
    R.string.strip_entry_invalid_range_unbounded,
    minimumValue
)
