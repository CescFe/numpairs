package org.cescfe.numpairs.ui.screen

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.selected
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.stateDescription
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.DpOffset
import androidx.compose.ui.unit.dp
import org.cescfe.numpairs.R
import org.cescfe.numpairs.domain.puzzle.Operator
import org.cescfe.numpairs.domain.puzzle.PuzzleSamples
import org.cescfe.numpairs.ui.components.AvailableNumberChip
import org.cescfe.numpairs.ui.components.AvailableNumberChipStyle
import org.cescfe.numpairs.ui.components.PuzzleTile
import org.cescfe.numpairs.ui.theme.NumPairsTheme

private const val BOARD_MAX_VISUAL_COLUMN_COUNT = 4
private val BOARD_TILE_MIN_WIDTH = 112.dp
private val BOARD_TILE_MAX_WIDTH = 144.dp
private val BOARD_TILE_SPACING = 12.dp
private val STRIP_CHIP_SPACING = 4.dp
private val STRIP_HORIZONTAL_PADDING = 8.dp
private val STRIP_VERTICAL_PADDING = 14.dp
private val TILE_OPERATOR_MENU_CORNER_RADIUS = 16.dp
private val TILE_OPERATOR_MENU_PADDING = 8.dp
private val TILE_OPERATOR_MENU_OPTION_SPACING = 8.dp
private val TILE_OPERATOR_MENU_OPTION_HORIZONTAL_PADDING = 12.dp
private val TILE_OPERATOR_MENU_OPTION_VERTICAL_PADDING = 8.dp
private val TILE_OPERAND_SHEET_MAX_HEIGHT = 320.dp
private val TILE_OPERAND_SHEET_PADDING = 20.dp
private val TILE_OPERAND_SHEET_GRID_SPACING = 12.dp
private val TILE_OPERAND_SHEET_OPTION_MIN_WIDTH = 88.dp
private val TILE_OPERAND_SHEET_OPTION_MIN_HEIGHT = 88.dp
private val TILE_OPERAND_SHEET_OPTION_CORNER_RADIUS = 18.dp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GameScreen(
    uiState: GameUiState,
    modifier: Modifier = Modifier,
    onStripItemTapped: (Int) -> Unit = {},
    onStripItemEntryDismissed: () -> Unit = {},
    onStripItemEntryConfirmed: (Int) -> Unit = {},
    onTileLeftOperandTapped: (Int) -> Unit = {},
    onTileRightOperandTapped: (Int) -> Unit = {},
    onTileOperandSelectionDismissed: () -> Unit = {},
    onTileOperandSelectionConfirmed: (Int) -> Unit = {},
    onTileOperatorTapped: (Int) -> Unit = {},
    onTileOperatorSelectionDismissed: () -> Unit = {},
    onTileOperatorSelectionConfirmed: (Operator) -> Unit = {}
) {
    Scaffold(
        modifier = modifier
            .fillMaxSize()
            .testTag(GameScreenTestTags.SCREEN),
        topBar = {
            TopAppBar(
                title = {
                    Text(text = stringResource(R.string.app_name))
                }
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
            StripSection(
                stripItems = uiState.stripItems,
                onStripItemTapped = onStripItemTapped,
                modifier = Modifier.fillMaxWidth()
            )
            BoardSection(
                tiles = uiState.tiles,
                onTileLeftOperandTapped = onTileLeftOperandTapped,
                onTileRightOperandTapped = onTileRightOperandTapped,
                onTileOperatorTapped = onTileOperatorTapped,
                tileOperatorSelectionDialog = uiState.tileOperatorSelectionDialog,
                onTileOperatorSelectionDismissed = onTileOperatorSelectionDismissed,
                onTileOperatorSelectionConfirmed = onTileOperatorSelectionConfirmed,
                modifier = Modifier.fillMaxWidth()
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
}

@Composable
private fun BoardSection(
    tiles: List<TileUiState>,
    onTileLeftOperandTapped: (Int) -> Unit,
    onTileRightOperandTapped: (Int) -> Unit,
    onTileOperatorTapped: (Int) -> Unit,
    tileOperatorSelectionDialog: TileOperatorSelectionDialogUiState?,
    onTileOperatorSelectionDismissed: () -> Unit,
    onTileOperatorSelectionConfirmed: (Operator) -> Unit,
    modifier: Modifier = Modifier
) {
    val boardContentDescription = stringResource(R.string.board_content_description)

    BoxWithConstraints(
        modifier = modifier
            .testTag(GameScreenTestTags.BOARD)
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
                                modifier = Modifier
                                    .testTag(GameScreenTestTags.tile(tileIndex))
                                    .width(tileWidth)
                                    .wrapContentHeight(),
                                leftOperandModifier = Modifier.testTag(GameScreenTestTags.tileLeftOperand(tileIndex)),
                                onLeftOperandClick = { onTileLeftOperandTapped(tileIndex) },
                                operatorModifier = Modifier.testTag(GameScreenTestTags.tileOperator(tileIndex)),
                                onOperatorClick = { onTileOperatorTapped(tileIndex) },
                                operatorOverlay = {
                                    tileOperatorSelectionUiState?.let { dialogUiState ->
                                        TileOperatorSelectionMenu(
                                            dialogUiState = dialogUiState,
                                            onDismiss = onTileOperatorSelectionDismissed,
                                            onConfirm = onTileOperatorSelectionConfirmed
                                        )
                                    }
                                },
                                rightOperandModifier = Modifier.testTag(GameScreenTestTags.tileRightOperand(tileIndex)),
                                onRightOperandClick = { onTileRightOperandTapped(tileIndex) }
                            )
                        }
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun TileOperandSelectionSheet(
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
                .heightIn(max = TILE_OPERAND_SHEET_MAX_HEIGHT),
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
                key = { operandOption -> operandOption.value }
            ) { operandOption ->
                val operandSelectionLabel = operandOption.value.toString()
                val usageStateDescription = stringResource(
                    id = when (operandOption.usageState) {
                        TileOperandUsageState.UNUSED -> R.string.tile_operand_option_state_unused
                        TileOperandUsageState.USED_IN_OTHER_TILES -> R.string.tile_operand_option_state_used_elsewhere
                        TileOperandUsageState.USED_IN_SAME_TILE -> R.string.tile_operand_option_state_used_in_this_tile
                        TileOperandUsageState.USED_IN_SAME_AND_OTHER_TILES -> R.string.tile_operand_option_state_used_in_this_tile_and_elsewhere
                    }
                )
                val supportingText = when (operandOption.usageState) {
                    TileOperandUsageState.UNUSED -> stringResource(R.string.tile_operand_option_status_available)
                    TileOperandUsageState.USED_IN_OTHER_TILES -> if (operandOption.usedInOtherTilesCount > 1) {
                        stringResource(
                            R.string.tile_operand_option_status_used_elsewhere_count,
                            operandOption.usedInOtherTilesCount
                        )
                    } else {
                        stringResource(R.string.tile_operand_option_status_used_elsewhere)
                    }
                    TileOperandUsageState.USED_IN_SAME_TILE -> stringResource(R.string.tile_operand_option_status_used_in_this_tile)
                    TileOperandUsageState.USED_IN_SAME_AND_OTHER_TILES -> stringResource(
                        R.string.tile_operand_option_status_used_in_this_tile_and_elsewhere_count,
                        operandOption.usedInOtherTilesCount
                    )
                }
                val usageSummaryText = when {
                    operandOption.totalVisibleCount > 1 -> stringResource(
                        R.string.tile_operand_option_usage_summary,
                        operandOption.usedCount,
                        operandOption.totalVisibleCount
                    )
                    else -> null
                }
                val (containerColor, contentColor, borderColor, borderWidth) = operandOption.optionColors()
                val optionContentDescription = buildString {
                    append(operandSelectionLabel)
                    append(", ")
                    append(usageStateDescription)
                    usageSummaryText?.let { summary ->
                        append(", ")
                        append(summary)
                    }
                }

                Surface(
                    onClick = { onConfirm(operandOption.value) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .defaultMinSize(minHeight = TILE_OPERAND_SHEET_OPTION_MIN_HEIGHT)
                        .testTag(GameScreenTestTags.tileOperandOption(operandOption.value))
                        .semantics {
                            contentDescription = optionContentDescription
                            stateDescription = usageStateDescription
                        },
                    shape = RoundedCornerShape(TILE_OPERAND_SHEET_OPTION_CORNER_RADIUS),
                    color = containerColor,
                    contentColor = contentColor,
                    border = BorderStroke(
                        width = borderWidth,
                        color = borderColor
                    )
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 12.dp, vertical = 10.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Text(
                            text = operandSelectionLabel,
                            fontWeight = FontWeight.SemiBold
                        )
                        Text(
                            text = supportingText,
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.Medium
                        )
                        usageSummaryText?.let { summary ->
                            Text(
                                text = summary,
                                style = MaterialTheme.typography.labelSmall
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun TileOperandOptionUiState.optionColors(): OperandOptionColors = when (usageState) {
    TileOperandUsageState.UNUSED -> OperandOptionColors(
        containerColor = MaterialTheme.colorScheme.surface,
        contentColor = MaterialTheme.colorScheme.onSurface,
        borderColor = MaterialTheme.colorScheme.outlineVariant,
        borderWidth = 1.dp
    )
    TileOperandUsageState.USED_IN_OTHER_TILES -> OperandOptionColors(
        containerColor = MaterialTheme.colorScheme.secondaryContainer,
        contentColor = MaterialTheme.colorScheme.onSecondaryContainer,
        borderColor = MaterialTheme.colorScheme.secondary,
        borderWidth = 1.dp
    )
    TileOperandUsageState.USED_IN_SAME_TILE -> OperandOptionColors(
        containerColor = MaterialTheme.colorScheme.tertiaryContainer,
        contentColor = MaterialTheme.colorScheme.onTertiaryContainer,
        borderColor = MaterialTheme.colorScheme.tertiary,
        borderWidth = 1.dp
    )
    TileOperandUsageState.USED_IN_SAME_AND_OTHER_TILES -> OperandOptionColors(
        containerColor = MaterialTheme.colorScheme.tertiaryContainer,
        contentColor = MaterialTheme.colorScheme.onTertiaryContainer,
        borderColor = MaterialTheme.colorScheme.tertiary,
        borderWidth = 2.dp
    )
}

private data class OperandOptionColors(
    val containerColor: androidx.compose.ui.graphics.Color,
    val contentColor: androidx.compose.ui.graphics.Color,
    val borderColor: androidx.compose.ui.graphics.Color,
    val borderWidth: Dp
)

@Composable
private fun StripSection(
    stripItems: List<StripItemUiState>,
    onStripItemTapped: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    val stripContentDescription = stringResource(R.string.strip_content_description)

    Surface(
        modifier = modifier
            .testTag(GameScreenTestTags.STRIP)
            .semantics {
                contentDescription = stripContentDescription
            },
        color = MaterialTheme.colorScheme.surfaceVariant,
        shape = RoundedCornerShape(24.dp)
    ) {
        BoxWithConstraints(
            modifier = Modifier
                .fillMaxWidth()
                .padding(
                    horizontal = STRIP_HORIZONTAL_PADDING,
                    vertical = STRIP_VERTICAL_PADDING
                )
        ) {
            val chipCount = stripItems.size
            val chipWidth = calculateStripChipWidth(
                availableWidth = maxWidth,
                chipCount = chipCount
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(STRIP_CHIP_SPACING)
            ) {
                stripItems.forEachIndexed { index, stripItem ->
                    AvailableNumberChip(
                        label = stripItem.label,
                        modifier = Modifier
                            .width(chipWidth)
                            .testTag(GameScreenTestTags.stripItem(index)),
                        style = when (stripItem.visualStyle) {
                            StripItemVisualStyle.KNOWN -> AvailableNumberChipStyle.KNOWN
                            StripItemVisualStyle.HIDDEN -> AvailableNumberChipStyle.HIDDEN
                            StripItemVisualStyle.PLAYER_ENTERED -> AvailableNumberChipStyle.PLAYER_ENTERED
                        },
                        onClick = if (stripItem.isEntryEnabled) {
                            { onStripItemTapped(index) }
                        } else {
                            null
                        }
                    )
                }
            }
        }
    }
}

@Composable
private fun StripItemEntryDialog(
    dialogUiState: StripItemEntryDialogUiState,
    onDismiss: () -> Unit,
    onConfirm: (Int) -> Unit
) {
    var enteredValue by rememberSaveable(
        dialogUiState.stripItemIndex,
        dialogUiState.mode,
        dialogUiState.initialValue
    ) {
        mutableStateOf(dialogUiState.initialValue)
    }
    val parsedValue = enteredValue.toIntOrNull()
    val confirmedValue = parsedValue?.takeIf { it in dialogUiState.validRange }
    val isInputInvalid = enteredValue.isNotEmpty() && confirmedValue == null
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
private fun TileOperatorSelectionMenu(
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
                        fontWeight = if (isSelected) {
                            FontWeight.SemiBold
                        } else {
                            FontWeight.Normal
                        }
                    )
                }
            }
        }
    }
}

private fun calculateStripChipWidth(availableWidth: Dp, chipCount: Int): Dp {
    val totalSpacing = STRIP_CHIP_SPACING * (chipCount - 1)
    return (availableWidth - totalSpacing) / chipCount
}

private fun calculateBoardColumnCount(availableWidth: Dp): Int {
    val columnsThatFit = (
        (availableWidth.value + BOARD_TILE_SPACING.value) /
            (BOARD_TILE_MIN_WIDTH.value + BOARD_TILE_SPACING.value)
        ).toInt()

    return columnsThatFit.coerceIn(1, BOARD_MAX_VISUAL_COLUMN_COUNT)
}

private fun calculateBoardTileWidth(availableWidth: Dp, visualColumnCount: Int): Dp {
    val totalSpacing = BOARD_TILE_SPACING * (visualColumnCount - 1)
    val availableTileWidth = (availableWidth - totalSpacing) / visualColumnCount

    return availableTileWidth.coerceIn(BOARD_TILE_MIN_WIDTH, BOARD_TILE_MAX_WIDTH)
}

@Composable
private fun Operator.selectionLabel(): String = when (this) {
    Operator.Addition -> stringResource(R.string.tile_operator_option_addition)
    Operator.Multiplication -> stringResource(R.string.tile_operator_option_multiplication)
    Operator.Hidden -> error("Hidden operator is not a selectable option.")
}

@Preview(showBackground = true)
@Composable
private fun GameScreenPreview() {
    NumPairsTheme {
        GameScreen(
            uiState = GameUiState.from(PuzzleSamples.prototype)
        )
    }
}
