package org.cescfe.numpairs.ui.navigation

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier

sealed interface AppDestination {
    data object Game : AppDestination
}

@Composable
fun AppNavigation(modifier: Modifier = Modifier, startDestination: AppDestination = AppDestination.Game) {
    when (startDestination) {
        AppDestination.Game -> GameRoute(modifier = modifier)
    }
}
