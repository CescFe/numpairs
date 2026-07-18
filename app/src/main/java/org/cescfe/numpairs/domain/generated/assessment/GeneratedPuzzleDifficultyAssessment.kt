package org.cescfe.numpairs.domain.generated.assessment

data class GeneratedPuzzleDifficultyAssessmentExecutionPolicy(
    val maxCandidateExpansions: Int = 250_000,
    val validSolutionCountLimit: Int = 100
) {
    init {
        require(maxCandidateExpansions > 0) {
            "Difficulty-assessment candidate expansion limit must be positive."
        }
        require(validSolutionCountLimit > 0) {
            "Difficulty-assessment solution count limit must be positive."
        }
    }
}

fun interface GeneratedPuzzleDifficultyAssessmentCancellation {
    fun isCancelled(): Boolean

    companion object {
        val None: GeneratedPuzzleDifficultyAssessmentCancellation =
            GeneratedPuzzleDifficultyAssessmentCancellation { false }
    }
}

@ConsistentCopyVisibility
data class GeneratedPairAssessmentFact private constructor(
    val firstValue: Int,
    val secondValue: Int,
    val sumResult: Int,
    val productResult: Int
) : Comparable<GeneratedPairAssessmentFact> {
    override fun compareTo(other: GeneratedPairAssessmentFact): Int = compareValuesBy(
        this,
        other,
        GeneratedPairAssessmentFact::firstValue,
        GeneratedPairAssessmentFact::secondValue,
        GeneratedPairAssessmentFact::sumResult,
        GeneratedPairAssessmentFact::productResult
    )

    companion object {
        fun canonical(
            firstOperand: Int,
            secondOperand: Int,
            sumResult: Int = firstOperand + secondOperand,
            productResult: Int = firstOperand * secondOperand
        ): GeneratedPairAssessmentFact {
            require(firstOperand > 0 && secondOperand > 0) {
                "Assessment fact operands must be positive."
            }
            val (firstValue, secondValue) = if (firstOperand <= secondOperand) {
                firstOperand to secondOperand
            } else {
                secondOperand to firstOperand
            }
            require(firstValue.toLong() + secondValue.toLong() == sumResult.toLong()) {
                "Assessment fact sum must match its operands."
            }
            require(firstValue.toLong() * secondValue.toLong() == productResult.toLong()) {
                "Assessment fact product must match its operands."
            }
            return GeneratedPairAssessmentFact(
                firstValue = firstValue,
                secondValue = secondValue,
                sumResult = sumResult,
                productResult = productResult
            )
        }
    }
}

data class GeneratedPuzzleStructuralObservations(
    val knownEntryCount: Int,
    val longestHiddenRun: Int,
    val knownStripAnchorCount: Int,
    val unambiguousResultAnchorCount: Int,
    val repeatedValueGroupCountRange: IntRange,
    val plausibleDecoyCount: Int
)

data class GeneratedPuzzleDifficultyAssessmentReport(
    val initialPlausibleCandidateCount: Int,
    val initialForcedDeductionCount: Int,
    val forcedDeductionCount: Int,
    val firstForcedDeductionDepth: Int?,
    val maximumBranchingFactor: Int,
    val exploredAmbiguousStateCount: Int,
    val boundedValidSolutionCount: Int,
    val isValidSolutionCountLimitReached: Boolean,
    val structuralObservations: GeneratedPuzzleStructuralObservations
)

data class GeneratedPuzzleDifficultyAssessmentPolicy(
    val executionPolicy: GeneratedPuzzleDifficultyAssessmentExecutionPolicy,
    val minimumInitialPlausibleCandidateCount: Int,
    val minimumInitialForcedDeductionCount: Int,
    val minimumFirstForcedDeductionDepth: Int,
    val minimumPlausibleDecoyCount: Int,
    val minimumValidSolutionCount: Int = 1
) {
    init {
        require(minimumInitialPlausibleCandidateCount >= 0)
        require(minimumInitialForcedDeductionCount >= 0)
        require(minimumFirstForcedDeductionDepth >= 0)
        require(minimumPlausibleDecoyCount >= 0)
        require(minimumValidSolutionCount > 0)
        require(executionPolicy.validSolutionCountLimit >= minimumValidSolutionCount) {
            "Difficulty-assessment solution count limit must cover the required minimum."
        }
    }

    fun evaluate(report: GeneratedPuzzleDifficultyAssessmentReport): GeneratedPuzzleDifficultyPolicyEvaluation {
        val unmetRequirements = buildSet {
            if (report.initialPlausibleCandidateCount < minimumInitialPlausibleCandidateCount) {
                add(GeneratedPuzzleDifficultyRequirement.INITIAL_PLAUSIBLE_CANDIDATES)
            }
            if (report.initialForcedDeductionCount < minimumInitialForcedDeductionCount) {
                add(GeneratedPuzzleDifficultyRequirement.INITIAL_FORCED_DEDUCTIONS)
            }
            if ((report.firstForcedDeductionDepth ?: -1) < minimumFirstForcedDeductionDepth) {
                add(GeneratedPuzzleDifficultyRequirement.FIRST_FORCED_DEDUCTION_DEPTH)
            }
            if (report.structuralObservations.plausibleDecoyCount < minimumPlausibleDecoyCount) {
                add(GeneratedPuzzleDifficultyRequirement.PLAUSIBLE_DECOYS)
            }
            if (report.boundedValidSolutionCount < minimumValidSolutionCount) {
                add(GeneratedPuzzleDifficultyRequirement.VALID_SOLUTIONS)
            }
        }
        return GeneratedPuzzleDifficultyPolicyEvaluation(
            report = report,
            unmetRequirements = unmetRequirements
        )
    }
}

data class GeneratedPuzzleDifficultyPolicyEvaluation(
    val report: GeneratedPuzzleDifficultyAssessmentReport,
    val unmetRequirements: Set<GeneratedPuzzleDifficultyRequirement>
) {
    val isAccepted: Boolean
        get() = unmetRequirements.isEmpty()
}

enum class GeneratedPuzzleDifficultyRequirement {
    INITIAL_PLAUSIBLE_CANDIDATES,
    INITIAL_FORCED_DEDUCTIONS,
    FIRST_FORCED_DEDUCTION_DEPTH,
    PLAUSIBLE_DECOYS,
    VALID_SOLUTIONS
}

sealed interface GeneratedPuzzleDifficultyAssessmentOutcome {
    val workConsumed: Int

    data class Assessed(val report: GeneratedPuzzleDifficultyAssessmentReport, override val workConsumed: Int) :
        GeneratedPuzzleDifficultyAssessmentOutcome

    data class Unsatisfiable(
        val initialPlausibleCandidateCount: Int,
        val knownEntryCount: Int,
        val longestHiddenRun: Int,
        override val workConsumed: Int
    ) : GeneratedPuzzleDifficultyAssessmentOutcome

    data class Cancelled(override val workConsumed: Int) : GeneratedPuzzleDifficultyAssessmentOutcome

    data class WorkLimitReached(val maximumCandidateExpansions: Int, override val workConsumed: Int) :
        GeneratedPuzzleDifficultyAssessmentOutcome
}
