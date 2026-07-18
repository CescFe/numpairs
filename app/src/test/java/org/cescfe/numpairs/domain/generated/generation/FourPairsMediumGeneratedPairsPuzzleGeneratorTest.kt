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

class FourPairsMediumGeneratedPairsPuzzleGeneratorTest {
    @Test
    fun four_pairs_medium_generation_satisfies_the_documented_profile() {
        val profile = GeneratedPuzzleProfiles.FOUR_PAIRS_MEDIUM
        val generatedPuzzle = generatedPuzzle(profile = profile, seed = 42)
        val solvedPuzzle = generatedPuzzle.solvedPuzzle
        val initialPuzzle = generatedPuzzle.initialPuzzle
        val solvedValues = solvedPuzzle.requireKnownStripValues()
        val repeatedValueGroupCount = solvedValues.groupingBy { value -> value }
            .eachCount()
            .values
            .count { occurrenceCount -> occurrenceCount > 1 }
        val anchorMix = requireNotNull(profile.resultConstraints.productAnchorMix)
        val productAnchorCount = solvedPuzzle.multiplicationTiles().count { tile ->
            tile.result > anchorMix.productResultGreaterThan
        }

        assertTrue(solvedValues.all { value -> value in 1..40 })
        assertTrue(
            solvedValues.groupingBy { value -> value }.eachCount().values
                .all { occurrenceCount -> occurrenceCount <= 2 }
        )
        assertTrue(repeatedValueGroupCount <= 1)
        assertTrue(solvedPuzzle.multiplicationTiles().all { tile -> tile.result <= 400 })
        assertEquals(profile.size.boardTileCount, solvedPuzzle.board.tiles.map(Tile::result).toSet().size)
        assertTrue(productAnchorCount in 1..2)
        assertEquals(3, initialPuzzle.knownEntryIds().size)
        assertEquals(5, initialPuzzle.strip.entries.count { entry -> entry.item == StripItem.Hidden })
        assertTrue(initialPuzzle.knownEntryIds().distinctSolutionPairCount(generatedPuzzle) >= 2)
        assertTrue(initialPuzzle.knownEntryIds().maxConsecutiveHiddenEntries(8) <= 3)
    }

    @Test
    fun four_pairs_medium_generation_is_deterministic_for_the_same_seed() {
        val profile = GeneratedPuzzleProfiles.FOUR_PAIRS_MEDIUM

        assertEquals(
            generatedPuzzle(profile = profile, seed = 1234),
            generatedPuzzle(profile = profile, seed = 1234)
        )
    }

    @Test
    fun four_pairs_medium_meets_documented_population_targets() {
        val profile = GeneratedPuzzleProfiles.FOUR_PAIRS_MEDIUM
        val generatedPuzzles = (1..VARIETY_SAMPLE_SIZE).map { seed ->
            generatedPuzzle(profile = profile, seed = seed)
        }
        val repeatedTarget = requireNotNull(profile.varietyPolicy.repeatedValueGroupTarget)
        val repeatedPuzzleCount = generatedPuzzles.count { puzzle ->
            puzzle.solvedPuzzle.requireKnownStripValues().groupingBy { value -> value }
                .eachCount()
                .values
                .count { occurrenceCount -> occurrenceCount > 1 } == repeatedTarget.targetGroupCount
        }
        val decoyTarget = requireNotNull(profile.varietyPolicy.primeProductDecoyTarget)
        val primeProductDecoyPuzzleCount = generatedPuzzles.count { puzzle ->
            puzzle.solvedPuzzle.multiplicationTiles().count(Tile::isPrimeProductDecoy) == decoyTarget.targetPairCount
        }

        assertFrequencyWithinTarget(
            actualCount = repeatedPuzzleCount,
            targetPercentage = repeatedTarget.targetPuzzlePercent
        )
        assertFrequencyWithinTarget(
            actualCount = primeProductDecoyPuzzleCount,
            targetPercentage = decoyTarget.targetPuzzlePercent
        )
        profile.varietyPolicy.highValueMaskTargets.forEach { target ->
            val targetEntryIndex = profile.size.stripEntryCount - target.rankFromHighest
            val hiddenPuzzleCount = generatedPuzzles.count { puzzle ->
                puzzle.initialPuzzle.strip.entries[targetEntryIndex].item == StripItem.Hidden
            }
            assertFrequencyWithinTarget(
                actualCount = hiddenPuzzleCount,
                targetPercentage = target.targetHiddenProbability
            )
        }
    }

    @Test
    fun every_sampled_medium_puzzle_passes_the_bounded_assessment_policy() {
        val mediumProfile = GeneratedPuzzleProfiles.FOUR_PAIRS_MEDIUM
        val mediumPolicy = requireNotNull(mediumProfile.difficultyAssessmentPolicy)
        val assessor = GeneratedPairsDifficultyAssessor()
        val lowOpeningCandidateMaximum = listOf(1, 42).maxOf { seed ->
            val lowPuzzle = generatedPuzzle(profile = GeneratedPuzzleProfiles.FOUR_PAIRS_LOW, seed = seed)
            val outcome = assessor.assess(
                initialPuzzle = lowPuzzle.initialPuzzle,
                profile = GeneratedPuzzleProfiles.FOUR_PAIRS_LOW
            ) as GeneratedPuzzleDifficultyAssessmentOutcome.Assessed
            outcome.report.initialPlausibleCandidateCount
        }

        ASSESSMENT_SEEDS.forEach { seed ->
            val puzzle = generatedPuzzle(profile = mediumProfile, seed = seed)
            val outcome = assessor.assess(
                initialPuzzle = puzzle.initialPuzzle,
                profile = mediumProfile,
                executionPolicy = mediumPolicy.executionPolicy
            )
            assertTrue(
                "Assessment must complete for seed $seed.",
                outcome is GeneratedPuzzleDifficultyAssessmentOutcome.Assessed
            )
            val report = (outcome as GeneratedPuzzleDifficultyAssessmentOutcome.Assessed).report
            assertTrue("Assessment policy must accept seed $seed.", mediumPolicy.evaluate(report).isAccepted)
            assertTrue(report.initialPlausibleCandidateCount > lowOpeningCandidateMaximum)
        }
    }

    @Test
    fun four_pairs_medium_preserves_bounded_failure_and_cancellation() {
        val profile = GeneratedPuzzleProfiles.FOUR_PAIRS_MEDIUM
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
