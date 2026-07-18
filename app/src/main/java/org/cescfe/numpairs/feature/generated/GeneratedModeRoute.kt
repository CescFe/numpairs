package org.cescfe.numpairs.feature.generated

import android.content.Context
import android.content.ContextWrapper
import androidx.activity.ComponentActivity
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.ViewModelProvider
import org.cescfe.numpairs.R
import org.cescfe.numpairs.data.generated.session.GeneratedSessionId
import org.cescfe.numpairs.data.generated.session.GeneratedSessionRepository
import org.cescfe.numpairs.domain.puzzle.model.Puzzle
import org.cescfe.numpairs.feature.game.GameCompletionActions
import org.cescfe.numpairs.feature.game.GameRoute
import org.cescfe.numpairs.ui.theme.NumPairsComponents

@Composable
fun GeneratedModeRoute(
    challenge: GeneratedChallenge,
    launchIntent: GeneratedModeLaunchIntent = GeneratedModeLaunchIntent.DefaultNewPuzzle,
    title: String,
    generationUseCase: GeneratedPuzzleGenerationUseCase,
    generatedSessionRepository: GeneratedSessionRepository,
    modifier: Modifier = Modifier,
    isGeneratedGameHapticsEnabled: Boolean = true,
    isRulesHelperEnabled: Boolean = false,
    isRulesHelperActionDiscoveryDotVisible: Boolean = false,
    onRulesHelperActionTapped: () -> Unit = {},
    onRulesHelperPlayTutorialRequested: (() -> Unit)? = null,
    topBarActions: @Composable RowScope.() -> Unit = {},
    onNavigateBack: () -> Unit = {}
) {
    val viewModel = rememberGeneratedPuzzleViewModel(
        challenge = challenge,
        generationUseCase = generationUseCase,
        generatedSessionRepository = generatedSessionRepository
    )
    val uiState by viewModel.uiState.collectAsState()

    DisposableEffect(viewModel, launchIntent) {
        viewModel.onRouteEntered(launchIntent = launchIntent)
        onDispose(viewModel::onRouteExited)
    }

    val visibleSession = uiState.visibleSession()
    if (visibleSession != null) {
        GeneratedPuzzleGameBoundary(
            state = uiState,
            title = title,
            session = visibleSession,
            modifier = modifier,
            isRulesHelperEnabled = isRulesHelperEnabled,
            isRulesHelperActionDiscoveryDotVisible = isRulesHelperActionDiscoveryDotVisible,
            onRulesHelperActionTapped = onRulesHelperActionTapped,
            onRulesHelperPlayTutorialRequested = onRulesHelperPlayTutorialRequested,
            topBarActions = topBarActions,
            isGeneratedGameHapticsEnabled = isGeneratedGameHapticsEnabled,
            onNewPuzzleRequested = viewModel::onNewPuzzleRequested,
            onPuzzleChanged = viewModel::onPuzzleChanged,
            onReplacementTransitionConsumed = viewModel::onReplacementTransitionConsumed,
            onRetry = viewModel::retry,
            onNavigateBack = onNavigateBack
        )
        return
    }

    when (val state = uiState) {
        GeneratedPuzzleGenerationUiState.Idle -> Unit
        is GeneratedPuzzleGenerationUiState.Restoring,
        is GeneratedPuzzleGenerationUiState.Loading -> {
            GeneratedPuzzleInitialLoadingScreen(modifier = modifier)
        }

        is GeneratedPuzzleGenerationUiState.Failed -> {
            GeneratedPuzzleInitialFailureScreen(
                modifier = modifier,
                onRetry = viewModel::retry,
                onNavigateBack = onNavigateBack
            )
        }

        is GeneratedPuzzleGenerationUiState.Ready -> Unit
        is GeneratedPuzzleGenerationUiState.ResumeUnavailable -> {
            GeneratedSessionResumeUnavailableScreen(
                modifier = modifier,
                onNavigateBack = onNavigateBack
            )
        }
    }
}

@Composable
private fun GeneratedPuzzleGameBoundary(
    state: GeneratedPuzzleGenerationUiState,
    title: String,
    session: GeneratedModeGameSession,
    modifier: Modifier,
    isRulesHelperEnabled: Boolean,
    isRulesHelperActionDiscoveryDotVisible: Boolean,
    onRulesHelperActionTapped: () -> Unit,
    onRulesHelperPlayTutorialRequested: (() -> Unit)?,
    topBarActions: @Composable RowScope.() -> Unit,
    isGeneratedGameHapticsEnabled: Boolean,
    onNewPuzzleRequested: () -> Unit,
    onPuzzleChanged: (GeneratedSessionId, Puzzle) -> Unit,
    onReplacementTransitionConsumed: (GeneratedPuzzleReplacementTransition) -> Unit,
    onRetry: () -> Unit,
    onNavigateBack: () -> Unit
) {
    val replacementTransition = (state as? GeneratedPuzzleGenerationUiState.Ready)?.replacementTransition
    val entranceProgress = remember { Animatable(1f) }
    var presentedSessionId by remember { mutableStateOf(session.id) }
    var activeReplacementTransition by remember {
        mutableStateOf<GeneratedPuzzleReplacementTransition?>(null)
    }
    val currentSession by rememberUpdatedState(session)
    val transitionToStart = replacementTransition?.takeIf { transition ->
        transition.predecessorSessionId == presentedSessionId &&
            transition.successorSessionId == session.id &&
            activeReplacementTransition == null
    }
    val visibleTransition = activeReplacementTransition ?: transitionToStart
    val visibleProgress = if (transitionToStart != null && activeReplacementTransition == null) {
        0f
    } else {
        entranceProgress.value
    }

    LaunchedEffect(replacementTransition, session.id) {
        replacementTransition ?: return@LaunchedEffect
        if (transitionToStart != null) {
            entranceProgress.snapTo(0f)
            activeReplacementTransition = transitionToStart
            presentedSessionId = session.id
        }
        onReplacementTransitionConsumed(replacementTransition)
    }

    LaunchedEffect(activeReplacementTransition) {
        if (activeReplacementTransition != null) {
            entranceProgress.animateTo(
                targetValue = 1f,
                animationSpec = tween(
                    durationMillis = REPLACEMENT_TRANSITION_DURATION_MILLIS,
                    easing = FastOutSlowInEasing
                )
            )
            presentedSessionId = currentSession.id
            activeReplacementTransition = null
        }
    }

    LaunchedEffect(session) {
        if (activeReplacementTransition == null && replacementTransition == null) {
            presentedSessionId = session.id
            entranceProgress.snapTo(1f)
        }
    }

    GeneratedPuzzleGameContent(
        title = title,
        session = session,
        modifier = modifier
            .graphicsLayer {
                val scale = REPLACEMENT_TRANSITION_INITIAL_SCALE +
                    ((1f - REPLACEMENT_TRANSITION_INITIAL_SCALE) * visibleProgress)
                scaleX = scale
                scaleY = scale
                alpha = REPLACEMENT_TRANSITION_INITIAL_ALPHA +
                    ((1f - REPLACEMENT_TRANSITION_INITIAL_ALPHA) * visibleProgress)
            }
            .testTag(GENERATED_PUZZLE_CONTENT_TAG)
            .generatedReplacementTransitionSemantics(visibleTransition),
        isRulesHelperEnabled = isRulesHelperEnabled,
        isRulesHelperActionDiscoveryDotVisible = isRulesHelperActionDiscoveryDotVisible,
        onRulesHelperActionTapped = onRulesHelperActionTapped,
        onRulesHelperPlayTutorialRequested = onRulesHelperPlayTutorialRequested,
        topBarActions = topBarActions,
        isGeneratedGameHapticsEnabled = isGeneratedGameHapticsEnabled,
        onNewPuzzleRequested = onNewPuzzleRequested,
        onPuzzleChanged = onPuzzleChanged,
        onNavigateBack = onNavigateBack,
        overlay = {
            when (state) {
                is GeneratedPuzzleGenerationUiState.Loading -> GeneratedPuzzleLoadingOverlay()
                is GeneratedPuzzleGenerationUiState.Failed -> GeneratedPuzzleFailureDialog(
                    onRetry = onRetry,
                    onNavigateBack = onNavigateBack
                )

                GeneratedPuzzleGenerationUiState.Idle,
                is GeneratedPuzzleGenerationUiState.Restoring,
                is GeneratedPuzzleGenerationUiState.Ready,
                is GeneratedPuzzleGenerationUiState.ResumeUnavailable -> Unit
            }
        }
    )
}

@Composable
private fun GeneratedPuzzleGameContent(
    title: String,
    session: GeneratedModeGameSession,
    modifier: Modifier,
    isRulesHelperEnabled: Boolean,
    isRulesHelperActionDiscoveryDotVisible: Boolean,
    onRulesHelperActionTapped: () -> Unit,
    onRulesHelperPlayTutorialRequested: (() -> Unit)?,
    topBarActions: @Composable RowScope.() -> Unit,
    isGeneratedGameHapticsEnabled: Boolean,
    onNewPuzzleRequested: () -> Unit,
    onPuzzleChanged: (GeneratedSessionId, Puzzle) -> Unit,
    onNavigateBack: () -> Unit,
    overlay: @Composable () -> Unit = {}
) {
    val hapticFeedback = LocalHapticFeedback.current

    Box(modifier = modifier.fillMaxSize()) {
        GameRoute(
            title = title,
            initialPuzzle = session.currentPuzzle,
            gameSessionKey = session.request.profileId.value,
            puzzleResetKey = session.id,
            completionActions = GameCompletionActions(
                onNewPuzzleRequested = onNewPuzzleRequested,
                onReturnToMenuRequested = onNavigateBack
            ),
            isRulesHelperEnabled = isRulesHelperEnabled,
            isRulesHelperActionDiscoveryDotVisible = isRulesHelperActionDiscoveryDotVisible,
            onRulesHelperActionTapped = onRulesHelperActionTapped,
            onRulesHelperPlayTutorialRequested = onRulesHelperPlayTutorialRequested,
            isCorrectTileMotionEnabled = true,
            isCompletionCelebrationEnabled = true,
            topBarActions = topBarActions,
            onPuzzleChanged = { puzzle ->
                onPuzzleChanged(session.id, puzzle)
            },
            onTileAssignmentCommitted = {
                if (isGeneratedGameHapticsEnabled) {
                    hapticFeedback.performHapticFeedback(HapticFeedbackType.Confirm)
                }
            },
            onNavigateBack = onNavigateBack
        )
        overlay()
    }
}

private fun GeneratedPuzzleGenerationUiState.visibleSession(): GeneratedModeGameSession? = when (this) {
    is GeneratedPuzzleGenerationUiState.Ready -> session
    is GeneratedPuzzleGenerationUiState.Loading -> previousSession
    is GeneratedPuzzleGenerationUiState.Failed -> previousSession
    GeneratedPuzzleGenerationUiState.Idle,
    is GeneratedPuzzleGenerationUiState.Restoring,
    is GeneratedPuzzleGenerationUiState.ResumeUnavailable -> null
}

@Composable
private fun GeneratedPuzzleInitialLoadingScreen(modifier: Modifier) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .testTag(GENERATED_PUZZLE_LOADING_TAG),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        CircularProgressIndicator()
        GeneratedPuzzleLoadingMessage()
    }
}

@Composable
private fun GeneratedPuzzleLoadingOverlay() {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .testTag(GENERATED_PUZZLE_LOADING_TAG),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        CircularProgressIndicator()
        GeneratedPuzzleLoadingMessage()
    }
}

@Composable
private fun GeneratedPuzzleLoadingMessage() {
    Text(
        text = stringResource(R.string.generated_puzzle_loading_message),
        modifier = Modifier.padding(top = 16.dp),
        color = MaterialTheme.colorScheme.onBackground,
        style = MaterialTheme.typography.titleMedium
    )
}

@Composable
private fun GeneratedPuzzleInitialFailureScreen(modifier: Modifier, onRetry: () -> Unit, onNavigateBack: () -> Unit) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .testTag(GENERATED_PUZZLE_FAILURE_TAG),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = stringResource(R.string.generated_puzzle_failure_message),
            color = MaterialTheme.colorScheme.onBackground,
            style = MaterialTheme.typography.bodyLarge
        )
        Button(
            onClick = onRetry,
            modifier = Modifier.padding(top = 16.dp)
        ) {
            Text(
                text = stringResource(R.string.generated_puzzle_retry_button),
                style = MaterialTheme.typography.labelLarge
            )
        }
        Button(onClick = onNavigateBack) {
            Text(
                text = stringResource(R.string.generated_puzzle_back_to_menu_button),
                style = MaterialTheme.typography.labelLarge
            )
        }
    }
}

@Composable
private fun GeneratedSessionResumeUnavailableScreen(modifier: Modifier, onNavigateBack: () -> Unit) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .testTag(GENERATED_SESSION_RESUME_UNAVAILABLE_TAG),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = stringResource(R.string.generated_session_resume_unavailable_message),
            color = MaterialTheme.colorScheme.onBackground,
            style = MaterialTheme.typography.bodyLarge
        )
        Button(
            onClick = onNavigateBack,
            modifier = Modifier.padding(top = 16.dp)
        ) {
            Text(
                text = stringResource(R.string.generated_puzzle_back_to_menu_button),
                style = MaterialTheme.typography.labelLarge
            )
        }
    }
}

@Composable
private fun GeneratedPuzzleFailureDialog(onRetry: () -> Unit, onNavigateBack: () -> Unit) {
    AlertDialog(
        onDismissRequest = onNavigateBack,
        shape = NumPairsComponents.LargeShape,
        containerColor = NumPairsComponents.raisedSurfaceColor(),
        titleContentColor = MaterialTheme.colorScheme.onSurface,
        textContentColor = MaterialTheme.colorScheme.onSurfaceVariant,
        title = {
            Text(
                text = stringResource(R.string.generated_puzzle_failure_title),
                color = MaterialTheme.colorScheme.onSurface,
                style = MaterialTheme.typography.titleMedium
            )
        },
        text = {
            Text(
                text = stringResource(R.string.generated_puzzle_failure_message),
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                style = MaterialTheme.typography.bodyMedium
            )
        },
        confirmButton = {
            Button(onClick = onRetry) {
                Text(
                    text = stringResource(R.string.generated_puzzle_retry_button),
                    style = MaterialTheme.typography.labelLarge
                )
            }
        },
        dismissButton = {
            Button(onClick = onNavigateBack) {
                Text(
                    text = stringResource(R.string.generated_puzzle_back_to_menu_button),
                    style = MaterialTheme.typography.labelLarge
                )
            }
        }
    )
}

@Composable
private fun rememberGeneratedPuzzleViewModel(
    challenge: GeneratedChallenge,
    generationUseCase: GeneratedPuzzleGenerationUseCase,
    generatedSessionRepository: GeneratedSessionRepository
): GeneratedPuzzleViewModel {
    val activity = LocalContext.current.findComponentActivity()
        ?: error("GeneratedModeRoute requires a ComponentActivity host.")

    return remember(activity, challenge.id, generationUseCase, generatedSessionRepository) {
        ViewModelProvider(
            activity,
            GeneratedPuzzleViewModelFactory(
                challenge = challenge,
                generationUseCase = generationUseCase,
                generatedSessionRepository = generatedSessionRepository
            )
        )[challenge.generatedPuzzleViewModelKey(), GeneratedPuzzleViewModel::class.java]
    }
}

internal fun GeneratedChallenge.generatedPuzzleViewModelKey(): String = "generated-puzzle-${id.value}"

private class GeneratedPuzzleViewModelFactory(
    private val challenge: GeneratedChallenge,
    private val generationUseCase: GeneratedPuzzleGenerationUseCase,
    private val generatedSessionRepository: GeneratedSessionRepository
) : ViewModelProvider.Factory {
    override fun <T : androidx.lifecycle.ViewModel> create(modelClass: Class<T>): T {
        require(modelClass.isAssignableFrom(GeneratedPuzzleViewModel::class.java)) {
            "Unsupported ViewModel type ${modelClass.name}."
        }

        return requireNotNull(
            modelClass.cast(
                GeneratedPuzzleViewModel(
                    challenge = challenge,
                    generationUseCase = generationUseCase,
                    generatedSessionRepository = generatedSessionRepository
                )
            )
        )
    }
}

private tailrec fun Context.findComponentActivity(): ComponentActivity? = when (this) {
    is ComponentActivity -> this
    is ContextWrapper -> baseContext.findComponentActivity()
    else -> null
}

internal const val GENERATED_PUZZLE_LOADING_TAG = "generatedPuzzleLoading"
internal const val GENERATED_PUZZLE_FAILURE_TAG = "generatedPuzzleFailure"
internal const val GENERATED_SESSION_RESUME_UNAVAILABLE_TAG = "generatedSessionResumeUnavailable"
internal const val GENERATED_PUZZLE_CONTENT_TAG = "generatedPuzzleContent"
internal const val REPLACEMENT_TRANSITION_INITIAL_ALPHA = 0.82f
internal const val REPLACEMENT_TRANSITION_INITIAL_SCALE = 0.985f
internal const val REPLACEMENT_TRANSITION_DURATION_MILLIS = 260
