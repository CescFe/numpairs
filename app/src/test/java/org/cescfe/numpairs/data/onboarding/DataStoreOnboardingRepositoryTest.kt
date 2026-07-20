package org.cescfe.numpairs.data.onboarding

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.PreferenceDataStoreFactory
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import java.io.File
import java.io.IOException
import java.util.UUID
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancelAndJoin
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertSame
import org.junit.Assert.assertThrows
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TemporaryFolder

class DataStoreOnboardingRepositoryTest {
    @get:Rule
    val temporaryFolder = TemporaryFolder()

    private val dataStoreJobs = mutableListOf<Job>()

    @After
    fun tearDown() {
        dataStoreJobs.forEach(Job::cancel)
    }

    @Test
    fun `empty application data starts an unresolved first run`() = runBlocking {
        val state = createRepository().repository.onboardingState.first()

        assertEquals(OnboardingState(), state)
        assertFalse(state.firstRunTutorialOutcome.isResolved)
        assertEquals(OnboardingStageCheckpoint.NONE, state.lastCompletedStage)
    }

    @Test
    fun `read failures remain observable for startup recovery`() {
        val expectedFailure = IOException("Synthetic onboarding read failure.")
        val repository = DataStoreOnboardingRepository(
            object : DataStore<Preferences> {
                override val data = flow<Preferences> { throw expectedFailure }

                override suspend fun updateData(transform: suspend (Preferences) -> Preferences): Preferences =
                    error("No update is expected while testing a read failure.")
            }
        )

        val actualFailure = assertThrows(IOException::class.java) {
            runBlocking { repository.onboardingState.first() }
        }

        assertSame(expectedFailure, actualFailure)
    }

    @Test
    fun `stage checkpoints only move forward`() = runBlocking {
        val repository = createRepository().repository

        repository.recordStageCompleted(OnboardingStageCheckpoint.STAGE_TWO)
        repository.recordStageCompleted(OnboardingStageCheckpoint.STAGE_ONE)

        assertEquals(
            OnboardingStageCheckpoint.STAGE_TWO,
            repository.onboardingState.first().lastCompletedStage
        )
    }

    @Test(expected = IllegalArgumentException::class)
    fun `none cannot be recorded as a completed stage`() = runBlocking {
        createRepository().repository.recordStageCompleted(OnboardingStageCheckpoint.NONE)
    }

    @Test
    fun `tutorial completion is independent from stage checkpoint`() = runBlocking {
        val repository = createRepository().repository
        repository.recordStageCompleted(OnboardingStageCheckpoint.STAGE_TWO)

        repository.markTutorialCompleted()

        val state = repository.onboardingState.first()
        assertTrue(state.firstRunTutorialOutcome.isResolved)
        assertEquals(OnboardingStageCheckpoint.STAGE_TWO, state.lastCompletedStage)
        assertEquals(FirstRunTutorialOutcome.COMPLETED, state.firstRunTutorialOutcome)
    }

    @Test
    fun `explicit skip resolves the first run with a distinct outcome`() = runBlocking {
        val repository = createRepository().repository

        repository.markTutorialSkipped()

        val state = repository.onboardingState.first()
        assertTrue(state.firstRunTutorialOutcome.isResolved)
        assertEquals(FirstRunTutorialOutcome.SKIPPED, state.firstRunTutorialOutcome)
    }

    @Test
    fun `resolved first-run outcome cannot be overwritten`() = runBlocking {
        val repository = createRepository().repository
        repository.markTutorialSkipped()

        repository.markTutorialCompleted()

        assertEquals(
            FirstRunTutorialOutcome.SKIPPED,
            repository.onboardingState.first().firstRunTutorialOutcome
        )
    }

    @Test
    fun `unknown persisted outcome starts an unresolved first run`() = runBlocking {
        val fixture = createRepository()
        fixture.dataStore.edit { preferences ->
            preferences[intPreferencesKey("onboarding_first_run_tutorial_outcome")] = Int.MAX_VALUE
        }

        val state = fixture.repository.onboardingState.first()

        assertEquals(FirstRunTutorialOutcome.UNRESOLVED, state.firstRunTutorialOutcome)
    }

    @Test
    fun `clearing all application data starts a new first run`() = runBlocking {
        val fixture = createRepository()
        fixture.repository.recordStageCompleted(OnboardingStageCheckpoint.STAGE_THREE)
        fixture.repository.markTutorialCompleted()

        fixture.dataStore.edit { preferences -> preferences.clear() }

        assertEquals(OnboardingState(), fixture.repository.onboardingState.first())
    }

    @Test
    fun `resolved state persists across repository instances`() = runBlocking {
        val dataStoreFile = createDataStoreFile()
        val firstFixture = createRepository(dataStoreFile)
        firstFixture.repository.recordStageCompleted(OnboardingStageCheckpoint.STAGE_THREE)
        firstFixture.repository.markTutorialCompleted()
        firstFixture.close()

        val restoredState = createRepository(dataStoreFile).repository.onboardingState.first()

        assertEquals(OnboardingStageCheckpoint.STAGE_THREE, restoredState.lastCompletedStage)
        assertEquals(FirstRunTutorialOutcome.COMPLETED, restoredState.firstRunTutorialOutcome)
    }

    private fun createRepository(dataStoreFile: File = createDataStoreFile()): RepositoryFixture {
        val job = SupervisorJob()
        dataStoreJobs += job
        val dataStore = PreferenceDataStoreFactory.create(
            scope = CoroutineScope(job + Dispatchers.IO),
            produceFile = { dataStoreFile }
        )

        return RepositoryFixture(
            repository = DataStoreOnboardingRepository(dataStore),
            dataStore = dataStore,
            job = job
        )
    }

    private fun createDataStoreFile(): File = File(
        temporaryFolder.root,
        "${UUID.randomUUID()}.preferences_pb"
    )

    private data class RepositoryFixture(
        val repository: OnboardingRepository,
        val dataStore: DataStore<Preferences>,
        private val job: Job
    ) {
        suspend fun close() {
            job.cancelAndJoin()
        }
    }
}
