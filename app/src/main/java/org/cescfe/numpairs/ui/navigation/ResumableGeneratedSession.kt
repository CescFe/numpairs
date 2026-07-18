package org.cescfe.numpairs.ui.navigation

import org.cescfe.numpairs.data.generated.session.GeneratedSessionId
import org.cescfe.numpairs.data.generated.session.GeneratedSessionSnapshot
import org.cescfe.numpairs.feature.generated.GeneratedChallenge
import org.cescfe.numpairs.feature.generated.GeneratedChallengeCatalog

internal data class ResumableGeneratedSession(val sessionId: GeneratedSessionId, val challenge: GeneratedChallenge)

internal fun GeneratedSessionSnapshot?.toResumableGeneratedSessionOrNull(
    challengeCatalog: GeneratedChallengeCatalog
): ResumableGeneratedSession? {
    val snapshot = this ?: return null
    if (snapshot.currentPuzzle.isSolved) {
        return null
    }

    val challenge = challengeCatalog.resolveChallengeOrNull(
        modeId = snapshot.modeId,
        profileId = snapshot.profileId
    ) ?: return null

    return ResumableGeneratedSession(
        sessionId = snapshot.sessionId,
        challenge = challenge
    )
}
