package org.cescfe.numpairs.feature.tutorial

import org.cescfe.numpairs.R
import org.cescfe.numpairs.domain.puzzle.model.Operator
import org.cescfe.numpairs.domain.puzzle.model.StripItem

internal object LearnBasicsTutorialContent {
    val scenario: TutorialScenario = twoPairPracticeScenario()

    val steps: List<TutorialStep> = listOf(
        TutorialStep(
            order = 2,
            scenarioId = TutorialScenarioId.TWO_PAIR_PRACTICE,
            playerFacingCopyResId = R.string.tutorial_step_one_copy,
            highlightedTargets = listOf(
                TutorialHighlightTarget.StripEntries(indexes = listOf(1))
            ),
            requiredAction = TutorialRequiredAction.EnterStripValue(
                stripEntryIndex = 1,
                value = 2
            ),
            completionPredicate = TutorialStepCompletionPredicate.StripValueEntered(
                stripEntryIndex = 1,
                value = 2
            )
        ),
        TutorialStep(
            order = 3,
            scenarioId = TutorialScenarioId.TWO_PAIR_PRACTICE,
            playerFacingCopyResId = R.string.tutorial_step_two_copy,
            highlightedTargets = listOf(
                TutorialHighlightTarget.TileExpressionSlots(tileIndex = 0)
            ),
            requiredAction = TutorialRequiredAction.CompleteTileExpression(
                tileIndex = 0,
                leftStripEntryId = 0,
                operator = Operator.ADDITION,
                rightStripEntryId = 1
            ),
            completionPredicate = TutorialStepCompletionPredicate.TileExpressionCompleted(
                tileIndex = 0,
                leftValue = 1,
                operator = Operator.ADDITION,
                rightValue = 2
            )
        ),
        TutorialStep(
            order = 4,
            scenarioId = TutorialScenarioId.TWO_PAIR_PRACTICE,
            playerFacingCopyResId = R.string.tutorial_step_three_copy,
            highlightedTargets = listOf(
                TutorialHighlightTarget.TileExpressionSlots(tileIndex = 1)
            ),
            requiredAction = TutorialRequiredAction.CompleteTileExpression(
                tileIndex = 1,
                leftStripEntryId = 0,
                operator = Operator.MULTIPLICATION,
                rightStripEntryId = 1
            ),
            completionPredicate = TutorialStepCompletionPredicate.TileExpressionCompleted(
                tileIndex = 1,
                leftValue = 1,
                operator = Operator.MULTIPLICATION,
                rightValue = 2
            )
        ),
        TutorialStep(
            order = 5,
            scenarioId = TutorialScenarioId.TWO_PAIR_PRACTICE,
            playerFacingCopyResId = R.string.tutorial_step_four_copy,
            highlightedTargets = listOf(
                TutorialHighlightTarget.TileExpressionSlots(tileIndex = 2),
                TutorialHighlightTarget.TileExpressionSlots(tileIndex = 3)
            ),
            requiredAction = TutorialRequiredAction.CompleteScenario,
            completionPredicate = TutorialStepCompletionPredicate.ScenarioSolved
        )
    )

    private fun twoPairPracticeScenario(): TutorialScenario {
        val stripValues = listOf(1, 2, 3, 4)

        return TutorialScenario(
            id = TutorialScenarioId.TWO_PAIR_PRACTICE,
            stripValues = stripValues,
            initialPuzzle = puzzle(
                stripItems = listOf(
                    StripItem.Known(1),
                    StripItem.Hidden,
                    StripItem.Known(3),
                    StripItem.Known(4)
                ),
                tiles = hiddenTiles(results = listOf(3, 2, 7, 12))
            ),
            solvedPuzzle = solvedPuzzle(
                stripValues = stripValues,
                tileDefinitions = listOf(
                    TileDefinition(leftStripEntryId = 0, operator = Operator.ADDITION, rightStripEntryId = 1),
                    TileDefinition(leftStripEntryId = 0, operator = Operator.MULTIPLICATION, rightStripEntryId = 1),
                    TileDefinition(leftStripEntryId = 2, operator = Operator.ADDITION, rightStripEntryId = 3),
                    TileDefinition(leftStripEntryId = 2, operator = Operator.MULTIPLICATION, rightStripEntryId = 3)
                )
            ),
            intendedPairs = listOf(
                TutorialIntendedPair(firstStripEntryId = 0, secondStripEntryId = 1),
                TutorialIntendedPair(firstStripEntryId = 2, secondStripEntryId = 3)
            )
        )
    }
}
