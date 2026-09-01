package org.cescfe.numpairs.feature.daily

import java.time.DayOfWeek
import java.time.LocalDate
import org.cescfe.numpairs.domain.daily.DailyCandidateIndex
import org.cescfe.numpairs.domain.daily.DailyRecipeContract
import org.cescfe.numpairs.domain.daily.DailyRecipeContracts
import org.cescfe.numpairs.domain.daily.DailyRecipeProfileSelection
import org.cescfe.numpairs.domain.daily.DailyRecipeVersion
import org.cescfe.numpairs.domain.daily.Fnv1a32DailyCandidateSeedSchedule
import org.cescfe.numpairs.feature.generated.GeneratedChallengeCatalog
import org.cescfe.numpairs.feature.generated.GeneratedModes
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertSame
import org.junit.Assert.assertThrows
import org.junit.Test

class DailyRecipeTest {
    @Test
    fun catalog_keeps_the_legacy_four_pairs_low_recipe_resolvable_for_every_weekday() {
        val recipe = DailyRecipes.catalog.resolve(DailyRecipeVersion("daily-4-pairs-low-v1"))

        assertSame(DailyRecipes.FOUR_PAIRS_LOW_V1, recipe)
        assertSame(DailyRecipeContracts.FOUR_PAIRS_LOW_V1, recipe.contract)
        DayOfWeek.entries.forEach { dayOfWeek ->
            val identity = recipe.identityFor(dateFor(dayOfWeek))
            assertSame(GeneratedModes.FOUR_PAIRS_LOW, recipe.challengeFor(identity))
            assertSame(GeneratedModes.FOUR_PAIRS_LOW.profile, recipe.contract.profileFor(identity))
        }
        assertEquals(listOf(0, 1, 2, 3), recipe.candidateIndices.map(DailyCandidateIndex::value))
    }

    @Test
    fun weekly_recipe_maps_every_weekday_to_its_exact_generated_challenge() {
        val recipe = DailyRecipes.catalog.resolve(DailyRecipeVersion("daily-weekly-schedule-v2"))
        val expectedChallenges = mapOf(
            DayOfWeek.MONDAY to GeneratedModes.THREE_PAIRS_LOW,
            DayOfWeek.TUESDAY to GeneratedModes.FOUR_PAIRS_LOW,
            DayOfWeek.WEDNESDAY to GeneratedModes.THREE_PAIRS_MEDIUM,
            DayOfWeek.THURSDAY to GeneratedModes.FOUR_PAIRS_MEDIUM,
            DayOfWeek.FRIDAY to GeneratedModes.EIGHT_PAIRS_MEDIUM,
            DayOfWeek.SATURDAY to GeneratedModes.THREE_PAIRS_MEDIUM,
            DayOfWeek.SUNDAY to GeneratedModes.FOUR_PAIRS_LOW
        )

        assertSame(DailyRecipes.WEEKLY_SCHEDULE_V2, recipe)
        assertSame(DailyRecipeContracts.WEEKLY_SCHEDULE_V2, recipe.contract)
        expectedChallenges.forEach { (dayOfWeek, expectedChallenge) ->
            val identity = recipe.identityFor(dateFor(dayOfWeek))
            assertSame(expectedChallenge, recipe.challengeFor(identity))
            assertSame(expectedChallenge.profile, recipe.contract.profileFor(identity))
            assertSame(expectedChallenge, DailyRecipes.catalog.challengeFor(identity))
        }
    }

    @Test
    fun catalog_exposes_typed_absence_and_rejects_unknown_recipe_versions() {
        val unknownVersion = DailyRecipeVersion("unknown-daily-recipe")

        assertNull(DailyRecipes.catalog.resolveOrNull(unknownVersion))
        assertThrows(IllegalArgumentException::class.java) {
            DailyRecipes.catalog.resolve(unknownVersion)
        }
    }

    @Test
    fun recipe_derives_identity_and_seed_only_for_its_configured_version_and_candidates() {
        val recipe = DailyRecipes.FOUR_PAIRS_LOW_V1
        val identity = recipe.identityFor(LocalDate.of(2028, 2, 29))

        assertEquals(recipe.version, identity.recipeVersion)
        assertEquals(626215115, recipe.seedFor(identity, DailyCandidateIndex(0)))
        assertThrows(IllegalArgumentException::class.java) {
            recipe.seedFor(
                identity = identity.copy(recipeVersion = DailyRecipeVersion("different-version")),
                candidateIndex = DailyCandidateIndex(0)
            )
        }
        assertThrows(IllegalArgumentException::class.java) {
            recipe.seedFor(identity = identity, candidateIndex = DailyCandidateIndex(4))
        }
    }

    @Test
    fun weekly_recipe_keeps_challenge_identity_and_seed_stable_for_the_same_date_and_candidate() {
        val recipe = DailyRecipes.WEEKLY_SCHEDULE_V2
        val firstIdentity = recipe.identityFor(LocalDate.of(2026, 9, 5))
        val secondIdentity = recipe.identityFor(LocalDate.of(2026, 9, 5))

        assertEquals(firstIdentity, secondIdentity)
        assertSame(recipe.challengeFor(firstIdentity), recipe.challengeFor(secondIdentity))
        assertEquals(-1_126_657_553, recipe.seedFor(firstIdentity, DailyCandidateIndex(0)))
        assertEquals(
            recipe.seedFor(firstIdentity, DailyCandidateIndex(0)),
            recipe.seedFor(secondIdentity, DailyCandidateIndex(0))
        )
    }

    @Test
    fun recipe_and_catalog_reject_invalid_candidate_and_challenge_configuration() {
        assertThrows(IllegalArgumentException::class.java) {
            DailyRecipe(
                contract = DailyRecipeContract(
                    version = DailyRecipeVersion("daily-empty"),
                    profileSelection = DailyRecipeProfileSelection.Fixed(
                        GeneratedModes.FOUR_PAIRS_LOW.profile
                    ),
                    candidateIndices = emptyList(),
                    seedSchedule = Fnv1a32DailyCandidateSeedSchedule
                ),
                challenges = listOf(GeneratedModes.FOUR_PAIRS_LOW)
            )
        }
        assertThrows(IllegalArgumentException::class.java) {
            DailyRecipe(
                contract = DailyRecipeContract(
                    version = DailyRecipeVersion("daily-non-contiguous"),
                    profileSelection = DailyRecipeProfileSelection.Fixed(
                        GeneratedModes.FOUR_PAIRS_LOW.profile
                    ),
                    candidateIndices = listOf(DailyCandidateIndex(0), DailyCandidateIndex(2)),
                    seedSchedule = Fnv1a32DailyCandidateSeedSchedule
                ),
                challenges = listOf(GeneratedModes.FOUR_PAIRS_LOW)
            )
        }
        assertThrows(IllegalArgumentException::class.java) {
            DailyRecipe(
                contract = DailyRecipeContracts.FOUR_PAIRS_LOW_V1,
                challenges = listOf(
                    GeneratedModes.FOUR_PAIRS_LOW,
                    GeneratedModes.THREE_PAIRS_LOW
                )
            )
        }
        assertThrows(IllegalArgumentException::class.java) {
            DailyRecipeCatalog(
                recipes = listOf(DailyRecipes.FOUR_PAIRS_LOW_V1),
                generatedChallengeCatalog = GeneratedChallengeCatalog(
                    configurations = listOf(GeneratedModes.THREE_PAIRS)
                )
            )
        }
    }
}

private fun dateFor(dayOfWeek: DayOfWeek): LocalDate = LocalDate.of(2026, 8, 31).plusDays(
    (dayOfWeek.value - DayOfWeek.MONDAY.value).toLong()
)
