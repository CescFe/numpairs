package org.cescfe.numpairs.feature.onboarding

import androidx.activity.compose.BackHandler
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import kotlinx.coroutines.launch
import org.cescfe.numpairs.data.onboarding.OnboardingRepository
import org.cescfe.numpairs.data.onboarding.OnboardingStageCheckpoint
import org.cescfe.numpairs.data.onboarding.OnboardingState
import org.cescfe.numpairs.feature.tutorial.FinalValidationRoute
import org.cescfe.numpairs.feature.tutorial.GuidedIntroductionRoute
import org.cescfe.numpairs.feature.tutorial.GuidedOnboardingStage

@Composable
fun RequiredOnboardingRoute(
    onboardingState: OnboardingState,
    onboardingRepository: OnboardingRepository,
    modifier: Modifier = Modifier
) {
    val coroutineScope = rememberCoroutineScope()
    val nextGuidedStage = onboardingState.lastCompletedStage.nextRequiredGuidedStage()

    BackHandler(enabled = true, onBack = {})

    if (nextGuidedStage != null) {
        GuidedIntroductionRoute(
            modifier = modifier,
            startStage = nextGuidedStage,
            saveProgressAcrossRecreation = false,
            onStageCompleted = { stage ->
                onboardingRepository.recordStageCompleted(stage.toCheckpoint())
            },
            onIntroductionCompleted = {
                coroutineScope.launch {
                    onboardingRepository.markRequiredVersionCompleted()
                }
            }
        )
    } else {
        FinalValidationRoute(
            modifier = modifier,
            onValidationSolved = {
                coroutineScope.launch {
                    onboardingRepository.markRequiredVersionCompleted()
                }
            },
            onNavigateBack = {},
            onReturnToMenuRequested = {}
        )
    }
}

internal fun OnboardingStageCheckpoint.nextRequiredGuidedStage(): GuidedOnboardingStage? = when (this) {
    OnboardingStageCheckpoint.NONE -> GuidedOnboardingStage.NUMBER_PLACEMENT
    OnboardingStageCheckpoint.STAGE_ONE -> GuidedOnboardingStage.COMPLEMENTARY_PAIR
    OnboardingStageCheckpoint.STAGE_TWO -> GuidedOnboardingStage.HIDDEN_STRIP_VALUE
    OnboardingStageCheckpoint.STAGE_THREE -> null
}

private fun GuidedOnboardingStage.toCheckpoint(): OnboardingStageCheckpoint = when (this) {
    GuidedOnboardingStage.NUMBER_PLACEMENT -> OnboardingStageCheckpoint.STAGE_ONE
    GuidedOnboardingStage.COMPLEMENTARY_PAIR -> OnboardingStageCheckpoint.STAGE_TWO
    GuidedOnboardingStage.HIDDEN_STRIP_VALUE -> OnboardingStageCheckpoint.STAGE_THREE
}
