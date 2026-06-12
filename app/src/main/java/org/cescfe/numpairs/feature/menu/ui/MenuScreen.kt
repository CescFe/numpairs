package org.cescfe.numpairs.feature.menu.ui

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import org.cescfe.numpairs.R
import org.cescfe.numpairs.ui.theme.NumPairsTheme

@Composable
fun MenuScreen(
    modifier: Modifier = Modifier,
    onLearnBasicsTutorialSelected: () -> Unit = {},
    onPracticeFullPuzzleTutorialSelected: () -> Unit = {},
    onFourPairsSelected: () -> Unit = {}
) {
    var isTutorialSubmenuExpanded by rememberSaveable { mutableStateOf(false) }

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
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedButton(
                        onClick = {
                            isTutorialSubmenuExpanded = !isTutorialSubmenuExpanded
                        },
                        modifier = Modifier.testTag(MenuScreenTestTags.TUTORIAL_BUTTON),
                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary)
                    ) {
                        Text(
                            text = stringResource(R.string.menu_tutorial_button),
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                    if (isTutorialSubmenuExpanded) {
                        FilledTonalButton(
                            onClick = onLearnBasicsTutorialSelected,
                            modifier = Modifier.testTag(MenuScreenTestTags.TUTORIAL_LEARN_BASICS_BUTTON)
                        ) {
                            Text(text = stringResource(R.string.menu_tutorial_learn_basics_button))
                        }
                        FilledTonalButton(
                            onClick = onPracticeFullPuzzleTutorialSelected,
                            modifier = Modifier.testTag(MenuScreenTestTags.TUTORIAL_PRACTICE_FULL_PUZZLE_BUTTON)
                        ) {
                            Text(text = stringResource(R.string.menu_tutorial_practice_full_puzzle_button))
                        }
                    }
                }
                OutlinedButton(
                    onClick = onFourPairsSelected,
                    modifier = Modifier.testTag(MenuScreenTestTags.FOUR_PAIRS_BUTTON),
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary)
                ) {
                    Text(
                        text = stringResource(R.string.menu_four_pairs_button),
                        color = MaterialTheme.colorScheme.primary
                    )
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
