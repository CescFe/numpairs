package org.cescfe.numpairs.feature.game.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.DialogProperties
import org.cescfe.numpairs.R
import org.cescfe.numpairs.ui.theme.NumPairsTheme

@Composable
internal fun RulesHelperDialog(onDismiss: () -> Unit, modifier: Modifier = Modifier) {
    AlertDialog(
        modifier = modifier.testTag(GameScreenTestTags.RULES_HELPER_DIALOG),
        onDismissRequest = onDismiss,
        title = {
            RulesHelperTitle(onDismiss = onDismiss)
        },
        text = {
            RulesHelperContent()
        },
        confirmButton = {},
        properties = DialogProperties(
            dismissOnBackPress = true,
            dismissOnClickOutside = true
        )
    )
}

@Composable
private fun RulesHelperTitle(onDismiss: () -> Unit) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = stringResource(R.string.rules_helper_title),
            style = MaterialTheme.typography.headlineSmall
        )
        IconButton(
            onClick = onDismiss,
            modifier = Modifier.testTag(GameScreenTestTags.RULES_HELPER_CLOSE_BUTTON)
        ) {
            Icon(
                painter = painterResource(R.drawable.ic_close),
                contentDescription = stringResource(R.string.rules_helper_close_content_description)
            )
        }
    }
}

@Composable
private fun RulesHelperContent() {
    Column(
        modifier = Modifier
            .heightIn(max = RULES_HELPER_CONTENT_MAX_HEIGHT)
            .verticalScroll(rememberScrollState())
            .testTag(GameScreenTestTags.RULES_HELPER_CONTENT),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        RulesHelperSection(
            title = stringResource(R.string.rules_helper_strip_title),
            body = stringResource(R.string.rules_helper_strip_body)
        )
        RulesHelperSection(
            title = stringResource(R.string.rules_helper_board_title),
            body = stringResource(R.string.rules_helper_board_body)
        )
        RulesHelperSection(
            title = stringResource(R.string.rules_helper_expression_title),
            body = stringResource(R.string.rules_helper_expression_body)
        )
        RulesHelperSection(
            title = stringResource(R.string.rules_helper_pairs_title),
            body = stringResource(R.string.rules_helper_pairs_body)
        )
        RulesHelperSection(
            title = stringResource(R.string.rules_helper_completion_title),
            body = stringResource(R.string.rules_helper_completion_body)
        )
    }
}

@Composable
private fun RulesHelperSection(title: String, body: String) {
    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
        Text(
            text = title,
            style = MaterialTheme.typography.titleSmall,
            color = MaterialTheme.colorScheme.onSurface
        )
        Text(
            text = body,
            modifier = Modifier.padding(end = 4.dp),
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun RulesHelperDialogPreview() {
    NumPairsTheme {
        RulesHelperDialog(onDismiss = {})
    }
}

private val RULES_HELPER_CONTENT_MAX_HEIGHT = 360.dp
