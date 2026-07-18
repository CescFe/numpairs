package org.cescfe.numpairs.ui.navigation

import androidx.compose.ui.test.SemanticsNodeInteractionsProvider
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performClick
import org.cescfe.numpairs.feature.generated.selector.ui.GeneratedDifficultySelectorTestTags

internal fun SemanticsNodeInteractionsProvider.navigateToSelectedGeneratedChallenge(menuButtonTag: String) {
    onNodeWithTag(menuButtonTag)
        .assertIsDisplayed()
        .performClick()
    onNodeWithTag(GeneratedDifficultySelectorTestTags.PLAY_BUTTON)
        .assertIsDisplayed()
        .performClick()
}
