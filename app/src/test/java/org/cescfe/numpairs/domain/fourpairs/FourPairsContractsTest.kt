package org.cescfe.numpairs.domain.fourpairs

import org.cescfe.numpairs.domain.puzzle.Board
import org.cescfe.numpairs.domain.puzzle.Expression
import org.cescfe.numpairs.domain.puzzle.Operator
import org.cescfe.numpairs.domain.puzzle.Puzzle
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
    fun solution_requires_a_solved_puzzle() {
        assertThrows(IllegalArgumentException::class.java) {
            FourPairsSolution(solvedPuzzle = initialPuzzle())
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

private fun validSolution(): FourPairsSolution = FourPairsSolution(solvedPuzzle = solvedPuzzle())

private fun tileAssignment(leftEntryId: Int, operator: Operator, rightEntryId: Int): Tile = Tile(
    expression = Expression(
        leftOperand = Expression.Operand.Known(
            value = leftEntryId + 1,
            stripEntryId = leftEntryId
        ),
        operator = operator,
        rightOperand = Expression.Operand.Known(
            value = rightEntryId + 1,
            stripEntryId = rightEntryId
        )
    ),
    result = operator.apply(
        leftOperand = leftEntryId + 1,
        rightOperand = rightEntryId + 1
    )
)

private fun solvedPuzzle(): Puzzle = Puzzle(
    board = Board(
        tiles = listOf(
            tileAssignment(leftEntryId = 0, operator = Operator.ADDITION, rightEntryId = 1),
            tileAssignment(leftEntryId = 1, operator = Operator.MULTIPLICATION, rightEntryId = 0),
            tileAssignment(leftEntryId = 2, operator = Operator.ADDITION, rightEntryId = 3),
            tileAssignment(leftEntryId = 3, operator = Operator.MULTIPLICATION, rightEntryId = 2),
            tileAssignment(leftEntryId = 4, operator = Operator.ADDITION, rightEntryId = 5),
            tileAssignment(leftEntryId = 5, operator = Operator.MULTIPLICATION, rightEntryId = 4),
            tileAssignment(leftEntryId = 6, operator = Operator.ADDITION, rightEntryId = 7),
            tileAssignment(leftEntryId = 7, operator = Operator.MULTIPLICATION, rightEntryId = 6)
        )
    ),
    strip = Strip.fromEntries(
        entries = (0 until Strip.NUMBER_COUNT).map { stripEntryId ->
            StripEntry(
                id = stripEntryId,
                item = StripItem.Known(stripEntryId + 1)
            )
        }
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
