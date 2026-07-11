package org.cescfe.numpairs.domain.generated

import org.cescfe.numpairs.domain.generated.internal.GeneratedPuzzleHardRuleSet

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
