package org.cescfe.numpairs.data.onboarding

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.preferencesDataStore

internal const val ONBOARDING_PREFERENCES_DATA_STORE_FILE = "datastore/onboarding.preferences_pb"

private const val ONBOARDING_PREFERENCES_DATA_STORE_NAME = "onboarding"

internal val Context.onboardingPreferencesDataStore: DataStore<Preferences> by preferencesDataStore(
    name = ONBOARDING_PREFERENCES_DATA_STORE_NAME
)
