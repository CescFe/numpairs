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
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.selected
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.stateDescription
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.unit.DpOffset
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
    onQuickSelected: () -> Unit = {},
    onTutorialSelected: () -> Unit = {},
    onPersonalizationSelected: () -> Unit = {},
    onFourPairsSelected: () -> Unit = {},
    onEightPairsSelected: () -> Unit = {},
    onFourPairsDifficultySelected: (GeneratedDifficultyMenuOptionId) -> Unit = {},
    onEightPairsDifficultySelected: (GeneratedDifficultyMenuOptionId) -> Unit = {}
) {
    var expandedDifficultyMenu by rememberSaveable {
        mutableStateOf<ExpandedDifficultyMenu?>(null)
    }

    Scaffold(
        modifier = modifier
            .fillMaxSize()
            .testTag(MenuScreenTestTags.SCREEN),
        topBar = {
            MenuScreenTopBar(onSettingsSelected = onPersonalizationSelected)
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
                    QuickMenuButton(onClick = onQuickSelected)
                    GeneratedModeMenuRow(
                        state = fourPairsMode,
                        onPlay = onFourPairsSelected,
                        expanded = expandedDifficultyMenu == ExpandedDifficultyMenu.FOUR_PAIRS,
                        onToggleDifficultyMenu = {
                            expandedDifficultyMenu = expandedDifficultyMenu.toggled(
                                ExpandedDifficultyMenu.FOUR_PAIRS
                            )
                        },
                        onDismissDifficultyMenu = {
                            expandedDifficultyMenu = null
                        },
                        onDifficultySelected = onFourPairsDifficultySelected,
                        containerTestTag = MenuScreenTestTags.FOUR_PAIRS_SPLIT_CTA,
                        playTestTag = MenuScreenTestTags.FOUR_PAIRS_BUTTON,
                        difficultyTestTag = MenuScreenTestTags.FOUR_PAIRS_DIFFICULTY_BUTTON,
                        difficultyMenuTestTag = MenuScreenTestTags.FOUR_PAIRS_DIFFICULTY_MENU
                    )
                    GeneratedModeMenuRow(
                        state = eightPairsMode,
                        onPlay = onEightPairsSelected,
                        expanded = expandedDifficultyMenu == ExpandedDifficultyMenu.EIGHT_PAIRS,
                        onToggleDifficultyMenu = {
                            expandedDifficultyMenu = expandedDifficultyMenu.toggled(
                                ExpandedDifficultyMenu.EIGHT_PAIRS
                            )
                        },
                        onDismissDifficultyMenu = {
                            expandedDifficultyMenu = null
                        },
                        onDifficultySelected = onEightPairsDifficultySelected,
                        containerTestTag = MenuScreenTestTags.EIGHT_PAIRS_SPLIT_CTA,
                        playTestTag = MenuScreenTestTags.EIGHT_PAIRS_BUTTON,
                        difficultyTestTag = MenuScreenTestTags.EIGHT_PAIRS_DIFFICULTY_BUTTON,
                        difficultyMenuTestTag = MenuScreenTestTags.EIGHT_PAIRS_DIFFICULTY_MENU
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
                }
            }
        }
    }
}

@Composable
private fun QuickMenuButton(onClick: () -> Unit) {
    val contentDescription = stringResource(
        R.string.menu_play_quick_content_description,
        stringResource(R.string.quick_screen_title),
        stringResource(R.string.three_pairs_accessibility_name),
        stringResource(R.string.generated_difficulty_low)
    )

    NumPairsComponents.PrimaryCtaButton(
        onClick = onClick,
        modifier = Modifier
            .fillMaxWidth()
            .height(NumPairsComponents.ButtonHeight)
            .semantics {
                this.contentDescription = contentDescription
            }
            .testTag(MenuScreenTestTags.QUICK_BUTTON)
    ) {
        MenuButtonText(text = stringResource(R.string.quick_screen_title))
    }
}

@Composable
private fun GeneratedModeMenuRow(
    state: GeneratedModeMenuUiState,
    onPlay: () -> Unit,
    expanded: Boolean,
    onToggleDifficultyMenu: () -> Unit,
    onDismissDifficultyMenu: () -> Unit,
    onDifficultySelected: (GeneratedDifficultyMenuOptionId) -> Unit,
    containerTestTag: String,
    playTestTag: String,
    difficultyTestTag: String,
    difficultyMenuTestTag: String
) {
    val playContentDescription = stringResource(
        R.string.menu_play_generated_challenge_content_description,
        state.challengeName
    )
    val difficultyActionContentDescription = stringResource(
        if (expanded) {
            R.string.menu_close_generated_difficulty_content_description
        } else {
            R.string.menu_choose_generated_difficulty_content_description
        },
        state.modeName
    )
    val difficultyActionStateDescription = stringResource(
        if (expanded) {
            R.string.menu_generated_difficulty_expanded
        } else {
            R.string.menu_generated_difficulty_collapsed
        }
    )

    NumPairsComponents.PrimarySplitCtaButton(
        onPrimaryClick = onPlay,
        onSecondaryClick = onToggleDifficultyMenu,
        modifier = Modifier
            .fillMaxWidth()
            .height(NumPairsComponents.ButtonHeight)
            .testTag(containerTestTag),
        primaryActionModifier = Modifier
            .semantics {
                contentDescription = playContentDescription
            }
            .testTag(playTestTag),
        secondaryActionModifier = Modifier
            .semantics {
                contentDescription = difficultyActionContentDescription
                stateDescription = difficultyActionStateDescription
            }
            .testTag(difficultyTestTag),
        primaryContentPadding = PaddingValues(
            horizontal = MENU_GENERATED_MODE_BUTTON_HORIZONTAL_PADDING,
            vertical = 0.dp
        ),
        primaryContent = {
            MenuButtonText(
                text = state.challengeName,
                overflow = TextOverflow.Ellipsis,
                maxLines = 1
            )
        },
        secondaryContent = {
            Icon(
                painter = painterResource(
                    if (expanded) {
                        R.drawable.ic_arrow_up
                    } else {
                        R.drawable.ic_arrow_down
                    }
                ),
                contentDescription = null,
                modifier = Modifier.size(MENU_GENERATED_MODE_ICON_SIZE)
            )
            GeneratedDifficultyMenu(
                state = state,
                expanded = expanded,
                onDismiss = onDismissDifficultyMenu,
                onDifficultySelected = { optionId ->
                    onDifficultySelected(optionId)
                    onDismissDifficultyMenu()
                },
                testTag = difficultyMenuTestTag
            )
        }
    )
}

@Composable
private fun GeneratedDifficultyMenu(
    state: GeneratedModeMenuUiState,
    expanded: Boolean,
    onDismiss: () -> Unit,
    onDifficultySelected: (GeneratedDifficultyMenuOptionId) -> Unit,
    testTag: String
) {
    val menuContentDescription = stringResource(
        R.string.menu_generated_difficulty_options_content_description,
        state.modeName
    )
    val selectedStateDescription = stringResource(R.string.menu_generated_difficulty_selected)

    DropdownMenu(
        expanded = expanded,
        onDismissRequest = onDismiss,
        modifier = Modifier
            .width(MENU_GENERATED_DIFFICULTY_MENU_WIDTH)
            .selectableGroup()
            .semantics {
                contentDescription = menuContentDescription
            }
            .testTag(testTag),
        offset = DpOffset(
            x = MENU_GENERATED_DIFFICULTY_MENU_END_ALIGNMENT_OFFSET,
            y = MENU_GENERATED_DIFFICULTY_MENU_VERTICAL_OFFSET
        ),
        shape = NumPairsComponents.LargeShape,
        containerColor = NumPairsComponents.raisedSurfaceColor(),
        tonalElevation = 0.dp,
        shadowElevation = 0.dp,
        border = NumPairsComponents.subtleBorder()
    ) {
        state.difficultyOptions.forEach { option ->
            val selected = option.id == state.selectedDifficultyOptionId
            DropdownMenuItem(
                text = {
                    Text(
                        text = option.label,
                        fontWeight = if (selected) FontWeight.Bold else FontWeight.Normal
                    )
                },
                onClick = {
                    onDifficultySelected(option.id)
                },
                modifier = Modifier
                    .semantics {
                        this.selected = selected
                        role = Role.RadioButton
                        if (selected) {
                            stateDescription = selectedStateDescription
                        }
                    }
                    .testTag(MenuScreenTestTags.difficultyOption(option.id)),
                trailingIcon = {
                    RadioButton(
                        selected = selected,
                        onClick = null,
                        modifier = Modifier.clearAndSetSemantics {}
                    )
                }
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun MenuScreenTopBar(onSettingsSelected: () -> Unit) {
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
        },
        actions = {
            IconButton(
                onClick = onSettingsSelected,
                modifier = Modifier.testTag(MenuScreenTestTags.SETTINGS_ACTION),
                colors = NumPairsComponents.iconButtonColors()
            ) {
                Icon(
                    painter = painterResource(R.drawable.ic_settings),
                    contentDescription = stringResource(
                        R.string.menu_settings_action_content_description
                    ),
                    modifier = Modifier.size(MENU_TOP_BAR_ACTION_ICON_SIZE)
                )
            }
        }
    )
}

@Composable
private fun MenuButtonText(text: String, overflow: TextOverflow = TextOverflow.Clip, maxLines: Int = Int.MAX_VALUE) {
    Text(
        text = text,
        overflow = overflow,
        maxLines = maxLines,
        style = MaterialTheme.typography.labelLarge.copy(
            fontWeight = FontWeight.Bold,
            fontSize = MENU_BUTTON_TEXT_SIZE,
            lineHeight = MENU_BUTTON_TEXT_LINE_HEIGHT
        )
    )
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
                ),
                difficultyOptions = listOf(
                    GeneratedDifficultyMenuOptionUiState(
                        GeneratedDifficultyMenuOptionId("four-pairs-low"),
                        stringResource(R.string.generated_difficulty_low)
                    ),
                    GeneratedDifficultyMenuOptionUiState(
                        GeneratedDifficultyMenuOptionId("four-pairs-medium"),
                        stringResource(R.string.generated_difficulty_medium)
                    )
                ),
                selectedDifficultyOptionId = GeneratedDifficultyMenuOptionId("four-pairs-low")
            ),
            eightPairsMode = GeneratedModeMenuUiState(
                modeName = eightPairsName,
                challengeName = stringResource(
                    R.string.generated_challenge_title,
                    eightPairsName,
                    stringResource(R.string.generated_difficulty_medium)
                ),
                difficultyOptions = listOf(
                    GeneratedDifficultyMenuOptionUiState(
                        GeneratedDifficultyMenuOptionId("eight-pairs-medium"),
                        stringResource(R.string.generated_difficulty_medium)
                    ),
                    GeneratedDifficultyMenuOptionUiState(
                        GeneratedDifficultyMenuOptionId("eight-pairs-hard"),
                        stringResource(R.string.generated_difficulty_hard)
                    )
                ),
                selectedDifficultyOptionId = GeneratedDifficultyMenuOptionId("eight-pairs-medium")
            )
        )
    }
}

private fun ExpandedDifficultyMenu?.toggled(requested: ExpandedDifficultyMenu): ExpandedDifficultyMenu? =
    requested.takeUnless { current -> current == this }

private enum class ExpandedDifficultyMenu {
    FOUR_PAIRS,
    EIGHT_PAIRS
}

private val MENU_CONTENT_MAX_WIDTH = 360.dp
private val MENU_BRAND_MARK_SIZE = 32.dp
private val MENU_TOP_BAR_ACTION_ICON_SIZE = 24.dp
private val MENU_BUTTON_TEXT_SIZE = 22.sp
private val MENU_BUTTON_TEXT_LINE_HEIGHT = 36.sp
private val MENU_GENERATED_MODE_BUTTON_HORIZONTAL_PADDING = 12.dp
private val MENU_GENERATED_MODE_ICON_SIZE = 24.dp
private val MENU_GENERATED_DIFFICULTY_MENU_WIDTH = 200.dp
private val MENU_GENERATED_DIFFICULTY_MENU_END_ALIGNMENT_OFFSET =
    NumPairsComponents.ButtonHeight - MENU_GENERATED_DIFFICULTY_MENU_WIDTH
private val MENU_GENERATED_DIFFICULTY_MENU_VERTICAL_OFFSET = 4.dp
