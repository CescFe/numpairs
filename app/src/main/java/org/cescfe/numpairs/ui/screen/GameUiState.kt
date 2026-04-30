package org.cescfe.numpairs.ui.screen

import org.cescfe.numpairs.domain.puzzle.Expression
import org.cescfe.numpairs.domain.puzzle.OperandSelectionHint
import org.cescfe.numpairs.domain.puzzle.OperandSlot
import org.cescfe.numpairs.domain.puzzle.Operator
import org.cescfe.numpairs.domain.puzzle.Puzzle
import org.cescfe.numpairs.domain.puzzle.PuzzleCompletionState
import org.cescfe.numpairs.domain.puzzle.StripEntryRange
import org.cescfe.numpairs.domain.puzzle.StripItem
import org.cescfe.numpairs.domain.puzzle.Tile
import org.cescfe.numpairs.domain.puzzle.TileResolutionState
import org.cescfe.numpairs.domain.puzzle.operandSelectionHintsFor

data class GameUiState(
    val stripItems: List<StripItemUiState>,
    val tiles: List<TileUiState>,
    val puzzleOutcome: PuzzleOutcomeUiState? = null,
    val isSuccessOverlayVisible: Boolean = false,
    val stripItemEntryDialog: StripItemEntryDialogUiState? = null,
    val tileOperatorSelectionDialog: TileOperatorSelectionDialogUiState? = null,
    val tileOperandSelectionDialog: TileOperandSelectionDialogUiState? = null
) {
    companion object {
        fun from(
            puzzle: Puzzle,
            isSuccessOverlayVisible: Boolean = false,
            stripItemEntryDialogIndex: Int? = null,
            tileOperatorSelectionDialogIndex: Int? = null,
            tileOperandSelectionTarget: TileOperandSelectionTarget? = null
        ): GameUiState = GameUiState(
            stripItems = puzzle.strip.items.map(::StripItemUiState),
            tiles = puzzle.board.tiles.map(::TileUiState),
            puzzleOutcome = puzzle.outcomeUiState,
            isSuccessOverlayVisible = isSuccessOverlayVisible,
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
                    TileOperandSelectionDialogUiState(
                        tileIndex = target.tileIndex,
                        slot = target.slot,
                        availableOperands = puzzle.operandSelectionHintsFor(
                            tileIndex = target.tileIndex,
                            slot = target.slot
                        ).map(::TileOperandOptionUiState)
                    )
                }
        )
    }
}

sealed interface PuzzleOutcomeUiState {
    data object Solved : PuzzleOutcomeUiState

    data class Invalid(val completionState: PuzzleCompletionState) : PuzzleOutcomeUiState {
        init {
            require(completionState != PuzzleCompletionState.INCOMPLETE) {
                "Invalid puzzle outcomes require a completed puzzle."
            }
            require(completionState != PuzzleCompletionState.SOLVED) {
                "Solved puzzles must use the solved outcome."
            }
        }
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
    val slot: OperandSlot,
    val availableOperands: List<TileOperandOptionUiState>
)

data class TileOperandOptionUiState(
    val stripEntryId: Int,
    val value: Int,
    val additionUsed: Boolean,
    val multiplicationUsed: Boolean,
    val isSelectable: Boolean
) {
    constructor(operandSelectionHint: OperandSelectionHint) : this(
        stripEntryId = operandSelectionHint.stripEntry.entryId,
        value = operandSelectionHint.stripEntry.value,
        additionUsed = operandSelectionHint.usageByOperator.additionUsed,
        multiplicationUsed = operandSelectionHint.usageByOperator.multiplicationUsed,
        isSelectable = operandSelectionHint.isSelectable
    )
}

data class TileOperandSelectionTarget(val tileIndex: Int, val slot: OperandSlot)

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

private val Puzzle.outcomeUiState: PuzzleOutcomeUiState?
    get() = when (completionState) {
        PuzzleCompletionState.INCOMPLETE -> null
        PuzzleCompletionState.SOLVED -> PuzzleOutcomeUiState.Solved
        PuzzleCompletionState.INCORRECT_TILES,
        PuzzleCompletionState.MISSING_STRIP_ENTRY_IDENTITIES,
        PuzzleCompletionState.MISMATCHED_SUM_PRODUCT_PAIRINGS,
        PuzzleCompletionState.INVALID_STRIP_ENTRY_USAGE -> PuzzleOutcomeUiState.Invalid(
            completionState = completionState
        )
    }
