package org.cescfe.numpairs.ui.navigation

import androidx.activity.compose.BackHandler
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import org.cescfe.numpairs.data.generated.session.GeneratedSessionRepository
import org.cescfe.numpairs.data.onboarding.OnboardingRepository
import org.cescfe.numpairs.data.onboarding.OnboardingState
import org.cescfe.numpairs.data.preferences.TopAppBarActionDiscoveryRepository
import org.cescfe.numpairs.feature.fourpairs.FourPairsRoute
import org.cescfe.numpairs.feature.generated.GeneratedModeConfiguration
import org.cescfe.numpairs.feature.generated.GeneratedModeId
import org.cescfe.numpairs.feature.generated.GeneratedModeLaunchIntent
import org.cescfe.numpairs.feature.generated.GeneratedModeRegistry
import org.cescfe.numpairs.feature.generated.GeneratedModeRoute
import org.cescfe.numpairs.feature.generated.GeneratedModes
import org.cescfe.numpairs.feature.generated.GeneratedPuzzleGenerationUseCaseFactory
import org.cescfe.numpairs.feature.menu.MenuRoute
import org.cescfe.numpairs.feature.menu.ui.GeneratedSessionChoiceDialog
import org.cescfe.numpairs.feature.onboarding.OnboardingLoadingScreen
import org.cescfe.numpairs.feature.onboarding.RequiredOnboardingRoute
import org.cescfe.numpairs.feature.tutorial.GuidedIntroductionRoute

sealed interface AppDestination {
    data object Menu : AppDestination
    data object Tutorial : AppDestination
    data class GeneratedMode(
        val modeId: GeneratedModeId,
        val launchIntent: GeneratedModeLaunchIntent = GeneratedModeLaunchIntent.newPuzzle()
    ) : AppDestination
}

@Composable
fun AppNavigation(
    onboardingRepository: OnboardingRepository,
    generatedSessionRepository: GeneratedSessionRepository,
    topAppBarActionDiscoveryRepository: TopAppBarActionDiscoveryRepository,
    generatedModeRegistry: GeneratedModeRegistry,
    generatedPuzzleGenerationUseCaseFactory: GeneratedPuzzleGenerationUseCaseFactory,
    modifier: Modifier = Modifier,
    startDestination: AppDestination = AppDestination.Menu
) {
    val onboardingState by onboardingRepository.onboardingState.collectAsState(initial = OnboardingState())

    when {
        !onboardingState.isInitialized -> OnboardingLoadingScreen(modifier = modifier)
        !onboardingState.isRequiredVersionComplete() -> RequiredOnboardingRoute(
            onboardingState = onboardingState,
            onboardingRepository = onboardingRepository,
            modifier = modifier
        )
        else -> UnlockedAppNavigation(
            generatedSessionRepository = generatedSessionRepository,
            topAppBarActionDiscoveryRepository = topAppBarActionDiscoveryRepository,
            generatedModeRegistry = generatedModeRegistry,
            generatedPuzzleGenerationUseCaseFactory = generatedPuzzleGenerationUseCaseFactory,
            modifier = modifier,
            startDestination = startDestination
        )
    }
}

@Composable
private fun UnlockedAppNavigation(
    generatedSessionRepository: GeneratedSessionRepository,
    topAppBarActionDiscoveryRepository: TopAppBarActionDiscoveryRepository,
    generatedModeRegistry: GeneratedModeRegistry,
    generatedPuzzleGenerationUseCaseFactory: GeneratedPuzzleGenerationUseCaseFactory,
    modifier: Modifier,
    startDestination: AppDestination
) {
    val generatedSessionSnapshot by generatedSessionRepository.session.collectAsState(initial = null)
    val resumableSession = generatedSessionSnapshot.toResumableGeneratedSessionOrNull(
        modeRegistry = generatedModeRegistry
    )
    var pendingGeneratedModeChoice by remember {
        mutableStateOf<GeneratedModeConfiguration?>(null)
    }
    var currentDestination by remember(startDestination) {
        mutableStateOf(startDestination)
    }
    val navigateToMenu: () -> Unit = {
        pendingGeneratedModeChoice = null
        currentDestination = AppDestination.Menu
    }
    val navigateToNewGeneratedPuzzle: (GeneratedModeConfiguration) -> Unit = { mode ->
        currentDestination = AppDestination.GeneratedMode(
            modeId = mode.id,
            launchIntent = GeneratedModeLaunchIntent.newPuzzle()
        )
    }
    val onGeneratedModeSelected: (GeneratedModeConfiguration) -> Unit = { selectedMode ->
        if (resumableSession == null) {
            navigateToNewGeneratedPuzzle(selectedMode)
        } else {
            pendingGeneratedModeChoice = selectedMode
        }
    }

    BackHandler(enabled = currentDestination != AppDestination.Menu) {
        navigateToMenu()
    }

    when (val destination = currentDestination) {
        AppDestination.Menu -> {
            MenuRoute(
                modifier = modifier,
                resumeModeName = resumableSession?.mode?.localizedTitle(),
                onResumeSelected = {
                    resumableSession?.let { session ->
                        currentDestination = AppDestination.GeneratedMode(
                            modeId = session.mode.id,
                            launchIntent = GeneratedModeLaunchIntent.ResumeSession(
                                expectedSessionId = session.sessionId
                            )
                        )
                    }
                },
                onTutorialSelected = {
                    currentDestination = AppDestination.Tutorial
                },
                onFourPairsSelected = {
                    onGeneratedModeSelected(GeneratedModes.FOUR_PAIRS)
                },
                onEightPairsSelected = {
                    onGeneratedModeSelected(GeneratedModes.EIGHT_PAIRS)
                }
            )
            val selectedMode = pendingGeneratedModeChoice
            if (selectedMode != null && resumableSession != null) {
                val actionGuard = remember(selectedMode.id, resumableSession.sessionId) {
                    GeneratedSessionChoiceActionGuard()
                }
                GeneratedSessionChoiceDialog(
                    savedModeName = resumableSession.mode.localizedTitle(),
                    selectedModeName = selectedMode.localizedTitle(),
                    isSameMode = resumableSession.mode.id == selectedMode.id,
                    onResume = {
                        actionGuard.handle {
                            pendingGeneratedModeChoice = null
                            currentDestination = AppDestination.GeneratedMode(
                                modeId = resumableSession.mode.id,
                                launchIntent = GeneratedModeLaunchIntent.ResumeSession(
                                    expectedSessionId = resumableSession.sessionId
                                )
                            )
                        }
                    },
                    onNewPuzzle = {
                        actionGuard.handle {
                            pendingGeneratedModeChoice = null
                            navigateToNewGeneratedPuzzle(selectedMode)
                        }
                    },
                    onDismiss = {
                        if (!actionGuard.isHandled) {
                            pendingGeneratedModeChoice = null
                        }
                    }
                )
            }
        }
        AppDestination.Tutorial -> GuidedIntroductionRoute(
            modifier = modifier,
            onNavigateBack = navigateToMenu
        )
        is AppDestination.GeneratedMode -> {
            val mode = generatedModeRegistry.resolve(id = destination.modeId)
            val generationUseCase = remember(generatedPuzzleGenerationUseCaseFactory, mode.id) {
                generatedPuzzleGenerationUseCaseFactory.create(mode = mode)
            }

            when (mode.id) {
                GeneratedModes.FOUR_PAIRS.id -> FourPairsRoute(
                    modifier = modifier,
                    mode = mode,
                    launchIntent = destination.launchIntent,
                    generationUseCase = generationUseCase,
                    generatedSessionRepository = generatedSessionRepository,
                    topAppBarActionDiscoveryRepository = topAppBarActionDiscoveryRepository,
                    onNavigateBack = navigateToMenu
                )

                else -> GeneratedModeRoute(
                    mode = mode,
                    launchIntent = destination.launchIntent,
                    title = mode.localizedTitle(),
                    generationUseCase = generationUseCase,
                    generatedSessionRepository = generatedSessionRepository,
                    modifier = modifier,
                    onNavigateBack = navigateToMenu
                )
            }
        }
    }
}

@Composable
private fun GeneratedModeConfiguration.localizedTitle(): String = titleResourceIdOrNull()?.let { titleResourceId ->
    stringResource(id = titleResourceId)
} ?: id.value
