package org.cescfe.numpairs.feature.tutorial

import org.cescfe.numpairs.R
import org.cescfe.numpairs.domain.puzzle.model.Expression
import org.cescfe.numpairs.domain.puzzle.model.OperandSlot
import org.cescfe.numpairs.domain.puzzle.model.Operator
import org.cescfe.numpairs.domain.puzzle.model.StripItem

internal object StageOneNumberPlacementContent {
    val scenario: TutorialScenario = numberPlacementScenario()

    val step: TutorialStep = TutorialStep(
        order = 1,
        scenarioId = TutorialScenarioId.NUMBER_PLACEMENT,
        playerFacingCopyResId = R.string.tutorial_stage_one_place_number_copy,
        highlightedTargets = listOf(
            TutorialHighlightTarget.StripEntries(indexes = listOf(REQUIRED_STRIP_ENTRY_ID)),
            TutorialHighlightTarget.TileOperandSlot(
                tileIndex = TARGET_TILE_INDEX,
                slot = OperandSlot.LEFT
            )
        ),
        requiredAction = TutorialRequiredAction.PlaceTileOperand(
            tileIndex = TARGET_TILE_INDEX,
            slot = OperandSlot.LEFT,
            stripEntryId = REQUIRED_STRIP_ENTRY_ID
        ),
        completionPredicate = TutorialStepCompletionPredicate.TileOperandPlaced(
            tileIndex = TARGET_TILE_INDEX,
            slot = OperandSlot.LEFT,
            value = REQUIRED_VALUE
        )
    )

    private fun numberPlacementScenario(): TutorialScenario {
        val stripValues = listOf(2, 3)
        val solvedPuzzle = solvedPuzzle(
            stripValues = stripValues,
            tileDefinitions = listOf(
                TileDefinition(leftStripEntryId = 0, operator = Operator.ADDITION, rightStripEntryId = 1),
                TileDefinition(leftStripEntryId = 0, operator = Operator.MULTIPLICATION, rightStripEntryId = 1)
            )
        )
        val additionTile = solvedPuzzle.board.tiles[TARGET_TILE_INDEX]

        return TutorialScenario(
            id = TutorialScenarioId.NUMBER_PLACEMENT,
            stripValues = stripValues,
            initialPuzzle = puzzle(
                stripItems = stripValues.map(StripItem::Known),
                tiles = listOf(
                    additionTile.copy(
                        expression = additionTile.expression.copy(
                            leftOperand = Expression.Operand.Hidden
                        )
                    ),
                    solvedPuzzle.board.tiles[1]
                )
            ),
            solvedPuzzle = solvedPuzzle,
            intendedPairs = listOf(
                TutorialIntendedPair(firstStripEntryId = 0, secondStripEntryId = 1)
            )
        )
    }

    private const val TARGET_TILE_INDEX = 0
    private const val REQUIRED_STRIP_ENTRY_ID = 0
    private const val REQUIRED_VALUE = 2
}
