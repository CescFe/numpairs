package org.cescfe.numpairs.feature.tutorial

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

object TutorialMvpContent {
    val scenarios: List<TutorialScenario> = listOf(
        onePairOrientationScenario(),
        twoPairPracticeScenario(),
        finalEasyFourPairsScenario()
    )

    val steps: List<TutorialStep> = listOf(
        TutorialStep(
            order = 1,
            scenarioId = TutorialScenarioId.ONE_PAIR_ORIENTATION,
            playerFacingCopy = "Guess all the unknown elements.",
            highlightedTargets = listOf(
                TutorialHighlightTarget.HiddenStripEntries,
                TutorialHighlightTarget.HiddenTileExpressions
            ),
            requiredAction = null
        ),
        TutorialStep(
            order = 2,
            scenarioId = TutorialScenarioId.ONE_PAIR_ORIENTATION,
            playerFacingCopy = "Strip: numbers available to solve the puzzle. " +
                "Grid: tiles with a visible result and an unknown expression.",
            highlightedTargets = listOf(
                TutorialHighlightTarget.StripArea,
                TutorialHighlightTarget.GridArea
            ),
            requiredAction = null
        ),
        TutorialStep(
            order = 3,
            scenarioId = TutorialScenarioId.TWO_PAIR_PRACTICE,
            playerFacingCopy = "Guess hidden values to complete an ascending list of positive integers.",
            highlightedTargets = listOf(
                TutorialHighlightTarget.StripEntries(indexes = listOf(0, 1, 2))
            ),
            requiredAction = TutorialRequiredAction.EnterStripValue(
                stripEntryIndex = 1,
                value = 2
            )
        ),
        TutorialStep(
            order = 4,
            scenarioId = TutorialScenarioId.TWO_PAIR_PRACTICE,
            playerFacingCopy = "Fill each tile with two operands and one operator.",
            highlightedTargets = listOf(
                TutorialHighlightTarget.Tiles(indexes = listOf(0)),
                TutorialHighlightTarget.TileExpressionSlots(tileIndex = 0),
                TutorialHighlightTarget.StripEntries(indexes = listOf(0, 1))
            ),
            requiredAction = TutorialRequiredAction.CompleteTileExpression(
                tileIndex = 0,
                leftStripEntryId = 0,
                operator = Operator.ADDITION,
                rightStripEntryId = 1
            )
        ),
        TutorialStep(
            order = 5,
            scenarioId = TutorialScenarioId.TWO_PAIR_PRACTICE,
            playerFacingCopy = "Pair strip numbers so each pair creates one sum and one product " +
                "that match two grid results.",
            highlightedTargets = listOf(
                TutorialHighlightTarget.StripEntries(indexes = listOf(0, 1)),
                TutorialHighlightTarget.Tiles(indexes = listOf(0, 1))
            ),
            requiredAction = TutorialRequiredAction.CompleteTileExpression(
                tileIndex = 1,
                leftStripEntryId = 0,
                operator = Operator.MULTIPLICATION,
                rightStripEntryId = 1
            )
        ),
        TutorialStep(
            order = 6,
            scenarioId = TutorialScenarioId.FINAL_EASY_FOUR_PAIRS,
            playerFacingCopy = "Now finish the remaining unknowns.",
            highlightedTargets = listOf(
                TutorialHighlightTarget.HiddenStripEntries,
                TutorialHighlightTarget.HiddenTileExpressions
            ),
            requiredAction = TutorialRequiredAction.CompleteScenario
        )
    )

    fun scenario(id: TutorialScenarioId): TutorialScenario = scenarios.first { scenario -> scenario.id == id }
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
    ONE_PAIR_ORIENTATION,
    TWO_PAIR_PRACTICE,
    FINAL_EASY_FOUR_PAIRS
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
    val playerFacingCopy: String,
    val highlightedTargets: List<TutorialHighlightTarget>,
    val requiredAction: TutorialRequiredAction?
) {
    init {
        require(order > 0) {
            "Tutorial step order must be positive."
        }
        require(playerFacingCopy.isNotBlank()) {
            "Tutorial step copy must not be blank."
        }
        require(highlightedTargets.isNotEmpty()) {
            "Tutorial step must define at least one highlighted target."
        }
    }
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

    data object CompleteScenario : TutorialRequiredAction
}

private fun onePairOrientationScenario(): TutorialScenario {
    val stripValues = listOf(2, 3)

    return TutorialScenario(
        id = TutorialScenarioId.ONE_PAIR_ORIENTATION,
        stripValues = stripValues,
        initialPuzzle = puzzle(
            stripItems = listOf(
                StripItem.Known(2),
                StripItem.Hidden
            ),
            tiles = hiddenTiles(results = listOf(5, 6))
        ),
        solvedPuzzle = solvedPuzzle(
            stripValues = stripValues,
            tileDefinitions = listOf(
                TileDefinition(leftStripEntryId = 0, operator = Operator.ADDITION, rightStripEntryId = 1),
                TileDefinition(leftStripEntryId = 0, operator = Operator.MULTIPLICATION, rightStripEntryId = 1)
            )
        ),
        intendedPairs = listOf(
            TutorialIntendedPair(firstStripEntryId = 0, secondStripEntryId = 1)
        )
    )
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

private data class TileDefinition(val leftStripEntryId: Int, val operator: Operator, val rightStripEntryId: Int)

private fun puzzle(stripItems: List<StripItem>, tiles: List<Tile>): Puzzle = Puzzle(
    board = Board(tiles = tiles),
    strip = Strip.fromItems(items = stripItems)
)

private fun hiddenTiles(results: List<Int>): List<Tile> = results.map { result ->
    Tile(
        expression = hiddenExpression(),
        result = result
    )
}

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
