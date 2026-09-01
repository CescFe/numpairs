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

data class DailyRecipe(val contract: DailyRecipeContract, val challenges: List<GeneratedChallenge>) {
    val version: DailyRecipeVersion
        get() = contract.version

    val candidateIndices: List<DailyCandidateIndex>
        get() = contract.candidateIndices

    init {
        require(challenges.isNotEmpty()) {
            "A Daily recipe must configure at least one challenge."
        }
        require(challenges.map(GeneratedChallenge::id).distinct().size == challenges.size) {
            "Daily recipe challenge ids must be unique."
        }
        require(
            challenges.map { challenge ->
                challenge.profile
            }.toSet() == contract.profileSelection.profiles
        ) {
            "Daily recipe challenges must match every profile in their platform-independent contract."
        }
    }

    fun identityFor(localDate: LocalDate): DailyChallengeId = contract.identityFor(localDate)

    fun seedFor(identity: DailyChallengeId, candidateIndex: DailyCandidateIndex): Int =
        contract.seedFor(identity = identity, candidateIndex = candidateIndex)

    fun challengeFor(identity: DailyChallengeId): GeneratedChallenge {
        val profile = contract.profileFor(identity)
        return challenges.single { challenge -> challenge.profile == profile }
    }
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
                recipe.challenges.all { challenge ->
                    generatedChallengeCatalog.resolveChallenge(challenge.id) == challenge
                }
            }
        ) {
            "Every Daily recipe challenge must belong to the configured generated-challenge catalog."
        }
    }

    fun resolve(version: DailyRecipeVersion): DailyRecipe = requireNotNull(recipesByVersion[version]) {
        "No Daily recipe is configured for version ${version.value}."
    }

    fun resolveOrNull(version: DailyRecipeVersion): DailyRecipe? = recipesByVersion[version]

    fun challengeFor(identity: DailyChallengeId): GeneratedChallenge =
        resolve(identity.recipeVersion).challengeFor(identity)
}

object DailyRecipes {
    val FOUR_PAIRS_LOW_V1: DailyRecipe = DailyRecipe(
        contract = DailyRecipeContracts.FOUR_PAIRS_LOW_V1,
        challenges = listOf(GeneratedModes.FOUR_PAIRS_LOW)
    )
    val WEEKLY_SCHEDULE_V2: DailyRecipe = DailyRecipe(
        contract = DailyRecipeContracts.WEEKLY_SCHEDULE_V2,
        challenges = listOf(
            GeneratedModes.THREE_PAIRS_LOW,
            GeneratedModes.FOUR_PAIRS_LOW,
            GeneratedModes.THREE_PAIRS_MEDIUM,
            GeneratedModes.FOUR_PAIRS_MEDIUM
        )
    )
    val catalog: DailyRecipeCatalog = DailyRecipeCatalog(
        recipes = listOf(FOUR_PAIRS_LOW_V1, WEEKLY_SCHEDULE_V2),
        generatedChallengeCatalog = GeneratedModes.catalog
    )
}
