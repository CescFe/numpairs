package org.cescfe.numpairs.domain.generated.generation

import kotlin.random.Random
import org.cescfe.numpairs.domain.generated.assessment.GeneratedPairsDifficultyAssessor
import org.cescfe.numpairs.domain.generated.assessment.GeneratedPuzzleDifficultyAssessmentOutcome
import org.cescfe.numpairs.domain.generated.assessment.GeneratedPuzzleDifficultyPolicyEvaluation
import org.cescfe.numpairs.domain.generated.generation.internal.GeneratedPairsPuzzleAssembler
import org.cescfe.numpairs.domain.generated.generation.internal.GeneratedPairsSearchControl
import org.cescfe.numpairs.domain.generated.generation.internal.GeneratedPairsSearchControlResult
import org.cescfe.numpairs.domain.generated.generation.internal.GeneratedPairsSearchOutcome
import org.cescfe.numpairs.domain.generated.generation.internal.GeneratedPairsSolvedCandidate
import org.cescfe.numpairs.domain.generated.generation.internal.GeneratedPairsSolvedCandidateGenerator
import org.cescfe.numpairs.domain.generated.generation.internal.GeneratedPairsValuePairSelector
import org.cescfe.numpairs.domain.generated.generation.internal.GeneratedPairsVariationPlanOutcome
import org.cescfe.numpairs.domain.generated.generation.internal.GeneratedPairsVariationPlanSelector
import org.cescfe.numpairs.domain.generated.generation.internal.GeneratedStripMaskSelector
import org.cescfe.numpairs.domain.generated.profile.GeneratedPuzzleProfile
import org.cescfe.numpairs.domain.generated.puzzle.GeneratedPairsPuzzle
import org.cescfe.numpairs.domain.generated.puzzle.GeneratedPairsPuzzleCreation
import org.cescfe.numpairs.domain.generated.puzzle.GeneratedPairsPuzzleValidationViolation
import org.cescfe.numpairs.domain.puzzle.assignment.StripEntryId

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
                constraints = context.constraints.valuePairs
            )
        )
        val stripMaskSelector = GeneratedStripMaskSelector(
            profile = profile,
            random = random,
            constraint = context.constraints.stripMask
        )
        val puzzleAssembler = GeneratedPairsPuzzleAssembler(profile = profile, random = random)
        val rejections = mutableListOf<GeneratedPairsPuzzleCandidateRejection>()
        var fallbackCandidate: GeneratedPairsPuzzleCandidate? = null
        var attemptsUsed = 0

        while (attemptsUsed < request.executionPolicy.maxAttempts) {
            when (val controlResult = searchControl.check()) {
                is GeneratedPairsSearchControlResult.Continue -> Unit
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

            handleBuildOutcome(
                outcome = buildValidGeneratedPuzzle(
                    candidate = candidate,
                    puzzleAssembler = puzzleAssembler,
                    cancellation = cancellation
                ),
                request = request,
                attemptsUsed = attemptsUsed,
                searchControl = searchControl,
                rejections = rejections
            )?.let { outcome -> return outcome }
        }

        fallbackCandidate?.let { candidate ->
            handleBuildOutcome(
                outcome = buildValidGeneratedPuzzle(
                    candidate = candidate,
                    puzzleAssembler = puzzleAssembler,
                    cancellation = cancellation
                ),
                request = request,
                attemptsUsed = attemptsUsed,
                searchControl = searchControl,
                rejections = rejections
            )?.let { outcome -> return outcome }
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
        puzzleAssembler: GeneratedPairsPuzzleAssembler,
        cancellation: GeneratedPuzzleGenerationCancellation
    ): GeneratedPuzzleBuildOutcome {
        val solvedPuzzle = puzzleAssembler.buildSolvedPuzzle(candidate = candidate.solvedCandidate)
        return when (
            val creation = GeneratedPairsPuzzle.fromSolvedPuzzle(
                context = context,
                solvedPuzzle = solvedPuzzle,
                knownEntryIds = candidate.knownEntryIds
            )
        ) {
            is GeneratedPairsPuzzleCreation.Created -> assessDifficulty(
                puzzle = creation.puzzle,
                cancellation = cancellation
            )
            is GeneratedPairsPuzzleCreation.Rejected -> GeneratedPuzzleBuildOutcome.Rejected(creation.violations)
        }
    }

    private fun assessDifficulty(
        puzzle: GeneratedPairsPuzzle,
        cancellation: GeneratedPuzzleGenerationCancellation
    ): GeneratedPuzzleBuildOutcome {
        val policy = profile.difficultyAssessmentPolicy
            ?: return GeneratedPuzzleBuildOutcome.Created(puzzle = puzzle)
        return when (
            val outcome = GeneratedPairsDifficultyAssessor().assess(
                initialPuzzle = puzzle.initialPuzzle,
                profile = profile,
                executionPolicy = policy.executionPolicy,
                cancellation = { cancellation.isCancellationRequested() }
            )
        ) {
            is GeneratedPuzzleDifficultyAssessmentOutcome.Assessed -> {
                val evaluation = policy.evaluate(report = outcome.report)
                if (evaluation.isAccepted) {
                    GeneratedPuzzleBuildOutcome.Created(puzzle = puzzle)
                } else {
                    GeneratedPuzzleBuildOutcome.DifficultyRejected(evaluation = evaluation)
                }
            }

            is GeneratedPuzzleDifficultyAssessmentOutcome.Unsatisfiable ->
                GeneratedPuzzleBuildOutcome.DifficultyUnavailable(outcome = outcome)
            is GeneratedPuzzleDifficultyAssessmentOutcome.WorkLimitReached ->
                GeneratedPuzzleBuildOutcome.AssessmentWorkLimitReached
            is GeneratedPuzzleDifficultyAssessmentOutcome.Cancelled -> GeneratedPuzzleBuildOutcome.Cancelled
        }
    }

    private fun handleBuildOutcome(
        outcome: GeneratedPuzzleBuildOutcome,
        request: GeneratedPuzzleGenerationRequest,
        attemptsUsed: Int,
        searchControl: GeneratedPairsSearchControl,
        rejections: MutableList<GeneratedPairsPuzzleCandidateRejection>
    ): GeneratedPairsPuzzleGenerationOutcome? = when (outcome) {
        is GeneratedPuzzleBuildOutcome.Created -> GeneratedPairsPuzzleGenerationOutcome.Generated(
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
            null
        }

        is GeneratedPuzzleBuildOutcome.DifficultyRejected -> {
            rejections += GeneratedPairsPuzzleCandidateRejection.DifficultyAssessmentRejected(
                attempt = attemptsUsed,
                evaluation = outcome.evaluation
            )
            null
        }

        is GeneratedPuzzleBuildOutcome.DifficultyUnavailable -> {
            rejections += GeneratedPairsPuzzleCandidateRejection.DifficultyAssessmentUnavailable(
                attempt = attemptsUsed,
                outcome = outcome.outcome
            )
            null
        }

        GeneratedPuzzleBuildOutcome.AssessmentWorkLimitReached -> failure(
            request = request,
            attemptsUsed = attemptsUsed,
            searchControl = searchControl,
            reason = GeneratedPairsPuzzleGenerationFailureReason.DifficultyAssessmentWorkLimitReached,
            rejections = rejections
        )

        GeneratedPuzzleBuildOutcome.Cancelled -> failure(
            request = request,
            attemptsUsed = attemptsUsed,
            searchControl = searchControl,
            reason = GeneratedPairsPuzzleGenerationFailureReason.Cancelled,
            rejections = rejections
        )
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

private fun GeneratedPairsSearchControlResult.failureReason(): GeneratedPairsPuzzleGenerationFailureReason =
    when (this) {
        GeneratedPairsSearchControlResult.Continue ->
            error("A continuing search control result cannot terminate generation.")
        GeneratedPairsSearchControlResult.BudgetExhausted ->
            GeneratedPairsPuzzleGenerationFailureReason.SearchBudgetExhausted
        GeneratedPairsSearchControlResult.Cancelled ->
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
    data class DifficultyRejected(val evaluation: GeneratedPuzzleDifficultyPolicyEvaluation) :
        GeneratedPuzzleBuildOutcome
    data class DifficultyUnavailable(val outcome: GeneratedPuzzleDifficultyAssessmentOutcome.Unsatisfiable) :
        GeneratedPuzzleBuildOutcome
    data object AssessmentWorkLimitReached : GeneratedPuzzleBuildOutcome
    data object Cancelled : GeneratedPuzzleBuildOutcome
}
