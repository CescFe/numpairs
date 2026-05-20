package org.cescfe.numpairs.domain.puzzle

import org.cescfe.numpairs.domain.puzzle.support.hiddenTile
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class InitialPuzzleSolvabilityValidatorTest {
    @Test
    fun solvable_initial_puzzle_with_known_strip_is_valid() {
        val puzzle = initialPuzzle(
            stripItems = knownStripItems(1, 2, 6, 10, 25, 25, 50, 222),
            results = listOf(
                223,
                222,
                52,
                100,
                31,
                150,
                35,
                250
            )
        )

        val result = InitialPuzzleSolvabilityValidator.validate(puzzle)

        assertEquals(InitialPuzzleValidationResult.VALID, result)
        assertTrue(result.isValid)
    }

    @Test
    fun unsolvable_result_set_is_invalid() {
        val puzzle = initialPuzzle(
            stripItems = knownStripItems(1, 2, 6, 10, 25, 25, 50, 222),
            results = listOf(
                224,
                222,
                52,
                100,
                31,
                150,
                35,
                250
            )
        )

        assertEquals(
            InitialPuzzleValidationResult.INVALID,
            InitialPuzzleSolvabilityValidator.validate(puzzle)
        )
    }

    @Test
    fun repeated_values_are_treated_as_distinct_strip_entries() {
        val puzzle = initialPuzzle(
            stripItems = knownStripItems(2, 2, 2, 2, 5, 5, 7, 7),
            results = listOf(
                4,
                4,
                4,
                4,
                10,
                25,
                14,
                49
            )
        )

        assertEquals(
            InitialPuzzleValidationResult.VALID,
            InitialPuzzleSolvabilityValidator.validate(puzzle)
        )
    }

    @Test
    fun hidden_strip_entries_are_unsupported_for_the_first_iteration() {
        val puzzle = initialPuzzle(
            stripItems = listOf(
                StripItem.Known(1),
                StripItem.Known(2),
                StripItem.Known(6),
                StripItem.Known(10),
                StripItem.Known(25),
                StripItem.Hidden,
                StripItem.Known(50),
                StripItem.Known(222)
            )
        )

        assertEquals(
            InitialPuzzleValidationResult.UNSUPPORTED_INPUT_SHAPE,
            InitialPuzzleSolvabilityValidator.validate(puzzle)
        )
    }

    @Test
    fun non_hidden_tile_expressions_are_unsupported_for_initial_puzzle_validation() {
        val puzzle = initialPuzzle(
            stripItems = knownStripItems(1, 2, 6, 10, 25, 25, 50, 222)
        ).copy(
            board = Board(
                tiles = initialPuzzleResults().map(::hiddenTile).toMutableList().apply {
                    set(
                        0,
                        hiddenTile(result = 223)
                            .withLeftOperand(
                                value = 1,
                                stripEntryId = 0
                            )
                    )
                }
            )
        )

        assertEquals(
            InitialPuzzleValidationResult.UNSUPPORTED_INPUT_SHAPE,
            InitialPuzzleSolvabilityValidator.validate(puzzle)
        )
    }
}

private fun initialPuzzle(stripItems: List<StripItem>, results: List<Int> = initialPuzzleResults()): Puzzle = Puzzle(
    board = Board(
        tiles = results.map(::hiddenTile)
    ),
    strip = Strip.fromItems(items = stripItems)
)

private fun knownStripItems(vararg values: Int): List<StripItem> = values.map { value ->
    StripItem.Known(value)
}

private fun initialPuzzleResults(): List<Int> = listOf(
    223,
    222,
    52,
    100,
    31,
    150,
    35,
    250
)
