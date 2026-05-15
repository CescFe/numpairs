package org.cescfe.numpairs.feature.fourpairs.ui

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import org.cescfe.numpairs.R
import org.cescfe.numpairs.ui.theme.NumPairsTheme

@Composable
fun FourPairsScreen(modifier: Modifier = Modifier, onNavigateBack: () -> Unit = {}) {
    Scaffold(
        modifier = modifier
            .fillMaxSize()
            .testTag(FourPairsScreenTestTags.SCREEN),
        topBar = {
            FourPairsScreenTopBar(onNavigateBack = onNavigateBack)
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun FourPairsScreenTopBar(onNavigateBack: () -> Unit) {
    val backButtonContentDescription = stringResource(R.string.back_button_content_description)

    TopAppBar(
        navigationIcon = {
            IconButton(
                onClick = onNavigateBack,
                modifier = Modifier.testTag(FourPairsScreenTestTags.BACK_BUTTON)
            ) {
                Icon(
                    painter = painterResource(R.drawable.ic_chevron_left),
                    contentDescription = backButtonContentDescription
                )
            }
        },
        title = {
            Text(text = stringResource(R.string.four_pairs_screen_title))
        }
    )
}

@Preview(showBackground = true)
@Composable
private fun FourPairsScreenPreview() {
    NumPairsTheme {
        FourPairsScreen()
    }
}
