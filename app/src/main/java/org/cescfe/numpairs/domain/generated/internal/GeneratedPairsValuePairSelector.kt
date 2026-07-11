package org.cescfe.numpairs.domain.generated.internal

import kotlin.random.Random
import org.cescfe.numpairs.domain.generated.GeneratedPuzzleProfile

internal class GeneratedPairsValuePairSelector(
    private val profile: GeneratedPuzzleProfile,
    private val random: Random,
    private val hardRules: GeneratedValuePairRuleSet
) {
    constructor(profile: GeneratedPuzzleProfile, random: Random) : this(
        profile = profile,
        random = random,
        hardRules = GeneratedPuzzleHardRuleSet.from(profile = profile).valuePairs
    )

    fun selectValuePairs(variationPlan: GeneratedPairsVariationPlan): List<GeneratedPairsValuePair>? {
        val candidatePairs = candidateValuePairsForProfile().shuffled(random)
        val plannedPairs = chooseValuePairs(
            candidatePairs = candidatePairs,
            primeProductDecoyDirective = variationPlan.primeProductDecoyDirective
        )

        if (plannedPairs != null ||
            variationPlan.primeProductDecoyDirective == GeneratedPairsPrimeProductDecoyDirective.Unrestricted
        ) {
            return plannedPairs
        }

        return chooseValuePairs(
            candidatePairs = candidatePairs,
            primeProductDecoyDirective = GeneratedPairsPrimeProductDecoyDirective.Unrestricted
        )
    }

    private fun chooseValuePairs(
        candidatePairs: List<GeneratedPairsValuePair>,
        primeProductDecoyDirective: GeneratedPairsPrimeProductDecoyDirective
    ): List<GeneratedPairsValuePair>? = chooseValuePairs(
        candidatePairs = candidatePairs,
        selectedPairs = emptyList(),
        valueOccurrences = emptyMap(),
        usedResults = emptySet(),
        productAnchorCount = 0,
        primeProductDecoyCount = 0,
        primeProductDecoyDirective = primeProductDecoyDirective
    )

    private fun chooseValuePairs(
        candidatePairs: List<GeneratedPairsValuePair>,
        selectedPairs: List<GeneratedPairsValuePair>,
        valueOccurrences: Map<Int, Int>,
        usedResults: Set<Int>,
        productAnchorCount: Int,
        primeProductDecoyCount: Int,
        primeProductDecoyDirective: GeneratedPairsPrimeProductDecoyDirective
    ): List<GeneratedPairsValuePair>? {
        if (!hardRules.canStillSatisfyProductAnchorMix(
                productAnchorCount = productAnchorCount,
                remainingPairSlots = profile.size.pairCount - selectedPairs.size
            )
        ) {
            return null
        }

        if (!primeProductDecoyDirective.canStillBeSatisfied(
                currentPairCount = primeProductDecoyCount,
                remainingPairSlots = profile.size.pairCount - selectedPairs.size
            )
        ) {
            return null
        }

        if (selectedPairs.size == profile.size.pairCount) {
            return selectedPairs.takeIf { pairs ->
                hardRules.isComplete(pairs = pairs) &&
                    primeProductDecoyDirective.isSatisfiedBy(pairCount = primeProductDecoyCount)
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

        return orderedCandidatePairs.firstNotNullOfOrNull { candidatePair ->
            if (
                !hardRules.canBeAdded(
                    pair = candidatePair,
                    valueOccurrences = valueOccurrences,
                    usedResults = usedResults
                )
            ) {
                return@firstNotNullOfOrNull null
            }

            val updatedPrimeProductDecoyCount =
                primeProductDecoyCount + candidatePair.primeProductDecoyIncrement()
            if (!primeProductDecoyDirective.allows(pairCount = updatedPrimeProductDecoyCount)) {
                return@firstNotNullOfOrNull null
            }

            chooseValuePairs(
                candidatePairs = candidatePairs,
                selectedPairs = selectedPairs + candidatePair,
                valueOccurrences = valueOccurrences.with(candidatePair),
                usedResults = usedResults + candidatePair.resultValues,
                productAnchorCount = productAnchorCount + hardRules.productAnchorIncrement(pair = candidatePair),
                primeProductDecoyCount = updatedPrimeProductDecoyCount,
                primeProductDecoyDirective = primeProductDecoyDirective
            )
        }
    }

    private fun candidateValuePairsForProfile(): List<GeneratedPairsValuePair> = buildList {
        profile.stripValuePolicy.valueRange.forEach { firstValue ->
            (firstValue..profile.stripValuePolicy.valueRange.last).forEach { secondValue ->
                val candidatePair = GeneratedPairsValuePair(
                    firstValue = firstValue,
                    secondValue = secondValue
                )

                if (
                    hardRules.canBeAdded(
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
