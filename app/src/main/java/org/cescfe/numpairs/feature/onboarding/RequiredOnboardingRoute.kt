package org.cescfe.numpairs.feature.onboarding

import androidx.activity.compose.BackHandler
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.window.DialogProperties
import kotlinx.coroutines.launch
import org.cescfe.numpairs.R
import org.cescfe.numpairs.data.onboarding.OnboardingRepository
import org.cescfe.numpairs.data.onboarding.OnboardingStageCheckpoint
import org.cescfe.numpairs.data.onboarding.OnboardingState
import org.cescfe.numpairs.feature.tutorial.TutorialContent
import org.cescfe.numpairs.feature.tutorial.TutorialRoute
import org.cescfe.numpairs.ui.theme.NumPairsComponents

@Composable
fun RequiredOnboardingRoute(
    onboardingState: OnboardingState,
    onboardingRepository: OnboardingRepository,
    modifier: Modifier = Modifier
) {
    val coroutineScope = rememberCoroutineScope()
    val startStepIndex = onboardingState.nextRequiredTutorialStepIndex()
    var isSkipConfirmationVisible by remember { mutableStateOf(false) }
    var isSkipInProgress by remember { mutableStateOf(false) }
    val requestSkipConfirmation = {
        if (!isSkipInProgress) {
            isSkipConfirmationVisible = true
        }
    }

    BackHandler(enabled = true, onBack = requestSkipConfirmation)

    if (startStepIndex >= TutorialContent.learnBasicsSteps.size) {
        LaunchedEffect(onboardingRepository) {
            onboardingRepository.markTutorialCompleted()
        }
        OnboardingLoadingScreen(modifier = modifier)
    } else {
        TutorialRoute(
            modifier = modifier,
            startStepIndex = startStepIndex,
            saveProgressAcrossRecreation = false,
            onStepCompleted = { completedStepIndex ->
                completedStepIndex.toIntermediateCheckpointOrNull()?.let { checkpoint ->
                    onboardingRepository.recordStageCompleted(checkpoint)
                }
            },
            onTutorialCompleted = {
                coroutineScope.launch {
                    onboardingRepository.markTutorialCompleted()
                }
            },
            onSkipTutorialRequested = requestSkipConfirmation,
            onNavigateBack = requestSkipConfirmation
        )
    }

    if (isSkipConfirmationVisible) {
        SkipTutorialConfirmationDialog(
            onContinueTutorial = {
                isSkipConfirmationVisible = false
            },
            onSkipTutorial = {
                if (!isSkipInProgress) {
                    isSkipInProgress = true
                    isSkipConfirmationVisible = false
                    coroutineScope.launch {
                        onboardingRepository.markTutorialSkipped()
                    }
                }
            },
            onDismiss = {
                isSkipConfirmationVisible = false
            }
        )
    }
}

@Composable
private fun SkipTutorialConfirmationDialog(
    onContinueTutorial: () -> Unit,
    onSkipTutorial: () -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        modifier = Modifier.testTag(RequiredOnboardingTestTags.SKIP_CONFIRMATION_DIALOG),
        onDismissRequest = onDismiss,
        shape = NumPairsComponents.LargeShape,
        containerColor = NumPairsComponents.raisedSurfaceColor(),
        titleContentColor = MaterialTheme.colorScheme.onSurface,
        textContentColor = MaterialTheme.colorScheme.onSurfaceVariant,
        title = {
            Text(
                text = stringResource(R.string.onboarding_skip_tutorial_title),
                style = MaterialTheme.typography.titleMedium
            )
        },
        text = {
            Text(
                text = stringResource(R.string.onboarding_skip_tutorial_message),
                style = MaterialTheme.typography.bodyLarge
            )
        },
        confirmButton = {
            NumPairsComponents.PrimaryCtaButton(
                onClick = onContinueTutorial,
                modifier = Modifier.testTag(RequiredOnboardingTestTags.CONTINUE_TUTORIAL_BUTTON)
            ) {
                Text(
                    text = stringResource(R.string.onboarding_continue_tutorial_button),
                    style = MaterialTheme.typography.labelLarge
                )
            }
        },
        dismissButton = {
            OutlinedButton(
                onClick = onSkipTutorial,
                modifier = Modifier.testTag(RequiredOnboardingTestTags.SKIP_ANYWAY_BUTTON),
                shape = NumPairsComponents.MediumShape,
                colors = NumPairsComponents.secondaryButtonColors(),
                border = NumPairsComponents.secondaryButtonBorder()
            ) {
                Text(
                    text = stringResource(R.string.onboarding_skip_anyway_button),
                    style = MaterialTheme.typography.labelLarge
                )
            }
        },
        properties = DialogProperties(
            dismissOnBackPress = true,
            dismissOnClickOutside = true
        )
    )
}

internal fun OnboardingState.nextRequiredTutorialStepIndex(): Int = when (lastCompletedStage) {
    OnboardingStageCheckpoint.NONE -> 0
    OnboardingStageCheckpoint.STAGE_ONE -> 1
    OnboardingStageCheckpoint.STAGE_TWO -> 2
    OnboardingStageCheckpoint.STAGE_THREE -> 3
}

private fun Int.toIntermediateCheckpointOrNull(): OnboardingStageCheckpoint? = when (this) {
    0 -> OnboardingStageCheckpoint.STAGE_ONE
    1 -> OnboardingStageCheckpoint.STAGE_TWO
    else -> null
}

object RequiredOnboardingTestTags {
    const val SKIP_CONFIRMATION_DIALOG = "onboarding_skip_confirmation_dialog"
    const val CONTINUE_TUTORIAL_BUTTON = "onboarding_continue_tutorial_button"
    const val SKIP_ANYWAY_BUTTON = "onboarding_skip_anyway_button"
}
