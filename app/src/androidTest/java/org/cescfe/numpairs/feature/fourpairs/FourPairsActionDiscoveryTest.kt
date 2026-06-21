package org.cescfe.numpairs.feature.fourpairs

import androidx.activity.ComponentActivity
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.v2.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performClick
import androidx.test.ext.junit.runners.AndroidJUnit4
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import org.cescfe.numpairs.data.preferences.TopAppBarActionDiscoveryRepository
import org.cescfe.numpairs.data.preferences.TopAppBarActionDiscoveryState
import org.cescfe.numpairs.data.puzzle.seed.initialPuzzle
import org.cescfe.numpairs.feature.game.ui.GameScreenTestTags
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
    fun topAppBarActionsMarkHelpAndHintDiscoveryIndependently() {
        val actionDiscoveryRepository = FakeTopAppBarActionDiscoveryRepository()

        composeTestRule.setContent {
            NumPairsTheme {
                FourPairsRoute(
                    puzzleProvider = FourPairsPuzzleProvider { initialPuzzle },
                    topAppBarActionDiscoveryRepository = actionDiscoveryRepository
                )
            }
        }

        composeTestRule.runOnIdle {
            assertFalse(actionDiscoveryRepository.state.value.hasSeenHelpAction)
            assertFalse(actionDiscoveryRepository.state.value.hasSeenHintAction)
        }

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
    }

    private class FakeTopAppBarActionDiscoveryRepository(
        initialState: TopAppBarActionDiscoveryState = TopAppBarActionDiscoveryState()
    ) : TopAppBarActionDiscoveryRepository {
        private val mutableState = MutableStateFlow(initialState)

        val state: StateFlow<TopAppBarActionDiscoveryState> = mutableState.asStateFlow()

        override val discoveryState = state

        override suspend fun markHelpActionSeen() {
            mutableState.update { state ->
                state.copy(hasSeenHelpAction = true)
            }
        }

        override suspend fun markHintActionSeen() {
            mutableState.update { state ->
                state.copy(hasSeenHintAction = true)
            }
        }
    }
}
