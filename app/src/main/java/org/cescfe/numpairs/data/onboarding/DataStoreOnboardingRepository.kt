package org.cescfe.numpairs.data.onboarding

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class DataStoreOnboardingRepository(private val dataStore: DataStore<Preferences>) : OnboardingRepository {
    override val onboardingState: Flow<OnboardingState> = dataStore.data
        .map { preferences ->
            val completedVersion = preferences[PreferenceKeys.COMPLETED_VERSION] ?: 0
            OnboardingState(
                isInitialized = preferences[PreferenceKeys.IS_INITIALIZED] ?: false,
                completedVersion = completedVersion,
                lastCompletedStage = OnboardingStageCheckpoint.fromPersistedValue(
                    preferences[PreferenceKeys.LAST_COMPLETED_STAGE] ?: 0
                ),
                firstRunTutorialOutcome = FirstRunTutorialOutcome.fromPersistedValue(
                    value = preferences[PreferenceKeys.FIRST_RUN_TUTORIAL_OUTCOME],
                    completedVersion = completedVersion
                )
            )
        }

    override suspend fun initialize(installationKind: OnboardingInstallationKind) {
        dataStore.edit { preferences ->
            if (preferences[PreferenceKeys.IS_INITIALIZED] == true) {
                return@edit
            }

            preferences[PreferenceKeys.IS_INITIALIZED] = true
            when (installationKind) {
                OnboardingInstallationKind.FRESH_INSTALL -> {
                    preferences[PreferenceKeys.COMPLETED_VERSION] = 0
                    preferences[PreferenceKeys.FIRST_RUN_TUTORIAL_OUTCOME] =
                        FirstRunTutorialOutcome.UNRESOLVED.persistedValue
                }
                OnboardingInstallationKind.PRE_V6_UPGRADE -> {
                    preferences[PreferenceKeys.COMPLETED_VERSION] = REQUIRED_ONBOARDING_VERSION
                    preferences[PreferenceKeys.FIRST_RUN_TUTORIAL_OUTCOME] =
                        FirstRunTutorialOutcome.PRE_V6_UPGRADE.persistedValue
                }
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

    override suspend fun markTutorialCompleted() {
        resolveFirstRun(outcome = FirstRunTutorialOutcome.COMPLETED)
    }

    override suspend fun markTutorialSkipped() {
        resolveFirstRun(outcome = FirstRunTutorialOutcome.SKIPPED)
    }

    private suspend fun resolveFirstRun(outcome: FirstRunTutorialOutcome) {
        require(outcome == FirstRunTutorialOutcome.COMPLETED || outcome == FirstRunTutorialOutcome.SKIPPED) {
            "First-run Tutorial can be resolved only by completion or explicit skip."
        }

        dataStore.edit { preferences ->
            val currentVersion = preferences[PreferenceKeys.COMPLETED_VERSION] ?: 0
            if (currentVersion < REQUIRED_ONBOARDING_VERSION) {
                preferences[PreferenceKeys.COMPLETED_VERSION] = REQUIRED_ONBOARDING_VERSION
                preferences[PreferenceKeys.FIRST_RUN_TUTORIAL_OUTCOME] = outcome.persistedValue
            }
        }
    }

    private object PreferenceKeys {
        val IS_INITIALIZED = booleanPreferencesKey("onboarding_is_initialized")
        val COMPLETED_VERSION = intPreferencesKey("onboarding_completed_version")
        val LAST_COMPLETED_STAGE = intPreferencesKey("onboarding_last_completed_stage")
        val FIRST_RUN_TUTORIAL_OUTCOME = intPreferencesKey("onboarding_first_run_tutorial_outcome")
    }
}
