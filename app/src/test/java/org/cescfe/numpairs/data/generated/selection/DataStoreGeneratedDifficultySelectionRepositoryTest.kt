package org.cescfe.numpairs.data.generated.selection

import androidx.datastore.core.DataStore
import androidx.datastore.core.handlers.ReplaceFileCorruptionHandler
import androidx.datastore.preferences.core.PreferenceDataStoreFactory
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.emptyPreferences
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
import org.cescfe.numpairs.domain.generated.profile.DifficultyTier
import org.cescfe.numpairs.feature.generated.GeneratedModes
import org.cescfe.numpairs.feature.generated.GeneratedPlayOptionId
import org.cescfe.numpairs.feature.generated.GeneratedPlayOptions
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertThrows
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TemporaryFolder

class DataStoreGeneratedDifficultySelectionRepositoryTest {
    @get:Rule
    val temporaryFolder = TemporaryFolder()

    private val dataStoreJobs = mutableListOf<Job>()

    @After
    fun tearDown() {
        dataStoreJobs.forEach(Job::cancel)
    }

    @Test
    fun fresh_preferences_expose_option_fallbacks_without_persisting_them() = runBlocking {
        val fixture = createRepository()

        assertEquals(
            DifficultyTier.LOW,
            fixture.repository.selectedDifficulty(GeneratedPlayOptions.QUICK.id).first()
        )
        assertEquals(
            DifficultyTier.MEDIUM,
            fixture.repository.selectedDifficulty(GeneratedPlayOptions.CLASSIC.id).first()
        )
        assertTrue(fixture.dataStore.data.first().asMap().isEmpty())
    }

    @Test
    fun legacy_mode_preferences_supply_initial_quick_and_classic_selections_without_rewriting() = runBlocking {
        val fixture = createRepository()
        val legacyQuickKey = legacyDifficultyPreferenceKey(GeneratedModes.FOUR_PAIRS.id)
        val legacyClassicKey = legacyDifficultyPreferenceKey(GeneratedModes.EIGHT_PAIRS.id)
        fixture.dataStore.edit { preferences ->
            preferences[legacyQuickKey] = "medium"
            preferences[legacyClassicKey] = "hard"
        }

        assertEquals(
            DifficultyTier.MEDIUM,
            fixture.repository.selectedDifficulty(GeneratedPlayOptions.QUICK.id).first()
        )
        assertEquals(
            DifficultyTier.HARD,
            fixture.repository.selectedDifficulty(GeneratedPlayOptions.CLASSIC.id).first()
        )
        val storedPreferences = fixture.dataStore.data.first()
        assertEquals("medium", storedPreferences[legacyQuickKey])
        assertEquals("hard", storedPreferences[legacyClassicKey])
        assertNull(storedPreferences[difficultyPreferenceKey(GeneratedPlayOptions.QUICK.id)])
        assertNull(storedPreferences[difficultyPreferenceKey(GeneratedPlayOptions.CLASSIC.id)])
    }

    @Test
    fun explicit_option_selections_persist_independently_across_recreation() = runBlocking {
        val dataStoreFile = createDataStoreFile()
        val firstFixture = createRepository(dataStoreFile)

        firstFixture.repository.selectDifficulty(
            optionId = GeneratedPlayOptions.QUICK.id,
            difficulty = DifficultyTier.MEDIUM
        )
        firstFixture.repository.selectDifficulty(
            optionId = GeneratedPlayOptions.CLASSIC.id,
            difficulty = DifficultyTier.HARD
        )
        firstFixture.close()

        val secondFixture = createRepository(dataStoreFile)

        assertEquals(
            DifficultyTier.MEDIUM,
            secondFixture.repository.selectedDifficulty(GeneratedPlayOptions.QUICK.id).first()
        )
        assertEquals(
            DifficultyTier.HARD,
            secondFixture.repository.selectedDifficulty(GeneratedPlayOptions.CLASSIC.id).first()
        )
    }

    @Test
    fun explicit_option_value_takes_precedence_over_legacy_mode_value() = runBlocking {
        val fixture = createRepository()
        fixture.dataStore.edit { preferences ->
            preferences[legacyDifficultyPreferenceKey(GeneratedModes.FOUR_PAIRS.id)] = "medium"
        }
        fixture.repository.selectDifficulty(GeneratedPlayOptions.QUICK.id, DifficultyTier.LOW)

        assertEquals(
            DifficultyTier.LOW,
            fixture.repository.selectedDifficulty(GeneratedPlayOptions.QUICK.id).first()
        )
        assertEquals(
            "medium",
            fixture.dataStore.data.first()[legacyDifficultyPreferenceKey(GeneratedModes.FOUR_PAIRS.id)]
        )
    }

    @Test
    fun invalid_new_and_legacy_values_fall_back_without_rewriting_storage() = runBlocking {
        val fixture = createRepository()
        val quickKey = difficultyPreferenceKey(GeneratedPlayOptions.QUICK.id)
        val classicLegacyKey = legacyDifficultyPreferenceKey(GeneratedModes.EIGHT_PAIRS.id)
        fixture.dataStore.edit { preferences ->
            preferences[quickKey] = "hard"
            preferences[legacyDifficultyPreferenceKey(GeneratedModes.FOUR_PAIRS.id)] = "medium"
            preferences[classicLegacyKey] = "future-difficulty"
        }

        assertEquals(
            DifficultyTier.LOW,
            fixture.repository.selectedDifficulty(GeneratedPlayOptions.QUICK.id).first()
        )
        assertEquals(
            DifficultyTier.MEDIUM,
            fixture.repository.selectedDifficulty(GeneratedPlayOptions.CLASSIC.id).first()
        )
        val storedPreferences = fixture.dataStore.data.first()
        assertEquals("hard", storedPreferences[quickKey])
        assertEquals("future-difficulty", storedPreferences[classicLegacyKey])
    }

    @Test
    fun unknown_option_is_ignored_and_exposes_no_invented_fallback() = runBlocking {
        val fixture = createRepository()
        val unknownOption = GeneratedPlayOptionId("future-option")
        val unknownOptionKey = stringPreferencesKey(
            "generated_selected_difficulty_${unknownOption.value}"
        )
        fixture.dataStore.edit { preferences ->
            preferences[unknownOptionKey] = "hard"
        }

        assertNull(fixture.repository.selectedDifficulty(unknownOption).first())
        assertEquals("hard", fixture.dataStore.data.first()[unknownOptionKey])
    }

    @Test
    fun unsupported_and_unknown_explicit_selections_fail_before_writing() {
        val fixture = createRepository()

        assertThrows(IllegalArgumentException::class.java) {
            runBlocking {
                fixture.repository.selectDifficulty(GeneratedPlayOptions.QUICK.id, DifficultyTier.HARD)
            }
        }
        assertThrows(IllegalArgumentException::class.java) {
            runBlocking {
                fixture.repository.selectDifficulty(
                    GeneratedPlayOptionId("future-option"),
                    DifficultyTier.LOW
                )
            }
        }

        runBlocking {
            assertTrue(fixture.dataStore.data.first().asMap().isEmpty())
        }
    }

    @Test
    fun corrupt_preferences_file_recovers_to_safe_option_fallbacks() = runBlocking {
        val dataStoreFile = createDataStoreFile().apply {
            parentFile?.mkdirs()
            writeBytes(byteArrayOf(1, 2, 3, 4))
        }
        val fixture = createRepository(dataStoreFile)

        assertEquals(
            DifficultyTier.LOW,
            fixture.repository.selectedDifficulty(GeneratedPlayOptions.QUICK.id).first()
        )
        assertEquals(
            DifficultyTier.MEDIUM,
            fixture.repository.selectedDifficulty(GeneratedPlayOptions.CLASSIC.id).first()
        )
    }

    private fun createRepository(dataStoreFile: File = createDataStoreFile()): RepositoryFixture {
        val job = SupervisorJob()
        dataStoreJobs += job
        val dataStore = PreferenceDataStoreFactory.create(
            corruptionHandler = ReplaceFileCorruptionHandler {
                emptyPreferences()
            },
            scope = CoroutineScope(job + Dispatchers.IO),
            produceFile = { dataStoreFile }
        )

        return RepositoryFixture(
            repository = DataStoreGeneratedDifficultySelectionRepository(
                dataStore = dataStore,
                catalog = GeneratedModes.catalog,
                playOptions = GeneratedPlayOptions.ALL,
                fallbackDifficultyByOption = mapOf(
                    GeneratedPlayOptions.QUICK.id to DifficultyTier.LOW,
                    GeneratedPlayOptions.CLASSIC.id to DifficultyTier.MEDIUM
                ),
                legacyModeByOption = mapOf(
                    GeneratedPlayOptions.QUICK.id to GeneratedModes.FOUR_PAIRS.id,
                    GeneratedPlayOptions.CLASSIC.id to GeneratedModes.EIGHT_PAIRS.id
                )
            ),
            dataStore = dataStore,
            job = job
        )
    }

    private fun createDataStoreFile(): File = File(
        temporaryFolder.root,
        "${UUID.randomUUID()}.preferences_pb"
    )

    private data class RepositoryFixture(
        val repository: GeneratedDifficultySelectionRepository,
        val dataStore: DataStore<Preferences>,
        private val job: Job
    ) {
        suspend fun close() {
            job.cancelAndJoin()
        }
    }
}
