package org.cescfe.numpairs.domain.generated.generation

import org.cescfe.numpairs.domain.generated.profile.GeneratedPuzzleProfiles
import org.cescfe.numpairs.domain.puzzle.assignment.analyzeResolvedPuzzle
import org.cescfe.numpairs.domain.puzzle.model.Operator
import org.cescfe.numpairs.domain.puzzle.model.PuzzleCompletionState
import org.cescfe.numpairs.domain.puzzle.model.Tile
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class FourPairsLowGeneratedPairsPuzzleGeneratorTest {

    @Test
    fun four_pairs_low_profile_generation_satisfies_profile_constraints() {
        val profile = GeneratedPuzzleProfiles.FOUR_PAIRS_LOW
        val generatedPuzzle = generatedPuzzle(profile = profile, seed = 42)
        val solvedPuzzle = generatedPuzzle.solvedPuzzle
        val initialPuzzle = generatedPuzzle.initialPuzzle
        val solvedStripValues = solvedPuzzle.requireKnownStripValues()
        val initialKnownEntryIds = initialPuzzle.knownEntryIds()

        assertEquals(PuzzleCompletionState.SOLVED, solvedPuzzle.completionState)
        assertEquals(profile.size.pairCount, solvedPuzzle.additionTiles().size)
        assertEquals(profile.size.pairCount, solvedPuzzle.multiplicationTiles().size)
        assertEquals(profile.size.boardTileCount, solvedPuzzle.board.tiles.size)
        assertEquals(profile.size.stripEntryCount, solvedPuzzle.strip.entries.size)

        assertEquals(solvedStripValues.sorted(), solvedStripValues)
        assertTrue(solvedStripValues.all { value -> value in profile.stripValuePolicy.valueRange })
        assertTrue(
            solvedStripValues.groupingBy { value -> value }
                .eachCount()
                .all { (_, occurrenceCount) ->
                    occurrenceCount <= profile.stripValuePolicy.maxOccurrencesPerValue
                }
        )
        if (!profile.stripValuePolicy.allowsOne) {
            assertFalse(1 in solvedStripValues)
        }

        assertTrue(
            solvedPuzzle.multiplicationTiles().all { tile ->
                tile.result <= profile.resultConstraints.maxMultiplicationResult
            }
        )
        if (!profile.resultConstraints.allowsDuplicateBoardResults) {
            assertEquals(
                profile.size.boardTileCount,
                solvedPuzzle.board.tiles.map(Tile::result).toSet().size
            )
        }

        assertEquals(solvedPuzzle.board.tiles.map(Tile::result), initialPuzzle.board.tiles.map(Tile::result))
        assertGeneratedInitialPuzzleStructure(
            puzzle = initialPuzzle,
            profile = profile
        )
        assertTrue(profile.requiredHighestStripEntryId in initialKnownEntryIds)
    }

    @Test
    fun four_pairs_low_profile_generation_is_deterministic_for_the_same_seed() {
        val profile = GeneratedPuzzleProfiles.FOUR_PAIRS_LOW
        val firstPuzzle = generatedPuzzle(profile = profile, seed = 1234)
        val secondPuzzle = generatedPuzzle(profile = profile, seed = 1234)

        assertEquals(firstPuzzle, secondPuzzle)
    }

    @Test
    fun solved_puzzle_uses_matching_addition_and_multiplication_pairs() {
        val profile = GeneratedPuzzleProfiles.FOUR_PAIRS_LOW
        val solvedPuzzle = generatedPuzzle(profile = profile, seed = 81).solvedPuzzle
        val analysis = solvedPuzzle.analyzeResolvedPuzzle()
        val additionPairs = analysis.solutionPairsFor(operator = Operator.ADDITION)
        val multiplicationPairs = analysis.solutionPairsFor(operator = Operator.MULTIPLICATION)

        assertEquals(profile.size.pairCount, additionPairs.size)
        assertEquals(additionPairs, multiplicationPairs)
        assertEquals(PuzzleCompletionState.SOLVED, solvedPuzzle.completionState)
    }

    @Test
    fun known_strip_anchors_are_distributed_and_belong_to_different_pairs() {
        val profile = GeneratedPuzzleProfiles.FOUR_PAIRS_LOW
        val generatedPuzzle = generatedPuzzle(profile = profile, seed = 99)
        val initialKnownEntryIds = generatedPuzzle.initialPuzzle.knownEntryIds()
        val analysis = generatedPuzzle.solvedPuzzle.analyzeResolvedPuzzle()
        val solutionPairByEntryId = analysis.resolvedAssignments
            .filter { assignment -> assignment.operator == Operator.ADDITION }
            .flatMap { assignment ->
                val solutionPair = analysis.solutionPairByTileIndex.getValue(assignment.tileIndex)
                listOf(
                    assignment.leftOperand.stripEntryId.value to solutionPair,
                    assignment.rightOperand.stripEntryId.value to solutionPair
                )
            }.toMap()

        assertTrue(initialKnownEntryIds.size in profile.initialStripMaskPolicy.knownEntryCountRange)
        assertTrue(profile.requiredHighestStripEntryId in initialKnownEntryIds)
        assertTrue(
            initialKnownEntryIds.maxConsecutiveHiddenEntries(
                totalEntryCount = profile.size.stripEntryCount
            ) <= profile.initialStripMaskPolicy.maxConsecutiveHiddenEntries
        )
        assertEquals(
            initialKnownEntryIds.size,
            initialKnownEntryIds.map { entryId -> solutionPairByEntryId.getValue(entryId) }.toSet().size
        )
    }
}
