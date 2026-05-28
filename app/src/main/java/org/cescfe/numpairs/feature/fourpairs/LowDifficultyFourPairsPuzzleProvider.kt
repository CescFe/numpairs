package org.cescfe.numpairs.feature.fourpairs

import org.cescfe.numpairs.domain.fourpairs.FourPairsLowDifficultyPuzzleGenerator
import org.cescfe.numpairs.domain.puzzle.Puzzle

class LowDifficultyFourPairsPuzzleProvider private constructor(
    private val generator: FourPairsLowDifficultyPuzzleGenerator
) : FourPairsPuzzleProvider {
    constructor() : this(generator = FourPairsLowDifficultyPuzzleGenerator())

    constructor(seed: Int) : this(generator = FourPairsLowDifficultyPuzzleGenerator(seed = seed))

    override fun nextPuzzle(): Puzzle = generator.generate()
}
