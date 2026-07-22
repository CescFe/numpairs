package org.cescfe.numpairs.feature.tutorial

import org.cescfe.numpairs.R
import org.cescfe.numpairs.domain.puzzle.model.Board
import org.cescfe.numpairs.domain.puzzle.model.Expression
import org.cescfe.numpairs.domain.puzzle.model.OperandSlot
import org.cescfe.numpairs.domain.puzzle.model.Operator
import org.cescfe.numpairs.domain.puzzle.model.Puzzle
import org.cescfe.numpairs.domain.puzzle.model.PuzzleCompletionState
import org.cescfe.numpairs.domain.puzzle.model.StripItem
import org.cescfe.numpairs.domain.puzzle.model.Tile
import org.cescfe.numpairs.feature.game.GameTileExpressionSlot
import org.cescfe.numpairs.feature.game.GameTileExpressionSlotHighlight
import org.cescfe.numpairs.feature.game.presentation.GameUiState
import org.cescfe.numpairs.feature.game.presentation.TileUiState
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class TutorialContentTest {
    @Test
    fun exposes_authored_content_for_each_learning_flow() {
        assertEquals(
            listOf(
                TutorialScenarioId.STRIP_AND_TILES_INTRODUCTION,
                TutorialScenarioId.REPEATED_VALUE_PRACTICE,
                TutorialScenarioId.SOLVING_TIPS_PRACTICE
            ),
            TutorialContent.scenarios.map(TutorialScenario::id)
        )
        assertEquals(
            TutorialContent.learnBasicsSteps,
            TutorialContent.stepsFor(TutorialMode.LEARN_BASICS)
        )
        assertEquals(
            TutorialContent.solvingTipsPracticeSteps,
            TutorialContent.stepsFor(TutorialMode.SOLVING_TIPS_PRACTICE)
        )
        assertEquals(
            setOf(
                TutorialScenarioId.STRIP_AND_TILES_INTRODUCTION,
                TutorialScenarioId.REPEATED_VALUE_PRACTICE
            ),
            TutorialContent.stepsFor(TutorialMode.LEARN_BASICS).map(TutorialStep::scenarioId).toSet()
        )
        assertEquals(
            setOf(TutorialScenarioId.SOLVING_TIPS_PRACTICE),
            TutorialContent.stepsFor(TutorialMode.SOLVING_TIPS_PRACTICE).map(TutorialStep::scenarioId).toSet()
        )
    }

    @Test
    fun authored_scenarios_are_valid_puzzles_with_hidden_work_for_the_player() {
        TutorialContent.scenarios.forEach { scenario ->
            assertEquals(PuzzleCompletionState.INCOMPLETE, scenario.initialPuzzle.completionState)
            assertEquals(PuzzleCompletionState.SOLVED, scenario.solvedPuzzle.completionState)
            assertEquals(scenario.stripValues.size, scenario.initialPuzzle.strip.entries.size)
            assertEquals(scenario.stripValues.size, scenario.initialPuzzle.board.tiles.size)
            assertEquals(scenario.stripValues.map(StripItem::Known), scenario.solvedPuzzle.strip.items)
            assertEquals(
                scenario.initialPuzzle.board.tiles.map(Tile::result),
                scenario.solvedPuzzle.board.tiles.map(Tile::result)
            )
        }
    }

    @Test
    fun practice_scenarios_keep_hidden_strip_and_expression_work() {
        listOf(
            TutorialScenarioId.REPEATED_VALUE_PRACTICE,
            TutorialScenarioId.SOLVING_TIPS_PRACTICE
        ).forEach { scenarioId ->
            val scenario = TutorialContent.scenario(scenarioId)

            assertTrue(scenario.initialPuzzle.strip.items.any { stripItem -> stripItem == StripItem.Hidden })
            assertAllTilesHaveHiddenExpressions(scenario.initialPuzzle)
        }
    }

    @Test
    fun learn_basics_adds_manual_explanation_before_the_existing_guided_curriculum() {
        val introductionScenario = TutorialContent.scenario(TutorialScenarioId.STRIP_AND_TILES_INTRODUCTION)
        val repeatedValueScenario = TutorialContent.scenario(TutorialScenarioId.REPEATED_VALUE_PRACTICE)
        val steps = TutorialContent.stepsFor(TutorialMode.LEARN_BASICS)
        val guidedIntroductionPuzzle = requireNotNull(steps[4].entryPuzzle)
        val introductionWithCompletedStrip = guidedIntroductionPuzzle.withStripValue(index = 1, value = 3)

        assertEquals(
            listOf(
                TutorialScenarioId.STRIP_AND_TILES_INTRODUCTION,
                TutorialScenarioId.STRIP_AND_TILES_INTRODUCTION,
                TutorialScenarioId.STRIP_AND_TILES_INTRODUCTION,
                TutorialScenarioId.STRIP_AND_TILES_INTRODUCTION,
                TutorialScenarioId.STRIP_AND_TILES_INTRODUCTION,
                TutorialScenarioId.STRIP_AND_TILES_INTRODUCTION,
                TutorialScenarioId.REPEATED_VALUE_PRACTICE
            ),
            steps.map(TutorialStep::scenarioId)
        )
        assertEquals(
            listOf(
                null,
                null,
                null,
                null,
                TutorialProgressCheckpoint.STRIP_INTRODUCTION_COMPLETED,
                TutorialProgressCheckpoint.TILES_INTRODUCTION_COMPLETED,
                null
            ),
            steps.map(TutorialStep::progressCheckpoint)
        )

        assertEquals(
            listOf(
                R.string.tutorial_objective_explanation_copy,
                R.string.tutorial_strip_explanation_copy,
                R.string.tutorial_tile_explanation_copy,
                R.string.tutorial_pair_explanation_copy
            ),
            steps.take(4).map(TutorialStep::playerFacingCopyResId)
        )
        assertEquals(
            listOf(
                listOf(
                    TutorialHighlightTarget.StripEntries(indexes = listOf(0, 1, 2, 3)),
                    TutorialHighlightTarget.WholeTile(tileIndex = 0),
                    TutorialHighlightTarget.WholeTile(tileIndex = 1),
                    TutorialHighlightTarget.WholeTile(tileIndex = 2),
                    TutorialHighlightTarget.WholeTile(tileIndex = 3)
                ),
                listOf(
                    TutorialHighlightTarget.StripEntries(indexes = listOf(0, 1, 2, 3))
                ),
                listOf(
                    TutorialHighlightTarget.WholeTile(tileIndex = 0)
                ),
                listOf(
                    TutorialHighlightTarget.StripEntries(indexes = listOf(2, 3)),
                    TutorialHighlightTarget.WholeTile(tileIndex = 2),
                    TutorialHighlightTarget.WholeTile(tileIndex = 3)
                )
            ),
            steps.take(4).map(TutorialStep::highlightedTargets)
        )
        steps.take(4).forEach { step ->
            assertEquals(TutorialRequiredAction.NoInteraction, step.requiredAction)
            assertEquals(TutorialStepCompletionPredicate.ManualAdvance, step.completionPredicate)
            assertTrue(step.isBoardVisible)
            assertEquals(introductionScenario.initialPuzzle, step.entryPuzzle ?: introductionScenario.initialPuzzle)
        }

        steps[4].assertGuidedAction(
            expectedHighlights = listOf(
                TutorialHighlightTarget.StripEntries(indexes = listOf(1))
            ),
            expectedAction = TutorialRequiredAction.EnterStripValue(stripEntryIndex = 1, value = 3),
            incompletePuzzle = guidedIntroductionPuzzle,
            completePuzzle = introductionWithCompletedStrip
        )
        assertFalse(steps[4].isBoardVisible)
        assertEquals(R.string.tutorial_strip_entry_guidance, steps[4].stripEntryGuidanceResId)

        steps[5].assertGuidedAction(
            expectedHighlights = listOf(
                TutorialHighlightTarget.TileExpressionSlots(tileIndex = 0),
                TutorialHighlightTarget.WholeTile(tileIndex = 1)
            ),
            expectedAction = TutorialRequiredAction.CompleteTileWithNormalInteractions(tileIndex = 0),
            incompletePuzzle = introductionWithCompletedStrip,
            completePuzzle = introductionWithCompletedStrip.withSolvedScenarioTiles(introductionScenario, 0)
        )
        assertTrue(steps[5].isBoardVisible)
        assertEquals(null, steps[5].stripEntryGuidanceResId)
        assertEquals(introductionWithCompletedStrip, steps[5].entryPuzzle)

        steps[6].assertGuidedAction(
            expectedHighlights = listOf(
                TutorialHighlightTarget.HiddenStripEntries,
                TutorialHighlightTarget.HiddenTileExpressions
            ),
            expectedAction = TutorialRequiredAction.CompleteScenario,
            incompletePuzzle = repeatedValueScenario.initialPuzzle,
            completePuzzle = repeatedValueScenario.solvedPuzzle
        )
    }

    @Test
    fun manual_explanation_freezes_every_puzzle_interaction() {
        val scenario = TutorialContent.scenario(TutorialScenarioId.STRIP_AND_TILES_INTRODUCTION)

        TutorialContent.learnBasicsSteps.take(4).forEach { step ->
            val policy = step.toInteractionPolicy(scenario = scenario, uiState = null)

            scenario.stripValues.indices.forEach { index ->
                assertFalse(policy.canTapStripItem(index))
            }
            scenario.initialPuzzle.board.tiles.indices.forEach { index ->
                assertFalse(policy.canTapTileLeftOperand(index))
                assertFalse(policy.canTapTileRightOperand(index))
                assertFalse(policy.canTapTileOperator(index))
                assertFalse(policy.canTapTileReset(index))
            }
        }
    }

    @Test
    fun learn_basics_step_two_highlights_the_editable_slots_and_complementary_product_tile() {
        val scenario = TutorialContent.scenario(TutorialScenarioId.STRIP_AND_TILES_INTRODUCTION)
        val step = TutorialContent.learnBasicsSteps[5]

        val highlightState = step.toHighlightState(scenario = scenario, uiState = null)

        assertEquals(setOf(1), highlightState.tileIndexes)
        assertEquals(
            GameTileExpressionSlot.entries.mapTo(mutableSetOf()) { slot ->
                GameTileExpressionSlotHighlight(tileIndex = 0, slot = slot)
            },
            highlightState.tileExpressionSlots
        )
        assertFalse(highlightState.isTileHighlighted(index = 0))
        assertFalse(highlightState.isTileHighlighted(index = 2))
        assertFalse(highlightState.isTileHighlighted(index = 3))
    }

    @Test
    fun learn_basics_step_two_keeps_normal_choices_for_the_target_tile() {
        val scenario = TutorialContent.scenario(TutorialScenarioId.STRIP_AND_TILES_INTRODUCTION)
        val policy = TutorialContent.learnBasicsSteps[5].toInteractionPolicy(scenario = scenario, uiState = null)

        OperandSlot.entries.forEach { slot ->
            assertTrue(policy.canConfirmTileOperand(0, slot, 0))
            assertTrue(policy.canConfirmTileOperand(0, slot, 1))
            assertFalse(policy.canConfirmTileOperand(1, slot, 0))
        }
        assertTrue(policy.canTapTileLeftOperand(0))
        assertTrue(policy.canTapTileRightOperand(0))
        assertTrue(policy.canTapTileOperator(0))
        assertTrue(policy.canTapTileReset(0))
        assertTrue(policy.canConfirmTileOperator(0, Operator.ADDITION))
        assertTrue(policy.canConfirmTileOperator(0, Operator.MULTIPLICATION))
        assertFalse(policy.canTapStripItem(0))
        assertFalse(policy.canTapTileLeftOperand(1))
        assertFalse(policy.canTapTileRightOperand(1))
        assertFalse(policy.canTapTileOperator(1))
        assertFalse(policy.canTapTileReset(1))
        assertFalse(policy.canConfirmTileOperator(1, Operator.ADDITION))
    }

    @Test
    fun learn_basics_step_two_accepts_the_pair_in_either_operand_order() {
        val step = TutorialContent.learnBasicsSteps[5]

        assertTrue(step.isComplete(stepTwoUiState(leftValue = 2, operator = Operator.ADDITION, rightValue = 3)))
        assertTrue(step.isComplete(stepTwoUiState(leftValue = 3, operator = Operator.ADDITION, rightValue = 2)))
        assertFalse(step.isComplete(stepTwoUiState(leftValue = 3, operator = Operator.MULTIPLICATION, rightValue = 2)))
        assertFalse(step.isComplete(stepTwoUiState(leftValue = 2, operator = Operator.ADDITION, rightValue = 4)))
    }

    @Test
    fun strip_and_tiles_introduction_has_the_exact_authored_state() {
        val scenario = TutorialContent.scenario(TutorialScenarioId.STRIP_AND_TILES_INTRODUCTION)
        val tiles = scenario.initialPuzzle.board.tiles

        assertEquals(listOf(2, 3, 4, 5), scenario.stripValues)
        assertEquals(
            listOf(StripItem.Known(2), StripItem.Hidden, StripItem.Known(4), StripItem.Known(5)),
            scenario.initialPuzzle.strip.items
        )
        assertAllTilesHaveHiddenExpressions(scenario.initialPuzzle)
        assertEquals(listOf(5, 6, 9, 20), tiles.map(Tile::result))

        val guidedEntryPuzzle = requireNotNull(TutorialContent.learnBasicsSteps[4].entryPuzzle)
        assertTrue(guidedEntryPuzzle.board.tiles[0].hasHiddenExpression())
        assertEquals(
            Expression(2, Operator.MULTIPLICATION, 3),
            guidedEntryPuzzle.board.tiles[1].expression.withoutEntryIds()
        )
        assertEquals(
            Expression(4, Operator.ADDITION, 5),
            guidedEntryPuzzle.board.tiles[2].expression.withoutEntryIds()
        )
        assertEquals(
            Expression(4, Operator.MULTIPLICATION, 5),
            guidedEntryPuzzle.board.tiles[3].expression.withoutEntryIds()
        )
    }

    @Test
    fun repeated_value_practice_has_the_exact_authored_state_and_accepts_both_two_assignments() {
        val scenario = TutorialContent.scenario(TutorialScenarioId.REPEATED_VALUE_PRACTICE)
        val finalStep = TutorialContent.learnBasicsSteps.last()
        val alternateSolvedPuzzle = solvedPuzzle(
            stripValues = listOf(1, 2, 2, 3),
            tileDefinitions = listOf(
                TileDefinition(leftStripEntryId = 0, operator = Operator.ADDITION, rightStripEntryId = 2),
                TileDefinition(leftStripEntryId = 0, operator = Operator.MULTIPLICATION, rightStripEntryId = 2),
                TileDefinition(leftStripEntryId = 1, operator = Operator.ADDITION, rightStripEntryId = 3),
                TileDefinition(leftStripEntryId = 1, operator = Operator.MULTIPLICATION, rightStripEntryId = 3)
            )
        )

        assertEquals(listOf(1, 2, 2, 3), scenario.stripValues)
        assertEquals(
            listOf(StripItem.Hidden, StripItem.Hidden, StripItem.Known(2), StripItem.Known(3)),
            scenario.initialPuzzle.strip.items
        )
        assertEquals(listOf(3, 2, 5, 6), scenario.initialPuzzle.board.tiles.map(Tile::result))
        assertAllTilesHaveHiddenExpressions(scenario.initialPuzzle)
        assertEquals(PuzzleCompletionState.SOLVED, alternateSolvedPuzzle.completionState)
        assertTrue(finalStep.isComplete(GameUiState.from(scenario.solvedPuzzle)))
        assertTrue(finalStep.isComplete(GameUiState.from(alternateSolvedPuzzle)))
    }

    @Test
    fun solving_tips_practice_requires_the_first_pair_before_the_remaining_pair() {
        val scenario = TutorialContent.scenario(TutorialScenarioId.SOLVING_TIPS_PRACTICE)
        val steps = TutorialContent.stepsFor(TutorialMode.SOLVING_TIPS_PRACTICE)

        steps[0].assertGuidedAction(
            expectedHighlights = listOf(
                TutorialHighlightTarget.StripEntries(indexes = listOf(1)),
                TutorialHighlightTarget.TileExpressionSlots(tileIndex = 0)
            ),
            expectedAction = TutorialRequiredAction.CompleteTileExpressionsInOrder(
                expressions = listOf(
                    TutorialRequiredAction.CompleteTileExpression(
                        tileIndex = 0,
                        leftStripEntryId = 0,
                        operator = Operator.ADDITION,
                        rightStripEntryId = 1
                    ),
                    TutorialRequiredAction.CompleteTileExpression(
                        tileIndex = 1,
                        leftStripEntryId = 0,
                        operator = Operator.MULTIPLICATION,
                        rightStripEntryId = 1
                    )
                )
            ),
            incompletePuzzle = scenario.initialPuzzle,
            completePuzzle = scenario.initialPuzzle.withSolvedScenarioTiles(scenario, 0, 1)
        )
        assertFalse(
            steps[0].isComplete(
                GameUiState.from(scenario.initialPuzzle.withSolvedScenarioTiles(scenario, 0))
            )
        )

        steps[1].assertGuidedAction(
            expectedHighlights = listOf(
                TutorialHighlightTarget.StripEntries(indexes = listOf(2)),
                TutorialHighlightTarget.TileExpressionSlots(tileIndex = 2),
                TutorialHighlightTarget.TileExpressionSlots(tileIndex = 3)
            ),
            expectedAction = TutorialRequiredAction.CompleteTileExpressions(
                expressions = listOf(
                    TutorialRequiredAction.CompleteTileExpression(
                        tileIndex = 2,
                        leftStripEntryId = 2,
                        operator = Operator.MULTIPLICATION,
                        rightStripEntryId = 3
                    ),
                    TutorialRequiredAction.CompleteTileExpression(
                        tileIndex = 3,
                        leftStripEntryId = 2,
                        operator = Operator.ADDITION,
                        rightStripEntryId = 3
                    )
                )
            ),
            incompletePuzzle = scenario.initialPuzzle,
            completePuzzle = scenario.solvedPuzzle
        )
    }

    @Test
    fun solving_tips_practice_keeps_its_guided_expression_choices() {
        val scenario = TutorialContent.scenario(TutorialScenarioId.SOLVING_TIPS_PRACTICE)
        val policy = TutorialContent.solvingTipsPracticeSteps[0].toInteractionPolicy(
            scenario = scenario,
            uiState = null
        )

        assertTrue(policy.canConfirmTileOperand(0, OperandSlot.LEFT, 0))
        assertFalse(policy.canConfirmTileOperand(0, OperandSlot.LEFT, 1))
        assertTrue(policy.canConfirmTileOperand(0, OperandSlot.RIGHT, 1))
        assertFalse(policy.canConfirmTileOperand(0, OperandSlot.RIGHT, 0))
        assertTrue(policy.canConfirmTileOperator(0, Operator.ADDITION))
        assertFalse(policy.canConfirmTileOperator(0, Operator.MULTIPLICATION))
    }
}

private fun stepTwoUiState(leftValue: Int, operator: Operator, rightValue: Int): GameUiState = GameUiState(
    stripItems = emptyList(),
    tiles = listOf(
        TileUiState(
            leftOperandLabel = leftValue.toString(),
            operatorLabel = operator.symbol,
            rightOperandLabel = rightValue.toString(),
            resultLabel = "5"
        )
    )
)

private fun assertAllTilesHaveHiddenExpressions(puzzle: Puzzle) {
    assertTrue(
        puzzle.board.tiles.all { tile ->
            tile.hasHiddenExpression()
        }
    )
}

private fun Tile.hasHiddenExpression(): Boolean = expression.leftOperand == Expression.Operand.Hidden &&
    expression.operator == Operator.Hidden &&
    expression.rightOperand == Expression.Operand.Hidden

private fun Expression.withoutEntryIds(): Expression = copy(
    leftOperand = (leftOperand as Expression.Operand.Known).copy(stripEntryId = null),
    rightOperand = (rightOperand as Expression.Operand.Known).copy(stripEntryId = null)
)

private fun Puzzle.withStripValue(index: Int, value: Int): Puzzle = copy(
    strip = strip.withUpdatedEntry(index = index, value = value)
)

private fun Puzzle.withSolvedScenarioTiles(scenario: TutorialScenario, vararg tileIndexes: Int): Puzzle = copy(
    board = Board(
        tiles = board.tiles.toMutableList().apply {
            tileIndexes.forEach { tileIndex ->
                set(tileIndex, scenario.solvedPuzzle.board.tiles[tileIndex])
            }
        }
    )
)

private fun TutorialStep.assertGuidedAction(
    expectedHighlights: List<TutorialHighlightTarget>,
    expectedAction: TutorialRequiredAction,
    incompletePuzzle: Puzzle,
    completePuzzle: Puzzle
) {
    assertEquals(expectedHighlights, highlightedTargets)
    assertEquals(expectedAction, requiredAction)

    assertFalse(isComplete(GameUiState.from(incompletePuzzle)))
    assertTrue(isComplete(GameUiState.from(completePuzzle)))
}
