package org.cescfe.numpairs.domain.generated

import kotlin.random.Random
import org.cescfe.numpairs.domain.puzzle.model.Puzzle

class GeneratedPairsPuzzleGenerator(
    private val profile: GeneratedPuzzleProfile,
    private val random: Random = Random.Default,
    private val maxAttempts: Int = DEFAULT_MAX_ATTEMPTS
) {
    init {
        require(maxAttempts > 0) {
            "Maximum generation attempts must be positive."
        }
    }

    private val generationTargetSelector = GeneratedPairsGenerationTargetSelector(
        profile = profile,
        random = random
    )
    private val solvedCandidateGenerator = GeneratedPairsSolvedCandidateGenerator(
        valuePairSelector = GeneratedPairsValuePairSelector(
            profile = profile,
            random = random
        )
    )
    private val stripMaskSelector = GeneratedStripMaskSelector(
        profile = profile,
        random = random
    )
    private val puzzleAssembler = GeneratedPairsPuzzleAssembler(
        profile = profile,
        random = random
    )
    private val puzzleValidator = GeneratedPairsPuzzleValidator(profile = profile)

    constructor(
        profile: GeneratedPuzzleProfile,
        seed: Int,
        maxAttempts: Int = DEFAULT_MAX_ATTEMPTS
    ) : this(
        profile = profile,
        random = Random(seed),
        maxAttempts = maxAttempts
    )

    fun generate(): Puzzle = generateWithSolution().initialPuzzle

    fun generateWithSolution(): GeneratedPairsPuzzle {
        val generationTargets = generationTargetSelector.select()

        repeat(maxAttempts) {
            val solvedCandidate = solvedCandidateGenerator.generate(
                generationTargets = generationTargets
            ) ?: return@repeat
            val knownEntryIds = stripMaskSelector.selectKnownEntryIds(
                pairs = solvedCandidate.pairs
            ) ?: return@repeat
            val solvedPuzzle = puzzleAssembler.buildSolvedPuzzle(candidate = solvedCandidate)
            val generatedPuzzle = GeneratedPairsPuzzle(
                initialPuzzle = puzzleAssembler.buildInitialPuzzle(
                    solvedPuzzle = solvedPuzzle,
                    knownEntryIds = knownEntryIds
                ),
                solvedPuzzle = solvedPuzzle
            )

            if (puzzleValidator.isValid(generatedPuzzle = generatedPuzzle, pairs = solvedCandidate.pairs)) {
                return generatedPuzzle
            }
        }

        error(
            "Unable to generate a generated pairs puzzle for profile ${profile.id.value} after $maxAttempts attempts."
        )
    }

    companion object {
        private const val DEFAULT_MAX_ATTEMPTS = 50
    }
}
