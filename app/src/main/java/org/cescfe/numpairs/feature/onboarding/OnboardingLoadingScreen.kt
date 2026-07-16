package org.cescfe.numpairs.feature.onboarding

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag

@Composable
fun OnboardingLoadingScreen(modifier: Modifier = Modifier) {
    Box(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .testTag(ONBOARDING_LOADING_SCREEN_TEST_TAG),
        contentAlignment = Alignment.Center
    ) {
        CircularProgressIndicator()
    }
}

const val ONBOARDING_LOADING_SCREEN_TEST_TAG = "onboarding_loading_screen"
