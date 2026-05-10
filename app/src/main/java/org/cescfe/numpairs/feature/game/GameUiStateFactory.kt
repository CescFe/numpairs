package org.cescfe.numpairs.feature.game

import org.cescfe.numpairs.domain.puzzle.Operator
import org.cescfe.numpairs.domain.puzzle.Puzzle
import org.cescfe.numpairs.domain.puzzle.PuzzleCompletionState
import org.cescfe.numpairs.domain.puzzle.StripItem
import org.cescfe.numpairs.domain.puzzle.TileResolutionState
import org.cescfe.numpairs.domain.puzzle.mismatchedSumProductPairingTileIndexes
import org.cescfe.numpairs.domain.puzzle.operandSelectionChoicesFor

internal object GameUiStateFactory {
    private val availableOperators = listOf(
        Operator.ADDITION,
        Operator.MULTIPLICATION
    )

    fun create(puzzle: Puzzle, presentationState: GamePresentationState): GameUiState {
        val completionState = puzzle.completionState
        val mismatchedPairingTileIndexes = completionState.mismatchedPairingTileIndexes(puzzle = puzzle)

        return GameUiState(
            stripItems = puzzle.strip.items.map(::StripItemUiState),
            tiles = puzzle.board.tiles.mapIndexed { tileIndex, tile ->
                TileUiState(
                    tile = tile,
                    visualState = tile.visualState(
                        tileIndex = tileIndex,
                        mismatchedPairingTileIndexes = mismatchedPairingTileIndexes
                    )
                )
            },
            puzzleOutcome = completionState.outcomeUiState,
            isSuccessOverlayVisible = presentationState.isSuccessOverlayVisible(
                isPuzzleSolved = puzzle.isSolved
            ),
            stripItemEntryDialog = createStripItemEntryDialog(
                puzzle = puzzle,
                modal = presentationState.modal
            ),
            tileOperatorSelectionDialog = createTileOperatorSelectionDialog(
                puzzle = puzzle,
                modal = presentationState.modal
            ),
            tileOperandSelectionDialog = createTileOperandSelectionDialog(
                puzzle = puzzle,
                modal = presentationState.modal
            )
        )
    }

    private fun createStripItemEntryDialog(puzzle: Puzzle, modal: GameModalState?): StripItemEntryDialogUiState? {
        val stripItemIndex = (modal as? GameModalState.StripItemEntry)?.index ?: return null
        val stripItem = puzzle.strip.items.getOrNull(stripItemIndex) ?: return null

        return when (stripItem) {
            StripItem.Hidden -> StripItemEntryDialogUiState(
                stripItemIndex = stripItemIndex,
                validRange = puzzle.strip.validEntryRangeFor(stripItemIndex),
                mode = StripItemEntryDialogMode.CREATE,
                initialValue = ""
            )
            is StripItem.PlayerEntered -> StripItemEntryDialogUiState(
                stripItemIndex = stripItemIndex,
                validRange = puzzle.strip.validEntryRangeFor(stripItemIndex),
                mode = StripItemEntryDialogMode.EDIT,
                initialValue = stripItem.value.toString()
            )
            is StripItem.Known -> null
        }
    }

    private fun createTileOperatorSelectionDialog(
        puzzle: Puzzle,
        modal: GameModalState?
    ): TileOperatorSelectionDialogUiState? {
        val tileIndex = (modal as? GameModalState.TileOperatorSelection)?.tileIndex ?: return null
        val currentOperator = puzzle.board.tiles.getOrNull(tileIndex)?.expression?.operator ?: return null

        return TileOperatorSelectionDialogUiState(
            tileIndex = tileIndex,
            availableOperators = availableOperators,
            initialOperator = currentOperator.takeUnless { it == Operator.Hidden }
        )
    }

    private fun createTileOperandSelectionDialog(
        puzzle: Puzzle,
        modal: GameModalState?
    ): TileOperandSelectionDialogUiState? {
        val target = (modal as? GameModalState.TileOperandSelection)?.target ?: return null

        if (target.tileIndex !in puzzle.board.tiles.indices) {
            return null
        }

        return TileOperandSelectionDialogUiState(
            tileIndex = target.tileIndex,
            slot = target.slot,
            availableOperands = puzzle.operandSelectionChoicesFor(
                tileIndex = target.tileIndex,
                slot = target.slot
            ).map(::TileOperandOptionUiState)
        )
    }
}

private fun org.cescfe.numpairs.domain.puzzle.Tile.visualState(
    tileIndex: Int,
    mismatchedPairingTileIndexes: Set<Int>
): TileVisualState = when {
    resolutionState == TileResolutionState.INCORRECT -> TileVisualState.INCORRECT
    tileIndex in mismatchedPairingTileIndexes -> TileVisualState.MISMATCHED_PAIRING
    else -> TileVisualState.NORMAL
}

private fun PuzzleCompletionState.mismatchedPairingTileIndexes(puzzle: Puzzle): Set<Int> =
    if (this == PuzzleCompletionState.MISMATCHED_SUM_PRODUCT_PAIRINGS) {
        puzzle.mismatchedSumProductPairingTileIndexes.toSet()
    } else {
        emptySet()
    }
