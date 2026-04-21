package org.cescfe.numpairs.domain.puzzle

data class Strip(val items: List<StripItem>) {
    init {
        require(items.size == NUMBER_COUNT) {
            "Strip must contain exactly $NUMBER_COUNT items."
        }
    }

    companion object {
        const val NUMBER_COUNT = 8
    }
}
