package org.cescfe.numpairs.domain.eightpairs

import kotlin.random.Random
import org.cescfe.numpairs.domain.generated.GeneratedPairsPuzzle
import org.cescfe.numpairs.domain.generated.GeneratedPairsPuzzleGenerator
import org.cescfe.numpairs.domain.puzzle.model.Puzzle

class EightPairsMediumDifficultyPuzzleGenerator(
    random: Random = Random.Default,
    maxAttempts: Int = DEFAULT_MAX_ATTEMPTS
) {
    private val delegate = GeneratedPairsPuzzleGenerator(
        profile = EightPairsMediumDifficultyRules.profile,
        random = random,
        maxAttempts = maxAttempts
    )

    constructor(seed: Int) : this(random = Random(seed))

    fun generate(): Puzzle = delegate.generate()

    fun generateWithSolution(): GeneratedPairsPuzzle = delegate.generateWithSolution()

    companion object {
        private const val DEFAULT_MAX_ATTEMPTS = 50
    }
}
