package org.cescfe.numpairs.feature.game.ui

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
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ColorScheme
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
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
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.DpOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.zIndex
import org.cescfe.numpairs.R
import org.cescfe.numpairs.domain.puzzle.Operator
import org.cescfe.numpairs.feature.game.presentation.StripItemEntryDialogUiState
import org.cescfe.numpairs.feature.game.presentation.TileOperandOptionUiState
import org.cescfe.numpairs.feature.game.presentation.TileOperandSelectionDialogUiState
import org.cescfe.numpairs.feature.game.presentation.TileOperatorSelectionDialogUiState
import org.cescfe.numpairs.ui.theme.NumPairsTextStyles

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
        onDismissRequest = onDismiss
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
    val optionColors = operandOptionColors(
        enabled = operand.isSelectable,
        colorScheme = MaterialTheme.colorScheme
    )

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
                shape = RoundedCornerShape(TILE_OPERAND_SHEET_OPTION_CORNER_RADIUS),
                color = optionColors.container,
                contentColor = optionColors.content,
                border = BorderStroke(
                    width = 1.dp,
                    color = optionColors.border
                )
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
                usageState = operand.usageStateFor(Operator.ADDITION),
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
                usageState = operand.usageStateFor(Operator.MULTIPLICATION),
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
    usageState: OperandUsageHintState,
    stripEntryId: Int,
    enabled: Boolean = true
) {
    val colorScheme = MaterialTheme.colorScheme
    val hintContentDescription = when (operator) {
        Operator.Addition -> stringResource(R.string.tile_operand_usage_addition_hint)
        Operator.Multiplication -> stringResource(R.string.tile_operand_usage_multiplication_hint)
        Operator.Hidden -> error("Hidden operator does not expose operand usage hints.")
    }
    val hintStateDescription = stringResource(usageState.stateDescriptionResId)
    val resolvedColors = baseHintBadgeColors(
        usageState = usageState,
        operator = operator,
        colorScheme = colorScheme
    ).let { colors ->
        if (enabled) colors else colors.disabled(colorScheme)
    }

    Surface(
        modifier = modifier
            .testTag(GameScreenTestTags.tileOperandUsageHint(stripEntryId, operator))
            .semantics {
                contentDescription = hintContentDescription
                stateDescription = hintStateDescription
            },
        shape = RoundedCornerShape(TILE_OPERAND_HINT_CORNER_RADIUS),
        color = resolvedColors.container,
        contentColor = resolvedColors.content,
        border = BorderStroke(width = 1.dp, color = resolvedColors.border)
    ) {
        Text(
            text = operator.symbol,
            modifier = Modifier.padding(
                horizontal = TILE_OPERAND_HINT_HORIZONTAL_PADDING,
                vertical = TILE_OPERAND_HINT_VERTICAL_PADDING
            ),
            style = NumPairsTextStyles.PuzzleLabel
        )
    }
}

private data class HintBadgeColors(val container: Color, val content: Color, val border: Color)

private fun baseHintBadgeColors(
    usageState: OperandUsageHintState,
    operator: Operator,
    colorScheme: ColorScheme
): HintBadgeColors = when (usageState) {
    OperandUsageHintState.AVAILABLE -> HintBadgeColors(
        container = colorScheme.surfaceContainerHighest,
        content = colorScheme.onSurfaceVariant,
        border = colorScheme.outline
    )

    OperandUsageHintState.USED -> when (operator) {
        Operator.Addition,
        Operator.Multiplication -> HintBadgeColors(
            container = colorScheme.primaryContainer,
            content = colorScheme.onPrimaryContainer,
            border = colorScheme.primary
        )

        Operator.Hidden -> error("Hidden operator does not expose operand usage hints.")
    }
}

private fun HintBadgeColors.disabled(colorScheme: ColorScheme): HintBadgeColors = HintBadgeColors(
    container = lerp(container, colorScheme.surfaceContainerHigh, 0.35f),
    content = lerp(content, colorScheme.onSurface, 0.20f),
    border = lerp(border, colorScheme.outline, 0.35f)
)

private data class OperandOptionColors(val container: Color, val content: Color, val border: Color)

private fun operandOptionColors(enabled: Boolean, colorScheme: ColorScheme): OperandOptionColors = if (enabled) {
    OperandOptionColors(
        container = colorScheme.surface,
        content = colorScheme.onSurface,
        border = colorScheme.outline
    )
} else {
    OperandOptionColors(
        container = colorScheme.surfaceContainerHigh,
        content = colorScheme.onSurfaceVariant,
        border = colorScheme.outline
    )
}

private fun TileOperandOptionUiState.usageStateFor(operator: Operator): OperandUsageHintState = when (operator) {
    Operator.Addition -> when {
        additionUsed -> OperandUsageHintState.USED
        else -> OperandUsageHintState.AVAILABLE
    }

    Operator.Multiplication -> when {
        multiplicationUsed -> OperandUsageHintState.USED
        else -> OperandUsageHintState.AVAILABLE
    }

    Operator.Hidden -> error("Hidden operator does not expose operand usage hints.")
}

private enum class OperandUsageHintState(val stateDescriptionResId: Int) {
    AVAILABLE(R.string.tile_operand_usage_state_available),
    USED(R.string.tile_operand_usage_state_used)
}

@Composable
internal fun StripItemEntryDialog(
    dialogUiState: StripItemEntryDialogUiState,
    onDismiss: () -> Unit,
    onConfirm: (Int) -> Unit,
    canConfirm: (Int) -> Boolean = { true }
) {
    var enteredValue by rememberSaveable(
        dialogUiState.stripItemIndex,
        dialogUiState.mode,
        dialogUiState.initialValue
    ) {
        mutableStateOf(dialogUiState.initialValue)
    }
    val parsedValue = enteredValue.toIntOrNull()
    val valueInRange = parsedValue?.takeIf { value -> value in dialogUiState.validRange }
    val confirmedValue = valueInRange?.takeIf(canConfirm)
    val isInputInvalid = enteredValue.isNotEmpty() && parsedValue != null && valueInRange == null
    val validRangeText = dialogUiState.validRange.maximumValue?.let { maximumValue ->
        stringResource(
            R.string.strip_entry_valid_range_bounded,
            dialogUiState.validRange.minimumValue,
            maximumValue
        )
    } ?: stringResource(
        R.string.strip_entry_valid_range_unbounded,
        dialogUiState.validRange.minimumValue
    )

    AlertDialog(
        modifier = Modifier.testTag(GameScreenTestTags.STRIP_ENTRY_DIALOG),
        onDismissRequest = onDismiss,
        title = {
            Text(text = stringResource(R.string.strip_entry_dialog_title))
        },
        text = {
            OutlinedTextField(
                value = enteredValue,
                onValueChange = { newValue ->
                    enteredValue = newValue.filter(Char::isDigit)
                },
                modifier = Modifier.testTag(GameScreenTestTags.STRIP_ENTRY_INPUT),
                label = {
                    Text(text = stringResource(R.string.strip_entry_input_label))
                },
                supportingText = {
                    Text(
                        text = validRangeText,
                        modifier = Modifier.testTag(GameScreenTestTags.STRIP_ENTRY_RANGE)
                    )
                },
                isError = isInputInvalid,
                singleLine = true,
                textStyle = NumPairsTextStyles.NumericInput,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
            )
        },
        confirmButton = {
            Button(
                onClick = { onConfirm(confirmedValue ?: return@Button) },
                enabled = confirmedValue != null,
                modifier = Modifier.testTag(GameScreenTestTags.STRIP_ENTRY_CONFIRM)
            ) {
                Text(text = stringResource(R.string.confirm))
            }
        },
        dismissButton = {
            TextButton(
                onClick = onDismiss,
                modifier = Modifier.testTag(GameScreenTestTags.STRIP_ENTRY_CANCEL)
            ) {
                Text(text = stringResource(R.string.cancel))
            }
        }
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
        offset = DpOffset(x = 0.dp, y = 4.dp)
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
                        MaterialTheme.colorScheme.primaryContainer
                    } else {
                        MaterialTheme.colorScheme.surface
                    },
                    contentColor = if (isSelected) {
                        MaterialTheme.colorScheme.onPrimaryContainer
                    } else {
                        MaterialTheme.colorScheme.onSurface
                    },
                    border = BorderStroke(
                        width = 1.dp,
                        color = if (isSelected) {
                            MaterialTheme.colorScheme.primary
                        } else {
                            MaterialTheme.colorScheme.outlineVariant
                        }
                    )
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
