package org.cescfe.numpairs.domain.generated.internal

import kotlin.random.Random
import org.cescfe.numpairs.domain.generated.GeneratedPuzzleProfile
import org.cescfe.numpairs.domain.generated.ResultConstraints
import org.cescfe.numpairs.domain.generated.StripValuePolicy

internal class GeneratedPairsValuePairSelector(
    private val profile: GeneratedPuzzleProfile,
    private val random: Random
) {
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
        if (!canStillSatisfyProductAnchorMix(
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
                pairs.hasExpectedProductAnchorMix() &&
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
                !candidatePair.canBeAddedTo(
                    valueOccurrences = valueOccurrences,
                    usedResults = usedResults,
                    resultConstraints = profile.resultConstraints,
                    stripValuePolicy = profile.stripValuePolicy
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
                productAnchorCount = productAnchorCount + candidatePair.productAnchorIncrement(
                    productAnchorMix = profile.resultConstraints.productAnchorMix
                ),
                primeProductDecoyCount = updatedPrimeProductDecoyCount,
                primeProductDecoyDirective = primeProductDecoyDirective
            )
        }
    }

    private fun canStillSatisfyProductAnchorMix(productAnchorCount: Int, remainingPairSlots: Int): Boolean {
        val productAnchorMix = profile.resultConstraints.productAnchorMix ?: return true

        return productAnchorCount <= productAnchorMix.countRange.last &&
            productAnchorCount + remainingPairSlots >= productAnchorMix.countRange.first
    }

    private fun List<GeneratedPairsValuePair>.hasExpectedProductAnchorMix(): Boolean {
        val productAnchorMix = profile.resultConstraints.productAnchorMix ?: return true
        val productAnchorCount = count { pair ->
            pair.product > productAnchorMix.productResultGreaterThan
        }

        return productAnchorCount in productAnchorMix.countRange
    }

    private fun candidateValuePairsForProfile(): List<GeneratedPairsValuePair> = buildList {
        profile.stripValuePolicy.valueRange.forEach { firstValue ->
            (firstValue..profile.stripValuePolicy.valueRange.last).forEach { secondValue ->
                val candidatePair = GeneratedPairsValuePair(
                    firstValue = firstValue,
                    secondValue = secondValue
                )

                if (
                    candidatePair.requiredOccurrencesByValue()
                        .all { (_, occurrenceCount) ->
                            occurrenceCount <= profile.stripValuePolicy.maxOccurrencesPerValue
                        } &&
                    candidatePair.canBeAddedTo(
                        valueOccurrences = emptyMap(),
                        usedResults = emptySet(),
                        resultConstraints = profile.resultConstraints,
                        stripValuePolicy = profile.stripValuePolicy
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

private fun GeneratedPairsValuePair.canBeAddedTo(
    valueOccurrences: Map<Int, Int>,
    usedResults: Set<Int>,
    resultConstraints: ResultConstraints,
    stripValuePolicy: StripValuePolicy
): Boolean {
    if (product > resultConstraints.maxMultiplicationResult) {
        return false
    }

    if (!resultConstraints.allowsDuplicateBoardResults &&
        (resultValues.size != 2 || resultValues.any { result -> result in usedResults })
    ) {
        return false
    }

    return requiredOccurrencesByValue().all { (value, newOccurrenceCount) ->
        valueOccurrences.getOrDefault(value, 0) + newOccurrenceCount <= stripValuePolicy.maxOccurrencesPerValue
    }
}

private fun Map<Int, Int>.with(pair: GeneratedPairsValuePair): Map<Int, Int> {
    val updatedOccurrences = toMutableMap()

    pair.requiredOccurrencesByValue().forEach { (value, occurrenceCount) ->
        updatedOccurrences[value] = updatedOccurrences.getOrDefault(value, 0) + occurrenceCount
    }

    return updatedOccurrences
}
