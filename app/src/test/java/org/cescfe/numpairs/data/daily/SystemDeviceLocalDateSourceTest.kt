package org.cescfe.numpairs.data.daily

import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import org.junit.Assert.assertEquals
import org.junit.Test

class SystemDeviceLocalDateSourceTest {
    @Test
    fun current_date_changes_at_midnight_in_the_supplied_device_timezone() {
        var currentInstant = Instant.parse("2026-07-25T21:59:59Z")
        val source = SystemDeviceLocalDateSource(
            instantSource = { currentInstant },
            zoneIdSource = { ZoneId.of("Europe/Madrid") }
        )

        assertEquals(LocalDate.of(2026, 7, 25), source.currentDate())

        currentInstant = Instant.parse("2026-07-25T22:00:00Z")

        assertEquals(LocalDate.of(2026, 7, 26), source.currentDate())
    }

    @Test
    fun current_date_uses_the_current_supplied_device_timezone() {
        val currentInstant = Instant.parse("2026-07-25T23:30:00Z")
        var currentZone = ZoneId.of("America/New_York")
        val source = SystemDeviceLocalDateSource(
            instantSource = { currentInstant },
            zoneIdSource = { currentZone }
        )

        assertEquals(LocalDate.of(2026, 7, 25), source.currentDate())

        currentZone = ZoneId.of("Europe/Madrid")

        assertEquals(LocalDate.of(2026, 7, 26), source.currentDate())
    }
}
