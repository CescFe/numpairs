package org.cescfe.numpairs.feature.game

enum class GameSuccessOverlayVisualStyle {
    SUCCESS,
    PERSONAL_RECORD
}

enum class GameSuccessOverlayStandardBadge {
    OK,
    CHECK
}

data class GameSuccessOverlayCopy(
    val message: String,
    val supportingText: String,
    val highlightText: String? = null,
    val highlightContentDescription: String? = null
) {
    init {
        require(message.isNotBlank()) {
            "A success-overlay message must not be blank."
        }
        require(supportingText.isNotBlank()) {
            "Success-overlay supporting text must not be blank."
        }
        require(highlightText != null || highlightContentDescription == null) {
            "A success-overlay highlight description requires visible highlight text."
        }
    }
}

data class GameSuccessOverlayContent(
    val message: String,
    val supportingText: String,
    val highlightText: String? = null,
    val highlightContentDescription: String? = null,
    val contextText: String? = null,
    val contextContentDescription: String? = null,
    val visualStyle: GameSuccessOverlayVisualStyle = GameSuccessOverlayVisualStyle.SUCCESS,
    val standardBadge: GameSuccessOverlayStandardBadge = GameSuccessOverlayStandardBadge.OK,
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
