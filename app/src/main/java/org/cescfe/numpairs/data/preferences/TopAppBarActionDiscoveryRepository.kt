package org.cescfe.numpairs.data.preferences

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.emptyPreferences
import androidx.datastore.preferences.preferencesDataStore
import java.io.IOException
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map

private const val USER_PREFERENCES_DATA_STORE_NAME = "user_preferences"

private val Context.userPreferencesDataStore: DataStore<Preferences> by preferencesDataStore(
    name = USER_PREFERENCES_DATA_STORE_NAME
)

data class TopAppBarActionDiscoveryState(val hasSeenHelpAction: Boolean = false, val hasSeenHintAction: Boolean = false)

interface TopAppBarActionDiscoveryRepository {
    val discoveryState: Flow<TopAppBarActionDiscoveryState>

    suspend fun markHelpActionSeen()

    suspend fun markHintActionSeen()

    companion object {
        fun create(context: Context): TopAppBarActionDiscoveryRepository =
            DataStoreTopAppBarActionDiscoveryRepository(context.applicationContext.userPreferencesDataStore)
    }
}

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
