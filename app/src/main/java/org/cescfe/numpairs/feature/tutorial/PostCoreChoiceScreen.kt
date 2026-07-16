package org.cescfe.numpairs.feature.tutorial

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import org.cescfe.numpairs.R
import org.cescfe.numpairs.ui.theme.NumPairsComponents

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PostCoreChoiceScreen(
    onContinueGuided: () -> Unit,
    onStartValidation: () -> Unit,
    modifier: Modifier = Modifier,
    onNavigateBack: () -> Unit = {}
) {
    Scaffold(
        modifier = modifier
            .fillMaxSize()
            .testTag(PostCoreChoiceTestTags.SCREEN),
        topBar = {
            TopAppBar(
                colors = NumPairsComponents.topAppBarColors(),
                navigationIcon = {
                    IconButton(
                        onClick = onNavigateBack,
                        modifier = Modifier.testTag(PostCoreChoiceTestTags.BACK_BUTTON)
                    ) {
                        Icon(
                            painter = painterResource(R.drawable.ic_chevron_left),
                            contentDescription = stringResource(R.string.back_button_content_description)
                        )
                    }
                },
                title = {
                    Text(text = stringResource(R.string.tutorial_screen_title))
                }
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 24.dp, vertical = 32.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Column(
                modifier = Modifier.widthIn(max = 420.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Text(
                    text = stringResource(R.string.onboarding_post_core_title),
                    style = androidx.compose.material3.MaterialTheme.typography.headlineSmall,
                    textAlign = TextAlign.Center
                )
                Text(
                    text = stringResource(R.string.onboarding_post_core_message),
                    style = androidx.compose.material3.MaterialTheme.typography.bodyLarge,
                    textAlign = TextAlign.Center
                )
                NumPairsComponents.PrimaryCtaButton(
                    onClick = onContinueGuided,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag(PostCoreChoiceTestTags.CONTINUE_BUTTON)
                ) {
                    Text(text = stringResource(R.string.onboarding_continue_guided_button))
                }
                Button(
                    onClick = onStartValidation,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag(PostCoreChoiceTestTags.EARLY_VALIDATION_BUTTON),
                    shape = NumPairsComponents.MediumShape,
                    colors = NumPairsComponents.secondaryButtonColors(),
                    border = NumPairsComponents.secondaryButtonBorder()
                ) {
                    Text(text = stringResource(R.string.onboarding_early_validation_button))
                }
            }
        }
    }
}

object PostCoreChoiceTestTags {
    const val SCREEN = "post_core_choice_screen"
    const val BACK_BUTTON = "post_core_choice_back_button"
    const val CONTINUE_BUTTON = "post_core_choice_continue_button"
    const val EARLY_VALIDATION_BUTTON = "post_core_choice_early_validation_button"
}
