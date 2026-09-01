package org.cescfe.numpairs.feature.daily

import org.cescfe.numpairs.domain.daily.DailyChallengeId
import org.cescfe.numpairs.domain.daily.DailyPersonalBestCategory
import org.cescfe.numpairs.domain.daily.DailyPersonalBestCategoryResolver

class DailyRecipePersonalBestCategoryResolver(private val recipeCatalog: DailyRecipeCatalog = DailyRecipes.catalog) :
    DailyPersonalBestCategoryResolver {
    override fun categoryFor(identity: DailyChallengeId): DailyPersonalBestCategory? =
        recipeCatalog.resolveOrNull(identity.recipeVersion)?.let { recipe ->
            DailyPersonalBestCategory(
                generatedChallengeId = recipe.challengeFor(identity).id.value
            )
        }
}
