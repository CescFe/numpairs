package org.cescfe.numpairs.domain.puzzle

data class StripEntry(val id: Int, val item: StripItem) {
    init {
        require(id >= 0) {
            "Strip entry id must be non-negative."
        }
    }
}
