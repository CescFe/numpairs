package org.cescfe.numpairs.feature.tutorial

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import kotlinx.coroutines.launch

@Composable
fun GuidedIntroductionRoute(
    modifier: Modifier = Modifier,
    startStage: GuidedOnboardingStage = GuidedOnboardingStage.NUMBER_PLACEMENT,
    saveProgressAcrossRecreation: Boolean = true,
    showPostCoreChoice: Boolean = true,
    onStageCompleted: suspend (GuidedOnboardingStage) -> Unit = {},
    onIntroductionCompleted: () -> Unit = {},
    onNavigateBack: () -> Unit = {}
) {
    val phaseState = if (saveProgressAcrossRecreation) {
        rememberSaveable(startStage) { mutableStateOf(GuidedIntroductionPhase.from(startStage)) }
    } else {
        remember(startStage) { mutableStateOf(GuidedIntroductionPhase.from(startStage)) }
    }
    var phase by phaseState
    val currentOnStageCompleted by rememberUpdatedState(onStageCompleted)
    val currentOnIntroductionCompleted by rememberUpdatedState(onIntroductionCompleted)
    val coroutineScope = rememberCoroutineScope()

    when (phase) {
        GuidedIntroductionPhase.POST_CORE_CHOICE -> PostCoreChoiceScreen(
            modifier = modifier,
            onContinueGuided = {
                phase = GuidedIntroductionPhase.HIDDEN_STRIP_VALUE
            },
            onStartValidation = {
                phase = GuidedIntroductionPhase.FINAL_VALIDATION
            },
            onNavigateBack = onNavigateBack
        )
        GuidedIntroductionPhase.FINAL_VALIDATION -> FinalValidationRoute(
            modifier = modifier,
            onValidationSolved = currentOnIntroductionCompleted,
            onNavigateBack = onNavigateBack,
            onReturnToMenuRequested = onNavigateBack
        )
        else -> phase.guidedStage?.let { stage ->
            var isStageCompletionInProgress by remember(stage) { mutableStateOf(false) }

            TutorialRoute(
                modifier = modifier,
                mode = TutorialMode.LEARN_BASICS,
                guidedStage = stage,
                saveProgressAcrossRecreation = saveProgressAcrossRecreation,
                onTutorialCompleted = {
                    if (!isStageCompletionInProgress) {
                        isStageCompletionInProgress = true
                        coroutineScope.launch {
                            currentOnStageCompleted(stage)
                            phase = if (
                                stage == GuidedOnboardingStage.COMPLEMENTARY_PAIR &&
                                showPostCoreChoice
                            ) {
                                GuidedIntroductionPhase.POST_CORE_CHOICE
                            } else {
                                stage.nextOrNull()?.let(GuidedIntroductionPhase::from)
                                    ?: GuidedIntroductionPhase.FINAL_VALIDATION
                            }
                        }
                    }
                },
                onNavigateBack = onNavigateBack
            )
        }
    }
}

private enum class GuidedIntroductionPhase(val guidedStage: GuidedOnboardingStage?) {
    NUMBER_PLACEMENT(GuidedOnboardingStage.NUMBER_PLACEMENT),
    COMPLEMENTARY_PAIR(GuidedOnboardingStage.COMPLEMENTARY_PAIR),
    POST_CORE_CHOICE(null),
    HIDDEN_STRIP_VALUE(GuidedOnboardingStage.HIDDEN_STRIP_VALUE),
    FINAL_VALIDATION(null);

    companion object {
        fun from(stage: GuidedOnboardingStage): GuidedIntroductionPhase = entries
            .first { phase -> phase.guidedStage == stage }
    }
}
