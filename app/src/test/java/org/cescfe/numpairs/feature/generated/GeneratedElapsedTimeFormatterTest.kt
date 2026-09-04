package org.cescfe.numpairs.feature.generated

import org.cescfe.numpairs.domain.generated.GeneratedElapsedTime
import org.junit.Assert.assertEquals
import org.junit.Test

class GeneratedElapsedTimeFormatterTest {
    @Test
    fun `formats from zero truncates milliseconds and keeps minutes beyond an hour`() {
        assertEquals("00:00", GeneratedElapsedTimeFormatter.format(GeneratedElapsedTime(0)))
        assertEquals("02:05", GeneratedElapsedTimeFormatter.format(GeneratedElapsedTime(125_999)))
        assertEquals("60:01", GeneratedElapsedTimeFormatter.format(GeneratedElapsedTime(3_601_999)))
    }
}
