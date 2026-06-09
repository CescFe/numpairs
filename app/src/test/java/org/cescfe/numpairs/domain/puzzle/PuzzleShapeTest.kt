package org.cescfe.numpairs.domain.puzzle

import org.cescfe.numpairs.domain.puzzle.support.TileAssignment
import org.cescfe.numpairs.domain.puzzle.support.hiddenTile
import org.cescfe.numpairs.domain.puzzle.support.knownPuzzleWithAssignments
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertThrows
import org.junit.Test

class PuzzleShapeTest {
    @Test
    fun supports_a_valid_one_pair_puzzle() {
        val puzzle = knownPuzzleWithAssignments(
            stripValues = listOf(2, 3),
            TileAssignment(leftEntryId = 0, operator = Operator.ADDITION, rightEntryId = 1),
            TileAssignment(leftEntryId = 1, operator = Operator.MULTIPLICATION, rightEntryId = 0)
        )

        assertEquals(2, puzzle.strip.entries.size)
        assertEquals(2, puzzle.board.tiles.size)
        assertEquals(PuzzleCompletionState.SOLVED, puzzle.completionState)
        assertFalse(puzzle.hasMismatchedSumProductPairings)
        assertFalse(puzzle.hasInvalidStripEntryUsage)
    }

    @Test
    fun supports_a_valid_two_pair_puzzle() {
        val puzzle = knownPuzzleWithAssignments(
            stripValues = listOf(1, 2, 3, 4),
            TileAssignment(leftEntryId = 0, operator = Operator.ADDITION, rightEntryId = 1),
            TileAssignment(leftEntryId = 1, operator = Operator.MULTIPLICATION, rightEntryId = 0),
            TileAssignment(leftEntryId = 2, operator = Operator.ADDITION, rightEntryId = 3),
            TileAssignment(leftEntryId = 3, operator = Operator.MULTIPLICATION, rightEntryId = 2)
        )

        assertEquals(4, puzzle.strip.entries.size)
        assertEquals(4, puzzle.board.tiles.size)
        assertEquals(PuzzleCompletionState.SOLVED, puzzle.completionState)
        assertFalse(puzzle.hasMismatchedSumProductPairings)
        assertFalse(puzzle.hasInvalidStripEntryUsage)
    }

    @Test
    fun rejects_puzzles_with_fewer_than_two_strip_entries() {
        assertThrows(IllegalArgumentException::class.java) {
            Puzzle(
                board = Board(tiles = emptyList()),
                strip = Strip.fromItems(items = emptyList())
            )
        }
    }

    @Test
    fun rejects_puzzles_with_an_odd_strip_entry_count() {
        assertThrows(IllegalArgumentException::class.java) {
            Puzzle(
                board = Board(
                    tiles = listOf(
                        hiddenTile(result = 2),
                        hiddenTile(result = 3),
                        hiddenTile(result = 4)
                    )
                ),
                strip = Strip.fromItems(
                    items = listOf(
                        StripItem.Known(1),
                        StripItem.Known(2),
                        StripItem.Known(3)
                    )
                )
            )
        }
    }

    @Test
    fun rejects_puzzles_when_board_tile_count_does_not_match_strip_entry_count() {
        assertThrows(IllegalArgumentException::class.java) {
            Puzzle(
                board = Board(
                    tiles = listOf(
                        hiddenTile(result = 3),
                        hiddenTile(result = 2)
                    )
                ),
                strip = Strip.fromItems(
                    items = listOf(
                        StripItem.Known(1),
                        StripItem.Known(2),
                        StripItem.Known(3),
                        StripItem.Known(4)
                    )
                )
            )
        }
    }
}
