package org.cescfe.numpairs.domain.generated.generation

import org.cescfe.numpairs.domain.generated.generation.internal.GeneratedPuzzleConstraintSet
import org.cescfe.numpairs.domain.generated.profile.GeneratedPuzzleProfile

class GeneratedPuzzleGenerationContext private constructor(
    val profile: GeneratedPuzzleProfile,
    internal val constraints: GeneratedPuzzleConstraintSet
) {
    companion object {
        fun forProfile(profile: GeneratedPuzzleProfile): GeneratedPuzzleGenerationContext =
            GeneratedPuzzleGenerationContext(
                profile = profile,
                constraints = GeneratedPuzzleConstraintSet.from(profile = profile)
            )
    }
}
