package org.cescfe.numpairs.feature.game.ui.screen

import androidx.annotation.StringRes
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.lerp
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.selected
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.stateDescription
import androidx.compose.ui.unit.DpOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.zIndex
import org.cescfe.numpairs.R
import org.cescfe.numpairs.domain.puzzle.model.Operator
import org.cescfe.numpairs.feature.game.presentation.TileOperandOptionUiState
import org.cescfe.numpairs.feature.game.presentation.TileOperandSelectionDialogUiState
import org.cescfe.numpairs.feature.game.presentation.TileOperatorSelectionDialogUiState
import org.cescfe.numpairs.feature.game.ui.indicators.OperandUsageIndicatorColors
import org.cescfe.numpairs.feature.game.ui.indicators.OperandUsageIndicatorState
import org.cescfe.numpairs.feature.game.ui.indicators.operandUsageIndicatorColors
import org.cescfe.numpairs.feature.game.ui.indicators.usageIndicatorContentDescriptionResId
import org.cescfe.numpairs.feature.game.ui.indicators.usageIndicatorSymbol
import org.cescfe.numpairs.feature.game.ui.semantics.OperandSelectorUsageHintVisualStateValues
import org.cescfe.numpairs.feature.game.ui.semantics.operandSelectorUsageHintVisualState
import org.cescfe.numpairs.ui.theme.NumPairsComponents
import org.cescfe.numpairs.ui.theme.NumPairsTextStyles
import org.cescfe.numpairs.ui.theme.numPairsSemanticColors

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun TileOperandSelectionSheet(
    dialogUiState: TileOperandSelectionDialogUiState,
    onDismiss: () -> Unit,
    onConfirm: (Int) -> Unit
) {
    val operandSheetContentDescription = stringResource(R.string.tile_operand_dialog_title)

    ModalBottomSheet(
        modifier = Modifier
            .testTag(GameScreenTestTags.TILE_OPERAND_SELECTOR)
            .semantics {
                contentDescription = operandSheetContentDescription
            },
        onDismissRequest = onDismiss,
        shape = RoundedCornerShape(topStart = NumPairsComponents.LargeRadius, topEnd = NumPairsComponents.LargeRadius),
        containerColor = NumPairsComponents.raisedSurfaceColor(),
        contentColor = MaterialTheme.colorScheme.onSurface
    ) {
        Text(
            text = operandSheetContentDescription,
            style = MaterialTheme.typography.titleMedium,
            modifier = Modifier.padding(
                start = TILE_OPERAND_SHEET_PADDING,
                end = TILE_OPERAND_SHEET_PADDING,
                bottom = 12.dp
            )
        )
        LazyVerticalGrid(
            columns = GridCells.Adaptive(minSize = TILE_OPERAND_SHEET_OPTION_MIN_WIDTH),
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(max = TILE_OPERAND_SHEET_MAX_HEIGHT)
                .selectableGroup(),
            contentPadding = PaddingValues(
                start = TILE_OPERAND_SHEET_PADDING,
                end = TILE_OPERAND_SHEET_PADDING,
                bottom = TILE_OPERAND_SHEET_PADDING
            ),
            horizontalArrangement = Arrangement.spacedBy(TILE_OPERAND_SHEET_GRID_SPACING),
            verticalArrangement = Arrangement.spacedBy(TILE_OPERAND_SHEET_GRID_SPACING)
        ) {
            items(
                items = dialogUiState.availableOperands,
                key = TileOperandOptionUiState::stripEntryId
            ) { operand ->
                OperandSelectionOption(
                    operand = operand,
                    onConfirm = onConfirm
                )
            }
        }
    }
}

@Composable
private fun OperandSelectionOption(operand: TileOperandOptionUiState, onConfirm: (Int) -> Unit) {
    val operandSelectionLabel = operand.value.toString()
    val optionColors = operandOptionColors(enabled = operand.isSelectable)

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = TILE_OPERAND_HINT_OVERLAY_LIFT),
        contentAlignment = Alignment.TopCenter
    ) {
        Box {
            Surface(
                onClick = { onConfirm(operand.stripEntryId) },
                enabled = operand.isSelectable,
                modifier = Modifier
                    .widthIn(
                        min = TILE_OPERAND_SHEET_OPTION_CARD_MIN_WIDTH,
                        max = TILE_OPERAND_SHEET_OPTION_CARD_MAX_WIDTH
                    )
                    .defaultMinSize(minHeight = TILE_OPERAND_SHEET_OPTION_MIN_HEIGHT)
                    .testTag(GameScreenTestTags.tileOperandOption(operand.stripEntryId))
                    .semantics {
                        contentDescription = operandSelectionLabel
                    },
                shape = NumPairsComponents.LargeShape,
                color = optionColors.container,
                contentColor = optionColors.content,
                border = optionColors.border
            ) {
                Box(
                    modifier = Modifier.fillMaxWidth(),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = operandSelectionLabel,
                        modifier = Modifier.padding(horizontal = 14.dp, vertical = 9.dp),
                        style = NumPairsTextStyles.OperandOption
                    )
                }
            }
            OperandUsageHintBadge(
                operator = Operator.ADDITION,
                visualState = operand.usageHintVisualStateFor(Operator.ADDITION),
                stripEntryId = operand.stripEntryId,
                enabled = operand.isSelectable,
                modifier = Modifier
                    .zIndex(1f)
                    .align(Alignment.TopStart)
                    .padding(start = TILE_OPERAND_HINT_EDGE_INSET)
                    .offset(y = -TILE_OPERAND_HINT_OVERLAY_LIFT)
            )
            OperandUsageHintBadge(
                operator = Operator.MULTIPLICATION,
                visualState = operand.usageHintVisualStateFor(Operator.MULTIPLICATION),
                stripEntryId = operand.stripEntryId,
                enabled = operand.isSelectable,
                modifier = Modifier
                    .zIndex(1f)
                    .align(Alignment.TopEnd)
                    .padding(end = TILE_OPERAND_HINT_EDGE_INSET)
                    .offset(y = -TILE_OPERAND_HINT_OVERLAY_LIFT)
            )
        }
    }
}

@Composable
private fun OperandUsageHintBadge(
    modifier: Modifier = Modifier,
    operator: Operator,
    visualState: OperandSelectorUsageHintVisualState,
    stripEntryId: Int,
    enabled: Boolean = true
) {
    val hintContentDescription = stringResource(operator.usageIndicatorContentDescriptionResId)
    val hintStateDescription = stringResource(visualState.stateDescriptionResId)
    val resolvedColors = operandSelectorUsageHintColors(visualState).let { colors ->
        if (enabled || visualState != OperandSelectorUsageHintVisualState.AVAILABLE) {
            colors
        } else {
            colors.disabled()
        }
    }

    Surface(
        modifier = modifier
            .testTag(GameScreenTestTags.tileOperandUsageHint(stripEntryId, operator))
            .semantics {
                contentDescription = hintContentDescription
                stateDescription = hintStateDescription
                operandSelectorUsageHintVisualState = visualState.semanticsValue
            },
        shape = RoundedCornerShape(TILE_OPERAND_HINT_CORNER_RADIUS),
        color = resolvedColors.container,
        contentColor = resolvedColors.content,
        border = resolvedColors.border
    ) {
        Text(
            text = operator.usageIndicatorSymbol,
            modifier = Modifier.padding(
                horizontal = TILE_OPERAND_HINT_HORIZONTAL_PADDING,
                vertical = TILE_OPERAND_HINT_VERTICAL_PADDING
            ),
            style = NumPairsTextStyles.PuzzleLabel
        )
    }
}

@Composable
private fun operandSelectorUsageHintColors(
    visualState: OperandSelectorUsageHintVisualState
): OperandUsageIndicatorColors = when (visualState) {
    OperandSelectorUsageHintVisualState.AVAILABLE -> operandUsageIndicatorColors(
        OperandUsageIndicatorState.AVAILABLE
    )
    OperandSelectorUsageHintVisualState.USED_WITH_PAIRING_AVAILABLE -> operandSelectorPartialUsageHintColors()

    OperandSelectorUsageHintVisualState.USED_EXHAUSTED -> operandUsageIndicatorColors(
        OperandUsageIndicatorState.USED
    )

    OperandSelectorUsageHintVisualState.RULE_CONFLICT -> OperandUsageIndicatorColors(
        container = NumPairsComponents.errorContainerColor(),
        content = NumPairsComponents.errorContentColor(),
        border = NumPairsComponents.errorBorder()
    )
}

@Composable
private fun operandSelectorPartialUsageHintColors(): OperandUsageIndicatorColors = OperandUsageIndicatorColors(
    container = NumPairsComponents.subtleSurfaceColor(),
    content = MaterialTheme.numPairsSemanticColors.selection,
    border = BorderStroke(
        width = NumPairsComponents.FocusBorderWidth,
        color = MaterialTheme.numPairsSemanticColors.selection
    )
)

@Composable
private fun OperandUsageIndicatorColors.disabled(): OperandUsageIndicatorColors = OperandUsageIndicatorColors(
    container = lerp(container, NumPairsComponents.raisedSurfaceColor(), 0.35f),
    content = lerp(content, MaterialTheme.colorScheme.onSurfaceVariant, 0.25f),
    border = NumPairsComponents.subtleBorder()
)

private data class OperandOptionColors(val container: Color, val content: Color, val border: BorderStroke)

@Composable
private fun operandOptionColors(enabled: Boolean): OperandOptionColors = if (enabled) {
    OperandOptionColors(
        container = NumPairsComponents.raisedSurfaceColor(),
        content = MaterialTheme.colorScheme.onSurface,
        border = NumPairsComponents.defaultBorder()
    )
} else {
    OperandOptionColors(
        container = NumPairsComponents.subtleSurfaceColor(),
        content = MaterialTheme.colorScheme.onSurfaceVariant,
        border = NumPairsComponents.subtleBorder()
    )
}

private fun TileOperandOptionUiState.usageHintVisualStateFor(operator: Operator): OperandSelectorUsageHintVisualState =
    when {
        ruleConflictsFor(operator).isNotEmpty() -> OperandSelectorUsageHintVisualState.RULE_CONFLICT
        else -> when (operator) {
            Operator.Addition -> when {
                !additionUsed -> OperandSelectorUsageHintVisualState.AVAILABLE
                multiplicationUsed -> OperandSelectorUsageHintVisualState.USED_EXHAUSTED
                else -> OperandSelectorUsageHintVisualState.USED_WITH_PAIRING_AVAILABLE
            }

            Operator.Multiplication -> when {
                !multiplicationUsed -> OperandSelectorUsageHintVisualState.AVAILABLE
                additionUsed -> OperandSelectorUsageHintVisualState.USED_EXHAUSTED
                else -> OperandSelectorUsageHintVisualState.USED_WITH_PAIRING_AVAILABLE
            }

            Operator.Hidden -> error("Hidden operator does not expose operand usage hints.")
        }
    }

private fun TileOperandOptionUiState.ruleConflictsFor(operator: Operator) = when (operator) {
    Operator.Addition -> additionRuleConflicts
    Operator.Multiplication -> multiplicationRuleConflicts
    Operator.Hidden -> error("Hidden operator does not expose operand usage hints.")
}

private enum class OperandSelectorUsageHintVisualState(
    @get:StringRes val stateDescriptionResId: Int,
    val semanticsValue: String
) {
    AVAILABLE(
        R.string.tile_operand_usage_state_available,
        OperandSelectorUsageHintVisualStateValues.AVAILABLE
    ),
    USED_WITH_PAIRING_AVAILABLE(
        R.string.tile_operand_usage_state_used,
        OperandSelectorUsageHintVisualStateValues.USED_WITH_PAIRING_AVAILABLE
    ),
    USED_EXHAUSTED(
        R.string.tile_operand_usage_state_used,
        OperandSelectorUsageHintVisualStateValues.USED_EXHAUSTED
    ),
    RULE_CONFLICT(
        R.string.tile_operand_usage_state_rule_conflict,
        OperandSelectorUsageHintVisualStateValues.RULE_CONFLICT
    )
}

@Composable
internal fun TileOperatorSelectionMenu(
    dialogUiState: TileOperatorSelectionDialogUiState,
    onDismiss: () -> Unit,
    onConfirm: (Operator) -> Unit
) {
    val operatorMenuContentDescription = stringResource(R.string.tile_operator_dialog_title)

    DropdownMenu(
        expanded = true,
        onDismissRequest = onDismiss,
        modifier = Modifier
            .testTag(GameScreenTestTags.TILE_OPERATOR_SELECTOR)
            .semantics {
                contentDescription = operatorMenuContentDescription
            },
        offset = DpOffset(x = 0.dp, y = 4.dp),
        shape = NumPairsComponents.LargeShape,
        containerColor = NumPairsComponents.raisedSurfaceColor(),
        tonalElevation = 0.dp,
        shadowElevation = 0.dp,
        border = NumPairsComponents.subtleBorder()
    ) {
        Row(
            modifier = Modifier
                .padding(TILE_OPERATOR_MENU_PADDING)
                .selectableGroup(),
            horizontalArrangement = Arrangement.spacedBy(TILE_OPERATOR_MENU_OPTION_SPACING)
        ) {
            dialogUiState.availableOperators.forEach { operator ->
                val isSelected = dialogUiState.initialOperator == operator
                val operatorSelectionLabel = operator.selectionLabel()

                Surface(
                    onClick = { onConfirm(operator) },
                    modifier = Modifier
                        .defaultMinSize(minWidth = 0.dp)
                        .testTag(GameScreenTestTags.tileOperatorOption(operator))
                        .semantics {
                            contentDescription = operatorSelectionLabel
                            selected = isSelected
                        },
                    shape = RoundedCornerShape(TILE_OPERATOR_MENU_CORNER_RADIUS),
                    color = if (isSelected) {
                        MaterialTheme.numPairsSemanticColors.selectionContainer
                    } else {
                        NumPairsComponents.subtleSurfaceColor()
                    },
                    contentColor = if (isSelected) {
                        MaterialTheme.numPairsSemanticColors.onSelectionContainer
                    } else {
                        MaterialTheme.colorScheme.onSurfaceVariant
                    },
                    border = if (isSelected) {
                        BorderStroke(
                            width = NumPairsComponents.StrongBorderWidth,
                            color = MaterialTheme.numPairsSemanticColors.selection
                        )
                    } else {
                        NumPairsComponents.subtleBorder()
                    }
                ) {
                    Text(
                        text = operator.symbol,
                        modifier = Modifier.padding(
                            horizontal = TILE_OPERATOR_MENU_OPTION_HORIZONTAL_PADDING,
                            vertical = TILE_OPERATOR_MENU_OPTION_VERTICAL_PADDING
                        ),
                        style = if (isSelected) {
                            NumPairsTextStyles.OperatorOptionSelected
                        } else {
                            NumPairsTextStyles.OperatorOption
                        }
                    )
                }
            }
        }
    }
}

@Composable
private fun Operator.selectionLabel(): String = when (this) {
    Operator.Addition -> stringResource(R.string.tile_operator_option_addition)
    Operator.Multiplication -> stringResource(R.string.tile_operator_option_multiplication)
    Operator.Hidden -> error("Hidden operator is not a selectable option.")
}
