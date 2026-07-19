package org.cescfe.numpairs.feature.onboarding

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import org.cescfe.numpairs.R
import org.cescfe.numpairs.ui.theme.NumPairsComponents

@Composable
internal fun OnboardingStartupFailureScreen(isRetrying: Boolean, onRetry: () -> Unit, modifier: Modifier = Modifier) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(24.dp)
            .testTag(ONBOARDING_STARTUP_FAILURE_SCREEN_TEST_TAG),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = stringResource(R.string.onboarding_startup_failure_title),
            color = MaterialTheme.colorScheme.onBackground,
            textAlign = TextAlign.Center,
            style = MaterialTheme.typography.headlineSmall
        )
        Text(
            text = stringResource(R.string.onboarding_startup_failure_message),
            modifier = Modifier.padding(top = 12.dp),
            color = MaterialTheme.colorScheme.onBackground,
            textAlign = TextAlign.Center,
            style = MaterialTheme.typography.bodyLarge
        )
        NumPairsComponents.PrimaryCtaButton(
            onClick = onRetry,
            modifier = Modifier
                .padding(top = 24.dp)
                .testTag(ONBOARDING_STARTUP_RETRY_BUTTON_TEST_TAG),
            enabled = !isRetrying
        ) {
            Text(
                text = stringResource(R.string.onboarding_startup_retry_button),
                style = MaterialTheme.typography.labelLarge
            )
        }
    }
}

internal const val ONBOARDING_STARTUP_FAILURE_SCREEN_TEST_TAG = "onboarding_startup_failure_screen"
internal const val ONBOARDING_STARTUP_RETRY_BUTTON_TEST_TAG = "onboarding_startup_retry_button"
