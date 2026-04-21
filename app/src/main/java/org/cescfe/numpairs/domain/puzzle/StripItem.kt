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

    fun completeWith(value: Int): StripItem = when (this) {
        Hidden -> PlayerEntered(value)
        is Known -> error("Known strip items cannot be completed.")
        is PlayerEntered -> PlayerEntered(value)
    }
}
