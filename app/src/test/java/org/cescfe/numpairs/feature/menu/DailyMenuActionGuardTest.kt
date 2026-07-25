package org.cescfe.numpairs.feature.menu

import org.junit.Assert.assertEquals
import org.junit.Test

class DailyMenuActionGuardTest {
    @Test
    fun only_the_first_primary_activation_is_handled() {
        val guard = DailyMenuActionGuard()
        var actionCount = 0

        repeat(3) {
            guard.handle {
                actionCount += 1
            }
        }

        assertEquals(1, actionCount)
    }
}
