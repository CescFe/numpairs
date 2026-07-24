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
import androidx.compose.ui.semantics.SemanticsActions
import androidx.compose.ui.semantics.SemanticsProperties
import androidx.compose.ui.test.SemanticsMatcher
import androidx.compose.ui.test.assert
import androidx.compose.ui.test.assertContentDescriptionEquals
import androidx.compose.ui.test.assertHasClickAction
import androidx.compose.ui.test.assertHasNoClickAction
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertIsNotSelected
import androidx.compose.ui.test.assertIsSelected
import androidx.compose.ui.test.assertTextEquals
import androidx.compose.ui.test.click
import androidx.compose.ui.test.junit4.v2.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.onRoot
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollTo
import androidx.compose.ui.test.performSemanticsAction
import androidx.compose.ui.test.performTouchInput
import androidx.compose.ui.text.TextLayoutResult
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.test.espresso.Espresso.pressBackUnconditionally
import androidx.test.ext.junit.runners.AndroidJUnit4
import org.cescfe.numpairs.R
import org.cescfe.numpairs.ui.theme.NumPairsComponents
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
    fun settings_action_replaces_personalization_button_and_emits_action() {
        var settingsClickCount = 0
        setContent(
            onPersonalizationSelected = {
                settingsClickCount += 1
            }
        )

        composeTestRule
            .onNodeWithTag(MenuScreenTestTags.SETTINGS_ACTION)
            .assertIsDisplayed()
            .assertHasClickAction()
            .assertContentDescriptionEquals(
                string(R.string.menu_settings_action_content_description)
            )
            .performClick()
        composeTestRule
            .onNodeWithText(string(R.string.personalization_screen_title))
            .assertDoesNotExist()
        composeTestRule.runOnIdle {
            assertEquals(1, settingsClickCount)
        }
    }

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
            .assertHasClickAction()
            .performClick()
        composeTestRule
            .onNodeWithTag(MenuScreenTestTags.FOUR_PAIRS_DIFFICULTY_BUTTON)
            .assertHasClickAction()
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
    fun selector_action_switches_between_collapsed_and_expanded_arrow_states() {
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
            .onNodeWithTag(MenuScreenTestTags.EIGHT_PAIRS_BUTTON)
            .assertContentDescriptionEquals(
                string(
                    R.string.menu_play_generated_challenge_content_description,
                    eightPairsState.challengeName
                )
            )
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
    fun generated_mode_split_ctas_match_full_width_buttons_and_keep_distinct_touch_regions() {
        setContent()

        val tutorialBounds = composeTestRule
            .onNodeWithTag(MenuScreenTestTags.TUTORIAL_BUTTON)
            .assertIsDisplayed()
            .fetchSemanticsNode()
            .boundsInRoot
        val dividerWidth = with(composeTestRule.density) {
            NumPairsComponents.ThinBorderWidth.toPx()
        }
        val minimumTouchTargetWidth = with(composeTestRule.density) {
            48.dp.toPx()
        }
        listOf(
            Triple(
                MenuScreenTestTags.FOUR_PAIRS_SPLIT_CTA,
                MenuScreenTestTags.FOUR_PAIRS_BUTTON,
                MenuScreenTestTags.FOUR_PAIRS_DIFFICULTY_BUTTON
            ),
            Triple(
                MenuScreenTestTags.EIGHT_PAIRS_SPLIT_CTA,
                MenuScreenTestTags.EIGHT_PAIRS_BUTTON,
                MenuScreenTestTags.EIGHT_PAIRS_DIFFICULTY_BUTTON
            )
        ).forEach { (containerTag, playTag, difficultyTag) ->
            val containerBounds = composeTestRule
                .onNodeWithTag(containerTag)
                .assertIsDisplayed()
                .assertHasNoClickAction()
                .fetchSemanticsNode()
                .boundsInRoot
            val playBounds = composeTestRule
                .onNodeWithTag(playTag)
                .assertIsDisplayed()
                .assertHasClickAction()
                .fetchSemanticsNode()
                .boundsInRoot
            val difficultyBounds = composeTestRule
                .onNodeWithTag(difficultyTag)
                .assertIsDisplayed()
                .assertHasClickAction()
                .fetchSemanticsNode()
                .boundsInRoot

            assertEquals(tutorialBounds.left, containerBounds.left, 0.5f)
            assertEquals(tutorialBounds.right, containerBounds.right, 0.5f)
            assertEquals(tutorialBounds.height, containerBounds.height, 0.5f)
            assertEquals(containerBounds.left, playBounds.left, 0.5f)
            assertEquals(containerBounds.right, difficultyBounds.right, 0.5f)
            assertEquals(containerBounds.height, playBounds.height, 0.5f)
            assertEquals(containerBounds.height, difficultyBounds.height, 0.5f)
            assertEquals(difficultyBounds.width, difficultyBounds.height, 0.5f)
            assertEquals(dividerWidth, difficultyBounds.left - playBounds.right, 0.5f)
            assertTrue(difficultyBounds.width >= minimumTouchTargetWidth)
        }
    }

    @Test
    fun generated_mode_labels_match_the_other_menu_button_typography() {
        setContent()

        val tutorialStyle = textStyle(string(R.string.menu_tutorial_button))
        assertEquals(22.sp, tutorialStyle.fontSize)
        listOf(fourPairsState.challengeName, eightPairsState.challengeName).forEach { challengeName ->
            val challengeStyle = textStyle(challengeName)

            assertEquals(tutorialStyle.fontSize, challengeStyle.fontSize)
            assertEquals(tutorialStyle.fontWeight, challengeStyle.fontWeight)
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
        onPersonalizationSelected: () -> Unit = {},
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
                                onPersonalizationSelected = onPersonalizationSelected,
                                onFourPairsSelected = onFourPairsSelected,
                                onFourPairsDifficultySelected = onFourPairsDifficultySelected
                            )
                        }
                    } else {
                        MenuContent(
                            onPersonalizationSelected = onPersonalizationSelected,
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
        onPersonalizationSelected: () -> Unit,
        onFourPairsSelected: () -> Unit,
        onFourPairsDifficultySelected: (GeneratedDifficultyMenuOptionId) -> Unit
    ) {
        MenuScreen(
            fourPairsMode = fourPairsState,
            eightPairsMode = eightPairsState,
            onPersonalizationSelected = onPersonalizationSelected,
            onFourPairsSelected = onFourPairsSelected,
            onFourPairsDifficultySelected = onFourPairsDifficultySelected
        )
    }

    private fun string(stringResId: Int, vararg formatArgs: Any): String =
        composeTestRule.activity.getString(stringResId, *formatArgs)

    private fun textStyle(text: String): TextStyle {
        val layoutResults = mutableListOf<TextLayoutResult>()
        composeTestRule
            .onNodeWithText(text = text, useUnmergedTree = true)
            .performSemanticsAction(SemanticsActions.GetTextLayoutResult) { action ->
                action(layoutResults)
            }

        return layoutResults.single().layoutInput.style
    }

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
