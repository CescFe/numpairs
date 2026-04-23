package org.cescfe.numpairs.ui.screen

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
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
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import org.cescfe.numpairs.R
import org.cescfe.numpairs.domain.puzzle.PuzzleSamples
import org.cescfe.numpairs.ui.components.AvailableNumberChip
import org.cescfe.numpairs.ui.components.PuzzleTile
import org.cescfe.numpairs.ui.theme.NumPairsTheme

private const val BOARD_MAX_VISUAL_COLUMN_COUNT = 4
private val BOARD_TILE_MIN_WIDTH = 112.dp
private val BOARD_TILE_MAX_WIDTH = 144.dp
private val BOARD_TILE_SPACING = 12.dp
private val STRIP_CHIP_SPACING = 4.dp
private val STRIP_HORIZONTAL_PADDING = 8.dp
private val STRIP_VERTICAL_PADDING = 14.dp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GameScreen(
    uiState: GameUiState,
    modifier: Modifier = Modifier,
    onStripItemTapped: (Int) -> Unit = {},
    onStripItemEntryDismissed: () -> Unit = {},
    onStripItemEntryConfirmed: (Int) -> Unit = {}
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
                modifier = Modifier.fillMaxWidth()
            )
        }
    }

    uiState.stripItemEntryDialog?.let { dialogUiState ->
        HiddenStripItemEntryDialog(
            dialogUiState = dialogUiState,
            onDismiss = onStripItemEntryDismissed,
            onConfirm = onStripItemEntryConfirmed
        )
    }
}

@Composable
private fun BoardSection(tiles: List<TileUiState>, modifier: Modifier = Modifier) {
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
        val visualRows = tiles.chunked(visualColumnCount)

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
                        row.forEach { tile ->
                            PuzzleTile(
                                tile = tile,
                                modifier = Modifier
                                    .width(tileWidth)
                                    .wrapContentHeight()
                            )
                        }
                    }
                }
            }
        }
    }
}

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
private fun HiddenStripItemEntryDialog(
    dialogUiState: StripItemEntryDialogUiState,
    onDismiss: () -> Unit,
    onConfirm: (Int) -> Unit
) {
    var enteredValue by rememberSaveable(dialogUiState.stripItemIndex) { mutableStateOf("") }
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

@Preview(showBackground = true)
@Composable
private fun GameScreenPreview() {
    NumPairsTheme {
        GameScreen(
            uiState = GameUiState.from(PuzzleSamples.prototype)
        )
    }
}
