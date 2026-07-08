package org.cescfe.numpairs.feature.fourpairs

import org.cescfe.numpairs.domain.generated.GeneratedPairsPuzzleGenerator
import org.cescfe.numpairs.domain.generated.GeneratedPuzzleProfiles
import org.cescfe.numpairs.domain.puzzle.model.Puzzle

class LowDifficultyFourPairsPuzzleProvider private constructor(private val generator: GeneratedPairsPuzzleGenerator) :
    FourPairsPuzzleProvider {
    constructor() : this(
        generator = GeneratedPairsPuzzleGenerator(
            profile = GeneratedPuzzleProfiles.FOUR_PAIRS_LOW
        )
    )

    constructor(seed: Int) : this(
        generator = GeneratedPairsPuzzleGenerator(
            profile = GeneratedPuzzleProfiles.FOUR_PAIRS_LOW,
            seed = seed
        )
    )

    override fun nextPuzzle(): Puzzle = generator.generate()
}

object DefaultFourPairsPuzzleProvider : FourPairsPuzzleProvider {
    private val delegate = LowDifficultyFourPairsPuzzleProvider()

    override fun nextPuzzle(): Puzzle = delegate.nextPuzzle()
}
