package org.cescfe.numpairs.feature.game.presentation

import org.cescfe.numpairs.domain.puzzle.Operator
import org.cescfe.numpairs.domain.puzzle.Puzzle
import org.cescfe.numpairs.domain.puzzle.PuzzleCompletionState
import org.cescfe.numpairs.domain.puzzle.StripEntryUsageByOperator
import org.cescfe.numpairs.domain.puzzle.StripItem
import org.cescfe.numpairs.domain.puzzle.Tile
import org.cescfe.numpairs.domain.puzzle.TileResolutionState
import org.cescfe.numpairs.domain.puzzle.liveRuleConflictsByTile
import org.cescfe.numpairs.domain.puzzle.liveRuleConflictsForCandidate
import org.cescfe.numpairs.domain.puzzle.mismatchedSumProductPairingTileIndexes
import org.cescfe.numpairs.domain.puzzle.operandSelectionChoicesFor
import org.cescfe.numpairs.domain.puzzle.stripEntryUsageByOperator

internal object GameUiStateFactory {
    private val availableOperators = listOf(
        Operator.ADDITION,
        Operator.MULTIPLICATION
    )

    fun create(puzzle: Puzzle, presentationState: GamePresentationState): GameUiState {
        val completionState = puzzle.completionState
        val mismatchedPairingTileIndexes = completionState.mismatchedPairingTileIndexes(puzzle = puzzle)
        val stripEntryUsageById = puzzle.stripEntryUsageByOperator()
        val liveRuleConflictsByTile = puzzle.liveRuleConflictsByTile.mapValues { (_, conflicts) ->
            conflicts.map { conflict -> conflict.toUiState() }.toSet()
        }

        return GameUiState(
            stripItems = puzzle.strip.entries.map { stripEntry ->
                StripItemUiState(
                    stripItem = stripEntry.item,
                    usageByOperator = stripEntryUsageById.getOrDefault(stripEntry.id, StripEntryUsageByOperator())
                )
            },
            tiles = puzzle.board.tiles.mapIndexed { tileIndex, tile ->
                TileUiState(
                    tile = tile,
                    visualState = tile.visualState(
                        tileIndex = tileIndex,
                        mismatchedPairingTileIndexes = mismatchedPairingTileIndexes
                    ),
                    liveRuleConflicts = liveRuleConflictsByTile[tileIndex].orEmpty()
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

        val targetTile = puzzle.board.tiles[target.tileIndex]

        return TileOperandSelectionDialogUiState(
            tileIndex = target.tileIndex,
            slot = target.slot,
            availableOperands = puzzle.operandSelectionChoicesFor(
                tileIndex = target.tileIndex,
                slot = target.slot
            ).map { choice ->
                TileOperandOptionUiState(
                    choice = choice,
                    additionRuleConflicts = puzzle.liveRuleConflictsForCandidate(
                        tileIndex = target.tileIndex,
                        slot = target.slot,
                        stripEntryId = choice.stripEntryId,
                        operator = Operator.ADDITION
                    ).map { conflict -> conflict.toUiState() }.toSet(),
                    multiplicationRuleConflicts = puzzle.liveRuleConflictsForCandidate(
                        tileIndex = target.tileIndex,
                        slot = target.slot,
                        stripEntryId = choice.stripEntryId,
                        operator = Operator.MULTIPLICATION
                    ).map { conflict -> conflict.toUiState() }.toSet()
                )
            },
            operatorContext = targetTile.expression.operator
        )
    }
}

private fun Tile.visualState(tileIndex: Int, mismatchedPairingTileIndexes: Set<Int>): TileVisualState = when {
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
