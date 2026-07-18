package org.cescfe.numpairs.domain.generated.generation

import kotlin.math.abs
import org.cescfe.numpairs.domain.generated.assessment.GeneratedPairsDifficultyAssessor
import org.cescfe.numpairs.domain.generated.assessment.GeneratedPuzzleDifficultyAssessmentOutcome
import org.cescfe.numpairs.domain.generated.profile.GeneratedPuzzleProfiles
import org.cescfe.numpairs.domain.generated.profile.ProbabilityPercent
import org.cescfe.numpairs.domain.puzzle.model.StripItem
import org.cescfe.numpairs.domain.puzzle.model.Tile
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class EightPairsHardGeneratedPairsPuzzleGeneratorTest {
    @Test
    fun eight_pairs_hard_generation_satisfies_the_documented_profile() {
        val profile = GeneratedPuzzleProfiles.EIGHT_PAIRS_HARD
        val puzzle = generatedPuzzle(profile = profile, seed = 42)
        val solvedValues = puzzle.solvedPuzzle.requireKnownStripValues()
        val repeatedValueGroupCount = solvedValues.repeatedValueGroupCount()
        val anchorMix = requireNotNull(profile.resultConstraints.productAnchorMix)
        val productAnchorCount = puzzle.solvedPuzzle.multiplicationTiles().count { tile ->
            tile.result > anchorMix.productResultGreaterThan
        }
        val knownEntryIds = puzzle.initialPuzzle.knownEntryIds()

        assertTrue(solvedValues.all { value -> value in 1..99 })
        assertTrue(
            solvedValues.groupingBy { value -> value }.eachCount().values
                .all { occurrenceCount -> occurrenceCount <= 2 }
        )
        assertTrue(repeatedValueGroupCount in 1..2)
        assertTrue(puzzle.solvedPuzzle.multiplicationTiles().all { tile -> tile.result <= 1000 })
        assertEquals(16, puzzle.solvedPuzzle.board.tiles.map(Tile::result).toSet().size)
        assertTrue(productAnchorCount in 0..1)
        assertTrue(knownEntryIds.size in 4..5)
        assertTrue(puzzle.initialPuzzle.strip.entries.count { entry -> entry.item == StripItem.Hidden } in 11..12)
        assertTrue(knownEntryIds.distinctSolutionPairCount(puzzle) >= 3)
        assertTrue(knownEntryIds.maxConsecutiveHiddenEntries(16) <= 5)
    }

    @Test
    fun eight_pairs_hard_generation_is_deterministic_for_the_same_seed() {
        val profile = GeneratedPuzzleProfiles.EIGHT_PAIRS_HARD

        assertEquals(
            generatedPuzzle(profile = profile, seed = 1234),
            generatedPuzzle(profile = profile, seed = 1234)
        )
    }

    @Test
    fun hard_generation_meets_population_targets_and_stays_within_execution_bounds() {
        val profile = GeneratedPuzzleProfiles.EIGHT_PAIRS_HARD
        val generatedOutcomes = (1..VARIETY_SAMPLE_SIZE).map { seed ->
            val outcome = GeneratedPairsPuzzleGenerator(profile).generate(
                request = GeneratedPuzzleGenerationRequest(profile = profile, seed = seed)
            )
            check(outcome is GeneratedPairsPuzzleGenerationOutcome.Generated) {
                "Hard generation failed for seed $seed: $outcome"
            }
            outcome
        }
        assertTrue(generatedOutcomes.all { outcome -> outcome.attemptsUsed <= 160 })
        assertTrue(generatedOutcomes.all { outcome -> outcome.searchWorkConsumed <= 600_000 })
        assertTrue(
            generatedOutcomes.all { outcome ->
                outcome.puzzle.solvedPuzzle.requireKnownStripValues().repeatedValueGroupCount() in 1..2
            }
        )

        val decoyTarget = requireNotNull(profile.varietyPolicy.primeProductDecoyTarget)
        val primeProductDecoyPuzzleCount = generatedOutcomes.count { outcome ->
            outcome.puzzle.solvedPuzzle.multiplicationTiles().count(Tile::isPrimeProductDecoy) ==
                decoyTarget.targetPairCount
        }
        assertFrequencyWithinTarget(
            actualCount = primeProductDecoyPuzzleCount,
            targetPercentage = decoyTarget.targetPuzzlePercent
        )
        profile.varietyPolicy.highValueMaskTargets.forEach { target ->
            val targetEntryIndex = profile.size.stripEntryCount - target.rankFromHighest
            val hiddenPuzzleCount = generatedOutcomes.count { outcome ->
                outcome.puzzle.initialPuzzle.strip.entries[targetEntryIndex].item == StripItem.Hidden
            }
            assertFrequencyWithinTarget(
                actualCount = hiddenPuzzleCount,
                targetPercentage = target.targetHiddenProbability
            )
        }
    }

    @Test
    fun every_sampled_hard_puzzle_passes_the_evidence_based_assessment_policy() {
        val hardProfile = GeneratedPuzzleProfiles.EIGHT_PAIRS_HARD
        val hardPolicy = requireNotNull(hardProfile.difficultyAssessmentPolicy)
        val assessor = GeneratedPairsDifficultyAssessor()

        ASSESSMENT_SEEDS.forEach { seed ->
            val puzzle = generatedPuzzle(profile = hardProfile, seed = seed)
            val outcome = assessor.assess(
                initialPuzzle = puzzle.initialPuzzle,
                profile = hardProfile,
                executionPolicy = hardPolicy.executionPolicy
            )
            assertTrue(
                "Assessment must complete for seed $seed.",
                outcome is GeneratedPuzzleDifficultyAssessmentOutcome.Assessed
            )
            val report = (outcome as GeneratedPuzzleDifficultyAssessmentOutcome.Assessed).report
            assertTrue("Hard policy must accept seed $seed.", hardPolicy.evaluate(report).isAccepted)
            assertTrue(report.initialForcedDeductionCount < hardProfile.size.pairCount)
            assertTrue(report.maximumBranchingFactor >= 2)
            assertTrue(report.exploredAmbiguousStateCount >= 1)
        }
    }

    @Test
    fun eight_pairs_hard_preserves_bounded_failure_and_cancellation() {
        val profile = GeneratedPuzzleProfiles.EIGHT_PAIRS_HARD
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

    @Test
    fun hard_generation_handles_a_high_retry_fixture_within_its_profile_budget() {
        val profile = GeneratedPuzzleProfiles.EIGHT_PAIRS_HARD
        val outcome = GeneratedPairsPuzzleGenerator(profile).generate(
            request = GeneratedPuzzleGenerationRequest(profile = profile, seed = 386)
        )

        assertTrue(outcome is GeneratedPairsPuzzleGenerationOutcome.Generated)
        assertTrue(outcome.attemptsUsed <= profile.generationPolicy.maxAttempts)
        assertTrue(outcome.searchWorkConsumed <= profile.generationPolicy.maxSearchWork)
    }

    private fun assertFrequencyWithinTarget(actualCount: Int, targetPercentage: ProbabilityPercent) {
        val actualPercentage = actualCount * 100.0 / VARIETY_SAMPLE_SIZE
        assertTrue(
            "Expected ${targetPercentage.value}% within ±$VARIETY_TOLERANCE_PERCENTAGE_POINTS points, " +
                "but observed $actualPercentage% ($actualCount/$VARIETY_SAMPLE_SIZE).",
            abs(actualPercentage - targetPercentage.value) <= VARIETY_TOLERANCE_PERCENTAGE_POINTS
        )
    }

    private companion object {
        const val VARIETY_SAMPLE_SIZE = 500
        const val VARIETY_TOLERANCE_PERCENTAGE_POINTS = 5
        val ASSESSMENT_SEEDS = 1..50
    }
}

private fun List<Int>.repeatedValueGroupCount(): Int = groupingBy { value -> value }
    .eachCount()
    .values
    .count { occurrenceCount -> occurrenceCount > 1 }
