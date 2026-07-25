package org.cescfe.numpairs.feature.daily

import java.time.LocalDate
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.runBlocking
import org.cescfe.numpairs.data.daily.session.DailySessionClearResult
import org.cescfe.numpairs.data.daily.session.DailySessionCompletionResult
import org.cescfe.numpairs.data.daily.session.DailySessionId
import org.cescfe.numpairs.data.daily.session.DailySessionProgressUpdateResult
import org.cescfe.numpairs.data.daily.session.DailySessionReplacementResult
import org.cescfe.numpairs.data.daily.session.DailySessionRepository
import org.cescfe.numpairs.data.daily.session.DailySessionSnapshot
import org.cescfe.numpairs.data.daily.session.DailyState
import org.cescfe.numpairs.data.daily.session.generatedDailyFixture
import org.cescfe.numpairs.domain.daily.DailyChallengeId
import org.cescfe.numpairs.domain.daily.DailyRecipeVersion
import org.cescfe.numpairs.domain.daily.DeviceLocalDateSource
import org.cescfe.numpairs.domain.puzzle.model.Puzzle
import org.junit.Assert.assertEquals
import org.junit.Assert.assertSame
import org.junit.Assert.assertTrue
import org.junit.Test

class CurrentDailyAvailabilityResolverTest {
    @Test
    fun same_date_completion_takes_precedence_over_an_exact_active_session() = runBlocking {
        val date = LocalDate.of(2027, 4, 18)
        val snapshot = generatedDailyFixture(date = date).snapshot()
        val olderRecipeCompletion = DailyChallengeId(
            localDate = date,
            recipeVersion = DailyRecipeVersion("daily-older-supported-recipe")
        )
        val repository = FakeDailySessionRepository(
            initialState = DailyState(
                activeSession = snapshot,
                completedChallengeIds = listOf(olderRecipeCompletion)
            )
        )

        val availability = resolver(date = date, repository = repository).resolve()
            as CurrentDailyAvailability.CompletedToday

        assertEquals(olderRecipeCompletion, availability.completion)
        assertEquals(date, availability.currentDailyChallenge.identity.localDate)
        assertEquals(0, repository.mutationCount)
    }

    @Test
    fun exact_current_snapshot_is_restored_without_generation_or_repository_mutation() = runBlocking {
        val date = LocalDate.of(2027, 4, 18)
        val fixture = generatedDailyFixture(date = date)
        val snapshot = fixture.snapshot(currentPuzzle = fixture.progressPuzzle())
        val repository = FakeDailySessionRepository(
            initialState = DailyState(
                activeSession = snapshot,
                completedChallengeIds = emptyList()
            )
        )

        val availability = resolver(date = date, repository = repository).resolve()
            as CurrentDailyAvailability.ContinueToday

        assertSame(snapshot, availability.snapshot)
        assertEquals(0, repository.mutationCount)
    }

    @Test
    fun stale_snapshot_is_unavailable_and_remains_untouched() = runBlocking {
        val staleSnapshot = generatedDailyFixture(
            date = LocalDate.of(2027, 4, 17)
        ).snapshot()
        val repository = FakeDailySessionRepository(
            initialState = DailyState(
                activeSession = staleSnapshot,
                completedChallengeIds = emptyList()
            )
        )

        val availability = resolver(
            date = LocalDate.of(2027, 4, 18),
            repository = repository
        ).resolve()

        assertTrue(availability is CurrentDailyAvailability.StartToday)
        assertSame(staleSnapshot, repository.currentState.activeSession)
        assertEquals(0, repository.mutationCount)
    }

    @Test
    fun moving_the_trusted_clock_back_can_make_a_retained_exact_session_current_again() = runBlocking {
        var currentDate = LocalDate.of(2027, 4, 19)
        val retainedSnapshot = generatedDailyFixture(
            date = LocalDate.of(2027, 4, 18)
        ).snapshot()
        val repository = FakeDailySessionRepository(
            initialState = DailyState(
                activeSession = retainedSnapshot,
                completedChallengeIds = emptyList()
            )
        )
        val resolver = CurrentDailyAvailabilityResolver(
            currentDailyChallengeResolver = CurrentDailyChallengeResolver(
                localDateSource = DeviceLocalDateSource { currentDate }
            ),
            dailySessionRepository = repository
        )

        assertTrue(resolver.resolve() is CurrentDailyAvailability.StartToday)
        currentDate = LocalDate.of(2027, 4, 18)
        val restored = resolver.resolve() as CurrentDailyAvailability.ContinueToday

        assertSame(retainedSnapshot, restored.snapshot)
        assertEquals(0, repository.mutationCount)
    }
}

private fun resolver(date: LocalDate, repository: DailySessionRepository): CurrentDailyAvailabilityResolver =
    CurrentDailyAvailabilityResolver(
        currentDailyChallengeResolver = CurrentDailyChallengeResolver(
            localDateSource = DeviceLocalDateSource { date }
        ),
        dailySessionRepository = repository
    )

private class FakeDailySessionRepository(initialState: DailyState) : DailySessionRepository {
    private val mutableState = MutableStateFlow(initialState)
    override val state = mutableState

    val currentState: DailyState
        get() = mutableState.value

    var mutationCount: Int = 0
        private set

    override suspend fun replaceSession(snapshot: DailySessionSnapshot): DailySessionReplacementResult {
        mutationCount += 1
        mutableState.value = mutableState.value.copy(activeSession = snapshot)
        return DailySessionReplacementResult.Replaced
    }

    override suspend fun updateCurrentPuzzle(
        expectedSessionId: DailySessionId,
        puzzle: Puzzle
    ): DailySessionProgressUpdateResult {
        mutationCount += 1
        return DailySessionProgressUpdateResult.StaleSession
    }

    override suspend fun clearSession(expectedSessionId: DailySessionId): DailySessionClearResult {
        mutationCount += 1
        return DailySessionClearResult.StaleSession
    }

    override suspend fun complete(
        expectedSessionId: DailySessionId,
        expectedDailyChallengeId: DailyChallengeId,
        solvedPuzzle: Puzzle
    ): DailySessionCompletionResult {
        mutationCount += 1
        return DailySessionCompletionResult.StaleSession
    }
}
