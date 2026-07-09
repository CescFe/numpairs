package org.cescfe.numpairs.domain.generated.internal

import kotlin.random.Random
import org.cescfe.numpairs.domain.generated.GeneratedPuzzleProfile
import org.cescfe.numpairs.domain.puzzle.assignment.ResolvedOperandAssignment
import org.cescfe.numpairs.domain.puzzle.construction.resolvedTile
import org.cescfe.numpairs.domain.puzzle.construction.withHiddenExpression
import org.cescfe.numpairs.domain.puzzle.construction.withKnownEntriesOnly
import org.cescfe.numpairs.domain.puzzle.model.Board
import org.cescfe.numpairs.domain.puzzle.model.Operator
import org.cescfe.numpairs.domain.puzzle.model.Puzzle
import org.cescfe.numpairs.domain.puzzle.model.Strip
import org.cescfe.numpairs.domain.puzzle.model.StripItem
import org.cescfe.numpairs.domain.puzzle.model.Tile

internal class GeneratedPairsPuzzleAssembler(private val profile: GeneratedPuzzleProfile, private val random: Random) {
    fun buildSolvedPuzzle(candidate: GeneratedPairsSolvedCandidate): Puzzle {
        val solvedTiles = candidate.pairs.flatMap { pair -> pair.solvedTiles() }

        return Puzzle(
            board = Board(
                tiles = if (profile.generationPolicy.isBoardTileShufflingEnabled) {
                    solvedTiles.shuffled(random)
                } else {
                    solvedTiles
                }
            ),
            strip = Strip.fromItems(items = candidate.entries.map { entry -> StripItem.Known(entry.value) })
        )
    }

    fun buildInitialPuzzle(solvedPuzzle: Puzzle, knownEntryIds: Set<Int>): Puzzle = Puzzle(
        board = Board(
            tiles = solvedPuzzle.board.tiles.map(Tile::withHiddenExpression)
        ),
        strip = solvedPuzzle.strip.withKnownEntriesOnly(knownEntryIds = knownEntryIds)
    )
}

private fun GeneratedPairsEntryPair.solvedTiles(): List<Tile> = listOf(
    solvedTile(operator = Operator.ADDITION),
    solvedTile(operator = Operator.MULTIPLICATION)
)

private fun GeneratedPairsEntryPair.solvedTile(operator: Operator): Tile = resolvedTile(
    leftOperand = ResolvedOperandAssignment(
        stripEntryId = firstEntry.id,
        value = firstEntry.value
    ),
    operator = operator,
    rightOperand = ResolvedOperandAssignment(
        stripEntryId = secondEntry.id,
        value = secondEntry.value
    )
)
