package org.cescfe.numpairs.data.preferences

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.emptyPreferences
import java.io.IOException
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map

class DataStoreTopAppBarActionDiscoveryRepository(private val dataStore: DataStore<Preferences>) :
    TopAppBarActionDiscoveryRepository {
    override val discoveryState: Flow<TopAppBarActionDiscoveryState> = dataStore.data
        .catch { throwable ->
            if (throwable is IOException) {
                emit(emptyPreferences())
            } else {
                throw throwable
            }
        }
        .map { preferences ->
            TopAppBarActionDiscoveryState(
                hasSeenHelpAction = preferences[PreferenceKeys.HAS_SEEN_HELP_ACTION] ?: false,
                hasSeenHintAction = preferences[PreferenceKeys.HAS_SEEN_HINT_ACTION] ?: false
            )
        }

    override suspend fun markHelpActionSeen() {
        dataStore.edit { preferences ->
            preferences[PreferenceKeys.HAS_SEEN_HELP_ACTION] = true
        }
    }

    override suspend fun markHintActionSeen() {
        dataStore.edit { preferences ->
            preferences[PreferenceKeys.HAS_SEEN_HINT_ACTION] = true
        }
    }

    private object PreferenceKeys {
        val HAS_SEEN_HELP_ACTION = booleanPreferencesKey("has_seen_help_action")
        val HAS_SEEN_HINT_ACTION = booleanPreferencesKey("has_seen_hint_action")
    }
}
