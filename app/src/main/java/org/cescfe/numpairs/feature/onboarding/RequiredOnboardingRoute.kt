package org.cescfe.numpairs.feature.onboarding

import androidx.activity.compose.BackHandler
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import kotlinx.coroutines.launch
import org.cescfe.numpairs.data.onboarding.OnboardingPostCorePath
import org.cescfe.numpairs.data.onboarding.OnboardingRepository
import org.cescfe.numpairs.data.onboarding.OnboardingStageCheckpoint
import org.cescfe.numpairs.data.onboarding.OnboardingState
import org.cescfe.numpairs.feature.tutorial.FinalValidationRoute
import org.cescfe.numpairs.feature.tutorial.GuidedIntroductionRoute
import org.cescfe.numpairs.feature.tutorial.GuidedOnboardingStage
import org.cescfe.numpairs.feature.tutorial.PostCoreChoiceScreen

@Composable
fun RequiredOnboardingRoute(
    onboardingState: OnboardingState,
    onboardingRepository: OnboardingRepository,
    modifier: Modifier = Modifier
) {
    val coroutineScope = rememberCoroutineScope()
    val requiredStep = onboardingState.nextRequiredStep()

    BackHandler(enabled = true, onBack = {})

    when (requiredStep) {
        is RequiredOnboardingStep.GuidedStage -> GuidedIntroductionRoute(
            modifier = modifier,
            startStage = requiredStep.stage,
            saveProgressAcrossRecreation = false,
            showPostCoreChoice = false,
            onStageCompleted = { stage ->
                onboardingRepository.recordStageCompleted(stage.toCheckpoint())
            },
            onIntroductionCompleted = {
                coroutineScope.launch {
                    onboardingRepository.markTutorialCompleted()
                }
            }
        )
        RequiredOnboardingStep.PostCoreChoice -> PostCoreChoiceScreen(
            modifier = modifier,
            onContinueGuided = {
                coroutineScope.launch {
                    onboardingRepository.selectPostCorePath(OnboardingPostCorePath.CONTINUE_GUIDED)
                }
            },
            onStartValidation = {
                coroutineScope.launch {
                    onboardingRepository.selectPostCorePath(OnboardingPostCorePath.EARLY_VALIDATION)
                }
            }
        )
        RequiredOnboardingStep.FinalValidation -> FinalValidationRoute(
            modifier = modifier,
            onValidationSolved = {
                coroutineScope.launch {
                    onboardingRepository.markTutorialCompleted()
                }
            },
            onNavigateBack = {},
            onReturnToMenuRequested = {}
        )
    }
}

internal fun OnboardingState.nextRequiredStep(): RequiredOnboardingStep = when (lastCompletedStage) {
    OnboardingStageCheckpoint.NONE -> RequiredOnboardingStep.GuidedStage(GuidedOnboardingStage.NUMBER_PLACEMENT)
    OnboardingStageCheckpoint.STAGE_ONE ->
        RequiredOnboardingStep.GuidedStage(GuidedOnboardingStage.COMPLEMENTARY_PAIR)
    OnboardingStageCheckpoint.STAGE_TWO -> when (postCorePath) {
        OnboardingPostCorePath.UNDECIDED -> RequiredOnboardingStep.PostCoreChoice
        OnboardingPostCorePath.CONTINUE_GUIDED ->
            RequiredOnboardingStep.GuidedStage(GuidedOnboardingStage.HIDDEN_STRIP_VALUE)
        OnboardingPostCorePath.EARLY_VALIDATION -> RequiredOnboardingStep.FinalValidation
    }
    OnboardingStageCheckpoint.STAGE_THREE -> RequiredOnboardingStep.FinalValidation
}

internal sealed interface RequiredOnboardingStep {
    data class GuidedStage(val stage: GuidedOnboardingStage) : RequiredOnboardingStep

    data object PostCoreChoice : RequiredOnboardingStep

    data object FinalValidation : RequiredOnboardingStep
}

private fun GuidedOnboardingStage.toCheckpoint(): OnboardingStageCheckpoint = when (this) {
    GuidedOnboardingStage.NUMBER_PLACEMENT -> OnboardingStageCheckpoint.STAGE_ONE
    GuidedOnboardingStage.COMPLEMENTARY_PAIR -> OnboardingStageCheckpoint.STAGE_TWO
    GuidedOnboardingStage.HIDDEN_STRIP_VALUE -> OnboardingStageCheckpoint.STAGE_THREE
}
