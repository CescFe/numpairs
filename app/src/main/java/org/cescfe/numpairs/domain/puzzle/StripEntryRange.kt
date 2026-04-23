package org.cescfe.numpairs.domain.puzzle

data class StripEntryRange(val minimumValue: Int, val maximumValue: Int? = null) {
    init {
        require(minimumValue > 0) {
            "Strip entry minimum value must be a positive integer."
        }
        require(maximumValue == null || maximumValue >= minimumValue) {
            "Strip entry maximum value must be greater than or equal to the minimum value."
        }
    }

    operator fun contains(value: Int): Boolean =
        value >= minimumValue && (maximumValue == null || value <= maximumValue)
}
