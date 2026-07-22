package org.cescfe.numpairs.feature.tutorial

import androidx.annotation.StringRes
import org.cescfe.numpairs.domain.puzzle.model.Operator
import org.cescfe.numpairs.domain.puzzle.model.Puzzle
import org.cescfe.numpairs.domain.puzzle.model.PuzzleCompletionState
import org.cescfe.numpairs.domain.puzzle.model.StripItem
import org.cescfe.numpairs.domain.puzzle.model.Tile
import org.cescfe.numpairs.feature.game.presentation.GameUiState
import org.cescfe.numpairs.feature.game.presentation.PuzzleOutcomeUiState

enum class TutorialMode {
    LEARN_BASICS,
    SOLVING_TIPS_PRACTICE
}

enum class TutorialProgressCheckpoint {
    WORKED_EXAMPLE_COMPLETED
}

data class TutorialScenario(
    val id: TutorialScenarioId,
    val stripValues: List<Int>,
    val initialPuzzle: Puzzle,
    val solvedPuzzle: Puzzle
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
    STRIP_AND_TILES_INTRODUCTION,
    REPEATED_VALUE_PRACTICE,
    SOLVING_TIPS_PRACTICE
}

data class TutorialStep(
    val scenarioId: TutorialScenarioId,
    @param:StringRes val playerFacingCopyResId: Int,
    val highlightedTargets: List<TutorialHighlightTarget>,
    val requiredAction: TutorialRequiredAction,
    val completionPredicate: TutorialStepCompletionPredicate,
    val progressCheckpoint: TutorialProgressCheckpoint? = null,
    val isBoardVisible: Boolean = true,
    val dismissHighlightsAfterFirstPuzzleChange: Boolean = false,
    val entryPuzzle: Puzzle? = null
) {
    init {
        require(playerFacingCopyResId != 0) {
            "Tutorial step copy string resource must be defined."
        }
    }

    fun isComplete(uiState: GameUiState): Boolean = completionPredicate.isSatisfiedBy(uiState)
}

sealed interface TutorialHighlightTarget {
    data object HiddenStripEntries : TutorialHighlightTarget

    data object HiddenTileExpressions : TutorialHighlightTarget

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

    data class TileExpressionSlots(val tileIndex: Int) : TutorialHighlightTarget {
        init {
            require(tileIndex >= 0) {
                "Tutorial tile expression slot highlight index must be non-negative."
            }
        }
    }

    data class WholeTile(val tileIndex: Int) : TutorialHighlightTarget {
        init {
            require(tileIndex >= 0) {
                "Tutorial whole tile highlight index must be non-negative."
            }
        }
    }
}

sealed interface TutorialRequiredAction {
    data object NoInteraction : TutorialRequiredAction

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

    data object ManualAdvance : TutorialStepCompletionPredicate {
        override fun isSatisfiedBy(uiState: GameUiState): Boolean = false
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
            val expectedLeftValue = leftValue.toString()
            val expectedRightValue = rightValue.toString()
            val matchesAuthoredOrder =
                tile.leftOperandLabel == expectedLeftValue && tile.rightOperandLabel == expectedRightValue
            val matchesReversedOrder =
                tile.leftOperandLabel == expectedRightValue && tile.rightOperandLabel == expectedLeftValue

            return (matchesAuthoredOrder || matchesReversedOrder) && tile.operatorLabel == operator.symbol
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
