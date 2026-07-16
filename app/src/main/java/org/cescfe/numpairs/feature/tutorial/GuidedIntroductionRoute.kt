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
    onIntroductionCompleted: () -> Unit = {},
    onNavigateBack: () -> Unit = {}
) {
    var phase by rememberSaveable { mutableStateOf(GuidedIntroductionPhase.GUIDED_STAGES) }
    val currentOnIntroductionCompleted by rememberUpdatedState(onIntroductionCompleted)

    when (phase) {
        GuidedIntroductionPhase.GUIDED_STAGES -> TutorialRoute(
            modifier = modifier,
            mode = TutorialMode.LEARN_BASICS,
            onTutorialCompleted = {
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
    }
}

private enum class GuidedIntroductionPhase {
    GUIDED_STAGES,
    FINAL_VALIDATION
}
