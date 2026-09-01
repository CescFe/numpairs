package org.cescfe.numpairs.feature.daily

import android.content.Context
import android.content.ContextWrapper
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.platform.LocalResources
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import java.util.Locale
import kotlin.time.Duration.Companion.milliseconds
import kotlinx.coroutines.currentCoroutineContext
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import org.cescfe.numpairs.R
import org.cescfe.numpairs.data.daily.session.DailySessionRepository
import org.cescfe.numpairs.domain.daily.DailyChallengeId
import org.cescfe.numpairs.domain.daily.DailyCompletion
import org.cescfe.numpairs.domain.daily.DailyElapsedTime
import org.cescfe.numpairs.domain.daily.DailyMovementCount
import org.cescfe.numpairs.domain.daily.DeviceLocalDateSource
import org.cescfe.numpairs.feature.daily.calendar.DailyCalendarRoute
import org.cescfe.numpairs.feature.daily.share.AndroidDailyCompletionShareLauncher
import org.cescfe.numpairs.feature.daily.share.AndroidDailyCompletionSharePayloadFactory
import org.cescfe.numpairs.feature.daily.share.DailyCompletionShareLauncher
import org.cescfe.numpairs.feature.game.GameRoute
import org.cescfe.numpairs.feature.game.GameSuccessOverlayContent
import org.cescfe.numpairs.feature.generated.GeneratedPuzzleGenerationUseCaseFactory
import org.cescfe.numpairs.ui.theme.NumPairsComponents

@Composable
fun DailyChallengeRoute(
    identity: DailyChallengeId,
    dailySessionRepository: DailySessionRepository,
    deviceLocalDateSource: DeviceLocalDateSource,
    generatedPuzzleGenerationUseCaseFactory: GeneratedPuzzleGenerationUseCaseFactory,
    onNavigateBack: () -> Unit,
    modifier: Modifier = Modifier,
    isGeneratedGameHapticsEnabled: Boolean = true,
    compactTileSelectorsEnabled: Boolean = false,
    shareLauncher: DailyCompletionShareLauncher? = null,
    timeSource: DailyTimeSource = SystemDailyTimeSource
) {
    if (DailyRecipes.catalog.resolveOrNull(identity.recipeVersion) == null) {
        DailyFailureScreen(
            modifier = modifier,
            message = stringResource(R.string.daily_completion_unavailable_message),
            onRetry = null,
            onNavigateBack = onNavigateBack
        )
        return
    }
    val viewModel = rememberDailyPuzzleViewModel(
        identity = identity,
        dailySessionRepository = dailySessionRepository,
        generatedPuzzleGenerationUseCaseFactory = generatedPuzzleGenerationUseCaseFactory,
        timeSource = timeSource
    )
    val uiState by viewModel.uiState.collectAsState()
    var isCalendarVisible by remember(identity) { mutableStateOf(false) }
    val shareResult = rememberDailyShareResultAction(shareLauncher = shareLauncher)

    DisposableEffect(viewModel) {
        viewModel.onRouteEntered()
        onDispose(viewModel::onRouteExited)
    }

    BackHandler(enabled = isCalendarVisible) {
        isCalendarVisible = false
    }

    if (isCalendarVisible) {
        DailyCalendarRoute(
            dailySessionRepository = dailySessionRepository,
            deviceLocalDateSource = deviceLocalDateSource,
            onNavigateBack = {
                isCalendarVisible = false
            },
            modifier = modifier
        )
        return
    }

    when (val state = uiState) {
        DailyPuzzleUiState.Idle,
        DailyPuzzleUiState.Resolving,
        is DailyPuzzleUiState.Loading -> DailyLoadingScreen(
            modifier = modifier,
            onNavigateBack = onNavigateBack
        )

        is DailyPuzzleUiState.Failed -> DailyFailureScreen(
            modifier = modifier,
            message = stringResource(state.failure.messageResource()),
            onRetry = viewModel::retry,
            onNavigateBack = onNavigateBack
        )

        is DailyPuzzleUiState.Ready -> DailyGameContent(
            state = state,
            modifier = modifier,
            isGeneratedGameHapticsEnabled = isGeneratedGameHapticsEnabled,
            compactTileSelectorsEnabled = compactTileSelectorsEnabled,
            onPuzzleMutationCommitted = viewModel::onPuzzleMutationCommitted,
            onPuzzlePresented = viewModel::onPuzzlePresented,
            onTimerRefresh = viewModel::onTimerRefresh,
            onRetryPersistence = viewModel::retryPersistence,
            onNavigateBack = onNavigateBack
        )

        is DailyPuzzleUiState.Completed -> {
            val completion = state.completion.record()
            if (DailyRecipes.catalog.resolveOrNull(completion.identity.recipeVersion) == null) {
                DailyFailureScreen(
                    modifier = modifier,
                    message = stringResource(R.string.daily_completion_unavailable_message),
                    onRetry = null,
                    onNavigateBack = onNavigateBack
                )
            } else {
                DailyGameContent(
                    state = state,
                    modifier = modifier,
                    isGeneratedGameHapticsEnabled = isGeneratedGameHapticsEnabled,
                    compactTileSelectorsEnabled = compactTileSelectorsEnabled,
                    onPuzzleMutationCommitted = viewModel::onPuzzleMutationCommitted,
                    onPuzzlePresented = viewModel::onPuzzlePresented,
                    onTimerRefresh = viewModel::onTimerRefresh,
                    onRetryPersistence = viewModel::retryPersistence,
                    completionContent = dailyCompletionOverlayContent(
                        elapsedTime = completion.elapsedTime,
                        movementCount = completion.movementCount,
                        onShareResult = {
                            shareResult(completion)
                        },
                        onViewCalendar = {
                            isCalendarVisible = true
                        },
                        onNavigateBack = onNavigateBack
                    ),
                    onNavigateBack = onNavigateBack
                )
            }
        }

        is DailyPuzzleUiState.CompletedToday -> {
            if (DailyRecipes.catalog.resolveOrNull(state.completion.identity.recipeVersion) == null) {
                DailyFailureScreen(
                    modifier = modifier,
                    message = stringResource(R.string.daily_completion_unavailable_message),
                    onRetry = null,
                    onNavigateBack = onNavigateBack
                )
            } else {
                val presentation = rememberDailyChallengeTitle(state.completion.identity)
                DailyCompletionScreen(
                    presentation = presentation,
                    elapsedTime = state.completion.elapsedTime,
                    movementCount = state.completion.movementCount,
                    onShareResult = {
                        shareResult(state.completion)
                    },
                    onViewCalendar = {
                        isCalendarVisible = true
                    },
                    onNavigateBack = onNavigateBack,
                    modifier = modifier
                )
            }
        }
    }
}

@Composable
fun DailyCompletedTodayRoute(
    identity: DailyChallengeId,
    dailySessionRepository: DailySessionRepository,
    deviceLocalDateSource: DeviceLocalDateSource,
    onNavigateBack: () -> Unit,
    modifier: Modifier = Modifier,
    shareLauncher: DailyCompletionShareLauncher? = null
) {
    val dailyState by dailySessionRepository.state.collectAsState(initial = null)
    var isCalendarVisible by remember(identity) { mutableStateOf(false) }
    val shareResult = rememberDailyShareResultAction(shareLauncher = shareLauncher)

    BackHandler(enabled = isCalendarVisible) {
        isCalendarVisible = false
    }

    if (isCalendarVisible) {
        DailyCalendarRoute(
            dailySessionRepository = dailySessionRepository,
            deviceLocalDateSource = deviceLocalDateSource,
            onNavigateBack = {
                isCalendarVisible = false
            },
            modifier = modifier
        )
        return
    }

    when {
        dailyState == null -> DailyLoadingScreen(
            modifier = modifier,
            onNavigateBack = onNavigateBack
        )

        requireNotNull(dailyState).completions.any { completion -> completion.identity == identity } &&
            DailyRecipes.catalog.resolveOrNull(identity.recipeVersion) != null -> {
            val completion = requireNotNull(dailyState).completions.single { completion ->
                completion.identity == identity
            }
            val presentation = rememberDailyChallengeTitle(identity)
            DailyCompletionScreen(
                presentation = presentation,
                elapsedTime = completion.elapsedTime,
                movementCount = completion.movementCount,
                onShareResult = {
                    shareResult(completion)
                },
                onViewCalendar = {
                    isCalendarVisible = true
                },
                onNavigateBack = onNavigateBack,
                modifier = modifier
            )
        }

        else -> DailyFailureScreen(
            modifier = modifier,
            message = stringResource(R.string.daily_completion_unavailable_message),
            onRetry = null,
            onNavigateBack = onNavigateBack
        )
    }
}

@Composable
private fun DailyGameContent(
    state: DailyPuzzleUiState,
    modifier: Modifier,
    isGeneratedGameHapticsEnabled: Boolean,
    compactTileSelectorsEnabled: Boolean,
    onPuzzleMutationCommitted: (
        org.cescfe.numpairs.data.daily.session.DailySessionId,
        org.cescfe.numpairs.domain.puzzle.model.Puzzle
    ) -> Unit,
    onPuzzlePresented: (org.cescfe.numpairs.data.daily.session.DailySessionId) -> Unit,
    onTimerRefresh: (org.cescfe.numpairs.data.daily.session.DailySessionId) -> Unit,
    onRetryPersistence: () -> Unit,
    completionContent: GameSuccessOverlayContent? = null,
    onNavigateBack: () -> Unit
) {
    val session = when (state) {
        is DailyPuzzleUiState.Ready -> state.session
        is DailyPuzzleUiState.Completed -> state.session
        else -> error("Daily game content requires a playable session.")
    }
    val title = rememberDailyChallengeTitle(session.currentDailyChallenge.identity)
    val hapticFeedback = LocalHapticFeedback.current
    val elapsedTime = when (state) {
        is DailyPuzzleUiState.Ready -> state.elapsedTime ?: ZERO_DAILY_ELAPSED_TIME
        is DailyPuzzleUiState.Completed -> state.completion.record().elapsedTime
    }

    LaunchedEffect(session.id, session.currentPuzzle.isSolved) {
        if (session.currentPuzzle.isSolved) {
            return@LaunchedEffect
        }
        onPuzzlePresented(session.id)
        while (currentCoroutineContext().isActive) {
            delay(DAILY_TIMER_REFRESH_INTERVAL_MILLISECONDS.milliseconds)
            onTimerRefresh(session.id)
        }
    }

    Box(modifier = modifier.fillMaxSize()) {
        GameRoute(
            title = title.visibleText,
            titleContentDescription = title.accessibilityText,
            isNavigationIconVisible = state !is DailyPuzzleUiState.Completed,
            initialPuzzle = session.currentPuzzle,
            gameSessionKey = "DailyGame:${session.currentDailyChallenge.identity.canonicalKey()}",
            puzzleResetKey = session.id,
            isSuccessOverlayEnabled = state is DailyPuzzleUiState.Completed,
            successOverlayContent = completionContent,
            isCorrectTileMotionEnabled = true,
            isCompletionCelebrationEnabled = true,
            compactTileSelectorsEnabled = compactTileSelectorsEnabled,
            onPuzzleMutationCommitted = { puzzle ->
                onPuzzleMutationCommitted(session.id, puzzle)
            },
            onTileAssignmentCommitted = { _ ->
                if (isGeneratedGameHapticsEnabled) {
                    hapticFeedback.performHapticFeedback(HapticFeedbackType.Confirm)
                }
            },
            topBarActions = {
                elapsedTime?.let { visibleElapsedTime ->
                    DailyChronometer(elapsedTime = visibleElapsedTime)
                }
            },
            onNavigateBack = onNavigateBack
        )
        val persistenceFailure = (state as? DailyPuzzleUiState.Ready)?.persistenceFailure
        if (persistenceFailure != null) {
            DailyPersistenceFailureDialog(
                failure = persistenceFailure,
                onRetry = onRetryPersistence,
                onNavigateBack = onNavigateBack
            )
        }
    }
}

@Composable
internal fun dailyCompletionOverlayContent(
    elapsedTime: DailyElapsedTime?,
    movementCount: DailyMovementCount? = null,
    onShareResult: () -> Unit,
    onViewCalendar: () -> Unit,
    onNavigateBack: () -> Unit
): GameSuccessOverlayContent {
    val formattedElapsedTime = elapsedTime?.let(DailyElapsedTimeFormatter::format)
    val formattedMovementCount = movementCount?.let { count ->
        formattedDailyMovementCount(count)
    }
    val formattedResult = DailyCompletionResultFormatter.format(
        formattedElapsedTime = formattedElapsedTime,
        formattedMovementCount = formattedMovementCount
    )
    val resultContentDescription = dailyCompletionResultContentDescription(
        formattedElapsedTime = formattedElapsedTime,
        movementCount = movementCount
    )
    return GameSuccessOverlayContent(
        message = stringResource(R.string.daily_completion_message),
        supportingText = stringResource(R.string.daily_completion_supporting_text),
        highlightText = formattedResult,
        highlightContentDescription = resultContentDescription,
        primaryActionLabel = stringResource(R.string.daily_share_result_action),
        onPrimaryAction = onShareResult,
        secondaryActionLabel = stringResource(R.string.daily_view_calendar_action),
        onSecondaryAction = onViewCalendar,
        tertiaryActionLabel = stringResource(R.string.daily_back_to_menu_action),
        onTertiaryAction = onNavigateBack,
        onBackRequested = onNavigateBack
    )
}

@Composable
private fun formattedDailyMovementCount(movementCount: DailyMovementCount): String = pluralStringResource(
    R.plurals.daily_movement_count,
    movementCount.pluralQuantity(),
    DailyMovementCountFormatter.format(movementCount)
)

@Composable
private fun dailyCompletionResultContentDescription(
    formattedElapsedTime: String?,
    movementCount: DailyMovementCount?
): String? {
    val elapsedDescription = formattedElapsedTime?.let { elapsedTime ->
        stringResource(R.string.daily_elapsed_time_content_description, elapsedTime)
    }
    val movementDescription = movementCount?.let { count ->
        pluralStringResource(
            R.plurals.daily_movement_count_content_description,
            count.pluralQuantity(),
            DailyMovementCountFormatter.format(count)
        )
    }
    return when {
        elapsedDescription != null && movementDescription != null -> stringResource(
            R.string.daily_completion_result_content_description,
            elapsedDescription,
            movementDescription
        )

        elapsedDescription != null -> elapsedDescription

        else -> movementDescription
    }
}

@Composable
private fun rememberDailyChallengeTitle(identity: DailyChallengeId): DailyChallengeTitle {
    val configuration = LocalConfiguration.current
    val locale = remember(configuration) {
        Locale.forLanguageTag(configuration.locales[0].toLanguageTag())
    }
    val dailyName = stringResource(R.string.daily_game_name)
    val challengeName = stringResource(R.string.daily_completion_challenge_name)
    return remember(identity, dailyName, challengeName, locale) {
        DailyChallengeTitleFormatter().format(
            identity = identity,
            dailyName = dailyName,
            challengeName = challengeName,
            locale = locale
        )
    }
}

@Composable
private fun rememberDailyShareResultAction(shareLauncher: DailyCompletionShareLauncher?): (DailyCompletion) -> Unit {
    val context = LocalContext.current
    val defaultLauncher = remember(context) {
        AndroidDailyCompletionShareLauncher(context)
    }
    val resources = LocalResources.current
    val payloadFactory = remember(resources) {
        AndroidDailyCompletionSharePayloadFactory(resources)
    }
    val activeLauncher = shareLauncher ?: defaultLauncher
    return remember(activeLauncher, payloadFactory) {
        { completion ->
            activeLauncher.launch(payloadFactory.create(completion))
        }
    }
}

@Composable
private fun DailyLoadingScreen(modifier: Modifier, onNavigateBack: () -> Unit) {
    DailyStatusScreen(
        modifier = modifier.testTag(DailyScreenTestTags.LOADING),
        message = stringResource(R.string.daily_loading_message),
        isLoading = true,
        onRetry = null,
        onNavigateBack = onNavigateBack
    )
}

@Composable
private fun DailyFailureScreen(
    modifier: Modifier,
    message: String,
    onRetry: (() -> Unit)?,
    onNavigateBack: () -> Unit
) {
    DailyStatusScreen(
        modifier = modifier.testTag(DailyScreenTestTags.FAILURE),
        message = message,
        isLoading = false,
        onRetry = onRetry,
        onNavigateBack = onNavigateBack
    )
}

@Composable
private fun DailyStatusScreen(
    modifier: Modifier,
    message: String,
    isLoading: Boolean,
    onRetry: (() -> Unit)?,
    onNavigateBack: () -> Unit
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 24.dp, vertical = 24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        if (isLoading) {
            CircularProgressIndicator()
        }
        Text(
            text = message,
            modifier = Modifier
                .widthIn(max = DAILY_STATUS_MAX_WIDTH)
                .padding(vertical = 16.dp),
            color = MaterialTheme.colorScheme.onBackground,
            style = MaterialTheme.typography.bodyLarge
        )
        onRetry?.let { retry ->
            NumPairsComponents.PrimaryCtaButton(
                onClick = retry,
                modifier = Modifier
                    .fillMaxWidth()
                    .widthIn(max = DAILY_STATUS_MAX_WIDTH)
                    .testTag(DailyScreenTestTags.RETRY)
            ) {
                Text(text = stringResource(R.string.daily_retry_action))
            }
        }
        OutlinedButton(
            onClick = onNavigateBack,
            modifier = Modifier
                .fillMaxWidth()
                .widthIn(max = DAILY_STATUS_MAX_WIDTH)
                .padding(top = 8.dp),
            shape = NumPairsComponents.MediumShape,
            colors = NumPairsComponents.secondaryButtonColors(),
            border = NumPairsComponents.secondaryButtonBorder()
        ) {
            Text(text = stringResource(R.string.daily_back_to_menu_action))
        }
    }
}

@Composable
private fun DailyPersistenceFailureDialog(
    failure: DailyPuzzlePersistenceFailure,
    onRetry: () -> Unit,
    onNavigateBack: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onNavigateBack,
        modifier = Modifier.testTag(DailyScreenTestTags.PERSISTENCE_FAILURE),
        shape = NumPairsComponents.LargeShape,
        containerColor = NumPairsComponents.raisedSurfaceColor(),
        title = {
            Text(text = stringResource(R.string.daily_progress_failure_title))
        },
        text = {
            Text(text = stringResource(failure.messageResource()))
        },
        confirmButton = {
            Button(onClick = onRetry) {
                Text(text = stringResource(R.string.daily_retry_action))
            }
        },
        dismissButton = {
            Button(onClick = onNavigateBack) {
                Text(text = stringResource(R.string.daily_back_to_menu_action))
            }
        }
    )
}

private fun DailyPuzzlePreparationFailure.messageResource(): Int = when (this) {
    is DailyPuzzlePreparationFailure.GenerationExhausted -> R.string.daily_generation_failure_message
    is DailyPuzzlePreparationFailure.GenerationCancelled -> R.string.daily_generation_cancelled_message
    DailyPuzzlePreparationFailure.InvalidGeneratedPuzzle -> R.string.daily_invalid_puzzle_message
    DailyPuzzlePreparationFailure.Persistence -> R.string.daily_storage_failure_message
}

private fun DailyPuzzlePersistenceFailure.messageResource(): Int = when (this) {
    DailyPuzzlePersistenceFailure.StaleSession -> R.string.daily_stale_session_message
    DailyPuzzlePersistenceFailure.InvalidPuzzle -> R.string.daily_invalid_progress_message
    DailyPuzzlePersistenceFailure.InvalidTiming -> R.string.daily_invalid_progress_message
    DailyPuzzlePersistenceFailure.InvalidMovement -> R.string.daily_invalid_progress_message
    DailyPuzzlePersistenceFailure.Persistence -> R.string.daily_storage_failure_message
}

private fun DailyPuzzleCompletion.record(): DailyCompletion = when (this) {
    is DailyPuzzleCompletion.Completed -> completion
    is DailyPuzzleCompletion.AlreadyCompleted -> completion
}

private fun DailyChallengeId.canonicalKey(): String = "$canonicalLocalDate:${recipeVersion.value}"

private val DAILY_STATUS_MAX_WIDTH = 480.dp
private const val DAILY_TIMER_REFRESH_INTERVAL_MILLISECONDS = 250L
private val ZERO_DAILY_ELAPSED_TIME = DailyElapsedTime(0)

@Composable
private fun rememberDailyPuzzleViewModel(
    identity: DailyChallengeId,
    dailySessionRepository: DailySessionRepository,
    generatedPuzzleGenerationUseCaseFactory: GeneratedPuzzleGenerationUseCaseFactory,
    timeSource: DailyTimeSource
): DailyPuzzleViewModel {
    val activity = LocalContext.current.findComponentActivity()
        ?: error("DailyChallengeRoute requires a ComponentActivity host.")
    val currentDailyChallengeResolver = remember(identity) {
        CurrentDailyChallengeResolver(
            localDateSource = { identity.localDate },
            activeRecipeVersion = identity.recipeVersion
        )
    }
    val factory = remember(
        currentDailyChallengeResolver,
        dailySessionRepository,
        generatedPuzzleGenerationUseCaseFactory,
        timeSource
    ) {
        DailyPuzzleViewModelFactory(
            availabilityResolver = CurrentDailyAvailabilityResolver(
                currentDailyChallengeResolver = currentDailyChallengeResolver,
                dailySessionRepository = dailySessionRepository
            ),
            puzzleGenerator = DailyPuzzleGenerationUseCase(
                currentDailyChallengeResolver = currentDailyChallengeResolver,
                generatedPuzzleGenerationUseCaseFactory = generatedPuzzleGenerationUseCaseFactory
            ),
            dailySessionRepository = dailySessionRepository,
            timeSource = timeSource
        )
    }
    return remember(activity, identity, factory) {
        ViewModelProvider(activity, factory)[
            "DailyPuzzle:${identity.canonicalKey()}",
            DailyPuzzleViewModel::class.java
        ]
    }
}

private class DailyPuzzleViewModelFactory(
    private val availabilityResolver: CurrentDailyAvailabilityResolver,
    private val puzzleGenerator: DailyPuzzleGenerator,
    private val dailySessionRepository: DailySessionRepository,
    private val timeSource: DailyTimeSource
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        require(modelClass.isAssignableFrom(DailyPuzzleViewModel::class.java)) {
            "Unsupported ViewModel type ${modelClass.name}."
        }
        return requireNotNull(
            modelClass.cast(
                DailyPuzzleViewModel(
                    availabilityResolver = availabilityResolver,
                    puzzleGenerator = puzzleGenerator,
                    dailySessionRepository = dailySessionRepository,
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
