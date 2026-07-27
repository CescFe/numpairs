package org.cescfe.numpairs.domain.generated.generation

import org.cescfe.numpairs.domain.generated.assessment.GeneratedPairsDifficultyAssessor
import org.cescfe.numpairs.domain.generated.assessment.GeneratedPuzzleDifficultyAssessmentOutcome
import org.cescfe.numpairs.domain.generated.profile.GeneratedPuzzleProfiles
import org.cescfe.numpairs.domain.generated.puzzle.GeneratedPairsPuzzle
import org.cescfe.numpairs.domain.puzzle.model.StripItem
import org.cescfe.numpairs.domain.puzzle.model.Tile
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class ThreePairsLowGeneratedPairsPuzzleGeneratorTest {
    @Test
    fun three_pairs_low_generation_satisfies_the_documented_profile() {
        val generatedPuzzle = generatedPuzzle(profile = GeneratedPuzzleProfiles.THREE_PAIRS_LOW, seed = 42)

        assertDocumentedProfile(generatedPuzzle)
    }

    private fun assertDocumentedProfile(generatedPuzzle: GeneratedPairsPuzzle) {
        val profile = GeneratedPuzzleProfiles.THREE_PAIRS_LOW
        val solvedPuzzle = generatedPuzzle.solvedPuzzle
        val initialPuzzle = generatedPuzzle.initialPuzzle
        val solvedValues = solvedPuzzle.requireKnownStripValues()

        assertEquals(3, profile.size.pairCount)
        assertEquals(6, solvedPuzzle.strip.entries.size)
        assertEquals(6, solvedPuzzle.board.tiles.size)
        assertEquals(solvedValues.sorted(), solvedValues)
        assertTrue(solvedValues.all { value -> value in 2..15 })
        assertEquals(solvedValues.size, solvedValues.toSet().size)
        assertFalse(1 in solvedValues)
        assertTrue(solvedPuzzle.multiplicationTiles().all { tile -> tile.result <= 100 })
        assertEquals(6, solvedPuzzle.board.tiles.map(Tile::result).toSet().size)

        assertEquals(2, initialPuzzle.knownEntryIds().size)
        assertEquals(4, initialPuzzle.strip.entries.count { entry -> entry.item == StripItem.Hidden })
        assertTrue(profile.requiredHighestStripEntryId in initialPuzzle.knownEntryIds())
        assertEquals(2, initialPuzzle.knownEntryIds().distinctSolutionPairCount(generatedPuzzle))
        assertTrue(initialPuzzle.knownEntryIds().maxConsecutiveHiddenEntries(6) <= 2)
        assertGeneratedInitialPuzzleStructure(puzzle = initialPuzzle, profile = profile)
    }

    @Test
    fun three_pairs_low_generation_is_deterministic_for_the_same_seed() {
        val profile = GeneratedPuzzleProfiles.THREE_PAIRS_LOW

        assertEquals(
            generatedPuzzle(profile = profile, seed = 1234),
            generatedPuzzle(profile = profile, seed = 1234)
        )
    }

    @Test
    fun corpus_characterizes_reliable_generation_and_low_difficulty_assessment() {
        val profile = GeneratedPuzzleProfiles.THREE_PAIRS_LOW
        val assessmentPolicy = requireNotNull(profile.difficultyAssessmentPolicy)
        val assessor = GeneratedPairsDifficultyAssessor()
        val observations = CORPUS_SEEDS.map { seed ->
            val outcome = GeneratedPairsPuzzleGenerator(profile).generate(
                request = GeneratedPuzzleGenerationRequest(profile = profile, seed = seed)
            )
            val generated = outcome as GeneratedPairsPuzzleGenerationOutcome.Generated
            assertDocumentedProfile(generated.puzzle)
            val assessed = assessor.assess(
                initialPuzzle = generated.puzzle.initialPuzzle,
                profile = profile,
                executionPolicy = assessmentPolicy.executionPolicy
            ) as GeneratedPuzzleDifficultyAssessmentOutcome.Assessed

            assertTrue(
                "Assessment policy must accept seed $seed.",
                assessmentPolicy.evaluate(assessed.report).isAccepted
            )
            assertFalse(
                "Assessment solution count must stay below the configured limit for seed $seed.",
                assessed.report.isValidSolutionCountLimitReached
            )
            CorpusObservation(
                attemptsUsed = generated.attemptsUsed,
                searchWorkConsumed = generated.searchWorkConsumed,
                assessmentWorkConsumed = assessed.workConsumed,
                initialPlausibleCandidateCount = assessed.report.initialPlausibleCandidateCount,
                initialForcedDeductionCount = assessed.report.initialForcedDeductionCount,
                forcedDeductionCount = assessed.report.forcedDeductionCount,
                boundedValidSolutionCount = assessed.report.boundedValidSolutionCount,
                knownStripAnchorCount = assessed.report.structuralObservations.knownStripAnchorCount,
                unambiguousResultAnchorCount =
                    assessed.report.structuralObservations.unambiguousResultAnchorCount,
                longestHiddenRun = assessed.report.structuralObservations.longestHiddenRun
            )
        }

        assertEquals(
            CorpusCharacterization(
                attemptsUsed = 1..6,
                searchWorkConsumed = 8..80,
                assessmentWorkConsumed = 108..108,
                initialPlausibleCandidateCount = 3..5,
                initialForcedDeductionCount = 3..3,
                forcedDeductionCount = 3..3,
                boundedValidSolutionCount = 1..1,
                knownStripAnchorCount = 1..1,
                unambiguousResultAnchorCount = 2..6,
                longestHiddenRun = 2..2
            ),
            observations.characterization()
        )
    }

    @Test
    fun three_pairs_low_preserves_bounded_failure_and_cancellation() {
        val profile = GeneratedPuzzleProfiles.THREE_PAIRS_LOW
        val budgetFailure = GeneratedPairsPuzzleGenerator(profile).generate(
            request = GeneratedPuzzleGenerationRequest(
                profile = profile,
                seed = 2026,
                executionPolicy = GeneratedPuzzleGenerationExecutionPolicy(
                    maxAttempts = 1,
                    maxSearchWork = 1
                )
            )
        ) as GeneratedPairsPuzzleGenerationOutcome.Failed
        assertEquals(GeneratedPairsPuzzleGenerationFailureReason.SearchBudgetExhausted, budgetFailure.reason)

        val cancellation = GeneratedPairsPuzzleGenerator(profile).generate(
            request = GeneratedPuzzleGenerationRequest(profile = profile, seed = 2026),
            cancellation = { true }
        ) as GeneratedPairsPuzzleGenerationOutcome.Failed
        assertEquals(GeneratedPairsPuzzleGenerationFailureReason.Cancelled, cancellation.reason)
        assertEquals(0, cancellation.searchWorkConsumed)
    }

    private companion object {
        val CORPUS_SEEDS = 1..500
    }
}

private data class CorpusObservation(
    val attemptsUsed: Int,
    val searchWorkConsumed: Int,
    val assessmentWorkConsumed: Int,
    val initialPlausibleCandidateCount: Int,
    val initialForcedDeductionCount: Int,
    val forcedDeductionCount: Int,
    val boundedValidSolutionCount: Int,
    val knownStripAnchorCount: Int,
    val unambiguousResultAnchorCount: Int,
    val longestHiddenRun: Int
)

private data class CorpusCharacterization(
    val attemptsUsed: IntRange,
    val searchWorkConsumed: IntRange,
    val assessmentWorkConsumed: IntRange,
    val initialPlausibleCandidateCount: IntRange,
    val initialForcedDeductionCount: IntRange,
    val forcedDeductionCount: IntRange,
    val boundedValidSolutionCount: IntRange,
    val knownStripAnchorCount: IntRange,
    val unambiguousResultAnchorCount: IntRange,
    val longestHiddenRun: IntRange
)

private fun List<CorpusObservation>.characterization(): CorpusCharacterization = CorpusCharacterization(
    attemptsUsed = minOf(CorpusObservation::attemptsUsed)..maxOf(CorpusObservation::attemptsUsed),
    searchWorkConsumed =
        minOf(CorpusObservation::searchWorkConsumed)..maxOf(CorpusObservation::searchWorkConsumed),
    assessmentWorkConsumed =
        minOf(CorpusObservation::assessmentWorkConsumed)..maxOf(CorpusObservation::assessmentWorkConsumed),
    initialPlausibleCandidateCount =
        minOf(
            CorpusObservation::initialPlausibleCandidateCount
        )..maxOf(CorpusObservation::initialPlausibleCandidateCount),
    initialForcedDeductionCount =
        minOf(CorpusObservation::initialForcedDeductionCount)..maxOf(CorpusObservation::initialForcedDeductionCount),
    forcedDeductionCount =
        minOf(CorpusObservation::forcedDeductionCount)..maxOf(CorpusObservation::forcedDeductionCount),
    boundedValidSolutionCount =
        minOf(CorpusObservation::boundedValidSolutionCount)..maxOf(CorpusObservation::boundedValidSolutionCount),
    knownStripAnchorCount =
        minOf(CorpusObservation::knownStripAnchorCount)..maxOf(CorpusObservation::knownStripAnchorCount),
    unambiguousResultAnchorCount =
        minOf(CorpusObservation::unambiguousResultAnchorCount)..maxOf(CorpusObservation::unambiguousResultAnchorCount),
    longestHiddenRun = minOf(CorpusObservation::longestHiddenRun)..maxOf(CorpusObservation::longestHiddenRun)
)
