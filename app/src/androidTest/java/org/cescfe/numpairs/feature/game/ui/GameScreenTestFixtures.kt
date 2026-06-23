package org.cescfe.numpairs.feature.game.ui

import org.cescfe.numpairs.data.puzzle.seed.initialPuzzle
import org.cescfe.numpairs.domain.puzzle.OperandSlot
import org.cescfe.numpairs.domain.puzzle.PuzzleCompletionState
import org.cescfe.numpairs.feature.game.presentation.GameUiState
import org.cescfe.numpairs.feature.game.presentation.PuzzleOutcomeUiState
import org.cescfe.numpairs.feature.game.presentation.RuleConflictUiState
import org.cescfe.numpairs.feature.game.presentation.StripItemUiState
import org.cescfe.numpairs.feature.game.presentation.StripItemVisualStyle
import org.cescfe.numpairs.feature.game.presentation.TileOperandOptionUiState
import org.cescfe.numpairs.feature.game.presentation.TileOperandSelectionDialogUiState
import org.cescfe.numpairs.feature.game.presentation.TileUiState
import org.cescfe.numpairs.feature.game.presentation.TileVisualState

internal fun solvedOverlayUiState(isSuccessOverlayVisible: Boolean): GameUiState = GameUiState(
    stripItems = completedStripItems(),
    tiles = completedTiles(),
    puzzleOutcome = PuzzleOutcomeUiState.Solved,
    isSuccessOverlayVisible = isSuccessOverlayVisible
)

internal fun invalidOutcomeUiState(completionState: PuzzleCompletionState): GameUiState = GameUiState(
    stripItems = completedStripItems(),
    tiles = completedTiles(),
    puzzleOutcome = PuzzleOutcomeUiState.Invalid(completionState = completionState),
    isSuccessOverlayVisible = false
)

internal fun mismatchedPairingUiState(): GameUiState = GameUiState(
    stripItems = completedStripItems(),
    tiles = completedTiles().mapIndexed { index, tile ->
        tile.copy(
            visualState = if (index in listOf(0, 1, 2, 3)) {
                TileVisualState.MISMATCHED_PAIRING
            } else {
                TileVisualState.NORMAL
            }
        )
    },
    puzzleOutcome = PuzzleOutcomeUiState.Invalid(
        completionState = PuzzleCompletionState.MISMATCHED_SUM_PRODUCT_PAIRINGS
    ),
    isSuccessOverlayVisible = false
)

internal fun largeOperandBoardUiState(): GameUiState = GameUiState(
    stripItems = completedStripItems(),
    tiles = listOf(
        TileUiState(
            leftOperandLabel = "1",
            operatorLabel = "×",
            rightOperandLabel = "222",
            resultLabel = "222"
        ),
        TileUiState(
            leftOperandLabel = "8",
            operatorLabel = "+",
            rightOperandLabel = "9",
            resultLabel = "17"
        ),
        TileUiState(
            leftOperandLabel = "7",
            operatorLabel = "×",
            rightOperandLabel = "6",
            resultLabel = "42"
        ),
        TileUiState(
            leftOperandLabel = "12",
            operatorLabel = "+",
            rightOperandLabel = "5",
            resultLabel = "17"
        ),
        TileUiState(
            leftOperandLabel = "3",
            operatorLabel = "×",
            rightOperandLabel = "4",
            resultLabel = "12"
        ),
        TileUiState(
            leftOperandLabel = "16",
            operatorLabel = "+",
            rightOperandLabel = "2",
            resultLabel = "18"
        ),
        TileUiState(
            leftOperandLabel = "10",
            operatorLabel = "×",
            rightOperandLabel = "2",
            resultLabel = "20"
        ),
        TileUiState(
            leftOperandLabel = "9",
            operatorLabel = "+",
            rightOperandLabel = "1",
            resultLabel = "10"
        )
    )
)

internal fun repeatedOperandSelectorUiState(): GameUiState = GameUiState.from(initialPuzzle).copy(
    tileOperandSelectionDialog = TileOperandSelectionDialogUiState(
        tileIndex = 1,
        slot = OperandSlot.LEFT,
        availableOperands = listOf(
            TileOperandOptionUiState(
                stripEntryId = 0,
                value = 6,
                additionUsed = true,
                multiplicationUsed = false,
                isSelectable = true
            ),
            TileOperandOptionUiState(
                stripEntryId = 1,
                value = 6,
                additionUsed = false,
                multiplicationUsed = false,
                isSelectable = true
            ),
            TileOperandOptionUiState(
                stripEntryId = 4,
                value = 25,
                additionUsed = false,
                multiplicationUsed = false,
                isSelectable = true
            ),
            TileOperandOptionUiState(
                stripEntryId = 7,
                value = 222,
                additionUsed = false,
                multiplicationUsed = false,
                isSelectable = true
            )
        )
    )
)

internal fun operandSelectorUsageHintVisualStateUiState(): GameUiState = GameUiState.from(initialPuzzle).copy(
    tileOperandSelectionDialog = TileOperandSelectionDialogUiState(
        tileIndex = 1,
        slot = OperandSlot.LEFT,
        availableOperands = listOf(
            TileOperandOptionUiState(
                stripEntryId = 0,
                value = 2,
                additionUsed = false,
                multiplicationUsed = false,
                isSelectable = true
            ),
            TileOperandOptionUiState(
                stripEntryId = 1,
                value = 3,
                additionUsed = true,
                multiplicationUsed = false,
                isSelectable = true
            ),
            TileOperandOptionUiState(
                stripEntryId = 2,
                value = 5,
                additionUsed = true,
                multiplicationUsed = true,
                isSelectable = true
            ),
            TileOperandOptionUiState(
                stripEntryId = 3,
                value = 7,
                additionUsed = true,
                multiplicationUsed = false,
                isSelectable = true,
                additionRuleConflicts = setOf(RuleConflictUiState.DUPLICATE_OPERATOR_USAGE)
            ),
            TileOperandOptionUiState(
                stripEntryId = 4,
                value = 11,
                additionUsed = false,
                multiplicationUsed = true,
                isSelectable = true,
                multiplicationRuleConflicts = setOf(RuleConflictUiState.MISMATCHED_PAIRING)
            )
        )
    )
)

internal fun duplicateOperatorLocalConflictUiState(): GameUiState = GameUiState(
    stripItems = completedStripItems(),
    tiles = completedTiles().mapIndexed { index, tile ->
        tile.copy(
            visualState = if (index in listOf(0, 1)) {
                TileVisualState.LIVE_RULE_CONFLICT
            } else {
                TileVisualState.NORMAL
            },
            liveRuleConflicts = if (index in listOf(0, 1)) {
                setOf(RuleConflictUiState.DUPLICATE_OPERATOR_USAGE)
            } else {
                emptySet()
            }
        )
    }
)

internal fun mismatchedPairingLocalConflictUiState(): GameUiState = GameUiState(
    stripItems = completedStripItems(),
    tiles = completedTiles().mapIndexed { index, tile ->
        tile.copy(
            visualState = if (index in listOf(0, 1)) {
                TileVisualState.LIVE_RULE_CONFLICT
            } else {
                TileVisualState.NORMAL
            },
            liveRuleConflicts = if (index in listOf(0, 1)) {
                setOf(RuleConflictUiState.MISMATCHED_PAIRING)
            } else {
                emptySet()
            }
        )
    }
)

private fun completedStripItems(): List<StripItemUiState> = List(8) { index ->
    StripItemUiState(
        label = (index + 1).toString(),
        isEntryEnabled = false,
        visualStyle = StripItemVisualStyle.KNOWN
    )
}

private fun completedTiles(): List<TileUiState> = List(8) { index ->
    TileUiState(
        leftOperandLabel = (index + 1).toString(),
        operatorLabel = if (index % 2 == 0) "+" else "×",
        rightOperandLabel = (index + 2).toString(),
        resultLabel = if (index % 2 == 0) {
            (index + 1 + index + 2).toString()
        } else {
            ((index + 1) * (index + 2)).toString()
        },
        canReset = true
    )
}
