package org.cescfe.numpairs.domain.daily

@JvmInline
value class DailyElapsedTime(val milliseconds: Long) {
    init {
        require(milliseconds >= 0) {
            "Daily elapsed time must not be negative."
        }
    }
}

@JvmInline
value class DailyTimingStartInstant(val epochMilliseconds: Long) {
    init {
        require(epochMilliseconds >= 0) {
            "Daily timing start instant must not precede the Unix epoch."
        }
    }
}

data class DailyCompletion(val identity: DailyChallengeId, val elapsedTime: DailyElapsedTime?)
