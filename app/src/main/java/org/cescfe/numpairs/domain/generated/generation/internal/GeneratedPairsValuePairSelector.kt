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
            searchControl = searchControl
        )

        if (plannedPairs !is GeneratedPairsSearchOutcome.NoCandidate ||
            variationPlan.primeProductDecoyDirective == GeneratedPairsPrimeProductDecoyDirective.Unrestricted
        ) {
            return plannedPairs
        }

        return chooseValuePairs(
            candidatePairs = candidatePairs,
            primeProductDecoyDirective = GeneratedPairsPrimeProductDecoyDirective.Unrestricted,
            searchControl = searchControl
        )
    }

    private fun chooseValuePairs(
        candidatePairs: List<GeneratedPairsValuePair>,
        primeProductDecoyDirective: GeneratedPairsPrimeProductDecoyDirective,
        searchControl: GeneratedPairsSearchControl?
    ): GeneratedPairsSearchOutcome<List<GeneratedPairsValuePair>> = chooseValuePairs(
        candidatePairs = candidatePairs,
        selectedPairs = emptyList(),
        valueOccurrences = emptyMap(),
        usedResults = emptySet(),
        productAnchorCount = 0,
        primeProductDecoyCount = 0,
        primeProductDecoyDirective = primeProductDecoyDirective,
        searchControl = searchControl
    )

    private fun chooseValuePairs(
        candidatePairs: List<GeneratedPairsValuePair>,
        selectedPairs: List<GeneratedPairsValuePair>,
        valueOccurrences: Map<Int, Int>,
        usedResults: Set<Int>,
        productAnchorCount: Int,
        primeProductDecoyCount: Int,
        primeProductDecoyDirective: GeneratedPairsPrimeProductDecoyDirective,
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

        if (selectedPairs.size == profile.size.pairCount) {
            return if (
                constraints.isComplete(pairs = selectedPairs) &&
                primeProductDecoyDirective.isSatisfiedBy(pairCount = primeProductDecoyCount)
            ) {
                GeneratedPairsSearchOutcome.Found(selectedPairs)
            } else {
                GeneratedPairsSearchOutcome.NoCandidate
            }
        }

        val orderedCandidatePairs = if (
            primeProductDecoyDirective.shouldPreferPrimeProductDecoy(
                currentPairCount = primeProductDecoyCount
            )
        ) {
            candidatePairs.sortedByDescending { pair ->
                if (pair.isPrimeProductDecoy()) 1 else 0
            }
        } else {
            candidatePairs
        }

        orderedCandidatePairs.forEach { candidatePair ->
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

            when (
                val outcome = chooseValuePairs(
                    candidatePairs = candidatePairs,
                    selectedPairs = selectedPairs + candidatePair,
                    valueOccurrences = valueOccurrences.with(candidatePair),
                    usedResults = usedResults + candidatePair.resultValues,
                    productAnchorCount = productAnchorCount +
                        constraints.productAnchorIncrement(pair = candidatePair),
                    primeProductDecoyCount = updatedPrimeProductDecoyCount,
                    primeProductDecoyDirective = primeProductDecoyDirective,
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

private fun Map<Int, Int>.with(pair: GeneratedPairsValuePair): Map<Int, Int> {
    val updatedOccurrences = toMutableMap()

    pair.requiredOccurrencesByValue().forEach { (value, occurrenceCount) ->
        updatedOccurrences[value] = updatedOccurrences.getOrDefault(value, 0) + occurrenceCount
    }

    return updatedOccurrences
}
