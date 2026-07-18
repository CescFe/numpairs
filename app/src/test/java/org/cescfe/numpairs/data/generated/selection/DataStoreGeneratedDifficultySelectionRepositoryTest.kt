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
import org.cescfe.numpairs.feature.generated.GeneratedModeId
import org.cescfe.numpairs.feature.generated.GeneratedModes
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
    fun fresh_preferences_expose_mode_specific_fallbacks_without_persisting_them() = runBlocking {
        val fixture = createRepository()

        assertEquals(
            DifficultyTier.LOW,
            fixture.repository.selectedDifficulty(GeneratedModes.FOUR_PAIRS.id).first()
        )
        assertEquals(
            DifficultyTier.MEDIUM,
            fixture.repository.selectedDifficulty(GeneratedModes.EIGHT_PAIRS.id).first()
        )
        assertTrue(fixture.dataStore.data.first().asMap().isEmpty())
    }

    @Test
    fun explicit_supported_selections_persist_independently_across_recreation() = runBlocking {
        val dataStoreFile = createDataStoreFile()
        val firstFixture = createRepository(dataStoreFile)

        firstFixture.repository.selectDifficulty(
            modeId = GeneratedModes.FOUR_PAIRS.id,
            difficulty = DifficultyTier.MEDIUM
        )
        firstFixture.repository.selectDifficulty(
            modeId = GeneratedModes.EIGHT_PAIRS.id,
            difficulty = DifficultyTier.HARD
        )
        firstFixture.close()

        val secondFixture = createRepository(dataStoreFile)

        assertEquals(
            DifficultyTier.MEDIUM,
            secondFixture.repository.selectedDifficulty(GeneratedModes.FOUR_PAIRS.id).first()
        )
        assertEquals(
            DifficultyTier.HARD,
            secondFixture.repository.selectedDifficulty(GeneratedModes.EIGHT_PAIRS.id).first()
        )
    }

    @Test
    fun changing_one_mode_does_not_overwrite_the_other_mode() = runBlocking {
        val fixture = createRepository()
        fixture.repository.selectDifficulty(GeneratedModes.FOUR_PAIRS.id, DifficultyTier.MEDIUM)

        assertEquals(
            DifficultyTier.MEDIUM,
            fixture.repository.selectedDifficulty(GeneratedModes.FOUR_PAIRS.id).first()
        )
        assertEquals(
            DifficultyTier.MEDIUM,
            fixture.repository.selectedDifficulty(GeneratedModes.EIGHT_PAIRS.id).first()
        )

        fixture.repository.selectDifficulty(GeneratedModes.EIGHT_PAIRS.id, DifficultyTier.HARD)

        assertEquals(
            DifficultyTier.MEDIUM,
            fixture.repository.selectedDifficulty(GeneratedModes.FOUR_PAIRS.id).first()
        )
        assertEquals(
            DifficultyTier.HARD,
            fixture.repository.selectedDifficulty(GeneratedModes.EIGHT_PAIRS.id).first()
        )
    }

    @Test
    fun unknown_future_and_unsupported_values_fall_back_without_rewriting_storage() = runBlocking {
        val fixture = createRepository()
        val fourPairsKey = difficultyPreferenceKey(GeneratedModes.FOUR_PAIRS.id)
        val eightPairsKey = difficultyPreferenceKey(GeneratedModes.EIGHT_PAIRS.id)
        fixture.dataStore.edit { preferences ->
            preferences[fourPairsKey] = "hard"
            preferences[eightPairsKey] = "future-difficulty"
        }

        assertEquals(
            DifficultyTier.LOW,
            fixture.repository.selectedDifficulty(GeneratedModes.FOUR_PAIRS.id).first()
        )
        assertEquals(
            DifficultyTier.MEDIUM,
            fixture.repository.selectedDifficulty(GeneratedModes.EIGHT_PAIRS.id).first()
        )
        val storedPreferences = fixture.dataStore.data.first()
        assertEquals("hard", storedPreferences[fourPairsKey])
        assertEquals("future-difficulty", storedPreferences[eightPairsKey])
    }

    @Test
    fun unknown_mode_is_ignored_and_exposes_no_invented_fallback() = runBlocking {
        val fixture = createRepository()
        val unknownMode = GeneratedModeId("future-mode")
        val unknownModeKey = stringPreferencesKey("generated_selected_difficulty_${unknownMode.value}")
        fixture.dataStore.edit { preferences ->
            preferences[unknownModeKey] = "hard"
        }

        assertNull(fixture.repository.selectedDifficulty(unknownMode).first())
        assertEquals("hard", fixture.dataStore.data.first()[unknownModeKey])
    }

    @Test
    fun unsupported_and_unknown_explicit_selections_fail_before_writing() {
        val fixture = createRepository()

        assertThrows(IllegalArgumentException::class.java) {
            runBlocking {
                fixture.repository.selectDifficulty(GeneratedModes.FOUR_PAIRS.id, DifficultyTier.HARD)
            }
        }
        assertThrows(IllegalArgumentException::class.java) {
            runBlocking {
                fixture.repository.selectDifficulty(GeneratedModeId("future-mode"), DifficultyTier.LOW)
            }
        }

        runBlocking {
            assertTrue(fixture.dataStore.data.first().asMap().isEmpty())
        }
    }

    @Test
    fun corrupt_preferences_file_recovers_to_safe_fallbacks() = runBlocking {
        val dataStoreFile = createDataStoreFile().apply {
            parentFile?.mkdirs()
            writeBytes(byteArrayOf(1, 2, 3, 4))
        }
        val fixture = createRepository(dataStoreFile)

        assertEquals(
            DifficultyTier.LOW,
            fixture.repository.selectedDifficulty(GeneratedModes.FOUR_PAIRS.id).first()
        )
        assertEquals(
            DifficultyTier.MEDIUM,
            fixture.repository.selectedDifficulty(GeneratedModes.EIGHT_PAIRS.id).first()
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
                fallbackDifficultyByMode = mapOf(
                    GeneratedModes.FOUR_PAIRS.id to DifficultyTier.LOW,
                    GeneratedModes.EIGHT_PAIRS.id to DifficultyTier.MEDIUM
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
