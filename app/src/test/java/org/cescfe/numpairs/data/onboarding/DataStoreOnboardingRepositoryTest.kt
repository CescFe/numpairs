package org.cescfe.numpairs.data.onboarding

import androidx.datastore.preferences.core.PreferenceDataStoreFactory
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
    }

    @Test
    fun `pre-v6 upgrade starts required version complete`() = runBlocking {
        val fixture = createRepository()

        fixture.repository.initialize(OnboardingInstallationKind.PRE_V6_UPGRADE)

        val state = fixture.repository.onboardingState.first()
        assertTrue(state.isInitialized)
        assertTrue(state.isRequiredVersionComplete())
        assertEquals(REQUIRED_ONBOARDING_VERSION, state.completedVersion)
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
    fun `final completion is versioned and independent from stage checkpoint`() = runBlocking {
        val fixture = createRepository()
        fixture.repository.initialize(OnboardingInstallationKind.FRESH_INSTALL)
        fixture.repository.recordStageCompleted(OnboardingStageCheckpoint.STAGE_TWO)

        fixture.repository.markRequiredVersionCompleted()

        val state = fixture.repository.onboardingState.first()
        assertTrue(state.isRequiredVersionComplete())
        assertEquals(REQUIRED_ONBOARDING_VERSION, state.completedVersion)
        assertEquals(OnboardingStageCheckpoint.STAGE_TWO, state.lastCompletedStage)
    }

    @Test
    fun `state persists across repository instances`() = runBlocking {
        val dataStoreFile = createDataStoreFile()
        val firstFixture = createRepository(dataStoreFile)
        firstFixture.repository.initialize(OnboardingInstallationKind.FRESH_INSTALL)
        firstFixture.repository.recordStageCompleted(OnboardingStageCheckpoint.STAGE_THREE)
        firstFixture.close()

        val secondFixture = createRepository(dataStoreFile)

        assertEquals(
            OnboardingStageCheckpoint.STAGE_THREE,
            secondFixture.repository.onboardingState.first().lastCompletedStage
        )
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
            job = job
        )
    }

    private fun createDataStoreFile(): File = File(
        temporaryFolder.root,
        "${UUID.randomUUID()}.preferences_pb"
    )

    private data class RepositoryFixture(val repository: OnboardingRepository, private val job: Job) {
        suspend fun close() {
            job.cancelAndJoin()
        }
    }
}
