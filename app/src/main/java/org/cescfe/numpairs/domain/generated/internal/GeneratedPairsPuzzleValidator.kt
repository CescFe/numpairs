package org.cescfe.numpairs.domain.generated.internal

import org.cescfe.numpairs.domain.generated.GeneratedPairsPuzzle
import org.cescfe.numpairs.domain.generated.GeneratedPuzzleProfile
import org.cescfe.numpairs.domain.puzzle.assignment.IndexedResolvedTileAssignment
import org.cescfe.numpairs.domain.puzzle.assignment.resolvedTileAssignments
import org.cescfe.numpairs.domain.puzzle.model.Expression
import org.cescfe.numpairs.domain.puzzle.model.Operator
import org.cescfe.numpairs.domain.puzzle.model.Puzzle
import org.cescfe.numpairs.domain.puzzle.model.PuzzleCompletionState
import org.cescfe.numpairs.domain.puzzle.model.StripEntry
import org.cescfe.numpairs.domain.puzzle.model.StripItem
import org.cescfe.numpairs.domain.puzzle.model.Tile

internal class GeneratedPairsPuzzleValidator(private val profile: GeneratedPuzzleProfile) {
    private val hardRules = GeneratedPuzzleHardRuleSet.from(profile = profile)

    fun validate(generatedPuzzle: GeneratedPairsPuzzle): GeneratedPairsPuzzleValidationReport {
        val solvedPuzzle = generatedPuzzle.solvedPuzzle
        val initialPuzzle = generatedPuzzle.initialPuzzle
        val solvedKnownValuesById = solvedPuzzle.knownStripValuesById()
        val assignments = solvedPuzzle.resolvedTileAssignments()
        val additionPairKeys = assignments.pairKeysFor(operator = Operator.ADDITION)

        val violations = buildList {
            addPuzzleStateViolations(generatedPuzzle = generatedPuzzle)
            addTransformationViolations(
                initialPuzzle = initialPuzzle,
                solvedPuzzle = solvedPuzzle,
                solvedKnownValuesById = solvedKnownValuesById
            )
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

            val knownEntryIds = initialPuzzle.knownStripEntryIds()
            addAll(
                hardRules.stripMask.violationsFor(
                    knownEntryIds = knownEntryIds,
                    hiddenEntryCount = initialPuzzle.strip.entries.count { entry ->
                        entry.item == StripItem.Hidden
                    },
                    pairKeys = additionPairKeys
                )
            )
        }

        return GeneratedPairsPuzzleValidationReport(violations = violations)
    }

    private fun MutableList<GeneratedPairsPuzzleValidationViolation>.addPuzzleStateViolations(
        generatedPuzzle: GeneratedPairsPuzzle
    ) {
        val solvedPuzzle = generatedPuzzle.solvedPuzzle
        val initialPuzzle = generatedPuzzle.initialPuzzle

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
        solvedKnownValuesById: Map<Int, Int>
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

        val solvedEntryIds = solvedPuzzle.strip.entries.map(StripEntry::id)
        val initialEntryIds = initialPuzzle.strip.entries.map(StripEntry::id)
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
            entry.id.takeUnless { solvedKnownValuesById[entry.id] == knownItem.value }
        }
        if (mismatchedVisibleEntryIds.isNotEmpty()) {
            add(
                GeneratedPairsPuzzleValidationViolation.InitialVisibleStripValueMismatch(
                    entryIds = mismatchedVisibleEntryIds
                )
            )
        }

        val playerEnteredEntryIds = initialPuzzle.strip.entries.mapNotNullTo(mutableSetOf()) { entry ->
            entry.id.takeIf { entry.item is StripItem.PlayerEntered }
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
        solvedKnownValuesById: Map<Int, Int>
    ) {
        val nonKnownEntryIds = solvedPuzzle.strip.entries.mapNotNullTo(mutableSetOf()) { entry ->
            entry.id.takeUnless { entry.item is StripItem.Known }
        }
        if (nonKnownEntryIds.isNotEmpty()) {
            add(
                GeneratedPairsPuzzleValidationViolation.SolvedStripValuesNotFullyKnown(
                    entryIds = nonKnownEntryIds
                )
            )
        }

        val values = solvedPuzzle.strip.entries.mapNotNull { entry -> solvedKnownValuesById[entry.id] }
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
        solvedKnownValuesById: Map<Int, Int>,
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

        val solvedEntryIds = solvedPuzzle.strip.entries.mapTo(mutableSetOf(), StripEntry::id)
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

        val additionPairs = assignments.pairKeysFor(operator = Operator.ADDITION)
        val multiplicationPairs = assignments.pairKeysFor(operator = Operator.MULTIPLICATION)
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

private fun Puzzle.knownStripValuesById(): Map<Int, Int> = strip.entries.mapNotNull { entry ->
    (entry.item as? StripItem.Known)?.let { item -> entry.id to item.value }
}.toMap()

private fun Puzzle.knownStripEntryIds(): Set<Int> = strip.entries
    .filter { entry -> entry.item is StripItem.Known }
    .mapTo(mutableSetOf(), StripEntry::id)

private val IndexedResolvedTileAssignment.operandEntryIds: List<Int>
    get() = listOf(leftOperand.stripEntryId, rightOperand.stripEntryId)

private fun List<IndexedResolvedTileAssignment>.usageCountsFor(operator: Operator): Map<Int, Int> =
    filter { assignment -> assignment.operator == operator }
        .flatMap(IndexedResolvedTileAssignment::operandEntryIds)
        .groupingBy { entryId -> entryId }
        .eachCount()

private fun List<IndexedResolvedTileAssignment>.pairKeysFor(operator: Operator): Set<GeneratedPairsEntryPairKey> =
    filter { assignment -> assignment.operator == operator }
        .mapTo(mutableSetOf()) { assignment ->
            GeneratedPairsEntryPairKey(
                firstEntryId = minOf(
                    assignment.leftOperand.stripEntryId,
                    assignment.rightOperand.stripEntryId
                ),
                secondEntryId = maxOf(
                    assignment.leftOperand.stripEntryId,
                    assignment.rightOperand.stripEntryId
                )
            )
        }
