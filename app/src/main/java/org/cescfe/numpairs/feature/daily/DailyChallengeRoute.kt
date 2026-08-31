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
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import java.util.Locale
import org.cescfe.numpairs.R
import org.cescfe.numpairs.data.daily.session.DailySessionRepository
import org.cescfe.numpairs.domain.daily.DailyChallengeId
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
    shareLauncher: DailyCompletionShareLauncher? = null
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
        generatedPuzzleGenerationUseCaseFactory = generatedPuzzleGenerationUseCaseFactory
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
            onPuzzleChanged = viewModel::onCommittedPuzzleChanged,
            onRetryPersistence = viewModel::retryPersistence,
            onNavigateBack = onNavigateBack
        )

        is DailyPuzzleUiState.Completed -> {
            val completedIdentity = state.completedIdentity()
            if (DailyRecipes.catalog.resolveOrNull(completedIdentity.recipeVersion) == null) {
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
                    onPuzzleChanged = viewModel::onCommittedPuzzleChanged,
                    onRetryPersistence = viewModel::retryPersistence,
                    completionContent = dailyCompletionOverlayContent(
                        onShareResult = {
                            shareResult(completedIdentity)
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
                    onShareResult = {
                        shareResult(state.completion.identity)
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
            val presentation = rememberDailyChallengeTitle(identity)
            DailyCompletionScreen(
                presentation = presentation,
                onShareResult = {
                    shareResult(identity)
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
    onPuzzleChanged: (
        org.cescfe.numpairs.data.daily.session.DailySessionId,
        org.cescfe.numpairs.domain.puzzle.model.Puzzle
    ) -> Unit,
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
private fun dailyCompletionOverlayContent(
    onShareResult: () -> Unit,
    onViewCalendar: () -> Unit,
    onNavigateBack: () -> Unit
): GameSuccessOverlayContent = GameSuccessOverlayContent(
    message = stringResource(R.string.daily_completion_message),
    supportingText = stringResource(R.string.daily_completion_supporting_text),
    primaryActionLabel = stringResource(R.string.daily_share_result_action),
    onPrimaryAction = onShareResult,
    secondaryActionLabel = stringResource(R.string.daily_view_calendar_action),
    onSecondaryAction = onViewCalendar,
    tertiaryActionLabel = stringResource(R.string.daily_back_to_menu_action),
    onTertiaryAction = onNavigateBack,
    onBackRequested = onNavigateBack
)

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
private fun rememberDailyShareResultAction(shareLauncher: DailyCompletionShareLauncher?): (DailyChallengeId) -> Unit {
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
        { completedIdentity ->
            activeLauncher.launch(payloadFactory.create(completedIdentity))
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
    DailyPuzzlePersistenceFailure.Persistence -> R.string.daily_storage_failure_message
}

private fun DailyPuzzleUiState.Completed.completedIdentity(): DailyChallengeId = when (val result = completion) {
    is DailyPuzzleCompletion.Completed -> result.completion.identity
    is DailyPuzzleCompletion.AlreadyCompleted -> result.completion.identity
}

private fun DailyChallengeId.canonicalKey(): String = "$canonicalLocalDate:${recipeVersion.value}"

private val DAILY_STATUS_MAX_WIDTH = 480.dp

@Composable
private fun rememberDailyPuzzleViewModel(
    identity: DailyChallengeId,
    dailySessionRepository: DailySessionRepository,
    generatedPuzzleGenerationUseCaseFactory: GeneratedPuzzleGenerationUseCaseFactory
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
        generatedPuzzleGenerationUseCaseFactory
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
            dailySessionRepository = dailySessionRepository
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
    private val dailySessionRepository: DailySessionRepository
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
                    dailySessionRepository = dailySessionRepository
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
