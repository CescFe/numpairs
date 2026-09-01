package org.cescfe.numpairs.domain.daily

import java.time.LocalDate
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class DailyPersonalBestHistoryTest {
    @Test
    fun category_best_is_the_minimum_timed_duration_and_ignores_movement_count() {
        val history = history(
            completion(date = "2027-01-01", category = THREE_LOW, elapsedMilliseconds = 5_000, movements = 4),
            completion(date = "2027-01-02", category = FOUR_LOW, elapsedMilliseconds = 1_000, movements = 3),
            completion(date = "2027-01-03", category = THREE_LOW, elapsedMilliseconds = 4_000, movements = 99),
            completion(date = "2027-01-04", category = THREE_LOW, elapsedMilliseconds = null, movements = 1),
            completion(date = "2027-01-05", category = THREE_MEDIUM, elapsedMilliseconds = 3_000),
            completion(date = "2027-01-06", category = FOUR_MEDIUM, elapsedMilliseconds = 2_000),
            completion(date = "2027-01-07", category = EIGHT_MEDIUM, elapsedMilliseconds = 8_000)
        )

        assertDailyElapsedTimeEquals(4_000, history.bestElapsedTimeFor(THREE_LOW))
        assertDailyElapsedTimeEquals(1_000, history.bestElapsedTimeFor(FOUR_LOW))
        assertDailyElapsedTimeEquals(3_000, history.bestElapsedTimeFor(THREE_MEDIUM))
        assertDailyElapsedTimeEquals(2_000, history.bestElapsedTimeFor(FOUR_MEDIUM))
        assertDailyElapsedTimeEquals(8_000, history.bestElapsedTimeFor(EIGHT_MEDIUM))
    }

    @Test
    fun first_timed_completion_establishes_a_baseline_without_becoming_a_record() {
        val result = history().resultFor(
            completion(date = "2027-01-01", category = THREE_LOW, elapsedMilliseconds = 5_000)
        )

        assertEquals(THREE_LOW, result.category)
        assertDailyElapsedTimeEquals(5_000, result.currentElapsedTime)
        assertNull(result.previousBestElapsedTime)
        assertDailyElapsedTimeEquals(5_000, result.bestElapsedTime)
        assertEquals(DailyPersonalBestOutcome.BASELINE, result.outcome)
    }

    @Test
    fun only_a_strictly_faster_completion_is_a_personal_record() {
        val history = history(
            completion(date = "2027-01-01", category = THREE_LOW, elapsedMilliseconds = 5_000),
            completion(date = "2027-01-02", category = FOUR_LOW, elapsedMilliseconds = 500)
        )

        val record = history.resultFor(
            completion(date = "2027-01-03", category = THREE_LOW, elapsedMilliseconds = 4_999)
        )
        val tie = history.resultFor(
            completion(date = "2027-01-03", category = THREE_LOW, elapsedMilliseconds = 5_000)
        )
        val slower = history.resultFor(
            completion(date = "2027-01-03", category = THREE_LOW, elapsedMilliseconds = 5_001)
        )

        assertEquals(DailyPersonalBestOutcome.PERSONAL_RECORD, record.outcome)
        assertDailyElapsedTimeEquals(5_000, record.previousBestElapsedTime)
        assertDailyElapsedTimeEquals(4_999, record.bestElapsedTime)
        assertEquals(DailyPersonalBestOutcome.NOT_RECORD, tie.outcome)
        assertDailyElapsedTimeEquals(5_000, tie.bestElapsedTime)
        assertEquals(DailyPersonalBestOutcome.NOT_RECORD, slower.outcome)
        assertDailyElapsedTimeEquals(5_000, slower.bestElapsedTime)
    }

    @Test
    fun untimed_and_unresolved_completions_never_establish_or_improve_a_best() {
        val history = history(
            completion(date = "2027-01-01", category = THREE_LOW, elapsedMilliseconds = 5_000)
        )

        val untimed = history.resultFor(
            completion(date = "2027-01-02", category = THREE_LOW, elapsedMilliseconds = null)
        )
        val unresolved = history.resultFor(
            DailyCompletion(
                identity = identity(date = "2027-01-03", recipeVersion = "unsupported"),
                elapsedTime = DailyElapsedTime(1),
                movementCount = DailyMovementCount.ZERO
            )
        )

        assertEquals(DailyPersonalBestOutcome.NOT_RECORD, untimed.outcome)
        assertDailyElapsedTimeEquals(5_000, untimed.previousBestElapsedTime)
        assertDailyElapsedTimeEquals(5_000, untimed.bestElapsedTime)
        assertEquals(DailyPersonalBestOutcome.NOT_RECORD, unresolved.outcome)
        assertNull(unresolved.category)
        assertNull(unresolved.previousBestElapsedTime)
        assertNull(unresolved.bestElapsedTime)
    }

    @Test
    fun historical_result_is_recreated_from_earlier_completions_without_future_results_reclassifying_it() {
        val current = completion(date = "2027-01-02", category = THREE_LOW, elapsedMilliseconds = 4_000)
        val history = history(
            completion(date = "2027-01-01", category = THREE_LOW, elapsedMilliseconds = 5_000),
            current,
            completion(date = "2027-01-03", category = THREE_LOW, elapsedMilliseconds = 3_000)
        )

        val result = history.resultFor(current)

        assertEquals(DailyPersonalBestOutcome.PERSONAL_RECORD, result.outcome)
        assertDailyElapsedTimeEquals(5_000, result.previousBestElapsedTime)
        assertDailyElapsedTimeEquals(4_000, result.bestElapsedTime)
    }

    private fun history(vararg completions: DailyCompletion): DailyPersonalBestHistory = DailyPersonalBestHistory(
        completions = completions.toList(),
        categoryResolver = { identity ->
            identity.recipeVersion.value
                .takeIf(CATEGORIES::containsKey)
                ?.let(::DailyPersonalBestCategory)
        }
    )

    private fun completion(
        date: String,
        category: DailyPersonalBestCategory,
        elapsedMilliseconds: Long?,
        movements: Long = 0
    ): DailyCompletion = DailyCompletion(
        identity = identity(date = date, recipeVersion = category.generatedChallengeId),
        elapsedTime = elapsedMilliseconds?.let(::DailyElapsedTime),
        movementCount = DailyMovementCount(movements)
    )

    private fun identity(date: String, recipeVersion: String): DailyChallengeId = DailyChallengeId(
        localDate = LocalDate.parse(date),
        recipeVersion = DailyRecipeVersion(recipeVersion)
    )

    private companion object {
        val THREE_LOW = DailyPersonalBestCategory("three-pairs-low")
        val FOUR_LOW = DailyPersonalBestCategory("four-pairs-low")
        val THREE_MEDIUM = DailyPersonalBestCategory("three-pairs-medium")
        val FOUR_MEDIUM = DailyPersonalBestCategory("four-pairs-medium")
        val EIGHT_MEDIUM = DailyPersonalBestCategory("eight-pairs-medium")
        val CATEGORIES = listOf(
            THREE_LOW,
            FOUR_LOW,
            THREE_MEDIUM,
            FOUR_MEDIUM,
            EIGHT_MEDIUM
        ).associateBy(DailyPersonalBestCategory::generatedChallengeId)
    }
}
