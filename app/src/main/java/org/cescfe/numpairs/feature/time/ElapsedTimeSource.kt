package org.cescfe.numpairs.feature.time

import android.os.SystemClock

data class ElapsedTimeReading(val epochMilliseconds: Long, val monotonicMilliseconds: Long) {
    init {
        require(epochMilliseconds >= 0) {
            "Wall-clock time must not precede the Unix epoch."
        }
        require(monotonicMilliseconds >= 0) {
            "Monotonic time must not be negative."
        }
    }
}

fun interface ElapsedTimeSource {
    fun read(): ElapsedTimeReading
}

object SystemElapsedTimeSource : ElapsedTimeSource {
    override fun read(): ElapsedTimeReading = ElapsedTimeReading(
        epochMilliseconds = System.currentTimeMillis(),
        monotonicMilliseconds = SystemClock.elapsedRealtime()
    )
}
