package org.cescfe.numpairs.feature.menu.ui

import androidx.activity.ComponentActivity
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.SemanticsProperties
import androidx.compose.ui.test.SemanticsMatcher
import androidx.compose.ui.test.assert
import androidx.compose.ui.test.assertContentDescriptionEquals
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertIsNotSelected
import androidx.compose.ui.test.assertIsSelected
import androidx.compose.ui.test.assertTextEquals
import androidx.compose.ui.test.click
import androidx.compose.ui.test.junit4.v2.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onRoot
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollTo
import androidx.compose.ui.test.performTouchInput
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.test.espresso.Espresso.pressBackUnconditionally
import androidx.test.ext.junit.runners.AndroidJUnit4
import org.cescfe.numpairs.R
import org.cescfe.numpairs.ui.theme.NumPairsTheme
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class MenuScreenTest {
    @get:Rule
    val composeTestRule = createAndroidComposeRule<ComponentActivity>()

    @Test
    fun generated_mode_rows_identify_the_selected_challenge_and_emit_distinct_actions() {
        var playCount = 0
        var selectedDifficulty: GeneratedDifficultyMenuOptionId? = null
        setContent(
            onFourPairsSelected = { playCount += 1 },
            onFourPairsDifficultySelected = { optionId ->
                selectedDifficulty = optionId
            }
        )

        composeTestRule
            .onNodeWithTag(MenuScreenTestTags.FOUR_PAIRS_BUTTON)
            .assertIsDisplayed()
            .assertTextEquals(fourPairsState.challengeName)
            .assertContentDescriptionEquals(
                string(
                    R.string.menu_play_generated_challenge_content_description,
                    fourPairsState.challengeName
                )
            )
            .performClick()
        composeTestRule
            .onNodeWithTag(MenuScreenTestTags.FOUR_PAIRS_DIFFICULTY_BUTTON)
            .performClick()
        composeTestRule
            .onNodeWithTag(MenuScreenTestTags.difficultyOption(fourPairsMediumId))
            .performClick()
        composeTestRule
            .onNodeWithTag(MenuScreenTestTags.FOUR_PAIRS_DIFFICULTY_MENU)
            .assertDoesNotExist()

        composeTestRule.runOnIdle {
            assertEquals(1, playCount)
            assertEquals(fourPairsMediumId, selectedDifficulty)
        }
    }

    @Test
    fun selector_exposes_only_supported_radio_options_and_a_non_color_selection_state() {
        setContent()

        composeTestRule
            .onNodeWithTag(MenuScreenTestTags.FOUR_PAIRS_DIFFICULTY_BUTTON)
            .performClick()
        composeTestRule
            .onNodeWithTag(MenuScreenTestTags.difficultyOption(fourPairsLowId))
            .assertIsSelected()
            .assert(
                SemanticsMatcher.expectValue(
                    SemanticsProperties.Role,
                    Role.RadioButton
                )
            ).assert(
                SemanticsMatcher.expectValue(
                    SemanticsProperties.StateDescription,
                    string(R.string.menu_generated_difficulty_selected)
                )
            )
        composeTestRule
            .onNodeWithTag(MenuScreenTestTags.difficultyOption(fourPairsMediumId))
            .assertIsNotSelected()
        composeTestRule
            .onNodeWithTag(MenuScreenTestTags.difficultyOption(eightPairsHardId))
            .assertDoesNotExist()
    }

    @Test
    fun selector_action_switches_between_menu_and_close_states() {
        setContent()
        val action = composeTestRule
            .onNodeWithTag(MenuScreenTestTags.FOUR_PAIRS_DIFFICULTY_BUTTON)
            .assertContentDescriptionEquals(
                string(
                    R.string.menu_choose_generated_difficulty_content_description,
                    fourPairsState.modeName
                )
            ).assert(
                SemanticsMatcher.expectValue(
                    SemanticsProperties.StateDescription,
                    string(R.string.menu_generated_difficulty_collapsed)
                )
            )

        action.performClick()

        action
            .assertContentDescriptionEquals(
                string(
                    R.string.menu_close_generated_difficulty_content_description,
                    fourPairsState.modeName
                )
            ).assert(
                SemanticsMatcher.expectValue(
                    SemanticsProperties.StateDescription,
                    string(R.string.menu_generated_difficulty_expanded)
                )
            ).performClick()
        composeTestRule
            .onNodeWithTag(MenuScreenTestTags.FOUR_PAIRS_DIFFICULTY_MENU)
            .assertDoesNotExist()
    }

    @Test
    fun selector_end_aligns_with_its_trigger_in_ltr() {
        assertSelectorEndAlignment(layoutDirection = LayoutDirection.Ltr)
    }

    @Test
    fun selector_end_aligns_with_its_trigger_in_rtl() {
        assertSelectorEndAlignment(layoutDirection = LayoutDirection.Rtl)
    }

    @Test
    fun compact_width_and_increased_text_scale_keep_selector_inside_the_screen() {
        setContent(width = 320.dp, height = 640.dp, fontScale = 2f)

        composeTestRule
            .onNodeWithTag(MenuScreenTestTags.EIGHT_PAIRS_DIFFICULTY_BUTTON)
            .performScrollTo()
            .performClick()

        val screenBounds = composeTestRule
            .onNodeWithTag(MenuScreenTestTags.SCREEN)
            .fetchSemanticsNode()
            .boundsInRoot
        val menuBounds = composeTestRule
            .onNodeWithTag(MenuScreenTestTags.EIGHT_PAIRS_DIFFICULTY_MENU)
            .assertIsDisplayed()
            .fetchSemanticsNode()
            .boundsInRoot

        assertTrue(menuBounds.left >= screenBounds.left)
        assertTrue(menuBounds.right <= screenBounds.right)
        composeTestRule
            .onNodeWithTag(MenuScreenTestTags.difficultyOption(eightPairsHardId))
            .assertIsDisplayed()
    }

    @Test
    fun outside_tap_and_system_back_dismiss_without_selecting() {
        var selectionCount = 0
        setContent(
            onFourPairsDifficultySelected = {
                selectionCount += 1
            }
        )

        composeTestRule
            .onNodeWithTag(MenuScreenTestTags.FOUR_PAIRS_DIFFICULTY_BUTTON)
            .performClick()
        composeTestRule
            .onRoot()
            .performTouchInput {
                click(Offset(1f, 1f))
            }
        composeTestRule
            .onNodeWithTag(MenuScreenTestTags.FOUR_PAIRS_DIFFICULTY_MENU)
            .assertDoesNotExist()

        composeTestRule
            .onNodeWithTag(MenuScreenTestTags.FOUR_PAIRS_DIFFICULTY_BUTTON)
            .performClick()
        pressBackUnconditionally()
        composeTestRule
            .onNodeWithTag(MenuScreenTestTags.FOUR_PAIRS_DIFFICULTY_MENU)
            .assertDoesNotExist()
        composeTestRule.runOnIdle {
            assertEquals(0, selectionCount)
        }
    }

    @Test
    fun opening_another_mode_replaces_the_current_selector() {
        setContent()

        composeTestRule
            .onNodeWithTag(MenuScreenTestTags.FOUR_PAIRS_DIFFICULTY_BUTTON)
            .performClick()
        composeTestRule
            .onNodeWithTag(MenuScreenTestTags.EIGHT_PAIRS_DIFFICULTY_BUTTON)
            .performClick()

        composeTestRule
            .onNodeWithTag(MenuScreenTestTags.FOUR_PAIRS_DIFFICULTY_MENU)
            .assertDoesNotExist()
        composeTestRule
            .onNodeWithTag(MenuScreenTestTags.EIGHT_PAIRS_DIFFICULTY_MENU)
            .assertIsDisplayed()
    }

    @Test
    fun difficulty_actions_are_square_and_match_the_primary_action_height() {
        setContent()

        listOf(
            MenuScreenTestTags.FOUR_PAIRS_BUTTON to MenuScreenTestTags.FOUR_PAIRS_DIFFICULTY_BUTTON,
            MenuScreenTestTags.EIGHT_PAIRS_BUTTON to MenuScreenTestTags.EIGHT_PAIRS_DIFFICULTY_BUTTON
        ).forEach { (playTag, difficultyTag) ->
            val playBounds = composeTestRule
                .onNodeWithTag(playTag)
                .assertIsDisplayed()
                .fetchSemanticsNode()
                .boundsInRoot
            val difficultyBounds = composeTestRule
                .onNodeWithTag(difficultyTag)
                .assertIsDisplayed()
                .fetchSemanticsNode()
                .boundsInRoot

            assertEquals(playBounds.height, difficultyBounds.height, 0.5f)
            assertEquals(difficultyBounds.width, difficultyBounds.height, 0.5f)
            assertTrue(playBounds.right < difficultyBounds.left)
        }
    }

    private fun assertSelectorEndAlignment(layoutDirection: LayoutDirection) {
        setContent(
            width = 700.dp,
            height = 600.dp,
            layoutDirection = layoutDirection
        )

        composeTestRule
            .onNodeWithTag(MenuScreenTestTags.FOUR_PAIRS_DIFFICULTY_BUTTON)
            .performClick()
        val triggerBounds = composeTestRule
            .onNodeWithTag(MenuScreenTestTags.FOUR_PAIRS_DIFFICULTY_BUTTON)
            .fetchSemanticsNode()
            .boundsInRoot
        val menuBounds = composeTestRule
            .onNodeWithTag(MenuScreenTestTags.FOUR_PAIRS_DIFFICULTY_MENU)
            .assertIsDisplayed()
            .fetchSemanticsNode()
            .boundsInRoot

        if (layoutDirection == LayoutDirection.Ltr) {
            assertEquals(triggerBounds.right, menuBounds.right, 1f)
        } else {
            assertEquals(triggerBounds.left, menuBounds.left, 1f)
        }
    }

    private fun setContent(
        width: Dp? = null,
        height: Dp? = null,
        fontScale: Float = 1f,
        layoutDirection: LayoutDirection = LayoutDirection.Ltr,
        onFourPairsSelected: () -> Unit = {},
        onFourPairsDifficultySelected: (GeneratedDifficultyMenuOptionId) -> Unit = {}
    ) {
        composeTestRule.setContent {
            val currentDensity = LocalDensity.current
            CompositionLocalProvider(
                LocalDensity provides Density(
                    density = currentDensity.density,
                    fontScale = fontScale
                ),
                LocalLayoutDirection provides layoutDirection
            ) {
                NumPairsTheme {
                    if (width != null && height != null) {
                        Box(modifier = Modifier.size(width = width, height = height)) {
                            MenuContent(
                                onFourPairsSelected = onFourPairsSelected,
                                onFourPairsDifficultySelected = onFourPairsDifficultySelected
                            )
                        }
                    } else {
                        MenuContent(
                            onFourPairsSelected = onFourPairsSelected,
                            onFourPairsDifficultySelected = onFourPairsDifficultySelected
                        )
                    }
                }
            }
        }
    }

    @Composable
    private fun MenuContent(
        onFourPairsSelected: () -> Unit,
        onFourPairsDifficultySelected: (GeneratedDifficultyMenuOptionId) -> Unit
    ) {
        MenuScreen(
            fourPairsMode = fourPairsState,
            eightPairsMode = eightPairsState,
            onFourPairsSelected = onFourPairsSelected,
            onFourPairsDifficultySelected = onFourPairsDifficultySelected
        )
    }

    private fun string(stringResId: Int, vararg formatArgs: Any): String =
        composeTestRule.activity.getString(stringResId, *formatArgs)

    private companion object {
        val fourPairsLowId = GeneratedDifficultyMenuOptionId("four-pairs-low")
        val fourPairsMediumId = GeneratedDifficultyMenuOptionId("four-pairs-medium")
        val eightPairsMediumId = GeneratedDifficultyMenuOptionId("eight-pairs-medium")
        val eightPairsHardId = GeneratedDifficultyMenuOptionId("eight-pairs-hard")

        val fourPairsState = GeneratedModeMenuUiState(
            modeName = "4 pairs",
            challengeName = "4 pairs · Low",
            difficultyOptions = listOf(
                GeneratedDifficultyMenuOptionUiState(fourPairsLowId, "Low"),
                GeneratedDifficultyMenuOptionUiState(fourPairsMediumId, "Medium")
            ),
            selectedDifficultyOptionId = fourPairsLowId
        )
        val eightPairsState = GeneratedModeMenuUiState(
            modeName = "8 pairs",
            challengeName = "8 pairs · Medium",
            difficultyOptions = listOf(
                GeneratedDifficultyMenuOptionUiState(eightPairsMediumId, "Medium"),
                GeneratedDifficultyMenuOptionUiState(eightPairsHardId, "Hard")
            ),
            selectedDifficultyOptionId = eightPairsMediumId
        )
    }
}
