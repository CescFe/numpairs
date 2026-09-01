package org.cescfe.numpairs.feature.daily

import org.cescfe.numpairs.domain.daily.DailyChallengeId
import org.cescfe.numpairs.domain.daily.DailyRecipeVersion
import org.cescfe.numpairs.domain.daily.DeviceLocalDateSource

data class CurrentDailyChallenge(val identity: DailyChallengeId, val recipe: DailyRecipe) {
    val challenge = recipe.challengeFor(identity)

    init {
        require(identity.recipeVersion == recipe.version) {
            "Current Daily Challenge identity and recipe version must match."
        }
    }
}

class CurrentDailyChallengeResolver(
    private val localDateSource: DeviceLocalDateSource,
    private val recipeCatalog: DailyRecipeCatalog = DailyRecipes.catalog,
    private val activeRecipeVersion: DailyRecipeVersion = DailyRecipes.WEEKLY_SCHEDULE_V2.version
) {
    fun resolve(): CurrentDailyChallenge {
        val localDate = localDateSource.currentDate()
        val recipe = recipeCatalog.resolve(activeRecipeVersion)
        return CurrentDailyChallenge(
            identity = recipe.identityFor(localDate),
            recipe = recipe
        )
    }
}
