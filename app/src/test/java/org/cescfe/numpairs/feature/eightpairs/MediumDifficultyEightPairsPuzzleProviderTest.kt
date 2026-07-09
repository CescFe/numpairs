package org.cescfe.numpairs.feature.eightpairs

import org.cescfe.numpairs.domain.generated.GeneratedPairsPuzzleGenerator
import org.cescfe.numpairs.domain.generated.GeneratedPuzzleProfile
import org.cescfe.numpairs.domain.generated.GeneratedPuzzleProfiles
import org.cescfe.numpairs.domain.puzzle.model.Expression
import org.cescfe.numpairs.domain.puzzle.model.Operator
import org.cescfe.numpairs.domain.puzzle.model.PuzzleCompletionState
import org.cescfe.numpairs.domain.puzzle.model.StripItem
import org.cescfe.numpairs.domain.puzzle.model.Tile
import org.junit.Assert.assertEquals
import org.junit.Assert.assertSame
import org.junit.Assert.assertTrue
import org.junit.Test

class MediumDifficultyEightPairsPuzzleProviderTest {

    @Test
    fun returns_a_generated_initial_puzzle() {
        val profile = GeneratedPuzzleProfiles.EIGHT_PAIRS_MEDIUM
        val puzzle = MediumDifficultyEightPairsPuzzleProvider(seed = 2026).nextPuzzle()

        assertEquals(profile.size.boardTileCount, puzzle.board.tiles.size)
        assertEquals(profile.size.stripEntryCount, puzzle.strip.entries.size)
        assertEquals(PuzzleCompletionState.INCOMPLETE, puzzle.completionState)
        assertTrue(puzzle.board.tiles.all(Tile::hasHiddenExpression))
        assertInitialStripMaskMatchesProfile(
            knownEntryCount = puzzle.strip.entries.count { entry -> entry.item is StripItem.Known },
            hiddenEntryCount = puzzle.strip.entries.count { entry -> entry.item == StripItem.Hidden },
            profile = profile
        )
    }

    @Test
    fun seeded_provider_uses_the_domain_generator_initial_puzzle() {
        val expectedPuzzle = GeneratedPairsPuzzleGenerator(
            profile = GeneratedPuzzleProfiles.EIGHT_PAIRS_MEDIUM,
            seed = 1234
        ).generate()

        val providedPuzzle = MediumDifficultyEightPairsPuzzleProvider(seed = 1234).nextPuzzle()

        assertEquals(expectedPuzzle, providedPuzzle)
    }

    @Test
    fun seeded_providers_are_deterministic() {
        val firstPuzzle = MediumDifficultyEightPairsPuzzleProvider(seed = 42).nextPuzzle()
        val secondPuzzle = MediumDifficultyEightPairsPuzzleProvider(seed = 42).nextPuzzle()

        assertEquals(firstPuzzle, secondPuzzle)
    }

    @Test
    fun provider_can_be_faked_by_callers() {
        val fakePuzzle = MediumDifficultyEightPairsPuzzleProvider(seed = 1).nextPuzzle()
        val fakeProvider = EightPairsPuzzleProvider { fakePuzzle }

        assertSame(fakePuzzle, fakeProvider.nextPuzzle())
    }
}

private fun Tile.hasHiddenExpression(): Boolean = expression.leftOperand == Expression.Operand.Hidden &&
    expression.operator == Operator.Hidden &&
    expression.rightOperand == Expression.Operand.Hidden

private fun assertInitialStripMaskMatchesProfile(
    knownEntryCount: Int,
    hiddenEntryCount: Int,
    profile: GeneratedPuzzleProfile
) {
    assertTrue(knownEntryCount in profile.initialStripMaskPolicy.knownEntryCountRange)
    assertTrue(hiddenEntryCount in profile.initialStripMaskPolicy.hiddenEntryCountRange)
}
