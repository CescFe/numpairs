package org.cescfe.numpairs.feature.game.presentation

import org.cescfe.numpairs.domain.puzzle.Expression
import org.cescfe.numpairs.domain.puzzle.Operator
import org.cescfe.numpairs.domain.puzzle.Puzzle
import org.cescfe.numpairs.domain.puzzle.PuzzleCompletionState
import org.cescfe.numpairs.domain.puzzle.StripEntryUsageByOperator
import org.cescfe.numpairs.domain.puzzle.StripItem
import org.cescfe.numpairs.domain.puzzle.Tile
import org.cescfe.numpairs.domain.puzzle.TileResolutionState
import org.cescfe.numpairs.domain.puzzle.liveRuleConflictsByTile
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
        val liveRuleConflictsByUsage = puzzle.liveRuleConflictsByUsage(liveRuleConflictsByTile)

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
                modal = presentationState.modal,
                liveRuleConflictsByUsage = liveRuleConflictsByUsage
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
        modal: GameModalState?,
        liveRuleConflictsByUsage: Map<Pair<Int, Operator>, Set<RuleConflictUiState>>
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
            ).map { choice ->
                TileOperandOptionUiState(
                    choice = choice,
                    additionRuleConflicts = liveRuleConflictsByUsage[choice.stripEntryId to Operator.ADDITION]
                        .orEmpty(),
                    multiplicationRuleConflicts = liveRuleConflictsByUsage[
                        choice.stripEntryId to Operator.MULTIPLICATION
                    ].orEmpty()
                )
            }
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

private fun Puzzle.liveRuleConflictsByUsage(
    liveRuleConflictsByTile: Map<Int, Set<RuleConflictUiState>>
): Map<Pair<Int, Operator>, Set<RuleConflictUiState>> = liveRuleConflictsByTile
    .flatMap { (tileIndex, conflicts) ->
        val tile = board.tiles.getOrNull(tileIndex) ?: return@flatMap emptyList()
        val operator = tile.expression.operator

        if (operator == Operator.Hidden) {
            emptyList()
        } else {
            tile.expression.stripEntryIds.map { stripEntryId ->
                stripEntryId to operator to conflicts
            }
        }
    }
    .groupBy(
        keySelector = { (stripEntryIdAndOperator, _) -> stripEntryIdAndOperator },
        valueTransform = { (_, conflicts) -> conflicts }
    )
    .mapValues { (_, conflictSets) -> conflictSets.flatten().toSet() }

private val Expression.stripEntryIds: List<Int>
    get() = listOfNotNull(
        leftOperand.stripEntryId,
        rightOperand.stripEntryId
    )

private val Expression.Operand.stripEntryId: Int?
    get() = when (this) {
        Expression.Operand.Hidden -> null
        is Expression.Operand.Known -> stripEntryId
    }
