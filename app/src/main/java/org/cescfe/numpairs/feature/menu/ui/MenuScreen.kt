package org.cescfe.numpairs.feature.menu.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import org.cescfe.numpairs.R
import org.cescfe.numpairs.ui.theme.NumPairsTheme

@Composable
fun MenuScreen(modifier: Modifier = Modifier, onTutorialSelected: () -> Unit = {}) {
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
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Text(
                    text = stringResource(R.string.menu_title),
                    style = MaterialTheme.typography.headlineSmall,
                    textAlign = TextAlign.Center
                )
                Text(
                    text = stringResource(R.string.menu_supporting_text),
                    style = MaterialTheme.typography.bodyLarge,
                    textAlign = TextAlign.Center
                )
                Button(
                    onClick = onTutorialSelected,
                    modifier = Modifier.testTag(MenuScreenTestTags.TUTORIAL_BUTTON)
                ) {
                    Text(text = stringResource(R.string.menu_tutorial_button))
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun MenuScreenTopBar() {
    TopAppBar(
        title = {
            Text(text = stringResource(R.string.app_name))
        }
    )
}

@Preview(showBackground = true)
@Composable
private fun MenuScreenPreview() {
    NumPairsTheme {
        MenuScreen()
    }
}
