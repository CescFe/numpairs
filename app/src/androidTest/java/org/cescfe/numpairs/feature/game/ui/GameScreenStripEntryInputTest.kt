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
            .assertStripEntryInputDisplayed()
            .assertStripEntryInputFocused()
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
            .submitStripEntryInput()
            .assertStripEntryInputValue("9")
            .assertStripEntryInputInvalid()
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
    }

    @Test
    fun nonDigitInputIsFilteredOutByTheInlineField() {
        screen
            .tapStripItem(1)
            .enterStripValue("2a!")
            .assertStripEntryInputValue("2")
    }

    @Test
    fun nonActiveStripChipsKeepTheirCurrentAppearance() {
        screen
            .tapStripItem(1)
            .assertStripItemDescription(
                2,
                R.string.strip_item_known_content_description,
                "6"
            )
    }
}
