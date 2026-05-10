package org.cescfe.numpairs.ui.screen

internal fun solvedOverlayUiState(isSuccessOverlayVisible: Boolean): GameUiState = GameUiState(
    stripItems = List(8) { index ->
        StripItemUiState(
            label = (index + 1).toString(),
            isEntryEnabled = false,
            visualStyle = StripItemVisualStyle.KNOWN
        )
    },
    tiles = List(8) { index ->
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
    },
    puzzleOutcome = PuzzleOutcomeUiState.Solved,
    isSuccessOverlayVisible = isSuccessOverlayVisible
)

internal fun largeOperandBoardUiState(): GameUiState = GameUiState(
    stripItems = List(8) { index ->
        StripItemUiState(
            label = (index + 1).toString(),
            isEntryEnabled = false,
            visualStyle = StripItemVisualStyle.KNOWN
        )
    },
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
