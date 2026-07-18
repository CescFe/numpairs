package org.cescfe.numpairs.domain.generated.generation.internal

import kotlin.random.Random
import org.cescfe.numpairs.domain.generated.profile.GeneratedPuzzleProfile
import org.cescfe.numpairs.domain.puzzle.assignment.StripEntryId

internal class GeneratedStripMaskSelector(
    private val profile: GeneratedPuzzleProfile,
    private val random: Random,
    private val constraint: GeneratedStripMaskConstraint
) {
    constructor(profile: GeneratedPuzzleProfile, random: Random) : this(
        profile = profile,
        random = random,
        constraint = GeneratedPuzzleConstraintSet.from(profile = profile).stripMask
    )

    fun selectKnownEntryIds(
        pairs: List<GeneratedPairsEntryPair>,
        variationPlan: GeneratedPairsVariationPlan
    ): GeneratedPairsStripMaskSelection? = when (
        val outcome = selectKnownEntryIdsWithControl(
            pairs = pairs,
            variationPlan = variationPlan,
            searchControl = null
        )
    ) {
        is GeneratedPairsSearchOutcome.Found -> outcome.value
        GeneratedPairsSearchOutcome.NoCandidate,
        GeneratedPairsSearchOutcome.BudgetExhausted,
        GeneratedPairsSearchOutcome.Cancelled -> null
    }

    fun selectKnownEntryIds(
        pairs: List<GeneratedPairsEntryPair>,
        variationPlan: GeneratedPairsVariationPlan,
        searchControl: GeneratedPairsSearchControl
    ): GeneratedPairsSearchOutcome<GeneratedPairsStripMaskSelection> = selectKnownEntryIdsWithControl(
        pairs = pairs,
        variationPlan = variationPlan,
        searchControl = searchControl
    )

    private fun selectKnownEntryIdsWithControl(
        pairs: List<GeneratedPairsEntryPair>,
        variationPlan: GeneratedPairsVariationPlan,
        searchControl: GeneratedPairsSearchControl?
    ): GeneratedPairsSearchOutcome<GeneratedPairsStripMaskSelection> {
        when (
            val plannedSelection = selectKnownEntryIds(
                pairs = pairs,
                visibilityDirectives = variationPlan.stripEntryVisibilityDirectives,
                searchControl = searchControl
            )
        ) {
            is GeneratedPairsSearchOutcome.Found -> {
                return GeneratedPairsSearchOutcome.Found(
                    GeneratedPairsStripMaskSelection(
                        knownEntryIds = plannedSelection.value,
                        variationPlanOutcome = GeneratedPairsVariationPlanOutcome.HONORED
                    )
                )
            }

            GeneratedPairsSearchOutcome.BudgetExhausted -> return GeneratedPairsSearchOutcome.BudgetExhausted
            GeneratedPairsSearchOutcome.Cancelled -> return GeneratedPairsSearchOutcome.Cancelled
            GeneratedPairsSearchOutcome.NoCandidate -> Unit
        }

        return when (
            val fallbackSelection = selectKnownEntryIds(
                pairs = pairs,
                visibilityDirectives = emptyMap(),
                searchControl = searchControl
            )
        ) {
            is GeneratedPairsSearchOutcome.Found -> GeneratedPairsSearchOutcome.Found(
                GeneratedPairsStripMaskSelection(
                    knownEntryIds = fallbackSelection.value,
                    variationPlanOutcome = GeneratedPairsVariationPlanOutcome.FALLBACK
                )
            )

            GeneratedPairsSearchOutcome.NoCandidate -> GeneratedPairsSearchOutcome.NoCandidate
            GeneratedPairsSearchOutcome.BudgetExhausted -> GeneratedPairsSearchOutcome.BudgetExhausted
            GeneratedPairsSearchOutcome.Cancelled -> GeneratedPairsSearchOutcome.Cancelled
        }
    }

    private fun selectKnownEntryIds(
        pairs: List<GeneratedPairsEntryPair>,
        visibilityDirectives: Map<StripEntryId, GeneratedPairsStripEntryVisibilityDirective>,
        searchControl: GeneratedPairsSearchControl?
    ): GeneratedPairsSearchOutcome<Set<StripEntryId>> {
        val knownEntryCounts = profile.initialStripMaskPolicy.knownEntryCountRange.toList().shuffled(random)

        knownEntryCounts.forEach { knownEntryCount ->
            when (
                val selection = selectKnownEntryIdsForCount(
                    knownEntryCount = knownEntryCount,
                    pairs = pairs,
                    visibilityDirectives = visibilityDirectives,
                    searchControl = searchControl
                )
            ) {
                is GeneratedPairsSearchOutcome.Found -> return selection
                GeneratedPairsSearchOutcome.BudgetExhausted,
                GeneratedPairsSearchOutcome.Cancelled -> return selection
                GeneratedPairsSearchOutcome.NoCandidate -> Unit
            }
        }

        return GeneratedPairsSearchOutcome.NoCandidate
    }

    private fun selectKnownEntryIdsForCount(
        knownEntryCount: Int,
        pairs: List<GeneratedPairsEntryPair>,
        visibilityDirectives: Map<StripEntryId, GeneratedPairsStripEntryVisibilityDirective>,
        searchControl: GeneratedPairsSearchControl?
    ): GeneratedPairsSearchOutcome<Set<StripEntryId>> {
        var selectedCandidate: Set<StripEntryId>? = null
        var matchingCandidateCount = 0
        val solutionPairs = pairs.mapTo(mutableSetOf(), GeneratedPairsEntryPair::solutionPair)

        for (knownEntryIds in knownEntryIdCandidatesFor(knownEntryCount = knownEntryCount)) {
            searchControl?.consumeCandidateExpansion()?.let { result ->
                if (result != GeneratedPairsSearchControlResult.Continue) {
                    return result.toSearchOutcome()
                }
            }

            if (
                !constraint.isSatisfied(
                    knownEntryIds = knownEntryIds,
                    hiddenEntryCount = profile.size.stripEntryCount - knownEntryIds.size,
                    solutionPairs = solutionPairs
                ) ||
                !knownEntryIds.matches(visibilityDirectives = visibilityDirectives)
            ) {
                continue
            }

            matchingCandidateCount++
            if (random.nextInt(matchingCandidateCount) == 0) {
                selectedCandidate = knownEntryIds
            }
        }

        return selectedCandidate?.let { candidate -> GeneratedPairsSearchOutcome.Found(candidate) }
            ?: GeneratedPairsSearchOutcome.NoCandidate
    }

    private fun Set<StripEntryId>.matches(
        visibilityDirectives: Map<StripEntryId, GeneratedPairsStripEntryVisibilityDirective>
    ): Boolean = visibilityDirectives.all { (entryId, directive) ->
        when (directive) {
            GeneratedPairsStripEntryVisibilityDirective.KNOWN -> entryId in this
            GeneratedPairsStripEntryVisibilityDirective.HIDDEN -> entryId !in this
        }
    }

    private fun knownEntryIdCandidatesFor(knownEntryCount: Int): Sequence<Set<StripEntryId>> {
        val requiredKnownEntryIds = profile.requiredKnownEntryIds()

        if (requiredKnownEntryIds.size > knownEntryCount) {
            return emptySequence()
        }

        val additionalKnownEntryCount = knownEntryCount - requiredKnownEntryIds.size
        val optionalEntryIds = (0 until profile.size.stripEntryCount).map(::StripEntryId).filterNot { entryId ->
            entryId in requiredKnownEntryIds
        }

        return optionalEntryIds
            .combinationsSequence(size = additionalKnownEntryCount)
            .map { additionalKnownEntryIds -> additionalKnownEntryIds + requiredKnownEntryIds }
    }
}
