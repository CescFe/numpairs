package org.cescfe.numpairs.data.onboarding

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TemporaryFolder

class FilePreV6UpgradeMarkerTest {
    @get:Rule
    val temporaryFolder = TemporaryFolder()

    @Test
    fun `marker can be written and consumed locally`() {
        val marker = FilePreV6UpgradeMarker(temporaryFolder.root.resolve("no_backup/pre_v6_upgrade"))

        assertFalse(marker.isMarked())
        marker.mark()
        assertTrue(marker.isMarked())
        marker.clear()
        assertFalse(marker.isMarked())
    }
}
