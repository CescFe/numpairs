package org.cescfe.numpairs.domain.puzzle

sealed interface StripItem {
    data object Hidden : StripItem

    data class Known(val value: Int) : StripItem {
        init {
            require(value > 0) {
                "Strip item value must be a positive integer."
            }
        }
    }

    data class PlayerEntered(val value: Int) : StripItem {
        init {
            require(value > 0) {
                "Strip item value must be a positive integer."
            }
        }
    }
}
