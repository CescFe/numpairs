package org.cescfe.numpairs.data.preferences

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.emptyPreferences
import androidx.datastore.preferences.core.stringPreferencesKey
import java.io.IOException
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map

class DataStorePersonalizationPreferencesRepository(private val dataStore: DataStore<Preferences>) :
    PersonalizationPreferencesRepository {
    override val preferences: Flow<PersonalizationPreferences> = dataStore.data
        .catch { throwable ->
            if (throwable is IOException) {
                emit(emptyPreferences())
            } else {
                throw throwable
            }
        }
        .map { preferences ->
            PersonalizationPreferences(
                selectedTheme = PersonalizationTheme.fromPersistedValue(
                    preferences[PreferenceKeys.SELECTED_THEME]
                ),
                generatedGameHapticsEnabled = preferences[PreferenceKeys.GENERATED_GAME_HAPTICS_ENABLED] ?: true
            )
        }

    override suspend fun selectTheme(theme: PersonalizationTheme) {
        dataStore.edit { preferences ->
            preferences[PreferenceKeys.SELECTED_THEME] = theme.persistedValue
        }
    }

    override suspend fun setGeneratedGameHapticsEnabled(enabled: Boolean) {
        dataStore.edit { preferences ->
            preferences[PreferenceKeys.GENERATED_GAME_HAPTICS_ENABLED] = enabled
        }
    }

    private object PreferenceKeys {
        val SELECTED_THEME = stringPreferencesKey("personalization_selected_theme")
        val GENERATED_GAME_HAPTICS_ENABLED = booleanPreferencesKey("personalization_generated_game_haptics_enabled")
    }
}
