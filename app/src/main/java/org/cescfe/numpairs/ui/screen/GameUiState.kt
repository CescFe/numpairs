package org.cescfe.numpairs.ui.screen

import org.cescfe.numpairs.domain.puzzle.Expression
import org.cescfe.numpairs.domain.puzzle.Operator
import org.cescfe.numpairs.domain.puzzle.Puzzle
import org.cescfe.numpairs.domain.puzzle.StripEntryRange
import org.cescfe.numpairs.domain.puzzle.StripItem
import org.cescfe.numpairs.domain.puzzle.Tile

data class GameUiState(
    val stripItems: List<StripItemUiState>,
    val tiles: List<TileUiState>,
    val stripItemEntryDialog: StripItemEntryDialogUiState? = null,
    val tileOperatorSelectionDialog: TileOperatorSelectionDialogUiState? = null,
    val tileOperandSelectionDialog: TileOperandSelectionDialogUiState? = null
) {
    companion object {
        fun from(
            puzzle: Puzzle,
            stripItemEntryDialogIndex: Int? = null,
            tileOperatorSelectionDialogIndex: Int? = null,
            tileOperandSelectionTarget: TileOperandSelectionTarget? = null
        ): GameUiState = GameUiState(
            stripItems = puzzle.strip.items.map(::StripItemUiState),
            tiles = puzzle.board.tiles.map(::TileUiState),
            stripItemEntryDialog = stripItemEntryDialogIndex?.let { stripItemIndex ->
                val stripItem = puzzle.strip.items[stripItemIndex]

                StripItemEntryDialogUiState(
                    stripItemIndex = stripItemIndex,
                    validRange = puzzle.strip.validEntryRangeFor(stripItemIndex),
                    mode = when (stripItem) {
                        StripItem.Hidden -> StripItemEntryDialogMode.CREATE
                        is StripItem.PlayerEntered -> StripItemEntryDialogMode.EDIT
                        is StripItem.Known -> error("Known strip items do not support entry dialogs.")
                    },
                    initialValue = when (stripItem) {
                        StripItem.Hidden -> ""
                        is StripItem.PlayerEntered -> stripItem.value.toString()
                        is StripItem.Known -> error("Known strip items do not support entry dialogs.")
                    }
                )
            },
            tileOperatorSelectionDialog = tileOperatorSelectionDialogIndex
                ?.takeIf { tileIndex -> tileIndex in puzzle.board.tiles.indices }
                ?.let { tileIndex ->
                    val currentOperator = puzzle.board.tiles[tileIndex].expression.operator

                    TileOperatorSelectionDialogUiState(
                        tileIndex = tileIndex,
                        availableOperators = listOf(
                            Operator.ADDITION,
                            Operator.MULTIPLICATION
                        ),
                        initialOperator = currentOperator.takeUnless { it == Operator.Hidden }
                    )
                },
            tileOperandSelectionDialog = tileOperandSelectionTarget
                ?.takeIf { target -> target.tileIndex in puzzle.board.tiles.indices }
                ?.takeIf { target ->
                    puzzle.board.tiles[target.tileIndex].operandAt(target.slot) == Expression.Operand.Hidden
                }
                ?.let { target ->
                    TileOperandSelectionDialogUiState(
                        tileIndex = target.tileIndex,
                        slot = target.slot,
                        availableOperands = puzzle.strip.items.mapNotNull { stripItem -> stripItem.visibleValue }
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
        isEntryEnabled = stripItem == StripItem.Hidden || stripItem is StripItem.PlayerEntered,
        visualStyle = when (stripItem) {
            is StripItem.Known -> StripItemVisualStyle.KNOWN
            StripItem.Hidden -> StripItemVisualStyle.HIDDEN
            is StripItem.PlayerEntered -> StripItemVisualStyle.PLAYER_ENTERED
        }
    )
}

enum class StripItemVisualStyle {
    KNOWN,
    HIDDEN,
    PLAYER_ENTERED
}

data class StripItemEntryDialogUiState(
    val stripItemIndex: Int,
    val validRange: StripEntryRange,
    val mode: StripItemEntryDialogMode,
    val initialValue: String
)

enum class StripItemEntryDialogMode {
    CREATE,
    EDIT
}

data class TileOperatorSelectionDialogUiState(
    val tileIndex: Int,
    val availableOperators: List<Operator>,
    val initialOperator: Operator? = null
)

data class TileOperandSelectionDialogUiState(
    val tileIndex: Int,
    val slot: TileOperandSlot,
    val availableOperands: List<Int>
)

data class TileOperandSelectionTarget(
    val tileIndex: Int,
    val slot: TileOperandSlot
)

enum class TileOperandSlot {
    LEFT,
    RIGHT
}

data class TileUiState(
    val leftOperandLabel: String,
    val operatorLabel: String,
    val rightOperandLabel: String,
    val resultLabel: String
) {
    constructor(tile: Tile) : this(
        leftOperandLabel = tile.expression.leftOperand.label,
        operatorLabel = tile.expression.operator.symbol,
        rightOperandLabel = tile.expression.rightOperand.label,
        resultLabel = tile.result.toString()
    )
}

private val Expression.Operand.label: String
    get() = when (this) {
        Expression.Operand.Hidden -> "?"
        is Expression.Operand.Known -> value.toString()
    }

private fun Tile.operandAt(slot: TileOperandSlot): Expression.Operand = when (slot) {
    TileOperandSlot.LEFT -> expression.leftOperand
    TileOperandSlot.RIGHT -> expression.rightOperand
}

private val StripItem.visibleValue: Int?
    get() = when (this) {
        StripItem.Hidden -> null
        is StripItem.Known -> value
        is StripItem.PlayerEntered -> value
    }
