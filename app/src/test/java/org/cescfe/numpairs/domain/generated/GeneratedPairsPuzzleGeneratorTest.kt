package org.cescfe.numpairs.domain.generated

import org.cescfe.numpairs.domain.fourpairs.FourPairsLowDifficultyRules
import org.cescfe.numpairs.domain.puzzle.model.Expression
import org.cescfe.numpairs.domain.puzzle.model.Operator
import org.cescfe.numpairs.domain.puzzle.model.Puzzle
import org.cescfe.numpairs.domain.puzzle.model.PuzzleCompletionState
import org.cescfe.numpairs.domain.puzzle.model.StripItem
import org.cescfe.numpairs.domain.puzzle.model.Tile
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertThrows
import org.junit.Assert.assertTrue
import org.junit.Test

class GeneratedPairsPuzzleGeneratorTest {
    @Test
    fun four_pairs_low_profile_generation_satisfies_profile_constraints() {
        val generatedPuzzle = GeneratedPairsPuzzleGenerator(
            profile = GeneratedPuzzleProfiles.FOUR_PAIRS_LOW,
            seed = 42
        ).generateWithSolution()
        val solvedPuzzle = generatedPuzzle.solvedPuzzle
        val initialPuzzle = generatedPuzzle.initialPuzzle
        val solvedStripValues = solvedPuzzle.requireKnownStripValues()
        val initialKnownEntryIds = initialPuzzle.knownEntryIds()

        assertEquals(PuzzleCompletionState.SOLVED, solvedPuzzle.completionState)
        assertEquals(PuzzleCompletionState.INCOMPLETE, initialPuzzle.completionState)
        assertEquals(FourPairsLowDifficultyRules.PAIR_COUNT, solvedPuzzle.additionTiles().size)
        assertEquals(FourPairsLowDifficultyRules.PAIR_COUNT, solvedPuzzle.multiplicationTiles().size)
        assertEquals(FourPairsLowDifficultyRules.BOARD_TILE_COUNT, solvedPuzzle.board.tiles.size)
        assertEquals(FourPairsLowDifficultyRules.STRIP_ENTRY_COUNT, solvedPuzzle.strip.entries.size)

        assertEquals(solvedStripValues.sorted(), solvedStripValues)
        assertEquals(solvedStripValues.size, solvedStripValues.toSet().size)
        assertTrue(solvedStripValues.all { value -> value in FourPairsLowDifficultyRules.stripValueRange })
        assertFalse(1 in solvedStripValues)

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
        assertTrue(initialPuzzle.board.tiles.all(Tile::hasHiddenExpression))
        assertEquals(FourPairsLowDifficultyRules.KNOWN_STRIP_ENTRY_COUNT, initialKnownEntryIds.size)
        assertEquals(
            FourPairsLowDifficultyRules.HIDDEN_STRIP_ENTRY_COUNT,
            initialPuzzle.strip.entries.count { entry -> entry.item == StripItem.Hidden }
        )
        assertTrue(FourPairsLowDifficultyRules.STRIP_ENTRY_COUNT - 1 in initialKnownEntryIds)
        assertTrue(
            initialKnownEntryIds.maxConsecutiveHiddenEntries(
                totalEntryCount = FourPairsLowDifficultyRules.STRIP_ENTRY_COUNT
            ) <= FourPairsLowDifficultyRules.MAX_CONSECUTIVE_HIDDEN_ENTRIES
        )
    }

    @Test
    fun four_pairs_low_profile_generation_is_deterministic_for_the_same_seed() {
        val firstPuzzle = GeneratedPairsPuzzleGenerator(
            profile = GeneratedPuzzleProfiles.FOUR_PAIRS_LOW,
            seed = 1234
        ).generateWithSolution()
        val secondPuzzle = GeneratedPairsPuzzleGenerator(
            profile = GeneratedPuzzleProfiles.FOUR_PAIRS_LOW,
            seed = 1234
        ).generateWithSolution()

        assertEquals(firstPuzzle, secondPuzzle)
    }

    @Test
    fun generator_rejects_non_positive_max_attempts() {
        assertThrows(IllegalArgumentException::class.java) {
            GeneratedPairsPuzzleGenerator(
                profile = GeneratedPuzzleProfiles.FOUR_PAIRS_LOW,
                maxAttempts = 0
            )
        }
    }

    @Test
    fun generator_fails_after_bounded_attempts_when_profile_cannot_be_satisfied() {
        val impossibleProfile = GeneratedPuzzleProfiles.FOUR_PAIRS_LOW.copy(
            resultConstraints = GeneratedPuzzleProfiles.FOUR_PAIRS_LOW.resultConstraints.copy(
                maxMultiplicationResult = 1
            )
        )

        assertThrows(IllegalStateException::class.java) {
            GeneratedPairsPuzzleGenerator(
                profile = impossibleProfile,
                seed = 2026,
                maxAttempts = 1
            ).generateWithSolution()
        }
    }
}

private fun Puzzle.requireKnownStripValues(): List<Int> = strip.entries.map { entry ->
    (entry.item as StripItem.Known).value
}

private fun Puzzle.additionTiles(): List<Tile> = tilesFor(operator = Operator.ADDITION)

private fun Puzzle.multiplicationTiles(): List<Tile> = tilesFor(operator = Operator.MULTIPLICATION)

private fun Puzzle.tilesFor(operator: Operator): List<Tile> = board.tiles.filter { tile ->
    tile.expression.operator == operator
}

private fun Tile.hasHiddenExpression(): Boolean = expression.leftOperand == Expression.Operand.Hidden &&
    expression.operator == Operator.Hidden &&
    expression.rightOperand == Expression.Operand.Hidden

private fun Puzzle.knownEntryIds(): Set<Int> = strip.entries
    .filter { entry -> entry.item is StripItem.Known }
    .map { entry -> entry.id }
    .toSet()

private fun Set<Int>.maxConsecutiveHiddenEntries(totalEntryCount: Int): Int {
    var currentHiddenCount = 0
    var maxHiddenCount = 0

    repeat(totalEntryCount) { entryId ->
        if (entryId in this) {
            currentHiddenCount = 0
        } else {
            currentHiddenCount++
            maxHiddenCount = maxOf(maxHiddenCount, currentHiddenCount)
        }
    }

    return maxHiddenCount
}
