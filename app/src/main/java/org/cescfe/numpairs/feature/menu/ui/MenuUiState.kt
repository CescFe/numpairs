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

data class GeneratedPlayOptionMenuUiState(
    val optionName: String,
    val selectionName: String,
    val difficultyOptions: List<GeneratedDifficultyMenuOptionUiState>,
    val selectedDifficultyOptionId: GeneratedDifficultyMenuOptionId
) {
    init {
        require(optionName.isNotBlank()) {
            "Generated play option Menu name must not be blank."
        }
        require(selectionName.isNotBlank()) {
            "Generated play option selection name must not be blank."
        }
        require(difficultyOptions.isNotEmpty()) {
            "Generated play option Menu must expose at least one difficulty."
        }
        require(difficultyOptions.map { option -> option.id }.distinct().size == difficultyOptions.size) {
            "Generated play option Menu difficulty ids must be unique."
        }
        require(difficultyOptions.any { option -> option.id == selectedDifficultyOptionId }) {
            "Selected generated difficulty must be present in the Menu options."
        }
    }
}
