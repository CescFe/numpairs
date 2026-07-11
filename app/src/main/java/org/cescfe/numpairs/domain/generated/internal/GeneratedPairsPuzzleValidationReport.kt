package org.cescfe.numpairs.domain.generated.internal

import org.cescfe.numpairs.domain.generated.GeneratedPairsPuzzleValidationViolation

internal data class GeneratedPairsPuzzleValidationReport(
    val violations: List<GeneratedPairsPuzzleValidationViolation>
) {
    val isValid: Boolean
        get() = violations.isEmpty()
}
