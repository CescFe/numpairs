package org.cescfe.numpairs.feature.generated

import org.cescfe.numpairs.domain.generated.profile.DifficultyTier
import org.cescfe.numpairs.domain.generated.profile.GeneratedPuzzleProfile
import org.cescfe.numpairs.domain.generated.profile.GeneratedPuzzleProfileDefinition
import org.cescfe.numpairs.domain.generated.profile.GeneratedPuzzleProfileId
import org.cescfe.numpairs.domain.generated.profile.GeneratedPuzzleProfiles
import org.cescfe.numpairs.domain.generated.profile.GeneratedPuzzleSize
import org.cescfe.numpairs.domain.generated.profile.GenerationPolicy
import org.cescfe.numpairs.domain.generated.profile.InitialStripMaskPolicy
import org.cescfe.numpairs.domain.generated.profile.RequiredKnownStripAnchor
import org.cescfe.numpairs.domain.generated.profile.ResultConstraints
import org.cescfe.numpairs.domain.generated.profile.StripKnownEntryDistributionPolicy
import org.cescfe.numpairs.domain.generated.profile.StripValuePolicy
import org.cescfe.numpairs.domain.generated.profile.getOrThrow
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotEquals
import org.junit.Assert.assertSame
import org.junit.Assert.assertThrows
import org.junit.Test

class GeneratedChallengeCatalogTest {
    @Test
    fun difficulty_tiers_are_closed_and_independent_from_size() {
        assertEquals(
            listOf(DifficultyTier.LOW, DifficultyTier.MEDIUM, DifficultyTier.HARD),
            DifficultyTier.entries
        )
        assertEquals(DifficultyTier.LOW, GeneratedPuzzleProfiles.FOUR_PAIRS_LOW.difficulty)
        assertEquals(DifficultyTier.MEDIUM, GeneratedPuzzleProfiles.FOUR_PAIRS_MEDIUM.difficulty)
        assertEquals(DifficultyTier.MEDIUM, GeneratedPuzzleProfiles.EIGHT_PAIRS_MEDIUM.difficulty)
        assertEquals(DifficultyTier.HARD, GeneratedPuzzleProfiles.EIGHT_PAIRS_HARD.difficulty)
    }

    @Test
    fun configured_catalog_registers_low_and_medium_for_four_pairs() {
        assertEquals("four-pairs", GeneratedModes.FOUR_PAIRS.id.value)
        assertEquals("eight-pairs", GeneratedModes.EIGHT_PAIRS.id.value)
        assertEquals("4-pairs-low", GeneratedModes.FOUR_PAIRS_LOW.profile.id.value)
        assertEquals("4-pairs-medium", GeneratedModes.FOUR_PAIRS_MEDIUM.profile.id.value)
        assertEquals("8-pairs-medium", GeneratedModes.EIGHT_PAIRS_MEDIUM.profile.id.value)
        assertEquals("8-pairs-hard", GeneratedModes.EIGHT_PAIRS_HARD.profile.id.value)
        assertEquals(
            listOf(
                GeneratedModes.FOUR_PAIRS_LOW,
                GeneratedModes.FOUR_PAIRS_MEDIUM,
                GeneratedModes.EIGHT_PAIRS_MEDIUM,
                GeneratedModes.EIGHT_PAIRS_HARD
            ),
            GeneratedModes.catalog.allChallenges
        )
        assertSame(
            GeneratedModes.FOUR_PAIRS_MEDIUM,
            GeneratedModes.catalog.resolveChallenge(
                modeId = GeneratedModes.FOUR_PAIRS.id,
                difficulty = DifficultyTier.MEDIUM
            )
        )
        assertSame(
            GeneratedModes.FOUR_PAIRS_LOW,
            GeneratedModes.catalog.resolveChallenge(
                modeId = GeneratedModes.FOUR_PAIRS.id,
                difficulty = DifficultyTier.LOW
            )
        )
        assertSame(
            GeneratedModes.EIGHT_PAIRS_MEDIUM,
            GeneratedModes.catalog.resolveChallenge(
                modeId = GeneratedModes.EIGHT_PAIRS.id,
                profileId = GeneratedPuzzleProfiles.EIGHT_PAIRS_MEDIUM.id
            )
        )
        assertSame(
            GeneratedModes.EIGHT_PAIRS_HARD,
            GeneratedModes.catalog.resolveChallenge(
                modeId = GeneratedModes.EIGHT_PAIRS.id,
                difficulty = DifficultyTier.HARD
            )
        )
    }

    @Test
    fun one_mode_can_expose_multiple_difficulties_with_the_same_size() {
        val modeId = GeneratedModeId("four-pairs-test")
        val low = challenge(
            id = "four-pairs-test-low",
            modeId = modeId,
            profile = fourPairsProfile(id = "four-pairs-test-low-profile", difficulty = DifficultyTier.LOW)
        )
        val medium = challenge(
            id = "four-pairs-test-medium",
            modeId = modeId,
            profile = fourPairsProfile(id = "four-pairs-test-medium-profile", difficulty = DifficultyTier.MEDIUM)
        )
        val mode = GeneratedModeConfiguration(
            id = modeId,
            size = low.profile.size,
            challenges = listOf(low, medium)
        )
        val catalog = GeneratedChallengeCatalog(listOf(mode))

        assertSame(low, catalog.resolveChallenge(modeId = modeId, difficulty = DifficultyTier.LOW))
        assertSame(medium, catalog.resolveChallenge(modeId = modeId, difficulty = DifficultyTier.MEDIUM))
        assertNotEquals(low.generatedPuzzleViewModelKey(), medium.generatedPuzzleViewModelKey())
    }

    @Test
    fun challenge_and_mode_reject_profile_difficulty_size_and_ownership_mismatches() {
        val modeId = GeneratedModeId("mismatch-mode")
        assertThrows(IllegalArgumentException::class.java) {
            GeneratedChallenge(
                id = GeneratedChallengeId("difficulty-mismatch"),
                modeId = modeId,
                difficulty = DifficultyTier.HARD,
                profile = GeneratedPuzzleProfiles.FOUR_PAIRS_LOW
            )
        }

        val validChallenge = GeneratedChallenge(
            id = GeneratedChallengeId("size-mismatch"),
            modeId = modeId,
            difficulty = DifficultyTier.LOW,
            profile = GeneratedPuzzleProfiles.FOUR_PAIRS_LOW
        )
        assertThrows(IllegalArgumentException::class.java) {
            GeneratedModeConfiguration(
                id = modeId,
                size = GeneratedPuzzleProfiles.EIGHT_PAIRS_MEDIUM.size,
                challenges = listOf(validChallenge)
            )
        }
        assertThrows(IllegalArgumentException::class.java) {
            GeneratedModeConfiguration(
                id = GeneratedModeId("different-owner"),
                size = GeneratedPuzzleProfiles.FOUR_PAIRS_LOW.size,
                challenges = listOf(validChallenge)
            )
        }
    }

    @Test
    fun catalog_rejects_duplicate_challenge_difficulty_and_profile_identities() {
        val firstModeId = GeneratedModeId("first-mode")
        val secondModeId = GeneratedModeId("second-mode")
        val firstProfile = fourPairsProfile(id = "first-profile", difficulty = DifficultyTier.LOW)
        val secondProfile = fourPairsProfile(id = "second-profile", difficulty = DifficultyTier.MEDIUM)
        val duplicateId = GeneratedChallengeId("duplicate-challenge")
        val firstChallenge = GeneratedChallenge(
            id = duplicateId,
            modeId = firstModeId,
            difficulty = firstProfile.difficulty,
            profile = firstProfile
        )
        val secondChallenge = GeneratedChallenge(
            id = duplicateId,
            modeId = secondModeId,
            difficulty = secondProfile.difficulty,
            profile = secondProfile
        )
        val firstMode = GeneratedModeConfiguration(
            id = firstModeId,
            size = firstProfile.size,
            challenges = listOf(firstChallenge)
        )
        val secondMode = GeneratedModeConfiguration(
            id = secondModeId,
            size = secondProfile.size,
            challenges = listOf(secondChallenge)
        )

        assertThrows(IllegalArgumentException::class.java) {
            GeneratedChallengeCatalog(listOf(firstMode, secondMode))
        }
        assertThrows(IllegalArgumentException::class.java) {
            GeneratedModeConfiguration(
                id = firstModeId,
                size = firstProfile.size,
                challenges = listOf(
                    firstChallenge,
                    firstChallenge.copy(id = GeneratedChallengeId("same-difficulty"))
                )
            )
        }
        assertThrows(IllegalArgumentException::class.java) {
            GeneratedChallengeCatalog(
                listOf(
                    firstMode,
                    GeneratedModeConfiguration(
                        id = secondModeId,
                        size = firstProfile.size,
                        challenges = listOf(
                            firstChallenge.copy(
                                id = GeneratedChallengeId("reused-profile"),
                                modeId = secondModeId
                            )
                        )
                    )
                )
            )
        }
    }

    @Test
    fun unsupported_mode_difficulty_profile_and_challenge_resolutions_are_rejected() {
        assertThrows(IllegalArgumentException::class.java) {
            GeneratedModes.catalog.resolveChallenge(GeneratedChallengeId("unknown"))
        }
        assertThrows(IllegalArgumentException::class.java) {
            GeneratedModes.catalog.resolveChallenge(
                modeId = GeneratedModes.FOUR_PAIRS.id,
                difficulty = DifficultyTier.HARD
            )
        }
        assertThrows(IllegalArgumentException::class.java) {
            GeneratedModes.catalog.resolveChallenge(
                modeId = GeneratedModes.FOUR_PAIRS.id,
                profileId = GeneratedPuzzleProfiles.EIGHT_PAIRS_MEDIUM.id
            )
        }
        assertEquals(
            null,
            GeneratedModes.catalog.resolveChallengeOrNull(
                modeId = GeneratedModes.FOUR_PAIRS.id.value,
                profileId = GeneratedPuzzleProfiles.EIGHT_PAIRS_MEDIUM.id.value
            )
        )
    }
}

private fun challenge(id: String, modeId: GeneratedModeId, profile: GeneratedPuzzleProfile): GeneratedChallenge =
    GeneratedChallenge(
        id = GeneratedChallengeId(id),
        modeId = modeId,
        difficulty = profile.difficulty,
        profile = profile
    )

private fun fourPairsProfile(id: String, difficulty: DifficultyTier): GeneratedPuzzleProfile =
    GeneratedPuzzleProfile.create(
        definition = GeneratedPuzzleProfileDefinition(
            id = GeneratedPuzzleProfileId(id),
            difficulty = difficulty,
            size = GeneratedPuzzleSize(pairCount = 4),
            stripValuePolicy = StripValuePolicy(
                valueRange = 2..20,
                maxOccurrencesPerValue = 1
            ),
            resultConstraints = ResultConstraints(
                maxMultiplicationResult = 150,
                allowsDuplicateBoardResults = false
            ),
            initialStripMaskPolicy = InitialStripMaskPolicy(
                knownEntryCountRange = 3..3,
                requiredAnchors = setOf(RequiredKnownStripAnchor.HIGHEST_STRIP_ENTRY),
                distributionPolicy =
                StripKnownEntryDistributionPolicy.SpreadAcrossStripAndPairsWhenPossible,
                maxConsecutiveHiddenEntries = 2
            ),
            generationPolicy = GenerationPolicy(isBoardTileShufflingEnabled = true)
        )
    ).getOrThrow()
