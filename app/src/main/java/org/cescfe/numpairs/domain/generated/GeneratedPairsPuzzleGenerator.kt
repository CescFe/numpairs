package org.cescfe.numpairs.domain.generated

import kotlin.random.Random
import org.cescfe.numpairs.domain.generated.internal.GeneratedPairsPuzzleAssembler
import org.cescfe.numpairs.domain.generated.internal.GeneratedPairsSolvedCandidate
import org.cescfe.numpairs.domain.generated.internal.GeneratedPairsSolvedCandidateGenerator
import org.cescfe.numpairs.domain.generated.internal.GeneratedPairsValuePairSelector
import org.cescfe.numpairs.domain.generated.internal.GeneratedPairsVariationPlanOutcome
import org.cescfe.numpairs.domain.generated.internal.GeneratedPairsVariationPlanSelector
import org.cescfe.numpairs.domain.generated.internal.GeneratedStripMaskSelector
import org.cescfe.numpairs.domain.puzzle.assignment.StripEntryId
import org.cescfe.numpairs.domain.puzzle.model.Puzzle

class GeneratedPairsPuzzleGenerator(
    private val context: GeneratedPuzzleGenerationContext,
    private val random: Random = Random.Default,
    private val maxAttempts: Int = DEFAULT_MAX_ATTEMPTS
) {
    private val profile: GeneratedPuzzleProfile = context.profile

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
            random = random,
            hardRules = context.hardRules.valuePairs
        )
    )
    private val stripMaskSelector = GeneratedStripMaskSelector(
        profile = profile,
        random = random,
        hardRule = context.hardRules.stripMask
    )
    private val puzzleAssembler = GeneratedPairsPuzzleAssembler(
        profile = profile,
        random = random
    )
    constructor(
        profile: GeneratedPuzzleProfile,
        random: Random = Random.Default,
        maxAttempts: Int = DEFAULT_MAX_ATTEMPTS
    ) : this(
        context = GeneratedPuzzleGenerationContext.forProfile(profile = profile),
        random = random,
        maxAttempts = maxAttempts
    )

    constructor(
        profile: GeneratedPuzzleProfile,
        seed: Int,
        maxAttempts: Int = DEFAULT_MAX_ATTEMPTS
    ) : this(
        context = GeneratedPuzzleGenerationContext.forProfile(profile = profile),
        random = Random(seed),
        maxAttempts = maxAttempts
    )

    constructor(
        context: GeneratedPuzzleGenerationContext,
        seed: Int,
        maxAttempts: Int = DEFAULT_MAX_ATTEMPTS
    ) : this(
        context = context,
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
        return when (
            val creation = GeneratedPairsPuzzle.fromSolvedPuzzle(
                context = context,
                solvedPuzzle = solvedPuzzle,
                knownEntryIds = candidate.knownEntryIds
            )
        ) {
            is GeneratedPairsPuzzleCreation.Created -> creation.puzzle
            is GeneratedPairsPuzzleCreation.Rejected -> null
        }
    }

    companion object {
        private const val DEFAULT_MAX_ATTEMPTS = 50
    }
}

private data class GeneratedPairsPuzzleCandidate(
    val solvedCandidate: GeneratedPairsSolvedCandidate,
    val knownEntryIds: Set<StripEntryId>
)
