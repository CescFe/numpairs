package org.cescfe.numpairs.data.preferences

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.preferencesDataStore

private const val USER_PREFERENCES_DATA_STORE_NAME = "user_preferences"

internal val Context.userPreferencesDataStore: DataStore<Preferences> by preferencesDataStore(
    name = USER_PREFERENCES_DATA_STORE_NAME
)
