package org.cescfe.numpairs.domain.generated.generation.internal

import kotlin.random.Random
import org.cescfe.numpairs.domain.generated.profile.GeneratedPuzzleProfile
import org.cescfe.numpairs.domain.generated.profile.PrimeProductDecoyPairPattern
import org.cescfe.numpairs.domain.generated.profile.ProbabilityPercent
import org.cescfe.numpairs.domain.puzzle.assignment.StripEntryId

internal class GeneratedPairsVariationPlanSelector(
    private val profile: GeneratedPuzzleProfile,
    private val nextProbabilityRoll: () -> Int
) {
    constructor(
        profile: GeneratedPuzzleProfile,
        random: Random
    ) : this(
        profile = profile,
        nextProbabilityRoll = {
            random.nextInt(GENERATED_PAIRS_PROBABILITY_PERCENT_UPPER_BOUND)
        }
    )

    fun select(): GeneratedPairsVariationPlan = GeneratedPairsVariationPlan(
        primeProductDecoyDirective = selectPrimeProductDecoyDirective(),
        stripEntryVisibilityDirectives = selectStripEntryVisibilityDirectives()
    )

    private fun selectPrimeProductDecoyDirective(): GeneratedPairsPrimeProductDecoyDirective {
        val primeProductDecoyTarget = profile.varietyPolicy.primeProductDecoyTarget
            ?: return GeneratedPairsPrimeProductDecoyDirective.Unrestricted

        return when (primeProductDecoyTarget.pairPattern) {
            PrimeProductDecoyPairPattern.ONE_AND_PRIME -> {
                if (shouldApply(probability = primeProductDecoyTarget.targetPuzzlePercent)) {
                    GeneratedPairsPrimeProductDecoyDirective.Include(
                        pairCount = primeProductDecoyTarget.targetPairCount
                    )
                } else {
                    GeneratedPairsPrimeProductDecoyDirective.Exclude
                }
            }
        }
    }

    private fun selectStripEntryVisibilityDirectives(): Map<StripEntryId, GeneratedPairsStripEntryVisibilityDirective> =
        profile.varietyPolicy.highValueMaskTargets.associate { target ->
            val entryId = StripEntryId(profile.size.stripEntryCount - target.rankFromHighest)
            val directive = if (shouldApply(probability = target.targetHiddenProbability)) {
                GeneratedPairsStripEntryVisibilityDirective.HIDDEN
            } else {
                GeneratedPairsStripEntryVisibilityDirective.KNOWN
            }

            entryId to directive
        }

    private fun shouldApply(probability: ProbabilityPercent): Boolean {
        val probabilityRoll = nextProbabilityRoll()

        require(probabilityRoll in 0 until GENERATED_PAIRS_PROBABILITY_PERCENT_UPPER_BOUND) {
            "Generated pairs probability rolls must be between 0 and 99."
        }

        return probabilityRoll < probability.value
    }
}
