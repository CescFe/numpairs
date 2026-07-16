package org.cescfe.numpairs.feature.tutorial

import org.cescfe.numpairs.domain.puzzle.model.Expression
import org.cescfe.numpairs.domain.puzzle.model.Operator
import org.cescfe.numpairs.domain.puzzle.model.PuzzleCompletionState
import org.cescfe.numpairs.domain.puzzle.model.StripEntryRange
import org.cescfe.numpairs.domain.puzzle.model.StripItem
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class FinalValidationContentTest {
    private val scenario = FinalValidationContent.scenario

    @Test
    fun authors_exactly_two_simple_complementary_pairs() {
        assertEquals(TutorialScenarioId.FINAL_VALIDATION, scenario.id)
        assertEquals(listOf(2, 3, 4, 5), scenario.stripValues)
        assertEquals(4, scenario.initialPuzzle.strip.entries.size)
        assertEquals(4, scenario.initialPuzzle.board.tiles.size)
        assertEquals(listOf(5, 6, 9, 20), scenario.initialPuzzle.board.tiles.map { tile -> tile.result })
        assertEquals(
            listOf(
                TutorialIntendedPair(firstStripEntryId = 0, secondStripEntryId = 1),
                TutorialIntendedPair(firstStripEntryId = 2, secondStripEntryId = 3)
            ),
            scenario.intendedPairs
        )
    }

    @Test
    fun masks_one_inferable_strip_value_and_every_expression() {
        assertEquals(
            listOf(
                StripItem.Known(2),
                StripItem.Hidden,
                StripItem.Known(4),
                StripItem.Known(5)
            ),
            scenario.initialPuzzle.strip.items
        )
        assertEquals(1, scenario.initialPuzzle.strip.items.count { item -> item == StripItem.Hidden })
        assertTrue(
            scenario.initialPuzzle.board.tiles.all { tile ->
                tile.expression.leftOperand == Expression.Operand.Hidden &&
                    tile.expression.operator == Operator.Hidden &&
                    tile.expression.rightOperand == Expression.Operand.Hidden
            }
        )
        assertEquals(
            StripEntryRange(minimumValue = 2, maximumValue = 4),
            scenario.initialPuzzle.strip.validEntryRangeFor(index = 1)
        )
    }

    @Test
    fun provides_the_exact_valid_solved_puzzle() {
        val solvedExpressions = scenario.solvedPuzzle.board.tiles.map { tile -> tile.expression }

        assertEquals(PuzzleCompletionState.INCOMPLETE, scenario.initialPuzzle.completionState)
        assertEquals(PuzzleCompletionState.SOLVED, scenario.solvedPuzzle.completionState)
        assertEquals(scenario.stripValues.map(StripItem::Known), scenario.solvedPuzzle.strip.items)
        assertEquals(
            listOf(
                Expression(2, Operator.ADDITION, 3),
                Expression(2, Operator.MULTIPLICATION, 3),
                Expression(4, Operator.ADDITION, 5),
                Expression(4, Operator.MULTIPLICATION, 5)
            ),
            solvedExpressions.map { expression -> expression.withoutEntryIds() }
        )
    }

    @Test
    fun registers_validation_without_adding_a_guided_step() {
        assertEquals(scenario, TutorialContent.scenario(TutorialScenarioId.FINAL_VALIDATION))
        assertFalse(
            TutorialContent.steps.any { step ->
                step.scenarioId == TutorialScenarioId.FINAL_VALIDATION
            }
        )
    }

    private fun Expression.withoutEntryIds(): Expression = copy(
        leftOperand = (leftOperand as Expression.Operand.Known).copy(stripEntryId = null),
        rightOperand = (rightOperand as Expression.Operand.Known).copy(stripEntryId = null)
    )
}
