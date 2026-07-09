package org.cescfe.numpairs.feature.eightpairs

import org.cescfe.numpairs.domain.generated.GeneratedPairsPuzzleGenerator
import org.cescfe.numpairs.domain.generated.GeneratedPuzzleProfiles
import org.cescfe.numpairs.domain.puzzle.model.Puzzle

class MediumDifficultyEightPairsPuzzleProvider private constructor(
    private val generator: GeneratedPairsPuzzleGenerator
) : EightPairsPuzzleProvider {
    constructor() : this(
        generator = GeneratedPairsPuzzleGenerator(
            profile = GeneratedPuzzleProfiles.EIGHT_PAIRS_MEDIUM
        )
    )

    constructor(seed: Int) : this(
        generator = GeneratedPairsPuzzleGenerator(
            profile = GeneratedPuzzleProfiles.EIGHT_PAIRS_MEDIUM,
            seed = seed
        )
    )

    override fun nextPuzzle(): Puzzle = generator.generate()
}

object DefaultEightPairsPuzzleProvider : EightPairsPuzzleProvider {
    private val delegate = MediumDifficultyEightPairsPuzzleProvider()

    override fun nextPuzzle(): Puzzle = delegate.nextPuzzle()
}
