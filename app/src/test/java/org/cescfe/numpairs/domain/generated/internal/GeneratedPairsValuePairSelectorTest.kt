package org.cescfe.numpairs.domain.generated.internal

import kotlin.random.Random
import org.cescfe.numpairs.domain.generated.GeneratedPuzzleProfile
import org.cescfe.numpairs.domain.generated.GeneratedPuzzleProfileId
import org.cescfe.numpairs.domain.generated.GeneratedPuzzleProfiles
import org.cescfe.numpairs.domain.generated.GeneratedPuzzleSize
import org.cescfe.numpairs.domain.generated.GenerationPolicy
import org.cescfe.numpairs.domain.generated.InitialStripMaskPolicy
import org.cescfe.numpairs.domain.generated.ResultConstraints
import org.cescfe.numpairs.domain.generated.StripKnownEntryDistributionPolicy
import org.cescfe.numpairs.domain.generated.StripValuePolicy
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Test

class GeneratedPairsValuePairSelectorTest {

    @Test
    fun inclusion_plan_selects_exactly_the_requested_prime_product_decoy_count() {
        val selectedPairs = requireNotNull(
            GeneratedPairsValuePairSelector(
                profile = GeneratedPuzzleProfiles.EIGHT_PAIRS_MEDIUM,
                random = Random(42)
            ).selectValuePairs(
                variationPlan = variationPlan(
                    primeProductDecoyDirective = GeneratedPairsPrimeProductDecoyDirective.Include(pairCount = 1)
                )
            )
        )

        assertEquals(1, selectedPairs.count(GeneratedPairsValuePair::isPrimeProductDecoy))
    }

    @Test
    fun exclusion_plan_prevents_naturally_occurring_prime_product_decoys() {
        val selectedPairs = requireNotNull(
            GeneratedPairsValuePairSelector(
                profile = GeneratedPuzzleProfiles.EIGHT_PAIRS_MEDIUM,
                random = Random(42)
            ).selectValuePairs(
                variationPlan = variationPlan(
                    primeProductDecoyDirective = GeneratedPairsPrimeProductDecoyDirective.Exclude
                )
            )
        )

        assertEquals(0, selectedPairs.count(GeneratedPairsValuePair::isPrimeProductDecoy))
    }

    @Test
    fun infeasible_exclusion_falls_back_to_the_only_hard_valid_decoy() {
        val selectedPairs = GeneratedPairsValuePairSelector(
            profile = singlePairProfile(
                id = "only-decoy",
                valueRange = 1..2,
                maxMultiplicationResult = 2
            ),
            random = Random(42)
        ).selectValuePairs(
            variationPlan = variationPlan(
                primeProductDecoyDirective = GeneratedPairsPrimeProductDecoyDirective.Exclude
            )
        )

        assertNotNull(selectedPairs)
        assertEquals(1, selectedPairs.orEmpty().count(GeneratedPairsValuePair::isPrimeProductDecoy))
    }

    @Test
    fun infeasible_inclusion_falls_back_to_the_only_hard_valid_non_decoy() {
        val selectedPairs = GeneratedPairsValuePairSelector(
            profile = singlePairProfile(
                id = "only-non-decoy",
                valueRange = 2..3,
                maxMultiplicationResult = 6
            ),
            random = Random(42)
        ).selectValuePairs(
            variationPlan = variationPlan(
                primeProductDecoyDirective = GeneratedPairsPrimeProductDecoyDirective.Include(pairCount = 1)
            )
        )

        assertNotNull(selectedPairs)
        assertEquals(0, selectedPairs.orEmpty().count(GeneratedPairsValuePair::isPrimeProductDecoy))
    }
}

private fun variationPlan(
    primeProductDecoyDirective: GeneratedPairsPrimeProductDecoyDirective
): GeneratedPairsVariationPlan = GeneratedPairsVariationPlan(
    primeProductDecoyDirective = primeProductDecoyDirective,
    stripEntryVisibilityDirectives = emptyMap()
)

private fun singlePairProfile(id: String, valueRange: IntRange, maxMultiplicationResult: Int): GeneratedPuzzleProfile {
    val size = GeneratedPuzzleSize(pairCount = 1)

    return GeneratedPuzzleProfile(
        id = GeneratedPuzzleProfileId(id),
        size = size,
        stripValuePolicy = StripValuePolicy(
            valueRange = valueRange,
            maxOccurrencesPerValue = 1
        ),
        resultConstraints = ResultConstraints(
            pairCount = size.pairCount,
            maxMultiplicationResult = maxMultiplicationResult,
            allowsDuplicateBoardResults = false
        ),
        initialStripMaskPolicy = InitialStripMaskPolicy(
            stripEntryCount = size.stripEntryCount,
            knownEntryCountRange = 1..1,
            requiredAnchors = emptySet(),
            distributionPolicy = StripKnownEntryDistributionPolicy.UNRESTRICTED,
            maxConsecutiveHiddenEntries = 1
        ),
        generationPolicy = GenerationPolicy(
            isBoardTileShufflingEnabled = false,
            isBoundedGenerationExpected = true,
            isDeterministicGenerationExpected = true
        )
    )
}
