package org.cescfe.numpairs.data.onboarding

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class DataStoreOnboardingRepository(private val dataStore: DataStore<Preferences>) : OnboardingRepository {
    override val onboardingState: Flow<OnboardingState> = dataStore.data
        .map { preferences ->
            OnboardingState(
                lastCompletedStage = OnboardingStageCheckpoint.fromPersistedValue(
                    preferences[PreferenceKeys.LAST_COMPLETED_STAGE] ?: 0
                ),
                firstRunTutorialOutcome = FirstRunTutorialOutcome.fromPersistedValue(
                    value = preferences[PreferenceKeys.FIRST_RUN_TUTORIAL_OUTCOME]
                )
            )
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
            val currentOutcome = FirstRunTutorialOutcome.fromPersistedValue(
                preferences[PreferenceKeys.FIRST_RUN_TUTORIAL_OUTCOME]
            )
            if (!currentOutcome.isResolved) {
                preferences[PreferenceKeys.FIRST_RUN_TUTORIAL_OUTCOME] = outcome.persistedValue
            }
        }
    }

    private object PreferenceKeys {
        val LAST_COMPLETED_STAGE = intPreferencesKey("onboarding_last_completed_stage")
        val FIRST_RUN_TUTORIAL_OUTCOME = intPreferencesKey("onboarding_first_run_tutorial_outcome")
    }
}
