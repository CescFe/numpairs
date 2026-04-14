package org.cescfe.numpairs

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import org.cescfe.numpairs.domain.puzzle.PuzzleSamples
import org.cescfe.numpairs.ui.screen.GameScreen
import org.cescfe.numpairs.ui.theme.NumPairsTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            NumPairsTheme {
                GameScreen(puzzle = PuzzleSamples.prototype)
            }
        }
    }
}
