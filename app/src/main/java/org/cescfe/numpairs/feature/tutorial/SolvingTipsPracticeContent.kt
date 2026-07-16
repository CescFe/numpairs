package org.cescfe.numpairs.feature.tutorial

import org.cescfe.numpairs.R
import org.cescfe.numpairs.domain.puzzle.model.Operator
import org.cescfe.numpairs.domain.puzzle.model.StripItem

internal object SolvingTipsPracticeContent {
    val scenario: TutorialScenario = solvingTipsPracticeScenario()

    val steps: List<TutorialStep> = listOf(
        TutorialStep(
            order = 6,
            scenarioId = TutorialScenarioId.SOLVING_TIPS_PRACTICE,
            playerFacingCopyResId = R.string.tutorial_solving_tips_step_one_copy,
            highlightedTargets = listOf(
                TutorialHighlightTarget.StripEntries(indexes = listOf(1)),
                TutorialHighlightTarget.TileExpressionSlots(tileIndex = 0)
            ),
            requiredAction = TutorialRequiredAction.CompleteTileExpressionsInOrder(
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
            completionPredicate = TutorialStepCompletionPredicate.TileExpressionsCompleted(
                expressions = listOf(
                    TutorialStepCompletionPredicate.TileExpressionCompleted(
                        tileIndex = 0,
                        leftValue = 2,
                        operator = Operator.ADDITION,
                        rightValue = 3
                    ),
                    TutorialStepCompletionPredicate.TileExpressionCompleted(
                        tileIndex = 1,
                        leftValue = 2,
                        operator = Operator.MULTIPLICATION,
                        rightValue = 3
                    )
                )
            )
        ),
        TutorialStep(
            order = 7,
            scenarioId = TutorialScenarioId.SOLVING_TIPS_PRACTICE,
            playerFacingCopyResId = R.string.tutorial_solving_tips_step_two_copy,
            highlightedTargets = listOf(
                TutorialHighlightTarget.StripEntries(indexes = listOf(2)),
                TutorialHighlightTarget.TileExpressionSlots(tileIndex = 2),
                TutorialHighlightTarget.TileExpressionSlots(tileIndex = 3)
            ),
            requiredAction = TutorialRequiredAction.CompleteTileExpressions(
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
            completionPredicate = TutorialStepCompletionPredicate.ScenarioSolved
        )
    )

    private fun solvingTipsPracticeScenario(): TutorialScenario {
        val stripValues = listOf(2, 3, 4, 8)
        val solvedPuzzle = solvedPuzzle(
            stripValues = stripValues,
            tileDefinitions = listOf(
                TileDefinition(leftStripEntryId = 0, operator = Operator.ADDITION, rightStripEntryId = 1),
                TileDefinition(leftStripEntryId = 0, operator = Operator.MULTIPLICATION, rightStripEntryId = 1),
                TileDefinition(leftStripEntryId = 2, operator = Operator.MULTIPLICATION, rightStripEntryId = 3),
                TileDefinition(leftStripEntryId = 2, operator = Operator.ADDITION, rightStripEntryId = 3)
            )
        )

        return TutorialScenario(
            id = TutorialScenarioId.SOLVING_TIPS_PRACTICE,
            stripValues = stripValues,
            initialPuzzle = puzzle(
                stripItems = listOf(
                    StripItem.Known(2),
                    StripItem.Hidden,
                    StripItem.Hidden,
                    StripItem.Known(8)
                ),
                tiles = listOf(
                    hiddenTile(result = 5),
                    hiddenTile(result = 6),
                    hiddenTile(result = 32),
                    hiddenTile(result = 12)
                )
            ),
            solvedPuzzle = solvedPuzzle,
            intendedPairs = listOf(
                TutorialIntendedPair(firstStripEntryId = 0, secondStripEntryId = 1),
                TutorialIntendedPair(firstStripEntryId = 2, secondStripEntryId = 3)
            )
        )
    }
}
