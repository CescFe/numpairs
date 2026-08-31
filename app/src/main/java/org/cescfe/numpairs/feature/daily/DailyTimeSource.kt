package org.cescfe.numpairs.feature.daily

import android.os.SystemClock

data class DailyTimeReading(val epochMilliseconds: Long, val monotonicMilliseconds: Long) {
    init {
        require(epochMilliseconds >= 0) {
            "Daily wall-clock time must not precede the Unix epoch."
        }
        require(monotonicMilliseconds >= 0) {
            "Daily monotonic time must not be negative."
        }
    }
}

fun interface DailyTimeSource {
    fun read(): DailyTimeReading
}

object SystemDailyTimeSource : DailyTimeSource {
    override fun read(): DailyTimeReading = DailyTimeReading(
        epochMilliseconds = System.currentTimeMillis(),
        monotonicMilliseconds = SystemClock.elapsedRealtime()
    )
}
