package org.cescfe.numpairs.domain.generated.generation

import org.cescfe.numpairs.domain.generated.assessment.GeneratedPuzzleDifficultyAssessmentOutcome
import org.cescfe.numpairs.domain.generated.assessment.GeneratedPuzzleDifficultyPolicyEvaluation
import org.cescfe.numpairs.domain.generated.profile.GeneratedPuzzleProfile
import org.cescfe.numpairs.domain.generated.profile.GeneratedPuzzleProfileId
import org.cescfe.numpairs.domain.generated.puzzle.GeneratedPairsPuzzle
import org.cescfe.numpairs.domain.generated.puzzle.GeneratedPairsPuzzleValidationViolation

data class GeneratedPuzzleGenerationExecutionPolicy(
    val maxAttempts: Int = DEFAULT_MAX_ATTEMPTS,
    val maxSearchWork: Int = DEFAULT_MAX_SEARCH_WORK
) {
    init {
        require(maxAttempts > 0) {
            "Maximum generation attempts must be positive."
        }
        require(maxSearchWork > 0) {
            "Maximum generation search work must be positive."
        }
    }

    private companion object {
        const val DEFAULT_MAX_ATTEMPTS = 50
        const val DEFAULT_MAX_SEARCH_WORK = 250_000
    }
}

data class GeneratedPuzzleGenerationRequest(
    val profile: GeneratedPuzzleProfile,
    val seed: Int,
    val executionPolicy: GeneratedPuzzleGenerationExecutionPolicy = GeneratedPuzzleGenerationExecutionPolicy()
) {
    val profileId: GeneratedPuzzleProfileId
        get() = profile.id
}

fun interface GeneratedPuzzleGenerationCancellation {
    fun isCancellationRequested(): Boolean

    companion object {
        val None = GeneratedPuzzleGenerationCancellation { false }
    }
}

sealed interface GeneratedPairsPuzzleGenerationOutcome {
    val request: GeneratedPuzzleGenerationRequest
    val attemptsUsed: Int
    val searchWorkConsumed: Int

    data class Generated(
        override val request: GeneratedPuzzleGenerationRequest,
        val puzzle: GeneratedPairsPuzzle,
        override val attemptsUsed: Int,
        override val searchWorkConsumed: Int
    ) : GeneratedPairsPuzzleGenerationOutcome

    data class Failed(
        override val request: GeneratedPuzzleGenerationRequest,
        override val attemptsUsed: Int,
        override val searchWorkConsumed: Int,
        val reason: GeneratedPairsPuzzleGenerationFailureReason,
        val candidateRejections: List<GeneratedPairsPuzzleCandidateRejection>
    ) : GeneratedPairsPuzzleGenerationOutcome
}

sealed interface GeneratedPairsPuzzleGenerationFailureReason {
    data object AttemptsExhausted : GeneratedPairsPuzzleGenerationFailureReason
    data object SearchBudgetExhausted : GeneratedPairsPuzzleGenerationFailureReason
    data object DifficultyAssessmentWorkLimitReached : GeneratedPairsPuzzleGenerationFailureReason
    data object Cancelled : GeneratedPairsPuzzleGenerationFailureReason
}

sealed interface GeneratedPairsPuzzleCandidateRejection {
    val attempt: Int

    data class ValuePairSelectionFailed(override val attempt: Int) : GeneratedPairsPuzzleCandidateRejection

    data class StripMaskSelectionFailed(override val attempt: Int) : GeneratedPairsPuzzleCandidateRejection

    data class FinalValidationFailed(
        override val attempt: Int,
        val violations: List<GeneratedPairsPuzzleValidationViolation>
    ) : GeneratedPairsPuzzleCandidateRejection {
        init {
            require(violations.isNotEmpty()) {
                "A final validation rejection requires at least one violation."
            }
        }
    }

    data class DifficultyAssessmentRejected(
        override val attempt: Int,
        val evaluation: GeneratedPuzzleDifficultyPolicyEvaluation
    ) : GeneratedPairsPuzzleCandidateRejection {
        init {
            require(!evaluation.isAccepted) {
                "A difficulty-assessment rejection requires unmet policy expectations."
            }
        }
    }

    data class DifficultyAssessmentUnavailable(
        override val attempt: Int,
        val outcome: GeneratedPuzzleDifficultyAssessmentOutcome
    ) : GeneratedPairsPuzzleCandidateRejection {
        init {
            require(outcome is GeneratedPuzzleDifficultyAssessmentOutcome.Unsatisfiable) {
                "Only an unsatisfiable assessment can be recorded as unavailable."
            }
        }
    }
}
