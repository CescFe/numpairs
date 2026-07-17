package org.cescfe.numpairs.ui.navigation

internal class GeneratedSessionChoiceActionGuard {
    var isHandled: Boolean = false
        private set

    fun handle(action: () -> Unit) {
        if (isHandled) {
            return
        }

        isHandled = true
        action()
    }
}
