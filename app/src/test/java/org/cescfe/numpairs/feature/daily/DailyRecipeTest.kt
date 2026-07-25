package org.cescfe.numpairs.feature.daily

import java.time.LocalDate
import org.cescfe.numpairs.domain.daily.DailyCandidateIndex
import org.cescfe.numpairs.domain.daily.DailyRecipeContract
import org.cescfe.numpairs.domain.daily.DailyRecipeContracts
import org.cescfe.numpairs.domain.daily.DailyRecipeVersion
import org.cescfe.numpairs.domain.daily.Fnv1a32DailyCandidateSeedSchedule
import org.cescfe.numpairs.domain.generated.profile.DifficultyTier
import org.cescfe.numpairs.feature.generated.GeneratedChallengeCatalog
import org.cescfe.numpairs.feature.generated.GeneratedModes
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertSame
import org.junit.Assert.assertThrows
import org.junit.Test

class DailyRecipeTest {
    @Test
    fun v10_catalog_resolves_the_exact_four_pairs_low_recipe() {
        val recipe = DailyRecipes.catalog.resolve(DailyRecipeVersion("daily-4-pairs-low-v1"))

        assertSame(DailyRecipes.FOUR_PAIRS_LOW_V1, recipe)
        assertSame(DailyRecipeContracts.FOUR_PAIRS_LOW_V1, recipe.contract)
        assertSame(GeneratedModes.FOUR_PAIRS_LOW, recipe.challenge)
        assertEquals(GeneratedModes.FOUR_PAIRS.id, recipe.challenge.modeId)
        assertEquals(DifficultyTier.LOW, recipe.challenge.difficulty)
        assertEquals(GeneratedModes.FOUR_PAIRS_LOW.profile, recipe.challenge.profile)
        assertEquals(listOf(0, 1, 2, 3), recipe.candidateIndices.map(DailyCandidateIndex::value))
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
    fun recipe_and_catalog_reject_invalid_candidate_and_challenge_configuration() {
        assertThrows(IllegalArgumentException::class.java) {
            DailyRecipe(
                contract = DailyRecipeContract(
                    version = DailyRecipeVersion("daily-empty"),
                    profile = GeneratedModes.FOUR_PAIRS_LOW.profile,
                    candidateIndices = emptyList(),
                    seedSchedule = Fnv1a32DailyCandidateSeedSchedule
                ),
                challenge = GeneratedModes.FOUR_PAIRS_LOW
            )
        }
        assertThrows(IllegalArgumentException::class.java) {
            DailyRecipe(
                contract = DailyRecipeContract(
                    version = DailyRecipeVersion("daily-non-contiguous"),
                    profile = GeneratedModes.FOUR_PAIRS_LOW.profile,
                    candidateIndices = listOf(DailyCandidateIndex(0), DailyCandidateIndex(2)),
                    seedSchedule = Fnv1a32DailyCandidateSeedSchedule
                ),
                challenge = GeneratedModes.FOUR_PAIRS_LOW
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
