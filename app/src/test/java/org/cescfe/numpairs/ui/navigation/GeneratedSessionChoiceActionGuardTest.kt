package org.cescfe.numpairs.ui.navigation

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class GeneratedSessionChoiceActionGuardTest {
    @Test
    fun `only the first session choice action is handled`() {
        val guard = GeneratedSessionChoiceActionGuard()
        var handledCount = 0

        guard.handle {
            handledCount++
        }
        guard.handle {
            handledCount++
        }

        assertTrue(guard.isHandled)
        assertEquals(1, handledCount)
    }
}
