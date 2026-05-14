package org.cescfe.numpairs.ui.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import org.cescfe.numpairs.feature.game.GameRoute
import org.cescfe.numpairs.feature.menu.MenuRoute

sealed interface AppDestination {
    data object Menu : AppDestination
    data object Game : AppDestination
}

@Composable
fun AppNavigation(modifier: Modifier = Modifier, startDestination: AppDestination = AppDestination.Game) {
    var currentDestination by remember(startDestination) {
        mutableStateOf(startDestination)
    }

    when (currentDestination) {
        AppDestination.Menu -> MenuRoute(
            modifier = modifier,
            onTutorialSelected = {
                currentDestination = AppDestination.Game
            }
        )
        AppDestination.Game -> GameRoute(modifier = modifier)
    }
}
