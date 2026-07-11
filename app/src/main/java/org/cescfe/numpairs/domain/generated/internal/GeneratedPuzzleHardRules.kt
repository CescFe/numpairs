package org.cescfe.numpairs.domain.generated.internal

import org.cescfe.numpairs.domain.generated.GeneratedPairsPuzzleValidationViolation
import org.cescfe.numpairs.domain.generated.GeneratedPuzzlePairSpecification
import org.cescfe.numpairs.domain.generated.GeneratedPuzzlePairValues
import org.cescfe.numpairs.domain.generated.GeneratedPuzzleProfile
import org.cescfe.numpairs.domain.generated.ProductAnchorMix
import org.cescfe.numpairs.domain.generated.ResultConstraints
import org.cescfe.numpairs.domain.generated.StripKnownEntryDistributionPolicy
import org.cescfe.numpairs.domain.generated.StripValuePolicy
import org.cescfe.numpairs.domain.generated.acceptsBoardResults
import org.cescfe.numpairs.domain.generated.isAnchor
import org.cescfe.numpairs.domain.puzzle.assignment.StripEntryId
import org.cescfe.numpairs.domain.puzzle.assignment.UnorderedStripEntryPair

internal data class GeneratedPuzzleHardRuleSet(
    val valuePairs: GeneratedValuePairRuleSet,
    val stripValues: GeneratedStripValueRule,
    val boardResults: GeneratedBoardResultRule,
    val multiplicationResults: GeneratedMultiplicationResultRule,
    val stripMask: GeneratedStripMaskRule
) {
    companion object {
        fun from(profile: GeneratedPuzzleProfile): GeneratedPuzzleHardRuleSet {
            val stripValues = GeneratedStripValueRule(policy = profile.stripValuePolicy)
            val boardResults = GeneratedBoardResultRule(constraints = profile.resultConstraints)
            val multiplicationResults = GeneratedMultiplicationResultRule(
                constraints = profile.resultConstraints
            )

            return GeneratedPuzzleHardRuleSet(
                valuePairs = GeneratedValuePairRuleSet(
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
                stripMask = GeneratedStripMaskRule(profile = profile)
            )
        }
    }
}

internal class GeneratedValuePairRuleSet(
    private val pairSpecification: GeneratedPuzzlePairSpecification,
    private val stripValues: GeneratedStripValueRule,
    private val boardResults: GeneratedBoardResultRule,
    private val multiplicationResults: GeneratedMultiplicationResultRule,
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

internal class GeneratedStripValueRule(private val policy: StripValuePolicy) {
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
    }
}

internal class GeneratedBoardResultRule(private val constraints: ResultConstraints) {
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

internal class GeneratedMultiplicationResultRule(private val constraints: ResultConstraints) {
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

internal class GeneratedStripMaskRule(private val profile: GeneratedPuzzleProfile) {
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
        StripKnownEntryDistributionPolicy.SPREAD_ACROSS_STRIP_AND_PAIRS_WHEN_POSSIBLE -> {
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

        StripKnownEntryDistributionPolicy.UNRESTRICTED -> true
    }
}

private fun GeneratedPairsPairValues.toProfilePairValues(): GeneratedPuzzlePairValues = GeneratedPuzzlePairValues(
    firstValue = firstValue,
    secondValue = secondValue
)
