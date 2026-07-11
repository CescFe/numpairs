package org.cescfe.numpairs.domain.generated

import kotlin.random.Random
import org.cescfe.numpairs.domain.generated.internal.GeneratedPairsPuzzleAssembler
import org.cescfe.numpairs.domain.generated.internal.GeneratedPairsSearchControl
import org.cescfe.numpairs.domain.generated.internal.GeneratedPairsSearchOutcome
import org.cescfe.numpairs.domain.generated.internal.GeneratedPairsSolvedCandidate
import org.cescfe.numpairs.domain.generated.internal.GeneratedPairsSolvedCandidateGenerator
import org.cescfe.numpairs.domain.generated.internal.GeneratedPairsValuePairSelector
import org.cescfe.numpairs.domain.generated.internal.GeneratedPairsVariationPlanOutcome
import org.cescfe.numpairs.domain.generated.internal.GeneratedPairsVariationPlanSelector
import org.cescfe.numpairs.domain.generated.internal.GeneratedStripMaskSelector
import org.cescfe.numpairs.domain.puzzle.assignment.StripEntryId

/**
 * Each call receives its own seed and execution policy, so concurrent requests never share a
 * mutable random stream.
 */
class GeneratedPairsPuzzleGenerator(private val context: GeneratedPuzzleGenerationContext) {
    private val profile: GeneratedPuzzleProfile = context.profile

    constructor(profile: GeneratedPuzzleProfile) : this(
        context = GeneratedPuzzleGenerationContext.forProfile(profile = profile)
    )

    fun generate(
        request: GeneratedPuzzleGenerationRequest,
        cancellation: GeneratedPuzzleGenerationCancellation = GeneratedPuzzleGenerationCancellation.None
    ): GeneratedPairsPuzzleGenerationOutcome {
        require(request.profileId == profile.id) {
            "Generation request profile ${request.profileId.value} does not match generator profile ${profile.id.value}."
        }

        val random = Random(request.seed)
        val searchControl = GeneratedPairsSearchControl(
            executionPolicy = request.executionPolicy,
            cancellation = cancellation
        )
        val variationPlan = GeneratedPairsVariationPlanSelector(profile = profile, random = random).select()
        val solvedCandidateGenerator = GeneratedPairsSolvedCandidateGenerator(
            valuePairSelector = GeneratedPairsValuePairSelector(
                profile = profile,
                random = random,
                hardRules = context.hardRules.valuePairs
            )
        )
        val stripMaskSelector = GeneratedStripMaskSelector(
            profile = profile,
            random = random,
            hardRule = context.hardRules.stripMask
        )
        val puzzleAssembler = GeneratedPairsPuzzleAssembler(profile = profile, random = random)
        val rejections = mutableListOf<GeneratedPairsPuzzleCandidateRejection>()
        var fallbackCandidate: GeneratedPairsPuzzleCandidate? = null
        var attemptsUsed = 0

        while (attemptsUsed < request.executionPolicy.maxAttempts) {
            when (val controlResult = searchControl.check()) {
                is org.cescfe.numpairs.domain.generated.internal.GeneratedPairsSearchControlResult.Continue -> Unit
                else -> return failure(
                    request = request,
                    attemptsUsed = attemptsUsed,
                    searchControl = searchControl,
                    reason = controlResult.failureReason(),
                    rejections = rejections
                )
            }

            attemptsUsed++
            val solvedCandidate = when (
                val outcome = solvedCandidateGenerator.generate(
                    variationPlan = variationPlan,
                    searchControl = searchControl
                )
            ) {
                is GeneratedPairsSearchOutcome.Found -> outcome.value
                GeneratedPairsSearchOutcome.NoCandidate -> {
                    rejections +=
                        GeneratedPairsPuzzleCandidateRejection.ValuePairSelectionFailed(attempt = attemptsUsed)
                    continue
                }

                GeneratedPairsSearchOutcome.BudgetExhausted,
                GeneratedPairsSearchOutcome.Cancelled -> return failure(
                    request = request,
                    attemptsUsed = attemptsUsed,
                    searchControl = searchControl,
                    reason = outcome.failureReason(),
                    rejections = rejections
                )
            }
            val stripMaskSelection = when (
                val outcome = stripMaskSelector.selectKnownEntryIds(
                    pairs = solvedCandidate.pairs,
                    variationPlan = variationPlan,
                    searchControl = searchControl
                )
            ) {
                is GeneratedPairsSearchOutcome.Found -> outcome.value
                GeneratedPairsSearchOutcome.NoCandidate -> {
                    rejections +=
                        GeneratedPairsPuzzleCandidateRejection.StripMaskSelectionFailed(attempt = attemptsUsed)
                    continue
                }

                GeneratedPairsSearchOutcome.BudgetExhausted,
                GeneratedPairsSearchOutcome.Cancelled -> return failure(
                    request = request,
                    attemptsUsed = attemptsUsed,
                    searchControl = searchControl,
                    reason = outcome.failureReason(),
                    rejections = rejections
                )
            }
            val candidate = GeneratedPairsPuzzleCandidate(
                solvedCandidate = solvedCandidate,
                knownEntryIds = stripMaskSelection.knownEntryIds
            )

            if (stripMaskSelection.variationPlanOutcome == GeneratedPairsVariationPlanOutcome.FALLBACK) {
                fallbackCandidate = fallbackCandidate ?: candidate
                continue
            }

            when (val outcome = buildValidGeneratedPuzzle(candidate = candidate, puzzleAssembler = puzzleAssembler)) {
                is GeneratedPuzzleBuildOutcome.Created -> return GeneratedPairsPuzzleGenerationOutcome.Generated(
                    request = request,
                    puzzle = outcome.puzzle,
                    attemptsUsed = attemptsUsed,
                    searchWorkConsumed = searchControl.searchWorkConsumed
                )

                is GeneratedPuzzleBuildOutcome.Rejected -> {
                    rejections += GeneratedPairsPuzzleCandidateRejection.FinalValidationFailed(
                        attempt = attemptsUsed,
                        violations = outcome.violations
                    )
                }
            }
        }

        fallbackCandidate?.let { candidate ->
            when (val outcome = buildValidGeneratedPuzzle(candidate = candidate, puzzleAssembler = puzzleAssembler)) {
                is GeneratedPuzzleBuildOutcome.Created -> return GeneratedPairsPuzzleGenerationOutcome.Generated(
                    request = request,
                    puzzle = outcome.puzzle,
                    attemptsUsed = attemptsUsed,
                    searchWorkConsumed = searchControl.searchWorkConsumed
                )

                is GeneratedPuzzleBuildOutcome.Rejected -> {
                    rejections += GeneratedPairsPuzzleCandidateRejection.FinalValidationFailed(
                        attempt = attemptsUsed,
                        violations = outcome.violations
                    )
                }
            }
        }

        return failure(
            request = request,
            attemptsUsed = attemptsUsed,
            searchControl = searchControl,
            reason = GeneratedPairsPuzzleGenerationFailureReason.AttemptsExhausted,
            rejections = rejections
        )
    }

    private fun buildValidGeneratedPuzzle(
        candidate: GeneratedPairsPuzzleCandidate,
        puzzleAssembler: GeneratedPairsPuzzleAssembler
    ): GeneratedPuzzleBuildOutcome {
        val solvedPuzzle = puzzleAssembler.buildSolvedPuzzle(candidate = candidate.solvedCandidate)
        return when (
            val creation = GeneratedPairsPuzzle.fromSolvedPuzzle(
                context = context,
                solvedPuzzle = solvedPuzzle,
                knownEntryIds = candidate.knownEntryIds
            )
        ) {
            is GeneratedPairsPuzzleCreation.Created -> GeneratedPuzzleBuildOutcome.Created(creation.puzzle)
            is GeneratedPairsPuzzleCreation.Rejected -> GeneratedPuzzleBuildOutcome.Rejected(creation.violations)
        }
    }
}

private fun failure(
    request: GeneratedPuzzleGenerationRequest,
    attemptsUsed: Int,
    searchControl: GeneratedPairsSearchControl,
    reason: GeneratedPairsPuzzleGenerationFailureReason,
    rejections: List<GeneratedPairsPuzzleCandidateRejection>
): GeneratedPairsPuzzleGenerationOutcome.Failed = GeneratedPairsPuzzleGenerationOutcome.Failed(
    request = request,
    attemptsUsed = attemptsUsed,
    searchWorkConsumed = searchControl.searchWorkConsumed,
    reason = reason,
    candidateRejections = rejections.toList()
)

private fun org.cescfe.numpairs.domain.generated.internal.GeneratedPairsSearchControlResult.failureReason():
    GeneratedPairsPuzzleGenerationFailureReason =
    when (this) {
        org.cescfe.numpairs.domain.generated.internal.GeneratedPairsSearchControlResult.Continue ->
            error("A continuing search control result cannot terminate generation.")
        org.cescfe.numpairs.domain.generated.internal.GeneratedPairsSearchControlResult.BudgetExhausted ->
            GeneratedPairsPuzzleGenerationFailureReason.SearchBudgetExhausted
        org.cescfe.numpairs.domain.generated.internal.GeneratedPairsSearchControlResult.Cancelled ->
            GeneratedPairsPuzzleGenerationFailureReason.Cancelled
    }

private fun GeneratedPairsSearchOutcome<*>.failureReason(): GeneratedPairsPuzzleGenerationFailureReason = when (this) {
    GeneratedPairsSearchOutcome.BudgetExhausted -> GeneratedPairsPuzzleGenerationFailureReason.SearchBudgetExhausted
    GeneratedPairsSearchOutcome.Cancelled -> GeneratedPairsPuzzleGenerationFailureReason.Cancelled
    is GeneratedPairsSearchOutcome.Found,
    GeneratedPairsSearchOutcome.NoCandidate -> error("A non-terminal search outcome cannot terminate generation.")
}

private data class GeneratedPairsPuzzleCandidate(
    val solvedCandidate: GeneratedPairsSolvedCandidate,
    val knownEntryIds: Set<StripEntryId>
)

private sealed interface GeneratedPuzzleBuildOutcome {
    data class Created(val puzzle: GeneratedPairsPuzzle) : GeneratedPuzzleBuildOutcome
    data class Rejected(val violations: List<GeneratedPairsPuzzleValidationViolation>) : GeneratedPuzzleBuildOutcome
}
