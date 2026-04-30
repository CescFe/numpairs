package org.cescfe.numpairs

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import org.cescfe.numpairs.ui.screen.GameScreen
import org.cescfe.numpairs.ui.screen.GameViewModel
import org.cescfe.numpairs.ui.theme.NumPairsTheme

class MainActivity : ComponentActivity() {
    private val gameViewModel: GameViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            val uiState by gameViewModel.uiState.collectAsState()

            NumPairsTheme {
                GameScreen(
                    uiState = uiState,
                    onStripItemTapped = gameViewModel::onStripItemTapped,
                    onStripItemEntryDismissed = gameViewModel::onStripItemEntryDismissed,
                    onStripItemEntryConfirmed = gameViewModel::onStripItemEntryConfirmed,
                    onTileLeftOperandTapped = gameViewModel::onTileLeftOperandTapped,
                    onTileRightOperandTapped = gameViewModel::onTileRightOperandTapped,
                    onTileOperandSelectionDismissed = gameViewModel::onTileOperandSelectionDismissed,
                    onTileOperandSelectionConfirmed = gameViewModel::onTileOperandSelectionConfirmed,
                    onTileOperatorTapped = gameViewModel::onTileOperatorTapped,
                    onTileOperatorSelectionDismissed = gameViewModel::onTileOperatorSelectionDismissed,
                    onTileOperatorSelectionConfirmed = gameViewModel::onTileOperatorSelectionConfirmed,
                    onSuccessOverlayDismissed = gameViewModel::onSuccessOverlayDismissed
                )
            }
        }
    }
}
