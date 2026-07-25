package org.cescfe.numpairs.data.session

import androidx.datastore.core.DataStore
import androidx.datastore.core.handlers.ReplaceFileCorruptionHandler
import androidx.datastore.preferences.core.PreferenceDataStoreFactory
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.emptyPreferences
import java.io.File
import java.time.LocalDate
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancelAndJoin
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.cescfe.numpairs.data.daily.session.DailySessionRepository
import org.cescfe.numpairs.data.daily.session.DailyState
import org.cescfe.numpairs.data.daily.session.DataStoreDailySessionRepository
import org.cescfe.numpairs.data.daily.session.generatedDailyFixture
import org.cescfe.numpairs.data.generated.session.DataStoreGeneratedSessionRepository
import org.cescfe.numpairs.data.generated.session.GeneratedSessionId
import org.cescfe.numpairs.data.generated.session.GeneratedSessionRepository
import org.cescfe.numpairs.data.generated.session.GeneratedSessionSnapshot
import org.cescfe.numpairs.domain.generated.generation.generatedPuzzle
import org.cescfe.numpairs.domain.generated.profile.GeneratedPuzzleProfiles
import org.cescfe.numpairs.domain.puzzle.model.StripItem
import org.cescfe.numpairs.feature.generated.GeneratedModes
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TemporaryFolder

class GeneratedAndDailySessionRecreationTest {
    @get:Rule
    val temporaryFolder = TemporaryFolder()

    private val dataStoreJobs = mutableListOf<Job>()

    @After
    fun tearDown() {
        dataStoreJobs.forEach(Job::cancel)
    }

    @Test
    fun quick_and_daily_restore_exactly_and_mutate_independently_across_recreation() = runBlocking {
        val generatedSessionFile = temporaryFile("generated_session.preferences_pb")
        val dailySessionFile = temporaryFile("daily_challenge.preferences_pb")
        val originalRepositories = createRepositories(
            generatedSessionFile = generatedSessionFile,
            dailySessionFile = dailySessionFile
        )
        val quickSession = quickSession()
        val dailyFixture = generatedDailyFixture(date = LocalDate.of(2027, 4, 18))
        val dailySession = dailyFixture.snapshot(currentPuzzle = dailyFixture.progressPuzzle())

        originalRepositories.generated.replace(quickSession)
        originalRepositories.daily.replaceSession(dailySession)

        assertEquals(quickSession, originalRepositories.generated.session.first())
        assertEquals(
            DailyState(activeSession = dailySession, completedChallengeIds = emptyList()),
            originalRepositories.daily.state.first()
        )

        originalRepositories.close()
        val recreatedRepositories = createRepositories(
            generatedSessionFile = generatedSessionFile,
            dailySessionFile = dailySessionFile
        )

        assertEquals(quickSession, recreatedRepositories.generated.session.first())
        assertEquals(
            DailyState(activeSession = dailySession, completedChallengeIds = emptyList()),
            recreatedRepositories.daily.state.first()
        )

        recreatedRepositories.daily.complete(
            expectedSessionId = dailySession.sessionId,
            expectedDailyChallengeId = dailySession.dailyChallengeId,
            solvedPuzzle = dailyFixture.solvedProgressPuzzle()
        )

        assertEquals(quickSession, recreatedRepositories.generated.session.first())
        assertEquals(
            DailyState(
                activeSession = null,
                completedChallengeIds = listOf(dailySession.dailyChallengeId)
            ),
            recreatedRepositories.daily.state.first()
        )

        val replacementQuickSession = quickSession(sessionId = "quick-replacement", seed = 298)
        recreatedRepositories.generated.replace(replacementQuickSession)
        assertTrue(recreatedRepositories.generated.clear(replacementQuickSession.sessionId))

        assertNull(recreatedRepositories.generated.session.first())
        assertEquals(
            DailyState(
                activeSession = null,
                completedChallengeIds = listOf(dailySession.dailyChallengeId)
            ),
            recreatedRepositories.daily.state.first()
        )
    }

    private fun quickSession(sessionId: String = "quick-session", seed: Int = 297): GeneratedSessionSnapshot {
        val generated = generatedPuzzle(
            profile = GeneratedPuzzleProfiles.THREE_PAIRS_LOW,
            seed = seed
        )
        val firstHiddenIndex = generated.initialPuzzle.strip.items.indexOfFirst { item ->
            item == StripItem.Hidden
        }
        val solutionValue = (generated.solvedPuzzle.strip.items[firstHiddenIndex] as StripItem.Known).value
        val currentPuzzle = generated.initialPuzzle.copy(
            strip = generated.initialPuzzle.strip.withUpdatedEntry(
                index = firstHiddenIndex,
                value = solutionValue
            )
        )

        return GeneratedSessionSnapshot(
            sessionId = GeneratedSessionId(sessionId),
            modeId = GeneratedModes.THREE_PAIRS.id.value,
            profileId = GeneratedModes.THREE_PAIRS_LOW.profile.id.value,
            seed = seed,
            initialPuzzle = generated.initialPuzzle,
            currentPuzzle = currentPuzzle
        )
    }

    private fun createRepositories(generatedSessionFile: File, dailySessionFile: File): RepositorySet {
        val generatedDataStore = createDataStore(generatedSessionFile)
        val dailyDataStore = createDataStore(dailySessionFile)

        return RepositorySet(
            generated = DataStoreGeneratedSessionRepository(generatedDataStore.dataStore),
            daily = DataStoreDailySessionRepository(dailyDataStore.dataStore),
            jobs = listOf(generatedDataStore.job, dailyDataStore.job)
        )
    }

    private fun createDataStore(file: File): DataStoreFixture {
        val job = SupervisorJob()
        dataStoreJobs += job
        return DataStoreFixture(
            dataStore = PreferenceDataStoreFactory.create(
                corruptionHandler = ReplaceFileCorruptionHandler {
                    emptyPreferences()
                },
                scope = CoroutineScope(job + Dispatchers.IO),
                produceFile = { file }
            ),
            job = job
        )
    }

    private fun temporaryFile(name: String): File = File(temporaryFolder.root, name)

    private data class DataStoreFixture(val dataStore: DataStore<Preferences>, val job: Job)

    private data class RepositorySet(
        val generated: GeneratedSessionRepository,
        val daily: DailySessionRepository,
        private val jobs: List<Job>
    ) {
        suspend fun close() {
            jobs.forEach { job -> job.cancelAndJoin() }
        }
    }
}
