package org.cescfe.numpairs.feature.daily

import java.time.LocalDate
import org.cescfe.numpairs.domain.daily.DailyCandidateIndex
import org.cescfe.numpairs.domain.daily.DailyChallengeId
import org.cescfe.numpairs.domain.daily.DailyRecipeContract
import org.cescfe.numpairs.domain.daily.DailyRecipeContractCatalog
import org.cescfe.numpairs.domain.daily.DailyRecipeContracts
import org.cescfe.numpairs.domain.daily.DailyRecipeVersion
import org.cescfe.numpairs.feature.generated.GeneratedChallenge
import org.cescfe.numpairs.feature.generated.GeneratedChallengeCatalog
import org.cescfe.numpairs.feature.generated.GeneratedModes

data class DailyRecipe(val contract: DailyRecipeContract, val challenge: GeneratedChallenge) {
    val version: DailyRecipeVersion
        get() = contract.version

    val candidateIndices: List<DailyCandidateIndex>
        get() = contract.candidateIndices

    init {
        require(challenge.profile == contract.profile) {
            "Daily recipe challenge profile must match its platform-independent contract."
        }
    }

    fun identityFor(localDate: LocalDate): DailyChallengeId = contract.identityFor(localDate)

    fun seedFor(identity: DailyChallengeId, candidateIndex: DailyCandidateIndex): Int =
        contract.seedFor(identity = identity, candidateIndex = candidateIndex)
}

class DailyRecipeCatalog(
    recipes: Collection<DailyRecipe>,
    generatedChallengeCatalog: GeneratedChallengeCatalog,
    recipeContractCatalog: DailyRecipeContractCatalog = DailyRecipeContracts.catalog
) {
    val all: List<DailyRecipe> = recipes.toList()
    private val recipesByVersion: Map<DailyRecipeVersion, DailyRecipe> = all.associateBy(DailyRecipe::version)

    init {
        require(all.isNotEmpty()) {
            "At least one Daily recipe must be configured."
        }
        require(recipesByVersion.size == all.size) {
            "Daily recipe versions must be unique."
        }
        require(all.all { recipe -> recipeContractCatalog.resolve(recipe.version) == recipe.contract }) {
            "Every Daily recipe must use its configured platform-independent contract."
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
        contract = DailyRecipeContracts.FOUR_PAIRS_LOW_V1,
        challenge = GeneratedModes.FOUR_PAIRS_LOW
    )
    val catalog: DailyRecipeCatalog = DailyRecipeCatalog(
        recipes = listOf(FOUR_PAIRS_LOW_V1),
        generatedChallengeCatalog = GeneratedModes.catalog
    )
}
