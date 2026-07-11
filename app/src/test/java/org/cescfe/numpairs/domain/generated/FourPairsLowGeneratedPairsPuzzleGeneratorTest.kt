package org.cescfe.numpairs.domain.generated

import org.cescfe.numpairs.domain.puzzle.assignment.IndexedResolvedTileAssignment
import org.cescfe.numpairs.domain.puzzle.assignment.UnorderedStripEntryPair
import org.cescfe.numpairs.domain.puzzle.assignment.resolvedTileAssignments
import org.cescfe.numpairs.domain.puzzle.model.Operator
import org.cescfe.numpairs.domain.puzzle.model.Puzzle
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
        val generatedPuzzle = GeneratedPairsPuzzleGenerator(
            profile = profile,
            seed = 42
        ).generateWithSolution()
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
        val firstPuzzle = GeneratedPairsPuzzleGenerator(
            profile = profile,
            seed = 1234
        ).generateWithSolution()
        val secondPuzzle = GeneratedPairsPuzzleGenerator(
            profile = profile,
            seed = 1234
        ).generateWithSolution()

        assertEquals(firstPuzzle, secondPuzzle)
    }

    @Test
    fun solved_puzzle_uses_matching_addition_and_multiplication_pairs() {
        val profile = GeneratedPuzzleProfiles.FOUR_PAIRS_LOW
        val solvedPuzzle = GeneratedPairsPuzzleGenerator(
            profile = profile,
            seed = 81
        ).generateWithSolution().solvedPuzzle
        val additionPairs = solvedPuzzle.pairKeysFor(operator = Operator.ADDITION)
        val multiplicationPairs = solvedPuzzle.pairKeysFor(operator = Operator.MULTIPLICATION)

        assertEquals(profile.size.pairCount, additionPairs.size)
        assertEquals(additionPairs, multiplicationPairs)
        assertEquals(PuzzleCompletionState.SOLVED, solvedPuzzle.completionState)
    }

    @Test
    fun known_strip_anchors_are_distributed_and_belong_to_different_pairs() {
        val profile = GeneratedPuzzleProfiles.FOUR_PAIRS_LOW
        val generatedPuzzle = GeneratedPairsPuzzleGenerator(
            profile = profile,
            seed = 99
        ).generateWithSolution()
        val initialKnownEntryIds = generatedPuzzle.initialPuzzle.knownEntryIds()
        val pairKeyByEntryId = generatedPuzzle.solvedPuzzle.pairKeyByEntryId()

        assertTrue(initialKnownEntryIds.size in profile.initialStripMaskPolicy.knownEntryCountRange)
        assertTrue(profile.requiredHighestStripEntryId in initialKnownEntryIds)
        assertTrue(
            initialKnownEntryIds.maxConsecutiveHiddenEntries(
                totalEntryCount = profile.size.stripEntryCount
            ) <= profile.initialStripMaskPolicy.maxConsecutiveHiddenEntries
        )
        assertEquals(
            initialKnownEntryIds.size,
            initialKnownEntryIds.map { entryId -> pairKeyByEntryId.getValue(entryId) }.toSet().size
        )
    }
}

private fun Puzzle.pairKeysFor(operator: Operator): Set<UnorderedStripEntryPair> = resolvedTileAssignments()
    .filter { assignment -> assignment.operator == operator }
    .map(IndexedResolvedTileAssignment::pairKey)
    .toSet()

private fun Puzzle.pairKeyByEntryId(): Map<Int, UnorderedStripEntryPair> = resolvedTileAssignments()
    .filter { assignment -> assignment.operator == Operator.ADDITION }
    .flatMap { assignment ->
        val pairKey = assignment.pairKey

        listOf(
            assignment.leftOperand.stripEntryId.value to pairKey,
            assignment.rightOperand.stripEntryId.value to pairKey
        )
    }
    .toMap()

private val IndexedResolvedTileAssignment.pairKey: UnorderedStripEntryPair
    get() = UnorderedStripEntryPair.of(
        firstEntryId = leftOperand.stripEntryId,
        secondEntryId = rightOperand.stripEntryId
    )
