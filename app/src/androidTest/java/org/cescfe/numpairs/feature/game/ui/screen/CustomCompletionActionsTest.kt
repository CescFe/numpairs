package org.cescfe.numpairs.feature.game.ui.screen

import androidx.activity.ComponentActivity
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.v2.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollTo
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.dp
import androidx.test.ext.junit.runners.AndroidJUnit4
import org.cescfe.numpairs.feature.game.GameSuccessOverlayContent
import org.cescfe.numpairs.ui.theme.NumPairsTheme
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class CustomCompletionActionsTest {
    @get:Rule
    val composeTestRule = createAndroidComposeRule<ComponentActivity>()

    @Test
    fun custom_completion_exposes_primary_secondary_and_tertiary_actions() {
        var primaryCount = 0
        var secondaryCount = 0
        var tertiaryCount = 0

        composeTestRule.setContent {
            NumPairsTheme {
                SuccessOverlay(
                    onDismiss = {},
                    content = GameSuccessOverlayContent(
                        message = "Daily completed!",
                        supportingText = "Today’s challenge is complete.",
                        primaryActionLabel = "Share result",
                        onPrimaryAction = { primaryCount += 1 },
                        secondaryActionLabel = "View calendar",
                        onSecondaryAction = { secondaryCount += 1 },
                        tertiaryActionLabel = "Back to menu",
                        onTertiaryAction = { tertiaryCount += 1 },
                        onBackRequested = { tertiaryCount += 1 }
                    )
                )
            }
        }

        composeTestRule
            .onNodeWithTag(GameScreenTestTags.SUCCESS_OVERLAY_PRIMARY_ACTION)
            .assertIsDisplayed()
            .performClick()
        composeTestRule
            .onNodeWithTag(GameScreenTestTags.SUCCESS_OVERLAY_SECONDARY_ACTION)
            .assertIsDisplayed()
            .performClick()
        composeTestRule
            .onNodeWithTag(GameScreenTestTags.SUCCESS_OVERLAY_TERTIARY_ACTION)
            .assertIsDisplayed()
            .performClick()

        composeTestRule.runOnIdle {
            assertEquals(1, primaryCount)
            assertEquals(1, secondaryCount)
            assertEquals(1, tertiaryCount)
        }
    }

    @Test
    fun custom_completion_actions_remain_reachable_in_compact_scaled_content() {
        composeTestRule.setContent {
            val density = LocalDensity.current
            CompositionLocalProvider(
                LocalDensity provides Density(
                    density = density.density,
                    fontScale = 2f
                )
            ) {
                NumPairsTheme {
                    Box(modifier = Modifier.size(width = 320.dp, height = 420.dp)) {
                        SuccessOverlay(
                            onDismiss = {},
                            content = GameSuccessOverlayContent(
                                message = "Daily completed!",
                                supportingText = "Today’s challenge is complete.",
                                primaryActionLabel = "Share result",
                                onPrimaryAction = {},
                                secondaryActionLabel = "View calendar",
                                onSecondaryAction = {},
                                tertiaryActionLabel = "Back to menu",
                                onTertiaryAction = {}
                            )
                        )
                    }
                }
            }
        }

        composeTestRule
            .onNodeWithTag(GameScreenTestTags.SUCCESS_OVERLAY_TERTIARY_ACTION)
            .performScrollTo()
            .assertIsDisplayed()
    }
}
