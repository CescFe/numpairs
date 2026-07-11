package org.cescfe.numpairs.domain.generated.internal

import org.cescfe.numpairs.domain.generated.GeneratedPuzzleProfile
import org.cescfe.numpairs.domain.generated.resolveEntryIds
import org.cescfe.numpairs.domain.puzzle.assignment.StripEntryId

internal const val GENERATED_PAIRS_PROBABILITY_PERCENT_UPPER_BOUND = 100

internal fun GeneratedPuzzleProfile.requiredKnownEntryIds(): Set<StripEntryId> =
    initialStripMaskPolicy.requiredAnchors.resolveEntryIds(
        stripEntryCount = size.stripEntryCount
    ).mapTo(mutableSetOf(), ::StripEntryId)

internal fun Set<StripEntryId>.maxGeneratedPairsConsecutiveHiddenEntries(totalEntryCount: Int): Int {
    var currentHiddenCount = 0
    var maxHiddenCount = 0

    repeat(totalEntryCount) { entryId ->
        if (StripEntryId(entryId) in this) {
            currentHiddenCount = 0
        } else {
            currentHiddenCount++
            maxHiddenCount = maxOf(maxHiddenCount, currentHiddenCount)
        }
    }

    return maxHiddenCount
}

internal fun <T> List<T>.combinations(size: Int): List<Set<T>> {
    if (size == 0) {
        return listOf(emptySet())
    }
    if (size > this.size) {
        return emptyList()
    }

    val combinations = mutableListOf<Set<T>>()
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
