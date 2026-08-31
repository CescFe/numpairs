package org.cescfe.numpairs.feature.daily.calendar

import java.time.DayOfWeek
import java.time.LocalDate
import java.time.YearMonth
import java.util.Locale
import org.cescfe.numpairs.domain.daily.DailyChallengeId
import org.cescfe.numpairs.domain.daily.DailyCompletion
import org.cescfe.numpairs.domain.daily.DailyRecipeVersion
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertThrows
import org.junit.Assert.assertTrue
import org.junit.Test

class DailyCalendarMonthTest {
    @Test
    fun locale_first_day_controls_weekday_order_and_leading_positions() {
        val currentDate = LocalDate.of(2026, 7, 25)

        val sundayFirst = DailyCalendarMonth.create(
            capturedCurrentDate = currentDate,
            locale = Locale.US
        )
        val mondayFirst = DailyCalendarMonth.create(
            capturedCurrentDate = currentDate,
            locale = Locale.forLanguageTag("es-ES")
        )

        assertEquals(DayOfWeek.SUNDAY, sundayFirst.firstDayOfWeek)
        assertEquals(
            listOf(
                DayOfWeek.SUNDAY,
                DayOfWeek.MONDAY,
                DayOfWeek.TUESDAY,
                DayOfWeek.WEDNESDAY,
                DayOfWeek.THURSDAY,
                DayOfWeek.FRIDAY,
                DayOfWeek.SATURDAY
            ),
            sundayFirst.weekdayOrder
        )
        assertEquals(3, sundayFirst.leadingOutsideMonthCount)
        assertEquals(DayOfWeek.MONDAY, mondayFirst.firstDayOfWeek)
        assertEquals(2, mondayFirst.leadingOutsideMonthCount)
    }

    @Test
    fun leap_month_contains_every_date_once_in_complete_weeks() {
        val month = DailyCalendarMonth.create(
            capturedCurrentDate = LocalDate.of(2028, 2, 29),
            locale = Locale.UK
        )

        val dates = month.inMonthDates

        assertEquals(29, dates.size)
        assertEquals(LocalDate.of(2028, 2, 1), dates.first().date)
        assertEquals(LocalDate.of(2028, 2, 29), dates.last().date)
        assertEquals(0, month.positions.size % 7)
        assertEquals(dates.map(DailyCalendarDate::date).distinct(), dates.map(DailyCalendarDate::date))
    }

    @Test
    fun date_flags_preserve_factual_completion_without_modelling_missed_days() {
        val currentDate = LocalDate.of(2026, 7, 25)
        val pastCompletion = identity(LocalDate.of(2026, 7, 3))
        val todayCompletion = identity(currentDate)
        val futureCompletionFromTrustedClock = identity(LocalDate.of(2026, 7, 27))
        val month = DailyCalendarMonth.create(
            capturedCurrentDate = currentDate,
            completions = listOf(
                completion(pastCompletion),
                completion(todayCompletion),
                completion(futureCompletionFromTrustedClock)
            ),
            locale = Locale.UK
        )

        assertEquals(
            DailyCalendarDate(
                date = pastCompletion.localDate,
                isToday = false,
                isCompleted = true,
                isFuture = false
            ),
            month.date(pastCompletion.localDate)
        )
        assertEquals(
            DailyCalendarDate(
                date = currentDate,
                isToday = true,
                isCompleted = true,
                isFuture = false
            ),
            month.date(currentDate)
        )
        assertEquals(
            DailyCalendarDate(
                date = futureCompletionFromTrustedClock.localDate,
                isToday = false,
                isCompleted = true,
                isFuture = true
            ),
            month.date(futureCompletionFromTrustedClock.localDate)
        )
        assertEquals(
            DailyCalendarDate(
                date = LocalDate.of(2026, 7, 4),
                isToday = false,
                isCompleted = false,
                isFuture = false
            ),
            month.date(LocalDate.of(2026, 7, 4))
        )
    }

    @Test
    fun previous_month_allows_forward_navigation_but_current_and_future_months_do_not() {
        val currentDate = LocalDate.of(2026, 7, 25)
        val previous = DailyCalendarMonth.create(
            capturedCurrentDate = currentDate,
            displayedMonth = YearMonth.of(2026, 6)
        )
        val current = DailyCalendarMonth.create(
            capturedCurrentDate = currentDate
        )

        assertTrue(previous.canNavigateToNextMonth)
        assertFalse(current.canNavigateToNextMonth)
        assertThrows(IllegalArgumentException::class.java) {
            DailyCalendarMonth.create(
                capturedCurrentDate = currentDate,
                displayedMonth = YearMonth.of(2026, 8)
            )
        }
    }

    @Test
    fun duplicate_recipe_completions_for_one_date_are_rejected() {
        val completedDate = LocalDate.of(2026, 7, 3)

        assertThrows(IllegalArgumentException::class.java) {
            DailyCalendarMonth.create(
                capturedCurrentDate = LocalDate.of(2026, 7, 25),
                completions = listOf(
                    completion(identity(completedDate, recipe = "recipe-one")),
                    completion(identity(completedDate, recipe = "recipe-two"))
                )
            )
        }
    }
}

private val DailyCalendarMonth.inMonthDates: List<DailyCalendarDate>
    get() = positions.filterIsInstance<DailyCalendarPosition.InMonth>()
        .map(DailyCalendarPosition.InMonth::calendarDate)

private val DailyCalendarMonth.leadingOutsideMonthCount: Int
    get() = positions.takeWhile { position ->
        position == DailyCalendarPosition.OutsideMonth
    }.size

private fun DailyCalendarMonth.date(date: LocalDate): DailyCalendarDate = inMonthDates.single { calendarDate ->
    calendarDate.date == date
}

private fun identity(date: LocalDate, recipe: String = "daily-test-recipe"): DailyChallengeId = DailyChallengeId(
    localDate = date,
    recipeVersion = DailyRecipeVersion(recipe)
)

private fun completion(identity: DailyChallengeId): DailyCompletion = DailyCompletion(
    identity = identity,
    elapsedTime = null
)
