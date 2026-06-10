package org.cescfe.numpairs.feature.tutorial

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import org.cescfe.numpairs.R
import org.cescfe.numpairs.feature.game.GameRoute
import org.cescfe.numpairs.feature.tutorial.ui.TutorialScreenTestTags

@Composable
fun TutorialRoute(modifier: Modifier = Modifier, onNavigateBack: () -> Unit = {}) {
    val steps = TutorialMvpContent.steps
    var currentStepIndex by rememberSaveable { mutableIntStateOf(0) }
    val currentStep = steps[currentStepIndex]
    val currentScenario = TutorialMvpContent.scenario(currentStep.scenarioId)

    GameRoute(
        title = stringResource(R.string.tutorial_screen_title),
        initialPuzzle = currentScenario.initialPuzzle,
        modifier = modifier,
        gameSessionKey = TUTORIAL_GAME_SESSION_KEY,
        puzzleResetKey = currentScenario.id,
        contentBeforePuzzle = {
            TutorialInstructionSurface(
                currentStep = currentStep,
                totalSteps = steps.size,
                canNavigateToPreviousStep = currentStepIndex > 0,
                canNavigateToNextStep = currentStepIndex < steps.lastIndex,
                onPreviousStepRequested = {
                    currentStepIndex = (currentStepIndex - 1).coerceAtLeast(0)
                },
                onNextStepRequested = {
                    currentStepIndex = (currentStepIndex + 1).coerceAtMost(steps.lastIndex)
                },
                modifier = Modifier.fillMaxWidth()
            )
        },
        onNavigateBack = onNavigateBack
    )
}

@Composable
private fun TutorialInstructionSurface(
    currentStep: TutorialStep,
    totalSteps: Int,
    canNavigateToPreviousStep: Boolean,
    canNavigateToNextStep: Boolean,
    onPreviousStepRequested: () -> Unit,
    onNextStepRequested: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier.testTag(TutorialScreenTestTags.INSTRUCTION_SURFACE),
        shape = MaterialTheme.shapes.medium,
        color = MaterialTheme.colorScheme.secondaryContainer,
        contentColor = MaterialTheme.colorScheme.onSecondaryContainer
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(
                text = stringResource(
                    R.string.tutorial_step_indicator,
                    currentStep.order,
                    totalSteps
                ),
                modifier = Modifier.testTag(TutorialScreenTestTags.STEP_INDICATOR),
                style = MaterialTheme.typography.labelLarge
            )
            Text(
                text = currentStep.playerFacingCopy,
                modifier = Modifier.testTag(TutorialScreenTestTags.STEP_COPY),
                style = MaterialTheme.typography.bodyLarge
            )
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                OutlinedButton(
                    onClick = onPreviousStepRequested,
                    enabled = canNavigateToPreviousStep,
                    modifier = Modifier
                        .weight(1f)
                        .testTag(TutorialScreenTestTags.PREVIOUS_STEP_BUTTON)
                ) {
                    Text(text = stringResource(R.string.tutorial_previous_step_button))
                }
                Button(
                    onClick = onNextStepRequested,
                    enabled = canNavigateToNextStep,
                    modifier = Modifier
                        .weight(1f)
                        .testTag(TutorialScreenTestTags.NEXT_STEP_BUTTON)
                ) {
                    Text(text = stringResource(R.string.tutorial_next_step_button))
                }
            }
        }
    }
}

private const val TUTORIAL_GAME_SESSION_KEY = "tutorial-walkthrough"
