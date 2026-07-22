package org.cescfe.numpairs.feature.tutorial

import org.cescfe.numpairs.R
import org.cescfe.numpairs.domain.puzzle.model.Operator
import org.cescfe.numpairs.domain.puzzle.model.StripItem

internal object LearnBasicsTutorialContent {
    private val stripAndTilesIntroductionScenario = stripAndTilesIntroductionScenario()
    private val repeatedValuePracticeScenario = repeatedValuePracticeScenario()
    private val completedIntroductionStripPuzzle = stripAndTilesIntroductionScenario.initialPuzzle.copy(
        strip = stripAndTilesIntroductionScenario.initialPuzzle.strip.withUpdatedEntry(index = 1, value = 3)
    )

    val scenarios: List<TutorialScenario> = listOf(
        stripAndTilesIntroductionScenario,
        repeatedValuePracticeScenario
    )

    val steps: List<TutorialStep> = listOf(
        TutorialStep(
            scenarioId = TutorialScenarioId.STRIP_AND_TILES_INTRODUCTION,
            playerFacingCopyResId = R.string.tutorial_strip_introduction_copy,
            highlightedTargets = listOf(
                TutorialHighlightTarget.StripEntries(indexes = listOf(1))
            ),
            requiredAction = TutorialRequiredAction.EnterStripValue(
                stripEntryIndex = 1,
                value = 3
            ),
            completionPredicate = TutorialStepCompletionPredicate.StripValueEntered(
                stripEntryIndex = 1,
                value = 3
            ),
            progressCheckpoint = TutorialProgressCheckpoint.STRIP_INTRODUCTION_COMPLETED,
            isBoardVisible = false,
            stripEntryGuidanceResId = R.string.tutorial_strip_entry_guidance
        ),
        TutorialStep(
            scenarioId = TutorialScenarioId.STRIP_AND_TILES_INTRODUCTION,
            playerFacingCopyResId = R.string.tutorial_tiles_introduction_copy,
            highlightedTargets = listOf(
                TutorialHighlightTarget.TileExpressionSlots(tileIndex = 0),
                TutorialHighlightTarget.WholeTile(tileIndex = 1)
            ),
            requiredAction = TutorialRequiredAction.CompleteTileWithNormalInteractions(tileIndex = 0),
            completionPredicate = TutorialStepCompletionPredicate.TileExpressionCompleted(
                tileIndex = 0,
                leftValue = 2,
                operator = Operator.ADDITION,
                rightValue = 3
            ),
            progressCheckpoint = TutorialProgressCheckpoint.TILES_INTRODUCTION_COMPLETED,
            entryPuzzle = completedIntroductionStripPuzzle
        ),
        TutorialStep(
            scenarioId = TutorialScenarioId.REPEATED_VALUE_PRACTICE,
            playerFacingCopyResId = R.string.tutorial_repeated_value_practice_copy,
            highlightedTargets = listOf(
                TutorialHighlightTarget.HiddenStripEntries,
                TutorialHighlightTarget.HiddenTileExpressions
            ),
            requiredAction = TutorialRequiredAction.CompleteScenario,
            completionPredicate = TutorialStepCompletionPredicate.ScenarioSolved
        )
    )

    private fun stripAndTilesIntroductionScenario(): TutorialScenario {
        val stripValues = listOf(2, 3, 4, 5)
        val solvedPuzzle = solvedPuzzle(
            stripValues = stripValues,
            tileDefinitions = listOf(
                TileDefinition(leftStripEntryId = 0, operator = Operator.ADDITION, rightStripEntryId = 1),
                TileDefinition(leftStripEntryId = 0, operator = Operator.MULTIPLICATION, rightStripEntryId = 1),
                TileDefinition(leftStripEntryId = 2, operator = Operator.ADDITION, rightStripEntryId = 3),
                TileDefinition(leftStripEntryId = 2, operator = Operator.MULTIPLICATION, rightStripEntryId = 3)
            )
        )

        return TutorialScenario(
            id = TutorialScenarioId.STRIP_AND_TILES_INTRODUCTION,
            stripValues = stripValues,
            initialPuzzle = puzzle(
                stripItems = listOf(
                    StripItem.Known(2),
                    StripItem.Hidden,
                    StripItem.Known(4),
                    StripItem.Known(5)
                ),
                tiles = listOf(hiddenTile(result = 5)) + solvedPuzzle.board.tiles.drop(1)
            ),
            solvedPuzzle = solvedPuzzle
        )
    }

    private fun repeatedValuePracticeScenario(): TutorialScenario {
        val stripValues = listOf(1, 2, 2, 3)

        return TutorialScenario(
            id = TutorialScenarioId.REPEATED_VALUE_PRACTICE,
            stripValues = stripValues,
            initialPuzzle = puzzle(
                stripItems = listOf(
                    StripItem.Hidden,
                    StripItem.Hidden,
                    StripItem.Known(2),
                    StripItem.Known(3)
                ),
                tiles = hiddenTiles(results = listOf(3, 2, 5, 6))
            ),
            solvedPuzzle = solvedPuzzle(
                stripValues = stripValues,
                tileDefinitions = listOf(
                    TileDefinition(leftStripEntryId = 0, operator = Operator.ADDITION, rightStripEntryId = 1),
                    TileDefinition(leftStripEntryId = 0, operator = Operator.MULTIPLICATION, rightStripEntryId = 1),
                    TileDefinition(leftStripEntryId = 2, operator = Operator.ADDITION, rightStripEntryId = 3),
                    TileDefinition(leftStripEntryId = 2, operator = Operator.MULTIPLICATION, rightStripEntryId = 3)
                )
            )
        )
    }
}
