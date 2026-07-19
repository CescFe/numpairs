package org.cescfe.numpairs.data.onboarding

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.PreferenceDataStoreFactory
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
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
import org.junit.Assert.assertFalse
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
    fun `state is uninitialized before installation policy is applied`() = runBlocking {
        val fixture = createRepository()

        assertEquals(OnboardingState(), fixture.repository.onboardingState.first())
    }

    @Test
    fun `fresh installation starts required version incomplete`() = runBlocking {
        val fixture = createRepository()

        fixture.repository.initialize(OnboardingInstallationKind.FRESH_INSTALL)

        val state = fixture.repository.onboardingState.first()
        assertTrue(state.isInitialized)
        assertFalse(state.isRequiredVersionComplete())
        assertEquals(OnboardingStageCheckpoint.NONE, state.lastCompletedStage)
        assertEquals(FirstRunTutorialOutcome.UNRESOLVED, state.firstRunTutorialOutcome)
    }

    @Test
    fun `pre-v6 upgrade starts required version complete`() = runBlocking {
        val fixture = createRepository()

        fixture.repository.initialize(OnboardingInstallationKind.PRE_V6_UPGRADE)

        val state = fixture.repository.onboardingState.first()
        assertTrue(state.isInitialized)
        assertTrue(state.isRequiredVersionComplete())
        assertEquals(REQUIRED_ONBOARDING_VERSION, state.completedVersion)
        assertEquals(FirstRunTutorialOutcome.PRE_V6_UPGRADE, state.firstRunTutorialOutcome)
    }

    @Test
    fun `initialization never overwrites existing progress`() = runBlocking {
        val fixture = createRepository()
        fixture.repository.initialize(OnboardingInstallationKind.FRESH_INSTALL)
        fixture.repository.recordStageCompleted(OnboardingStageCheckpoint.STAGE_TWO)

        fixture.repository.initialize(OnboardingInstallationKind.PRE_V6_UPGRADE)

        val state = fixture.repository.onboardingState.first()
        assertFalse(state.isRequiredVersionComplete())
        assertEquals(OnboardingStageCheckpoint.STAGE_TWO, state.lastCompletedStage)
        assertEquals(FirstRunTutorialOutcome.UNRESOLVED, state.firstRunTutorialOutcome)
    }

    @Test
    fun `stage checkpoints only move forward`() = runBlocking {
        val fixture = createRepository()
        fixture.repository.initialize(OnboardingInstallationKind.FRESH_INSTALL)

        fixture.repository.recordStageCompleted(OnboardingStageCheckpoint.STAGE_TWO)
        fixture.repository.recordStageCompleted(OnboardingStageCheckpoint.STAGE_ONE)

        assertEquals(
            OnboardingStageCheckpoint.STAGE_TWO,
            fixture.repository.onboardingState.first().lastCompletedStage
        )
    }

    @Test(expected = IllegalArgumentException::class)
    fun `none cannot be recorded as a completed stage`() = runBlocking {
        createRepository().repository.recordStageCompleted(OnboardingStageCheckpoint.NONE)
    }

    @Test
    fun `tutorial completion is versioned and independent from stage checkpoint`() = runBlocking {
        val fixture = createRepository()
        fixture.repository.initialize(OnboardingInstallationKind.FRESH_INSTALL)
        fixture.repository.recordStageCompleted(OnboardingStageCheckpoint.STAGE_TWO)

        fixture.repository.markTutorialCompleted()

        val state = fixture.repository.onboardingState.first()
        assertTrue(state.isRequiredVersionComplete())
        assertEquals(REQUIRED_ONBOARDING_VERSION, state.completedVersion)
        assertEquals(OnboardingStageCheckpoint.STAGE_TWO, state.lastCompletedStage)
        assertEquals(FirstRunTutorialOutcome.COMPLETED, state.firstRunTutorialOutcome)
    }

    @Test
    fun `explicit skip resolves the version with a distinct outcome`() = runBlocking {
        val fixture = createRepository()
        fixture.repository.initialize(OnboardingInstallationKind.FRESH_INSTALL)

        fixture.repository.markTutorialSkipped()

        val state = fixture.repository.onboardingState.first()
        assertTrue(state.isRequiredVersionComplete())
        assertEquals(REQUIRED_ONBOARDING_VERSION, state.completedVersion)
        assertEquals(FirstRunTutorialOutcome.SKIPPED, state.firstRunTutorialOutcome)
    }

    @Test
    fun `resolved first-run outcome cannot be overwritten`() = runBlocking {
        val fixture = createRepository()
        fixture.repository.initialize(OnboardingInstallationKind.FRESH_INSTALL)
        fixture.repository.markTutorialSkipped()

        fixture.repository.markTutorialCompleted()

        assertEquals(
            FirstRunTutorialOutcome.SKIPPED,
            fixture.repository.onboardingState.first().firstRunTutorialOutcome
        )
    }

    @Test
    fun `completed state without an outcome is recognized as legacy completion`() = runBlocking {
        val fixture = createRepository()
        fixture.dataStore.edit { preferences ->
            preferences[booleanPreferencesKey("onboarding_is_initialized")] = true
            preferences[intPreferencesKey("onboarding_completed_version")] = REQUIRED_ONBOARDING_VERSION
        }

        val state = fixture.repository.onboardingState.first()

        assertTrue(state.isRequiredVersionComplete())
        assertEquals(FirstRunTutorialOutcome.LEGACY_COMPLETED, state.firstRunTutorialOutcome)
    }

    @Test
    fun `unknown persisted outcome falls back safely`() = runBlocking {
        val fixture = createRepository()
        fixture.dataStore.edit { preferences ->
            preferences[booleanPreferencesKey("onboarding_is_initialized")] = true
            preferences[intPreferencesKey("onboarding_completed_version")] = REQUIRED_ONBOARDING_VERSION
            preferences[intPreferencesKey("onboarding_first_run_tutorial_outcome")] = Int.MAX_VALUE
        }

        val state = fixture.repository.onboardingState.first()

        assertTrue(state.isRequiredVersionComplete())
        assertEquals(FirstRunTutorialOutcome.LEGACY_COMPLETED, state.firstRunTutorialOutcome)
    }

    @Test
    fun `state persists across repository instances`() = runBlocking {
        val dataStoreFile = createDataStoreFile()
        val firstFixture = createRepository(dataStoreFile)
        firstFixture.repository.initialize(OnboardingInstallationKind.FRESH_INSTALL)
        firstFixture.repository.recordStageCompleted(OnboardingStageCheckpoint.STAGE_THREE)
        firstFixture.repository.markTutorialCompleted()
        firstFixture.close()

        val secondFixture = createRepository(dataStoreFile)
        val restoredState = secondFixture.repository.onboardingState.first()

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
