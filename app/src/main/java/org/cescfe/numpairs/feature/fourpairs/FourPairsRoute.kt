package org.cescfe.numpairs.feature.fourpairs

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import kotlinx.coroutines.launch
import org.cescfe.numpairs.R
import org.cescfe.numpairs.data.preferences.TopAppBarActionDiscoveryRepository
import org.cescfe.numpairs.data.preferences.TopAppBarActionDiscoveryState
import org.cescfe.numpairs.domain.puzzle.model.Puzzle
import org.cescfe.numpairs.feature.game.GameCompletionActions
import org.cescfe.numpairs.feature.game.GameRoute
import org.cescfe.numpairs.feature.game.ui.actions.HintAction
import org.cescfe.numpairs.feature.game.ui.help.SolvingTipsDialog
import org.cescfe.numpairs.feature.tutorial.TutorialMode
import org.cescfe.numpairs.feature.tutorial.TutorialOverlayHost

@Composable
fun FourPairsRoute(
    topAppBarActionDiscoveryRepository: TopAppBarActionDiscoveryRepository,
    modifier: Modifier = Modifier,
    puzzleProvider: FourPairsPuzzleProvider = DefaultFourPairsPuzzleProvider,
    tutorialOverlayMode: TutorialMode? = null,
    onTutorialOverlayClosed: () -> Unit = {},
    onNavigateBack: () -> Unit = {}
) {
    val actionDiscoveryState: TopAppBarActionDiscoveryState? by topAppBarActionDiscoveryRepository.discoveryState
        .collectAsState(initial = null)
    val coroutineScope = rememberCoroutineScope()
    val gameSessionFactory = remember(puzzleProvider) {
        FourPairsGameSessionFactory(puzzleProvider = puzzleProvider)
    }
    var gameSession by remember(gameSessionFactory) {
        mutableStateOf(gameSessionFactory.create())
    }
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
        GameRoute(
            title = stringResource(R.string.four_pairs_screen_title),
            initialPuzzle = gameSession.initialPuzzle,
            gameSessionKey = FOUR_PAIRS_GAME_SESSION_KEY,
            puzzleResetKey = gameSession.id,
            completionActions = GameCompletionActions(
                onNewPuzzleRequested = {
                    gameSession = gameSessionFactory.create()
                },
                onReturnToMenuRequested = onNavigateBack
            ),
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

private data class GeneratedFourPairsGameSession(val id: Int, val initialPuzzle: Puzzle)

private class FourPairsGameSessionFactory(private val puzzleProvider: FourPairsPuzzleProvider) {
    private var nextGameSessionId = 0

    fun create(): GeneratedFourPairsGameSession = GeneratedFourPairsGameSession(
        id = nextGameSessionId++,
        initialPuzzle = puzzleProvider.nextPuzzle()
    )
}

private const val FOUR_PAIRS_GAME_SESSION_KEY = "four-pairs"
