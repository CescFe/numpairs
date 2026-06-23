package org.cescfe.numpairs.feature.game.ui.screen

import androidx.test.ext.junit.runners.AndroidJUnit4
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class GameScreenSuccessOverlayTest : GameScreenTestHost() {
    @Test
    fun solvedPuzzleShowsTheSuccessOverlayOverTheBoardAndBackDismissesIt() {
        showSolvedOverlayFixture()

        screen
            .assertBoardDisplayed()
            .assertSuccessOverlayVisible()
            .assertSuccessOverlayMessageDisplayed()
            .pressBack()
            .assertSuccessOverlayHidden()
    }

    @Test
    fun tappingTheSuccessOverlayDismissesIt() {
        showSolvedOverlayFixture()

        screen
            .tapSuccessOverlay()
            .assertSuccessOverlayHidden()
    }

    @Test
    fun solvedPuzzleDoesNotShowSuccessOverlayWhenItIsDisabled() {
        disableSuccessOverlay()
        showSolvedOverlayFixture()

        screen
            .assertBoardDisplayed()
            .assertSuccessOverlayHidden()
    }

    @Test
    fun successOverlayBlocksPuzzleInteractionsUntilItIsDismissed() {
        showInteractiveSuccessOverlayFixture()

        screen
            .scrollToBoard()
            .assertSuccessOverlayVisible()
            .tapTileOperator(0)
            .assertOperatorSelectorHidden()
            .assertSuccessOverlayHidden()
            .tapTileOperator(0)
            .assertOperatorSelectorDisplayed()
    }
}
