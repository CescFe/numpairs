package org.cescfe.numpairs.ui.navigation

import androidx.activity.compose.BackHandler
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import org.cescfe.numpairs.data.preferences.TopAppBarActionDiscoveryRepository
import org.cescfe.numpairs.feature.fourpairs.FourPairsRoute
import org.cescfe.numpairs.feature.generated.GeneratedModeId
import org.cescfe.numpairs.feature.generated.GeneratedModeRegistry
import org.cescfe.numpairs.feature.generated.GeneratedModeRoute
import org.cescfe.numpairs.feature.generated.GeneratedModes
import org.cescfe.numpairs.feature.generated.GeneratedPuzzleGenerationUseCaseFactory
import org.cescfe.numpairs.feature.menu.MenuRoute
import org.cescfe.numpairs.feature.tutorial.TutorialRoute

sealed interface AppDestination {
    data object Menu : AppDestination
    data object Tutorial : AppDestination
    data class GeneratedMode(val modeId: GeneratedModeId) : AppDestination
}

@Composable
fun AppNavigation(
    topAppBarActionDiscoveryRepository: TopAppBarActionDiscoveryRepository,
    generatedModeRegistry: GeneratedModeRegistry,
    generatedPuzzleGenerationUseCaseFactory: GeneratedPuzzleGenerationUseCaseFactory,
    modifier: Modifier = Modifier,
    startDestination: AppDestination = AppDestination.Menu
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
        AppDestination.Tutorial -> TutorialRoute(
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
                    generationUseCase = generationUseCase,
                    topAppBarActionDiscoveryRepository = topAppBarActionDiscoveryRepository,
                    onNavigateBack = navigateToMenu
                )

                else -> GeneratedModeRoute(
                    mode = mode,
                    title = mode.titleResourceIdOrNull()?.let { titleResourceId ->
                        stringResource(id = titleResourceId)
                    } ?: mode.id.value,
                    generationUseCase = generationUseCase,
                    modifier = modifier,
                    onNavigateBack = navigateToMenu
                )
            }
        }
    }
}
