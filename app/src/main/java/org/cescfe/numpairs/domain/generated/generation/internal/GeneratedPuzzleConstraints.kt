package org.cescfe.numpairs.domain.generated.generation.internal

import org.cescfe.numpairs.domain.generated.profile.GeneratedPuzzlePairSpecification
import org.cescfe.numpairs.domain.generated.profile.GeneratedPuzzlePairValues
import org.cescfe.numpairs.domain.generated.profile.GeneratedPuzzleProfile
import org.cescfe.numpairs.domain.generated.profile.ProductAnchorMix
import org.cescfe.numpairs.domain.generated.profile.ResultConstraints
import org.cescfe.numpairs.domain.generated.profile.StripKnownEntryDistributionPolicy
import org.cescfe.numpairs.domain.generated.profile.StripValuePolicy
import org.cescfe.numpairs.domain.generated.profile.acceptsBoardResults
import org.cescfe.numpairs.domain.generated.profile.isAnchor
import org.cescfe.numpairs.domain.generated.puzzle.GeneratedPairsPuzzleValidationViolation
import org.cescfe.numpairs.domain.puzzle.assignment.StripEntryId
import org.cescfe.numpairs.domain.puzzle.assignment.UnorderedStripEntryPair

internal data class GeneratedPuzzleConstraintSet(
    val valuePairs: GeneratedValuePairConstraintSet,
    val stripValues: GeneratedStripValueConstraint,
    val boardResults: GeneratedBoardResultConstraint,
    val multiplicationResults: GeneratedMultiplicationResultConstraint,
    val stripMask: GeneratedStripMaskConstraint
) {
    companion object {
        fun from(profile: GeneratedPuzzleProfile): GeneratedPuzzleConstraintSet {
            val stripValues = GeneratedStripValueConstraint(policy = profile.stripValuePolicy)
            val boardResults = GeneratedBoardResultConstraint(constraints = profile.resultConstraints)
            val multiplicationResults = GeneratedMultiplicationResultConstraint(
                constraints = profile.resultConstraints
            )

            return GeneratedPuzzleConstraintSet(
                valuePairs = GeneratedValuePairConstraintSet(
                    pairSpecification = GeneratedPuzzlePairSpecification(
                        stripValuePolicy = profile.stripValuePolicy,
                        resultConstraints = profile.resultConstraints
                    ),
                    stripValues = stripValues,
                    boardResults = boardResults,
                    multiplicationResults = multiplicationResults,
                    productAnchorMix = profile.resultConstraints.productAnchorMix
                ),
                stripValues = stripValues,
                boardResults = boardResults,
                multiplicationResults = multiplicationResults,
                stripMask = GeneratedStripMaskConstraint(profile = profile)
            )
        }
    }
}

internal class GeneratedValuePairConstraintSet(
    private val pairSpecification: GeneratedPuzzlePairSpecification,
    private val stripValues: GeneratedStripValueConstraint,
    private val boardResults: GeneratedBoardResultConstraint,
    private val multiplicationResults: GeneratedMultiplicationResultConstraint,
    private val productAnchorMix: ProductAnchorMix?
) {
    fun canBeAdded(pair: GeneratedPairsPairValues, valueOccurrences: Map<Int, Int>, usedResults: Set<Int>): Boolean =
        pairSpecification.canBeAdded(
            pair = pair.toProfilePairValues(),
            valueOccurrences = valueOccurrences,
            usedResults = usedResults
        )

    fun canStillSatisfyProductAnchorMix(productAnchorCount: Int, remainingPairSlots: Int): Boolean {
        val anchorMix = productAnchorMix ?: return true

        return productAnchorCount <= anchorMix.countRange.last &&
            productAnchorCount + remainingPairSlots >= anchorMix.countRange.first
    }

    fun isComplete(pairs: List<GeneratedPairsPairValues>): Boolean = stripValues.violationsFor(
        values = pairs.flatMap { pair -> listOf(pair.firstValue, pair.secondValue) }
    ).isEmpty() &&
        boardResults.violationsFor(
            results = pairs.flatMap { pair -> listOf(pair.sum, pair.product) }
        ).isEmpty() &&
        multiplicationResults.violationsFor(
            resultsByIndex = pairs.mapIndexed { index, pair -> index to pair.product }.toMap()
        ).isEmpty()

    fun productAnchorIncrement(pair: GeneratedPairsPairValues): Int =
        if (productAnchorMix?.isAnchor(product = pair.product) == true) 1 else 0
}

internal class GeneratedStripValueConstraint(private val policy: StripValuePolicy) {
    fun violationsFor(values: List<Int>): List<GeneratedPairsPuzzleValidationViolation> = buildList {
        val valuesOutsideRange = values.filterTo(mutableSetOf()) { value -> value !in policy.valueRange }
        if (valuesOutsideRange.isNotEmpty()) {
            add(
                GeneratedPairsPuzzleValidationViolation.StripValueOutsideRange(
                    allowedRange = policy.valueRange,
                    observedValues = valuesOutsideRange
                )
            )
        }

        val excessiveOccurrences = values.groupingBy { value -> value }
            .eachCount()
            .filterValues { occurrenceCount -> occurrenceCount > policy.maxOccurrencesPerValue }
        if (excessiveOccurrences.isNotEmpty()) {
            add(
                GeneratedPairsPuzzleValidationViolation.StripValueOccurrenceExceeded(
                    maximumAllowed = policy.maxOccurrencesPerValue,
                    observedOccurrencesByValue = excessiveOccurrences
                )
            )
        }

        val repeatedValueGroups = values.groupingBy { value -> value }
            .eachCount()
            .filterValues { occurrenceCount -> occurrenceCount > 1 }
        if (repeatedValueGroups.size < policy.minRepeatedValueGroupCount) {
            add(
                GeneratedPairsPuzzleValidationViolation.RepeatedStripValueGroupCountBelowMinimum(
                    minimumRequired = policy.minRepeatedValueGroupCount,
                    observedRepeatedValues = repeatedValueGroups.keys
                )
            )
        }
        policy.maxRepeatedValueGroupCount?.let { maximumAllowed ->
            if (repeatedValueGroups.size > maximumAllowed) {
                add(
                    GeneratedPairsPuzzleValidationViolation.RepeatedStripValueGroupCountExceeded(
                        maximumAllowed = maximumAllowed,
                        observedRepeatedValues = repeatedValueGroups.keys
                    )
                )
            }
        }
    }
}

internal class GeneratedBoardResultConstraint(private val constraints: ResultConstraints) {
    fun violationsFor(results: List<Int>): List<GeneratedPairsPuzzleValidationViolation> {
        if (constraints.acceptsBoardResults(results = results.map(Int::toLong))) {
            return emptyList()
        }

        val duplicateResults = results.groupingBy { result -> result }
            .eachCount()
            .filterValues { occurrenceCount -> occurrenceCount > 1 }
            .keys
        return listOf(
            GeneratedPairsPuzzleValidationViolation.DuplicateBoardResults(
                duplicateResults = duplicateResults
            )
        )
    }
}

internal class GeneratedMultiplicationResultConstraint(private val constraints: ResultConstraints) {
    fun violationsFor(resultsByIndex: Map<Int, Int>): List<GeneratedPairsPuzzleValidationViolation> = buildList {
        val excessiveResults = resultsByIndex.filterValues { result ->
            result > constraints.maxMultiplicationResult
        }
        if (excessiveResults.isNotEmpty()) {
            add(
                GeneratedPairsPuzzleValidationViolation.MultiplicationResultExceeded(
                    maximumAllowed = constraints.maxMultiplicationResult,
                    observedResultsByTileIndex = excessiveResults
                )
            )
        }

        constraints.productAnchorMix?.let { productAnchorMix ->
            val anchorCount = resultsByIndex.values.count(productAnchorMix::isAnchor)
            if (anchorCount !in productAnchorMix.countRange) {
                add(
                    GeneratedPairsPuzzleValidationViolation.ProductAnchorMixOutsideRange(
                        expectedRange = productAnchorMix.countRange,
                        observedCount = anchorCount
                    )
                )
            }
        }
    }
}

internal class GeneratedStripMaskConstraint(private val profile: GeneratedPuzzleProfile) {
    fun isSatisfied(
        knownEntryIds: Set<StripEntryId>,
        hiddenEntryCount: Int,
        solutionPairs: Set<UnorderedStripEntryPair>
    ): Boolean = violationsFor(
        knownEntryIds = knownEntryIds,
        hiddenEntryCount = hiddenEntryCount,
        solutionPairs = solutionPairs
    ).isEmpty()

    fun violationsFor(
        knownEntryIds: Set<StripEntryId>,
        hiddenEntryCount: Int,
        solutionPairs: Set<UnorderedStripEntryPair>
    ): List<GeneratedPairsPuzzleValidationViolation> = buildList {
        if (knownEntryIds.size !in profile.initialStripMaskPolicy.knownEntryCountRange) {
            add(
                GeneratedPairsPuzzleValidationViolation.KnownStripEntryCountOutsideRange(
                    expectedRange = profile.initialStripMaskPolicy.knownEntryCountRange,
                    observedCount = knownEntryIds.size
                )
            )
        }
        if (hiddenEntryCount !in profile.hiddenEntryCountRange) {
            add(
                GeneratedPairsPuzzleValidationViolation.HiddenStripEntryCountOutsideRange(
                    expectedRange = profile.hiddenEntryCountRange,
                    observedCount = hiddenEntryCount
                )
            )
        }

        val missingAnchorIds = profile.requiredKnownEntryIds() - knownEntryIds
        if (missingAnchorIds.isNotEmpty()) {
            add(
                GeneratedPairsPuzzleValidationViolation.RequiredKnownStripAnchorMissing(
                    missingEntryIds = missingAnchorIds
                )
            )
        }

        val maximumHiddenRun = knownEntryIds.maxGeneratedPairsConsecutiveHiddenEntries(
            totalEntryCount = profile.size.stripEntryCount
        )
        if (maximumHiddenRun > profile.initialStripMaskPolicy.maxConsecutiveHiddenEntries) {
            add(
                GeneratedPairsPuzzleValidationViolation.HiddenStripRunExceeded(
                    maximumAllowed = profile.initialStripMaskPolicy.maxConsecutiveHiddenEntries,
                    maximumObserved = maximumHiddenRun
                )
            )
        }

        if (!hasExpectedDistribution(knownEntryIds = knownEntryIds, solutionPairs = solutionPairs)) {
            add(
                GeneratedPairsPuzzleValidationViolation.KnownStripEntryDistributionMismatch(
                    knownEntryIds = knownEntryIds
                )
            )
        }
    }

    private fun hasExpectedDistribution(
        knownEntryIds: Set<StripEntryId>,
        solutionPairs: Set<UnorderedStripEntryPair>
    ): Boolean = when (profile.initialStripMaskPolicy.distributionPolicy) {
        StripKnownEntryDistributionPolicy.SpreadAcrossStripAndPairsWhenPossible -> {
            val solutionPairByEntryId = solutionPairs.flatMap { solutionPair ->
                listOf(
                    solutionPair.firstEntryId to solutionPair,
                    solutionPair.secondEntryId to solutionPair
                )
            }.toMap()
            val knownSolutionPairs = knownEntryIds.mapNotNull(solutionPairByEntryId::get)

            knownSolutionPairs.size == knownEntryIds.size &&
                knownSolutionPairs.toSet().size == knownEntryIds.size
        }

        is StripKnownEntryDistributionPolicy.AtLeastDistinctSolutionPairs -> {
            val policy = profile.initialStripMaskPolicy.distributionPolicy
            val solutionPairByEntryId = solutionPairs.flatMap { solutionPair ->
                listOf(
                    solutionPair.firstEntryId to solutionPair,
                    solutionPair.secondEntryId to solutionPair
                )
            }.toMap()
            knownEntryIds.mapNotNull(solutionPairByEntryId::get).toSet().size >= policy.minimumPairCount
        }

        StripKnownEntryDistributionPolicy.Unrestricted -> true
    }
}

private fun GeneratedPairsPairValues.toProfilePairValues(): GeneratedPuzzlePairValues = GeneratedPuzzlePairValues(
    firstValue = firstValue,
    secondValue = secondValue
)
