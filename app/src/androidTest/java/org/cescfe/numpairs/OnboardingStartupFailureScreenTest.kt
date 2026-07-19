package org.cescfe.numpairs

import androidx.activity.ComponentActivity
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertIsEnabled
import androidx.compose.ui.test.assertIsNotEnabled
import androidx.compose.ui.test.junit4.v2.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import org.cescfe.numpairs.feature.onboarding.ONBOARDING_STARTUP_FAILURE_SCREEN_TEST_TAG
import org.cescfe.numpairs.feature.onboarding.ONBOARDING_STARTUP_RETRY_BUTTON_TEST_TAG
import org.cescfe.numpairs.feature.onboarding.OnboardingStartupFailureScreen
import org.cescfe.numpairs.ui.theme.NumPairsTheme
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test

class OnboardingStartupFailureScreenTest {
    @get:Rule
    val composeTestRule = createAndroidComposeRule<ComponentActivity>()

    @Test
    fun storageFailureOffersAnExplicitRetry() {
        var retryRequested = false
        setContent(isRetrying = false) {
            retryRequested = true
        }

        composeTestRule
            .onNodeWithTag(ONBOARDING_STARTUP_FAILURE_SCREEN_TEST_TAG)
            .assertIsDisplayed()
        composeTestRule
            .onNodeWithText(string(R.string.onboarding_startup_failure_title))
            .assertIsDisplayed()
        composeTestRule
            .onNodeWithText(string(R.string.onboarding_startup_failure_message))
            .assertIsDisplayed()
        composeTestRule
            .onNodeWithTag(ONBOARDING_STARTUP_RETRY_BUTTON_TEST_TAG)
            .assertIsEnabled()
            .performClick()

        assertTrue(retryRequested)
    }

    @Test
    fun retryActionIsDisabledWhileARequestIsInProgress() {
        setContent(isRetrying = true)

        composeTestRule
            .onNodeWithTag(ONBOARDING_STARTUP_RETRY_BUTTON_TEST_TAG)
            .assertIsNotEnabled()
    }

    private fun setContent(isRetrying: Boolean, onRetry: () -> Unit = {}) {
        composeTestRule.setContent {
            NumPairsTheme {
                OnboardingStartupFailureScreen(
                    isRetrying = isRetrying,
                    onRetry = onRetry
                )
            }
        }
    }

    private fun string(stringResId: Int): String = composeTestRule.activity.getString(stringResId)
}
