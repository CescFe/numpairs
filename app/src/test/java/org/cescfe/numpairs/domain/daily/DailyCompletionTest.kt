package org.cescfe.numpairs.domain.daily

import java.time.LocalDate
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertThrows
import org.junit.Test

class DailyCompletionTest {
    @Test
    fun elapsed_time_uses_non_negative_millisecond_precision() {
        assertEquals(0, DailyElapsedTime(0).milliseconds)
        assertEquals(91_234, DailyElapsedTime(91_234).milliseconds)

        assertThrows(IllegalArgumentException::class.java) {
            DailyElapsedTime(-1)
        }
    }

    @Test
    fun timing_start_uses_non_negative_unix_epoch_milliseconds() {
        assertEquals(0, DailyTimingStartInstant(0).epochMilliseconds)
        assertEquals(
            1_798_761_600_123,
            DailyTimingStartInstant(1_798_761_600_123).epochMilliseconds
        )

        assertThrows(IllegalArgumentException::class.java) {
            DailyTimingStartInstant(-1)
        }
    }

    @Test
    fun legacy_completion_explicitly_owns_no_elapsed_time() {
        val completion = DailyCompletion(
            identity = DailyChallengeId(
                localDate = LocalDate.of(2027, 1, 1),
                recipeVersion = DailyRecipeVersion("retired-daily-recipe")
            ),
            elapsedTime = null
        )

        assertNull(completion.elapsedTime)
    }
}
