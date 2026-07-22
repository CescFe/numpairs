package org.cescfe.numpairs.feature.tutorial

import org.cescfe.numpairs.R
import org.cescfe.numpairs.domain.puzzle.model.Operator
import org.cescfe.numpairs.domain.puzzle.model.Puzzle
import org.cescfe.numpairs.domain.puzzle.model.StripItem

internal object LearnBasicsTutorialContent {
    private val stripAndTilesIntroductionScenario = stripAndTilesIntroductionScenario()
    private val repeatedValuePracticeScenario = repeatedValuePracticeScenario()
    private val workedExampleFrames = workedExampleFrames()

    val scenarios: List<TutorialScenario> = listOf(
        stripAndTilesIntroductionScenario,
        repeatedValuePracticeScenario
    )

    val steps: List<TutorialStep> = explanationSteps() + listOf(
        workedExampleStep(),
        TutorialStep(
            scenarioId = TutorialScenarioId.REPEATED_VALUE_PRACTICE,
            playerFacingCopyResId = R.string.tutorial_repeated_value_practice_copy,
            highlightedTargets = listOf(
                TutorialHighlightTarget.StripEntries(indexes = listOf(2, 3)),
                TutorialHighlightTarget.WholeTile(tileIndex = 3)
            ),
            requiredAction = TutorialRequiredAction.CompleteScenario,
            completionPredicate = TutorialStepCompletionPredicate.ScenarioSolved,
            dismissHighlightsAfterFirstPuzzleChange = true
        )
    )

    private fun workedExampleStep(): TutorialStep {
        val initialFrame = workedExampleFrames.first()

        return TutorialStep(
            scenarioId = TutorialScenarioId.STRIP_AND_TILES_INTRODUCTION,
            playerFacingCopyResId = initialFrame.playerFacingCopyResId,
            highlightedTargets = initialFrame.highlightedTargets,
            requiredAction = TutorialRequiredAction.PlayWorkedExample(frames = workedExampleFrames),
            completionPredicate = TutorialStepCompletionPredicate.ManualAdvance,
            progressCheckpoint = TutorialProgressCheckpoint.WORKED_EXAMPLE_COMPLETED,
            entryPuzzle = initialFrame.puzzle
        )
    }

    private fun workedExampleFrames(): List<TutorialWorkedExampleFrame> {
        val scenario = stripAndTilesIntroductionScenario
        val initialPuzzle = scenario.initialPuzzle
        val productFourAndFive = initialPuzzle.withSolvedTiles(scenario = scenario, tileIndexes = intArrayOf(3))
        val pairFourAndFive = productFourAndFive.withSolvedTiles(scenario = scenario, tileIndexes = intArrayOf(2))
        val completedStrip = pairFourAndFive.copy(
            strip = pairFourAndFive.strip.withUpdatedEntry(index = 1, value = 3)
        )
        val productTwoAndThree = completedStrip.withSolvedTiles(scenario = scenario, tileIndexes = intArrayOf(1))
        val solvedExample = productTwoAndThree.withSolvedTiles(scenario = scenario, tileIndexes = intArrayOf(0))

        return listOf(
            TutorialWorkedExampleFrame(
                playerFacingCopyResId = R.string.tutorial_worked_example_introduction_copy,
                puzzle = initialPuzzle,
                highlightedTargets = listOf(
                    TutorialHighlightTarget.StripEntries(indexes = listOf(0, 1, 2, 3)),
                    TutorialHighlightTarget.WholeTile(tileIndex = 0),
                    TutorialHighlightTarget.WholeTile(tileIndex = 1),
                    TutorialHighlightTarget.WholeTile(tileIndex = 2),
                    TutorialHighlightTarget.WholeTile(tileIndex = 3)
                )
            ),
            workedExampleFrame(
                copyResId = R.string.tutorial_worked_example_product_four_five_copy,
                puzzle = productFourAndFive,
                stripEntryIndexes = listOf(2, 3),
                tileIndexes = listOf(3)
            ),
            workedExampleFrame(
                copyResId = R.string.tutorial_worked_example_sum_four_five_copy,
                puzzle = pairFourAndFive,
                stripEntryIndexes = listOf(2, 3),
                tileIndexes = listOf(2)
            ),
            workedExampleFrame(
                copyResId = R.string.tutorial_worked_example_reveal_three_copy,
                puzzle = completedStrip,
                stripEntryIndexes = listOf(0, 1),
                tileIndexes = listOf(0, 1)
            ),
            workedExampleFrame(
                copyResId = R.string.tutorial_worked_example_product_two_three_copy,
                puzzle = productTwoAndThree,
                stripEntryIndexes = listOf(0, 1),
                tileIndexes = listOf(1)
            ),
            workedExampleFrame(
                copyResId = R.string.tutorial_worked_example_sum_two_three_copy,
                puzzle = solvedExample,
                stripEntryIndexes = listOf(0, 1),
                tileIndexes = listOf(0)
            )
        )
    }

    private fun workedExampleFrame(
        copyResId: Int,
        puzzle: Puzzle,
        stripEntryIndexes: List<Int>,
        tileIndexes: List<Int>
    ): TutorialWorkedExampleFrame = TutorialWorkedExampleFrame(
        playerFacingCopyResId = copyResId,
        puzzle = puzzle,
        highlightedTargets = listOf(
            TutorialHighlightTarget.StripEntries(indexes = stripEntryIndexes)
        ) + tileIndexes.map(TutorialHighlightTarget::WholeTile)
    )

    private fun Puzzle.withSolvedTiles(scenario: TutorialScenario, tileIndexes: IntArray): Puzzle = copy(
        board = board.copy(
            tiles = board.tiles.toMutableList().apply {
                tileIndexes.forEach { tileIndex ->
                    set(tileIndex, scenario.solvedPuzzle.board.tiles[tileIndex])
                }
            }
        )
    )

    private fun explanationSteps(): List<TutorialStep> = listOf(
        manualExplanationStep(
            copyResId = R.string.tutorial_objective_explanation_copy,
            highlightedTargets = listOf(
                TutorialHighlightTarget.StripEntries(indexes = listOf(0, 1, 2, 3)),
                TutorialHighlightTarget.WholeTile(tileIndex = 0),
                TutorialHighlightTarget.WholeTile(tileIndex = 1),
                TutorialHighlightTarget.WholeTile(tileIndex = 2),
                TutorialHighlightTarget.WholeTile(tileIndex = 3)
            )
        ),
        manualExplanationStep(
            copyResId = R.string.tutorial_strip_explanation_copy,
            highlightedTargets = listOf(
                TutorialHighlightTarget.StripEntries(indexes = listOf(0, 1, 2, 3))
            )
        ),
        manualExplanationStep(
            copyResId = R.string.tutorial_tile_explanation_copy,
            highlightedTargets = listOf(
                TutorialHighlightTarget.WholeTile(tileIndex = 0)
            )
        ),
        manualExplanationStep(
            copyResId = R.string.tutorial_pair_explanation_copy,
            highlightedTargets = listOf(
                TutorialHighlightTarget.StripEntries(indexes = listOf(2, 3)),
                TutorialHighlightTarget.WholeTile(tileIndex = 2),
                TutorialHighlightTarget.WholeTile(tileIndex = 3)
            )
        )
    )

    private fun manualExplanationStep(copyResId: Int, highlightedTargets: List<TutorialHighlightTarget>): TutorialStep =
        TutorialStep(
            scenarioId = TutorialScenarioId.STRIP_AND_TILES_INTRODUCTION,
            playerFacingCopyResId = copyResId,
            highlightedTargets = highlightedTargets,
            requiredAction = TutorialRequiredAction.NoInteraction,
            completionPredicate = TutorialStepCompletionPredicate.ManualAdvance
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
                tiles = hiddenTiles(results = listOf(5, 6, 9, 20))
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
