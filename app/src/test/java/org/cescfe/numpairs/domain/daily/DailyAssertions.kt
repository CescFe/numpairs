package org.cescfe.numpairs.domain.daily

import org.junit.Assert.assertEquals

internal fun assertDailyElapsedTimeEquals(expectedMilliseconds: Long, actual: DailyElapsedTime?) {
    val actualMilliseconds = requireNotNull(actual) {
        "Expected a Daily elapsed time."
    }.milliseconds
    assertEquals(expectedMilliseconds, actualMilliseconds)
}
