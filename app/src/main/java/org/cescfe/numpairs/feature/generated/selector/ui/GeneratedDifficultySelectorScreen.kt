package org.cescfe.numpairs.feature.generated.selector.ui

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.stateDescription
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import org.cescfe.numpairs.R
import org.cescfe.numpairs.ui.theme.NumPairsComponents
import org.cescfe.numpairs.ui.theme.NumPairsTheme
import org.cescfe.numpairs.ui.theme.numPairsSemanticColors

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GeneratedDifficultySelectorScreen(
    state: GeneratedDifficultySelectorUiState,
    onDifficultySelected: (GeneratedDifficultyOptionId) -> Unit,
    onPlay: (GeneratedDifficultyOptionId) -> Unit,
    onNavigateBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    Scaffold(
        modifier = modifier
            .fillMaxSize()
            .testTag(GeneratedDifficultySelectorTestTags.SCREEN),
        containerColor = MaterialTheme.colorScheme.background,
        contentColor = MaterialTheme.colorScheme.onBackground,
        topBar = {
            TopAppBar(
                colors = NumPairsComponents.topAppBarColors(),
                navigationIcon = {
                    IconButton(
                        onClick = onNavigateBack,
                        modifier = Modifier.testTag(GeneratedDifficultySelectorTestTags.BACK_BUTTON)
                    ) {
                        Icon(
                            painter = painterResource(R.drawable.ic_chevron_left),
                            contentDescription = stringResource(R.string.back_button_content_description)
                        )
                    }
                },
                title = {
                    Text(text = stringResource(R.string.difficulty_selector_screen_title))
                }
            )
        },
        bottomBar = {
            DifficultySelectorBottomBar(
                state = state,
                onPlay = onPlay
            )
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 24.dp, vertical = 20.dp),
            contentAlignment = Alignment.TopCenter
        ) {
            Column(
                modifier = Modifier
                    .widthIn(max = DIFFICULTY_SELECTOR_CONTENT_MAX_WIDTH)
                    .fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Text(
                    text = state.modeName,
                    modifier = Modifier.testTag(GeneratedDifficultySelectorTestTags.MODE_NAME),
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = stringResource(R.string.difficulty_selector_supporting_text),
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    style = MaterialTheme.typography.bodyLarge
                )
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .selectableGroup()
                        .testTag(GeneratedDifficultySelectorTestTags.OPTION_GROUP),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    state.options.forEach { option ->
                        DifficultyOption(
                            option = option,
                            selected = option.id == state.selectedOptionId,
                            onClick = { onDifficultySelected(option.id) }
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun DifficultyOption(option: GeneratedDifficultyOptionUiState, selected: Boolean, onClick: () -> Unit) {
    val semanticColors = MaterialTheme.numPairsSemanticColors
    val selectedDescription = stringResource(R.string.difficulty_selector_selected)

    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .defaultMinSize(minHeight = DIFFICULTY_OPTION_MIN_HEIGHT)
            .selectable(
                selected = selected,
                onClick = onClick,
                role = Role.RadioButton
            )
            .semantics {
                if (selected) {
                    stateDescription = selectedDescription
                }
            }
            .testTag(GeneratedDifficultySelectorTestTags.option(option.id)),
        color = if (selected) {
            semanticColors.selectionContainer
        } else {
            MaterialTheme.colorScheme.surface
        },
        contentColor = if (selected) {
            semanticColors.onSelectionContainer
        } else {
            MaterialTheme.colorScheme.onSurface
        },
        shape = NumPairsComponents.MediumShape,
        border = if (selected) {
            BorderStroke(NumPairsComponents.StrongBorderWidth, semanticColors.selection)
        } else {
            NumPairsComponents.subtleBorder()
        }
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 12.dp),
            horizontalArrangement = Arrangement.spacedBy(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(2.dp)
            ) {
                Text(
                    text = option.label,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                if (selected) {
                    Text(
                        text = selectedDescription,
                        modifier = Modifier.testTag(GeneratedDifficultySelectorTestTags.SELECTED_LABEL),
                        style = MaterialTheme.typography.bodySmall
                    )
                }
            }
            RadioButton(
                selected = selected,
                onClick = null,
                modifier = Modifier
                    .size(DIFFICULTY_RADIO_SIZE)
                    .clearAndSetSemantics {}
            )
        }
    }
}

@Composable
private fun DifficultySelectorBottomBar(
    state: GeneratedDifficultySelectorUiState,
    onPlay: (GeneratedDifficultyOptionId) -> Unit
) {
    Surface(
        color = MaterialTheme.colorScheme.background,
        contentColor = MaterialTheme.colorScheme.onBackground
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp, vertical = 16.dp),
            contentAlignment = Alignment.Center
        ) {
            NumPairsComponents.PrimaryCtaButton(
                onClick = { onPlay(state.selectedOptionId) },
                modifier = Modifier
                    .widthIn(max = DIFFICULTY_SELECTOR_CONTENT_MAX_WIDTH)
                    .fillMaxWidth()
                    .testTag(GeneratedDifficultySelectorTestTags.PLAY_BUTTON)
            ) {
                Text(
                    text = stringResource(
                        R.string.difficulty_selector_play_button,
                        state.modeName,
                        state.selectedOption.label
                    ),
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}

@Preview(name = "4 Pairs · Low", showBackground = true)
@Composable
private fun FourPairsDifficultySelectorPreview() {
    NumPairsTheme {
        GeneratedDifficultySelectorScreen(
            state = fourPairsPreviewState,
            onDifficultySelected = {},
            onPlay = {},
            onNavigateBack = {}
        )
    }
}

@Preview(name = "8 Pairs · Hard", showBackground = true)
@Composable
private fun EightPairsDifficultySelectorPreview() {
    NumPairsTheme {
        GeneratedDifficultySelectorScreen(
            state = eightPairsPreviewState,
            onDifficultySelected = {},
            onPlay = {},
            onNavigateBack = {}
        )
    }
}

object GeneratedDifficultySelectorTestTags {
    const val SCREEN = "generated_difficulty_selector_screen"
    const val BACK_BUTTON = "generated_difficulty_selector_back"
    const val MODE_NAME = "generated_difficulty_selector_mode"
    const val OPTION_GROUP = "generated_difficulty_selector_options"
    const val SELECTED_LABEL = "generated_difficulty_selector_selected_label"
    const val PLAY_BUTTON = "generated_difficulty_selector_play"

    fun option(id: GeneratedDifficultyOptionId): String = "generated_difficulty_option_${id.value}"
}

private val fourPairsPreviewState = GeneratedDifficultySelectorUiState(
    modeName = "4 pairs",
    options = listOf(
        GeneratedDifficultyOptionUiState(GeneratedDifficultyOptionId("low"), "Low"),
        GeneratedDifficultyOptionUiState(GeneratedDifficultyOptionId("medium"), "Medium")
    ),
    selectedOptionId = GeneratedDifficultyOptionId("low")
)

private val eightPairsPreviewState = GeneratedDifficultySelectorUiState(
    modeName = "8 pairs",
    options = listOf(
        GeneratedDifficultyOptionUiState(GeneratedDifficultyOptionId("medium"), "Medium"),
        GeneratedDifficultyOptionUiState(GeneratedDifficultyOptionId("hard"), "Hard")
    ),
    selectedOptionId = GeneratedDifficultyOptionId("hard")
)

private val DIFFICULTY_SELECTOR_CONTENT_MAX_WIDTH = 520.dp
private val DIFFICULTY_OPTION_MIN_HEIGHT = 72.dp
private val DIFFICULTY_RADIO_SIZE = 48.dp
