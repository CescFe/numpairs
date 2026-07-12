package org.cescfe.numpairs.domain.generated.generation

import org.cescfe.numpairs.domain.generated.profile.GeneratedPuzzleProfile
import org.cescfe.numpairs.domain.generated.puzzle.GeneratedPairsPuzzle

fun generatedPuzzle(
    profile: GeneratedPuzzleProfile,
    seed: Int,
    executionPolicy: GeneratedPuzzleGenerationExecutionPolicy = GeneratedPuzzleGenerationExecutionPolicy()
): GeneratedPairsPuzzle = when (
    val outcome = GeneratedPairsPuzzleGenerator(profile = profile).generate(
        request = GeneratedPuzzleGenerationRequest(
            profile = profile,
            seed = seed,
            executionPolicy = executionPolicy
        )
    )
) {
    is GeneratedPairsPuzzleGenerationOutcome.Generated -> outcome.puzzle
    is GeneratedPairsPuzzleGenerationOutcome.Failed -> error(
        "Expected generated puzzle for ${profile.id.value}, seed $seed, but generation failed: ${outcome.reason}."
    )
}
