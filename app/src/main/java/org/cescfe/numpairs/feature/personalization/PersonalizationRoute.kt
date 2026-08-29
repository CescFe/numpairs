package org.cescfe.numpairs.feature.personalization

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import kotlinx.coroutines.launch
import org.cescfe.numpairs.data.preferences.PersonalizationPreferences
import org.cescfe.numpairs.data.preferences.PersonalizationPreferencesRepository
import org.cescfe.numpairs.feature.personalization.ui.PersonalizationScreen

@Composable
fun PersonalizationRoute(
    repository: PersonalizationPreferencesRepository,
    onNavigateBack: () -> Unit,
    modifier: Modifier = Modifier,
    externalUriLauncher: ExternalUriLauncher? = null
) {
    val preferences by repository.preferences.collectAsState(initial = PersonalizationPreferences())
    val coroutineScope = rememberCoroutineScope()
    val context = LocalContext.current
    val defaultExternalUriLauncher = remember(context) {
        AndroidExternalUriLauncher(context)
    }
    val activeExternalUriLauncher = externalUriLauncher ?: defaultExternalUriLauncher

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
        onOpenSourceRepositorySelected = {
            activeExternalUriLauncher.launch(OPEN_SOURCE_REPOSITORY_URL)
        },
        onPrivacyPolicySelected = {
            activeExternalUriLauncher.launch(PRIVACY_POLICY_URL)
        },
        onNavigateBack = onNavigateBack,
        modifier = modifier
    )
}
