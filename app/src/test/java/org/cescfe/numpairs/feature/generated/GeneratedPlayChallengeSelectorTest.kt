package org.cescfe.numpairs.feature.generated

import org.cescfe.numpairs.domain.generated.profile.DifficultyTier
import org.junit.Assert.assertEquals
import org.junit.Assert.assertSame
import org.junit.Assert.assertThrows
import org.junit.Test

class GeneratedPlayChallengeSelectorTest {
    @Test
    fun configured_play_options_have_stable_identities_and_supported_difficulties() {
        assertEquals("quick", GeneratedPlayOptions.QUICK.id.value)
        assertEquals(
            listOf(DifficultyTier.LOW, DifficultyTier.MEDIUM),
            GeneratedPlayOptions.QUICK.difficulties
        )
        assertEquals("classic", GeneratedPlayOptions.CLASSIC.id.value)
        assertEquals(
            listOf(DifficultyTier.MEDIUM, DifficultyTier.HARD),
            GeneratedPlayOptions.CLASSIC.difficulties
        )
        assertEquals(
            listOf(GeneratedPlayOptions.QUICK, GeneratedPlayOptions.CLASSIC),
            GeneratedPlayOptions.ALL
        )
    }

    @Test
    fun quick_low_resolves_exactly_thirty_five_three_pairs_and_sixty_five_four_pairs_buckets() {
        assertQuickDistribution(
            difficulty = DifficultyTier.LOW,
            threePairsChallenge = GeneratedModes.THREE_PAIRS_LOW,
            fourPairsChallenge = GeneratedModes.FOUR_PAIRS_LOW
        )
    }

    @Test
    fun quick_medium_resolves_exactly_thirty_five_three_pairs_and_sixty_five_four_pairs_buckets() {
        assertQuickDistribution(
            difficulty = DifficultyTier.MEDIUM,
            threePairsChallenge = GeneratedModes.THREE_PAIRS_MEDIUM,
            fourPairsChallenge = GeneratedModes.FOUR_PAIRS_MEDIUM
        )
    }

    @Test
    fun classic_resolves_directly_without_consuming_a_quick_bucket() {
        val selector = GeneratedPlayChallengeSelector(
            quickBucketSource = GeneratedQuickSelectionBucketSource {
                error("Classic selection must not request a Quick bucket.")
            }
        )

        assertSame(
            GeneratedModes.EIGHT_PAIRS_MEDIUM,
            selector.select(GeneratedPlayOptions.CLASSIC.id, DifficultyTier.MEDIUM)
        )
        assertSame(
            GeneratedModes.EIGHT_PAIRS_HARD,
            selector.select(GeneratedPlayOptions.CLASSIC.id, DifficultyTier.HARD)
        )
    }

    @Test
    fun configured_challenges_map_back_to_their_player_facing_option() {
        val selector = GeneratedPlayChallengeSelector()

        listOf(
            GeneratedModes.THREE_PAIRS_LOW,
            GeneratedModes.THREE_PAIRS_MEDIUM,
            GeneratedModes.FOUR_PAIRS_LOW,
            GeneratedModes.FOUR_PAIRS_MEDIUM
        ).forEach { challenge ->
            assertSame(GeneratedPlayOptions.QUICK, selector.optionFor(challenge))
        }
        listOf(
            GeneratedModes.EIGHT_PAIRS_MEDIUM,
            GeneratedModes.EIGHT_PAIRS_HARD
        ).forEach { challenge ->
            assertSame(GeneratedPlayOptions.CLASSIC, selector.optionFor(challenge))
        }
    }

    @Test
    fun unknown_options_unsupported_difficulties_and_invalid_buckets_are_rejected() {
        val selector = GeneratedPlayChallengeSelector()

        assertThrows(IllegalArgumentException::class.java) {
            selector.select(GeneratedPlayOptionId("unknown"), DifficultyTier.LOW)
        }
        assertThrows(IllegalArgumentException::class.java) {
            selector.select(GeneratedPlayOptions.QUICK.id, DifficultyTier.HARD)
        }
        assertThrows(IllegalArgumentException::class.java) {
            selector.select(GeneratedPlayOptions.CLASSIC.id, DifficultyTier.LOW)
        }
        assertThrows(IllegalArgumentException::class.java) {
            GeneratedPlayChallengeSelector(
                quickBucketSource = GeneratedQuickSelectionBucketSource { 100 }
            ).select(GeneratedPlayOptions.QUICK.id, DifficultyTier.LOW)
        }
    }

    private fun assertQuickDistribution(
        difficulty: DifficultyTier,
        threePairsChallenge: GeneratedChallenge,
        fourPairsChallenge: GeneratedChallenge
    ) {
        var bucket = 0
        val selector = GeneratedPlayChallengeSelector(
            quickBucketSource = GeneratedQuickSelectionBucketSource { bucket }
        )
        val selectedChallenges = (0 until 100).map { currentBucket ->
            bucket = currentBucket
            selector.select(GeneratedPlayOptions.QUICK.id, difficulty)
        }

        assertEquals(35, selectedChallenges.count { challenge -> challenge == threePairsChallenge })
        assertEquals(65, selectedChallenges.count { challenge -> challenge == fourPairsChallenge })
        assertEquals(
            List(35) { threePairsChallenge } + List(65) { fourPairsChallenge },
            selectedChallenges
        )
    }
}
