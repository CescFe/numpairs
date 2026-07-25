package org.cescfe.numpairs.feature.game

data class GameSuccessOverlayContent(
    val message: String,
    val supportingText: String,
    val primaryActionLabel: String,
    val onPrimaryAction: () -> Unit,
    val secondaryActionLabel: String? = null,
    val onSecondaryAction: (() -> Unit)? = null,
    val tertiaryActionLabel: String? = null,
    val onTertiaryAction: (() -> Unit)? = null,
    val onBackRequested: (() -> Unit)? = null
) {
    init {
        require((secondaryActionLabel == null) == (onSecondaryAction == null)) {
            "A custom secondary completion action requires both a label and callback."
        }
        require((tertiaryActionLabel == null) == (onTertiaryAction == null)) {
            "A custom tertiary completion action requires both a label and callback."
        }
    }
}
