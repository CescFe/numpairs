package org.cescfe.numpairs.domain.generated.internal

import org.cescfe.numpairs.domain.generated.GeneratedPairsPuzzle
import org.cescfe.numpairs.domain.generated.GeneratedPuzzleProfile
import org.cescfe.numpairs.domain.generated.StripKnownEntryDistributionPolicy
import org.cescfe.numpairs.domain.puzzle.model.Expression
import org.cescfe.numpairs.domain.puzzle.model.Operator
import org.cescfe.numpairs.domain.puzzle.model.Puzzle
import org.cescfe.numpairs.domain.puzzle.model.PuzzleCompletionState
import org.cescfe.numpairs.domain.puzzle.model.StripItem
import org.cescfe.numpairs.domain.puzzle.model.Tile

internal class GeneratedPairsPuzzleValidator(private val profile: GeneratedPuzzleProfile) {
    fun isValid(generatedPuzzle: GeneratedPairsPuzzle, pairs: List<GeneratedPairsEntryPair>): Boolean =
        generatedPuzzle.solvedPuzzle.completionState == PuzzleCompletionState.SOLVED &&
            generatedPuzzle.solvedPuzzle.board.tiles.size == profile.size.boardTileCount &&
            generatedPuzzle.solvedPuzzle.strip.entries.size == profile.size.stripEntryCount &&
            generatedPuzzle.initialPuzzle.hasHiddenTileExpressions() &&
            generatedPuzzle.hasMatchingBoardResults() &&
            generatedPuzzle.solvedPuzzle.hasExpectedStripValues() &&
            generatedPuzzle.solvedPuzzle.hasExpectedBoardResults() &&
            pairs.all { pair -> pair.hasExpectedProduct() } &&
            pairs.hasExpectedProductAnchorMix() &&
            generatedPuzzle.initialPuzzle.hasExpectedStripMask(pairs = pairs)

    private fun GeneratedPairsPuzzle.hasMatchingBoardResults(): Boolean =
        initialPuzzle.board.tiles.map(Tile::result) == solvedPuzzle.board.tiles.map(Tile::result)

    private fun Puzzle.hasHiddenTileExpressions(): Boolean = board.tiles.all { tile ->
        tile.hasHiddenExpression()
    }

    private fun Puzzle.hasExpectedStripValues(): Boolean {
        val values = knownStripValues()

        return values.size == profile.size.stripEntryCount &&
            values == values.sorted() &&
            values.all { value -> value in profile.stripValuePolicy.valueRange } &&
            values.groupingBy { value -> value }
                .eachCount()
                .all { (_, occurrenceCount) ->
                    occurrenceCount <= profile.stripValuePolicy.maxOccurrencesPerValue
                }
    }

    private fun Puzzle.knownStripValues(): List<Int> = strip.entries.mapNotNull { entry ->
        (entry.item as? StripItem.Known)?.value
    }

    private fun Puzzle.hasExpectedBoardResults(): Boolean {
        if (profile.resultConstraints.allowsDuplicateBoardResults) {
            return true
        }

        return board.tiles.map(Tile::result).toSet().size == profile.size.boardTileCount
    }

    private fun GeneratedPairsEntryPair.hasExpectedProduct(): Boolean =
        product <= profile.resultConstraints.maxMultiplicationResult

    private fun List<GeneratedPairsEntryPair>.hasExpectedProductAnchorMix(): Boolean {
        val productAnchorMix = profile.resultConstraints.productAnchorMix ?: return true
        val productAnchorCount = count { pair ->
            pair.product > productAnchorMix.productResultGreaterThan
        }

        return productAnchorCount in productAnchorMix.countRange
    }

    private fun Puzzle.hasExpectedStripMask(pairs: List<GeneratedPairsEntryPair>): Boolean {
        val knownEntryIds = knownStripEntryIds()

        return knownEntryIds.size in profile.initialStripMaskPolicy.knownEntryCountRange &&
            strip.entries.count { entry -> entry.item == StripItem.Hidden } in
            profile.initialStripMaskPolicy.hiddenEntryCountRange &&
            knownEntryIds.containsAll(profile.requiredKnownEntryIds()) &&
            knownEntryIds.maxGeneratedPairsConsecutiveHiddenEntries(totalEntryCount = profile.size.stripEntryCount) <=
            profile.initialStripMaskPolicy.maxConsecutiveHiddenEntries &&
            knownEntryIds.hasExpectedDistribution(pairs = pairs)
    }

    private fun Set<Int>.hasExpectedDistribution(pairs: List<GeneratedPairsEntryPair>): Boolean =
        when (profile.initialStripMaskPolicy.distributionPolicy) {
            StripKnownEntryDistributionPolicy.SPREAD_ACROSS_STRIP_AND_PAIRS_WHEN_POSSIBLE -> {
                val pairKeyByEntryId = pairs.flatMap { pair ->
                    pair.entryIds.map { entryId -> entryId to pair.key }
                }.toMap()

                map { entryId -> pairKeyByEntryId.getValue(entryId) }.toSet().size == size
            }

            StripKnownEntryDistributionPolicy.UNRESTRICTED -> true
        }

    private fun Puzzle.knownStripEntryIds(): Set<Int> = strip.entries
        .filter { entry -> entry.item is StripItem.Known }
        .map { entry -> entry.id }
        .toSet()

    private fun Tile.hasHiddenExpression(): Boolean = expression.leftOperand == Expression.Operand.Hidden &&
        expression.operator == Operator.Hidden &&
        expression.rightOperand == Expression.Operand.Hidden
}
