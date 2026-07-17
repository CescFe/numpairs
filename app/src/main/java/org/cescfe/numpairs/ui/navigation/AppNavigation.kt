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
import org.cescfe.numpairs.feature.generated.GeneratedModeId
import org.cescfe.numpairs.feature.generated.GeneratedModeLaunchIntent
import org.cescfe.numpairs.feature.generated.GeneratedModeRegistry
import org.cescfe.numpairs.feature.generated.GeneratedModeRoute
import org.cescfe.numpairs.feature.generated.GeneratedModes
import org.cescfe.numpairs.feature.generated.GeneratedPuzzleGenerationUseCaseFactory
import org.cescfe.numpairs.feature.menu.MenuRoute
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
    var currentDestination by remember(startDestination) {
        mutableStateOf(startDestination)
    }
    val navigateToMenu: () -> Unit = {
        currentDestination = AppDestination.Menu
    }

    BackHandler(enabled = currentDestination != AppDestination.Menu) {
        navigateToMenu()
    }

    when (val destination = currentDestination) {
        AppDestination.Menu -> MenuRoute(
            modifier = modifier,
            onTutorialSelected = {
                currentDestination = AppDestination.Tutorial
            },
            onFourPairsSelected = {
                currentDestination = AppDestination.GeneratedMode(modeId = GeneratedModes.FOUR_PAIRS.id)
            },
            onEightPairsSelected = {
                currentDestination = AppDestination.GeneratedMode(modeId = GeneratedModes.EIGHT_PAIRS.id)
            }
        )
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
                    title = mode.titleResourceIdOrNull()?.let { titleResourceId ->
                        stringResource(id = titleResourceId)
                    } ?: mode.id.value,
                    generationUseCase = generationUseCase,
                    generatedSessionRepository = generatedSessionRepository,
                    modifier = modifier,
                    onNavigateBack = navigateToMenu
                )
            }
        }
    }
}
