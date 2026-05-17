package org.cescfe.numpairs.domain.fourpairs

import org.cescfe.numpairs.domain.puzzle.Board
import org.cescfe.numpairs.domain.puzzle.Expression
import org.cescfe.numpairs.domain.puzzle.IndexedResolvedTileAssignment
import org.cescfe.numpairs.domain.puzzle.Operator
import org.cescfe.numpairs.domain.puzzle.Puzzle
import org.cescfe.numpairs.domain.puzzle.ResolvedOperandAssignment
import org.cescfe.numpairs.domain.puzzle.Strip
import org.cescfe.numpairs.domain.puzzle.StripEntry
import org.cescfe.numpairs.domain.puzzle.StripItem
import org.cescfe.numpairs.domain.puzzle.Tile
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
        val initialPuzzle = initialPuzzle()
        val solution = validSolution()
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
    fun solution_preserves_strip_entry_ids_and_indexed_tile_assignments() {
        val solution = validSolution()

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
    fun solution_requires_eight_unique_strip_entry_ids() {
        assertThrows(IllegalArgumentException::class.java) {
            validSolution(
                stripEntries = solvedStripEntries().dropLast(1)
            )
        }

        assertThrows(IllegalArgumentException::class.java) {
            validSolution(
                stripEntries = solvedStripEntries().toMutableList().apply {
                    set(7, FourPairsSolvedStripEntry(stripEntryId = 6, value = 8))
                }
            )
        }
    }

    @Test
    fun solution_requires_one_assignment_for_each_tile_index() {
        assertThrows(IllegalArgumentException::class.java) {
            validSolution(
                tileAssignments = validTileAssignments().dropLast(1)
            )
        }

        assertThrows(IllegalArgumentException::class.java) {
            validSolution(
                tileAssignments = validTileAssignments().toMutableList().apply {
                    set(
                        7,
                        tileAssignment(
                            tileIndex = 6,
                            leftEntryId = 6,
                            operator = Operator.MULTIPLICATION,
                            rightEntryId = 7
                        )
                    )
                }
            )
        }
    }

    @Test
    fun solution_requires_tile_assignments_to_reference_solution_strip_entry_ids() {
        assertThrows(IllegalArgumentException::class.java) {
            validSolution(
                tileAssignments = validTileAssignments().toMutableList().apply {
                    set(
                        0,
                        tileAssignment(
                            tileIndex = 0,
                            leftEntryId = 0,
                            operator = Operator.ADDITION,
                            rightEntryId = 99
                        )
                    )
                }
            )
        }
    }

    @Test
    fun generated_puzzle_requires_solution_strip_entry_ids_to_match_the_initial_puzzle() {
        val initialPuzzle = initialPuzzle(stripEntryIds = (10 until 18).toList())

        assertThrows(IllegalArgumentException::class.java) {
            FourPairsGeneratedPuzzle(
                initialPuzzle = initialPuzzle,
                solution = validSolution(),
                difficulty = FourPairsDifficulty.LOW
            )
        }
    }

    @Test
    fun solved_strip_entries_require_valid_identity_and_value() {
        assertThrows(IllegalArgumentException::class.java) {
            FourPairsSolvedStripEntry(stripEntryId = -1, value = 1)
        }

        assertThrows(IllegalArgumentException::class.java) {
            FourPairsSolvedStripEntry(stripEntryId = 0, value = 0)
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

private fun validSolution(
    stripEntries: List<FourPairsSolvedStripEntry> = solvedStripEntries(),
    tileAssignments: List<IndexedResolvedTileAssignment> = validTileAssignments()
): FourPairsSolution = FourPairsSolution(
    stripEntries = stripEntries,
    tileAssignments = tileAssignments
)

private fun solvedStripEntries(): List<FourPairsSolvedStripEntry> = (0 until Strip.NUMBER_COUNT).map { stripEntryId ->
    FourPairsSolvedStripEntry(
        stripEntryId = stripEntryId,
        value = stripEntryId + 1
    )
}

private fun validTileAssignments(): List<IndexedResolvedTileAssignment> = listOf(
    tileAssignment(tileIndex = 0, leftEntryId = 0, operator = Operator.ADDITION, rightEntryId = 1),
    tileAssignment(tileIndex = 1, leftEntryId = 1, operator = Operator.MULTIPLICATION, rightEntryId = 0),
    tileAssignment(tileIndex = 2, leftEntryId = 2, operator = Operator.ADDITION, rightEntryId = 3),
    tileAssignment(tileIndex = 3, leftEntryId = 3, operator = Operator.MULTIPLICATION, rightEntryId = 2),
    tileAssignment(tileIndex = 4, leftEntryId = 4, operator = Operator.ADDITION, rightEntryId = 5),
    tileAssignment(tileIndex = 5, leftEntryId = 5, operator = Operator.MULTIPLICATION, rightEntryId = 4),
    tileAssignment(tileIndex = 6, leftEntryId = 6, operator = Operator.ADDITION, rightEntryId = 7),
    tileAssignment(tileIndex = 7, leftEntryId = 7, operator = Operator.MULTIPLICATION, rightEntryId = 6)
)

private fun tileAssignment(
    tileIndex: Int,
    leftEntryId: Int,
    operator: Operator,
    rightEntryId: Int
): IndexedResolvedTileAssignment = IndexedResolvedTileAssignment(
    tileIndex = tileIndex,
    leftOperand = ResolvedOperandAssignment(
        stripEntryId = leftEntryId,
        value = leftEntryId + 1
    ),
    operator = operator,
    rightOperand = ResolvedOperandAssignment(
        stripEntryId = rightEntryId,
        value = rightEntryId + 1
    )
)

private fun initialPuzzle(stripEntryIds: List<Int> = (0 until Strip.NUMBER_COUNT).toList()): Puzzle = Puzzle(
    board = Board(
        tiles = List(Board.TILE_COUNT) { tileIndex ->
            Tile(
                expression = Expression(
                    leftOperand = Expression.Operand.Hidden,
                    operator = Operator.Hidden,
                    rightOperand = Expression.Operand.Hidden
                ),
                result = tileIndex + 1
            )
        }
    ),
    strip = Strip.fromEntries(
        entries = stripEntryIds.mapIndexed { index, stripEntryId ->
            StripEntry(
                id = stripEntryId,
                item = StripItem.Known(index + 1)
            )
        }
    )
)
