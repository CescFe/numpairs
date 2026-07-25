package org.cescfe.numpairs.domain.daily

import java.time.LocalDate

fun interface DeviceLocalDateSource {
    fun currentDate(): LocalDate
}
