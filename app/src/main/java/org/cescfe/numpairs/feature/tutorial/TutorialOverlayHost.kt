package org.cescfe.numpairs.feature.tutorial

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import org.cescfe.numpairs.feature.tutorial.ui.TutorialScreenTestTags

@Composable
fun TutorialOverlayHost(
    tutorialMode: TutorialMode?,
    onTutorialClosed: () -> Unit,
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit
) {
    Box(modifier = modifier.fillMaxSize()) {
        content()

        tutorialMode?.let { mode ->
            FullScreenTutorialOverlay(
                mode = mode,
                onTutorialClosed = onTutorialClosed
            )
        }
    }
}

@Composable
private fun FullScreenTutorialOverlay(mode: TutorialMode, onTutorialClosed: () -> Unit) {
    Dialog(
        onDismissRequest = onTutorialClosed,
        properties = DialogProperties(
            dismissOnBackPress = true,
            dismissOnClickOutside = false,
            usePlatformDefaultWidth = false
        )
    ) {
        Surface(
            modifier = Modifier
                .fillMaxSize()
                .testTag(TutorialScreenTestTags.FULL_SCREEN_OVERLAY),
            color = MaterialTheme.colorScheme.background
        ) {
            TutorialRoute(
                modifier = Modifier.fillMaxSize(),
                mode = mode,
                onNavigateBack = onTutorialClosed
            )
        }
    }
}
