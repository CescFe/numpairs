package org.cescfe.numpairs.feature.daily.calendar

import java.time.LocalDate

object DailyCalendarScreenTestTags {
    const val SCREEN = "daily_calendar_screen"
    const val BACK_BUTTON = "daily_calendar_back_button"
    const val PREVIOUS_MONTH_BUTTON = "daily_calendar_previous_month_button"
    const val NEXT_MONTH_BUTTON = "daily_calendar_next_month_button"
    const val MONTH_HEADING = "daily_calendar_month_heading"

    fun date(date: LocalDate): String = "daily_calendar_date_$date"
}
