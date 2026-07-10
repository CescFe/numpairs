package org.cescfe.numpairs.domain.generated.internal

import kotlin.random.Random
import org.cescfe.numpairs.domain.generated.GeneratedPuzzleProfile

internal class GeneratedStripMaskSelector(private val profile: GeneratedPuzzleProfile, private val random: Random) {
    private val hardRule = GeneratedPuzzleHardRuleSet.from(profile = profile).stripMask

    fun selectKnownEntryIds(
        pairs: List<GeneratedPairsEntryPair>,
        variationPlan: GeneratedPairsVariationPlan
    ): GeneratedPairsStripMaskSelection? {
        val plannedCandidateGroups = knownEntryIdCandidateGroups(
            pairs = pairs,
            visibilityDirectives = variationPlan.stripEntryVisibilityDirectives
        )
        plannedCandidateGroups.selectKnownEntryIds()?.let { knownEntryIds ->
            return GeneratedPairsStripMaskSelection(
                knownEntryIds = knownEntryIds,
                variationPlanOutcome = GeneratedPairsVariationPlanOutcome.HONORED
            )
        }

        val fallbackKnownEntryIds = knownEntryIdCandidateGroups(
            pairs = pairs,
            visibilityDirectives = emptyMap()
        ).selectKnownEntryIds() ?: return null

        return GeneratedPairsStripMaskSelection(
            knownEntryIds = fallbackKnownEntryIds,
            variationPlanOutcome = GeneratedPairsVariationPlanOutcome.FALLBACK
        )
    }

    private fun knownEntryIdCandidateGroups(
        pairs: List<GeneratedPairsEntryPair>,
        visibilityDirectives: Map<Int, GeneratedPairsStripEntryVisibilityDirective>
    ): List<List<Set<Int>>> = profile.initialStripMaskPolicy.knownEntryCountRange.mapNotNull { knownEntryCount ->
        hardValidKnownEntryIdCandidates(
            knownEntryCount = knownEntryCount,
            pairs = pairs
        ).filter { knownEntryIds ->
            knownEntryIds.matches(visibilityDirectives = visibilityDirectives)
        }.takeIf { candidates -> candidates.isNotEmpty() }
    }

    private fun hardValidKnownEntryIdCandidates(
        knownEntryCount: Int,
        pairs: List<GeneratedPairsEntryPair>
    ): List<Set<Int>> = knownEntryIdCandidatesFor(knownEntryCount = knownEntryCount)
        .filter { knownEntryIds ->
            hardRule.isSatisfied(
                knownEntryIds = knownEntryIds,
                hiddenEntryCount = profile.size.stripEntryCount - knownEntryIds.size,
                pairKeys = pairs.mapTo(mutableSetOf(), GeneratedPairsEntryPair::key)
            )
        }

    private fun Set<Int>.matches(visibilityDirectives: Map<Int, GeneratedPairsStripEntryVisibilityDirective>): Boolean =
        visibilityDirectives.all { (entryId, directive) ->
            when (directive) {
                GeneratedPairsStripEntryVisibilityDirective.KNOWN -> entryId in this
                GeneratedPairsStripEntryVisibilityDirective.HIDDEN -> entryId !in this
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

    private fun List<List<Set<Int>>>.selectKnownEntryIds(): Set<Int>? = randomOrNull()?.randomOrNull()

    private fun <T> List<T>.randomOrNull(): T? = takeIf { it.isNotEmpty() }?.let { values ->
        values[random.nextInt(values.size)]
    }
}
