package org.cescfe.numpairs.feature.generated

import androidx.activity.ComponentActivity
import androidx.compose.foundation.layout.width
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.test.assertHasClickAction
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertTextEquals
import androidx.compose.ui.test.junit4.v2.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.unit.dp
import androidx.test.ext.junit.runners.AndroidJUnit4
import org.cescfe.numpairs.data.puzzle.seed.samplePuzzle
import org.cescfe.numpairs.domain.generated.GeneratedElapsedTime
import org.cescfe.numpairs.feature.game.presentation.GameUiState
import org.cescfe.numpairs.feature.game.ui.actions.HintAction
import org.cescfe.numpairs.feature.game.ui.screen.GameScreen
import org.cescfe.numpairs.feature.game.ui.screen.GameScreenTestTags
import org.cescfe.numpairs.ui.theme.NumPairsTheme
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class GeneratedChronometerTest {
    @get:Rule
    val composeTestRule = createAndroidComposeRule<ComponentActivity>()

    @Test
    fun expanded_chronometer_collapses_to_icon_and_restores_without_changing_elapsed_time() {
        composeTestRule.setContent {
            var isExpanded by remember { mutableStateOf(true) }
            NumPairsTheme {
                GeneratedChronometer(
                    elapsedTime = GeneratedElapsedTime(3_601_999),
                    isExpanded = isExpanded,
                    onExpandedChange = { expanded -> isExpanded = expanded }
                )
            }
        }

        composeTestRule
            .onNodeWithTag(GENERATED_CHRONOMETER_VALUE_TAG, useUnmergedTree = true)
            .assertIsDisplayed()
            .assertTextEquals("60:01")
        composeTestRule
            .onNodeWithContentDescription("Elapsed time: 60:01")
            .assertHasClickAction()
            .performClick()

        composeTestRule
            .onNodeWithTag(GENERATED_CHRONOMETER_VALUE_TAG, useUnmergedTree = true)
            .assertDoesNotExist()
        composeTestRule
            .onNodeWithContentDescription("Show elapsed time")
            .assertIsDisplayed()
            .assertHasClickAction()
            .performClick()

        composeTestRule
            .onNodeWithTag(GENERATED_CHRONOMETER_VALUE_TAG, useUnmergedTree = true)
            .assertIsDisplayed()
            .assertTextEquals("60:01")
    }

    @Test
    fun expanded_chronometer_remains_usable_beside_hint_rules_and_a_single_line_title() {
        val title = "Quick · Mittelschwer"
        composeTestRule.setContent {
            NumPairsTheme {
                GameScreen(
                    title = title,
                    uiState = GameUiState.from(samplePuzzle),
                    modifier = Modifier.width(360.dp),
                    isRulesHelperEnabled = true,
                    topBarActions = {
                        GeneratedChronometer(
                            elapsedTime = GeneratedElapsedTime(125_999),
                            isExpanded = true,
                            onExpandedChange = {}
                        )
                        HintAction(onClick = {})
                    }
                )
            }
        }

        composeTestRule.onNodeWithText(title).assertIsDisplayed()
        composeTestRule.onNodeWithTag(GENERATED_CHRONOMETER_TAG).assertIsDisplayed().assertHasClickAction()
        composeTestRule.onNodeWithTag(GameScreenTestTags.HINT_ACTION).assertIsDisplayed().assertHasClickAction()
        composeTestRule.onNodeWithTag(GameScreenTestTags.RULES_HELPER_ACTION).assertIsDisplayed().assertHasClickAction()
    }
}
