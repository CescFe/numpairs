package org.cescfe.numpairs.feature.game.ui

import org.cescfe.numpairs.domain.puzzle.PuzzleCompletionState
import org.cescfe.numpairs.feature.game.presentation.GameUiState
import org.cescfe.numpairs.feature.game.presentation.PuzzleOutcomeUiState
import org.cescfe.numpairs.feature.game.presentation.StripItemUiState
import org.cescfe.numpairs.feature.game.presentation.StripItemVisualStyle
import org.cescfe.numpairs.feature.game.presentation.TileVisualState
import org.cescfe.numpairs.feature.game.presentation.TileUiState

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
