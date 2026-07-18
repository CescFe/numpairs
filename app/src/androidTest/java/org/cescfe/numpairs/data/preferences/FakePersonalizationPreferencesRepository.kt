package org.cescfe.numpairs.data.preferences

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

class FakePersonalizationPreferencesRepository(
    initialPreferences: PersonalizationPreferences = PersonalizationPreferences()
) : PersonalizationPreferencesRepository {
    private val mutablePreferences = MutableStateFlow(initialPreferences)

    val state: StateFlow<PersonalizationPreferences> = mutablePreferences.asStateFlow()

    override val preferences = state

    override suspend fun selectTheme(theme: PersonalizationTheme) {
        mutablePreferences.update { preferences ->
            preferences.copy(selectedTheme = theme)
        }
    }

    override suspend fun setGeneratedGameHapticsEnabled(enabled: Boolean) {
        mutablePreferences.update { preferences ->
            preferences.copy(generatedGameHapticsEnabled = enabled)
        }
    }
}
