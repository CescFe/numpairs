package org.cescfe.numpairs.domain.generated.assessment

import org.cescfe.numpairs.domain.generated.generation.generatedPuzzle
import org.cescfe.numpairs.domain.generated.profile.DifficultyTier
import org.cescfe.numpairs.domain.generated.profile.GeneratedPuzzleProfile
import org.cescfe.numpairs.domain.generated.profile.GeneratedPuzzleProfileDefinition
import org.cescfe.numpairs.domain.generated.profile.GeneratedPuzzleProfileId
import org.cescfe.numpairs.domain.generated.profile.GeneratedPuzzleProfiles
import org.cescfe.numpairs.domain.generated.profile.GeneratedPuzzleSize
import org.cescfe.numpairs.domain.generated.profile.GenerationPolicy
import org.cescfe.numpairs.domain.generated.profile.InitialStripMaskPolicy
import org.cescfe.numpairs.domain.generated.profile.ResultConstraints
import org.cescfe.numpairs.domain.generated.profile.StripKnownEntryDistributionPolicy
import org.cescfe.numpairs.domain.generated.profile.StripValuePolicy
import org.cescfe.numpairs.domain.generated.profile.getOrThrow
import org.cescfe.numpairs.domain.puzzle.model.Board
import org.cescfe.numpairs.domain.puzzle.model.Expression
import org.cescfe.numpairs.domain.puzzle.model.Operator
import org.cescfe.numpairs.domain.puzzle.model.Puzzle
import org.cescfe.numpairs.domain.puzzle.model.Strip
import org.cescfe.numpairs.domain.puzzle.model.StripItem
import org.cescfe.numpairs.domain.puzzle.model.Tile
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotEquals
import org.junit.Assert.assertThrows
import org.junit.Assert.assertTrue
import org.junit.Test

class GeneratedPairsDifficultyAssessorTest {
    private val assessor = GeneratedPairsDifficultyAssessor()

    @Test
    fun canonical_pair_facts_ignore_left_right_order_but_preserve_operator_results() {
        assertEquals(
            GeneratedPairAssessmentFact.canonical(firstOperand = 2, secondOperand = 3),
            GeneratedPairAssessmentFact.canonical(firstOperand = 3, secondOperand = 2)
        )
        assertNotEquals(
            GeneratedPairAssessmentFact.canonical(firstOperand = 1, secondOperand = 5),
            GeneratedPairAssessmentFact.canonical(firstOperand = 2, secondOperand = 3)
        )
        assertThrows(IllegalArgumentException::class.java) {
            GeneratedPairAssessmentFact.canonical(
                firstOperand = 2,
                secondOperand = 3,
                sumResult = 6,
                productResult = 5
            )
        }
    }

    @Test
    fun assessment_is_deterministic_and_ignores_board_action_order() {
        val puzzle = ambiguousPuzzle()
        val reorderedPuzzle = puzzle.copy(
            board = puzzle.board.copy(tiles = puzzle.board.tiles.reversed())
        )

        val first = assessor.assess(initialPuzzle = puzzle, profile = ambiguousProfile())
        val second = assessor.assess(initialPuzzle = puzzle, profile = ambiguousProfile())
        val reordered = assessor.assess(initialPuzzle = reorderedPuzzle, profile = ambiguousProfile())

        assertEquals(first, second)
        assertEquals(first, reordered)
    }

    @Test
    fun assessment_reports_ambiguity_for_distinct_valid_solution_equivalence_classes() {
        val outcome = assessor.assess(
            initialPuzzle = ambiguousPuzzle(),
            profile = ambiguousProfile(),
            executionPolicy = GeneratedPuzzleDifficultyAssessmentExecutionPolicy(
                validSolutionCountLimit = 1
            )
        ) as GeneratedPuzzleDifficultyAssessmentOutcome.Assessed

        assertEquals(1, outcome.report.boundedValidSolutionCount)
        assertTrue(outcome.report.isValidSolutionCountLimitReached)
        assertTrue(outcome.report.initialPlausibleCandidateCount >= 3)
        assertTrue(outcome.report.maximumBranchingFactor >= 2)
        assertTrue(outcome.report.exploredAmbiguousStateCount >= 1)
        assertEquals(0, outcome.report.structuralObservations.knownEntryCount)
        assertEquals(4, outcome.report.structuralObservations.longestHiddenRun)
        assertEquals(1..1, outcome.report.structuralObservations.repeatedValueGroupCountRange)
    }

    @Test
    fun equal_value_entry_permutations_collapse_to_arithmetic_solution_classes() {
        val puzzle = Puzzle(
            board = Board(tiles = listOf(5, 5, 6, 6).map(::hiddenTile)),
            strip = Strip.fromItems(items = List(4) { StripItem.Hidden })
        )

        val outcome = assessor.assess(
            initialPuzzle = puzzle,
            profile = ambiguousProfile()
        ) as GeneratedPuzzleDifficultyAssessmentOutcome.Assessed

        assertEquals(3, outcome.report.boundedValidSolutionCount)
        assertFalse(outcome.report.isValidSolutionCountLimitReached)
    }

    @Test
    fun impossible_candidate_returns_unsatisfiable() {
        val puzzle = ambiguousPuzzle().copy(
            board = Board(
                tiles = listOf(2, 3, 5, 100).map(::hiddenTile)
            )
        )

        val outcome = assessor.assess(initialPuzzle = puzzle, profile = ambiguousProfile())

        assertTrue(outcome is GeneratedPuzzleDifficultyAssessmentOutcome.Unsatisfiable)
    }

    @Test
    fun work_and_cancellation_are_typed_and_bounded() {
        assertThrows(IllegalArgumentException::class.java) {
            GeneratedPuzzleDifficultyAssessmentExecutionPolicy(maxCandidateExpansions = 0)
        }
        assertThrows(IllegalArgumentException::class.java) {
            GeneratedPuzzleDifficultyAssessmentExecutionPolicy(validSolutionCountLimit = 0)
        }
        val workLimited = assessor.assess(
            initialPuzzle = ambiguousPuzzle(),
            profile = ambiguousProfile(),
            executionPolicy = GeneratedPuzzleDifficultyAssessmentExecutionPolicy(
                maxCandidateExpansions = 1
            )
        )
        assertEquals(
            GeneratedPuzzleDifficultyAssessmentOutcome.WorkLimitReached(
                maximumCandidateExpansions = 1,
                workConsumed = 1
            ),
            workLimited
        )

        var cancellationChecks = 0
        val cancelled = assessor.assess(
            initialPuzzle = ambiguousPuzzle(),
            profile = ambiguousProfile(),
            cancellation = GeneratedPuzzleDifficultyAssessmentCancellation {
                cancellationChecks++
                cancellationChecks > 3
            }
        )
        assertTrue(cancelled is GeneratedPuzzleDifficultyAssessmentOutcome.Cancelled)
        assertTrue(cancelled.workConsumed > 0)
    }

    @Test
    fun existing_profiles_are_assessed_over_a_deterministic_seed_corpus() {
        val expectedCharacterization = mapOf(
            ("4-pairs-low" to 1) to Characterization(
                initialCandidates = 4,
                forcedDeductions = 4,
                resultAnchors = 8,
                repeatedGroups = 0..0,
                decoys = 0,
                work = 194
            ),
            ("4-pairs-low" to 42) to Characterization(
                initialCandidates = 5,
                forcedDeductions = 4,
                resultAnchors = 6,
                repeatedGroups = 0..0,
                decoys = 1,
                work = 194
            ),
            ("8-pairs-medium" to 1) to Characterization(
                initialCandidates = 13,
                forcedDeductions = 8,
                resultAnchors = 7,
                repeatedGroups = 4..4,
                decoys = 5,
                work = 4_958
            ),
            ("8-pairs-medium" to 42) to Characterization(
                initialCandidates = 12,
                forcedDeductions = 8,
                resultAnchors = 9,
                repeatedGroups = 0..0,
                decoys = 4,
                work = 4_958
            )
        )
        val corpus = listOf(
            GeneratedPuzzleProfiles.FOUR_PAIRS_LOW to listOf(1, 42),
            GeneratedPuzzleProfiles.EIGHT_PAIRS_MEDIUM to listOf(1, 42)
        )

        corpus.forEach { (profile, seeds) ->
            seeds.forEach { seed ->
                val puzzle = generatedPuzzle(profile = profile, seed = seed).initialPuzzle
                val first = assessor.assess(initialPuzzle = puzzle, profile = profile)
                val second = assessor.assess(initialPuzzle = puzzle, profile = profile)

                assertEquals(first, second)
                val assessed = first as GeneratedPuzzleDifficultyAssessmentOutcome.Assessed
                assertEquals(
                    expectedCharacterization.getValue(profile.id.value to seed),
                    assessed.characterization()
                )
                assertTrue(assessed.report.initialPlausibleCandidateCount >= profile.size.pairCount)
                assertEquals(profile.size.pairCount, assessed.report.initialForcedDeductionCount)
                assertEquals(1, assessed.report.firstForcedDeductionDepth)
                assertEquals(0, assessed.report.maximumBranchingFactor)
                assertEquals(0, assessed.report.exploredAmbiguousStateCount)
                assertTrue(assessed.report.boundedValidSolutionCount >= 1)
                assertFalse(assessed.report.isValidSolutionCountLimitReached)
                assertTrue(
                    assessed.report.structuralObservations.knownEntryCount in
                        profile.initialStripMaskPolicy.knownEntryCountRange
                )
                assertTrue(
                    assessed.report.structuralObservations.longestHiddenRun <=
                        profile.initialStripMaskPolicy.maxConsecutiveHiddenEntries
                )
            }
        }
    }
}

private data class Characterization(
    val initialCandidates: Int,
    val forcedDeductions: Int,
    val resultAnchors: Int,
    val repeatedGroups: IntRange,
    val decoys: Int,
    val work: Int
)

private fun GeneratedPuzzleDifficultyAssessmentOutcome.Assessed.characterization(): Characterization = Characterization(
    initialCandidates = report.initialPlausibleCandidateCount,
    forcedDeductions = report.forcedDeductionCount,
    resultAnchors = report.structuralObservations.unambiguousResultAnchorCount,
    repeatedGroups = report.structuralObservations.repeatedValueGroupCountRange,
    decoys = report.structuralObservations.plausibleDecoyCount,
    work = workConsumed
)

private fun ambiguousProfile(): GeneratedPuzzleProfile = GeneratedPuzzleProfile.create(
    definition = GeneratedPuzzleProfileDefinition(
        id = GeneratedPuzzleProfileId("assessment-ambiguous-two-pairs"),
        difficulty = DifficultyTier.MEDIUM,
        size = GeneratedPuzzleSize(pairCount = 2),
        stripValuePolicy = StripValuePolicy(
            valueRange = 1..6,
            maxOccurrencesPerValue = 2
        ),
        resultConstraints = ResultConstraints(
            maxMultiplicationResult = 36,
            allowsDuplicateBoardResults = true
        ),
        initialStripMaskPolicy = InitialStripMaskPolicy(
            knownEntryCountRange = 0..0,
            requiredAnchors = emptySet(),
            distributionPolicy = StripKnownEntryDistributionPolicy.Unrestricted,
            maxConsecutiveHiddenEntries = 4
        ),
        generationPolicy = GenerationPolicy(isBoardTileShufflingEnabled = true)
    )
).getOrThrow()

private fun ambiguousPuzzle(): Puzzle = Puzzle(
    board = Board(tiles = listOf(2, 3, 5, 6).map(::hiddenTile)),
    strip = Strip.fromItems(items = List(4) { StripItem.Hidden })
)

private fun hiddenTile(result: Int): Tile = Tile(
    expression = Expression(
        leftOperand = Expression.Operand.Hidden,
        operator = Operator.Hidden,
        rightOperand = Expression.Operand.Hidden
    ),
    result = result
)
