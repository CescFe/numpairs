package org.cescfe.numpairs.domain.fourpairs

import org.cescfe.numpairs.domain.puzzle.Board
import org.cescfe.numpairs.domain.puzzle.Operator
import org.cescfe.numpairs.domain.puzzle.Puzzle
import org.cescfe.numpairs.domain.puzzle.PuzzleSolution
import org.cescfe.numpairs.domain.puzzle.Strip
import org.cescfe.numpairs.domain.puzzle.StripEntry
import org.cescfe.numpairs.domain.puzzle.StripItem
import org.cescfe.numpairs.domain.puzzle.support.TileAssignment
import org.cescfe.numpairs.domain.puzzle.support.defaultKnownStripValues
import org.cescfe.numpairs.domain.puzzle.support.hiddenTile
import org.cescfe.numpairs.domain.puzzle.support.knownPuzzleWithAssignments
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertThrows
import org.junit.Assert.assertTrue
import org.junit.Test

class FourPairsContractsTest {
    @Test
    fun generation_request_defaults_to_low_difficulty_without_a_seed() {
        val request = FourPairsGenerationRequest()

        assertNull(request.seed)
        assertEquals(FourPairsDifficulty.LOW, request.difficulty)
    }

    @Test
    fun generated_puzzle_exposes_initial_puzzle_solution_and_difficulty() {
        val initialPuzzle = initialPuzzleFor(solvedPuzzle())
        val solution = PuzzleSolution(solvedPuzzle = solvedPuzzle())
        val generatedPuzzle = FourPairsGeneratedPuzzle(
            initialPuzzle = initialPuzzle,
            solution = solution,
            difficulty = FourPairsDifficulty.LOW
        )

        assertEquals(initialPuzzle, generatedPuzzle.initialPuzzle)
        assertEquals(solution, generatedPuzzle.solution)
        assertEquals(FourPairsDifficulty.LOW, generatedPuzzle.difficulty)
    }

    @Test
    fun generated_puzzle_requires_the_solution_to_solve_the_initial_puzzle() {
        val incompatibleInitialPuzzle = initialPuzzleFor(
            solvedPuzzle = solvedPuzzle(),
            tileResults = List(Board.TILE_COUNT) { index -> index + 100 }
        )

        assertThrows(IllegalArgumentException::class.java) {
            FourPairsGeneratedPuzzle(
                initialPuzzle = incompatibleInitialPuzzle,
                solution = PuzzleSolution(solvedPuzzle = solvedPuzzle()),
                difficulty = FourPairsDifficulty.LOW
            )
        }
    }

    @Test
    fun validation_result_exposes_validity_and_requires_failures_for_invalid_results() {
        val invalidResult = FourPairsValidationResult.Invalid(
            failures = setOf(FourPairsValidationFailure.NO_SOLUTION)
        )

        assertTrue(FourPairsValidationResult.Valid.isValid)
        assertFalse(invalidResult.isValid)

        assertThrows(IllegalArgumentException::class.java) {
            FourPairsValidationResult.Invalid(failures = emptySet())
        }
    }
}

private fun solvedPuzzle(): Puzzle = knownPuzzleWithAssignments(
    stripValues = defaultKnownStripValues(),
    *solvedTileAssignments().toTypedArray()
)

private fun solvedTileAssignments(): List<TileAssignment> = listOf(
    TileAssignment(leftEntryId = 0, operator = Operator.ADDITION, rightEntryId = 1),
    TileAssignment(leftEntryId = 1, operator = Operator.MULTIPLICATION, rightEntryId = 0),
    TileAssignment(leftEntryId = 2, operator = Operator.ADDITION, rightEntryId = 3),
    TileAssignment(leftEntryId = 3, operator = Operator.MULTIPLICATION, rightEntryId = 2),
    TileAssignment(leftEntryId = 4, operator = Operator.ADDITION, rightEntryId = 5),
    TileAssignment(leftEntryId = 5, operator = Operator.MULTIPLICATION, rightEntryId = 4),
    TileAssignment(leftEntryId = 6, operator = Operator.ADDITION, rightEntryId = 7),
    TileAssignment(leftEntryId = 7, operator = Operator.MULTIPLICATION, rightEntryId = 6)
)

private fun initialPuzzleFor(
    solvedPuzzle: Puzzle,
    tileResults: List<Int> = solvedPuzzle.board.tiles.map { tile -> tile.result }
): Puzzle = Puzzle(
    board = Board(tiles = tileResults.map(::hiddenTile)),
    strip = Strip.fromEntries(
        entries = initialStripItems().mapIndexed { index, stripItem ->
            StripEntry(
                id = index,
                item = stripItem
            )
        }
    )
)

private fun initialStripItems(): List<StripItem> = listOf(
    StripItem.Hidden,
    StripItem.Hidden,
    StripItem.Known(3),
    StripItem.Hidden,
    StripItem.Hidden,
    StripItem.Known(6),
    StripItem.Hidden,
    StripItem.Known(8)
)
