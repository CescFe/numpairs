package org.cescfe.numpairs.feature.menu

internal class DailyMenuActionGuard {
    private var isHandled: Boolean = false

    fun handle(action: () -> Unit) {
        if (isHandled) {
            return
        }
        isHandled = true
        action()
    }
}
