package org.cescfe.numpairs.domain.generated

import kotlin.random.Random

internal class GeneratedPairsGenerationTargetSelector(
    private val profile: GeneratedPuzzleProfile,
    private val random: Random
) {
    fun select(): GeneratedPairsGenerationTargets {
        val primeProductDecoyTarget = profile.generationPolicy.primeProductDecoyTarget
        val primeProductDecoyPairTargetCount = if (primeProductDecoyTarget == null) {
            0
        } else if (
            primeProductDecoyTarget.pairPattern == PrimeProductDecoyPairPattern.ONE_AND_PRIME &&
            random.nextInt(GENERATED_PAIRS_PROBABILITY_PERCENT_UPPER_BOUND) <
            primeProductDecoyTarget.targetPuzzlePercent.value
        ) {
            primeProductDecoyTarget.targetPairCount
        } else {
            0
        }

        return GeneratedPairsGenerationTargets(
            primeProductDecoyPairTargetCount = primeProductDecoyPairTargetCount
        )
    }
}
