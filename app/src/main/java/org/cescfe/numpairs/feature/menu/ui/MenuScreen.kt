package org.cescfe.numpairs.feature.menu.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import org.cescfe.numpairs.R
import org.cescfe.numpairs.data.preferences.PersonalizationTheme
import org.cescfe.numpairs.ui.theme.NumPairsComponents
import org.cescfe.numpairs.ui.theme.NumPairsTheme
import org.cescfe.numpairs.ui.theme.NumPairsThemePreviewParameterProvider

@Composable
fun MenuScreen(
    fourPairsMode: GeneratedModeMenuUiState,
    eightPairsMode: GeneratedModeMenuUiState,
    modifier: Modifier = Modifier,
    resumeChallengeName: String? = null,
    onResumeSelected: () -> Unit = {},
    onTutorialSelected: () -> Unit = {},
    onPersonalizationSelected: () -> Unit = {},
    onFourPairsSelected: () -> Unit = {},
    onEightPairsSelected: () -> Unit = {},
    onFourPairsDifficultySelected: () -> Unit = {},
    onEightPairsDifficultySelected: () -> Unit = {}
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
                .verticalScroll(rememberScrollState())
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
                    resumeChallengeName?.let { challengeName ->
                        val resumeContentDescription = stringResource(
                            R.string.menu_resume_content_description,
                            challengeName
                        )
                        NumPairsComponents.PrimaryCtaButton(
                            onClick = onResumeSelected,
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(NumPairsComponents.ButtonHeight)
                                .semantics {
                                    contentDescription = resumeContentDescription
                                }
                                .testTag(MenuScreenTestTags.RESUME_BUTTON)
                        ) {
                            MenuButtonText(text = stringResource(R.string.menu_resume_button))
                        }
                    }
                    GeneratedModeMenuRow(
                        state = fourPairsMode,
                        onPlay = onFourPairsSelected,
                        onChooseDifficulty = onFourPairsDifficultySelected,
                        playTestTag = MenuScreenTestTags.FOUR_PAIRS_BUTTON,
                        difficultyTestTag = MenuScreenTestTags.FOUR_PAIRS_DIFFICULTY_BUTTON
                    )
                    GeneratedModeMenuRow(
                        state = eightPairsMode,
                        onPlay = onEightPairsSelected,
                        onChooseDifficulty = onEightPairsDifficultySelected,
                        playTestTag = MenuScreenTestTags.EIGHT_PAIRS_BUTTON,
                        difficultyTestTag = MenuScreenTestTags.EIGHT_PAIRS_DIFFICULTY_BUTTON
                    )
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
                    Button(
                        onClick = onPersonalizationSelected,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(NumPairsComponents.ButtonHeight)
                            .testTag(MenuScreenTestTags.PERSONALIZATION_BUTTON),
                        shape = NumPairsComponents.MediumShape,
                        colors = NumPairsComponents.secondaryButtonColors(),
                        border = NumPairsComponents.secondaryButtonBorder()
                    ) {
                        MenuButtonText(text = stringResource(R.string.menu_personalization_button))
                    }
                }
            }
        }
    }
}

@Composable
private fun GeneratedModeMenuRow(
    state: GeneratedModeMenuUiState,
    onPlay: () -> Unit,
    onChooseDifficulty: () -> Unit,
    playTestTag: String,
    difficultyTestTag: String
) {
    val playContentDescription = stringResource(
        R.string.menu_play_generated_challenge_content_description,
        state.challengeName
    )
    val chooseDifficultyContentDescription = stringResource(
        R.string.menu_choose_generated_difficulty_content_description,
        state.modeName
    )

    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(MENU_GENERATED_MODE_ACTION_SPACING)
    ) {
        NumPairsComponents.PrimaryCtaButton(
            onClick = onPlay,
            modifier = Modifier
                .weight(1f)
                .height(NumPairsComponents.ButtonHeight)
                .semantics {
                    contentDescription = playContentDescription
                }
                .testTag(playTestTag),
            contentPadding = PaddingValues(
                horizontal = MENU_GENERATED_MODE_BUTTON_HORIZONTAL_PADDING,
                vertical = 0.dp
            )
        ) {
            Text(
                text = state.challengeName,
                overflow = TextOverflow.Ellipsis,
                maxLines = 1,
                style = MaterialTheme.typography.labelLarge.copy(
                    fontWeight = FontWeight.Bold,
                    fontSize = MENU_GENERATED_MODE_BUTTON_TEXT_SIZE,
                    lineHeight = MENU_GENERATED_MODE_BUTTON_TEXT_LINE_HEIGHT
                )
            )
        }
        Button(
            onClick = onChooseDifficulty,
            modifier = Modifier
                .size(NumPairsComponents.ButtonHeight)
                .semantics {
                    contentDescription = chooseDifficultyContentDescription
                }
                .testTag(difficultyTestTag),
            shape = NumPairsComponents.MediumShape,
            colors = NumPairsComponents.secondaryButtonColors(),
            border = NumPairsComponents.secondaryButtonBorder(),
            contentPadding = PaddingValues(0.dp)
        ) {
            Icon(
                painter = painterResource(R.drawable.ic_menu),
                contentDescription = null,
                modifier = Modifier.size(MENU_GENERATED_MODE_ICON_SIZE)
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun MenuScreenTopBar() {
    CenterAlignedTopAppBar(
        title = {
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                NumPairsComponents.BrandMark(modifier = Modifier.size(MENU_BRAND_MARK_SIZE))
                Text(
                    text = stringResource(R.string.app_name),
                    color = MaterialTheme.colorScheme.primary
                )
            }
        }
    )
}

@Composable
private fun MenuButtonText(text: String) {
    Text(
        text = text,
        style = MaterialTheme.typography.labelLarge.copy(
            fontWeight = FontWeight.Bold,
            fontSize = MENU_BUTTON_TEXT_SIZE,
            lineHeight = MENU_BUTTON_TEXT_LINE_HEIGHT
        )
    )
}

data class GeneratedModeMenuUiState(val modeName: String, val challengeName: String) {
    init {
        require(modeName.isNotBlank()) {
            "Generated mode Menu name must not be blank."
        }
        require(challengeName.isNotBlank()) {
            "Generated challenge Menu name must not be blank."
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun MenuScreenPreview(
    @PreviewParameter(NumPairsThemePreviewParameterProvider::class) theme: PersonalizationTheme
) {
    NumPairsTheme(theme = theme) {
        val fourPairsName = stringResource(R.string.four_pairs_screen_title)
        val eightPairsName = stringResource(R.string.eight_pairs_screen_title)
        MenuScreen(
            fourPairsMode = GeneratedModeMenuUiState(
                modeName = fourPairsName,
                challengeName = stringResource(
                    R.string.generated_challenge_title,
                    fourPairsName,
                    stringResource(R.string.generated_difficulty_low)
                )
            ),
            eightPairsMode = GeneratedModeMenuUiState(
                modeName = eightPairsName,
                challengeName = stringResource(
                    R.string.generated_challenge_title,
                    eightPairsName,
                    stringResource(R.string.generated_difficulty_medium)
                )
            )
        )
    }
}

private val MENU_CONTENT_MAX_WIDTH = 360.dp
private val MENU_BRAND_MARK_SIZE = 32.dp
private val MENU_BUTTON_TEXT_SIZE = 22.sp
private val MENU_BUTTON_TEXT_LINE_HEIGHT = 36.sp
private val MENU_GENERATED_MODE_BUTTON_TEXT_SIZE = 18.sp
private val MENU_GENERATED_MODE_BUTTON_TEXT_LINE_HEIGHT = 24.sp
private val MENU_GENERATED_MODE_BUTTON_HORIZONTAL_PADDING = 12.dp
private val MENU_GENERATED_MODE_ACTION_SPACING = 8.dp
private val MENU_GENERATED_MODE_ICON_SIZE = 24.dp
