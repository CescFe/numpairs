package org.cescfe.numpairs.feature.generated.selector.ui

import androidx.activity.ComponentActivity
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.SemanticsProperties
import androidx.compose.ui.test.SemanticsMatcher
import androidx.compose.ui.test.assert
import androidx.compose.ui.test.assertHasClickAction
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertIsEnabled
import androidx.compose.ui.test.assertIsNotSelected
import androidx.compose.ui.test.assertIsSelected
import androidx.compose.ui.test.assertTextContains
import androidx.compose.ui.test.assertTextEquals
import androidx.compose.ui.test.junit4.v2.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollTo
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.test.ext.junit.runners.AndroidJUnit4
import org.cescfe.numpairs.R
import org.cescfe.numpairs.ui.theme.NumPairsTheme
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class GeneratedDifficultySelectorScreenTest {
    @get:Rule
    val composeTestRule = createAndroidComposeRule<ComponentActivity>()

    @Test
    fun four_pairs_presents_only_low_and_medium_with_an_identified_primary_action() {
        setContent(state = fourPairsState)

        composeTestRule
            .onNodeWithTag(GeneratedDifficultySelectorTestTags.MODE_NAME)
            .assertTextEquals("4 pairs")
        composeTestRule.onNodeWithTag(optionTag(lowId)).assertIsSelected()
        composeTestRule.onNodeWithTag(optionTag(mediumId)).assertIsNotSelected()
        composeTestRule.onNodeWithTag(optionTag(hardId)).assertDoesNotExist()
        composeTestRule
            .onNodeWithTag(GeneratedDifficultySelectorTestTags.PLAY_BUTTON)
            .assertTextContains("4 pairs", substring = true)
            .assertTextContains("Low", substring = true)
    }

    @Test
    fun eight_pairs_presents_only_medium_and_hard_with_every_option_enabled() {
        setContent(state = eightPairsState)

        composeTestRule
            .onNodeWithTag(GeneratedDifficultySelectorTestTags.MODE_NAME)
            .assertTextEquals("8 pairs")
        composeTestRule.onNodeWithTag(optionTag(lowId)).assertDoesNotExist()
        composeTestRule.onNodeWithTag(optionTag(mediumId)).assertIsNotSelected().assertIsEnabled()
        composeTestRule.onNodeWithTag(optionTag(hardId)).assertIsSelected().assertIsEnabled()
        composeTestRule
            .onNodeWithTag(GeneratedDifficultySelectorTestTags.PLAY_BUTTON)
            .assertTextContains("8 pairs", substring = true)
            .assertTextContains("Hard", substring = true)
    }

    @Test
    fun selection_play_and_back_emit_explicit_events_from_state_driven_content() {
        var selectedId: GeneratedDifficultyOptionId? = null
        var playedId: GeneratedDifficultyOptionId? = null
        var backCount = 0
        var state by mutableStateOf(fourPairsState)
        setContent(
            stateProvider = { state },
            onDifficultySelected = { optionId ->
                selectedId = optionId
                state = state.copy(selectedOptionId = optionId)
            },
            onPlay = { optionId -> playedId = optionId },
            onNavigateBack = { backCount += 1 }
        )

        composeTestRule.onNodeWithTag(optionTag(mediumId)).performClick().assertIsSelected()
        composeTestRule.onNodeWithTag(GeneratedDifficultySelectorTestTags.PLAY_BUTTON).performClick()
        composeTestRule.onNodeWithTag(GeneratedDifficultySelectorTestTags.BACK_BUTTON).performClick()

        composeTestRule.runOnIdle {
            assertEquals(mediumId, selectedId)
            assertEquals(mediumId, playedId)
            assertEquals(1, backCount)
        }
    }

    @Test
    fun selected_option_exposes_radio_semantics_and_a_non_color_selected_label() {
        setContent(state = fourPairsState)
        val selectedText = composeTestRule.activity.getString(R.string.difficulty_selector_selected)

        composeTestRule
            .onNodeWithTag(optionTag(lowId))
            .assertIsSelected()
            .assertHasClickAction()
            .assert(
                SemanticsMatcher.expectValue(
                    SemanticsProperties.Role,
                    Role.RadioButton
                )
            ).assert(
                SemanticsMatcher.expectValue(
                    SemanticsProperties.StateDescription,
                    selectedText
                )
            )
        composeTestRule
            .onNodeWithTag(
                GeneratedDifficultySelectorTestTags.SELECTED_LABEL,
                useUnmergedTree = true
            )
            .assertIsDisplayed()
            .assertTextEquals(selectedText)
        composeTestRule
            .onNodeWithContentDescription(
                composeTestRule.activity.getString(R.string.back_button_content_description)
            )
            .assertHasClickAction()
    }

    @Test
    fun controls_meet_minimum_touch_targets() {
        setContent(state = fourPairsState)
        val minimumTouchTargetPx = 48 * composeTestRule.activity.resources.displayMetrics.density

        listOf(lowId, mediumId).forEach { optionId ->
            val bounds = composeTestRule
                .onNodeWithTag(optionTag(optionId))
                .assertIsDisplayed()
                .fetchSemanticsNode()
                .boundsInRoot
            assertTrue("${optionId.value} option was too short", bounds.height >= minimumTouchTargetPx)
            assertTrue("${optionId.value} option was too narrow", bounds.width >= minimumTouchTargetPx)
        }
        val playBounds = composeTestRule
            .onNodeWithTag(GeneratedDifficultySelectorTestTags.PLAY_BUTTON)
            .assertIsDisplayed()
            .fetchSemanticsNode()
            .boundsInRoot
        assertTrue(playBounds.height >= minimumTouchTargetPx)
        assertTrue(playBounds.width >= minimumTouchTargetPx)
    }

    @Test
    fun compact_width_and_increased_text_scale_keep_options_and_primary_action_reachable() {
        setContent(
            state = eightPairsState,
            width = 320.dp,
            height = 480.dp,
            fontScale = 2f
        )

        val screenBounds = composeTestRule
            .onNodeWithTag(GeneratedDifficultySelectorTestTags.SCREEN)
            .assertIsDisplayed()
            .fetchSemanticsNode()
            .boundsInRoot
        val hardBounds = composeTestRule
            .onNodeWithTag(optionTag(hardId))
            .performScrollTo()
            .assertIsDisplayed()
            .fetchSemanticsNode()
            .boundsInRoot
        val playBounds = composeTestRule
            .onNodeWithTag(GeneratedDifficultySelectorTestTags.PLAY_BUTTON)
            .assertIsDisplayed()
            .fetchSemanticsNode()
            .boundsInRoot

        assertTrue(hardBounds.left >= screenBounds.left)
        assertTrue(hardBounds.right <= screenBounds.right)
        assertTrue(playBounds.left >= screenBounds.left)
        assertTrue(playBounds.right <= screenBounds.right)
    }

    private fun setContent(
        state: GeneratedDifficultySelectorUiState,
        width: Dp? = null,
        height: Dp? = null,
        fontScale: Float = 1f
    ) {
        setContent(
            stateProvider = { state },
            width = width,
            height = height,
            fontScale = fontScale
        )
    }

    private fun setContent(
        stateProvider: () -> GeneratedDifficultySelectorUiState,
        onDifficultySelected: (GeneratedDifficultyOptionId) -> Unit = {},
        onPlay: (GeneratedDifficultyOptionId) -> Unit = {},
        onNavigateBack: () -> Unit = {},
        width: Dp? = null,
        height: Dp? = null,
        fontScale: Float = 1f
    ) {
        composeTestRule.setContent {
            val currentDensity = LocalDensity.current
            CompositionLocalProvider(
                LocalDensity provides Density(
                    density = currentDensity.density,
                    fontScale = fontScale
                )
            ) {
                NumPairsTheme {
                    if (width != null && height != null) {
                        Box(modifier = Modifier.size(width = width, height = height)) {
                            GeneratedDifficultySelectorScreen(
                                state = stateProvider(),
                                onDifficultySelected = onDifficultySelected,
                                onPlay = onPlay,
                                onNavigateBack = onNavigateBack
                            )
                        }
                    } else {
                        GeneratedDifficultySelectorScreen(
                            state = stateProvider(),
                            onDifficultySelected = onDifficultySelected,
                            onPlay = onPlay,
                            onNavigateBack = onNavigateBack
                        )
                    }
                }
            }
        }
    }

    private fun optionTag(id: GeneratedDifficultyOptionId): String = GeneratedDifficultySelectorTestTags.option(id)

    private companion object {
        val lowId = GeneratedDifficultyOptionId("low")
        val mediumId = GeneratedDifficultyOptionId("medium")
        val hardId = GeneratedDifficultyOptionId("hard")

        val fourPairsState = GeneratedDifficultySelectorUiState(
            modeName = "4 pairs",
            options = listOf(
                GeneratedDifficultyOptionUiState(lowId, "Low"),
                GeneratedDifficultyOptionUiState(mediumId, "Medium")
            ),
            selectedOptionId = lowId
        )

        val eightPairsState = GeneratedDifficultySelectorUiState(
            modeName = "8 pairs",
            options = listOf(
                GeneratedDifficultyOptionUiState(mediumId, "Medium"),
                GeneratedDifficultyOptionUiState(hardId, "Hard")
            ),
            selectedOptionId = hardId
        )
    }
}
