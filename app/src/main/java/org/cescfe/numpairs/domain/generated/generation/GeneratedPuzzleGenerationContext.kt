package org.cescfe.numpairs.domain.generated.generation

import org.cescfe.numpairs.domain.generated.generation.internal.GeneratedPuzzleHardRuleSet
import org.cescfe.numpairs.domain.generated.profile.GeneratedPuzzleProfile

class GeneratedPuzzleGenerationContext private constructor(
    val profile: GeneratedPuzzleProfile,
    internal val hardRules: GeneratedPuzzleHardRuleSet
) {
    companion object {
        fun forProfile(profile: GeneratedPuzzleProfile): GeneratedPuzzleGenerationContext =
            GeneratedPuzzleGenerationContext(
                profile = profile,
                hardRules = GeneratedPuzzleHardRuleSet.from(profile = profile)
            )
    }
}
