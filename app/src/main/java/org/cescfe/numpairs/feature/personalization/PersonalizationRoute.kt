package org.cescfe.numpairs.feature.personalization

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import kotlinx.coroutines.launch
import org.cescfe.numpairs.data.preferences.PersonalizationPreferences
import org.cescfe.numpairs.data.preferences.PersonalizationPreferencesRepository
import org.cescfe.numpairs.feature.personalization.ui.PersonalizationScreen

@Composable
fun PersonalizationRoute(
    repository: PersonalizationPreferencesRepository,
    onNavigateBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val preferences by repository.preferences.collectAsState(initial = PersonalizationPreferences())
    val coroutineScope = rememberCoroutineScope()

    PersonalizationScreen(
        preferences = preferences,
        onThemeSelected = { theme ->
            coroutineScope.launch {
                repository.selectTheme(theme)
            }
        },
        onGeneratedGameHapticsEnabledChanged = { enabled ->
            coroutineScope.launch {
                repository.setGeneratedGameHapticsEnabled(enabled)
            }
        },
        onNavigateBack = onNavigateBack,
        modifier = modifier
    )
}
