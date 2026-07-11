package org.cescfe.numpairs.feature.fourpairs

import androidx.activity.ComponentActivity
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.v2.createAndroidComposeRule
import androidx.compose.ui.test.onAllNodesWithTag
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performClick
import androidx.test.ext.junit.runners.AndroidJUnit4
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.emptyFlow
import org.cescfe.numpairs.data.preferences.FakeTopAppBarActionDiscoveryRepository
import org.cescfe.numpairs.data.preferences.TopAppBarActionDiscoveryRepository
import org.cescfe.numpairs.data.preferences.TopAppBarActionDiscoveryState
import org.cescfe.numpairs.data.puzzle.seed.samplePuzzle
import org.cescfe.numpairs.domain.puzzle.model.Puzzle
import org.cescfe.numpairs.feature.game.ui.screen.GameScreenTestTags
import org.cescfe.numpairs.feature.generated.GeneratedPuzzleGenerationResult
import org.cescfe.numpairs.feature.generated.GeneratedPuzzleGenerationUseCase
import org.cescfe.numpairs.ui.theme.NumPairsTheme
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class FourPairsActionDiscoveryTest {
    @get:Rule
    val composeTestRule = createAndroidComposeRule<ComponentActivity>()

    @Test
    fun discoveryDotsAreHiddenUntilDiscoveryStateLoads() {
        val actionDiscoveryRepository = LoadingTopAppBarActionDiscoveryRepository()

        composeTestRule.setContent {
            NumPairsTheme {
                FourPairsRoute(
                    generationUseCase = generatedPuzzleUseCase(puzzle = samplePuzzle),
                    topAppBarActionDiscoveryRepository = actionDiscoveryRepository
                )
            }
        }

        composeTestRule
            .onNodeWithTag(GameScreenTestTags.RULES_HELPER_ACTION)
            .assertIsDisplayed()
        composeTestRule
            .onNodeWithTag(GameScreenTestTags.HINT_ACTION)
            .assertIsDisplayed()
        composeTestRule
            .onNodeWithTag(
                GameScreenTestTags.RULES_HELPER_ACTION_DISCOVERY_DOT,
                useUnmergedTree = true
            )
            .assertDoesNotExist()
        composeTestRule
            .onNodeWithTag(
                GameScreenTestTags.HINT_ACTION_DISCOVERY_DOT,
                useUnmergedTree = true
            )
            .assertDoesNotExist()
    }

    @Test
    fun topAppBarActionsMarkHelpAndHintDiscoveryIndependently() {
        val actionDiscoveryRepository = FakeTopAppBarActionDiscoveryRepository()

        composeTestRule.setContent {
            NumPairsTheme {
                FourPairsRoute(
                    generationUseCase = generatedPuzzleUseCase(puzzle = samplePuzzle),
                    topAppBarActionDiscoveryRepository = actionDiscoveryRepository
                )
            }
        }

        composeTestRule.runOnIdle {
            assertFalse(actionDiscoveryRepository.state.value.hasSeenHelpAction)
            assertFalse(actionDiscoveryRepository.state.value.hasSeenHintAction)
        }
        waitForDiscoveryDots(
            rulesHelperDotVisible = true,
            hintDotVisible = true
        )
        composeTestRule
            .onNodeWithTag(
                GameScreenTestTags.RULES_HELPER_ACTION_DISCOVERY_DOT,
                useUnmergedTree = true
            )
            .assertIsDisplayed()
        composeTestRule
            .onNodeWithTag(
                GameScreenTestTags.HINT_ACTION_DISCOVERY_DOT,
                useUnmergedTree = true
            )
            .assertIsDisplayed()

        composeTestRule
            .onNodeWithTag(GameScreenTestTags.RULES_HELPER_ACTION)
            .assertIsDisplayed()
            .performClick()

        composeTestRule.waitUntil {
            actionDiscoveryRepository.state.value.hasSeenHelpAction
        }
        composeTestRule.runOnIdle {
            assertTrue(actionDiscoveryRepository.state.value.hasSeenHelpAction)
            assertFalse(actionDiscoveryRepository.state.value.hasSeenHintAction)
        }
        waitForDiscoveryDots(
            rulesHelperDotVisible = false,
            hintDotVisible = true
        )
        composeTestRule
            .onNodeWithTag(
                GameScreenTestTags.RULES_HELPER_ACTION_DISCOVERY_DOT,
                useUnmergedTree = true
            )
            .assertDoesNotExist()
        composeTestRule
            .onNodeWithTag(
                GameScreenTestTags.HINT_ACTION_DISCOVERY_DOT,
                useUnmergedTree = true
            )
            .assertIsDisplayed()

        composeTestRule
            .onNodeWithTag(GameScreenTestTags.RULES_HELPER_CLOSE_BUTTON)
            .assertIsDisplayed()
            .performClick()

        composeTestRule
            .onNodeWithTag(GameScreenTestTags.HINT_ACTION)
            .assertIsDisplayed()
            .performClick()

        composeTestRule.waitUntil {
            actionDiscoveryRepository.state.value.hasSeenHintAction
        }
        composeTestRule.runOnIdle {
            assertTrue(actionDiscoveryRepository.state.value.hasSeenHelpAction)
            assertTrue(actionDiscoveryRepository.state.value.hasSeenHintAction)
        }
        waitForDiscoveryDots(
            rulesHelperDotVisible = false,
            hintDotVisible = false
        )
        composeTestRule
            .onNodeWithTag(
                GameScreenTestTags.RULES_HELPER_ACTION_DISCOVERY_DOT,
                useUnmergedTree = true
            )
            .assertDoesNotExist()
        composeTestRule
            .onNodeWithTag(
                GameScreenTestTags.HINT_ACTION_DISCOVERY_DOT,
                useUnmergedTree = true
            )
            .assertDoesNotExist()
    }

    private fun waitForDiscoveryDots(rulesHelperDotVisible: Boolean, hintDotVisible: Boolean) {
        composeTestRule.waitUntil {
            hasDiscoveryDot(
                testTag = GameScreenTestTags.RULES_HELPER_ACTION_DISCOVERY_DOT
            ) == rulesHelperDotVisible &&
                hasDiscoveryDot(
                    testTag = GameScreenTestTags.HINT_ACTION_DISCOVERY_DOT
                ) == hintDotVisible
        }
    }

    private fun generatedPuzzleUseCase(puzzle: Puzzle): GeneratedPuzzleGenerationUseCase =
        GeneratedPuzzleGenerationUseCase { request ->
            GeneratedPuzzleGenerationResult.Generated(
                request = request,
                initialPuzzle = puzzle
            )
        }

    private fun hasDiscoveryDot(testTag: String): Boolean = composeTestRule
        .onAllNodesWithTag(testTag, useUnmergedTree = true)
        .fetchSemanticsNodes()
        .isNotEmpty()

    private class LoadingTopAppBarActionDiscoveryRepository : TopAppBarActionDiscoveryRepository {
        override val discoveryState: Flow<TopAppBarActionDiscoveryState> = emptyFlow()

        override suspend fun markHelpActionSeen() = Unit

        override suspend fun markHintActionSeen() = Unit
    }
}
