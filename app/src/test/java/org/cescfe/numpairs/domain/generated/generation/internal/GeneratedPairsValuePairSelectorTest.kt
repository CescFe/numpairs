package org.cescfe.numpairs.domain.generated.generation.internal

import kotlin.random.Random
import org.cescfe.numpairs.domain.generated.profile.DifficultyTier
import org.cescfe.numpairs.domain.generated.profile.GeneratedPuzzleProfile
import org.cescfe.numpairs.domain.generated.profile.GeneratedPuzzleProfileDefinition
import org.cescfe.numpairs.domain.generated.profile.GeneratedPuzzleProfileId
import org.cescfe.numpairs.domain.generated.profile.GeneratedPuzzleProfiles
import org.cescfe.numpairs.domain.generated.profile.GeneratedPuzzleSize
import org.cescfe.numpairs.domain.generated.profile.GenerationPolicy
import org.cescfe.numpairs.domain.generated.profile.InitialStripMaskPolicy
import org.cescfe.numpairs.domain.generated.profile.ResultConstraints
import org.cescfe.numpairs.domain.generated.profile.StripKnownEntryDistributionPolicy
import org.cescfe.numpairs.domain.generated.profile.StripValuePolicy
import org.cescfe.numpairs.domain.generated.profile.getOrThrow
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

    @Test
    fun repetition_plans_select_exactly_one_or_zero_repeated_value_groups() {
        val selector = GeneratedPairsValuePairSelector(
            profile = GeneratedPuzzleProfiles.FOUR_PAIRS_MEDIUM,
            random = Random(42)
        )
        val includedPairs = requireNotNull(
            selector.selectValuePairs(
                variationPlan = variationPlan(
                    repeatedValueGroupDirective = GeneratedPairsRepeatedValueGroupDirective.Include(groupCount = 1)
                )
            )
        )
        val excludedPairs = requireNotNull(
            selector.selectValuePairs(
                variationPlan = variationPlan(
                    repeatedValueGroupDirective = GeneratedPairsRepeatedValueGroupDirective.Exclude
                )
            )
        )

        assertEquals(1, includedPairs.repeatedValueGroupCount())
        assertEquals(0, excludedPairs.repeatedValueGroupCount())
    }

    @Test
    fun repetition_range_plan_selects_a_hard_valid_group_count() {
        val selectedPairs = requireNotNull(
            GeneratedPairsValuePairSelector(
                profile = GeneratedPuzzleProfiles.EIGHT_PAIRS_HARD,
                random = Random(42)
            ).selectValuePairs(
                variationPlan = variationPlan(
                    repeatedValueGroupDirective = GeneratedPairsRepeatedValueGroupDirective.IncludeRange(
                        groupCountRange = 1..2
                    )
                )
            )
        )

        assertEquals(2, selectedPairs.repeatedValueGroupCount())
    }
}

private fun variationPlan(
    primeProductDecoyDirective: GeneratedPairsPrimeProductDecoyDirective =
        GeneratedPairsPrimeProductDecoyDirective.Unrestricted,
    repeatedValueGroupDirective: GeneratedPairsRepeatedValueGroupDirective =
        GeneratedPairsRepeatedValueGroupDirective.Unrestricted
): GeneratedPairsVariationPlan = GeneratedPairsVariationPlan(
    primeProductDecoyDirective = primeProductDecoyDirective,
    repeatedValueGroupDirective = repeatedValueGroupDirective,
    stripEntryVisibilityDirectives = emptyMap()
)

private fun List<GeneratedPairsValuePair>.repeatedValueGroupCount(): Int =
    flatMap { pair -> listOf(pair.firstValue, pair.secondValue) }
        .groupingBy { value -> value }
        .eachCount()
        .values
        .count { occurrenceCount -> occurrenceCount > 1 }

private fun singlePairProfile(id: String, valueRange: IntRange, maxMultiplicationResult: Int): GeneratedPuzzleProfile {
    val size = GeneratedPuzzleSize(pairCount = 1)

    return GeneratedPuzzleProfile.create(
        definition = GeneratedPuzzleProfileDefinition(
            id = GeneratedPuzzleProfileId(id),
            difficulty = DifficultyTier.LOW,
            size = size,
            stripValuePolicy = StripValuePolicy(
                valueRange = valueRange,
                maxOccurrencesPerValue = 1
            ),
            resultConstraints = ResultConstraints(
                maxMultiplicationResult = maxMultiplicationResult,
                allowsDuplicateBoardResults = false
            ),
            initialStripMaskPolicy = InitialStripMaskPolicy(
                knownEntryCountRange = 1..1,
                requiredAnchors = emptySet(),
                distributionPolicy = StripKnownEntryDistributionPolicy.Unrestricted,
                maxConsecutiveHiddenEntries = 1
            ),
            generationPolicy = GenerationPolicy(
                isBoardTileShufflingEnabled = false
            )
        )
    ).getOrThrow()
}
