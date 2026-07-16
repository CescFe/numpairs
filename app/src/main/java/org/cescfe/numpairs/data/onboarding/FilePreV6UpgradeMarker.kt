package org.cescfe.numpairs.data.onboarding

import java.io.File
import java.io.IOException

class FilePreV6UpgradeMarker(private val markerFile: File) : PreV6UpgradeMarker {
    override fun isMarked(): Boolean = markerFile.isFile

    override fun mark() {
        runCatching {
            markerFile.parentFile?.mkdirs()
            markerFile.createNewFile()
        }.onFailure { throwable ->
            if (throwable !is IOException) {
                throw throwable
            }
        }
    }

    override fun clear() {
        if (markerFile.exists() && !markerFile.delete()) {
            throw IOException("Could not clear the pre-v6 upgrade marker.")
        }
    }
}
