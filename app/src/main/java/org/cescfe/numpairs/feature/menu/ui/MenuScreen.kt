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
import java.time.LocalDate
import org.cescfe.numpairs.R
import org.cescfe.numpairs.data.preferences.PersonalizationTheme
import org.cescfe.numpairs.feature.daily.DailyRecipes
import org.cescfe.numpairs.feature.daily.localizedDailyChallengeName
import org.cescfe.numpairs.ui.theme.NumPairsComponents
import org.cescfe.numpairs.ui.theme.NumPairsTheme
import org.cescfe.numpairs.ui.theme.NumPairsThemePreviewParameterProvider

@Composable
fun MenuScreen(
    quickOption: GeneratedPlayOptionMenuUiState,
    classicOption: GeneratedPlayOptionMenuUiState,
    modifier: Modifier = Modifier,
    dailyChallenge: DailyMenuUiState? = null,
    resumeChallengeName: String? = null,
    onDailySelected: () -> Unit = {},
    onDailyCalendarSelected: () -> Unit = {},
    onResumeSelected: () -> Unit = {},
    onTutorialSelected: () -> Unit = {},
    onPersonalizationSelected: () -> Unit = {},
    onQuickSelected: () -> Unit = {},
    onClassicSelected: () -> Unit = {},
    onQuickDifficultySelected: (GeneratedDifficultyMenuOptionId) -> Unit = {},
    onClassicDifficultySelected: (GeneratedDifficultyMenuOptionId) -> Unit = {}
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
                    dailyChallenge?.let { state ->
                        DailyMenuRow(
                            state = state,
                            onPrimaryClick = onDailySelected,
                            onCalendarClick = onDailyCalendarSelected
                        )
                    }
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
                    GeneratedPlayOptionMenuRow(
                        state = quickOption,
                        onPlay = onQuickSelected,
                        expanded = expandedDifficultyMenu == ExpandedDifficultyMenu.QUICK,
                        onToggleDifficultyMenu = {
                            expandedDifficultyMenu = expandedDifficultyMenu.toggled(
                                ExpandedDifficultyMenu.QUICK
                            )
                        },
                        onDismissDifficultyMenu = {
                            expandedDifficultyMenu = null
                        },
                        onDifficultySelected = onQuickDifficultySelected,
                        containerTestTag = MenuScreenTestTags.QUICK_SPLIT_CTA,
                        playTestTag = MenuScreenTestTags.QUICK_BUTTON,
                        difficultyTestTag = MenuScreenTestTags.QUICK_DIFFICULTY_BUTTON,
                        difficultyMenuTestTag = MenuScreenTestTags.QUICK_DIFFICULTY_MENU
                    )
                    GeneratedPlayOptionMenuRow(
                        state = classicOption,
                        onPlay = onClassicSelected,
                        expanded = expandedDifficultyMenu == ExpandedDifficultyMenu.CLASSIC,
                        onToggleDifficultyMenu = {
                            expandedDifficultyMenu = expandedDifficultyMenu.toggled(
                                ExpandedDifficultyMenu.CLASSIC
                            )
                        },
                        onDismissDifficultyMenu = {
                            expandedDifficultyMenu = null
                        },
                        onDifficultySelected = onClassicDifficultySelected,
                        containerTestTag = MenuScreenTestTags.CLASSIC_SPLIT_CTA,
                        playTestTag = MenuScreenTestTags.CLASSIC_BUTTON,
                        difficultyTestTag = MenuScreenTestTags.CLASSIC_DIFFICULTY_BUTTON,
                        difficultyMenuTestTag = MenuScreenTestTags.CLASSIC_DIFFICULTY_MENU
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
private fun DailyMenuRow(state: DailyMenuUiState, onPrimaryClick: () -> Unit, onCalendarClick: () -> Unit) {
    val challengeName = state.identity.localizedDailyChallengeName()
    val label = stringResource(
        when (state) {
            is DailyMenuUiState.StartToday -> R.string.menu_daily_start_button
            is DailyMenuUiState.ContinueToday -> R.string.menu_daily_continue_button
            is DailyMenuUiState.CompletedToday -> R.string.menu_daily_completed_button
        }
    )
    val actionContentDescription = stringResource(
        when (state) {
            is DailyMenuUiState.StartToday -> R.string.menu_daily_start_content_description
            is DailyMenuUiState.ContinueToday -> R.string.menu_daily_continue_content_description
            is DailyMenuUiState.CompletedToday -> R.string.menu_daily_completed_content_description
        },
        challengeName
    )
    val actionStateDescription = stringResource(
        when (state) {
            is DailyMenuUiState.StartToday -> R.string.menu_daily_not_started_state
            is DailyMenuUiState.ContinueToday -> R.string.menu_daily_in_progress_state
            is DailyMenuUiState.CompletedToday -> R.string.menu_daily_completed_state
        }
    )
    val calendarContentDescription = stringResource(
        R.string.menu_daily_calendar_content_description
    )

    NumPairsComponents.PrimarySplitCtaButton(
        onPrimaryClick = onPrimaryClick,
        onSecondaryClick = onCalendarClick,
        modifier = Modifier
            .fillMaxWidth()
            .height(NumPairsComponents.ButtonHeight)
            .testTag(MenuScreenTestTags.DAILY_SPLIT_CTA),
        primaryActionModifier = Modifier
            .semantics {
                contentDescription = actionContentDescription
                stateDescription = actionStateDescription
                selected = state is DailyMenuUiState.CompletedToday
            }
            .testTag(MenuScreenTestTags.DAILY_BUTTON),
        secondaryActionModifier = Modifier
            .semantics {
                contentDescription = calendarContentDescription
            }
            .testTag(MenuScreenTestTags.DAILY_CALENDAR_BUTTON),
        primaryContentPadding = PaddingValues(
            horizontal = MENU_PLAY_OPTION_BUTTON_HORIZONTAL_PADDING,
            vertical = 0.dp
        ),
        primaryContent = {
            MenuButtonText(
                text = label,
                overflow = TextOverflow.Ellipsis,
                maxLines = 1
            )
        },
        secondaryContent = {
            Icon(
                painter = painterResource(R.drawable.ic_calendar),
                contentDescription = null,
                modifier = Modifier.size(MENU_PLAY_OPTION_ICON_SIZE)
            )
        }
    )
}

@Composable
private fun GeneratedPlayOptionMenuRow(
    state: GeneratedPlayOptionMenuUiState,
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
        state.selectionName
    )
    val difficultyActionContentDescription = stringResource(
        if (expanded) {
            R.string.menu_close_generated_difficulty_content_description
        } else {
            R.string.menu_choose_generated_difficulty_content_description
        },
        state.optionName
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
            horizontal = MENU_PLAY_OPTION_BUTTON_HORIZONTAL_PADDING,
            vertical = 0.dp
        ),
        primaryContent = {
            MenuButtonText(
                text = state.selectionName,
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
                modifier = Modifier.size(MENU_PLAY_OPTION_ICON_SIZE)
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
    state: GeneratedPlayOptionMenuUiState,
    expanded: Boolean,
    onDismiss: () -> Unit,
    onDifficultySelected: (GeneratedDifficultyMenuOptionId) -> Unit,
    testTag: String
) {
    val menuContentDescription = stringResource(
        R.string.menu_generated_difficulty_options_content_description,
        state.optionName
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
        val quickName = stringResource(R.string.quick_screen_title)
        val classicName = stringResource(R.string.classic_screen_title)
        MenuScreen(
            dailyChallenge = DailyMenuUiState.StartToday(
                identity = DailyRecipes.FOUR_PAIRS_LOW_V1.identityFor(
                    LocalDate.of(2026, 7, 25)
                )
            ),
            quickOption = GeneratedPlayOptionMenuUiState(
                optionName = quickName,
                selectionName = stringResource(
                    R.string.generated_challenge_title,
                    quickName,
                    stringResource(R.string.generated_difficulty_low)
                ),
                difficultyOptions = listOf(
                    GeneratedDifficultyMenuOptionUiState(
                        GeneratedDifficultyMenuOptionId("low"),
                        stringResource(R.string.generated_difficulty_low)
                    ),
                    GeneratedDifficultyMenuOptionUiState(
                        GeneratedDifficultyMenuOptionId("medium"),
                        stringResource(R.string.generated_difficulty_medium)
                    )
                ),
                selectedDifficultyOptionId = GeneratedDifficultyMenuOptionId("low")
            ),
            classicOption = GeneratedPlayOptionMenuUiState(
                optionName = classicName,
                selectionName = stringResource(
                    R.string.generated_challenge_title,
                    classicName,
                    stringResource(R.string.generated_difficulty_medium)
                ),
                difficultyOptions = listOf(
                    GeneratedDifficultyMenuOptionUiState(
                        GeneratedDifficultyMenuOptionId("medium"),
                        stringResource(R.string.generated_difficulty_medium)
                    ),
                    GeneratedDifficultyMenuOptionUiState(
                        GeneratedDifficultyMenuOptionId("hard"),
                        stringResource(R.string.generated_difficulty_hard)
                    )
                ),
                selectedDifficultyOptionId = GeneratedDifficultyMenuOptionId("medium")
            )
        )
    }
}

private fun ExpandedDifficultyMenu?.toggled(requested: ExpandedDifficultyMenu): ExpandedDifficultyMenu? =
    requested.takeUnless { current -> current == this }

private enum class ExpandedDifficultyMenu {
    QUICK,
    CLASSIC
}

private val MENU_CONTENT_MAX_WIDTH = 360.dp
private val MENU_BRAND_MARK_SIZE = 32.dp
private val MENU_TOP_BAR_ACTION_ICON_SIZE = 24.dp
private val MENU_BUTTON_TEXT_SIZE = 22.sp
private val MENU_BUTTON_TEXT_LINE_HEIGHT = 36.sp
private val MENU_PLAY_OPTION_BUTTON_HORIZONTAL_PADDING = 12.dp
private val MENU_PLAY_OPTION_ICON_SIZE = 24.dp
private val MENU_GENERATED_DIFFICULTY_MENU_WIDTH = 200.dp
private val MENU_GENERATED_DIFFICULTY_MENU_END_ALIGNMENT_OFFSET =
    NumPairsComponents.ButtonHeight - MENU_GENERATED_DIFFICULTY_MENU_WIDTH
private val MENU_GENERATED_DIFFICULTY_MENU_VERTICAL_OFFSET = 4.dp
