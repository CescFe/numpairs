package org.cescfe.numpairs.feature.menu.ui

import androidx.activity.ComponentActivity
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
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
import java.time.LocalDate
import org.cescfe.numpairs.R
import org.cescfe.numpairs.feature.daily.DailyRecipes
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
    fun daily_primary_state_updates_label_action_and_non_color_semantics() {
        val identity = DailyRecipes.FOUR_PAIRS_LOW_V1.identityFor(
            LocalDate.of(2026, 7, 25)
        )
        var dailyState by mutableStateOf<DailyMenuUiState>(
            DailyMenuUiState.StartToday(identity)
        )

        composeTestRule.setContent {
            NumPairsTheme {
                MenuScreen(
                    dailyChallenge = dailyState,
                    fourPairsMode = fourPairsState,
                    eightPairsMode = eightPairsState
                )
            }
        }

        composeTestRule
            .onNodeWithTag(MenuScreenTestTags.DAILY_BUTTON)
            .assertTextEquals(string(R.string.menu_daily_start_button))
            .assertContentDescriptionEquals(
                string(R.string.menu_daily_start_content_description)
            ).assert(
                SemanticsMatcher.expectValue(
                    SemanticsProperties.StateDescription,
                    string(R.string.menu_daily_not_started_state)
                )
            ).assertIsNotSelected()

        composeTestRule.runOnIdle {
            dailyState = DailyMenuUiState.ContinueToday(identity)
        }
        composeTestRule
            .onNodeWithTag(MenuScreenTestTags.DAILY_BUTTON)
            .assertTextEquals(string(R.string.menu_daily_continue_button))
            .assertContentDescriptionEquals(
                string(R.string.menu_daily_continue_content_description)
            ).assert(
                SemanticsMatcher.expectValue(
                    SemanticsProperties.StateDescription,
                    string(R.string.menu_daily_in_progress_state)
                )
            ).assertIsNotSelected()

        composeTestRule.runOnIdle {
            dailyState = DailyMenuUiState.CompletedToday(identity)
        }
        composeTestRule
            .onNodeWithTag(MenuScreenTestTags.DAILY_BUTTON)
            .assertTextEquals(string(R.string.menu_daily_completed_button))
            .assertContentDescriptionEquals(
                string(R.string.menu_daily_completed_content_description)
            ).assert(
                SemanticsMatcher.expectValue(
                    SemanticsProperties.StateDescription,
                    string(R.string.menu_daily_completed_state)
                )
            ).assertIsSelected()
    }

    @Test
    fun daily_split_action_is_first_full_width_and_emits_primary_and_calendar_separately() {
        val identity = DailyRecipes.FOUR_PAIRS_LOW_V1.identityFor(
            LocalDate.of(2026, 7, 25)
        )
        var primaryCount = 0
        var calendarCount = 0

        setContent(
            dailyChallenge = DailyMenuUiState.StartToday(identity),
            resumeChallengeName = "4 pairs · Low",
            onDailySelected = { primaryCount += 1 },
            onDailyCalendarSelected = { calendarCount += 1 }
        )

        val dailyContainer = composeTestRule
            .onNodeWithTag(MenuScreenTestTags.DAILY_SPLIT_CTA)
            .assertIsDisplayed()
            .assertHasNoClickAction()
            .fetchSemanticsNode()
            .boundsInRoot
        val dailyPrimary = composeTestRule
            .onNodeWithTag(MenuScreenTestTags.DAILY_BUTTON)
            .assertHasClickAction()
            .fetchSemanticsNode()
            .boundsInRoot
        val dailyCalendar = composeTestRule
            .onNodeWithTag(MenuScreenTestTags.DAILY_CALENDAR_BUTTON)
            .assertHasClickAction()
            .assertContentDescriptionEquals(
                string(R.string.menu_daily_calendar_content_description)
            ).fetchSemanticsNode()
            .boundsInRoot
        val resumeTop = composeTestRule
            .onNodeWithTag(MenuScreenTestTags.RESUME_BUTTON)
            .fetchSemanticsNode()
            .boundsInRoot
            .top
        val quickBounds = composeTestRule
            .onNodeWithTag(MenuScreenTestTags.QUICK_BUTTON)
            .fetchSemanticsNode()
            .boundsInRoot

        assertTrue(dailyContainer.top < resumeTop)
        assertTrue(resumeTop < quickBounds.top)
        assertEquals(quickBounds.left, dailyContainer.left, 0.5f)
        assertEquals(quickBounds.right, dailyContainer.right, 0.5f)
        assertEquals(dailyContainer.left, dailyPrimary.left, 0.5f)
        assertEquals(dailyContainer.right, dailyCalendar.right, 0.5f)
        assertTrue(
            dailyCalendar.width >= with(composeTestRule.density) {
                48.dp.toPx()
            }
        )

        composeTestRule
            .onNodeWithTag(MenuScreenTestTags.DAILY_CALENDAR_BUTTON)
            .performClick()
        composeTestRule
            .onNodeWithTag(MenuScreenTestTags.DAILY_BUTTON)
            .performClick()
        composeTestRule.runOnIdle {
            assertEquals(1, primaryCount)
            assertEquals(1, calendarCount)
        }
    }

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
    fun quick_is_one_accessible_primary_action_without_a_difficulty_selector() {
        var quickClickCount = 0
        setContent(
            onQuickSelected = {
                quickClickCount += 1
            }
        )

        val quickNode = composeTestRule
            .onNodeWithTag(MenuScreenTestTags.QUICK_BUTTON)
            .assertIsDisplayed()
            .assertTextEquals(string(R.string.quick_screen_title))
            .assertContentDescriptionEquals(
                string(
                    R.string.menu_play_quick_content_description,
                    string(R.string.quick_screen_title),
                    string(R.string.three_pairs_accessibility_name),
                    string(R.string.generated_difficulty_low)
                )
            )
            .assertHasClickAction()
        composeTestRule
            .onNodeWithTag(MenuScreenTestTags.QUICK_DIFFICULTY_BUTTON)
            .assertDoesNotExist()
        composeTestRule
            .onNodeWithTag(MenuScreenTestTags.FOUR_PAIRS_DIFFICULTY_BUTTON)
            .assertIsDisplayed()
        composeTestRule
            .onNodeWithTag(MenuScreenTestTags.EIGHT_PAIRS_DIFFICULTY_BUTTON)
            .assertIsDisplayed()

        val quickTop = quickNode.fetchSemanticsNode().boundsInRoot.top
        val fourPairsTop = composeTestRule
            .onNodeWithTag(MenuScreenTestTags.FOUR_PAIRS_BUTTON)
            .fetchSemanticsNode()
            .boundsInRoot
            .top
        assertTrue(quickTop < fourPairsTop)

        quickNode.performClick()
        composeTestRule.runOnIdle {
            assertEquals(1, quickClickCount)
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
        val quickBounds = composeTestRule
            .onNodeWithTag(MenuScreenTestTags.QUICK_BUTTON)
            .assertIsDisplayed()
            .fetchSemanticsNode()
            .boundsInRoot
        assertEquals(tutorialBounds.left, quickBounds.left, 0.5f)
        assertEquals(tutorialBounds.right, quickBounds.right, 0.5f)
        assertEquals(tutorialBounds.height, quickBounds.height, 0.5f)
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
        listOf(
            string(R.string.quick_screen_title),
            fourPairsState.challengeName,
            eightPairsState.challengeName
        ).forEach { actionName ->
            val challengeStyle = textStyle(actionName)

            assertEquals(tutorialStyle.fontSize, challengeStyle.fontSize)
            assertEquals(tutorialStyle.fontWeight, challengeStyle.fontWeight)
        }
    }

    @Test
    fun compact_height_large_text_and_rtl_keep_quick_scrollable_and_full_width() {
        setContent(
            width = 320.dp,
            height = 360.dp,
            fontScale = 2f,
            layoutDirection = LayoutDirection.Rtl,
            dailyChallenge = DailyMenuUiState.StartToday(
                DailyRecipes.FOUR_PAIRS_LOW_V1.identityFor(
                    LocalDate.of(2026, 7, 25)
                )
            )
        )

        val dailyBounds = composeTestRule
            .onNodeWithTag(MenuScreenTestTags.DAILY_SPLIT_CTA)
            .assertIsDisplayed()
            .fetchSemanticsNode()
            .boundsInRoot
        val quickBounds = composeTestRule
            .onNodeWithTag(MenuScreenTestTags.QUICK_BUTTON)
            .performScrollTo()
            .assertIsDisplayed()
            .fetchSemanticsNode()
            .boundsInRoot
        val fourPairsBounds = composeTestRule
            .onNodeWithTag(MenuScreenTestTags.FOUR_PAIRS_SPLIT_CTA)
            .performScrollTo()
            .assertIsDisplayed()
            .fetchSemanticsNode()
            .boundsInRoot

        assertEquals(fourPairsBounds.left, quickBounds.left, 0.5f)
        assertEquals(fourPairsBounds.right, quickBounds.right, 0.5f)
        assertEquals(fourPairsBounds.height, quickBounds.height, 0.5f)
        assertEquals(quickBounds.left, dailyBounds.left, 0.5f)
        assertEquals(quickBounds.right, dailyBounds.right, 0.5f)
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
        dailyChallenge: DailyMenuUiState? = null,
        resumeChallengeName: String? = null,
        onDailySelected: () -> Unit = {},
        onDailyCalendarSelected: () -> Unit = {},
        onPersonalizationSelected: () -> Unit = {},
        onQuickSelected: () -> Unit = {},
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
                                dailyChallenge = dailyChallenge,
                                resumeChallengeName = resumeChallengeName,
                                onDailySelected = onDailySelected,
                                onDailyCalendarSelected = onDailyCalendarSelected,
                                onPersonalizationSelected = onPersonalizationSelected,
                                onQuickSelected = onQuickSelected,
                                onFourPairsSelected = onFourPairsSelected,
                                onFourPairsDifficultySelected = onFourPairsDifficultySelected
                            )
                        }
                    } else {
                        MenuContent(
                            dailyChallenge = dailyChallenge,
                            resumeChallengeName = resumeChallengeName,
                            onDailySelected = onDailySelected,
                            onDailyCalendarSelected = onDailyCalendarSelected,
                            onPersonalizationSelected = onPersonalizationSelected,
                            onQuickSelected = onQuickSelected,
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
        dailyChallenge: DailyMenuUiState?,
        resumeChallengeName: String?,
        onDailySelected: () -> Unit,
        onDailyCalendarSelected: () -> Unit,
        onPersonalizationSelected: () -> Unit,
        onQuickSelected: () -> Unit,
        onFourPairsSelected: () -> Unit,
        onFourPairsDifficultySelected: (GeneratedDifficultyMenuOptionId) -> Unit
    ) {
        MenuScreen(
            dailyChallenge = dailyChallenge,
            resumeChallengeName = resumeChallengeName,
            fourPairsMode = fourPairsState,
            eightPairsMode = eightPairsState,
            onDailySelected = onDailySelected,
            onDailyCalendarSelected = onDailyCalendarSelected,
            onPersonalizationSelected = onPersonalizationSelected,
            onQuickSelected = onQuickSelected,
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
