package org.cescfe.numpairs.feature.game.presentation

import org.cescfe.numpairs.domain.puzzle.LivePuzzleRuleConflict
import org.cescfe.numpairs.domain.puzzle.OperandSelectionChoice
import org.cescfe.numpairs.domain.puzzle.StripEntryUsageByOperator
import org.cescfe.numpairs.domain.puzzle.model.Expression
import org.cescfe.numpairs.domain.puzzle.model.OperandSlot
import org.cescfe.numpairs.domain.puzzle.model.Operator
import org.cescfe.numpairs.domain.puzzle.model.Puzzle
import org.cescfe.numpairs.domain.puzzle.model.PuzzleCompletionState
import org.cescfe.numpairs.domain.puzzle.model.StripEntryRange
import org.cescfe.numpairs.domain.puzzle.model.StripItem
import org.cescfe.numpairs.domain.puzzle.model.Tile
import org.cescfe.numpairs.domain.puzzle.model.TileResolutionState

data class GameUiState(
    val stripItems: List<StripItemUiState>,
    val tiles: List<TileUiState>,
    val puzzleOutcome: PuzzleOutcomeUiState? = null,
    val isSuccessOverlayVisible: Boolean = false,
    val stripItemEntryInput: StripItemEntryInputUiState? = null,
    val tileOperatorSelectionDialog: TileOperatorSelectionDialogUiState? = null,
    val tileOperandSelectionDialog: TileOperandSelectionDialogUiState? = null
) {
    companion object {
        fun from(puzzle: Puzzle, presentationState: GamePresentationState = GamePresentationState()): GameUiState =
            GameUiStateFactory.create(
                puzzle = puzzle,
                presentationState = presentationState
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

data class StripItemUiState(
    val label: String,
    val isEntryEnabled: Boolean,
    val visualStyle: StripItemVisualStyle,
    val additionUsed: Boolean = false,
    val multiplicationUsed: Boolean = false
) {
    constructor(
        stripItem: StripItem,
        usageByOperator: StripEntryUsageByOperator = StripEntryUsageByOperator()
    ) : this(
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
        },
        additionUsed = usageByOperator.additionUsed,
        multiplicationUsed = usageByOperator.multiplicationUsed
    )
}

enum class StripItemVisualStyle {
    KNOWN,
    HIDDEN,
    PLAYER_ENTERED
}

data class StripItemEntryInputUiState(
    val stripItemIndex: Int,
    val draftText: String,
    val validRange: StripEntryRange,
    val isInvalid: Boolean
)

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
    val isSelectable: Boolean,
    val additionRuleConflicts: Set<RuleConflictUiState> = emptySet(),
    val multiplicationRuleConflicts: Set<RuleConflictUiState> = emptySet()
) {
    constructor(
        choice: OperandSelectionChoice,
        additionRuleConflicts: Set<RuleConflictUiState> = emptySet(),
        multiplicationRuleConflicts: Set<RuleConflictUiState> = emptySet()
    ) : this(
        stripEntryId = choice.stripEntryId,
        value = choice.value,
        additionUsed = choice.usageByOperator.additionUsed,
        multiplicationUsed = choice.usageByOperator.multiplicationUsed,
        isSelectable = choice.canBeSelected,
        additionRuleConflicts = additionRuleConflicts,
        multiplicationRuleConflicts = multiplicationRuleConflicts
    )
}

data class TileUiState(
    val leftOperandLabel: String,
    val operatorLabel: String,
    val rightOperandLabel: String,
    val resultLabel: String,
    val visualState: TileVisualState = TileVisualState.NORMAL,
    val canReset: Boolean = false,
    val liveRuleConflicts: Set<RuleConflictUiState> = emptySet()
) {
    val isInvalid: Boolean
        get() = visualState == TileVisualState.INCORRECT

    val isPairingMismatchHighlighted: Boolean
        get() = visualState == TileVisualState.MISMATCHED_PAIRING

    constructor(
        tile: Tile,
        visualState: TileVisualState = tile.defaultVisualState,
        liveRuleConflicts: Set<RuleConflictUiState> = emptySet()
    ) : this(
        leftOperandLabel = tile.expression.leftOperand.label,
        operatorLabel = tile.expression.operator.symbol,
        rightOperandLabel = tile.expression.rightOperand.label,
        resultLabel = tile.result.toString(),
        visualState = visualState,
        canReset = tile.canReset,
        liveRuleConflicts = liveRuleConflicts
    )
}

enum class RuleConflictUiState {
    DUPLICATE_OPERATOR_USAGE,
    MISMATCHED_PAIRING
}

enum class TileVisualState {
    NORMAL,
    INCORRECT,
    MISMATCHED_PAIRING,
    LIVE_RULE_CONFLICT
}

private val Tile.defaultVisualState: TileVisualState
    get() = if (resolutionState == TileResolutionState.INCORRECT) {
        TileVisualState.INCORRECT
    } else {
        TileVisualState.NORMAL
    }

private val Expression.Operand.label: String
    get() = when (this) {
        Expression.Operand.Hidden -> "?"
        is Expression.Operand.Known -> value.toString()
    }

internal val PuzzleCompletionState.outcomeUiState: PuzzleOutcomeUiState?
    get() = when (this) {
        PuzzleCompletionState.INCOMPLETE -> null
        PuzzleCompletionState.SOLVED -> PuzzleOutcomeUiState.Solved
        PuzzleCompletionState.INCORRECT_TILES,
        PuzzleCompletionState.MISSING_STRIP_ENTRY_IDENTITIES,
        PuzzleCompletionState.MISMATCHED_SUM_PRODUCT_PAIRINGS,
        PuzzleCompletionState.INVALID_STRIP_ENTRY_USAGE -> PuzzleOutcomeUiState.Invalid(
            completionState = this
        )
    }

internal fun LivePuzzleRuleConflict.toUiState(): RuleConflictUiState = when (this) {
    LivePuzzleRuleConflict.DUPLICATE_OPERATOR_USAGE -> RuleConflictUiState.DUPLICATE_OPERATOR_USAGE
    LivePuzzleRuleConflict.MISMATCHED_PAIRING -> RuleConflictUiState.MISMATCHED_PAIRING
}
