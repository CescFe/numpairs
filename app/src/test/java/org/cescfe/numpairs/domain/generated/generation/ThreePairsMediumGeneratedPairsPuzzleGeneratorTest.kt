package org.cescfe.numpairs.domain.generated.generation

import kotlin.math.abs
import org.cescfe.numpairs.domain.generated.assessment.GeneratedPairsDifficultyAssessor
import org.cescfe.numpairs.domain.generated.assessment.GeneratedPuzzleDifficultyAssessmentOutcome
import org.cescfe.numpairs.domain.generated.profile.GeneratedPuzzleProfiles
import org.cescfe.numpairs.domain.generated.profile.ProbabilityPercent
import org.cescfe.numpairs.domain.generated.puzzle.GeneratedPairsPuzzle
import org.cescfe.numpairs.domain.puzzle.model.StripItem
import org.cescfe.numpairs.domain.puzzle.model.Tile
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class ThreePairsMediumGeneratedPairsPuzzleGeneratorTest {
    @Test
    fun three_pairs_medium_generation_satisfies_the_documented_profile() {
        val generatedPuzzle = generatedPuzzle(
            profile = GeneratedPuzzleProfiles.THREE_PAIRS_MEDIUM,
            seed = 42
        )

        assertDocumentedProfile(generatedPuzzle)
    }

    private fun assertDocumentedProfile(generatedPuzzle: GeneratedPairsPuzzle) {
        val profile = GeneratedPuzzleProfiles.THREE_PAIRS_MEDIUM
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

        assertEquals(3, profile.size.pairCount)
        assertEquals(6, solvedPuzzle.strip.entries.size)
        assertEquals(6, solvedPuzzle.board.tiles.size)
        assertEquals(solvedValues.sorted(), solvedValues)
        assertTrue(solvedValues.all { value -> value in 1..30 })
        assertTrue(
            solvedValues.groupingBy { value -> value }.eachCount().values
                .all { occurrenceCount -> occurrenceCount <= 2 }
        )
        assertTrue(repeatedValueGroupCount <= 1)
        assertTrue(solvedPuzzle.multiplicationTiles().all { tile -> tile.result <= 225 })
        assertEquals(6, solvedPuzzle.board.tiles.map(Tile::result).toSet().size)
        assertEquals(1, productAnchorCount)

        assertEquals(2, initialPuzzle.knownEntryIds().size)
        assertEquals(4, initialPuzzle.strip.entries.count { entry -> entry.item == StripItem.Hidden })
        assertTrue(initialPuzzle.knownEntryIds().maxConsecutiveHiddenEntries(6) <= 3)
        assertGeneratedInitialPuzzleStructure(puzzle = initialPuzzle, profile = profile)
    }

    @Test
    fun three_pairs_medium_generation_is_deterministic_for_the_same_seed() {
        val profile = GeneratedPuzzleProfiles.THREE_PAIRS_MEDIUM

        assertEquals(
            generatedPuzzle(profile = profile, seed = 1234),
            generatedPuzzle(profile = profile, seed = 1234)
        )
    }

    @Test
    fun three_pairs_medium_meets_documented_population_targets() {
        val profile = GeneratedPuzzleProfiles.THREE_PAIRS_MEDIUM
        val generatedPuzzles = CORPUS_SEEDS.map { seed ->
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
            puzzle.solvedPuzzle.multiplicationTiles().count(Tile::isPrimeProductDecoy) ==
                decoyTarget.targetPairCount
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
    fun corpus_characterizes_reliable_generation_and_medium_difficulty_assessment() {
        val profile = GeneratedPuzzleProfiles.THREE_PAIRS_MEDIUM
        val assessmentPolicy = requireNotNull(profile.difficultyAssessmentPolicy)
        val assessor = GeneratedPairsDifficultyAssessor()
        val observations = CORPUS_SEEDS.map { seed ->
            val outcome = GeneratedPairsPuzzleGenerator(profile).generate(
                request = GeneratedPuzzleGenerationRequest(profile = profile, seed = seed)
            )
            val generated = checkNotNull(outcome as? GeneratedPairsPuzzleGenerationOutcome.Generated) {
                "Expected generated puzzle for seed $seed, but received $outcome."
            }
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
            MediumCorpusObservation(
                attemptsUsed = generated.attemptsUsed,
                searchWorkConsumed = generated.searchWorkConsumed,
                assessmentWorkConsumed = assessed.workConsumed,
                initialPlausibleCandidateCount = assessed.report.initialPlausibleCandidateCount,
                initialForcedDeductionCount = assessed.report.initialForcedDeductionCount,
                forcedDeductionCount = assessed.report.forcedDeductionCount,
                plausibleDecoyCount = assessed.report.structuralObservations.plausibleDecoyCount,
                boundedValidSolutionCount = assessed.report.boundedValidSolutionCount,
                knownStripAnchorCount = assessed.report.structuralObservations.knownStripAnchorCount,
                unambiguousResultAnchorCount =
                assessed.report.structuralObservations.unambiguousResultAnchorCount,
                longestHiddenRun = assessed.report.structuralObservations.longestHiddenRun
            )
        }

        assertEquals(
            MediumCorpusCharacterization(
                attemptsUsed = 1..160,
                searchWorkConsumed = 18..6344,
                assessmentWorkConsumed = 468..470,
                initialPlausibleCandidateCount = 3..6,
                initialForcedDeductionCount = 1..3,
                forcedDeductionCount = 2..3,
                plausibleDecoyCount = 0..3,
                boundedValidSolutionCount = 1..2,
                knownStripAnchorCount = 0..0,
                unambiguousResultAnchorCount = 1..6,
                longestHiddenRun = 2..3
            ),
            observations.characterization()
        )
        val lowOpeningCandidateAverage = CORPUS_SEEDS.map { seed ->
            val lowPuzzle = generatedPuzzle(profile = GeneratedPuzzleProfiles.THREE_PAIRS_LOW, seed = seed)
            val lowAssessment = assessor.assess(
                initialPuzzle = lowPuzzle.initialPuzzle,
                profile = GeneratedPuzzleProfiles.THREE_PAIRS_LOW
            ) as GeneratedPuzzleDifficultyAssessmentOutcome.Assessed
            lowAssessment.report.initialPlausibleCandidateCount
        }.average()
        assertTrue(
            observations.map(MediumCorpusObservation::initialPlausibleCandidateCount).average() >
                lowOpeningCandidateAverage
        )
        assertTrue(observations.map(MediumCorpusObservation::initialForcedDeductionCount).average() < 3.0)
    }

    @Test
    fun three_pairs_medium_preserves_bounded_failure_and_cancellation() {
        val profile = GeneratedPuzzleProfiles.THREE_PAIRS_MEDIUM
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
        val actualPercentage = actualCount * 100.0 / CORPUS_SEEDS.count()
        assertTrue(
            "Expected ${targetPercentage.value}% within ±$VARIETY_TOLERANCE_PERCENTAGE_POINTS points, " +
                "but observed $actualPercentage% ($actualCount/${CORPUS_SEEDS.count()}).",
            abs(actualPercentage - targetPercentage.value) <= VARIETY_TOLERANCE_PERCENTAGE_POINTS
        )
    }

    private companion object {
        const val VARIETY_TOLERANCE_PERCENTAGE_POINTS = 5
        val CORPUS_SEEDS = 1..500
    }
}

private data class MediumCorpusObservation(
    val attemptsUsed: Int,
    val searchWorkConsumed: Int,
    val assessmentWorkConsumed: Int,
    val initialPlausibleCandidateCount: Int,
    val initialForcedDeductionCount: Int,
    val forcedDeductionCount: Int,
    val plausibleDecoyCount: Int,
    val boundedValidSolutionCount: Int,
    val knownStripAnchorCount: Int,
    val unambiguousResultAnchorCount: Int,
    val longestHiddenRun: Int
)

private data class MediumCorpusCharacterization(
    val attemptsUsed: IntRange,
    val searchWorkConsumed: IntRange,
    val assessmentWorkConsumed: IntRange,
    val initialPlausibleCandidateCount: IntRange,
    val initialForcedDeductionCount: IntRange,
    val forcedDeductionCount: IntRange,
    val plausibleDecoyCount: IntRange,
    val boundedValidSolutionCount: IntRange,
    val knownStripAnchorCount: IntRange,
    val unambiguousResultAnchorCount: IntRange,
    val longestHiddenRun: IntRange
)

private fun List<MediumCorpusObservation>.characterization(): MediumCorpusCharacterization =
    MediumCorpusCharacterization(
        attemptsUsed = minOf(MediumCorpusObservation::attemptsUsed)..maxOf(MediumCorpusObservation::attemptsUsed),
        searchWorkConsumed =
        minOf(MediumCorpusObservation::searchWorkConsumed)..maxOf(MediumCorpusObservation::searchWorkConsumed),
        assessmentWorkConsumed =
        minOf(MediumCorpusObservation::assessmentWorkConsumed)..maxOf(
            MediumCorpusObservation::assessmentWorkConsumed
        ),
        initialPlausibleCandidateCount =
        minOf(MediumCorpusObservation::initialPlausibleCandidateCount)..maxOf(
            MediumCorpusObservation::initialPlausibleCandidateCount
        ),
        initialForcedDeductionCount =
        minOf(MediumCorpusObservation::initialForcedDeductionCount)..maxOf(
            MediumCorpusObservation::initialForcedDeductionCount
        ),
        forcedDeductionCount =
        minOf(MediumCorpusObservation::forcedDeductionCount)..maxOf(MediumCorpusObservation::forcedDeductionCount),
        plausibleDecoyCount =
        minOf(MediumCorpusObservation::plausibleDecoyCount)..maxOf(MediumCorpusObservation::plausibleDecoyCount),
        boundedValidSolutionCount =
        minOf(MediumCorpusObservation::boundedValidSolutionCount)..maxOf(
            MediumCorpusObservation::boundedValidSolutionCount
        ),
        knownStripAnchorCount =
        minOf(MediumCorpusObservation::knownStripAnchorCount)..maxOf(
            MediumCorpusObservation::knownStripAnchorCount
        ),
        unambiguousResultAnchorCount =
        minOf(MediumCorpusObservation::unambiguousResultAnchorCount)..maxOf(
            MediumCorpusObservation::unambiguousResultAnchorCount
        ),
        longestHiddenRun =
        minOf(MediumCorpusObservation::longestHiddenRun)..maxOf(MediumCorpusObservation::longestHiddenRun)
    )
