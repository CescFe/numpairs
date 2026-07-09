package org.cescfe.numpairs.domain.generated

import kotlin.random.Random

internal class GeneratedPairsValuePairSelector(
    private val profile: GeneratedPuzzleProfile,
    private val random: Random
) {
    fun selectValuePairs(generationTargets: GeneratedPairsGenerationTargets): List<GeneratedPairsValuePair>? =
        chooseValuePairs(
            candidatePairs = candidateValuePairsForProfile().shuffled(random),
            selectedPairs = emptyList(),
            valueOccurrences = emptyMap(),
            usedResults = emptySet(),
            productAnchorCount = 0,
            primeProductDecoyCount = 0,
            generationTargets = generationTargets
        )

    private fun chooseValuePairs(
        candidatePairs: List<GeneratedPairsValuePair>,
        selectedPairs: List<GeneratedPairsValuePair>,
        valueOccurrences: Map<Int, Int>,
        usedResults: Set<Int>,
        productAnchorCount: Int,
        primeProductDecoyCount: Int,
        generationTargets: GeneratedPairsGenerationTargets
    ): List<GeneratedPairsValuePair>? {
        if (!canStillSatisfyProductAnchorMix(
                productAnchorCount = productAnchorCount,
                remainingPairSlots = profile.size.pairCount - selectedPairs.size
            )
        ) {
            return null
        }

        if (selectedPairs.size == profile.size.pairCount) {
            return selectedPairs.takeIf { pairs ->
                pairs.hasExpectedProductAnchorMix()
            }
        }

        val orderedCandidatePairs = if (generationTargets.shouldPreferPrimeProductDecoy(primeProductDecoyCount)) {
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

            chooseValuePairs(
                candidatePairs = candidatePairs,
                selectedPairs = selectedPairs + candidatePair,
                valueOccurrences = valueOccurrences.with(candidatePair),
                usedResults = usedResults + candidatePair.resultValues,
                productAnchorCount = productAnchorCount + candidatePair.productAnchorIncrement(
                    productAnchorMix = profile.resultConstraints.productAnchorMix
                ),
                primeProductDecoyCount = primeProductDecoyCount + candidatePair.primeProductDecoyIncrement(),
                generationTargets = generationTargets
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
