package org.cescfe.numpairs.domain.puzzle

data class Strip(val numbers: List<Int>) {
    init {
        require(numbers.size == NUMBER_COUNT) {
            "Strip must contain exactly $NUMBER_COUNT numbers."
        }
    }

    companion object {
        const val NUMBER_COUNT = 8
    }
}
