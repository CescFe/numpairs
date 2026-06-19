package org.cescfe.numpairs.feature.menu.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Button
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import org.cescfe.numpairs.R
import org.cescfe.numpairs.ui.theme.NumPairsComponents
import org.cescfe.numpairs.ui.theme.NumPairsTheme

@Composable
fun MenuScreen(
    modifier: Modifier = Modifier,
    onTutorialSelected: () -> Unit = {},
    onFourPairsSelected: () -> Unit = {}
) {
    Scaffold(
        modifier = modifier
            .fillMaxSize()
            .testTag(MenuScreenTestTags.SCREEN),
        topBar = {
            MenuScreenTopBar()
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 24.dp, vertical = 20.dp),
            contentAlignment = Alignment.Center
        ) {
            BoxWithConstraints {
                val contentWidth = if (maxWidth < MENU_CONTENT_MAX_WIDTH) {
                    maxWidth
                } else {
                    MENU_CONTENT_MAX_WIDTH
                }

                Column(
                    modifier = Modifier.width(contentWidth),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    NumPairsComponents.PrimaryCtaButton(
                        onClick = onFourPairsSelected,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(NumPairsComponents.ButtonHeight)
                            .testTag(MenuScreenTestTags.FOUR_PAIRS_BUTTON)
                    ) {
                        MenuButtonText(text = stringResource(R.string.menu_four_pairs_button))
                    }
                    Button(
                        onClick = onTutorialSelected,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(NumPairsComponents.ButtonHeight)
                            .testTag(MenuScreenTestTags.TUTORIAL_BUTTON),
                        shape = NumPairsComponents.MediumShape,
                        colors = NumPairsComponents.secondaryButtonColors(),
                        border = NumPairsComponents.secondaryButtonBorder()
                    ) {
                        MenuButtonText(text = stringResource(R.string.menu_tutorial_button))
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun MenuScreenTopBar() {
    CenterAlignedTopAppBar(
        title = {
            Text(
                text = stringResource(R.string.app_name),
                color = MaterialTheme.colorScheme.primary
            )
        }
    )
}

@Composable
private fun MenuButtonText(text: String) {
    Text(
        text = text,
        style = MaterialTheme.typography.labelLarge.copy(
            fontSize = MENU_BUTTON_TEXT_SIZE,
            lineHeight = MENU_BUTTON_TEXT_LINE_HEIGHT
        )
    )
}

@Preview(showBackground = true)
@Composable
private fun MenuScreenPreview() {
    NumPairsTheme {
        MenuScreen()
    }
}

private val MENU_CONTENT_MAX_WIDTH = 360.dp
private val MENU_BUTTON_TEXT_SIZE = 22.sp
private val MENU_BUTTON_TEXT_LINE_HEIGHT = 36.sp
