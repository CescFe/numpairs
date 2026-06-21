package org.cescfe.numpairs.ui.navigation

import androidx.activity.compose.BackHandler
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import org.cescfe.numpairs.data.preferences.TopAppBarActionDiscoveryRepository
import org.cescfe.numpairs.feature.fourpairs.DefaultFourPairsPuzzleProvider
import org.cescfe.numpairs.feature.fourpairs.FourPairsPuzzleProvider
import org.cescfe.numpairs.feature.fourpairs.FourPairsRoute
import org.cescfe.numpairs.feature.menu.MenuRoute
import org.cescfe.numpairs.feature.tutorial.TutorialRoute

sealed interface AppDestination {
    data object Menu : AppDestination
    data object Tutorial : AppDestination
    data object FourPairs : AppDestination
}

@Composable
fun AppNavigation(
    topAppBarActionDiscoveryRepository: TopAppBarActionDiscoveryRepository,
    modifier: Modifier = Modifier,
    startDestination: AppDestination = AppDestination.Menu,
    fourPairsPuzzleProvider: FourPairsPuzzleProvider = DefaultFourPairsPuzzleProvider
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

    when (currentDestination) {
        AppDestination.Menu -> MenuRoute(
            modifier = modifier,
            onTutorialSelected = {
                currentDestination = AppDestination.Tutorial
            },
            onFourPairsSelected = {
                currentDestination = AppDestination.FourPairs
            }
        )
        AppDestination.Tutorial -> TutorialRoute(
            modifier = modifier,
            onNavigateBack = navigateToMenu
        )
        AppDestination.FourPairs -> FourPairsRoute(
            modifier = modifier,
            puzzleProvider = fourPairsPuzzleProvider,
            topAppBarActionDiscoveryRepository = topAppBarActionDiscoveryRepository,
            onNavigateBack = navigateToMenu
        )
    }
}
