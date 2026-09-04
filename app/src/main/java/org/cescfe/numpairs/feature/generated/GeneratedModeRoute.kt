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
import kotlin.time.Duration.Companion.milliseconds
import kotlinx.coroutines.currentCoroutineContext
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import org.cescfe.numpairs.R
import org.cescfe.numpairs.data.generated.session.GeneratedSessionId
import org.cescfe.numpairs.data.generated.session.GeneratedSessionRepository
import org.cescfe.numpairs.domain.generated.GeneratedElapsedTime
import org.cescfe.numpairs.domain.generated.GeneratedPersonalBestOutcome
import org.cescfe.numpairs.domain.generated.GeneratedPersonalBestResult
import org.cescfe.numpairs.feature.game.GameCompletionActions
import org.cescfe.numpairs.feature.game.GameRoute
import org.cescfe.numpairs.feature.game.GameSuccessOverlayVisualStyle
import org.cescfe.numpairs.feature.game.StandardCompletionCelebrationContext
import org.cescfe.numpairs.feature.game.StandardCompletionCelebrationSelector
import org.cescfe.numpairs.feature.game.localizedCopy
import org.cescfe.numpairs.feature.game.presentation.CommittedPuzzleMutation
import org.cescfe.numpairs.feature.time.ElapsedTimeSource
import org.cescfe.numpairs.feature.time.SystemElapsedTimeSource
import org.cescfe.numpairs.ui.theme.NumPairsComponents

@Composable
fun GeneratedModeRoute(
    challenge: GeneratedChallenge,
    title: String,
    generationUseCase: GeneratedPuzzleGenerationUseCase,
    generatedSessionRepository: GeneratedSessionRepository,
    modifier: Modifier = Modifier,
    launchIntent: GeneratedModeLaunchIntent = GeneratedModeLaunchIntent.DefaultNewPuzzle,
    isGeneratedGameHapticsEnabled: Boolean = true,
    compactTileSelectorsEnabled: Boolean = false,
    isChronometerExpanded: Boolean = true,
    onChronometerExpandedChange: (Boolean) -> Unit = {},
    timeSource: ElapsedTimeSource = SystemElapsedTimeSource,
    isRulesHelperEnabled: Boolean = false,
    isRulesHelperActionDiscoveryDotVisible: Boolean = false,
    onRulesHelperActionTapped: () -> Unit = {},
    onRulesHelperPlayTutorialRequested: (() -> Unit)? = null,
    topBarActions: @Composable RowScope.() -> Unit = {},
    newPuzzleChallengeProvider: (() -> GeneratedChallenge)? = null,
    replacementGenerationUseCaseFactory: GeneratedPuzzleGenerationUseCaseFactory? = null,
    onNavigateBack: () -> Unit = {}
) {
    require((newPuzzleChallengeProvider == null) == (replacementGenerationUseCaseFactory == null)) {
        "A replacement challenge provider and generation factory must be supplied together."
    }
    val viewModel = rememberGeneratedPuzzleViewModel(
        challenge = challenge,
        generationUseCase = generationUseCase,
        generatedSessionRepository = generatedSessionRepository,
        timeSource = timeSource
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
            compactTileSelectorsEnabled = compactTileSelectorsEnabled,
            isChronometerExpanded = isChronometerExpanded,
            onChronometerExpandedChange = onChronometerExpandedChange,
            onNewPuzzleRequested = {
                if (newPuzzleChallengeProvider == null) {
                    viewModel.onNewPuzzleRequested()
                } else {
                    viewModel.onNewPuzzleRequested {
                        val replacementChallenge = newPuzzleChallengeProvider()
                        GeneratedPuzzleGenerationDefinition(
                            challenge = replacementChallenge,
                            generationUseCase = requireNotNull(replacementGenerationUseCaseFactory)
                                .create(replacementChallenge)
                        )
                    }
                }
            },
            onPuzzleMutationCommitted = viewModel::onPuzzleMutationCommitted,
            onPuzzlePresented = viewModel::onPuzzlePresented,
            onTimerRefresh = viewModel::onTimerRefresh,
            onReplacementTransitionConsumed = viewModel::onReplacementTransitionConsumed,
            onRetryPersistence = viewModel::retryPersistence,
            onRetry = viewModel::retry,
            onNavigateBack = onNavigateBack
        )
        return
    }

    when (uiState) {
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
    compactTileSelectorsEnabled: Boolean,
    isChronometerExpanded: Boolean,
    onChronometerExpandedChange: (Boolean) -> Unit,
    onNewPuzzleRequested: () -> Unit,
    onPuzzleMutationCommitted: (GeneratedSessionId, CommittedPuzzleMutation) -> Unit,
    onPuzzlePresented: (GeneratedSessionId) -> Unit,
    onTimerRefresh: (GeneratedSessionId) -> Unit,
    onReplacementTransitionConsumed: (GeneratedPuzzleReplacementTransition) -> Unit,
    onRetryPersistence: () -> Unit,
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
    val visibleProgress = if (transitionToStart != null) {
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
        compactTileSelectorsEnabled = compactTileSelectorsEnabled,
        elapsedTime = (state as? GeneratedPuzzleGenerationUiState.Ready)?.elapsedTime
            ?: session.snapshot.completionElapsedTime,
        personalBestResult = state.visiblePersonalBestResult(),
        isChronometerExpanded = isChronometerExpanded,
        onChronometerExpandedChange = onChronometerExpandedChange,
        onNewPuzzleRequested = onNewPuzzleRequested,
        onPuzzleMutationCommitted = onPuzzleMutationCommitted,
        onPuzzlePresented = onPuzzlePresented,
        onTimerRefresh = onTimerRefresh,
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
    if ((state as? GeneratedPuzzleGenerationUiState.Ready)?.hasPersistenceFailure == true) {
        GeneratedPersistenceFailureDialog(
            onRetry = onRetryPersistence,
            onNavigateBack = onNavigateBack
        )
    }
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
    compactTileSelectorsEnabled: Boolean,
    elapsedTime: GeneratedElapsedTime?,
    personalBestResult: GeneratedPersonalBestResult?,
    isChronometerExpanded: Boolean,
    onChronometerExpandedChange: (Boolean) -> Unit,
    onNewPuzzleRequested: () -> Unit,
    onPuzzleMutationCommitted: (GeneratedSessionId, CommittedPuzzleMutation) -> Unit,
    onPuzzlePresented: (GeneratedSessionId) -> Unit,
    onTimerRefresh: (GeneratedSessionId) -> Unit,
    onNavigateBack: () -> Unit,
    overlay: @Composable () -> Unit = {}
) {
    val hapticFeedback = LocalHapticFeedback.current
    val formattedElapsedTime = session.snapshot.completionElapsedTime
        ?.let(GeneratedElapsedTimeFormatter::format)
    val formattedBestTime = personalBestResult
        ?.takeIf { result -> result.currentElapsedTime != null }
        ?.bestElapsedTime
        ?.let(GeneratedElapsedTimeFormatter::format)
    val standardCompletionCelebrationCopy = StandardCompletionCelebrationSelector.select(
        StandardCompletionCelebrationContext(
            generatedChallengeId = session.challenge.id.value,
            completionId = session.id.value,
            difficulty = session.challenge.difficulty,
            correctionCount = session.snapshot.correctionCount
        )
    ).localizedCopy().copy(
        highlightText = formattedElapsedTime,
        highlightContentDescription = formattedElapsedTime?.let { formatted ->
            stringResource(R.string.generated_elapsed_time_content_description, formatted)
        },
        contextText = formattedBestTime?.let { formatted ->
            stringResource(R.string.generated_personal_best, formatted)
        },
        contextContentDescription = formattedBestTime?.let { formatted ->
            stringResource(R.string.generated_personal_best_content_description, formatted)
        }
    )
    val completionCelebrationCopy = if (
        personalBestResult?.outcome == GeneratedPersonalBestOutcome.PERSONAL_RECORD
    ) {
        require(personalBestResult.currentElapsedTime == session.snapshot.completionElapsedTime) {
            "Generated personal-record presentation must use the frozen completion duration."
        }
        require(personalBestResult.category?.generatedChallengeId == session.challenge.id.value) {
            "Generated personal-record presentation must use the completed challenge category."
        }
        val currentTime = GeneratedElapsedTimeFormatter.format(
            requireNotNull(personalBestResult.currentElapsedTime)
        )
        val previousBest = GeneratedElapsedTimeFormatter.format(
            requireNotNull(personalBestResult.previousBestElapsedTime)
        )
        val category = session.challenge.localizedPersonalRecordCategory()
        standardCompletionCelebrationCopy.copy(
            message = stringResource(R.string.generated_personal_record_message),
            supportingText = stringResource(R.string.generated_personal_record_supporting_text),
            highlightText = currentTime,
            highlightContentDescription = stringResource(
                R.string.generated_personal_record_current_time_content_description,
                currentTime
            ),
            contextText = stringResource(
                R.string.generated_personal_record_context,
                category,
                previousBest
            ),
            contextContentDescription = stringResource(
                R.string.generated_personal_record_context_content_description,
                category,
                previousBest
            ),
            visualStyle = GameSuccessOverlayVisualStyle.PERSONAL_RECORD,
            badgeContentDescription = stringResource(
                R.string.generated_personal_record_badge_content_description
            )
        )
    } else {
        standardCompletionCelebrationCopy
    }

    LaunchedEffect(session.id, session.currentPuzzle.isSolved) {
        if (session.currentPuzzle.isSolved) {
            return@LaunchedEffect
        }
        onPuzzlePresented(session.id)
        while (currentCoroutineContext().isActive) {
            delay(GENERATED_TIMER_REFRESH_INTERVAL_MILLISECONDS.milliseconds)
            onTimerRefresh(session.id)
        }
    }

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
            successOverlayCopy = completionCelebrationCopy,
            compactTileSelectorsEnabled = compactTileSelectorsEnabled,
            topBarActions = {
                GeneratedChronometer(
                    elapsedTime = elapsedTime ?: ZERO_GENERATED_ELAPSED_TIME,
                    isExpanded = isChronometerExpanded,
                    onExpandedChange = onChronometerExpandedChange
                )
                topBarActions()
            },
            onPuzzleMutationCommitted = { mutation ->
                onPuzzleMutationCommitted(session.id, mutation)
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

private fun GeneratedPuzzleGenerationUiState.visiblePersonalBestResult(): GeneratedPersonalBestResult? = when (this) {
    is GeneratedPuzzleGenerationUiState.Ready -> personalBestResult

    is GeneratedPuzzleGenerationUiState.Loading -> previousPersonalBestResult

    is GeneratedPuzzleGenerationUiState.Failed -> previousPersonalBestResult

    GeneratedPuzzleGenerationUiState.Idle,
    is GeneratedPuzzleGenerationUiState.Restoring,
    is GeneratedPuzzleGenerationUiState.ResumeUnavailable -> null
}

@Composable
private fun GeneratedChallenge.localizedPersonalRecordCategory(): String {
    val size = stringResource(
        when (modeId) {
            GeneratedModes.THREE_PAIRS.id -> R.string.three_pairs_screen_title
            GeneratedModes.FOUR_PAIRS.id -> R.string.four_pairs_screen_title
            GeneratedModes.EIGHT_PAIRS.id -> R.string.eight_pairs_screen_title
            else -> error("No personal-record category is configured for mode ${modeId.value}.")
        }
    )
    return stringResource(R.string.generated_challenge_title, size, difficulty.localizedTitle())
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
private fun GeneratedPersistenceFailureDialog(onRetry: () -> Unit, onNavigateBack: () -> Unit) {
    AlertDialog(
        onDismissRequest = onNavigateBack,
        shape = NumPairsComponents.LargeShape,
        containerColor = NumPairsComponents.raisedSurfaceColor(),
        title = {
            Text(text = stringResource(R.string.generated_progress_failure_title))
        },
        text = {
            Text(text = stringResource(R.string.generated_progress_failure_message))
        },
        confirmButton = {
            Button(onClick = onRetry) {
                Text(text = stringResource(R.string.generated_puzzle_retry_button))
            }
        },
        dismissButton = {
            Button(onClick = onNavigateBack) {
                Text(text = stringResource(R.string.generated_puzzle_back_to_menu_button))
            }
        }
    )
}

@Composable
private fun rememberGeneratedPuzzleViewModel(
    challenge: GeneratedChallenge,
    generationUseCase: GeneratedPuzzleGenerationUseCase,
    generatedSessionRepository: GeneratedSessionRepository,
    timeSource: ElapsedTimeSource
): GeneratedPuzzleViewModel {
    val activity = LocalContext.current.findComponentActivity()
        ?: error("GeneratedModeRoute requires a ComponentActivity host.")

    return remember(activity, challenge.id, generationUseCase, generatedSessionRepository, timeSource) {
        ViewModelProvider(
            activity,
            GeneratedPuzzleViewModelFactory(
                challenge = challenge,
                generationUseCase = generationUseCase,
                generatedSessionRepository = generatedSessionRepository,
                timeSource = timeSource
            )
        )[challenge.generatedPuzzleViewModelKey(), GeneratedPuzzleViewModel::class.java]
    }
}

internal fun GeneratedChallenge.generatedPuzzleViewModelKey(): String = "generated-puzzle-${id.value}"

private class GeneratedPuzzleViewModelFactory(
    private val challenge: GeneratedChallenge,
    private val generationUseCase: GeneratedPuzzleGenerationUseCase,
    private val generatedSessionRepository: GeneratedSessionRepository,
    private val timeSource: ElapsedTimeSource
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
                    generatedSessionRepository = generatedSessionRepository,
                    timeSource = timeSource
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
private const val GENERATED_TIMER_REFRESH_INTERVAL_MILLISECONDS = 250L
private val ZERO_GENERATED_ELAPSED_TIME = GeneratedElapsedTime(0)
internal const val REPLACEMENT_TRANSITION_INITIAL_ALPHA = 0.82f
internal const val REPLACEMENT_TRANSITION_INITIAL_SCALE = 0.985f
internal const val REPLACEMENT_TRANSITION_DURATION_MILLIS = 260
