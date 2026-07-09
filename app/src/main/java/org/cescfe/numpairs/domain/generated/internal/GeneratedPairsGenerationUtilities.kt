package org.cescfe.numpairs.domain.generated.internal

import org.cescfe.numpairs.domain.generated.GeneratedPuzzleProfile
import org.cescfe.numpairs.domain.generated.RequiredKnownStripAnchor

internal const val GENERATED_PAIRS_PROBABILITY_PERCENT_UPPER_BOUND = 100

internal fun GeneratedPuzzleProfile.requiredKnownEntryIds(): Set<Int> =
    initialStripMaskPolicy.requiredAnchors.map { anchor ->
        when (anchor) {
            RequiredKnownStripAnchor.HIGHEST_STRIP_ENTRY -> size.stripEntryCount - 1
        }
    }.toSet()

internal fun Set<Int>.maxGeneratedPairsConsecutiveHiddenEntries(totalEntryCount: Int): Int {
    var currentHiddenCount = 0
    var maxHiddenCount = 0

    repeat(totalEntryCount) { entryId ->
        if (entryId in this) {
            currentHiddenCount = 0
        } else {
            currentHiddenCount++
            maxHiddenCount = maxOf(maxHiddenCount, currentHiddenCount)
        }
    }

    return maxHiddenCount
}

internal fun List<Int>.combinations(size: Int): List<Set<Int>> {
    if (size == 0) {
        return listOf(emptySet())
    }
    if (size > this.size) {
        return emptyList()
    }

    val combinations = mutableListOf<Set<Int>>()
    val lastStartIndex = this.size - size

    for (index in 0..lastStartIndex) {
        val firstValue = this[index]
        val remainingValues = drop(index + 1)

        remainingValues.combinations(size = size - 1).forEach { remainingCombination ->
            combinations += remainingCombination + firstValue
        }
    }

    return combinations
}
