package org.cescfe.numpairs.feature.personalization.ui

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.selection.toggleable
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.stateDescription
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import org.cescfe.numpairs.R
import org.cescfe.numpairs.data.preferences.PersonalizationPreferences
import org.cescfe.numpairs.data.preferences.PersonalizationTheme
import org.cescfe.numpairs.ui.theme.NumPairsComponents
import org.cescfe.numpairs.ui.theme.NumPairsTheme
import org.cescfe.numpairs.ui.theme.NumPairsThemePreviewColors
import org.cescfe.numpairs.ui.theme.numPairsSemanticColors
import org.cescfe.numpairs.ui.theme.previewColors

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PersonalizationScreen(
    preferences: PersonalizationPreferences,
    onThemeSelected: (PersonalizationTheme) -> Unit,
    onGeneratedGameHapticsEnabledChanged: (Boolean) -> Unit,
    onNavigateBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    Scaffold(
        modifier = modifier
            .fillMaxSize()
            .testTag(PersonalizationScreenTestTags.SCREEN),
        containerColor = MaterialTheme.colorScheme.background,
        contentColor = MaterialTheme.colorScheme.onBackground,
        topBar = {
            TopAppBar(
                colors = NumPairsComponents.topAppBarColors(),
                navigationIcon = {
                    IconButton(
                        onClick = onNavigateBack,
                        modifier = Modifier.testTag(PersonalizationScreenTestTags.BACK_BUTTON)
                    ) {
                        Icon(
                            painter = painterResource(R.drawable.ic_chevron_left),
                            contentDescription = stringResource(R.string.back_button_content_description)
                        )
                    }
                },
                title = {
                    Text(text = stringResource(R.string.personalization_screen_title))
                }
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 24.dp, vertical = 20.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .widthIn(max = PERSONALIZATION_CONTENT_MAX_WIDTH),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                BrandHeader()
                Text(
                    text = stringResource(R.string.personalization_theme_section_title),
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = stringResource(R.string.personalization_theme_supporting_text),
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    style = MaterialTheme.typography.bodyMedium
                )
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .selectableGroup(),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    PersonalizationTheme.entries.forEach { theme ->
                        ThemeOption(
                            theme = theme,
                            selected = preferences.selectedTheme == theme,
                            onClick = { onThemeSelected(theme) }
                        )
                    }
                }
                Text(
                    text = stringResource(R.string.personalization_feedback_section_title),
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )
                HapticsPreference(
                    enabled = preferences.generatedGameHapticsEnabled,
                    onEnabledChanged = onGeneratedGameHapticsEnabledChanged
                )
            }
        }
    }
}

@Composable
private fun BrandHeader() {
    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        NumPairsComponents.BrandMark(
            modifier = Modifier
                .size(PERSONALIZATION_BRAND_MARK_SIZE)
                .testTag(PersonalizationScreenTestTags.BRAND_MARK),
            contentDescription = stringResource(R.string.personalization_brand_content_description)
        )
        Text(
            text = stringResource(R.string.app_name),
            color = MaterialTheme.colorScheme.primary,
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold
        )
    }
}

@Composable
private fun ThemeOption(theme: PersonalizationTheme, selected: Boolean, onClick: () -> Unit) {
    val semanticColors = MaterialTheme.numPairsSemanticColors
    val selectedDescription = stringResource(R.string.personalization_theme_selected)
    val colors = theme.previewColors()

    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .defaultMinSize(minHeight = THEME_OPTION_MIN_HEIGHT)
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
            .testTag(PersonalizationScreenTestTags.themeOption(theme)),
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
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = theme.localizedName(),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                if (selected) {
                    Text(
                        text = selectedDescription,
                        style = MaterialTheme.typography.bodySmall
                    )
                }
            }
            ThemePreview(colors = colors)
        }
    }
}

@Composable
private fun ThemePreview(colors: NumPairsThemePreviewColors) {
    Row(
        modifier = Modifier.testTag(PersonalizationScreenTestTags.THEME_PREVIEW),
        horizontalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        listOf(
            colors.background,
            colors.surface,
            colors.primary,
            colors.accent
        ).forEach { color ->
            Box(
                modifier = Modifier
                    .size(THEME_PREVIEW_SWATCH_SIZE)
                    .background(color, CircleShape)
                    .border(1.dp, colors.outline, CircleShape)
            )
        }
    }
}

@Composable
private fun HapticsPreference(enabled: Boolean, onEnabledChanged: (Boolean) -> Unit) {
    val stateDescriptionText = stringResource(
        if (enabled) {
            R.string.personalization_haptics_enabled
        } else {
            R.string.personalization_haptics_disabled
        }
    )

    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .defaultMinSize(minHeight = HAPTICS_OPTION_MIN_HEIGHT)
            .toggleable(
                value = enabled,
                role = Role.Switch,
                onValueChange = onEnabledChanged
            )
            .semantics(mergeDescendants = true) {
                stateDescription = stateDescriptionText
            }
            .testTag(PersonalizationScreenTestTags.HAPTICS_TOGGLE),
        shape = NumPairsComponents.MediumShape,
        color = MaterialTheme.colorScheme.surface,
        contentColor = MaterialTheme.colorScheme.onSurface,
        border = NumPairsComponents.subtleBorder()
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
                    text = stringResource(R.string.personalization_haptics_title),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = stringResource(R.string.personalization_haptics_supporting_text),
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    style = MaterialTheme.typography.bodySmall
                )
            }
            Switch(
                checked = enabled,
                onCheckedChange = null
            )
        }
    }
}

@Composable
private fun PersonalizationTheme.localizedName(): String = stringResource(
    when (this) {
        PersonalizationTheme.WARM -> R.string.personalization_theme_warm
        PersonalizationTheme.FROST -> R.string.personalization_theme_frost
        PersonalizationTheme.OBSIDIAN -> R.string.personalization_theme_obsidian
        PersonalizationTheme.TERMINAL -> R.string.personalization_theme_terminal
        PersonalizationTheme.EMBER -> R.string.personalization_theme_ember
    }
)

@Preview(showBackground = true)
@Composable
private fun PersonalizationScreenPreview() {
    NumPairsTheme {
        PersonalizationScreen(
            preferences = PersonalizationPreferences(),
            onThemeSelected = {},
            onGeneratedGameHapticsEnabledChanged = {},
            onNavigateBack = {}
        )
    }
}

object PersonalizationScreenTestTags {
    const val SCREEN = "personalization_screen"
    const val BACK_BUTTON = "personalization_back_button"
    const val BRAND_MARK = "personalization_brand_mark"
    const val THEME_PREVIEW = "personalization_theme_preview"
    const val HAPTICS_TOGGLE = "personalization_haptics_toggle"

    fun themeOption(theme: PersonalizationTheme): String = "personalization_theme_${theme.name.lowercase()}"
}

private val PERSONALIZATION_CONTENT_MAX_WIDTH = 520.dp
private val PERSONALIZATION_BRAND_MARK_SIZE = 72.dp
private val THEME_OPTION_MIN_HEIGHT = 72.dp
private val THEME_PREVIEW_SWATCH_SIZE = 22.dp
private val HAPTICS_OPTION_MIN_HEIGHT = 80.dp
