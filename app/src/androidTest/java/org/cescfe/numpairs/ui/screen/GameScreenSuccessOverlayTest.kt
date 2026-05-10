package org.cescfe.numpairs.ui.screen

import androidx.test.ext.junit.runners.AndroidJUnit4
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class GameScreenSuccessOverlayTest : GameScreenTestHost() {
    @Test
    fun solvedPuzzleShowsSuccessOverlayWhileKeepingTheBoardVisible() {
        showSolvedOverlayFixture()

        screen
            .assertBoardDisplayed()
            .assertSuccessOverlayVisible()
            .assertSuccessOverlayMessageDisplayed()
    }

    @Test
    fun successOverlayCanBeDismissedWithBack() {
        showSolvedOverlayFixture()

        screen
            .assertSuccessOverlayVisible()
            .pressBack()
            .assertSuccessOverlayHidden()
    }

    @Test
    fun successOverlayCanBeDismissedByTap() {
        showSolvedOverlayFixture()

        screen
            .tapSuccessOverlay()
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
