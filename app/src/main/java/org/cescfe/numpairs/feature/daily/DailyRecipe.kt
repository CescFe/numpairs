package org.cescfe.numpairs.feature.daily

import java.time.LocalDate
import org.cescfe.numpairs.domain.daily.DailyCandidateIndex
import org.cescfe.numpairs.domain.daily.DailyCandidateSeedSchedule
import org.cescfe.numpairs.domain.daily.DailyChallengeId
import org.cescfe.numpairs.domain.daily.DailyRecipeVersion
import org.cescfe.numpairs.domain.daily.Fnv1a32DailyCandidateSeedSchedule
import org.cescfe.numpairs.feature.generated.GeneratedChallenge
import org.cescfe.numpairs.feature.generated.GeneratedChallengeCatalog
import org.cescfe.numpairs.feature.generated.GeneratedModes

data class DailyRecipe(
    val version: DailyRecipeVersion,
    val challenge: GeneratedChallenge,
    val candidateIndices: List<DailyCandidateIndex>,
    private val seedSchedule: DailyCandidateSeedSchedule
) {
    init {
        require(candidateIndices.isNotEmpty()) {
            "A Daily recipe must configure at least one candidate."
        }
        require(candidateIndices == List(candidateIndices.size, ::DailyCandidateIndex)) {
            "Daily recipe candidate indexes must be contiguous and start at zero."
        }
    }

    fun identityFor(localDate: LocalDate): DailyChallengeId = DailyChallengeId(
        localDate = localDate,
        recipeVersion = version
    )

    fun seedFor(identity: DailyChallengeId, candidateIndex: DailyCandidateIndex): Int {
        require(identity.recipeVersion == version) {
            "Daily Challenge identity must use recipe ${version.value}."
        }
        require(candidateIndex in candidateIndices) {
            "Daily candidate index ${candidateIndex.value} is not configured by recipe ${version.value}."
        }
        return seedSchedule.seedFor(identity = identity, candidateIndex = candidateIndex)
    }
}

class DailyRecipeCatalog(recipes: Collection<DailyRecipe>, generatedChallengeCatalog: GeneratedChallengeCatalog) {
    val all: List<DailyRecipe> = recipes.toList()
    private val recipesByVersion: Map<DailyRecipeVersion, DailyRecipe> = all.associateBy(DailyRecipe::version)

    init {
        require(all.isNotEmpty()) {
            "At least one Daily recipe must be configured."
        }
        require(recipesByVersion.size == all.size) {
            "Daily recipe versions must be unique."
        }
        require(
            all.all { recipe ->
                generatedChallengeCatalog.resolveChallenge(recipe.challenge.id) == recipe.challenge
            }
        ) {
            "Every Daily recipe challenge must belong to the configured generated-challenge catalog."
        }
    }

    fun resolve(version: DailyRecipeVersion): DailyRecipe = requireNotNull(recipesByVersion[version]) {
        "No Daily recipe is configured for version ${version.value}."
    }

    fun resolveOrNull(version: DailyRecipeVersion): DailyRecipe? = recipesByVersion[version]
}

object DailyRecipes {
    val FOUR_PAIRS_LOW_V1: DailyRecipe = DailyRecipe(
        version = DailyRecipeVersion("daily-4-pairs-low-v1"),
        challenge = GeneratedModes.FOUR_PAIRS_LOW,
        candidateIndices = List(4, ::DailyCandidateIndex),
        seedSchedule = Fnv1a32DailyCandidateSeedSchedule
    )
    val catalog: DailyRecipeCatalog = DailyRecipeCatalog(
        recipes = listOf(FOUR_PAIRS_LOW_V1),
        generatedChallengeCatalog = GeneratedModes.catalog
    )
}
