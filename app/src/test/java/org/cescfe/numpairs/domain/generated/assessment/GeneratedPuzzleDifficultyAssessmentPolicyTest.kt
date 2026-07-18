package org.cescfe.numpairs.domain.generated.assessment

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class GeneratedPuzzleDifficultyAssessmentPolicyTest {
    @Test
    fun policy_reports_each_unmet_difficulty_expectation_without_requiring_uniqueness() {
        val policy = GeneratedPuzzleDifficultyAssessmentPolicy(
            executionPolicy = GeneratedPuzzleDifficultyAssessmentExecutionPolicy(validSolutionCountLimit = 20),
            minimumInitialPlausibleCandidateCount = 6,
            minimumInitialForcedDeductionCount = 1,
            maximumInitialForcedDeductionCount = 1,
            minimumFirstForcedDeductionDepth = 1,
            minimumPlausibleDecoyCount = 1,
            minimumMaximumBranchingFactor = 2,
            minimumExploredAmbiguousStateCount = 1,
            minimumValidSolutionCount = 1
        )
        val acceptedReport = report(
            initialPlausibleCandidateCount = 6,
            initialForcedDeductionCount = 1,
            firstForcedDeductionDepth = 1,
            plausibleDecoyCount = 1,
            validSolutionCount = 2,
            maximumBranchingFactor = 2,
            exploredAmbiguousStateCount = 1
        )

        assertTrue(policy.evaluate(acceptedReport).isAccepted)
        assertEquals(
            setOf(
                GeneratedPuzzleDifficultyRequirement.INITIAL_PLAUSIBLE_CANDIDATES,
                GeneratedPuzzleDifficultyRequirement.INITIAL_FORCED_DEDUCTION_MAXIMUM,
                GeneratedPuzzleDifficultyRequirement.FIRST_FORCED_DEDUCTION_DEPTH,
                GeneratedPuzzleDifficultyRequirement.PLAUSIBLE_DECOYS,
                GeneratedPuzzleDifficultyRequirement.MAXIMUM_BRANCHING_FACTOR,
                GeneratedPuzzleDifficultyRequirement.EXPLORED_AMBIGUOUS_STATES,
                GeneratedPuzzleDifficultyRequirement.VALID_SOLUTIONS
            ),
            policy.evaluate(
                report(
                    initialPlausibleCandidateCount = 5,
                    initialForcedDeductionCount = 2,
                    firstForcedDeductionDepth = null,
                    plausibleDecoyCount = 0,
                    validSolutionCount = 0,
                    maximumBranchingFactor = 0,
                    exploredAmbiguousStateCount = 0
                )
            ).unmetRequirements
        )
    }
}

private fun report(
    initialPlausibleCandidateCount: Int,
    initialForcedDeductionCount: Int,
    firstForcedDeductionDepth: Int?,
    plausibleDecoyCount: Int,
    validSolutionCount: Int,
    maximumBranchingFactor: Int,
    exploredAmbiguousStateCount: Int
): GeneratedPuzzleDifficultyAssessmentReport = GeneratedPuzzleDifficultyAssessmentReport(
    initialPlausibleCandidateCount = initialPlausibleCandidateCount,
    initialForcedDeductionCount = initialForcedDeductionCount,
    forcedDeductionCount = initialForcedDeductionCount,
    firstForcedDeductionDepth = firstForcedDeductionDepth,
    maximumBranchingFactor = maximumBranchingFactor,
    exploredAmbiguousStateCount = exploredAmbiguousStateCount,
    boundedValidSolutionCount = validSolutionCount,
    isValidSolutionCountLimitReached = false,
    structuralObservations = GeneratedPuzzleStructuralObservations(
        knownEntryCount = 3,
        longestHiddenRun = 3,
        knownStripAnchorCount = 0,
        unambiguousResultAnchorCount = 0,
        repeatedValueGroupCountRange = 0..1,
        plausibleDecoyCount = plausibleDecoyCount
    )
)
