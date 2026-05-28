package org.cescfe.numpairs.feature.fourpairs

import org.cescfe.numpairs.domain.fourpairs.FourPairsLowDifficultyPuzzleGenerator
import org.cescfe.numpairs.domain.puzzle.Board
import org.cescfe.numpairs.domain.puzzle.Expression
import org.cescfe.numpairs.domain.puzzle.Operator
import org.cescfe.numpairs.domain.puzzle.PuzzleCompletionState
import org.cescfe.numpairs.domain.puzzle.Strip
import org.cescfe.numpairs.domain.puzzle.StripItem
import org.cescfe.numpairs.domain.puzzle.Tile
import org.junit.Assert.assertEquals
import org.junit.Assert.assertSame
import org.junit.Assert.assertTrue
import org.junit.Test

class LowDifficultyFourPairsPuzzleProviderTest {
    @Test
    fun returns_a_generated_initial_puzzle() {
        val puzzle = LowDifficultyFourPairsPuzzleProvider(seed = 2026).nextPuzzle()

        assertEquals(Board.TILE_COUNT, puzzle.board.tiles.size)
        assertEquals(Strip.NUMBER_COUNT, puzzle.strip.entries.size)
        assertEquals(PuzzleCompletionState.INCOMPLETE, puzzle.completionState)
        assertTrue(puzzle.board.tiles.all(Tile::hasHiddenExpression))
        assertEquals(3, puzzle.strip.entries.count { entry -> entry.item is StripItem.Known })
        assertEquals(5, puzzle.strip.entries.count { entry -> entry.item == StripItem.Hidden })
    }

    @Test
    fun seeded_provider_uses_the_domain_generator_initial_puzzle() {
        val expectedPuzzle = FourPairsLowDifficultyPuzzleGenerator(seed = 1234).generate()

        val providedPuzzle = LowDifficultyFourPairsPuzzleProvider(seed = 1234).nextPuzzle()

        assertEquals(expectedPuzzle, providedPuzzle)
    }

    @Test
    fun seeded_providers_are_deterministic() {
        val firstPuzzle = LowDifficultyFourPairsPuzzleProvider(seed = 42).nextPuzzle()
        val secondPuzzle = LowDifficultyFourPairsPuzzleProvider(seed = 42).nextPuzzle()

        assertEquals(firstPuzzle, secondPuzzle)
    }

    @Test
    fun provider_can_be_faked_by_callers() {
        val fakePuzzle = LowDifficultyFourPairsPuzzleProvider(seed = 1).nextPuzzle()
        val fakeProvider = FourPairsPuzzleProvider { fakePuzzle }

        assertSame(fakePuzzle, fakeProvider.nextPuzzle())
    }
}

private fun Tile.hasHiddenExpression(): Boolean = expression.leftOperand == Expression.Operand.Hidden &&
    expression.operator == Operator.Hidden &&
    expression.rightOperand == Expression.Operand.Hidden
