package org.cescfe.numpairs.feature.menu.ui

@JvmInline
value class GeneratedDifficultyMenuOptionId(val value: String) {
    init {
        require(value.isNotBlank()) {
            "Generated difficulty Menu option id must not be blank."
        }
    }
}

data class GeneratedDifficultyMenuOptionUiState(val id: GeneratedDifficultyMenuOptionId, val label: String) {
    init {
        require(label.isNotBlank()) {
            "Generated difficulty Menu option label must not be blank."
        }
    }
}

data class GeneratedModeMenuUiState(
    val modeName: String,
    val challengeName: String,
    val difficultyOptions: List<GeneratedDifficultyMenuOptionUiState>,
    val selectedDifficultyOptionId: GeneratedDifficultyMenuOptionId
) {
    init {
        require(modeName.isNotBlank()) {
            "Generated mode Menu name must not be blank."
        }
        require(challengeName.isNotBlank()) {
            "Generated challenge Menu name must not be blank."
        }
        require(difficultyOptions.isNotEmpty()) {
            "Generated mode Menu must expose at least one difficulty option."
        }
        require(difficultyOptions.map { option -> option.id }.distinct().size == difficultyOptions.size) {
            "Generated mode Menu difficulty option ids must be unique."
        }
        require(difficultyOptions.any { option -> option.id == selectedDifficultyOptionId }) {
            "Selected generated difficulty must be present in the Menu options."
        }
    }
}
