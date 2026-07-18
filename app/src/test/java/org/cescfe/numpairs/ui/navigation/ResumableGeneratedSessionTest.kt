package org.cescfe.numpairs.ui.navigation

import org.cescfe.numpairs.data.generated.session.GeneratedSessionId
import org.cescfe.numpairs.data.generated.session.GeneratedSessionSnapshot
import org.cescfe.numpairs.data.puzzle.seed.samplePuzzle
import org.cescfe.numpairs.domain.puzzle.model.Expression
import org.cescfe.numpairs.domain.puzzle.model.Operator
import org.cescfe.numpairs.domain.puzzle.model.Puzzle
import org.cescfe.numpairs.feature.game.presentation.support.solvedPuzzleWithKnownStripAndAssignments
import org.cescfe.numpairs.feature.generated.GeneratedModes
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class ResumableGeneratedSessionTest {
    @Test
    fun `valid unfinished snapshot resolves its exact configured challenge`() {
        val snapshot = snapshot()

        assertEquals(
            ResumableGeneratedSession(
                sessionId = snapshot.sessionId,
                challenge = GeneratedModes.FOUR_PAIRS_LOW
            ),
            snapshot.toResumableGeneratedSessionOrNull(GeneratedModes.catalog)
        )
    }

    @Test
    fun `missing unknown mismatched and solved snapshots are not resumable`() {
        val solvedPuzzle = solvedPuzzleWithKnownStripAndAssignments()
        val unavailableSnapshots = listOf(
            null,
            snapshot(modeId = "unknown-mode"),
            snapshot(profileId = GeneratedModes.EIGHT_PAIRS_MEDIUM.profile.id.value),
            snapshot(
                initialPuzzle = initialPuzzleFor(solvedPuzzle),
                currentPuzzle = solvedPuzzle
            )
        )

        unavailableSnapshots.forEach { unavailableSnapshot ->
            assertNull(
                unavailableSnapshot.toResumableGeneratedSessionOrNull(
                    challengeCatalog = GeneratedModes.catalog
                )
            )
        }
    }
}

private fun snapshot(
    modeId: String = GeneratedModes.FOUR_PAIRS.id.value,
    profileId: String = GeneratedModes.FOUR_PAIRS_LOW.profile.id.value,
    initialPuzzle: Puzzle = samplePuzzle,
    currentPuzzle: Puzzle = samplePuzzle
): GeneratedSessionSnapshot = GeneratedSessionSnapshot(
    sessionId = GeneratedSessionId("session-213"),
    modeId = modeId,
    profileId = profileId,
    seed = 213,
    initialPuzzle = initialPuzzle,
    currentPuzzle = currentPuzzle
)

private fun initialPuzzleFor(solvedPuzzle: Puzzle): Puzzle = solvedPuzzle.copy(
    board = solvedPuzzle.board.copy(
        tiles = solvedPuzzle.board.tiles.map { tile ->
            tile.copy(
                expression = tile.expression.copy(
                    leftOperand = Expression.Operand.Hidden,
                    operator = Operator.Hidden,
                    rightOperand = Expression.Operand.Hidden
                )
            )
        }
    )
)
