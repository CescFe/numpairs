package org.cescfe.numpairs.feature.tutorial

import org.cescfe.numpairs.domain.puzzle.model.Operator
import org.cescfe.numpairs.domain.puzzle.model.StripItem

internal object FinalValidationContent {
    val scenario: TutorialScenario = finalValidationScenario()

    private fun finalValidationScenario(): TutorialScenario {
        val stripValues = listOf(2, 3, 4, 5)

        return TutorialScenario(
            id = TutorialScenarioId.FINAL_VALIDATION,
            stripValues = stripValues,
            initialPuzzle = puzzle(
                stripItems = listOf(
                    StripItem.Known(2),
                    StripItem.Hidden,
                    StripItem.Known(4),
                    StripItem.Known(5)
                ),
                tiles = hiddenTiles(results = listOf(5, 6, 9, 20))
            ),
            solvedPuzzle = solvedPuzzle(
                stripValues = stripValues,
                tileDefinitions = listOf(
                    TileDefinition(leftStripEntryId = 0, operator = Operator.ADDITION, rightStripEntryId = 1),
                    TileDefinition(
                        leftStripEntryId = 0,
                        operator = Operator.MULTIPLICATION,
                        rightStripEntryId = 1
                    ),
                    TileDefinition(leftStripEntryId = 2, operator = Operator.ADDITION, rightStripEntryId = 3),
                    TileDefinition(
                        leftStripEntryId = 2,
                        operator = Operator.MULTIPLICATION,
                        rightStripEntryId = 3
                    )
                )
            ),
            intendedPairs = listOf(
                TutorialIntendedPair(firstStripEntryId = 0, secondStripEntryId = 1),
                TutorialIntendedPair(firstStripEntryId = 2, secondStripEntryId = 3)
            )
        )
    }
}
