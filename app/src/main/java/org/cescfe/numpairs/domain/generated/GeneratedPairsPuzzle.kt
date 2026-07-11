package org.cescfe.numpairs.domain.generated

import org.cescfe.numpairs.domain.generated.internal.GeneratedPairsPuzzleValidator
import org.cescfe.numpairs.domain.puzzle.assignment.StripEntryId
import org.cescfe.numpairs.domain.puzzle.construction.withHiddenExpression
import org.cescfe.numpairs.domain.puzzle.construction.withKnownEntriesOnly
import org.cescfe.numpairs.domain.puzzle.model.Board
import org.cescfe.numpairs.domain.puzzle.model.Puzzle
import org.cescfe.numpairs.domain.puzzle.model.Strip
import org.cescfe.numpairs.domain.puzzle.model.Tile

@ConsistentCopyVisibility
data class GeneratedPairsPuzzle private constructor(val initialPuzzle: Puzzle, val solvedPuzzle: Puzzle) {
    companion object {
        fun create(
            profile: GeneratedPuzzleProfile,
            initialPuzzle: Puzzle,
            solvedPuzzle: Puzzle
        ): GeneratedPairsPuzzleCreation {
            val initialSnapshot = initialPuzzle.snapshot()
            val solvedSnapshot = solvedPuzzle.snapshot()
            val report = GeneratedPairsPuzzleValidator(profile = profile).validate(
                initialPuzzle = initialSnapshot,
                solvedPuzzle = solvedSnapshot
            )

            return if (report.isValid) {
                GeneratedPairsPuzzleCreation.Created(
                    puzzle = GeneratedPairsPuzzle(
                        initialPuzzle = initialSnapshot,
                        solvedPuzzle = solvedSnapshot
                    )
                )
            } else {
                GeneratedPairsPuzzleCreation.Rejected(violations = report.violations)
            }
        }

        internal fun fromSolvedPuzzle(
            profile: GeneratedPuzzleProfile,
            solvedPuzzle: Puzzle,
            knownEntryIds: Set<StripEntryId>
        ): GeneratedPairsPuzzleCreation {
            val validator = GeneratedPairsPuzzleValidator(profile = profile)
            val solvedSnapshot = solvedPuzzle.snapshot()
            val solvedReport = validator.validateSolvedPuzzle(solvedPuzzle = solvedSnapshot)
            if (!solvedReport.isValid) {
                return GeneratedPairsPuzzleCreation.Rejected(violations = solvedReport.violations)
            }

            val initialPuzzle = solvedSnapshot.toInitialPuzzle(knownEntryIds = knownEntryIds)
            val report = validator.validate(
                initialPuzzle = initialPuzzle,
                solvedPuzzle = solvedSnapshot
            )

            return if (report.isValid) {
                GeneratedPairsPuzzleCreation.Created(
                    puzzle = GeneratedPairsPuzzle(
                        initialPuzzle = initialPuzzle,
                        solvedPuzzle = solvedSnapshot
                    )
                )
            } else {
                GeneratedPairsPuzzleCreation.Rejected(violations = report.violations)
            }
        }
    }
}

private fun Puzzle.toInitialPuzzle(knownEntryIds: Set<StripEntryId>): Puzzle = Puzzle(
    board = Board(tiles = board.tiles.map(Tile::withHiddenExpression)),
    strip = strip.withKnownEntriesOnly(
        knownEntryIds = knownEntryIds.mapTo(mutableSetOf(), StripEntryId::value)
    )
)

private fun Puzzle.snapshot(): Puzzle = Puzzle(
    board = Board(tiles = board.tiles.toList()),
    strip = Strip.fromEntries(entries = strip.entries.toList())
)
