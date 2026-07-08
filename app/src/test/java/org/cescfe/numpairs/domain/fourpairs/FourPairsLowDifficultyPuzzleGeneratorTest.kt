package org.cescfe.numpairs.domain.fourpairs

import org.cescfe.numpairs.domain.generated.GeneratedPairsPuzzleGenerator
import org.cescfe.numpairs.domain.generated.GeneratedPuzzleProfiles
import org.cescfe.numpairs.domain.puzzle.assignment.IndexedResolvedTileAssignment
import org.cescfe.numpairs.domain.puzzle.assignment.resolvedTileAssignments
import org.cescfe.numpairs.domain.puzzle.model.Expression
import org.cescfe.numpairs.domain.puzzle.model.Operator
import org.cescfe.numpairs.domain.puzzle.model.Puzzle
import org.cescfe.numpairs.domain.puzzle.model.PuzzleCompletionState
import org.cescfe.numpairs.domain.puzzle.model.StripItem
import org.cescfe.numpairs.domain.puzzle.model.Tile
import org.junit.Assert.assertEquals
import org.junit.Assert.assertThrows
import org.junit.Assert.assertTrue
import org.junit.Test

class FourPairsLowDifficultyPuzzleGeneratorTest {
    @Test
    fun generate_returns_the_initial_player_facing_puzzle() {
        val generatedPuzzle = FourPairsLowDifficultyPuzzleGenerator(seed = 2026).generateWithSolution()
        val initialPuzzle = FourPairsLowDifficultyPuzzleGenerator(seed = 2026).generate()

        assertEquals(generatedPuzzle.initialPuzzle, initialPuzzle)
        assertEquals(PuzzleCompletionState.INCOMPLETE, initialPuzzle.completionState)
        assertInitialPuzzleMask(initialPuzzle)
    }

    @Test
    fun generate_with_solution_delegates_to_the_reusable_four_pairs_low_profile_generator() {
        val generatedPuzzle = FourPairsLowDifficultyPuzzleGenerator(seed = 2026).generateWithSolution()
        val reusableGeneratedPuzzle = GeneratedPairsPuzzleGenerator(
            profile = GeneratedPuzzleProfiles.FOUR_PAIRS_LOW,
            seed = 2026
        ).generateWithSolution()

        assertEquals(reusableGeneratedPuzzle.initialPuzzle, generatedPuzzle.initialPuzzle)
        assertEquals(reusableGeneratedPuzzle.solvedPuzzle, generatedPuzzle.solvedPuzzle)
    }

    @Test
    fun generated_puzzle_satisfies_low_difficulty_constraints() {
        val generatedPuzzle = FourPairsLowDifficultyPuzzleGenerator(seed = 42).generateWithSolution()
        val solvedPuzzle = generatedPuzzle.solvedPuzzle
        val initialPuzzle = generatedPuzzle.initialPuzzle
        val solvedStripValues = solvedPuzzle.requireKnownStripValues()

        assertEquals(PuzzleCompletionState.SOLVED, solvedPuzzle.completionState)
        assertEquals(FourPairsLowDifficultyRules.BOARD_TILE_COUNT, solvedPuzzle.board.tiles.size)
        assertEquals(FourPairsLowDifficultyRules.STRIP_ENTRY_COUNT, solvedPuzzle.strip.entries.size)
        assertEquals(solvedStripValues.sorted(), solvedStripValues)
        assertEquals(solvedStripValues.size, solvedStripValues.toSet().size)
        assertTrue(solvedStripValues.all { value -> value in FourPairsLowDifficultyRules.stripValueRange })
        assertTrue(
            solvedPuzzle.multiplicationTiles().all { tile ->
                tile.result <= FourPairsLowDifficultyRules.MAX_MULTIPLICATION_RESULT
            }
        )
        assertEquals(
            FourPairsLowDifficultyRules.BOARD_TILE_COUNT,
            solvedPuzzle.board.tiles.map(Tile::result).toSet().size
        )
        assertEquals(solvedPuzzle.board.tiles.map(Tile::result), initialPuzzle.board.tiles.map(Tile::result))
        assertInitialPuzzleMask(initialPuzzle)
    }

    @Test
    fun generated_puzzles_are_deterministic_for_the_same_seed() {
        val firstPuzzle = FourPairsLowDifficultyPuzzleGenerator(seed = 1234).generateWithSolution()
        val secondPuzzle = FourPairsLowDifficultyPuzzleGenerator(seed = 1234).generateWithSolution()

        assertEquals(firstPuzzle, secondPuzzle)
    }

    @Test
    fun solved_puzzle_uses_matching_addition_and_multiplication_pairs() {
        val solvedPuzzle = FourPairsLowDifficultyPuzzleGenerator(seed = 81).generateWithSolution().solvedPuzzle
        val additionPairs = solvedPuzzle.pairKeysFor(operator = Operator.ADDITION)
        val multiplicationPairs = solvedPuzzle.pairKeysFor(operator = Operator.MULTIPLICATION)

        assertEquals(FourPairsLowDifficultyRules.PAIR_COUNT, additionPairs.size)
        assertEquals(additionPairs, multiplicationPairs)
        assertEquals(PuzzleCompletionState.SOLVED, solvedPuzzle.completionState)
    }

    @Test
    fun generator_rejects_non_positive_max_attempts() {
        assertThrows(IllegalArgumentException::class.java) {
            FourPairsLowDifficultyPuzzleGenerator(maxAttempts = 0)
        }
    }

    @Test
    fun known_strip_anchors_are_distributed_and_belong_to_different_pairs() {
        val generatedPuzzle = FourPairsLowDifficultyPuzzleGenerator(seed = 99).generateWithSolution()
        val initialKnownEntryIds = generatedPuzzle.initialPuzzle.knownEntryIds()
        val highestEntryId = FourPairsLowDifficultyRules.STRIP_ENTRY_COUNT - 1
        val pairKeyByEntryId = generatedPuzzle.solvedPuzzle.pairKeyByEntryId()

        assertEquals(FourPairsLowDifficultyRules.KNOWN_STRIP_ENTRY_COUNT, initialKnownEntryIds.size)
        assertTrue(highestEntryId in initialKnownEntryIds)
        assertTrue(
            initialKnownEntryIds.maxConsecutiveHiddenEntries() <=
                FourPairsLowDifficultyRules.MAX_CONSECUTIVE_HIDDEN_ENTRIES
        )
        assertEquals(
            initialKnownEntryIds.size,
            initialKnownEntryIds.map { entryId -> pairKeyByEntryId.getValue(entryId) }.toSet().size
        )
    }
}

private fun assertInitialPuzzleMask(puzzle: Puzzle) {
    assertTrue(puzzle.board.tiles.all(Tile::hasHiddenExpression))
    assertEquals(FourPairsLowDifficultyRules.KNOWN_STRIP_ENTRY_COUNT, puzzle.knownEntryIds().size)
    assertEquals(
        FourPairsLowDifficultyRules.HIDDEN_STRIP_ENTRY_COUNT,
        puzzle.strip.entries.count { entry -> entry.item == StripItem.Hidden }
    )
}

private fun Tile.hasHiddenExpression(): Boolean = expression.leftOperand == Expression.Operand.Hidden &&
    expression.operator == Operator.Hidden &&
    expression.rightOperand == Expression.Operand.Hidden

private fun Puzzle.requireKnownStripValues(): List<Int> = strip.entries.map { entry ->
    (entry.item as StripItem.Known).value
}

private fun Puzzle.multiplicationTiles(): List<Tile> = board.tiles.filter { tile ->
    tile.expression.operator == Operator.MULTIPLICATION
}

private fun Puzzle.knownEntryIds(): Set<Int> = strip.entries
    .filter { entry -> entry.item is StripItem.Known }
    .map { entry -> entry.id }
    .toSet()

private fun Puzzle.pairKeysFor(operator: Operator): Set<TestPairKey> = resolvedTileAssignments()
    .filter { assignment -> assignment.operator == operator }
    .map(IndexedResolvedTileAssignment::pairKey)
    .toSet()

private fun Puzzle.pairKeyByEntryId(): Map<Int, TestPairKey> = resolvedTileAssignments()
    .filter { assignment -> assignment.operator == Operator.ADDITION }
    .flatMap { assignment ->
        val pairKey = assignment.pairKey

        listOf(
            assignment.leftOperand.stripEntryId to pairKey,
            assignment.rightOperand.stripEntryId to pairKey
        )
    }
    .toMap()

private val IndexedResolvedTileAssignment.pairKey: TestPairKey
    get() = TestPairKey(
        firstEntryId = minOf(leftOperand.stripEntryId, rightOperand.stripEntryId),
        secondEntryId = maxOf(leftOperand.stripEntryId, rightOperand.stripEntryId)
    )

private data class TestPairKey(val firstEntryId: Int, val secondEntryId: Int)

private fun Set<Int>.maxConsecutiveHiddenEntries(): Int {
    var currentHiddenCount = 0
    var maxHiddenCount = 0

    repeat(FourPairsLowDifficultyRules.STRIP_ENTRY_COUNT) { entryId ->
        if (entryId in this) {
            currentHiddenCount = 0
        } else {
            currentHiddenCount++
            maxHiddenCount = maxOf(maxHiddenCount, currentHiddenCount)
        }
    }

    return maxHiddenCount
}
