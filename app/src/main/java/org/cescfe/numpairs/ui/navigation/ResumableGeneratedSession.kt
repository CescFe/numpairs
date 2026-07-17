package org.cescfe.numpairs.ui.navigation

import org.cescfe.numpairs.data.generated.session.GeneratedSessionId
import org.cescfe.numpairs.data.generated.session.GeneratedSessionSnapshot
import org.cescfe.numpairs.feature.generated.GeneratedModeConfiguration
import org.cescfe.numpairs.feature.generated.GeneratedModeRegistry

internal data class ResumableGeneratedSession(val sessionId: GeneratedSessionId, val mode: GeneratedModeConfiguration)

internal fun GeneratedSessionSnapshot?.toResumableGeneratedSessionOrNull(
    modeRegistry: GeneratedModeRegistry
): ResumableGeneratedSession? {
    val snapshot = this ?: return null
    if (snapshot.currentPuzzle.isSolved) {
        return null
    }

    val mode = modeRegistry.all.singleOrNull { configuration ->
        configuration.id.value == snapshot.modeId
    } ?: return null
    if (mode.profile.id.value != snapshot.profileId) {
        return null
    }

    return ResumableGeneratedSession(
        sessionId = snapshot.sessionId,
        mode = mode
    )
}
