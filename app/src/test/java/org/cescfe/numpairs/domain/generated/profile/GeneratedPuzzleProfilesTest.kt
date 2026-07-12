package org.cescfe.numpairs.domain.generated.profile

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class GeneratedPuzzleProfilesTest {
    @Test
    fun four_pairs_low_profile_matches_documented_rules() {
        val profile = GeneratedPuzzleProfiles.FOUR_PAIRS_LOW

        assertEquals(GeneratedPuzzleProfileId("4-pairs-low"), profile.id)
        assertEquals(4, profile.size.pairCount)
        assertEquals(8, profile.size.stripEntryCount)
        assertEquals(8, profile.size.boardTileCount)

        assertEquals(2..20, profile.stripValuePolicy.valueRange)
        assertEquals(1, profile.stripValuePolicy.maxOccurrencesPerValue)
        assertFalse(profile.stripValuePolicy.allowsOne)

        assertEquals(150, profile.resultConstraints.maxMultiplicationResult)
        assertFalse(profile.resultConstraints.allowsDuplicateBoardResults)
        assertNull(profile.resultConstraints.productAnchorMix)

        assertEquals(3..3, profile.initialStripMaskPolicy.knownEntryCountRange)
        assertEquals(5..5, profile.hiddenEntryCountRange)
        assertEquals(
            setOf(RequiredKnownStripAnchor.HIGHEST_STRIP_ENTRY),
            profile.initialStripMaskPolicy.requiredAnchors
        )
        assertEquals(
            StripKnownEntryDistributionPolicy.SPREAD_ACROSS_STRIP_AND_PAIRS_WHEN_POSSIBLE,
            profile.initialStripMaskPolicy.distributionPolicy
        )
        assertEquals(2, profile.initialStripMaskPolicy.maxConsecutiveHiddenEntries)

        assertTrue(profile.generationPolicy.isBoardTileShufflingEnabled)
        assertNull(profile.varietyPolicy.primeProductDecoyTarget)
    }

    @Test
    fun eight_pairs_medium_profile_matches_documented_rules() {
        val profile = GeneratedPuzzleProfiles.EIGHT_PAIRS_MEDIUM

        assertEquals(GeneratedPuzzleProfileId("8-pairs-medium"), profile.id)
        assertEquals(8, profile.size.pairCount)
        assertEquals(16, profile.size.stripEntryCount)
        assertEquals(16, profile.size.boardTileCount)

        assertEquals(1..99, profile.stripValuePolicy.valueRange)
        assertEquals(2, profile.stripValuePolicy.maxOccurrencesPerValue)
        assertTrue(profile.stripValuePolicy.allowsOne)

        assertEquals(1000, profile.resultConstraints.maxMultiplicationResult)
        assertFalse(profile.resultConstraints.allowsDuplicateBoardResults)

        val productAnchorMix = requireNotNull(profile.resultConstraints.productAnchorMix)
        assertEquals(198, productAnchorMix.productResultGreaterThan)
        assertEquals(2..4, productAnchorMix.countRange)

        assertEquals(6..7, profile.initialStripMaskPolicy.knownEntryCountRange)
        assertEquals(9..10, profile.hiddenEntryCountRange)
        assertEquals(emptySet<RequiredKnownStripAnchor>(), profile.initialStripMaskPolicy.requiredAnchors)
        assertEquals(
            StripKnownEntryDistributionPolicy.UNRESTRICTED,
            profile.initialStripMaskPolicy.distributionPolicy
        )
        assertEquals(4, profile.initialStripMaskPolicy.maxConsecutiveHiddenEntries)
        assertEquals(
            listOf(
                HighValueMaskTarget(
                    rankFromHighest = 1,
                    targetHiddenProbability = ProbabilityPercent(20)
                ),
                HighValueMaskTarget(
                    rankFromHighest = 2,
                    targetHiddenProbability = ProbabilityPercent(40)
                ),
                HighValueMaskTarget(
                    rankFromHighest = 3,
                    targetHiddenProbability = ProbabilityPercent(40)
                )
            ),
            profile.varietyPolicy.highValueMaskTargets
        )

        assertTrue(profile.generationPolicy.isBoardTileShufflingEnabled)

        val primeProductDecoyTarget = requireNotNull(profile.varietyPolicy.primeProductDecoyTarget)
        assertEquals(ProbabilityPercent(30), primeProductDecoyTarget.targetPuzzlePercent)
        assertEquals(1, primeProductDecoyTarget.targetPairCount)
        assertEquals(PrimeProductDecoyPairPattern.ONE_AND_PRIME, primeProductDecoyTarget.pairPattern)
    }
}
