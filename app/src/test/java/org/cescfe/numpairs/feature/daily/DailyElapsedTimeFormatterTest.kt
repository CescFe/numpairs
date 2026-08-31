package org.cescfe.numpairs.feature.daily

import org.cescfe.numpairs.domain.daily.DailyElapsedTime
import org.junit.Assert.assertEquals
import org.junit.Test

class DailyElapsedTimeFormatterTest {
    @Test
    fun elapsed_time_truncates_subseconds_and_pads_minutes_and_seconds() {
        assertEquals("00:00", format(999))
        assertEquals("00:01", format(1_999))
        assertEquals("09:05", format(545_999))
    }

    @Test
    fun elapsed_time_keeps_minutes_beyond_one_hour() {
        assertEquals("59:59", format(3_599_999))
        assertEquals("60:00", format(3_600_000))
        assertEquals("125:07", format(7_507_999))
    }

    private fun format(milliseconds: Long): String = DailyElapsedTimeFormatter.format(DailyElapsedTime(milliseconds))
}
