package org.cescfe.numpairs.feature.game.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import org.cescfe.numpairs.R
import org.cescfe.numpairs.ui.theme.NumPairsSand

@Composable
internal fun GameTopAppBarAction(
    icon: Painter,
    contentDescription: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    isDiscoveryDotVisible: Boolean = false,
    discoveryDotTestTag: String? = null
) {
    IconButton(
        onClick = onClick,
        modifier = modifier
    ) {
        Box(contentAlignment = Alignment.Center) {
            Icon(
                painter = icon,
                contentDescription = contentDescription
            )
            if (isDiscoveryDotVisible) {
                Box(
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .offset(
                            x = TOP_APP_BAR_ACTION_DISCOVERY_DOT_OFFSET,
                            y = -TOP_APP_BAR_ACTION_DISCOVERY_DOT_OFFSET
                        )
                        .size(TOP_APP_BAR_ACTION_DISCOVERY_DOT_SIZE)
                        .background(NumPairsSand, CircleShape)
                        .then(discoveryDotTestTag?.let { Modifier.testTag(it) } ?: Modifier)
                )
            }
        }
    }
}

@Composable
internal fun RulesHelperAction(
    modifier: Modifier = Modifier,
    onClick: () -> Unit,
    isDiscoveryDotVisible: Boolean = false
) {
    GameTopAppBarAction(
        icon = painterResource(R.drawable.ic_help),
        contentDescription = stringResource(R.string.rules_helper_action_content_description),
        onClick = onClick,
        modifier = modifier.testTag(GameScreenTestTags.RULES_HELPER_ACTION),
        isDiscoveryDotVisible = isDiscoveryDotVisible,
        discoveryDotTestTag = GameScreenTestTags.RULES_HELPER_ACTION_DISCOVERY_DOT
    )
}

@Composable
internal fun HintAction(modifier: Modifier = Modifier, onClick: () -> Unit, isDiscoveryDotVisible: Boolean = false) {
    GameTopAppBarAction(
        icon = painterResource(R.drawable.ic_hint),
        contentDescription = stringResource(R.string.hint_action_content_description),
        onClick = onClick,
        modifier = modifier.testTag(GameScreenTestTags.HINT_ACTION),
        isDiscoveryDotVisible = isDiscoveryDotVisible,
        discoveryDotTestTag = GameScreenTestTags.HINT_ACTION_DISCOVERY_DOT
    )
}

private val TOP_APP_BAR_ACTION_DISCOVERY_DOT_SIZE = 8.dp
private val TOP_APP_BAR_ACTION_DISCOVERY_DOT_OFFSET = 2.dp
