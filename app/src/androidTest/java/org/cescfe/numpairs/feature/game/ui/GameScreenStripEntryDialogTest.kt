package org.cescfe.numpairs.feature.game.ui

import androidx.test.ext.junit.runners.AndroidJUnit4
import org.cescfe.numpairs.R
import org.cescfe.numpairs.domain.puzzle.StripEntryRange
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class GameScreenStripEntryDialogTest : GameScreenTestHost() {
    @Test
    fun hiddenStripItemsOpenTheDialogWithTheExpectedRangeAndCanBeConfirmed() {
        screen
            .tapStripItem(1)
            .assertStripEntryDialogDisplayed()
            .assertStripEntryValidRange(minimum = 1, maximum = 6)
            .enterStripValue("2")
            .confirmStripEntry()
            .assertStripItemDescription(
                1,
                R.string.strip_item_player_entered_content_description,
                "2"
            )
    }

    @Test
    fun tappingAPlayerEnteredStripItemReopensTheDialogWithItsPrefilledValue() {
        screen
            .tapStripItem(1)
            .enterStripValue("2")
            .confirmStripEntry()
            .tapStripItem(1)
            .assertStripEntryInputValue("2")
    }

    @Test
    fun invalidValuesDisableConfirmationAndCancelLeavesTheStripUnchanged() {
        screen
            .tapStripItem(1)
            .enterStripValue("9")
            .assertStripEntryConfirmDisabled()
            .cancelStripEntry()
            .assertStripItemDescription(
                1,
                R.string.strip_item_hidden_content_description
            )
    }

    @Test
    fun stripEntryDialogDisplaysAnUnboundedRangeWhenNoUpperBoundExists() {
        showUiStateFixture(
            stripEntryDialogUiState(
                validRange = StripEntryRange(minimumValue = 3, maximumValue = null)
            )
        )

        screen
            .assertStripEntryDialogDisplayed()
            .assertStripEntryValidRange(minimum = 3)
    }

    @Test
    fun nonDigitInputIsFilteredOutByTheComposeTextField() {
        showUiStateFixture(
            stripEntryDialogUiState(
                validRange = StripEntryRange(minimumValue = 1, maximumValue = 6)
            )
        )

        screen
            .assertStripEntryDialogDisplayed()
            .enterStripValue("2a!")
            .assertStripEntryInputValue("2")
    }

    @Test
    fun invalidMixedInputRemainsDisabledAfterFilteringThroughTheDialogField() {
        showUiStateFixture(
            stripEntryDialogUiState(
                validRange = StripEntryRange(minimumValue = 1, maximumValue = 6)
            )
        )

        screen
            .assertStripEntryDialogDisplayed()
            .enterStripValue("9a!")
            .assertStripEntryInputValue("9")
            .assertStripEntryConfirmDisabled()
    }
}
