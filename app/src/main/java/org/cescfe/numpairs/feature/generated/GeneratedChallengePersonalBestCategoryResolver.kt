package org.cescfe.numpairs.feature.generated

import org.cescfe.numpairs.domain.generated.GeneratedPersonalBestCategory
import org.cescfe.numpairs.domain.generated.GeneratedPersonalBestCategoryResolver

class GeneratedChallengePersonalBestCategoryResolver(
    private val catalog: GeneratedChallengeCatalog = GeneratedModes.catalog
) : GeneratedPersonalBestCategoryResolver {
    override fun categoryFor(modeId: String, profileId: String): GeneratedPersonalBestCategory? =
        catalog.resolveChallengeOrNull(modeId = modeId, profileId = profileId)
            ?.id
            ?.value
            ?.let(GeneratedPersonalBestCategory::fromGeneratedChallengeIdOrNull)
}
