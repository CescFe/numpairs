package org.cescfe.numpairs.feature.menu.ui

import org.cescfe.numpairs.domain.daily.DailyChallengeId

sealed interface DailyMenuUiState {
    val identity: DailyChallengeId

    data class StartToday(override val identity: DailyChallengeId) : DailyMenuUiState

    data class ContinueToday(override val identity: DailyChallengeId) : DailyMenuUiState

    data class CompletedToday(override val identity: DailyChallengeId) : DailyMenuUiState
}
