package org.cescfe.numpairs.data.preferences

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.PreferenceDataStoreFactory
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import java.io.File
import java.util.UUID
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancelAndJoin
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TemporaryFolder

class DataStorePersonalizationPreferencesRepositoryTest {
    @get:Rule
    val temporaryFolder = TemporaryFolder()

    private val dataStoreJobs = mutableListOf<Job>()

    @After
    fun tearDown() {
        dataStoreJobs.forEach(Job::cancel)
    }

    @Test
    fun freshPreferencesDefaultToWarmWithGeneratedGameHapticsEnabled() = runBlocking {
        val fixture = createRepository()

        assertEquals(
            PersonalizationPreferences(
                selectedTheme = PersonalizationTheme.WARM,
                generatedGameHapticsEnabled = true
            ),
            fixture.repository.preferences.first()
        )
    }

    @Test
    fun everyThemeIdentityRoundTripsThroughItsPersistedValue() {
        PersonalizationTheme.entries.forEach { theme ->
            assertEquals(
                theme,
                PersonalizationTheme.fromPersistedValue(theme.persistedValue)
            )
        }
    }

    @Test
    fun themeAndHapticsPersistIndependentlyAcrossRepositoryInstances() = runBlocking {
        val dataStoreFile = createDataStoreFile()
        val firstFixture = createRepository(dataStoreFile)

        firstFixture.repository.selectTheme(PersonalizationTheme.TERMINAL)
        firstFixture.repository.setGeneratedGameHapticsEnabled(false)
        firstFixture.close()

        val secondFixture = createRepository(dataStoreFile)

        assertEquals(
            PersonalizationPreferences(
                selectedTheme = PersonalizationTheme.TERMINAL,
                generatedGameHapticsEnabled = false
            ),
            secondFixture.repository.preferences.first()
        )
    }

    @Test
    fun unknownStoredThemeFallsBackToWarmWithoutChangingHaptics() = runBlocking {
        val fixture = createRepository()
        fixture.dataStore.edit { preferences ->
            preferences[stringPreferencesKey("personalization_selected_theme")] = "future-theme"
        }
        fixture.repository.setGeneratedGameHapticsEnabled(false)

        assertEquals(
            PersonalizationPreferences(
                selectedTheme = PersonalizationTheme.WARM,
                generatedGameHapticsEnabled = false
            ),
            fixture.repository.preferences.first()
        )
    }

    @Test
    fun selectingThemeDoesNotOverwriteHapticsPreference() = runBlocking {
        val fixture = createRepository()

        fixture.repository.setGeneratedGameHapticsEnabled(false)
        fixture.repository.selectTheme(PersonalizationTheme.EMBER)

        assertEquals(
            PersonalizationPreferences(
                selectedTheme = PersonalizationTheme.EMBER,
                generatedGameHapticsEnabled = false
            ),
            fixture.repository.preferences.first()
        )
    }

    private fun createRepository(dataStoreFile: File = createDataStoreFile()): RepositoryFixture {
        val job = SupervisorJob()
        val scope = CoroutineScope(job + Dispatchers.IO)
        dataStoreJobs += job
        val dataStore = PreferenceDataStoreFactory.create(
            scope = scope,
            produceFile = { dataStoreFile }
        )

        return RepositoryFixture(
            repository = DataStorePersonalizationPreferencesRepository(dataStore),
            dataStore = dataStore,
            job = job
        )
    }

    private fun createDataStoreFile(): File = File(
        temporaryFolder.root,
        "${UUID.randomUUID()}.preferences_pb"
    )

    private data class RepositoryFixture(
        val repository: PersonalizationPreferencesRepository,
        val dataStore: DataStore<Preferences>,
        private val job: Job
    ) {
        suspend fun close() {
            job.cancelAndJoin()
        }
    }
}
