package org.cescfe.numpairs.domain.generated.puzzle.internal

import org.cescfe.numpairs.domain.generated.generation.GeneratedPuzzleGenerationContext
import org.cescfe.numpairs.domain.generated.puzzle.GeneratedPairsPuzzleValidationViolation
import org.cescfe.numpairs.domain.puzzle.assignment.ResolvedPuzzleAnalysis
import org.cescfe.numpairs.domain.puzzle.assignment.StripEntryId
import org.cescfe.numpairs.domain.puzzle.assignment.analyzeResolvedPuzzle
import org.cescfe.numpairs.domain.puzzle.model.Expression
import org.cescfe.numpairs.domain.puzzle.model.Operator
import org.cescfe.numpairs.domain.puzzle.model.Puzzle
import org.cescfe.numpairs.domain.puzzle.model.PuzzleCompletionState
import org.cescfe.numpairs.domain.puzzle.model.StripItem
import org.cescfe.numpairs.domain.puzzle.model.Tile

internal class GeneratedPairsPuzzleValidator(context: GeneratedPuzzleGenerationContext) {
    private val profile = context.profile
    private val constraints = context.constraints

    fun validate(initialPuzzle: Puzzle, solvedPuzzle: Puzzle): GeneratedPairsPuzzleValidationReport {
        val solvedAnalysis = solvedPuzzle.analyzeResolvedPuzzle()

        val violations = buildList {
            addAll(
                validateSolvedPuzzle(
                    solvedPuzzle = solvedPuzzle,
                    solvedAnalysis = solvedAnalysis
                ).violations
            )
            addInitialPuzzleShapeViolations(initialPuzzle = initialPuzzle)
            addTransformationViolations(
                initialPuzzle = initialPuzzle,
                solvedPuzzle = solvedPuzzle,
                solvedKnownValuesById = solvedAnalysis.knownStripValuesByEntryId
            )

            val knownEntryIds = initialPuzzle.knownStripEntryIds()
            addAll(
                constraints.stripMask.violationsFor(
                    knownEntryIds = knownEntryIds,
                    hiddenEntryCount = initialPuzzle.strip.entries.count { entry ->
                        entry.item == StripItem.Hidden
                    },
                    solutionPairs = solvedAnalysis.solutionPairsFor(operator = Operator.ADDITION)
                )
            )
        }

        return GeneratedPairsPuzzleValidationReport(violations = violations)
    }

    fun validateSolvedPuzzle(solvedPuzzle: Puzzle): GeneratedPairsPuzzleValidationReport = validateSolvedPuzzle(
        solvedPuzzle = solvedPuzzle,
        solvedAnalysis = solvedPuzzle.analyzeResolvedPuzzle()
    )

    private fun validateSolvedPuzzle(
        solvedPuzzle: Puzzle,
        solvedAnalysis: ResolvedPuzzleAnalysis
    ): GeneratedPairsPuzzleValidationReport {
        val violations = buildList {
            addSolvedPuzzleStateViolations(solvedPuzzle = solvedPuzzle)
            addSolvedStripViolations(
                solvedPuzzle = solvedPuzzle,
                solvedKnownValuesById = solvedAnalysis.knownStripValuesByEntryId
            )
            addAssignmentViolations(solvedAnalysis = solvedAnalysis)

            addAll(
                constraints.stripValues.violationsFor(
                    values = solvedAnalysis.knownStripValuesByEntryId.values.toList()
                )
            )
            addAll(
                constraints.boardResults.violationsFor(
                    results = solvedPuzzle.board.tiles.map(Tile::result)
                )
            )
            addAll(
                constraints.multiplicationResults.violationsFor(
                    resultsByIndex = solvedAnalysis.resolvedAssignments
                        .filter { assignment -> assignment.operator == Operator.MULTIPLICATION }
                        .associate { assignment ->
                            assignment.tileIndex to solvedPuzzle.board.tiles[assignment.tileIndex].result
                        }
                )
            )
        }

        return GeneratedPairsPuzzleValidationReport(violations = violations)
    }

    private fun MutableList<GeneratedPairsPuzzleValidationViolation>.addSolvedPuzzleStateViolations(
        solvedPuzzle: Puzzle
    ) {
        val completionState = solvedPuzzle.completionState
        if (completionState != PuzzleCompletionState.SOLVED) {
            add(
                GeneratedPairsPuzzleValidationViolation.SolvedPuzzleNotSolved(
                    completionState = completionState
                )
            )
        }
        if (solvedPuzzle.board.tiles.size != profile.size.boardTileCount) {
            add(
                GeneratedPairsPuzzleValidationViolation.SolvedBoardTileCountMismatch(
                    expectedCount = profile.size.boardTileCount,
                    observedCount = solvedPuzzle.board.tiles.size
                )
            )
        }
        if (solvedPuzzle.strip.entries.size != profile.size.stripEntryCount) {
            add(
                GeneratedPairsPuzzleValidationViolation.SolvedStripEntryCountMismatch(
                    expectedCount = profile.size.stripEntryCount,
                    observedCount = solvedPuzzle.strip.entries.size
                )
            )
        }
    }

    private fun MutableList<GeneratedPairsPuzzleValidationViolation>.addInitialPuzzleShapeViolations(
        initialPuzzle: Puzzle
    ) {
        if (initialPuzzle.board.tiles.size != profile.size.boardTileCount) {
            add(
                GeneratedPairsPuzzleValidationViolation.InitialBoardTileCountMismatch(
                    expectedCount = profile.size.boardTileCount,
                    observedCount = initialPuzzle.board.tiles.size
                )
            )
        }
        if (initialPuzzle.strip.entries.size != profile.size.stripEntryCount) {
            add(
                GeneratedPairsPuzzleValidationViolation.InitialStripEntryCountMismatch(
                    expectedCount = profile.size.stripEntryCount,
                    observedCount = initialPuzzle.strip.entries.size
                )
            )
        }
    }

    private fun MutableList<GeneratedPairsPuzzleValidationViolation>.addTransformationViolations(
        initialPuzzle: Puzzle,
        solvedPuzzle: Puzzle,
        solvedKnownValuesById: Map<StripEntryId, Int>
    ) {
        val nonHiddenTileIndexes = initialPuzzle.board.tiles.mapIndexedNotNull { tileIndex, tile ->
            tileIndex.takeUnless { tile.hasHiddenExpression() }
        }
        if (nonHiddenTileIndexes.isNotEmpty()) {
            add(
                GeneratedPairsPuzzleValidationViolation.InitialTileExpressionsNotHidden(
                    tileIndexes = nonHiddenTileIndexes
                )
            )
        }

        val largestBoardSize = maxOf(initialPuzzle.board.tiles.size, solvedPuzzle.board.tiles.size)
        val mismatchedResultIndexes = (0 until largestBoardSize).filter { tileIndex ->
            initialPuzzle.board.tiles.getOrNull(tileIndex)?.result !=
                solvedPuzzle.board.tiles.getOrNull(tileIndex)?.result
        }
        if (mismatchedResultIndexes.isNotEmpty()) {
            add(
                GeneratedPairsPuzzleValidationViolation.BoardResultsMismatch(
                    mismatchedTileIndexes = mismatchedResultIndexes
                )
            )
        }

        val solvedEntryIds = solvedPuzzle.strip.entries.map { entry -> StripEntryId(entry.id) }
        val initialEntryIds = initialPuzzle.strip.entries.map { entry -> StripEntryId(entry.id) }
        if (initialEntryIds != solvedEntryIds) {
            add(
                GeneratedPairsPuzzleValidationViolation.InitialStripEntryIdentitiesMismatch(
                    solvedEntryIds = solvedEntryIds,
                    initialEntryIds = initialEntryIds
                )
            )
        }

        val mismatchedVisibleEntryIds = initialPuzzle.strip.entries.mapNotNullTo(mutableSetOf()) { entry ->
            val knownItem = entry.item as? StripItem.Known ?: return@mapNotNullTo null
            StripEntryId(entry.id).takeUnless { entryId -> solvedKnownValuesById[entryId] == knownItem.value }
        }
        if (mismatchedVisibleEntryIds.isNotEmpty()) {
            add(
                GeneratedPairsPuzzleValidationViolation.InitialVisibleStripValueMismatch(
                    entryIds = mismatchedVisibleEntryIds
                )
            )
        }

        val playerEnteredEntryIds = initialPuzzle.strip.entries.mapNotNullTo(mutableSetOf()) { entry ->
            StripEntryId(entry.id).takeIf { entry.item is StripItem.PlayerEntered }
        }
        if (playerEnteredEntryIds.isNotEmpty()) {
            add(
                GeneratedPairsPuzzleValidationViolation.InitialStripItemsNotMasked(
                    entryIds = playerEnteredEntryIds
                )
            )
        }
    }

    private fun MutableList<GeneratedPairsPuzzleValidationViolation>.addSolvedStripViolations(
        solvedPuzzle: Puzzle,
        solvedKnownValuesById: Map<StripEntryId, Int>
    ) {
        val nonKnownEntryIds = solvedPuzzle.strip.entries.mapNotNullTo(mutableSetOf()) { entry ->
            StripEntryId(entry.id).takeUnless { entry.item is StripItem.Known }
        }
        if (nonKnownEntryIds.isNotEmpty()) {
            add(
                GeneratedPairsPuzzleValidationViolation.SolvedStripValuesNotFullyKnown(
                    entryIds = nonKnownEntryIds
                )
            )
        }

        val values = solvedPuzzle.strip.entries.mapNotNull { entry ->
            solvedKnownValuesById[StripEntryId(entry.id)]
        }
        if (values != values.sorted()) {
            add(
                GeneratedPairsPuzzleValidationViolation.SolvedStripValuesNotSorted(
                    observedValues = values
                )
            )
        }
    }

    private fun MutableList<GeneratedPairsPuzzleValidationViolation>.addAssignmentViolations(
        solvedAnalysis: ResolvedPuzzleAnalysis
    ) {
        if (solvedAnalysis.unresolvedAssignments.isNotEmpty()) {
            add(
                GeneratedPairsPuzzleValidationViolation.SolvedTileAssignmentsIncomplete(
                    expectedCount = solvedAnalysis.resolvedAssignments.size +
                        solvedAnalysis.unresolvedAssignments.size,
                    observedCount = solvedAnalysis.resolvedAssignments.size
                )
            )
        }

        if (solvedAnalysis.unknownEntryIdsByTileIndex.isNotEmpty()) {
            add(
                GeneratedPairsPuzzleValidationViolation.SolvedOperandEntryReferenceInvalid(
                    unknownEntryIdsByTileIndex = solvedAnalysis.unknownEntryIdsByTileIndex
                )
            )
        }

        if (solvedAnalysis.operandValueMismatchTileIndexes.isNotEmpty()) {
            add(
                GeneratedPairsPuzzleValidationViolation.SolvedOperandValueMismatch(
                    tileIndexes = solvedAnalysis.operandValueMismatchTileIndexes
                )
            )
        }

        val additionUsageByEntryId = solvedAnalysis.usageCountsFor(operator = Operator.ADDITION)
        val multiplicationUsageByEntryId = solvedAnalysis.usageCountsFor(operator = Operator.MULTIPLICATION)
        val allReferencedEntryIds = additionUsageByEntryId.keys + multiplicationUsageByEntryId.keys
        val hasUsageMismatch = (solvedAnalysis.stripEntryIds + allReferencedEntryIds).any { entryId ->
            additionUsageByEntryId[entryId] != 1 || multiplicationUsageByEntryId[entryId] != 1
        }
        if (hasUsageMismatch) {
            add(
                GeneratedPairsPuzzleValidationViolation.SolvedStripEntryUsageMismatch(
                    additionUsageByEntryId = additionUsageByEntryId,
                    multiplicationUsageByEntryId = multiplicationUsageByEntryId
                )
            )
        }

        val additionPairs = solvedAnalysis.solutionPairsFor(operator = Operator.ADDITION)
        val multiplicationPairs = solvedAnalysis.solutionPairsFor(operator = Operator.MULTIPLICATION)
        if (additionPairs != multiplicationPairs) {
            add(
                GeneratedPairsPuzzleValidationViolation.SolvedSumProductPairingMismatch(
                    additionPairs = additionPairs,
                    multiplicationPairs = multiplicationPairs
                )
            )
        }
    }
}

private fun Tile.hasHiddenExpression(): Boolean = expression.leftOperand == Expression.Operand.Hidden &&
    expression.operator == Operator.Hidden &&
    expression.rightOperand == Expression.Operand.Hidden

private fun Puzzle.knownStripEntryIds(): Set<StripEntryId> = strip.entries
    .filter { entry -> entry.item is StripItem.Known }
    .mapTo(mutableSetOf()) { entry -> StripEntryId(entry.id) }
