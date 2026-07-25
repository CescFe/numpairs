package org.cescfe.numpairs.feature.daily

import kotlinx.coroutines.flow.first
import org.cescfe.numpairs.data.daily.session.DailySessionRepository
import org.cescfe.numpairs.data.daily.session.DailySessionSnapshot
import org.cescfe.numpairs.data.daily.session.DailyState
import org.cescfe.numpairs.domain.daily.DailyChallengeId

sealed interface CurrentDailyAvailability {
    val currentDailyChallenge: CurrentDailyChallenge

    data class StartToday(override val currentDailyChallenge: CurrentDailyChallenge) : CurrentDailyAvailability

    data class ContinueToday(
        override val currentDailyChallenge: CurrentDailyChallenge,
        val snapshot: DailySessionSnapshot
    ) : CurrentDailyAvailability {
        init {
            require(snapshot.dailyChallengeId == currentDailyChallenge.identity) {
                "A resumable Daily Session must match the captured current identity."
            }
            require(snapshot.recipeContract == currentDailyChallenge.recipe.contract) {
                "A resumable Daily Session must match the captured current recipe."
            }
        }
    }

    data class CompletedToday(
        override val currentDailyChallenge: CurrentDailyChallenge,
        val completion: DailyChallengeId
    ) : CurrentDailyAvailability {
        init {
            require(completion.localDate == currentDailyChallenge.identity.localDate) {
                "A current Daily completion must own the captured local date."
            }
        }
    }
}

class CurrentDailyAvailabilityResolver(
    private val currentDailyChallengeResolver: CurrentDailyChallengeResolver,
    private val dailySessionRepository: DailySessionRepository
) {
    suspend fun resolve(): CurrentDailyAvailability {
        val currentDailyChallenge = currentDailyChallengeResolver.resolve()
        val dailyState = dailySessionRepository.state.first()
        return resolve(
            currentDailyChallenge = currentDailyChallenge,
            dailyState = dailyState
        )
    }

    fun resolve(currentDailyChallenge: CurrentDailyChallenge, dailyState: DailyState): CurrentDailyAvailability {
        val completion = dailyState.completedChallengeIds.singleOrNull { completedIdentity ->
            completedIdentity.localDate == currentDailyChallenge.identity.localDate
        }
        if (completion != null) {
            return CurrentDailyAvailability.CompletedToday(
                currentDailyChallenge = currentDailyChallenge,
                completion = completion
            )
        }

        val activeSession = dailyState.activeSession
        return if (
            activeSession?.dailyChallengeId == currentDailyChallenge.identity &&
            activeSession.recipeContract == currentDailyChallenge.recipe.contract
        ) {
            CurrentDailyAvailability.ContinueToday(
                currentDailyChallenge = currentDailyChallenge,
                snapshot = activeSession
            )
        } else {
            CurrentDailyAvailability.StartToday(
                currentDailyChallenge = currentDailyChallenge
            )
        }
    }
}
