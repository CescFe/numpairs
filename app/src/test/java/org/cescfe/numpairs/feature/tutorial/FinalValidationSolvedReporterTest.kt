package org.cescfe.numpairs.feature.tutorial

import org.cescfe.numpairs.domain.puzzle.model.PuzzleCompletionState
import org.cescfe.numpairs.feature.game.presentation.PuzzleOutcomeUiState
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class FinalValidationSolvedReporterTest {
    @Test
    fun `incomplete and invalid states are not reported`() {
        val reporter = FinalValidationSolvedReporter()

        assertFalse(reporter.shouldReport(puzzleOutcome = null))
        assertFalse(
            reporter.shouldReport(
                puzzleOutcome = PuzzleOutcomeUiState.Invalid(PuzzleCompletionState.INCORRECT_TILES)
            )
        )
    }

    @Test
    fun `solved state is reported exactly once`() {
        val reporter = FinalValidationSolvedReporter()

        assertTrue(reporter.shouldReport(PuzzleOutcomeUiState.Solved))
        assertFalse(reporter.shouldReport(PuzzleOutcomeUiState.Solved))
    }

    @Test
    fun `a new validation session can report solved again`() {
        val firstSession = FinalValidationSolvedReporter()
        val secondSession = FinalValidationSolvedReporter()

        assertTrue(firstSession.shouldReport(PuzzleOutcomeUiState.Solved))
        assertTrue(secondSession.shouldReport(PuzzleOutcomeUiState.Solved))
    }
}
