package org.cescfe.numpairs.ui.navigation

import android.content.Context
import android.content.ContextWrapper
import androidx.activity.ComponentActivity
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.ViewModelProvider
import org.cescfe.numpairs.ui.screen.GameScreen
import org.cescfe.numpairs.ui.screen.GameViewModel

@Composable
fun GameRoute(modifier: Modifier = Modifier) {
    val gameViewModel = rememberGameViewModel()
    val uiState by gameViewModel.uiState.collectAsState()

    GameScreen(
        uiState = uiState,
        modifier = modifier,
        onStripItemTapped = gameViewModel::onStripItemTapped,
        onStripItemEntryDismissed = gameViewModel::onStripItemEntryDismissed,
        onStripItemEntryConfirmed = gameViewModel::onStripItemEntryConfirmed,
        onTileLeftOperandTapped = gameViewModel::onTileLeftOperandTapped,
        onTileRightOperandTapped = gameViewModel::onTileRightOperandTapped,
        onTileOperandSelectionDismissed = gameViewModel::onTileOperandSelectionDismissed,
        onTileOperandSelectionConfirmed = gameViewModel::onTileOperandSelectionConfirmed,
        onTileOperatorTapped = gameViewModel::onTileOperatorTapped,
        onTileResetTapped = gameViewModel::onTileResetTapped,
        onTileOperatorSelectionDismissed = gameViewModel::onTileOperatorSelectionDismissed,
        onTileOperatorSelectionConfirmed = gameViewModel::onTileOperatorSelectionConfirmed,
        onSuccessOverlayDismissed = gameViewModel::onSuccessOverlayDismissed
    )
}

@Composable
private fun rememberGameViewModel(): GameViewModel {
    val activity = LocalContext.current.findComponentActivity()
        ?: error("GameRoute requires a ComponentActivity host.")

    return remember(activity) {
        ViewModelProvider(activity)[GameViewModel::class.java]
    }
}

private tailrec fun Context.findComponentActivity(): ComponentActivity? = when (this) {
    is ComponentActivity -> this
    is ContextWrapper -> baseContext.findComponentActivity()
    else -> null
}
