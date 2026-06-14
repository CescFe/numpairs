package org.cescfe.numpairs.feature.tutorial

import androidx.annotation.StringRes
import org.cescfe.numpairs.R
import org.cescfe.numpairs.domain.puzzle.Board
import org.cescfe.numpairs.domain.puzzle.Operator
import org.cescfe.numpairs.domain.puzzle.Puzzle
import org.cescfe.numpairs.domain.puzzle.PuzzleCompletionState
import org.cescfe.numpairs.domain.puzzle.ResolvedOperandAssignment
import org.cescfe.numpairs.domain.puzzle.Strip
import org.cescfe.numpairs.domain.puzzle.StripItem
import org.cescfe.numpairs.domain.puzzle.Tile
import org.cescfe.numpairs.domain.puzzle.hiddenExpression
import org.cescfe.numpairs.domain.puzzle.resolvedTile
import org.cescfe.numpairs.feature.game.presentation.GameUiState
import org.cescfe.numpairs.feature.game.presentation.PuzzleOutcomeUiState
import org.cescfe.numpairs.feature.game.presentation.StripItemVisualStyle

object TutorialMvpContent {
    val scenarios: List<TutorialScenario> = listOf(
        twoPairPracticeScenario(),
        finalEasyFourPairsScenario(),
        solvingTipsPracticeScenario()
    )

    val steps: List<TutorialStep> = listOf(
        TutorialStep(
            order = 1,
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
            order = 2,
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
            order = 3,
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
            order = 4,
            scenarioId = TutorialScenarioId.TWO_PAIR_PRACTICE,
            playerFacingCopyResId = R.string.tutorial_step_four_copy,
            highlightedTargets = listOf(
                TutorialHighlightTarget.TileExpressionSlots(tileIndex = 2),
                TutorialHighlightTarget.TileExpressionSlots(tileIndex = 3)
            ),
            requiredAction = TutorialRequiredAction.CompleteScenario,
            completionPredicate = TutorialStepCompletionPredicate.ScenarioSolved
        ),
        TutorialStep(
            order = 5,
            scenarioId = TutorialScenarioId.FINAL_EASY_FOUR_PAIRS,
            playerFacingCopyResId = R.string.tutorial_step_five_copy,
            highlightedTargets = listOf(
                TutorialHighlightTarget.HiddenStripEntries,
                TutorialHighlightTarget.HiddenTileExpressions
            ),
            requiredAction = TutorialRequiredAction.CompleteScenario,
            completionPredicate = TutorialStepCompletionPredicate.ScenarioSolved
        ),
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
                TutorialHighlightTarget.StripEntries(indexes = listOf(2, 3)),
                TutorialHighlightTarget.Tiles(indexes = listOf(2, 3)),
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

    val learnBasicsSteps: List<TutorialStep> = steps.take(4)
    val practiceFullPuzzleSteps: List<TutorialStep> = listOf(steps[4])
    val solvingTipsPracticeSteps: List<TutorialStep> = steps.drop(5)

    fun stepsFor(mode: TutorialMode): List<TutorialStep> = when (mode) {
        TutorialMode.LEARN_BASICS -> learnBasicsSteps
        TutorialMode.PRACTICE_FULL_PUZZLE -> practiceFullPuzzleSteps
        TutorialMode.SOLVING_TIPS_PRACTICE -> solvingTipsPracticeSteps
    }

    fun scenario(id: TutorialScenarioId): TutorialScenario = scenarios.first { scenario -> scenario.id == id }
}

enum class TutorialMode {
    LEARN_BASICS,
    PRACTICE_FULL_PUZZLE,
    SOLVING_TIPS_PRACTICE
}

data class TutorialScenario(
    val id: TutorialScenarioId,
    val stripValues: List<Int>,
    val initialPuzzle: Puzzle,
    val solvedPuzzle: Puzzle,
    val intendedPairs: List<TutorialIntendedPair>
) {
    init {
        require(initialPuzzle.strip.entries.size == stripValues.size) {
            "Initial tutorial strip size must match the authored strip values."
        }
        require(solvedPuzzle.strip.items == stripValues.map(StripItem::Known)) {
            "Solved tutorial strip must contain the authored strip values."
        }
        require(initialPuzzle.board.tiles.map(Tile::result) == solvedPuzzle.board.tiles.map(Tile::result)) {
            "Initial and solved tutorial boards must expose the same tile results."
        }
        require(initialPuzzle.completionState == PuzzleCompletionState.INCOMPLETE) {
            "Tutorial initial puzzle must leave work for the player."
        }
        require(solvedPuzzle.completionState == PuzzleCompletionState.SOLVED) {
            "Tutorial solved puzzle must satisfy the domain rules."
        }
    }
}

enum class TutorialScenarioId {
    TWO_PAIR_PRACTICE,
    FINAL_EASY_FOUR_PAIRS,
    SOLVING_TIPS_PRACTICE
}

data class TutorialIntendedPair(val firstStripEntryId: Int, val secondStripEntryId: Int) {
    init {
        require(firstStripEntryId >= 0) {
            "First tutorial pair strip entry id must be non-negative."
        }
        require(secondStripEntryId >= 0) {
            "Second tutorial pair strip entry id must be non-negative."
        }
        require(firstStripEntryId != secondStripEntryId) {
            "Tutorial pairs must use two different strip entries."
        }
    }
}

data class TutorialStep(
    val order: Int,
    val scenarioId: TutorialScenarioId,
    @param:StringRes val playerFacingCopyResId: Int,
    val highlightedTargets: List<TutorialHighlightTarget>,
    val requiredAction: TutorialRequiredAction,
    val completionPredicate: TutorialStepCompletionPredicate
) {
    init {
        require(order > 0) {
            "Tutorial step order must be positive."
        }
        require(playerFacingCopyResId != 0) {
            "Tutorial step copy string resource must be defined."
        }
    }

    fun isComplete(uiState: GameUiState): Boolean = completionPredicate.isSatisfiedBy(uiState)
}

sealed interface TutorialHighlightTarget {
    data object HiddenStripEntries : TutorialHighlightTarget

    data object HiddenTileExpressions : TutorialHighlightTarget

    data object StripArea : TutorialHighlightTarget

    data object GridArea : TutorialHighlightTarget

    data class StripEntries(val indexes: List<Int>) : TutorialHighlightTarget {
        init {
            require(indexes.isNotEmpty()) {
                "Tutorial strip entry highlights must include at least one index."
            }
            require(indexes.all { index -> index >= 0 }) {
                "Tutorial strip entry highlight indexes must be non-negative."
            }
        }
    }

    data class Tiles(val indexes: List<Int>) : TutorialHighlightTarget {
        init {
            require(indexes.isNotEmpty()) {
                "Tutorial tile highlights must include at least one index."
            }
            require(indexes.all { index -> index >= 0 }) {
                "Tutorial tile highlight indexes must be non-negative."
            }
        }
    }

    data class TileExpressionSlots(val tileIndex: Int) : TutorialHighlightTarget {
        init {
            require(tileIndex >= 0) {
                "Tutorial tile expression slot highlight index must be non-negative."
            }
        }
    }
}

sealed interface TutorialRequiredAction {
    data class EnterStripValue(val stripEntryIndex: Int, val value: Int) : TutorialRequiredAction {
        init {
            require(stripEntryIndex >= 0) {
                "Tutorial strip entry action index must be non-negative."
            }
            require(value > 0) {
                "Tutorial strip entry action value must be positive."
            }
        }
    }

    data class CompleteTileExpression(
        val tileIndex: Int,
        val leftStripEntryId: Int,
        val operator: Operator,
        val rightStripEntryId: Int
    ) : TutorialRequiredAction {
        init {
            require(tileIndex >= 0) {
                "Tutorial tile action index must be non-negative."
            }
            require(leftStripEntryId >= 0) {
                "Tutorial left operand strip entry id must be non-negative."
            }
            require(rightStripEntryId >= 0) {
                "Tutorial right operand strip entry id must be non-negative."
            }
            require(operator != Operator.Hidden) {
                "Tutorial tile expression action requires a concrete operator."
            }
        }
    }

    data class CompleteTileExpressions(val expressions: List<CompleteTileExpression>) : TutorialRequiredAction {
        init {
            require(expressions.isNotEmpty()) {
                "Tutorial tile expression set action must include at least one expression."
            }
            require(expressions.map(CompleteTileExpression::tileIndex).toSet().size == expressions.size) {
                "Tutorial tile expression set action must target distinct tiles."
            }
        }
    }

    data class CompleteTileExpressionsInOrder(val expressions: List<CompleteTileExpression>) : TutorialRequiredAction {
        init {
            require(expressions.isNotEmpty()) {
                "Tutorial ordered tile expression action must include at least one expression."
            }
            require(expressions.map(CompleteTileExpression::tileIndex).toSet().size == expressions.size) {
                "Tutorial ordered tile expression action must target distinct tiles."
            }
        }
    }

    data object CompleteScenario : TutorialRequiredAction
}

sealed interface TutorialStepCompletionPredicate {
    fun isSatisfiedBy(uiState: GameUiState): Boolean

    data class StripValueEntered(val stripEntryIndex: Int, val value: Int) : TutorialStepCompletionPredicate {
        init {
            require(stripEntryIndex >= 0) {
                "Tutorial strip completion index must be non-negative."
            }
            require(value > 0) {
                "Tutorial strip completion value must be positive."
            }
        }

        override fun isSatisfiedBy(uiState: GameUiState): Boolean {
            val stripItem = uiState.stripItems.getOrNull(stripEntryIndex) ?: return false

            return stripItem.label == value.toString() &&
                stripItem.visualStyle == StripItemVisualStyle.PLAYER_ENTERED
        }
    }

    data class TileExpressionCompleted(
        val tileIndex: Int,
        val leftValue: Int,
        val operator: Operator,
        val rightValue: Int
    ) : TutorialStepCompletionPredicate {
        init {
            require(tileIndex >= 0) {
                "Tutorial tile completion index must be non-negative."
            }
            require(leftValue > 0) {
                "Tutorial left completion value must be positive."
            }
            require(rightValue > 0) {
                "Tutorial right completion value must be positive."
            }
            require(operator != Operator.Hidden) {
                "Tutorial tile completion requires a concrete operator."
            }
        }

        override fun isSatisfiedBy(uiState: GameUiState): Boolean {
            val tile = uiState.tiles.getOrNull(tileIndex) ?: return false

            return tile.leftOperandLabel == leftValue.toString() &&
                tile.operatorLabel == operator.symbol &&
                tile.rightOperandLabel == rightValue.toString()
        }
    }

    data class TileExpressionsCompleted(val expressions: List<TileExpressionCompleted>) :
        TutorialStepCompletionPredicate {
        init {
            require(expressions.isNotEmpty()) {
                "Tutorial tile expression completion set must include at least one expression."
            }
            require(expressions.map(TileExpressionCompleted::tileIndex).toSet().size == expressions.size) {
                "Tutorial tile expression completion set must target distinct tiles."
            }
        }

        override fun isSatisfiedBy(uiState: GameUiState): Boolean = expressions.all { expression ->
            expression.isSatisfiedBy(uiState)
        }
    }

    data object ScenarioSolved : TutorialStepCompletionPredicate {
        override fun isSatisfiedBy(uiState: GameUiState): Boolean = uiState.puzzleOutcome == PuzzleOutcomeUiState.Solved
    }
}

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

private fun finalEasyFourPairsScenario(): TutorialScenario {
    val stripValues = listOf(1, 2, 3, 4, 5, 6, 7, 8)

    return TutorialScenario(
        id = TutorialScenarioId.FINAL_EASY_FOUR_PAIRS,
        stripValues = stripValues,
        initialPuzzle = puzzle(
            stripItems = listOf(
                StripItem.Known(1),
                StripItem.Hidden,
                StripItem.Known(3),
                StripItem.Known(4),
                StripItem.Hidden,
                StripItem.Known(6),
                StripItem.Hidden,
                StripItem.Known(8)
            ),
            tiles = hiddenTiles(results = listOf(3, 2, 7, 12, 11, 30, 15, 56))
        ),
        solvedPuzzle = solvedPuzzle(
            stripValues = stripValues,
            tileDefinitions = listOf(
                TileDefinition(leftStripEntryId = 0, operator = Operator.ADDITION, rightStripEntryId = 1),
                TileDefinition(leftStripEntryId = 0, operator = Operator.MULTIPLICATION, rightStripEntryId = 1),
                TileDefinition(leftStripEntryId = 2, operator = Operator.ADDITION, rightStripEntryId = 3),
                TileDefinition(leftStripEntryId = 2, operator = Operator.MULTIPLICATION, rightStripEntryId = 3),
                TileDefinition(leftStripEntryId = 4, operator = Operator.ADDITION, rightStripEntryId = 5),
                TileDefinition(leftStripEntryId = 4, operator = Operator.MULTIPLICATION, rightStripEntryId = 5),
                TileDefinition(leftStripEntryId = 6, operator = Operator.ADDITION, rightStripEntryId = 7),
                TileDefinition(leftStripEntryId = 6, operator = Operator.MULTIPLICATION, rightStripEntryId = 7)
            )
        ),
        intendedPairs = listOf(
            TutorialIntendedPair(firstStripEntryId = 0, secondStripEntryId = 1),
            TutorialIntendedPair(firstStripEntryId = 2, secondStripEntryId = 3),
            TutorialIntendedPair(firstStripEntryId = 4, secondStripEntryId = 5),
            TutorialIntendedPair(firstStripEntryId = 6, secondStripEntryId = 7)
        )
    )
}

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
                StripItem.Hidden
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

private data class TileDefinition(val leftStripEntryId: Int, val operator: Operator, val rightStripEntryId: Int)

private fun puzzle(stripItems: List<StripItem>, tiles: List<Tile>): Puzzle = Puzzle(
    board = Board(tiles = tiles),
    strip = Strip.fromItems(items = stripItems)
)

private fun hiddenTiles(results: List<Int>): List<Tile> = results.map { result ->
    hiddenTile(result = result)
}

private fun hiddenTile(result: Int): Tile = Tile(
    expression = hiddenExpression(),
    result = result
)

private fun solvedPuzzle(stripValues: List<Int>, tileDefinitions: List<TileDefinition>): Puzzle = puzzle(
    stripItems = stripValues.map(StripItem::Known),
    tiles = tileDefinitions.map { tileDefinition ->
        resolvedTile(
            leftOperand = ResolvedOperandAssignment(
                stripEntryId = tileDefinition.leftStripEntryId,
                value = stripValues[tileDefinition.leftStripEntryId]
            ),
            operator = tileDefinition.operator,
            rightOperand = ResolvedOperandAssignment(
                stripEntryId = tileDefinition.rightStripEntryId,
                value = stripValues[tileDefinition.rightStripEntryId]
            )
        )
    }
)
