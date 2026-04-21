package org.cescfe.numpairs.ui.screen

import org.cescfe.numpairs.domain.puzzle.Puzzle
import org.cescfe.numpairs.domain.puzzle.StripItem
import org.cescfe.numpairs.domain.puzzle.Tile

data class GameUiState(val stripItems: List<StripItemUiState>, val tiles: List<TileUiState>) {
    companion object {
        fun from(puzzle: Puzzle): GameUiState = GameUiState(
            stripItems = puzzle.strip.items.map(::StripItemUiState),
            tiles = puzzle.board.tiles.map(::TileUiState)
        )
    }
}

data class StripItemUiState(val label: String) {
    constructor(stripItem: StripItem) : this(
        label = when (stripItem) {
            StripItem.Hidden -> "?"
            is StripItem.Known -> stripItem.value.toString()
            is StripItem.PlayerEntered -> stripItem.value.toString()
        }
    )
}

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
