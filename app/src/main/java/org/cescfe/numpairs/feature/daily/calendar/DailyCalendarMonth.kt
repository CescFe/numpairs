package org.cescfe.numpairs.feature.daily.calendar

import java.time.DayOfWeek
import java.time.LocalDate
import java.time.YearMonth
import java.time.temporal.WeekFields
import java.util.Locale
import org.cescfe.numpairs.domain.daily.DailyChallengeId

data class DailyCalendarDate(
    val date: LocalDate,
    val isToday: Boolean,
    val isCompleted: Boolean,
    val isFuture: Boolean
) {
    init {
        require(!isToday || !isFuture) {
            "Today cannot be a future Daily calendar date."
        }
    }
}

sealed interface DailyCalendarPosition {
    data object OutsideMonth : DailyCalendarPosition

    data class InMonth(val calendarDate: DailyCalendarDate) : DailyCalendarPosition
}

data class DailyCalendarMonth(
    val capturedCurrentDate: LocalDate,
    val displayedMonth: YearMonth,
    val firstDayOfWeek: DayOfWeek,
    val weekdayOrder: List<DayOfWeek>,
    val positions: List<DailyCalendarPosition>
) {
    val currentMonth: YearMonth = YearMonth.from(capturedCurrentDate)

    val canNavigateToNextMonth: Boolean
        get() = displayedMonth < currentMonth

    init {
        require(displayedMonth <= currentMonth) {
            "Daily calendar cannot display a future month."
        }
        require(weekdayOrder == orderedWeekdays(firstDayOfWeek)) {
            "Daily calendar weekday order must start with its locale first day."
        }
        require(positions.size in 28..42 && positions.size % DAYS_PER_WEEK == 0) {
            "Daily calendar positions must contain complete weeks."
        }
        require(
            positions.filterIsInstance<DailyCalendarPosition.InMonth>()
                .map { position -> position.calendarDate.date } ==
                (1..displayedMonth.lengthOfMonth()).map(displayedMonth::atDay)
        ) {
            "Daily calendar must contain each in-month date exactly once and in order."
        }
    }

    companion object {
        fun create(
            capturedCurrentDate: LocalDate,
            displayedMonth: YearMonth = YearMonth.from(capturedCurrentDate),
            completedChallengeIds: Collection<DailyChallengeId> = emptyList(),
            locale: Locale = Locale.getDefault()
        ): DailyCalendarMonth {
            require(displayedMonth <= YearMonth.from(capturedCurrentDate)) {
                "Daily calendar cannot display a future month."
            }
            require(
                completedChallengeIds.map(DailyChallengeId::localDate).distinct().size ==
                    completedChallengeIds.size
            ) {
                "Daily calendar history can contain at most one completion per local date."
            }
            val firstDayOfWeek = WeekFields.of(locale).firstDayOfWeek
            val firstDate = displayedMonth.atDay(1)
            val leadingPositionCount = (
                firstDate.dayOfWeek.value -
                    firstDayOfWeek.value +
                    DAYS_PER_WEEK
                ) % DAYS_PER_WEEK
            val completedDates = completedChallengeIds.map(DailyChallengeId::localDate).toSet()
            val inMonthPositions = (1..displayedMonth.lengthOfMonth()).map { day ->
                val date = displayedMonth.atDay(day)
                DailyCalendarPosition.InMonth(
                    calendarDate = DailyCalendarDate(
                        date = date,
                        isToday = date == capturedCurrentDate,
                        isCompleted = date in completedDates,
                        isFuture = date > capturedCurrentDate
                    )
                )
            }
            val populatedPositionCount = leadingPositionCount + inMonthPositions.size
            val trailingPositionCount = (
                DAYS_PER_WEEK -
                    populatedPositionCount % DAYS_PER_WEEK
                ) % DAYS_PER_WEEK

            return DailyCalendarMonth(
                capturedCurrentDate = capturedCurrentDate,
                displayedMonth = displayedMonth,
                firstDayOfWeek = firstDayOfWeek,
                weekdayOrder = orderedWeekdays(firstDayOfWeek),
                positions = List(leadingPositionCount) {
                    DailyCalendarPosition.OutsideMonth
                } + inMonthPositions + List(trailingPositionCount) {
                    DailyCalendarPosition.OutsideMonth
                }
            )
        }
    }
}

private fun orderedWeekdays(firstDayOfWeek: DayOfWeek): List<DayOfWeek> = List(DAYS_PER_WEEK) { offset ->
    DayOfWeek.of((firstDayOfWeek.value - 1 + offset) % DAYS_PER_WEEK + 1)
}

private const val DAYS_PER_WEEK = 7
