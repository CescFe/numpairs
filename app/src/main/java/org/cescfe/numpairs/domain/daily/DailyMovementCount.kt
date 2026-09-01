package org.cescfe.numpairs.domain.daily

@JvmInline
value class DailyMovementCount(val value: Long) {
    init {
        require(value >= 0) {
            "Daily movement count must not be negative."
        }
    }

    fun incremented(): DailyMovementCount {
        require(value < Long.MAX_VALUE) {
            "Daily movement count cannot be incremented beyond its supported range."
        }
        return DailyMovementCount(value + 1)
    }

    companion object {
        val ZERO: DailyMovementCount = DailyMovementCount(0)
    }
}
