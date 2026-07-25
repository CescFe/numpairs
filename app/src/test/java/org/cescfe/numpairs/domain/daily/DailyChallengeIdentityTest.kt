package org.cescfe.numpairs.domain.daily

import java.time.LocalDate
import org.junit.Assert.assertEquals
import org.junit.Assert.assertThrows
import org.junit.Test

class DailyChallengeIdentityTest {
    @Test
    fun identity_exposes_only_recipe_version_and_canonical_iso_local_date() {
        val identity = DailyChallengeId(
            localDate = LocalDate.of(2028, 2, 29),
            recipeVersion = DailyRecipeVersion("daily-4-pairs-low-v1")
        )

        assertEquals("2028-02-29", identity.canonicalLocalDate)
        assertEquals("daily-4-pairs-low-v1", identity.recipeVersion.value)
    }

    @Test
    fun recipe_version_and_candidate_index_reject_invalid_identity_components() {
        assertThrows(IllegalArgumentException::class.java) {
            DailyRecipeVersion(" ")
        }
        assertThrows(IllegalArgumentException::class.java) {
            DailyRecipeVersion("daily|v1")
        }
        assertThrows(IllegalArgumentException::class.java) {
            DailyCandidateIndex(-1)
        }
    }

    @Test
    fun fnv1a_seed_vectors_are_stable_across_dates_and_every_configured_index() {
        val expectedSeedsByDate = mapOf(
            LocalDate.of(2026, 1, 1) to listOf(-142727284, -125949665, -109172046, -92394427),
            LocalDate.of(2026, 12, 31) to listOf(192004267, 175226648, 225559505, 208781886),
            LocalDate.of(2028, 2, 29) to listOf(626215115, 609437496, 659770353, 642992734)
        )

        expectedSeedsByDate.forEach { (date, expectedSeeds) ->
            val identity = DailyChallengeId(
                localDate = date,
                recipeVersion = DailyRecipeVersion("daily-4-pairs-low-v1")
            )

            assertEquals(
                expectedSeeds,
                List(4) { candidateIndex ->
                    Fnv1a32DailyCandidateSeedSchedule.seedFor(
                        identity = identity,
                        candidateIndex = DailyCandidateIndex(candidateIndex)
                    )
                }
            )
        }
    }
}
