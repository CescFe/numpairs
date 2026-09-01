package org.cescfe.numpairs.feature.daily

import org.cescfe.numpairs.domain.daily.DailyMovementCount
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class DailyCompletionResultFormatterTest {
    @Test
    fun movement_count_preserves_zero_and_full_long_precision() {
        assertEquals("0", DailyMovementCountFormatter.format(DailyMovementCount.ZERO))
        assertEquals(
            Long.MAX_VALUE.toString(),
            DailyMovementCountFormatter.format(DailyMovementCount(Long.MAX_VALUE))
        )
    }

    @Test
    fun completion_result_combines_available_metrics_without_fabricating_missing_values() {
        assertEquals(
            "02:05 · 23 moves",
            DailyCompletionResultFormatter.format(
                formattedElapsedTime = "02:05",
                formattedMovementCount = "23 moves"
            )
        )
        assertEquals(
            "02:05",
            DailyCompletionResultFormatter.format(
                formattedElapsedTime = "02:05",
                formattedMovementCount = null
            )
        )
        assertEquals(
            "23 moves",
            DailyCompletionResultFormatter.format(
                formattedElapsedTime = null,
                formattedMovementCount = "23 moves"
            )
        )
        assertNull(
            DailyCompletionResultFormatter.format(
                formattedElapsedTime = null,
                formattedMovementCount = null
            )
        )
    }
}
