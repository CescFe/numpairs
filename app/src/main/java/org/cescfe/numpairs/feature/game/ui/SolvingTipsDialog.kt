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
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
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
import org.cescfe.numpairs.ui.theme.NumPairsComponents
import org.cescfe.numpairs.ui.theme.NumPairsTheme

@Composable
internal fun SolvingTipsDialog(
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier,
    onPracticeTipsRequested: () -> Unit = {}
) {
    val containerColor = NumPairsComponents.raisedSurfaceColor()

    AlertDialog(
        modifier = modifier.testTag(GameScreenTestTags.SOLVING_TIPS_DIALOG),
        onDismissRequest = onDismiss,
        shape = NumPairsComponents.LargeShape,
        containerColor = containerColor,
        titleContentColor = MaterialTheme.colorScheme.onSurface,
        textContentColor = MaterialTheme.colorScheme.onSurfaceVariant,
        title = {
            SolvingTipsTitle(onDismiss = onDismiss)
        },
        text = {
            SolvingTipsContent(containerColor = containerColor)
        },
        confirmButton = {
            TextButton(
                onClick = {
                    onDismiss()
                    onPracticeTipsRequested()
                },
                modifier = Modifier.testTag(GameScreenTestTags.SOLVING_TIPS_PRACTICE_BUTTON),
                colors = ButtonDefaults.textButtonColors(
                    contentColor = MaterialTheme.colorScheme.primary
                )
            ) {
                Text(text = stringResource(R.string.solving_tips_practice_button))
            }
        },
        properties = DialogProperties(
            dismissOnBackPress = true,
            dismissOnClickOutside = true
        )
    )
}

@Composable
private fun SolvingTipsTitle(onDismiss: () -> Unit) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = stringResource(R.string.solving_tips_title),
            style = MaterialTheme.typography.titleMedium
        )
        IconButton(
            onClick = onDismiss,
            modifier = Modifier.testTag(GameScreenTestTags.SOLVING_TIPS_CLOSE_BUTTON),
            colors = NumPairsComponents.iconButtonColors()
        ) {
            Icon(
                painter = painterResource(R.drawable.ic_close),
                contentDescription = stringResource(R.string.solving_tips_close_content_description)
            )
        }
    }
}

@Composable
private fun SolvingTipsContent(containerColor: Color) {
    val scrollState = rememberScrollState()
    val hasContentAbove by remember {
        derivedStateOf { scrollState.value > 0 }
    }
    val hasContentBelow by remember {
        derivedStateOf { scrollState.value < scrollState.maxValue }
    }

    Box(
        modifier = Modifier
            .heightIn(max = SOLVING_TIPS_CONTENT_MAX_HEIGHT)
            .testTag(GameScreenTestTags.SOLVING_TIPS_CONTENT)
    ) {
        Column(
            modifier = Modifier.verticalScroll(scrollState),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            SolvingTipsSection(
                title = stringResource(R.string.solving_tips_strip_title),
                bullets = listOf(
                    stringResource(R.string.solving_tips_strip_hidden_range),
                    stringResource(R.string.solving_tips_strip_highest_anchor)
                )
            )
            SolvingTipsSection(
                title = stringResource(R.string.solving_tips_products_title),
                bullets = listOf(
                    stringResource(R.string.solving_tips_products_large_results),
                    stringResource(R.string.solving_tips_products_factors)
                )
            )
            SolvingTipsSection(
                title = stringResource(R.string.solving_tips_sums_title),
                bullets = listOf(
                    stringResource(R.string.solving_tips_sums_prime_results)
                )
            )
            SolvingTipsSection(
                title = stringResource(R.string.solving_tips_ui_clues_title),
                bullets = listOf(
                    stringResource(R.string.solving_tips_ui_clues_operand_usage)
                )
            )
        }

        if (hasContentAbove) {
            SolvingTipsScrollFade(
                containerColor = containerColor,
                edge = SolvingTipsScrollEdge.Top
            )
        }
        if (hasContentBelow) {
            SolvingTipsScrollFade(
                containerColor = containerColor,
                edge = SolvingTipsScrollEdge.Bottom
            )
        }
    }
}

@Composable
private fun SolvingTipsSection(title: String, bullets: List<String>) {
    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
        Text(
            text = title,
            style = MaterialTheme.typography.titleSmall,
            color = MaterialTheme.colorScheme.onSurface
        )
        bullets.forEach { bullet ->
            SolvingTipsBullet(text = bullet)
        }
    }
}

@Composable
private fun SolvingTipsBullet(text: String) {
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
private fun BoxScope.SolvingTipsScrollFade(containerColor: Color, edge: SolvingTipsScrollEdge) {
    val colors = when (edge) {
        SolvingTipsScrollEdge.Top -> listOf(containerColor, Color.Transparent)
        SolvingTipsScrollEdge.Bottom -> listOf(Color.Transparent, containerColor)
    }
    val alignment = when (edge) {
        SolvingTipsScrollEdge.Top -> Alignment.TopCenter
        SolvingTipsScrollEdge.Bottom -> Alignment.BottomCenter
    }

    Box(
        modifier = Modifier
            .align(alignment)
            .fillMaxWidth()
            .height(SOLVING_TIPS_FADE_HEIGHT)
            .background(Brush.verticalGradient(colors = colors))
    )
}

private enum class SolvingTipsScrollEdge {
    Top,
    Bottom
}

@Preview(showBackground = true)
@Composable
private fun SolvingTipsDialogPreview() {
    NumPairsTheme {
        SolvingTipsDialog(onDismiss = {})
    }
}

private val SOLVING_TIPS_CONTENT_MAX_HEIGHT = 420.dp
private val SOLVING_TIPS_FADE_HEIGHT = 36.dp
private const val BULLET = "\u2022"
