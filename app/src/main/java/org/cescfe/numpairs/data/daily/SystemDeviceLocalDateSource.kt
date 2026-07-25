package org.cescfe.numpairs.data.daily

import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import org.cescfe.numpairs.domain.daily.DeviceLocalDateSource

class SystemDeviceLocalDateSource(
    private val instantSource: () -> Instant = Instant::now,
    private val zoneIdSource: () -> ZoneId = ZoneId::systemDefault
) : DeviceLocalDateSource {
    override fun currentDate(): LocalDate = instantSource()
        .atZone(zoneIdSource())
        .toLocalDate()
}
