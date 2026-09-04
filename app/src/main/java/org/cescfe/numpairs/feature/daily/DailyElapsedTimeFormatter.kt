package org.cescfe.numpairs.feature.daily

import org.cescfe.numpairs.domain.daily.DailyElapsedTime
import org.cescfe.numpairs.feature.time.ElapsedTimeFormatter

internal object DailyElapsedTimeFormatter {
    fun format(elapsedTime: DailyElapsedTime): String = ElapsedTimeFormatter.format(elapsedTime.milliseconds)
}
