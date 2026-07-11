package org.cescfe.numpairs.domain.generated.internal

import org.cescfe.numpairs.domain.generated.GeneratedPairsPuzzleValidationViolation
import org.cescfe.numpairs.domain.generated.GeneratedPuzzleProfile
import org.cescfe.numpairs.domain.puzzle.assignment.IndexedResolvedTileAssignment
import org.cescfe.numpairs.domain.puzzle.assignment.StripEntryId
import org.cescfe.numpairs.domain.puzzle.assignment.UnorderedStripEntryPair
import org.cescfe.numpairs.domain.puzzle.assignment.resolvedTileAssignments
import org.cescfe.numpairs.domain.puzzle.model.Expression
import org.cescfe.numpairs.domain.puzzle.model.Operator
import org.cescfe.numpairs.domain.puzzle.model.Puzzle
import org.cescfe.numpairs.domain.puzzle.model.PuzzleCompletionState
import org.cescfe.numpairs.domain.puzzle.model.StripItem
import org.cescfe.numpairs.domain.puzzle.model.Tile

internal class GeneratedPairsPuzzleValidator(private val profile: GeneratedPuzzleProfile) {
    private val hardRules = GeneratedPuzzleHardRuleSet.from(profile = profile)

    fun validate(initialPuzzle: Puzzle, solvedPuzzle: Puzzle): GeneratedPairsPuzzleValidationReport {
        val assignments = solvedPuzzle.resolvedTileAssignments()
        val additionSolutionPairs = assignments.solutionPairsFor(operator = Operator.ADDITION)

        val violations = buildList {
            addAll(validateSolvedPuzzle(solvedPuzzle = solvedPuzzle).violations)
            addInitialPuzzleShapeViolations(initialPuzzle = initialPuzzle)
            addTransformationViolations(
                initialPuzzle = initialPuzzle,
                solvedPuzzle = solvedPuzzle,
                solvedKnownValuesById = solvedPuzzle.knownStripValuesById()
            )

            val knownEntryIds = initialPuzzle.knownStripEntryIds()
            addAll(
                hardRules.stripMask.violationsFor(
                    knownEntryIds = knownEntryIds,
                    hiddenEntryCount = initialPuzzle.strip.entries.count { entry ->
                        entry.item == StripItem.Hidden
                    },
                    solutionPairs = additionSolutionPairs
                )
            )
        }

        return GeneratedPairsPuzzleValidationReport(violations = violations)
    }

    fun validateSolvedPuzzle(solvedPuzzle: Puzzle): GeneratedPairsPuzzleValidationReport {
        val solvedKnownValuesById = solvedPuzzle.knownStripValuesById()
        val assignments = solvedPuzzle.resolvedTileAssignments()

        val violations = buildList {
            addSolvedPuzzleStateViolations(solvedPuzzle = solvedPuzzle)
            addSolvedStripViolations(
                solvedPuzzle = solvedPuzzle,
                solvedKnownValuesById = solvedKnownValuesById
            )
            addAssignmentViolations(
                solvedPuzzle = solvedPuzzle,
                solvedKnownValuesById = solvedKnownValuesById,
                assignments = assignments
            )

            addAll(hardRules.stripValues.violationsFor(values = solvedKnownValuesById.values.toList()))
            addAll(
                hardRules.boardResults.violationsFor(
                    results = solvedPuzzle.board.tiles.map(Tile::result)
                )
            )
            addAll(
                hardRules.multiplicationResults.violationsFor(
                    resultsByIndex = assignments
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
        if (solvedPuzzle.completionState != PuzzleCompletionState.SOLVED) {
            add(
                GeneratedPairsPuzzleValidationViolation.SolvedPuzzleNotSolved(
                    completionState = solvedPuzzle.completionState
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
        solvedPuzzle: Puzzle,
        solvedKnownValuesById: Map<StripEntryId, Int>,
        assignments: List<IndexedResolvedTileAssignment>
    ) {
        if (assignments.size != solvedPuzzle.board.tiles.size) {
            add(
                GeneratedPairsPuzzleValidationViolation.SolvedTileAssignmentsIncomplete(
                    expectedCount = solvedPuzzle.board.tiles.size,
                    observedCount = assignments.size
                )
            )
        }

        val unknownEntryIdsByTileIndex = assignments.mapNotNull { assignment ->
            val unknownEntryIds = assignment.operandEntryIds.filterTo(mutableSetOf()) { entryId ->
                entryId !in solvedKnownValuesById
            }
            assignment.tileIndex.to(unknownEntryIds).takeIf { (_, entryIds) -> entryIds.isNotEmpty() }
        }.toMap()
        if (unknownEntryIdsByTileIndex.isNotEmpty()) {
            add(
                GeneratedPairsPuzzleValidationViolation.SolvedOperandEntryReferenceInvalid(
                    unknownEntryIdsByTileIndex = unknownEntryIdsByTileIndex
                )
            )
        }

        val valueMismatchTileIndexes = assignments.mapNotNullTo(mutableSetOf()) { assignment ->
            assignment.tileIndex.takeUnless {
                solvedKnownValuesById[assignment.leftOperand.stripEntryId] == assignment.leftOperand.value &&
                    solvedKnownValuesById[assignment.rightOperand.stripEntryId] == assignment.rightOperand.value
            }
        }
        if (valueMismatchTileIndexes.isNotEmpty()) {
            add(
                GeneratedPairsPuzzleValidationViolation.SolvedOperandValueMismatch(
                    tileIndexes = valueMismatchTileIndexes
                )
            )
        }

        val solvedEntryIds = solvedPuzzle.strip.entries.mapTo(mutableSetOf()) { entry -> StripEntryId(entry.id) }
        val additionUsageByEntryId = assignments.usageCountsFor(operator = Operator.ADDITION)
        val multiplicationUsageByEntryId = assignments.usageCountsFor(operator = Operator.MULTIPLICATION)
        val allReferencedEntryIds = additionUsageByEntryId.keys + multiplicationUsageByEntryId.keys
        val hasUsageMismatch = (solvedEntryIds + allReferencedEntryIds).any { entryId ->
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

        val additionPairs = assignments.solutionPairsFor(operator = Operator.ADDITION)
        val multiplicationPairs = assignments.solutionPairsFor(operator = Operator.MULTIPLICATION)
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

private fun Puzzle.knownStripValuesById(): Map<StripEntryId, Int> = strip.entries.mapNotNull { entry ->
    (entry.item as? StripItem.Known)?.let { item -> StripEntryId(entry.id) to item.value }
}.toMap()

private fun Puzzle.knownStripEntryIds(): Set<StripEntryId> = strip.entries
    .filter { entry -> entry.item is StripItem.Known }
    .mapTo(mutableSetOf()) { entry -> StripEntryId(entry.id) }

private val IndexedResolvedTileAssignment.operandEntryIds: List<StripEntryId>
    get() = listOf(leftOperand.stripEntryId, rightOperand.stripEntryId)

private fun List<IndexedResolvedTileAssignment>.usageCountsFor(operator: Operator): Map<StripEntryId, Int> =
    filter { assignment -> assignment.operator == operator }
        .flatMap(IndexedResolvedTileAssignment::operandEntryIds)
        .groupingBy { entryId -> entryId }
        .eachCount()

private fun List<IndexedResolvedTileAssignment>.solutionPairsFor(operator: Operator): Set<UnorderedStripEntryPair> =
    filter { assignment -> assignment.operator == operator }
        .mapTo(mutableSetOf()) { assignment ->
            UnorderedStripEntryPair.of(
                firstEntryId = assignment.leftOperand.stripEntryId,
                secondEntryId = assignment.rightOperand.stripEntryId
            )
        }
