package org.cescfe.numpairs.feature.daily

import org.cescfe.numpairs.domain.daily.DailyElapsedTime

internal object DailyElapsedTimeFormatter {
    fun format(elapsedTime: DailyElapsedTime): String {
        val totalSeconds = elapsedTime.milliseconds / MILLISECONDS_PER_SECOND
        val minutes = totalSeconds / SECONDS_PER_MINUTE
        val seconds = totalSeconds % SECONDS_PER_MINUTE
        return "${minutes.toString().padStart(MINIMUM_TIME_FIELD_WIDTH, '0')}:" +
            seconds.toString().padStart(MINIMUM_TIME_FIELD_WIDTH, '0')
    }
}

private const val MILLISECONDS_PER_SECOND = 1_000L
private const val SECONDS_PER_MINUTE = 60L
private const val MINIMUM_TIME_FIELD_WIDTH = 2
