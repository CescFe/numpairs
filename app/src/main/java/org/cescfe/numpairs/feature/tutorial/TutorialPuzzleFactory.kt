package org.cescfe.numpairs.feature.tutorial

import org.cescfe.numpairs.domain.puzzle.Board
import org.cescfe.numpairs.domain.puzzle.Operator
import org.cescfe.numpairs.domain.puzzle.Puzzle
import org.cescfe.numpairs.domain.puzzle.ResolvedOperandAssignment
import org.cescfe.numpairs.domain.puzzle.Strip
import org.cescfe.numpairs.domain.puzzle.StripItem
import org.cescfe.numpairs.domain.puzzle.Tile
import org.cescfe.numpairs.domain.puzzle.hiddenExpression
import org.cescfe.numpairs.domain.puzzle.resolvedTile

internal data class TileDefinition(val leftStripEntryId: Int, val operator: Operator, val rightStripEntryId: Int)

internal fun puzzle(stripItems: List<StripItem>, tiles: List<Tile>): Puzzle = Puzzle(
    board = Board(tiles = tiles),
    strip = Strip.fromItems(items = stripItems)
)

internal fun hiddenTiles(results: List<Int>): List<Tile> = results.map { result ->
    hiddenTile(result = result)
}

internal fun hiddenTile(result: Int): Tile = Tile(
    expression = hiddenExpression(),
    result = result
)

internal fun solvedPuzzle(stripValues: List<Int>, tileDefinitions: List<TileDefinition>): Puzzle = puzzle(
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
