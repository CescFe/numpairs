package org.cescfe.numpairs.feature.daily

import kotlinx.coroutines.flow.first
import org.cescfe.numpairs.data.daily.session.DailySessionRepository
import org.cescfe.numpairs.data.daily.session.DailySessionSnapshot
import org.cescfe.numpairs.data.daily.session.DailyState
import org.cescfe.numpairs.domain.daily.DailyCompletion

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
        val completion: DailyCompletion
    ) : CurrentDailyAvailability {
        init {
            require(completion.identity.localDate == currentDailyChallenge.identity.localDate) {
                "A current Daily completion must own the captured local date."
            }
        }
    }
}

class CurrentDailyAvailabilityResolver(
    private val currentDailyChallengeResolver: CurrentDailyChallengeResolver,
    private val dailySessionRepository: DailySessionRepository,
    private val recipeCatalog: DailyRecipeCatalog = DailyRecipes.catalog
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
        val completion = dailyState.completions.singleOrNull { completed ->
            completed.identity.localDate == currentDailyChallenge.identity.localDate
        }
        if (completion != null) {
            return CurrentDailyAvailability.CompletedToday(
                currentDailyChallenge = currentDailyChallenge,
                completion = completion
            )
        }

        val activeSession = dailyState.activeSession
        val resumableChallenge = activeSession?.takeIf { session ->
            session.dailyChallengeId.localDate == currentDailyChallenge.identity.localDate
        }?.let { session ->
            recipeCatalog.resolveOrNull(session.dailyChallengeId.recipeVersion)?.takeIf { recipe ->
                session.recipeContract == recipe.contract
            }?.let { recipe ->
                CurrentDailyChallenge(identity = session.dailyChallengeId, recipe = recipe)
            }
        }
        return if (activeSession != null && resumableChallenge != null) {
            CurrentDailyAvailability.ContinueToday(
                currentDailyChallenge = resumableChallenge,
                snapshot = activeSession
            )
        } else {
            CurrentDailyAvailability.StartToday(
                currentDailyChallenge = currentDailyChallenge
            )
        }
    }
}
