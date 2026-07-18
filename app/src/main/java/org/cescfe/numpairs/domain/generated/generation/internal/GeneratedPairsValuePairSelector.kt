package org.cescfe.numpairs.domain.generated.generation.internal

import kotlin.random.Random
import org.cescfe.numpairs.domain.generated.profile.GeneratedPuzzleProfile

internal class GeneratedPairsValuePairSelector(
    private val profile: GeneratedPuzzleProfile,
    private val random: Random,
    private val constraints: GeneratedValuePairConstraintSet
) {
    constructor(profile: GeneratedPuzzleProfile, random: Random) : this(
        profile = profile,
        random = random,
        constraints = GeneratedPuzzleConstraintSet.from(profile = profile).valuePairs
    )

    fun selectValuePairs(variationPlan: GeneratedPairsVariationPlan): List<GeneratedPairsValuePair>? =
        when (val outcome = selectValuePairsWithControl(variationPlan = variationPlan, searchControl = null)) {
            is GeneratedPairsSearchOutcome.Found -> outcome.value
            GeneratedPairsSearchOutcome.NoCandidate,
            GeneratedPairsSearchOutcome.BudgetExhausted,
            GeneratedPairsSearchOutcome.Cancelled -> null
        }

    fun selectValuePairs(
        variationPlan: GeneratedPairsVariationPlan,
        searchControl: GeneratedPairsSearchControl
    ): GeneratedPairsSearchOutcome<List<GeneratedPairsValuePair>> =
        selectValuePairsWithControl(variationPlan = variationPlan, searchControl = searchControl)

    private fun selectValuePairsWithControl(
        variationPlan: GeneratedPairsVariationPlan,
        searchControl: GeneratedPairsSearchControl?
    ): GeneratedPairsSearchOutcome<List<GeneratedPairsValuePair>> {
        val candidatePairs = candidateValuePairsForProfile().shuffled(random)
        val plannedPairs = chooseValuePairs(
            candidatePairs = candidatePairs,
            primeProductDecoyDirective = variationPlan.primeProductDecoyDirective,
            repeatedValueGroupDirective = variationPlan.repeatedValueGroupDirective,
            searchControl = searchControl
        )

        if (plannedPairs !is GeneratedPairsSearchOutcome.NoCandidate ||
            variationPlan.hasNoValuePairVarietyDirectives()
        ) {
            return plannedPairs
        }

        return chooseValuePairs(
            candidatePairs = candidatePairs,
            primeProductDecoyDirective = GeneratedPairsPrimeProductDecoyDirective.Unrestricted,
            repeatedValueGroupDirective = GeneratedPairsRepeatedValueGroupDirective.Unrestricted,
            searchControl = searchControl
        )
    }

    private fun chooseValuePairs(
        candidatePairs: List<GeneratedPairsValuePair>,
        primeProductDecoyDirective: GeneratedPairsPrimeProductDecoyDirective,
        repeatedValueGroupDirective: GeneratedPairsRepeatedValueGroupDirective,
        searchControl: GeneratedPairsSearchControl?
    ): GeneratedPairsSearchOutcome<List<GeneratedPairsValuePair>> = chooseValuePairs(
        candidatePairs = candidatePairs,
        selectedPairs = emptyList(),
        valueOccurrences = emptyMap(),
        usedResults = emptySet(),
        productAnchorCount = 0,
        primeProductDecoyCount = 0,
        minimumCandidateIndex = 0,
        primeProductDecoyDirective = primeProductDecoyDirective,
        repeatedValueGroupDirective = repeatedValueGroupDirective,
        searchControl = searchControl
    )

    private fun chooseValuePairs(
        candidatePairs: List<GeneratedPairsValuePair>,
        selectedPairs: List<GeneratedPairsValuePair>,
        valueOccurrences: Map<Int, Int>,
        usedResults: Set<Int>,
        productAnchorCount: Int,
        primeProductDecoyCount: Int,
        minimumCandidateIndex: Int,
        primeProductDecoyDirective: GeneratedPairsPrimeProductDecoyDirective,
        repeatedValueGroupDirective: GeneratedPairsRepeatedValueGroupDirective,
        searchControl: GeneratedPairsSearchControl?
    ): GeneratedPairsSearchOutcome<List<GeneratedPairsValuePair>> {
        searchControl?.check()?.let { result ->
            if (result != GeneratedPairsSearchControlResult.Continue) {
                return result.toSearchOutcome()
            }
        }

        if (!constraints.canStillSatisfyProductAnchorMix(
                productAnchorCount = productAnchorCount,
                remainingPairSlots = profile.size.pairCount - selectedPairs.size
            )
        ) {
            return GeneratedPairsSearchOutcome.NoCandidate
        }

        if (!primeProductDecoyDirective.canStillBeSatisfied(
                currentPairCount = primeProductDecoyCount,
                remainingPairSlots = profile.size.pairCount - selectedPairs.size
            )
        ) {
            return GeneratedPairsSearchOutcome.NoCandidate
        }

        val repeatedValueGroupCount = valueOccurrences.repeatedValueGroupCount()
        if (!repeatedValueGroupDirective.canStillBeSatisfied(
                currentGroupCount = repeatedValueGroupCount,
                remainingPairSlots = profile.size.pairCount - selectedPairs.size
            )
        ) {
            return GeneratedPairsSearchOutcome.NoCandidate
        }

        if (selectedPairs.size == profile.size.pairCount) {
            return if (
                constraints.isComplete(pairs = selectedPairs) &&
                primeProductDecoyDirective.isSatisfiedBy(pairCount = primeProductDecoyCount) &&
                repeatedValueGroupDirective.isSatisfiedBy(groupCount = repeatedValueGroupCount)
            ) {
                GeneratedPairsSearchOutcome.Found(selectedPairs)
            } else {
                GeneratedPairsSearchOutcome.NoCandidate
            }
        }

        var orderedCandidatePairs = candidatePairs.withIndex()
            .filter { indexedPair -> indexedPair.index >= minimumCandidateIndex }
        if (repeatedValueGroupDirective.shouldPreferRepeatedValueGroup(
                currentGroupCount = repeatedValueGroupCount
            )
        ) {
            orderedCandidatePairs = orderedCandidatePairs.sortedByDescending { indexedPair ->
                valueOccurrences.with(indexedPair.value).repeatedValueGroupCount() - repeatedValueGroupCount
            }
        }
        if (
            primeProductDecoyDirective.shouldPreferPrimeProductDecoy(
                currentPairCount = primeProductDecoyCount
            )
        ) {
            orderedCandidatePairs = orderedCandidatePairs.sortedByDescending { indexedPair ->
                if (indexedPair.value.isPrimeProductDecoy()) 1 else 0
            }
        }

        orderedCandidatePairs.forEach { indexedPair ->
            val candidatePair = indexedPair.value
            searchControl?.consumeCandidateExpansion()?.let { result ->
                if (result != GeneratedPairsSearchControlResult.Continue) {
                    return result.toSearchOutcome()
                }
            }

            if (
                !constraints.canBeAdded(
                    pair = candidatePair,
                    valueOccurrences = valueOccurrences,
                    usedResults = usedResults
                )
            ) {
                return@forEach
            }

            val updatedPrimeProductDecoyCount =
                primeProductDecoyCount + candidatePair.primeProductDecoyIncrement()
            if (!primeProductDecoyDirective.allows(pairCount = updatedPrimeProductDecoyCount)) {
                return@forEach
            }
            val updatedValueOccurrences = valueOccurrences.with(candidatePair)
            if (!repeatedValueGroupDirective.allows(
                    groupCount = updatedValueOccurrences.repeatedValueGroupCount()
                )
            ) {
                return@forEach
            }

            when (
                val outcome = chooseValuePairs(
                    candidatePairs = candidatePairs,
                    selectedPairs = selectedPairs + candidatePair,
                    valueOccurrences = updatedValueOccurrences,
                    usedResults = usedResults + candidatePair.resultValues,
                    productAnchorCount = productAnchorCount +
                        constraints.productAnchorIncrement(pair = candidatePair),
                    primeProductDecoyCount = updatedPrimeProductDecoyCount,
                    minimumCandidateIndex = indexedPair.index +
                        if (profile.resultConstraints.allowsDuplicateBoardResults) 0 else 1,
                    primeProductDecoyDirective = primeProductDecoyDirective,
                    repeatedValueGroupDirective = repeatedValueGroupDirective,
                    searchControl = searchControl
                )
            ) {
                is GeneratedPairsSearchOutcome.Found -> return outcome
                GeneratedPairsSearchOutcome.BudgetExhausted,
                GeneratedPairsSearchOutcome.Cancelled -> return outcome
                GeneratedPairsSearchOutcome.NoCandidate -> Unit
            }
        }

        return GeneratedPairsSearchOutcome.NoCandidate
    }

    private fun candidateValuePairsForProfile(): List<GeneratedPairsValuePair> = buildList {
        profile.stripValuePolicy.valueRange.forEach { firstValue ->
            (firstValue..profile.stripValuePolicy.valueRange.last).forEach { secondValue ->
                val candidatePair = GeneratedPairsValuePair(
                    firstValue = firstValue,
                    secondValue = secondValue
                )

                if (
                    constraints.canBeAdded(
                        pair = candidatePair,
                        valueOccurrences = emptyMap(),
                        usedResults = emptySet()
                    )
                ) {
                    add(candidatePair)
                }
            }
        }
    }
}

private fun GeneratedPairsVariationPlan.hasNoValuePairVarietyDirectives(): Boolean =
    primeProductDecoyDirective == GeneratedPairsPrimeProductDecoyDirective.Unrestricted &&
        repeatedValueGroupDirective == GeneratedPairsRepeatedValueGroupDirective.Unrestricted

private val GeneratedPairsPrimeProductDecoyDirective.requiredPairCount: Int?
    get() = when (this) {
        GeneratedPairsPrimeProductDecoyDirective.Unrestricted -> null
        GeneratedPairsPrimeProductDecoyDirective.Exclude -> 0
        is GeneratedPairsPrimeProductDecoyDirective.Include -> pairCount
    }

private fun GeneratedPairsPrimeProductDecoyDirective.canStillBeSatisfied(
    currentPairCount: Int,
    remainingPairSlots: Int
): Boolean {
    val requiredPairCount = requiredPairCount ?: return true

    return currentPairCount <= requiredPairCount &&
        currentPairCount + remainingPairSlots >= requiredPairCount
}

private fun GeneratedPairsPrimeProductDecoyDirective.isSatisfiedBy(pairCount: Int): Boolean =
    requiredPairCount?.let { requiredPairCount -> pairCount == requiredPairCount } ?: true

private fun GeneratedPairsPrimeProductDecoyDirective.shouldPreferPrimeProductDecoy(currentPairCount: Int): Boolean =
    requiredPairCount?.let { requiredPairCount -> currentPairCount < requiredPairCount } ?: false

private fun GeneratedPairsPrimeProductDecoyDirective.allows(pairCount: Int): Boolean =
    requiredPairCount?.let { requiredPairCount -> pairCount <= requiredPairCount } ?: true

private val GeneratedPairsRepeatedValueGroupDirective.requiredGroupCountRange: IntRange?
    get() = when (this) {
        GeneratedPairsRepeatedValueGroupDirective.Unrestricted -> null
        GeneratedPairsRepeatedValueGroupDirective.Exclude -> 0..0
        is GeneratedPairsRepeatedValueGroupDirective.Include -> groupCount..groupCount
        is GeneratedPairsRepeatedValueGroupDirective.IncludeRange -> groupCountRange
    }

private fun GeneratedPairsRepeatedValueGroupDirective.canStillBeSatisfied(
    currentGroupCount: Int,
    remainingPairSlots: Int
): Boolean {
    val requiredRange = requiredGroupCountRange ?: return true

    return currentGroupCount <= requiredRange.last &&
        currentGroupCount + remainingPairSlots * 2 >= requiredRange.first
}

private fun GeneratedPairsRepeatedValueGroupDirective.isSatisfiedBy(groupCount: Int): Boolean =
    requiredGroupCountRange?.let { requiredRange -> groupCount in requiredRange } ?: true

private fun GeneratedPairsRepeatedValueGroupDirective.shouldPreferRepeatedValueGroup(currentGroupCount: Int): Boolean =
    requiredGroupCountRange?.let { requiredRange -> currentGroupCount < requiredRange.first } ?: false

private fun GeneratedPairsRepeatedValueGroupDirective.allows(groupCount: Int): Boolean =
    requiredGroupCountRange?.let { requiredRange -> groupCount <= requiredRange.last } ?: true

private fun Map<Int, Int>.repeatedValueGroupCount(): Int = values.count { occurrenceCount -> occurrenceCount > 1 }

private fun Map<Int, Int>.with(pair: GeneratedPairsValuePair): Map<Int, Int> {
    val updatedOccurrences = toMutableMap()

    pair.requiredOccurrencesByValue().forEach { (value, occurrenceCount) ->
        updatedOccurrences[value] = updatedOccurrences.getOrDefault(value, 0) + occurrenceCount
    }

    return updatedOccurrences
}
