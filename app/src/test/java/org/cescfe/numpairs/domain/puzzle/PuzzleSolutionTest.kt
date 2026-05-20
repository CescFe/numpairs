package org.cescfe.numpairs.domain.puzzle

import org.cescfe.numpairs.domain.puzzle.support.TileAssignment
import org.cescfe.numpairs.domain.puzzle.support.defaultKnownStripValues
import org.cescfe.numpairs.domain.puzzle.support.hiddenTile
import org.cescfe.numpairs.domain.puzzle.support.knownPuzzleWithAssignments
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertThrows
import org.junit.Assert.assertTrue
import org.junit.Test

class PuzzleSolutionTest {
    @Test
    fun requires_a_solved_puzzle() {
        assertThrows(IllegalArgumentException::class.java) {
            PuzzleSolution(solvedPuzzle = initialPuzzleFor(solvedPuzzle()))
        }
    }

    @Test
    fun exposes_strip_entry_ids_and_indexed_tile_assignments_from_the_solved_puzzle() {
        val solution = PuzzleSolution(solvedPuzzle = solvedPuzzle())

        assertEquals((0 until Strip.NUMBER_COUNT).toSet(), solution.stripEntryIds)
        assertEquals(
            (0 until Board.TILE_COUNT).toList(),
            solution.tileAssignments.map { assignment ->
                assignment.tileIndex
            }
        )
        assertEquals(0, solution.tileAssignments.first().leftOperand.stripEntryId)
        assertEquals(1, solution.tileAssignments.first().rightOperand.stripEntryId)
    }

    @Test
    fun matches_an_initial_puzzle_with_the_same_strip_entries_results_and_visible_values() {
        val solvedPuzzle = solvedPuzzle()
        val solution = PuzzleSolution(solvedPuzzle = solvedPuzzle)

        assertTrue(solution.isSolutionFor(initialPuzzleFor(solvedPuzzle)))
    }

    @Test
    fun does_not_match_an_initial_puzzle_with_different_strip_entry_ids() {
        val solvedPuzzle = solvedPuzzle()
        val solution = PuzzleSolution(solvedPuzzle = solvedPuzzle)
        val initialPuzzle = initialPuzzleFor(
            solvedPuzzle = solvedPuzzle,
            stripEntryIds = (10 until 18).toList()
        )

        assertFalse(solution.isSolutionFor(initialPuzzle))
    }

    @Test
    fun does_not_match_an_initial_puzzle_with_different_board_results_by_index() {
        val solvedPuzzle = solvedPuzzle()
        val solution = PuzzleSolution(solvedPuzzle = solvedPuzzle)
        val initialPuzzle = initialPuzzleFor(
            solvedPuzzle = solvedPuzzle,
            tileResults = solvedPuzzle.board.tiles.mapIndexed { index, tile ->
                if (index == 0) tile.result + 1 else tile.result
            }
        )

        assertFalse(solution.isSolutionFor(initialPuzzle))
    }

    @Test
    fun does_not_match_an_initial_puzzle_when_a_visible_strip_value_differs() {
        val solvedPuzzle = solvedPuzzle()
        val solution = PuzzleSolution(solvedPuzzle = solvedPuzzle)
        val initialPuzzle = initialPuzzleFor(
            solvedPuzzle = solvedPuzzle,
            stripItems = listOf(
                StripItem.Hidden,
                StripItem.Hidden,
                StripItem.Known(4),
                StripItem.Hidden,
                StripItem.Hidden,
                StripItem.Known(6),
                StripItem.Hidden,
                StripItem.Known(8)
            )
        )

        assertFalse(solution.isSolutionFor(initialPuzzle))
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
    stripEntryIds: List<Int> = (0 until Strip.NUMBER_COUNT).toList(),
    stripItems: List<StripItem> = initialStripItems(),
    tileResults: List<Int> = solvedPuzzle.board.tiles.map { tile -> tile.result }
): Puzzle = Puzzle(
    board = Board(tiles = tileResults.map(::hiddenTile)),
    strip = Strip.fromEntries(
        entries = stripEntryIds.zip(stripItems).map { (stripEntryId, stripItem) ->
            StripEntry(
                id = stripEntryId,
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
