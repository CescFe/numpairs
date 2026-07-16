package org.cescfe.numpairs.data.onboarding

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.emptyPreferences
import androidx.datastore.preferences.core.intPreferencesKey
import java.io.IOException
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map

class DataStoreOnboardingRepository(private val dataStore: DataStore<Preferences>) : OnboardingRepository {
    override val onboardingState: Flow<OnboardingState> = dataStore.data
        .catch { throwable ->
            if (throwable is IOException) {
                emit(emptyPreferences())
            } else {
                throw throwable
            }
        }
        .map { preferences ->
            OnboardingState(
                isInitialized = preferences[PreferenceKeys.IS_INITIALIZED] ?: false,
                completedVersion = preferences[PreferenceKeys.COMPLETED_VERSION] ?: 0,
                lastCompletedStage = OnboardingStageCheckpoint.fromPersistedValue(
                    preferences[PreferenceKeys.LAST_COMPLETED_STAGE] ?: 0
                )
            )
        }

    override suspend fun initialize(installationKind: OnboardingInstallationKind) {
        dataStore.edit { preferences ->
            if (preferences[PreferenceKeys.IS_INITIALIZED] == true) {
                return@edit
            }

            preferences[PreferenceKeys.IS_INITIALIZED] = true
            preferences[PreferenceKeys.COMPLETED_VERSION] = when (installationKind) {
                OnboardingInstallationKind.FRESH_INSTALL -> 0
                OnboardingInstallationKind.PRE_V6_UPGRADE -> REQUIRED_ONBOARDING_VERSION
            }
            preferences[PreferenceKeys.LAST_COMPLETED_STAGE] = OnboardingStageCheckpoint.NONE.persistedValue
        }
    }

    override suspend fun recordStageCompleted(stage: OnboardingStageCheckpoint) {
        require(stage != OnboardingStageCheckpoint.NONE) {
            "A completed onboarding stage cannot be NONE."
        }

        dataStore.edit { preferences ->
            val currentValue = preferences[PreferenceKeys.LAST_COMPLETED_STAGE] ?: 0
            preferences[PreferenceKeys.LAST_COMPLETED_STAGE] = maxOf(currentValue, stage.persistedValue)
        }
    }

    override suspend fun markRequiredVersionCompleted() {
        dataStore.edit { preferences ->
            val currentVersion = preferences[PreferenceKeys.COMPLETED_VERSION] ?: 0
            preferences[PreferenceKeys.COMPLETED_VERSION] = maxOf(currentVersion, REQUIRED_ONBOARDING_VERSION)
        }
    }

    private object PreferenceKeys {
        val IS_INITIALIZED = booleanPreferencesKey("onboarding_is_initialized")
        val COMPLETED_VERSION = intPreferencesKey("onboarding_completed_version")
        val LAST_COMPLETED_STAGE = intPreferencesKey("onboarding_last_completed_stage")
    }
}
