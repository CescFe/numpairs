package org.cescfe.numpairs.domain.generated

import kotlin.random.Random
import org.cescfe.numpairs.domain.generated.internal.GeneratedPairsPuzzleAssembler
import org.cescfe.numpairs.domain.generated.internal.GeneratedPairsPuzzleValidator
import org.cescfe.numpairs.domain.generated.internal.GeneratedPairsSolvedCandidate
import org.cescfe.numpairs.domain.generated.internal.GeneratedPairsSolvedCandidateGenerator
import org.cescfe.numpairs.domain.generated.internal.GeneratedPairsValuePairSelector
import org.cescfe.numpairs.domain.generated.internal.GeneratedPairsVariationPlanOutcome
import org.cescfe.numpairs.domain.generated.internal.GeneratedPairsVariationPlanSelector
import org.cescfe.numpairs.domain.generated.internal.GeneratedStripMaskSelector
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

    private val variationPlanSelector = GeneratedPairsVariationPlanSelector(
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
        val variationPlan = variationPlanSelector.select()
        var fallbackCandidate: GeneratedPairsPuzzleCandidate? = null

        repeat(maxAttempts) {
            val solvedCandidate = solvedCandidateGenerator.generate(
                variationPlan = variationPlan
            ) ?: return@repeat
            val stripMaskSelection = stripMaskSelector.selectKnownEntryIds(
                pairs = solvedCandidate.pairs,
                variationPlan = variationPlan
            ) ?: return@repeat
            val candidate = GeneratedPairsPuzzleCandidate(
                solvedCandidate = solvedCandidate,
                knownEntryIds = stripMaskSelection.knownEntryIds
            )

            if (stripMaskSelection.variationPlanOutcome == GeneratedPairsVariationPlanOutcome.FALLBACK) {
                if (fallbackCandidate == null) {
                    fallbackCandidate = candidate
                }
                return@repeat
            }

            buildValidGeneratedPuzzle(candidate = candidate)?.let { generatedPuzzle ->
                return generatedPuzzle
            }
        }

        fallbackCandidate?.let { candidate ->
            buildValidGeneratedPuzzle(candidate = candidate)?.let { generatedPuzzle ->
                return generatedPuzzle
            }
        }

        error(
            "Unable to generate a generated pairs puzzle for profile ${profile.id.value} after $maxAttempts attempts."
        )
    }

    private fun buildValidGeneratedPuzzle(candidate: GeneratedPairsPuzzleCandidate): GeneratedPairsPuzzle? {
        val solvedPuzzle = puzzleAssembler.buildSolvedPuzzle(candidate = candidate.solvedCandidate)
        val generatedPuzzle = GeneratedPairsPuzzle(
            initialPuzzle = puzzleAssembler.buildInitialPuzzle(
                solvedPuzzle = solvedPuzzle,
                knownEntryIds = candidate.knownEntryIds
            ),
            solvedPuzzle = solvedPuzzle
        )

        return generatedPuzzle.takeIf {
            puzzleValidator.validate(generatedPuzzle = generatedPuzzle).isValid
        }
    }

    companion object {
        private const val DEFAULT_MAX_ATTEMPTS = 50
    }
}

private data class GeneratedPairsPuzzleCandidate(
    val solvedCandidate: GeneratedPairsSolvedCandidate,
    val knownEntryIds: Set<Int>
)
