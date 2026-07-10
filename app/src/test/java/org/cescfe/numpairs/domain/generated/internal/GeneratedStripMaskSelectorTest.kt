package org.cescfe.numpairs.domain.generated.internal

import kotlin.random.Random
import org.cescfe.numpairs.domain.generated.GeneratedPuzzleProfiles
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test

class GeneratedStripMaskSelectorTest {

    @Test
    fun mixed_visibility_plan_is_honored_when_a_valid_mask_exists() {
        val profile = GeneratedPuzzleProfiles.EIGHT_PAIRS_MEDIUM
        val highestEntryId = profile.size.stripEntryCount - 1
        val secondHighestEntryId = profile.size.stripEntryCount - 2
        val thirdHighestEntryId = profile.size.stripEntryCount - 3
        val selection = GeneratedStripMaskSelector(
            profile = profile,
            random = Random(42)
        ).selectKnownEntryIds(
            pairs = entryPairs(pairCount = profile.size.pairCount),
            variationPlan = variationPlan(
                visibilityDirectives = mapOf(
                    highestEntryId to GeneratedPairsStripEntryVisibilityDirective.HIDDEN,
                    secondHighestEntryId to GeneratedPairsStripEntryVisibilityDirective.KNOWN,
                    thirdHighestEntryId to GeneratedPairsStripEntryVisibilityDirective.HIDDEN
                )
            )
        )

        assertNotNull(selection)
        assertEquals(GeneratedPairsVariationPlanOutcome.HONORED, selection?.variationPlanOutcome)
        assertFalse(highestEntryId in selection?.knownEntryIds.orEmpty())
        assertTrue(secondHighestEntryId in selection?.knownEntryIds.orEmpty())
        assertFalse(thirdHighestEntryId in selection?.knownEntryIds.orEmpty())
    }

    @Test
    fun selector_checks_every_known_count_before_falling_back() {
        val profile = GeneratedPuzzleProfiles.EIGHT_PAIRS_MEDIUM
        val requiredKnownEntryIds = setOf(0, 2, 4, 6, 8, 10, 12)
        val selection = GeneratedStripMaskSelector(
            profile = profile,
            random = Random(42)
        ).selectKnownEntryIds(
            pairs = entryPairs(pairCount = profile.size.pairCount),
            variationPlan = variationPlan(
                visibilityDirectives = requiredKnownEntryIds.associateWith {
                    GeneratedPairsStripEntryVisibilityDirective.KNOWN
                }
            )
        )

        assertEquals(GeneratedPairsVariationPlanOutcome.HONORED, selection?.variationPlanOutcome)
        assertEquals(7, selection?.knownEntryIds?.size)
        assertTrue(selection?.knownEntryIds.orEmpty().containsAll(requiredKnownEntryIds))
    }

    @Test
    fun visibility_conflict_falls_back_without_overriding_a_hard_anchor() {
        val profile = GeneratedPuzzleProfiles.FOUR_PAIRS_LOW
        val highestEntryId = profile.size.stripEntryCount - 1
        val selection = GeneratedStripMaskSelector(
            profile = profile,
            random = Random(42)
        ).selectKnownEntryIds(
            pairs = entryPairs(pairCount = profile.size.pairCount),
            variationPlan = variationPlan(
                visibilityDirectives = mapOf(
                    highestEntryId to GeneratedPairsStripEntryVisibilityDirective.HIDDEN
                )
            )
        )

        assertNotNull(selection)
        assertEquals(GeneratedPairsVariationPlanOutcome.FALLBACK, selection?.variationPlanOutcome)
        assertTrue(highestEntryId in selection?.knownEntryIds.orEmpty())
    }
}

private fun variationPlan(
    visibilityDirectives: Map<Int, GeneratedPairsStripEntryVisibilityDirective>
): GeneratedPairsVariationPlan = GeneratedPairsVariationPlan(
    primeProductDecoyDirective = GeneratedPairsPrimeProductDecoyDirective.Unrestricted,
    stripEntryVisibilityDirectives = visibilityDirectives
)

private fun entryPairs(pairCount: Int): List<GeneratedPairsEntryPair> = List(pairCount) { pairIndex ->
    val firstEntryId = pairIndex * 2
    val secondEntryId = firstEntryId + 1

    GeneratedPairsEntryPair(
        firstEntry = GeneratedPairsStripEntry(
            id = firstEntryId,
            value = firstEntryId + 1
        ),
        secondEntry = GeneratedPairsStripEntry(
            id = secondEntryId,
            value = secondEntryId + 1
        )
    )
}
