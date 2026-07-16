package org.cescfe.numpairs.feature.tutorial

import org.cescfe.numpairs.R
import org.cescfe.numpairs.domain.puzzle.model.Operator
import org.cescfe.numpairs.domain.puzzle.model.StripItem

internal object StageThreeHiddenStripValueContent {
    val scenario: TutorialScenario = hiddenStripValueScenario()

    val step: TutorialStep = TutorialStep(
        order = 4,
        scenarioId = TutorialScenarioId.HIDDEN_STRIP_VALUE,
        playerFacingCopyResId = R.string.tutorial_stage_three_hidden_strip_value_copy,
        highlightedTargets = listOf(
            TutorialHighlightTarget.StripEntries(indexes = listOf(HIDDEN_STRIP_ENTRY_INDEX)),
            TutorialHighlightTarget.Tiles(indexes = listOf(CLUE_TILE_INDEX))
        ),
        requiredAction = TutorialRequiredAction.EnterStripValue(
            stripEntryIndex = HIDDEN_STRIP_ENTRY_INDEX,
            value = HIDDEN_STRIP_VALUE
        ),
        completionPredicate = TutorialStepCompletionPredicate.StripValueEntered(
            stripEntryIndex = HIDDEN_STRIP_ENTRY_INDEX,
            value = HIDDEN_STRIP_VALUE
        )
    )

    private fun hiddenStripValueScenario(): TutorialScenario {
        val stripValues = listOf(2, HIDDEN_STRIP_VALUE)
        val solvedPuzzle = solvedPuzzle(
            stripValues = stripValues,
            tileDefinitions = listOf(
                TileDefinition(
                    leftStripEntryId = 0,
                    operator = Operator.ADDITION,
                    rightStripEntryId = HIDDEN_STRIP_ENTRY_INDEX
                ),
                TileDefinition(
                    leftStripEntryId = 0,
                    operator = Operator.MULTIPLICATION,
                    rightStripEntryId = HIDDEN_STRIP_ENTRY_INDEX
                )
            )
        )

        return TutorialScenario(
            id = TutorialScenarioId.HIDDEN_STRIP_VALUE,
            stripValues = stripValues,
            initialPuzzle = puzzle(
                stripItems = listOf(
                    StripItem.Known(stripValues[0]),
                    StripItem.Hidden
                ),
                tiles = solvedPuzzle.board.tiles
            ),
            solvedPuzzle = solvedPuzzle,
            intendedPairs = listOf(
                TutorialIntendedPair(
                    firstStripEntryId = 0,
                    secondStripEntryId = HIDDEN_STRIP_ENTRY_INDEX
                )
            )
        )
    }

    private const val HIDDEN_STRIP_ENTRY_INDEX = 1
    private const val HIDDEN_STRIP_VALUE = 3
    private const val CLUE_TILE_INDEX = 0
}
