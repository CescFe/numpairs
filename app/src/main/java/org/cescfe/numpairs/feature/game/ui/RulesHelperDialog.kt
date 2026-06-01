package org.cescfe.numpairs.feature.game.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
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
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
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
    val containerColor = MaterialTheme.colorScheme.surfaceContainerHigh

    AlertDialog(
        modifier = modifier.testTag(GameScreenTestTags.RULES_HELPER_DIALOG),
        onDismissRequest = onDismiss,
        containerColor = containerColor,
        title = {
            RulesHelperTitle(onDismiss = onDismiss)
        },
        text = {
            RulesHelperContent(containerColor = containerColor)
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
private fun RulesHelperContent(containerColor: Color) {
    val scrollState = rememberScrollState()
    val hasContentAbove by remember {
        derivedStateOf { scrollState.value > 0 }
    }
    val hasContentBelow by remember {
        derivedStateOf { scrollState.value < scrollState.maxValue }
    }

    Box(
        modifier = Modifier
            .heightIn(max = RULES_HELPER_CONTENT_MAX_HEIGHT)
            .testTag(GameScreenTestTags.RULES_HELPER_CONTENT)
    ) {
        Column(
            modifier = Modifier
                .verticalScroll(scrollState)
                .padding(
                    top = if (hasContentAbove) RULES_HELPER_FADE_HEIGHT else 0.dp,
                    bottom = if (hasContentBelow) RULES_HELPER_FADE_HEIGHT else 0.dp
                ),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            RulesHelperSection(
                title = stringResource(R.string.rules_helper_objective_title),
                bullets = listOf(
                    stringResource(R.string.rules_helper_objective_complete)
                )
            )
            RulesHelperSection(
                title = stringResource(R.string.rules_helper_elements_title),
                bullets = listOf(
                    stringResource(R.string.rules_helper_elements_strip),
                    stringResource(R.string.rules_helper_elements_grid)
                )
            )
            RulesHelperSection(
                title = stringResource(R.string.rules_helper_strip_title),
                bullets = listOf(
                    stringResource(R.string.rules_helper_strip_hidden)
                )
            )
            RulesHelperSection(
                title = stringResource(R.string.rules_helper_grid_title),
                bullets = listOf(
                    stringResource(R.string.rules_helper_grid_expression),
                    stringResource(R.string.rules_helper_grid_pair_usage)
                )
            )
        }

        if (hasContentAbove) {
            RulesHelperScrollFade(
                containerColor = containerColor,
                edge = RulesHelperScrollEdge.Top
            )
        }
        if (hasContentBelow) {
            RulesHelperScrollFade(
                containerColor = containerColor,
                edge = RulesHelperScrollEdge.Bottom
            )
        }
    }
}

@Composable
private fun RulesHelperSection(title: String, bullets: List<String>) {
    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
        Text(
            text = title,
            style = MaterialTheme.typography.titleSmall,
            color = MaterialTheme.colorScheme.onSurface
        )
        bullets.forEach { bullet ->
            RulesHelperBullet(text = bullet)
        }
    }
}

@Composable
private fun RulesHelperBullet(text: String) {
    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        Text(
            text = BULLET,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Text(
            text = text,
            modifier = Modifier
                .weight(1f)
                .padding(end = 4.dp),
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
private fun BoxScope.RulesHelperScrollFade(containerColor: Color, edge: RulesHelperScrollEdge) {
    val colors = when (edge) {
        RulesHelperScrollEdge.Top -> listOf(containerColor, Color.Transparent)
        RulesHelperScrollEdge.Bottom -> listOf(Color.Transparent, containerColor)
    }
    val alignment = when (edge) {
        RulesHelperScrollEdge.Top -> Alignment.TopCenter
        RulesHelperScrollEdge.Bottom -> Alignment.BottomCenter
    }

    Box(
        modifier = Modifier
            .align(alignment)
            .fillMaxWidth()
            .height(RULES_HELPER_FADE_HEIGHT)
            .background(Brush.verticalGradient(colors = colors))
    )
}

private enum class RulesHelperScrollEdge {
    Top,
    Bottom
}

@Preview(showBackground = true)
@Composable
private fun RulesHelperDialogPreview() {
    NumPairsTheme {
        RulesHelperDialog(onDismiss = {})
    }
}

private val RULES_HELPER_CONTENT_MAX_HEIGHT = 420.dp
private val RULES_HELPER_FADE_HEIGHT = 36.dp
private const val BULLET = "\u2022"
