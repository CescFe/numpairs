package org.cescfe.numpairs.feature.menu.ui

import androidx.compose.material3.AlertDialog
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.window.DialogProperties
import org.cescfe.numpairs.R
import org.cescfe.numpairs.ui.theme.NumPairsComponents

@Composable
internal fun GeneratedSessionChoiceDialog(
    savedModeName: String,
    selectedModeName: String,
    onResume: () -> Unit,
    onNewPuzzle: () -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        modifier = Modifier.testTag(MenuScreenTestTags.SESSION_CHOICE_DIALOG),
        onDismissRequest = onDismiss,
        shape = NumPairsComponents.LargeShape,
        containerColor = NumPairsComponents.raisedSurfaceColor(),
        titleContentColor = MaterialTheme.colorScheme.onSurface,
        textContentColor = MaterialTheme.colorScheme.onSurfaceVariant,
        title = {
            Text(
                text = stringResource(R.string.generated_session_choice_title),
                style = MaterialTheme.typography.titleMedium
            )
        },
        text = {
            Text(
                text = stringResource(
                    R.string.generated_session_choice_mode_message,
                    savedModeName
                ),
                style = MaterialTheme.typography.bodyLarge
            )
        },
        confirmButton = {
            NumPairsComponents.PrimaryCtaButton(
                onClick = onResume,
                modifier = Modifier.testTag(MenuScreenTestTags.SESSION_CHOICE_RESUME_BUTTON)
            ) {
                Text(
                    text = stringResource(R.string.menu_resume_button),
                    style = MaterialTheme.typography.labelLarge
                )
            }
        },
        dismissButton = {
            OutlinedButton(
                onClick = onNewPuzzle,
                modifier = Modifier.testTag(MenuScreenTestTags.SESSION_CHOICE_NEW_PUZZLE_BUTTON),
                shape = NumPairsComponents.MediumShape,
                colors = NumPairsComponents.secondaryButtonColors(),
                border = NumPairsComponents.secondaryButtonBorder()
            ) {
                Text(
                    text = stringResource(
                        R.string.generated_session_choice_new_mode_button,
                        selectedModeName
                    ),
                    style = MaterialTheme.typography.labelLarge
                )
            }
        },
        properties = DialogProperties(
            dismissOnBackPress = true,
            dismissOnClickOutside = true
        )
    )
}
