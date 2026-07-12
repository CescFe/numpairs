package org.cescfe.numpairs.domain.generated.puzzle.internal

import org.cescfe.numpairs.domain.generated.puzzle.GeneratedPairsPuzzleValidationViolation

internal data class GeneratedPairsPuzzleValidationReport(
    val violations: List<GeneratedPairsPuzzleValidationViolation>
) {
    val isValid: Boolean
        get() = violations.isEmpty()
}
