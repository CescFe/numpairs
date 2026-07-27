package org.cescfe.numpairs.feature.generated

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import kotlinx.coroutines.launch
import org.cescfe.numpairs.data.generated.session.GeneratedSessionRepository
import org.cescfe.numpairs.data.preferences.TopAppBarActionDiscoveryRepository
import org.cescfe.numpairs.data.preferences.TopAppBarActionDiscoveryState
import org.cescfe.numpairs.feature.game.ui.actions.HintAction
import org.cescfe.numpairs.feature.game.ui.help.SolvingTipsDialog
import org.cescfe.numpairs.feature.tutorial.TutorialMode
import org.cescfe.numpairs.feature.tutorial.TutorialOverlayHost

@Composable
fun GeneratedLearningRoute(
    challenge: GeneratedChallenge,
    title: String,
    topAppBarActionDiscoveryRepository: TopAppBarActionDiscoveryRepository,
    generatedSessionRepository: GeneratedSessionRepository,
    modifier: Modifier = Modifier,
    generationUseCase: GeneratedPuzzleGenerationUseCase,
    launchIntent: GeneratedModeLaunchIntent = GeneratedModeLaunchIntent.DefaultNewPuzzle,
    isGeneratedGameHapticsEnabled: Boolean = true,
    tutorialOverlayMode: TutorialMode? = null,
    onTutorialOverlayClosed: () -> Unit = {},
    newPuzzleChallengeProvider: (() -> GeneratedChallenge)? = null,
    replacementGenerationUseCaseFactory: GeneratedPuzzleGenerationUseCaseFactory? = null,
    onNavigateBack: () -> Unit = {}
) {
    val actionDiscoveryState: TopAppBarActionDiscoveryState? by topAppBarActionDiscoveryRepository.discoveryState
        .collectAsState(initial = null)
    val coroutineScope = rememberCoroutineScope()
    var requestedTutorialOverlayMode by rememberSaveable {
        mutableStateOf<TutorialMode?>(null)
    }
    var isSolvingTipsDialogVisible by rememberSaveable {
        mutableStateOf(false)
    }
    val activeTutorialOverlayMode = tutorialOverlayMode ?: requestedTutorialOverlayMode

    TutorialOverlayHost(
        tutorialMode = activeTutorialOverlayMode,
        onTutorialClosed = {
            requestedTutorialOverlayMode = null
            onTutorialOverlayClosed()
        },
        modifier = modifier
    ) {
        GeneratedModeRoute(
            challenge = challenge,
            launchIntent = launchIntent,
            title = title,
            generationUseCase = generationUseCase,
            generatedSessionRepository = generatedSessionRepository,
            isGeneratedGameHapticsEnabled = isGeneratedGameHapticsEnabled,
            isRulesHelperEnabled = true,
            isRulesHelperActionDiscoveryDotVisible = actionDiscoveryState?.hasSeenHelpAction == false,
            onRulesHelperActionTapped = {
                if (actionDiscoveryState?.hasSeenHelpAction != true) {
                    coroutineScope.launch {
                        topAppBarActionDiscoveryRepository.markHelpActionSeen()
                    }
                }
            },
            onRulesHelperPlayTutorialRequested = {
                requestedTutorialOverlayMode = TutorialMode.LEARN_BASICS
            },
            topBarActions = {
                HintAction(
                    isDiscoveryDotVisible = actionDiscoveryState?.hasSeenHintAction == false,
                    onClick = {
                        if (actionDiscoveryState?.hasSeenHintAction != true) {
                            coroutineScope.launch {
                                topAppBarActionDiscoveryRepository.markHintActionSeen()
                            }
                        }
                        isSolvingTipsDialogVisible = true
                    }
                )
            },
            newPuzzleChallengeProvider = newPuzzleChallengeProvider,
            replacementGenerationUseCaseFactory = replacementGenerationUseCaseFactory,
            onNavigateBack = onNavigateBack
        )
        if (isSolvingTipsDialogVisible) {
            SolvingTipsDialog(
                onDismiss = {
                    isSolvingTipsDialogVisible = false
                },
                onPracticeTipsRequested = {
                    requestedTutorialOverlayMode = TutorialMode.SOLVING_TIPS_PRACTICE
                }
            )
        }
    }
}
