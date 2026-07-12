package org.cescfe.numpairs.domain.generated.generation.internal

import org.cescfe.numpairs.domain.generated.profile.GeneratedPuzzleProfile
import org.cescfe.numpairs.domain.generated.profile.resolveEntryIds
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

internal fun <T> List<T>.combinationsSequence(size: Int): Sequence<Set<T>> = sequence {
    if (size == 0) {
        yield(emptySet())
        return@sequence
    }
    if (size > this@combinationsSequence.size) {
        return@sequence
    }

    val lastStartIndex = this@combinationsSequence.size - size

    for (index in 0..lastStartIndex) {
        val firstValue = this@combinationsSequence[index]
        val remainingValues = this@combinationsSequence.drop(index + 1)

        remainingValues.combinationsSequence(size = size - 1).forEach { remainingCombination ->
            yield(remainingCombination + firstValue)
        }
    }
}
