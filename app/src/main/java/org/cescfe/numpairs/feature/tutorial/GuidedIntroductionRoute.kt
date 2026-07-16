package org.cescfe.numpairs.feature.tutorial

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier

@Composable
fun GuidedIntroductionRoute(
    modifier: Modifier = Modifier,
    startStage: GuidedOnboardingStage = GuidedOnboardingStage.NUMBER_PLACEMENT,
    onStageCompleted: (GuidedOnboardingStage) -> Unit = {},
    onIntroductionCompleted: () -> Unit = {},
    onNavigateBack: () -> Unit = {}
) {
    var phase by rememberSaveable(startStage) { mutableStateOf(GuidedIntroductionPhase.from(startStage)) }
    val currentOnStageCompleted by rememberUpdatedState(onStageCompleted)
    val currentOnIntroductionCompleted by rememberUpdatedState(onIntroductionCompleted)

    phase.guidedStage?.let { stage ->
        TutorialRoute(
            modifier = modifier,
            mode = TutorialMode.LEARN_BASICS,
            guidedStage = stage,
            onTutorialCompleted = {
                currentOnStageCompleted(stage)
                phase = stage.nextOrNull()?.let(GuidedIntroductionPhase::from)
                    ?: GuidedIntroductionPhase.FINAL_VALIDATION
            },
            onNavigateBack = onNavigateBack
        )
    } ?: FinalValidationRoute(
        modifier = modifier,
        onValidationSolved = currentOnIntroductionCompleted,
        onNavigateBack = onNavigateBack,
        onReturnToMenuRequested = onNavigateBack
    )
}

private enum class GuidedIntroductionPhase(val guidedStage: GuidedOnboardingStage?) {
    NUMBER_PLACEMENT(GuidedOnboardingStage.NUMBER_PLACEMENT),
    COMPLEMENTARY_PAIR(GuidedOnboardingStage.COMPLEMENTARY_PAIR),
    HIDDEN_STRIP_VALUE(GuidedOnboardingStage.HIDDEN_STRIP_VALUE),
    FINAL_VALIDATION(null);

    companion object {
        fun from(stage: GuidedOnboardingStage): GuidedIntroductionPhase = entries
            .first { phase -> phase.guidedStage == stage }
    }
}
