package org.cescfe.numpairs.domain.generated

import org.junit.Assert.assertEquals
import org.junit.Assert.assertThrows
import org.junit.Test

class GeneratedPuzzleProfileTest {

    @Test
    fun generated_puzzle_size_rejects_non_positive_pair_counts() {
        assertThrows(IllegalArgumentException::class.java) {
            GeneratedPuzzleSize(pairCount = 0)
        }
    }

    @Test
    fun generated_puzzle_size_derives_strip_entries_and_board_tiles_from_pair_count() {
        val size = GeneratedPuzzleSize(pairCount = 6)

        assertEquals(12, size.stripEntryCount)
        assertEquals(12, size.boardTileCount)
    }

    @Test
    fun generated_puzzle_profile_rejects_result_constraints_for_a_different_pair_count() {
        val profile = validGeneratedPuzzleProfile()

        assertThrows(IllegalArgumentException::class.java) {
            profile.withResultConstraintsPairCount(pairCount = profile.size.pairCount + 1)
        }
    }

    @Test
    fun generated_puzzle_profile_rejects_initial_strip_mask_policy_for_a_different_strip_size() {
        val profile = validGeneratedPuzzleProfile()

        assertThrows(IllegalArgumentException::class.java) {
            profile.withInitialStripEntryCount(stripEntryCount = profile.size.stripEntryCount + 1)
        }
    }

    @Test
    fun strip_value_policy_rejects_invalid_construction() {
        assertThrows(IllegalArgumentException::class.java) {
            StripValuePolicy(
                valueRange = IntRange.EMPTY,
                maxOccurrencesPerValue = 1
            )
        }
        assertThrows(IllegalArgumentException::class.java) {
            StripValuePolicy(
                valueRange = 0..2,
                maxOccurrencesPerValue = 1
            )
        }
        assertThrows(IllegalArgumentException::class.java) {
            StripValuePolicy(
                valueRange = 1..2,
                maxOccurrencesPerValue = 0
            )
        }
    }

    @Test
    fun result_constraints_reject_invalid_construction() {
        assertThrows(IllegalArgumentException::class.java) {
            ResultConstraints(
                pairCount = 4,
                maxMultiplicationResult = 0,
                allowsDuplicateBoardResults = false
            )
        }
        assertThrows(IllegalArgumentException::class.java) {
            ResultConstraints(
                pairCount = 4,
                maxMultiplicationResult = 150,
                allowsDuplicateBoardResults = false,
                productAnchorMix = ProductAnchorMix(
                    productResultGreaterThan = 198,
                    countRange = 0..5
                )
            )
        }
        assertThrows(IllegalArgumentException::class.java) {
            ResultConstraints(
                pairCount = 4,
                maxMultiplicationResult = 150,
                allowsDuplicateBoardResults = false,
                productAnchorMix = ProductAnchorMix(
                    productResultGreaterThan = 198,
                    countRange = -1..2
                )
            )
        }
    }

    @Test
    fun probability_percent_rejects_values_outside_zero_to_one_hundred() {
        assertThrows(IllegalArgumentException::class.java) {
            ProbabilityPercent(-1)
        }
        assertThrows(IllegalArgumentException::class.java) {
            ProbabilityPercent(101)
        }
    }

    @Test
    fun initial_strip_mask_policy_derives_hidden_entry_count_range_from_known_entry_count_range() {
        val maskPolicy = InitialStripMaskPolicy(
            stripEntryCount = 10,
            knownEntryCountRange = 3..4,
            requiredAnchors = emptySet(),
            distributionPolicy = StripKnownEntryDistributionPolicy.UNRESTRICTED,
            maxConsecutiveHiddenEntries = 2
        )

        assertEquals(6..7, maskPolicy.hiddenEntryCountRange)
    }

    @Test
    fun initial_strip_mask_policy_rejects_invalid_construction() {
        assertThrows(IllegalArgumentException::class.java) {
            InitialStripMaskPolicy(
                stripEntryCount = 8,
                knownEntryCountRange = 0..9,
                requiredAnchors = emptySet(),
                distributionPolicy = StripKnownEntryDistributionPolicy.UNRESTRICTED,
                maxConsecutiveHiddenEntries = 2
            )
        }
        assertThrows(IllegalArgumentException::class.java) {
            InitialStripMaskPolicy(
                stripEntryCount = 8,
                knownEntryCountRange = IntRange.EMPTY,
                requiredAnchors = emptySet(),
                distributionPolicy = StripKnownEntryDistributionPolicy.UNRESTRICTED,
                maxConsecutiveHiddenEntries = 2
            )
        }
        assertThrows(IllegalArgumentException::class.java) {
            InitialStripMaskPolicy(
                stripEntryCount = 8,
                knownEntryCountRange = 3..3,
                requiredAnchors = emptySet(),
                distributionPolicy = StripKnownEntryDistributionPolicy.UNRESTRICTED,
                maxConsecutiveHiddenEntries = 0
            )
        }
        assertThrows(IllegalArgumentException::class.java) {
            InitialStripMaskPolicy(
                stripEntryCount = 8,
                knownEntryCountRange = 3..3,
                requiredAnchors = emptySet(),
                distributionPolicy = StripKnownEntryDistributionPolicy.UNRESTRICTED,
                maxConsecutiveHiddenEntries = 2,
                highValueMaskTargets = listOf(
                    HighValueMaskTarget(
                        rankFromHighest = 9,
                        targetHiddenProbability = ProbabilityPercent(20)
                    )
                )
            )
        }
    }
}

private fun validGeneratedPuzzleProfile(): GeneratedPuzzleProfile {
    val size = GeneratedPuzzleSize(pairCount = 2)

    return GeneratedPuzzleProfile(
        id = GeneratedPuzzleProfileId("valid-test-profile"),
        size = size,
        stripValuePolicy = StripValuePolicy(
            valueRange = 2..10,
            maxOccurrencesPerValue = 1
        ),
        resultConstraints = ResultConstraints(
            pairCount = size.pairCount,
            maxMultiplicationResult = 50,
            allowsDuplicateBoardResults = false
        ),
        initialStripMaskPolicy = InitialStripMaskPolicy(
            stripEntryCount = size.stripEntryCount,
            knownEntryCountRange = 1..2,
            requiredAnchors = emptySet(),
            distributionPolicy = StripKnownEntryDistributionPolicy.UNRESTRICTED,
            maxConsecutiveHiddenEntries = 2
        ),
        generationPolicy = GenerationPolicy(
            isBoardTileShufflingEnabled = true,
            isBoundedGenerationExpected = true,
            isDeterministicGenerationExpected = true
        )
    )
}

private fun GeneratedPuzzleProfile.withResultConstraintsPairCount(pairCount: Int): GeneratedPuzzleProfile = copy(
    resultConstraints = resultConstraints.copy(pairCount = pairCount)
)

private fun GeneratedPuzzleProfile.withInitialStripEntryCount(stripEntryCount: Int): GeneratedPuzzleProfile = copy(
    initialStripMaskPolicy = initialStripMaskPolicy.copy(stripEntryCount = stripEntryCount)
)
