package org.cescfe.numpairs.feature.tutorial

import org.cescfe.numpairs.R
import org.cescfe.numpairs.domain.puzzle.model.Operator
import org.cescfe.numpairs.domain.puzzle.model.StripItem

internal object StageTwoComplementaryPairContent {
    val scenario: TutorialScenario = complementaryPairScenario()

    private val additionExpression = TutorialRequiredAction.CompleteTileExpression(
        tileIndex = ADDITION_TILE_INDEX,
        leftStripEntryId = FIRST_STRIP_ENTRY_ID,
        operator = Operator.ADDITION,
        rightStripEntryId = SECOND_STRIP_ENTRY_ID
    )
    private val multiplicationExpression = TutorialRequiredAction.CompleteTileExpression(
        tileIndex = MULTIPLICATION_TILE_INDEX,
        leftStripEntryId = FIRST_STRIP_ENTRY_ID,
        operator = Operator.MULTIPLICATION,
        rightStripEntryId = SECOND_STRIP_ENTRY_ID
    )

    val steps: List<TutorialStep> = listOf(
        TutorialStep(
            order = 2,
            scenarioId = TutorialScenarioId.COMPLEMENTARY_PAIR,
            playerFacingCopyResId = R.string.tutorial_stage_two_complete_sum_copy,
            highlightedTargets = listOf(
                TutorialHighlightTarget.TileExpressionSlots(tileIndex = ADDITION_TILE_INDEX)
            ),
            requiredAction = additionExpression,
            completionPredicate = additionExpression.toCompletionPredicate()
        ),
        TutorialStep(
            order = 3,
            scenarioId = TutorialScenarioId.COMPLEMENTARY_PAIR,
            playerFacingCopyResId = R.string.tutorial_stage_two_complete_product_copy,
            highlightedTargets = listOf(
                TutorialHighlightTarget.TileExpressionSlots(tileIndex = MULTIPLICATION_TILE_INDEX)
            ),
            requiredAction = multiplicationExpression,
            completionPredicate = TutorialStepCompletionPredicate.TileExpressionsCompleted(
                expressions = listOf(
                    additionExpression.toCompletionPredicate(),
                    multiplicationExpression.toCompletionPredicate()
                )
            )
        )
    )

    private fun complementaryPairScenario(): TutorialScenario {
        val stripValues = listOf(2, 3)

        return TutorialScenario(
            id = TutorialScenarioId.COMPLEMENTARY_PAIR,
            stripValues = stripValues,
            initialPuzzle = puzzle(
                stripItems = stripValues.map(StripItem::Known),
                tiles = hiddenTiles(results = listOf(5, 6))
            ),
            solvedPuzzle = solvedPuzzle(
                stripValues = stripValues,
                tileDefinitions = listOf(
                    TileDefinition(
                        leftStripEntryId = FIRST_STRIP_ENTRY_ID,
                        operator = Operator.ADDITION,
                        rightStripEntryId = SECOND_STRIP_ENTRY_ID
                    ),
                    TileDefinition(
                        leftStripEntryId = FIRST_STRIP_ENTRY_ID,
                        operator = Operator.MULTIPLICATION,
                        rightStripEntryId = SECOND_STRIP_ENTRY_ID
                    )
                )
            ),
            intendedPairs = listOf(
                TutorialIntendedPair(
                    firstStripEntryId = FIRST_STRIP_ENTRY_ID,
                    secondStripEntryId = SECOND_STRIP_ENTRY_ID
                )
            )
        )
    }

    private fun TutorialRequiredAction.CompleteTileExpression.toCompletionPredicate() =
        TutorialStepCompletionPredicate.TileExpressionCompleted(
            tileIndex = tileIndex,
            leftValue = scenario.stripValues[leftStripEntryId],
            operator = operator,
            rightValue = scenario.stripValues[rightStripEntryId]
        )

    private const val FIRST_STRIP_ENTRY_ID = 0
    private const val SECOND_STRIP_ENTRY_ID = 1
    private const val ADDITION_TILE_INDEX = 0
    private const val MULTIPLICATION_TILE_INDEX = 1
}
