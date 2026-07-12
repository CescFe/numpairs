package org.cescfe.numpairs.feature.generated

import android.content.Context
import android.content.ContextWrapper
import androidx.activity.ComponentActivity
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
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.ViewModelProvider
import org.cescfe.numpairs.R
import org.cescfe.numpairs.feature.game.GameCompletionActions
import org.cescfe.numpairs.feature.game.GameRoute
import org.cescfe.numpairs.ui.theme.NumPairsComponents

@Composable
fun GeneratedModeRoute(
    mode: GeneratedModeConfiguration,
    title: String,
    generationUseCase: GeneratedPuzzleGenerationUseCase,
    modifier: Modifier = Modifier,
    isRulesHelperEnabled: Boolean = false,
    isRulesHelperActionDiscoveryDotVisible: Boolean = false,
    onRulesHelperActionTapped: () -> Unit = {},
    onRulesHelperPlayTutorialRequested: (() -> Unit)? = null,
    topBarActions: @Composable RowScope.() -> Unit = {},
    onNavigateBack: () -> Unit = {}
) {
    val viewModel = rememberGeneratedPuzzleViewModel(
        mode = mode,
        generationUseCase = generationUseCase
    )
    val uiState by viewModel.uiState.collectAsState()

    DisposableEffect(viewModel) {
        viewModel.onRouteEntered()
        onDispose(viewModel::onRouteExited)
    }

    when (val state = uiState) {
        GeneratedPuzzleGenerationUiState.Idle -> Unit
        is GeneratedPuzzleGenerationUiState.Loading -> {
            state.previousSession?.let { session ->
                GeneratedPuzzleGameContent(
                    title = title,
                    session = session,
                    modifier = modifier,
                    isRulesHelperEnabled = isRulesHelperEnabled,
                    isRulesHelperActionDiscoveryDotVisible = isRulesHelperActionDiscoveryDotVisible,
                    onRulesHelperActionTapped = onRulesHelperActionTapped,
                    onRulesHelperPlayTutorialRequested = onRulesHelperPlayTutorialRequested,
                    topBarActions = topBarActions,
                    onNewPuzzleRequested = viewModel::onNewPuzzleRequested,
                    onNavigateBack = onNavigateBack,
                    overlay = { GeneratedPuzzleLoadingOverlay() }
                )
            } ?: GeneratedPuzzleInitialLoadingScreen(modifier = modifier)
        }

        is GeneratedPuzzleGenerationUiState.Ready -> GeneratedPuzzleGameContent(
            title = title,
            session = state.session,
            modifier = modifier,
            isRulesHelperEnabled = isRulesHelperEnabled,
            isRulesHelperActionDiscoveryDotVisible = isRulesHelperActionDiscoveryDotVisible,
            onRulesHelperActionTapped = onRulesHelperActionTapped,
            onRulesHelperPlayTutorialRequested = onRulesHelperPlayTutorialRequested,
            topBarActions = topBarActions,
            onNewPuzzleRequested = viewModel::onNewPuzzleRequested,
            onNavigateBack = onNavigateBack
        )

        is GeneratedPuzzleGenerationUiState.Failed -> {
            state.previousSession?.let { session ->
                GeneratedPuzzleGameContent(
                    title = title,
                    session = session,
                    modifier = modifier,
                    isRulesHelperEnabled = isRulesHelperEnabled,
                    isRulesHelperActionDiscoveryDotVisible = isRulesHelperActionDiscoveryDotVisible,
                    onRulesHelperActionTapped = onRulesHelperActionTapped,
                    onRulesHelperPlayTutorialRequested = onRulesHelperPlayTutorialRequested,
                    topBarActions = topBarActions,
                    onNewPuzzleRequested = viewModel::onNewPuzzleRequested,
                    onNavigateBack = onNavigateBack,
                    overlay = {
                        GeneratedPuzzleFailureDialog(
                            onRetry = viewModel::retry,
                            onNavigateBack = onNavigateBack
                        )
                    }
                )
            } ?: GeneratedPuzzleInitialFailureScreen(
                modifier = modifier,
                onRetry = viewModel::retry,
                onNavigateBack = onNavigateBack
            )
        }
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
    onNewPuzzleRequested: () -> Unit,
    onNavigateBack: () -> Unit,
    overlay: @Composable () -> Unit = {}
) {
    Box(modifier = modifier.fillMaxSize()) {
        GameRoute(
            title = title,
            initialPuzzle = session.initialPuzzle,
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
            topBarActions = topBarActions,
            onNavigateBack = onNavigateBack
        )
        overlay()
    }
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
    mode: GeneratedModeConfiguration,
    generationUseCase: GeneratedPuzzleGenerationUseCase
): GeneratedPuzzleViewModel {
    val activity = LocalContext.current.findComponentActivity()
        ?: error("GeneratedModeRoute requires a ComponentActivity host.")

    return remember(activity, mode.id, generationUseCase) {
        ViewModelProvider(
            activity,
            GeneratedPuzzleViewModelFactory(
                mode = mode,
                generationUseCase = generationUseCase
            )
        )["generated-puzzle-${mode.id.value}", GeneratedPuzzleViewModel::class.java]
    }
}

private class GeneratedPuzzleViewModelFactory(
    private val mode: GeneratedModeConfiguration,
    private val generationUseCase: GeneratedPuzzleGenerationUseCase
) : ViewModelProvider.Factory {
    override fun <T : androidx.lifecycle.ViewModel> create(modelClass: Class<T>): T {
        require(modelClass.isAssignableFrom(GeneratedPuzzleViewModel::class.java)) {
            "Unsupported ViewModel type ${modelClass.name}."
        }

        return requireNotNull(
            modelClass.cast(
                GeneratedPuzzleViewModel(
                    mode = mode,
                    generationUseCase = generationUseCase
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
