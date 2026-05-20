package org.cescfe.numpairs.domain.puzzle

data class PuzzleSolution(val solvedPuzzle: Puzzle) {
    init {
        require(solvedPuzzle.isSolved) {
            "Puzzle solutions require a solved puzzle."
        }
    }

    val stripEntryIds: Set<Int>
        get() = solvedPuzzle.strip.entries.map { stripEntry -> stripEntry.id }.toSet()

    val tileAssignments: List<IndexedResolvedTileAssignment>
        get() = solvedPuzzle.resolvedTileAssignments()

    fun isSolutionFor(initialPuzzle: Puzzle): Boolean = hasSameStripEntryIdsAs(initialPuzzle) &&
        hasSameBoardResultsAs(initialPuzzle) &&
        preservesVisibleStripValuesFrom(initialPuzzle)

    private fun hasSameStripEntryIdsAs(initialPuzzle: Puzzle): Boolean =
        stripEntryIds == initialPuzzle.strip.entries.map { stripEntry -> stripEntry.id }.toSet()

    private fun hasSameBoardResultsAs(initialPuzzle: Puzzle): Boolean =
        solvedPuzzle.board.tiles.map(Tile::result) == initialPuzzle.board.tiles.map(Tile::result)

    private fun preservesVisibleStripValuesFrom(initialPuzzle: Puzzle): Boolean =
        initialPuzzle.strip.entries.all { stripEntry ->
            val initialVisibleValue = initialPuzzle.strip.visibleValueForEntry(stripEntry.id)

            initialVisibleValue == null || solvedPuzzle.strip.visibleValueForEntry(stripEntry.id) == initialVisibleValue
        }
}
