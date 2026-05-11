package org.cescfe.numpairs.feature.game.ui

import androidx.test.ext.junit.runners.AndroidJUnit4
import org.cescfe.numpairs.R
import org.cescfe.numpairs.domain.puzzle.PuzzleCompletionState
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class GameScreenInvalidFeedbackTest : GameScreenTestHost() {
    @Test
    fun invalidOutcomeBannerShowsTheExpectedMessageForEachSupportedInvalidState() {
        assertInvalidOutcomeBanner(
            completionState = PuzzleCompletionState.INCORRECT_TILES,
            messageResId = R.string.puzzle_outcome_invalid_tiles_message
        )
        assertInvalidOutcomeBanner(
            completionState = PuzzleCompletionState.MISSING_STRIP_ENTRY_IDENTITIES,
            messageResId = R.string.puzzle_outcome_missing_identities_message
        )
        assertInvalidOutcomeBanner(
            completionState = PuzzleCompletionState.MISMATCHED_SUM_PRODUCT_PAIRINGS,
            messageResId = R.string.puzzle_outcome_mismatched_pairings_message
        )
        assertInvalidOutcomeBanner(
            completionState = PuzzleCompletionState.INVALID_STRIP_ENTRY_USAGE,
            messageResId = R.string.puzzle_outcome_invalid_usage_message
        )
    }

    @Test
    fun mismatchedPairingTilesExposeStateSemanticsAndRemainEditable() {
        showInteractiveMismatchedPairingFixture()

        screen
            .assertPuzzleOutcomeVisible()
            .assertPuzzleOutcomeMessageDisplayed(R.string.puzzle_outcome_mismatched_pairings_message)
            .assertSuccessOverlayHidden()
            .assertBoardDisplayed()
            .assertTileStateDescription(
                tileIndex = 0,
                stringResId = R.string.tile_state_mismatched_pairing
            )
            .assertTileStateDescription(
                tileIndex = 3,
                stringResId = R.string.tile_state_mismatched_pairing
            )
            .tapTileOperator(0)
            .assertOperatorSelectorDisplayed()
    }

    private fun assertInvalidOutcomeBanner(
        completionState: PuzzleCompletionState,
        messageResId: Int
    ) {
        showInvalidOutcomeFixture(completionState = completionState)

        screen
            .assertBoardDisplayed()
            .assertPuzzleOutcomeVisible()
            .assertPuzzleOutcomeTitleDisplayed()
            .assertPuzzleOutcomeMessageDisplayed(messageResId)
            .assertSuccessOverlayHidden()
    }
}
