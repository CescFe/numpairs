package org.cescfe.numpairs.ui.screen

import org.cescfe.numpairs.domain.puzzle.Expression
import org.cescfe.numpairs.domain.puzzle.Operator
import org.cescfe.numpairs.domain.puzzle.Puzzle
import org.cescfe.numpairs.domain.puzzle.StripEntryRange
import org.cescfe.numpairs.domain.puzzle.StripItem
import org.cescfe.numpairs.domain.puzzle.Tile
import org.cescfe.numpairs.domain.puzzle.TileResolutionState

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
                ?.let { target ->
                    val visibleOperands = puzzle.visibleStripValues()
                    val visibleOperandCounts = visibleOperands.countByValuePreservingOrder()
                    val sameTileOperand = puzzle.board.tiles[target.tileIndex].counterpartOperandValue(target.slot)
                    val otherTileOperandUsageCounts = puzzle.board.tiles
                        .withIndex()
                        .filter { indexedTile -> indexedTile.index != target.tileIndex }
                        .flatMap { indexedTile -> indexedTile.value.knownOperandValues() }
                        .groupingBy { value -> value }
                        .eachCount()

                    TileOperandSelectionDialogUiState(
                        tileIndex = target.tileIndex,
                        slot = target.slot,
                        availableOperands = visibleOperandCounts.map { (value, totalVisibleCount) ->
                            val isUsedInSameTile = sameTileOperand == value
                            val usedCount = otherTileOperandUsageCounts.getOrDefault(value, 0) +
                                if (isUsedInSameTile) {
                                    1
                                } else {
                                    0
                                }

                            TileOperandOptionUiState(
                                value = value,
                                totalVisibleCount = totalVisibleCount,
                                usedCount = usedCount,
                                isUsedInSameTile = isUsedInSameTile
                            )
                        }
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
    val availableOperands: List<TileOperandOptionUiState>
)

data class TileOperandOptionUiState(
    val value: Int,
    val totalVisibleCount: Int,
    val usedCount: Int,
    val isUsedInSameTile: Boolean
) {
    init {
        require(totalVisibleCount > 0) {
            "Operand options must represent at least one visible strip value."
        }
        require(usedCount >= 0) {
            "Operand usage counts cannot be negative."
        }
        require(!isUsedInSameTile || usedCount > 0) {
            "A same-tile usage marker requires at least one recorded usage."
        }
    }

    val usedInOtherTilesCount: Int
        get() = usedCount - if (isUsedInSameTile) 1 else 0

    val usageState: TileOperandUsageState
        get() = when {
            isUsedInSameTile && usedInOtherTilesCount > 0 -> TileOperandUsageState.USED_IN_SAME_AND_OTHER_TILES
            isUsedInSameTile -> TileOperandUsageState.USED_IN_SAME_TILE
            usedInOtherTilesCount > 0 -> TileOperandUsageState.USED_IN_OTHER_TILES
            else -> TileOperandUsageState.UNUSED
        }
}

enum class TileOperandUsageState {
    UNUSED,
    USED_IN_OTHER_TILES,
    USED_IN_SAME_TILE,
    USED_IN_SAME_AND_OTHER_TILES
}

data class TileOperandSelectionTarget(val tileIndex: Int, val slot: TileOperandSlot)

enum class TileOperandSlot {
    LEFT,
    RIGHT
}

data class TileUiState(
    val leftOperandLabel: String,
    val operatorLabel: String,
    val rightOperandLabel: String,
    val resultLabel: String,
    val isInvalid: Boolean = false
) {
    constructor(tile: Tile) : this(
        leftOperandLabel = tile.expression.leftOperand.label,
        operatorLabel = tile.expression.operator.symbol,
        rightOperandLabel = tile.expression.rightOperand.label,
        resultLabel = tile.result.toString(),
        isInvalid = tile.resolutionState == TileResolutionState.INCORRECT
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

private fun Tile.counterpartOperandValue(slot: TileOperandSlot): Int? = when (slot) {
    TileOperandSlot.LEFT -> expression.rightOperand.takeKnownValue()
    TileOperandSlot.RIGHT -> expression.leftOperand.takeKnownValue()
}

private fun Tile.knownOperandValues(): List<Int> = listOfNotNull(
    expression.leftOperand.takeKnownValue(),
    expression.rightOperand.takeKnownValue()
)

private fun Puzzle.visibleStripValues(): List<Int> = strip.items.mapNotNull { stripItem ->
    stripItem.visibleValue
}

private fun List<Int>.countByValuePreservingOrder(): List<Pair<Int, Int>> {
    val counts = linkedMapOf<Int, Int>()

    forEach { value ->
        counts[value] = counts.getOrDefault(value, 0) + 1
    }

    return counts.map { entry -> entry.key to entry.value }
}

private fun Expression.Operand.takeKnownValue(): Int? = when (this) {
    Expression.Operand.Hidden -> null
    is Expression.Operand.Known -> value
}

private val StripItem.visibleValue: Int?
    get() = when (this) {
        StripItem.Hidden -> null
        is StripItem.Known -> value
        is StripItem.PlayerEntered -> value
    }
