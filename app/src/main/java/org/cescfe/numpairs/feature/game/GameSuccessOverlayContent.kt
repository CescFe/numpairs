package org.cescfe.numpairs.feature.game

data class GameSuccessOverlayContent(
    val message: String,
    val supportingText: String,
    val primaryActionLabel: String,
    val onPrimaryAction: () -> Unit
)
