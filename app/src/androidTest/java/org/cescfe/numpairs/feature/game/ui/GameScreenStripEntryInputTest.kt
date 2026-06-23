package org.cescfe.numpairs.feature.game.ui

import androidx.test.ext.junit.runners.AndroidJUnit4
import org.cescfe.numpairs.R
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class GameScreenStripEntryInputTest : GameScreenTestHost() {
    @Test
    fun hiddenStripItemsRenderInlineInputAndCanBeConfirmedWithImeDone() {
        screen
            .tapStripItem(1)
            .assertNoDialogDisplayed()
            .assertStripEntryInputDisplayed()
            .assertStripEntryInputFocused()
            .assertStripEntryValidRange(minimum = 1, maximum = 6)
            .enterStripValue("2")
            .submitStripEntryInput()
            .assertStripItemDescription(
                1,
                R.string.strip_item_player_entered_content_description,
                "2"
            )
    }

    @Test
    fun tappingAPlayerEnteredStripItemStartsInlineInputWithItsPrefilledValue() {
        screen
            .tapStripItem(1)
            .enterStripValue("2")
            .submitStripEntryInput()
            .tapStripItem(1)
            .assertStripEntryInputValue("2")
            .assertStripEntryInputNotInvalid()
    }

    @Test
    fun invalidValuesKeepInlineInputActiveAndMarkItInvalid() {
        screen
            .tapStripItem(1)
            .enterStripValue("9")
            .assertStripEntryInputInvalid()
            .assertStripEntryInvalidRange(minimum = 1, maximum = 6)
            .submitStripEntryInput()
            .assertStripEntryInputValue("9")
            .assertStripEntryInputInvalid()
            .assertStripEntryInvalidRange(minimum = 1, maximum = 6)
            .assertStripItemDescription(
                1,
                R.string.strip_item_hidden_content_description
            )
    }

    @Test
    fun losingFocusWithAValidValueCommitsAndExitsInlineEditing() {
        screen
            .tapStripItem(1)
            .enterStripValue("2")
            .tapTileLeftOperand(0)
            .assertStripEntryInputHidden()
            .assertStripItemDescription(
                1,
                R.string.strip_item_player_entered_content_description,
                "2"
            )
    }

    @Test
    fun losingFocusWithAnInvalidValueKeepsInlineEditingActiveAndShowsFeedback() {
        screen
            .tapStripItem(1)
            .enterStripValue("9")
            .tapTileLeftOperand(0)
            .assertStripEntryInputDisplayed()
            .assertStripEntryInputInvalid()
            .assertStripEntryInvalidRange(minimum = 1, maximum = 6)
    }

    @Test
    fun losingFocusWithAnEmptyDraftCancelsInlineEditingWithoutChangingTheStripItem() {
        screen
            .tapStripItem(1)
            .tapTileLeftOperand(0)
            .assertStripEntryInputHidden()
            .assertStripItemDescription(
                1,
                R.string.strip_item_hidden_content_description
            )
    }

    @Test
    fun changingAnInvalidInlineInputClearsInvalidState() {
        screen
            .tapStripItem(1)
            .enterStripValue("9")
            .submitStripEntryInput()
            .assertStripEntryInputInvalid()
            .replaceStripValue("2")
            .assertStripEntryInputNotInvalid()
            .assertStripEntryValidRange(minimum = 1, maximum = 6)
    }

    @Test
    fun nonDigitInputIsFilteredOutByTheInlineField() {
        screen
            .tapStripItem(1)
            .enterStripValue("2a!")
            .assertStripEntryInputValue("2")
    }
}
