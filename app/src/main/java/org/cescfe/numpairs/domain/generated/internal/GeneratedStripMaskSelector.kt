package org.cescfe.numpairs.domain.generated.internal

import kotlin.random.Random
import org.cescfe.numpairs.domain.generated.GeneratedPuzzleProfile
import org.cescfe.numpairs.domain.generated.StripKnownEntryDistributionPolicy

internal class GeneratedStripMaskSelector(private val profile: GeneratedPuzzleProfile, private val random: Random) {
    fun selectKnownEntryIds(pairs: List<GeneratedPairsEntryPair>): Set<Int>? {
        val knownEntryCount = selectKnownEntryCount()
        val preferredHiddenEntryIds = selectPreferredHiddenEntryIds()
        val preferredCandidates = knownEntryIdCandidates(
            knownEntryCount = knownEntryCount,
            pairs = pairs,
            preferredHiddenEntryIds = preferredHiddenEntryIds
        )
        val candidates = preferredCandidates.ifEmpty {
            knownEntryIdCandidates(
                knownEntryCount = knownEntryCount,
                pairs = pairs,
                preferredHiddenEntryIds = emptySet()
            )
        }

        return candidates.randomOrNull()
    }

    private fun selectKnownEntryCount(): Int {
        val knownEntryCountRange = profile.initialStripMaskPolicy.knownEntryCountRange

        return if (knownEntryCountRange.first == knownEntryCountRange.last) {
            knownEntryCountRange.first
        } else {
            random.nextInt(
                from = knownEntryCountRange.first,
                until = knownEntryCountRange.last + 1
            )
        }
    }

    private fun selectPreferredHiddenEntryIds(): Set<Int> = profile.initialStripMaskPolicy.highValueMaskTargets
        .mapNotNull { target ->
            val shouldHideEntry =
                random.nextInt(GENERATED_PAIRS_PROBABILITY_PERCENT_UPPER_BOUND) <
                    target.targetHiddenProbability.value

            if (shouldHideEntry) {
                profile.size.stripEntryCount - target.rankFromHighest
            } else {
                null
            }
        }
        .toSet()

    private fun knownEntryIdCandidates(
        knownEntryCount: Int,
        pairs: List<GeneratedPairsEntryPair>,
        preferredHiddenEntryIds: Set<Int>
    ): List<Set<Int>> {
        val pairKeyByEntryId = pairs.flatMap { pair ->
            pair.entryIds.map { entryId -> entryId to pair.key }
        }.toMap()

        val candidates = knownEntryIdCandidatesFor(knownEntryCount = knownEntryCount)
            .filter { knownEntryIds ->
                knownEntryIds.none { entryId -> entryId in preferredHiddenEntryIds }
            }
            .filter { knownEntryIds ->
                knownEntryIds.maxGeneratedPairsConsecutiveHiddenEntries(
                    totalEntryCount = profile.size.stripEntryCount
                ) <=
                    profile.initialStripMaskPolicy.maxConsecutiveHiddenEntries
            }

        return when (profile.initialStripMaskPolicy.distributionPolicy) {
            StripKnownEntryDistributionPolicy.SPREAD_ACROSS_STRIP_AND_PAIRS_WHEN_POSSIBLE -> {
                candidates.filter { knownEntryIds ->
                    knownEntryIds
                        .map { entryId -> pairKeyByEntryId.getValue(entryId) }
                        .toSet()
                        .size == knownEntryIds.size
                }
            }

            StripKnownEntryDistributionPolicy.UNRESTRICTED -> candidates
        }
    }

    private fun knownEntryIdCandidatesFor(knownEntryCount: Int): List<Set<Int>> {
        val requiredKnownEntryIds = profile.requiredKnownEntryIds()

        if (requiredKnownEntryIds.size > knownEntryCount) {
            return emptyList()
        }

        val additionalKnownEntryCount = knownEntryCount - requiredKnownEntryIds.size
        val optionalEntryIds = (0 until profile.size.stripEntryCount).filterNot { entryId ->
            entryId in requiredKnownEntryIds
        }

        return optionalEntryIds
            .combinations(size = additionalKnownEntryCount)
            .map { additionalKnownEntryIds -> additionalKnownEntryIds + requiredKnownEntryIds }
    }

    private fun <T> List<T>.randomOrNull(): T? = takeIf { it.isNotEmpty() }?.let { values ->
        values[random.nextInt(values.size)]
    }
}
