package org.cescfe.numpairs.feature.fourpairs

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import kotlinx.coroutines.launch
import org.cescfe.numpairs.R
import org.cescfe.numpairs.data.preferences.TopAppBarActionDiscoveryRepository
import org.cescfe.numpairs.data.preferences.TopAppBarActionDiscoveryState
import org.cescfe.numpairs.feature.game.ui.actions.HintAction
import org.cescfe.numpairs.feature.game.ui.help.SolvingTipsDialog
import org.cescfe.numpairs.feature.generated.GeneratedModeConfiguration
import org.cescfe.numpairs.feature.generated.GeneratedModeRoute
import org.cescfe.numpairs.feature.generated.GeneratedModes
import org.cescfe.numpairs.feature.generated.GeneratedPuzzleProvider
import org.cescfe.numpairs.feature.tutorial.TutorialMode
import org.cescfe.numpairs.feature.tutorial.TutorialOverlayHost

@Composable
fun FourPairsRoute(
    topAppBarActionDiscoveryRepository: TopAppBarActionDiscoveryRepository,
    modifier: Modifier = Modifier,
    puzzleProvider: GeneratedPuzzleProvider,
    mode: GeneratedModeConfiguration = GeneratedModes.FOUR_PAIRS,
    tutorialOverlayMode: TutorialMode? = null,
    onTutorialOverlayClosed: () -> Unit = {},
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
            mode = mode,
            title = stringResource(R.string.four_pairs_screen_title),
            puzzleProvider = puzzleProvider,
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
