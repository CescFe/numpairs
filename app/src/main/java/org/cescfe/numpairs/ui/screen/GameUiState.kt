package org.cescfe.numpairs.ui.screen

import org.cescfe.numpairs.domain.puzzle.Puzzle
import org.cescfe.numpairs.domain.puzzle.StripEntryRange
import org.cescfe.numpairs.domain.puzzle.StripItem
import org.cescfe.numpairs.domain.puzzle.Tile

data class GameUiState(
    val stripItems: List<StripItemUiState>,
    val tiles: List<TileUiState>,
    val stripItemEntryDialog: StripItemEntryDialogUiState? = null
) {
    companion object {
        fun from(puzzle: Puzzle, stripItemEntryDialogIndex: Int? = null): GameUiState = GameUiState(
            stripItems = puzzle.strip.items.map(::StripItemUiState),
            tiles = puzzle.board.tiles.map(::TileUiState),
            stripItemEntryDialog = stripItemEntryDialogIndex?.let { stripItemIndex ->
                StripItemEntryDialogUiState(
                    stripItemIndex = stripItemIndex,
                    validRange = puzzle.strip.validEntryRangeFor(stripItemIndex)
                )
            }
        )
    }
}

data class StripItemUiState(val label: String, val isEntryEnabled: Boolean, val visualStyle: StripItemVisualStyle) {
    constructor(stripItem: StripItem) : this(
        label = when (stripItem) {
            StripItem.Hidden -> "?"
            is StripItem.Known -> stripItem.value.toString()
            is StripItem.PlayerEntered -> stripItem.value.toString()
        },
        isEntryEnabled = stripItem is StripItem.Hidden,
        visualStyle = when (stripItem) {
            is StripItem.Known -> StripItemVisualStyle.KNOWN
            StripItem.Hidden, is StripItem.PlayerEntered -> StripItemVisualStyle.MODIFIABLE
        }
    )
}

enum class StripItemVisualStyle {
    KNOWN,
    MODIFIABLE
}

data class StripItemEntryDialogUiState(val stripItemIndex: Int, val validRange: StripEntryRange)

data class TileUiState(
    val leftOperandLabel: String,
    val operatorLabel: String,
    val rightOperandLabel: String,
    val resultLabel: String
) {
    constructor(tile: Tile) : this(
        leftOperandLabel = tile.expression.leftOperand.toString(),
        operatorLabel = tile.expression.operator.symbol,
        rightOperandLabel = tile.expression.rightOperand.toString(),
        resultLabel = tile.result.toString()
    )
}
