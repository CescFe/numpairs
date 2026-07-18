package org.cescfe.numpairs.feature.generated.selector.ui

@JvmInline
value class GeneratedDifficultyOptionId(val value: String) {
    init {
        require(value.isNotBlank()) {
            "Generated difficulty option id must not be blank."
        }
    }
}

data class GeneratedDifficultyOptionUiState(val id: GeneratedDifficultyOptionId, val label: String) {
    init {
        require(label.isNotBlank()) {
            "Generated difficulty option label must not be blank."
        }
    }
}

data class GeneratedDifficultySelectorUiState(
    val modeName: String,
    val options: List<GeneratedDifficultyOptionUiState>,
    val selectedOptionId: GeneratedDifficultyOptionId
) {
    init {
        require(modeName.isNotBlank()) {
            "Generated difficulty selector mode name must not be blank."
        }
        require(options.isNotEmpty()) {
            "Generated difficulty selector must expose at least one option."
        }
        require(options.map { option -> option.id }.distinct().size == options.size) {
            "Generated difficulty selector option ids must be unique."
        }
        require(options.any { option -> option.id == selectedOptionId }) {
            "Selected generated difficulty must be present in the selector options."
        }
    }

    val selectedOption: GeneratedDifficultyOptionUiState
        get() = options.single { option -> option.id == selectedOptionId }
}
