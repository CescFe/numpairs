package org.cescfe.numpairs.feature.game

import androidx.test.ext.junit.runners.AndroidJUnit4
import org.cescfe.numpairs.R
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class GameScreenStripEntryDialogTest : GameScreenTestHost() {
    @Test
    fun tappingHiddenStripItemOpensEntryDialog() {
        screen
            .tapStripItem(1)
            .assertStripEntryDialogDisplayed()
            .assertStripEntryValidRange(minimum = 1, maximum = 6)
    }

    @Test
    fun confirmingEntryDialogCompletesTheHiddenStripItem() {
        screen
            .tapStripItem(1)
            .enterStripValue("2")
            .confirmStripEntry()
            .assertStripItemDescription(
                1,
                R.string.strip_item_player_entered_content_description,
                "2"
            )
    }

    @Test
    fun tappingPlayerEnteredStripItemReopensEntryDialogWithPrefilledValue() {
        screen
            .tapStripItem(1)
            .enterStripValue("2")
            .confirmStripEntry()
            .tapStripItem(1)
            .assertStripEntryInputValue("2")
    }

    @Test
    fun confirmingEntryDialogIsDisabledForOutOfRangeValues() {
        screen
            .tapStripItem(1)
            .enterStripValue("9")
            .assertStripEntryConfirmDisabled()
    }

    @Test
    fun cancellingEntryDialogLeavesTheHiddenStripItemUnchanged() {
        screen
            .tapStripItem(1)
            .enterStripValue("9")
            .cancelStripEntry()
            .assertStripItemDescription(
                1,
                R.string.strip_item_hidden_content_description
            )
    }
}
