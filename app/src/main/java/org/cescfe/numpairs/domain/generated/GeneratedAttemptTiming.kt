package org.cescfe.numpairs.domain.generated

@JvmInline
value class GeneratedElapsedTime(val milliseconds: Long) {
    init {
        require(milliseconds >= 0) {
            "Generated elapsed time must not be negative."
        }
    }
}

@JvmInline
value class GeneratedTimingStartInstant(val epochMilliseconds: Long) {
    init {
        require(epochMilliseconds >= 0) {
            "Generated timing start instant must not precede the Unix epoch."
        }
    }
}
