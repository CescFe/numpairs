package org.cescfe.numpairs.testing

import android.app.Instrumentation
import android.os.SystemClock
import android.view.InputDevice
import android.view.MotionEvent
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.test.SemanticsNodeInteraction
import androidx.test.platform.app.InstrumentationRegistry

internal fun SemanticsNodeInteraction.performGlobalTapNearTopLeft() {
    val node = fetchSemanticsNode("Failed to resolve the global tap target.")
    val positionOnScreen = node.positionOnScreen + Offset(1f, 1f)
    val instrumentation = InstrumentationRegistry.getInstrumentation()
    val downTime = SystemClock.uptimeMillis()

    instrumentation.sendTouchEvent(
        downTime = downTime,
        eventTime = downTime,
        action = MotionEvent.ACTION_DOWN,
        positionOnScreen = positionOnScreen
    )
    instrumentation.sendTouchEvent(
        downTime = downTime,
        eventTime = SystemClock.uptimeMillis(),
        action = MotionEvent.ACTION_UP,
        positionOnScreen = positionOnScreen
    )
}

private fun Instrumentation.sendTouchEvent(
    downTime: Long,
    eventTime: Long,
    action: Int,
    positionOnScreen: Offset
) {
    val event = touchEvent(
        downTime = downTime,
        eventTime = eventTime,
        action = action,
        positionOnScreen = positionOnScreen
    )
    try {
        sendPointerSync(event)
    } finally {
        event.recycle()
    }
}

private fun touchEvent(
    downTime: Long,
    eventTime: Long,
    action: Int,
    positionOnScreen: Offset
): MotionEvent = MotionEvent.obtain(
    downTime,
    eventTime,
    action,
    positionOnScreen.x,
    positionOnScreen.y,
    0
).apply {
    source = InputDevice.SOURCE_TOUCHSCREEN
}
