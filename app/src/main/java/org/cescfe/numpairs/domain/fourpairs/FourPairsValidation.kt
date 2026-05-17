package org.cescfe.numpairs.domain.fourpairs

sealed interface FourPairsValidationResult {
    val isValid: Boolean

    data object Valid : FourPairsValidationResult {
        override val isValid: Boolean = true
    }

    data class Invalid(val failures: Set<FourPairsValidationFailure>) : FourPairsValidationResult {
        init {
            require(failures.isNotEmpty()) {
                "Invalid Four Pairs validation results require at least one failure."
            }
        }

        override val isValid: Boolean = false
    }
}

enum class FourPairsValidationFailure {
    NO_SOLUTION,
    INVALID_SOLUTION,
    OUTSIDE_LOW_DIFFICULTY_PROFILE
}
