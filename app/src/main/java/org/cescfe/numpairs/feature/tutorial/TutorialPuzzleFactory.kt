package org.cescfe.numpairs.feature.tutorial

import org.cescfe.numpairs.domain.puzzle.assignment.ResolvedOperandAssignment
import org.cescfe.numpairs.domain.puzzle.assignment.StripEntryId
import org.cescfe.numpairs.domain.puzzle.construction.hiddenExpression
import org.cescfe.numpairs.domain.puzzle.construction.resolvedTile
import org.cescfe.numpairs.domain.puzzle.model.Board
import org.cescfe.numpairs.domain.puzzle.model.Operator
import org.cescfe.numpairs.domain.puzzle.model.Puzzle
import org.cescfe.numpairs.domain.puzzle.model.Strip
import org.cescfe.numpairs.domain.puzzle.model.StripItem
import org.cescfe.numpairs.domain.puzzle.model.Tile

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
                stripEntryId = StripEntryId(tileDefinition.leftStripEntryId),
                value = stripValues[tileDefinition.leftStripEntryId]
            ),
            operator = tileDefinition.operator,
            rightOperand = ResolvedOperandAssignment(
                stripEntryId = StripEntryId(tileDefinition.rightStripEntryId),
                value = stripValues[tileDefinition.rightStripEntryId]
            )
        )
    }
)
