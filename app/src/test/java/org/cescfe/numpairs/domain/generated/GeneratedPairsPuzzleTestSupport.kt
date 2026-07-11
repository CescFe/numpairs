package org.cescfe.numpairs.domain.generated

import org.cescfe.numpairs.domain.puzzle.model.Expression
import org.cescfe.numpairs.domain.puzzle.model.Operator
import org.cescfe.numpairs.domain.puzzle.model.Puzzle
import org.cescfe.numpairs.domain.puzzle.model.PuzzleCompletionState
import org.cescfe.numpairs.domain.puzzle.model.StripItem
import org.cescfe.numpairs.domain.puzzle.model.Tile
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue

internal fun assertGeneratedInitialPuzzleStructure(puzzle: Puzzle, profile: GeneratedPuzzleProfile) {
    val knownEntryIds = puzzle.knownEntryIds()

    assertEquals(PuzzleCompletionState.INCOMPLETE, puzzle.completionState)
    assertEquals(profile.size.boardTileCount, puzzle.board.tiles.size)
    assertEquals(profile.size.stripEntryCount, puzzle.strip.entries.size)
    assertTrue(puzzle.board.tiles.all(Tile::hasHiddenExpression))
    assertTrue(knownEntryIds.size in profile.initialStripMaskPolicy.knownEntryCountRange)
    assertTrue(
        puzzle.strip.entries.count { entry -> entry.item == StripItem.Hidden } in
            profile.hiddenEntryCountRange
    )
    assertTrue(
        knownEntryIds.maxConsecutiveHiddenEntries(
            totalEntryCount = profile.size.stripEntryCount
        ) <= profile.initialStripMaskPolicy.maxConsecutiveHiddenEntries
    )
}

internal fun Puzzle.requireKnownStripValues(): List<Int> = strip.entries.map { entry ->
    (entry.item as StripItem.Known).value
}

internal fun Puzzle.additionTiles(): List<Tile> = tilesFor(operator = Operator.ADDITION)

internal fun Puzzle.multiplicationTiles(): List<Tile> = tilesFor(operator = Operator.MULTIPLICATION)

internal fun Puzzle.tilesFor(operator: Operator): List<Tile> = board.tiles.filter { tile ->
    tile.expression.operator == operator
}

internal fun Tile.hasHiddenExpression(): Boolean = expression.leftOperand == Expression.Operand.Hidden &&
    expression.operator == Operator.Hidden &&
    expression.rightOperand == Expression.Operand.Hidden

internal fun Tile.isPrimeProductDecoy(): Boolean {
    val leftOperand = expression.leftOperand as? Expression.Operand.Known ?: return false
    val rightOperand = expression.rightOperand as? Expression.Operand.Known ?: return false

    return expression.operator == Operator.MULTIPLICATION &&
        (
            (leftOperand.value == 1 && rightOperand.value.isPrime()) ||
                (rightOperand.value == 1 && leftOperand.value.isPrime())
            )
}

internal fun Puzzle.knownEntryIds(): Set<Int> = strip.entries
    .filter { entry -> entry.item is StripItem.Known }
    .map { entry -> entry.id }
    .toSet()

internal val GeneratedPuzzleProfile.requiredHighestStripEntryId: Int
    get() = size.stripEntryCount - 1

internal fun Set<Int>.maxConsecutiveHiddenEntries(totalEntryCount: Int): Int {
    var currentHiddenCount = 0
    var maxHiddenCount = 0

    repeat(totalEntryCount) { entryId ->
        if (entryId in this) {
            currentHiddenCount = 0
        } else {
            currentHiddenCount++
            maxHiddenCount = maxOf(maxHiddenCount, currentHiddenCount)
        }
    }

    return maxHiddenCount
}

private fun Int.isPrime(): Boolean {
    if (this < 2) {
        return false
    }

    var candidateDivisor = 2
    while (candidateDivisor * candidateDivisor <= this) {
        if (this % candidateDivisor == 0) {
            return false
        }
        candidateDivisor++
    }

    return true
}
