package org.cescfe.numpairs.feature.game

enum class GameSuccessOverlayVisualStyle {
    SUCCESS,
    PERSONAL_RECORD
}

data class GameSuccessOverlayContent(
    val message: String,
    val supportingText: String,
    val highlightText: String? = null,
    val highlightContentDescription: String? = null,
    val contextText: String? = null,
    val contextContentDescription: String? = null,
    val visualStyle: GameSuccessOverlayVisualStyle = GameSuccessOverlayVisualStyle.SUCCESS,
    val badgeContentDescription: String? = null,
    val primaryActionLabel: String,
    val onPrimaryAction: () -> Unit,
    val secondaryActionLabel: String? = null,
    val onSecondaryAction: (() -> Unit)? = null,
    val tertiaryActionLabel: String? = null,
    val onTertiaryAction: (() -> Unit)? = null,
    val onBackRequested: (() -> Unit)? = null
) {
    init {
        require(highlightText != null || highlightContentDescription == null) {
            "A completion highlight description requires visible highlight text."
        }
        require(contextText != null || contextContentDescription == null) {
            "A completion context description requires visible context text."
        }
        require(
            visualStyle != GameSuccessOverlayVisualStyle.PERSONAL_RECORD ||
                !badgeContentDescription.isNullOrBlank()
        ) {
            "A personal-record completion badge requires an accessibility description."
        }
        require((secondaryActionLabel == null) == (onSecondaryAction == null)) {
            "A custom secondary completion action requires both a label and callback."
        }
        require((tertiaryActionLabel == null) == (onTertiaryAction == null)) {
            "A custom tertiary completion action requires both a label and callback."
        }
    }
}
