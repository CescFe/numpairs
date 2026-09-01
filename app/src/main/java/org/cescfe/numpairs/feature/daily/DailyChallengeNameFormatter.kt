package org.cescfe.numpairs.feature.daily

import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import org.cescfe.numpairs.R
import org.cescfe.numpairs.domain.daily.DailyChallengeId
import org.cescfe.numpairs.feature.generated.GeneratedChallengeId
import org.cescfe.numpairs.feature.generated.GeneratedModes

data class DailyChallengeNameCopy(val namesByChallengeId: Map<GeneratedChallengeId, String>) {
    init {
        require(namesByChallengeId.isNotEmpty()) {
            "Daily challenge names must not be empty."
        }
        require(namesByChallengeId.values.all(String::isNotBlank)) {
            "Daily challenge names must not be blank."
        }
    }
}

class DailyChallengeNameFormatter(private val recipeCatalog: DailyRecipeCatalog = DailyRecipes.catalog) {
    fun format(identity: DailyChallengeId, copy: DailyChallengeNameCopy): String {
        val challenge = recipeCatalog.challengeFor(identity)
        return requireNotNull(copy.namesByChallengeId[challenge.id]) {
            "No localized Daily challenge name is configured for ${challenge.id.value}."
        }
    }
}

@Composable
internal fun DailyChallengeId.localizedDailyChallengeName(): String {
    val threePairsName = stringResource(R.string.three_pairs_screen_title)
    val fourPairsName = stringResource(R.string.four_pairs_screen_title)
    val eightPairsName = stringResource(R.string.eight_pairs_screen_title)
    val lowDifficultyName = stringResource(R.string.generated_difficulty_low)
    val mediumDifficultyName = stringResource(R.string.generated_difficulty_medium)
    val copy = DailyChallengeNameCopy(
        namesByChallengeId = mapOf(
            GeneratedModes.THREE_PAIRS_LOW.id to stringResource(
                R.string.generated_challenge_title,
                threePairsName,
                lowDifficultyName
            ),
            GeneratedModes.FOUR_PAIRS_LOW.id to stringResource(
                R.string.generated_challenge_title,
                fourPairsName,
                lowDifficultyName
            ),
            GeneratedModes.THREE_PAIRS_MEDIUM.id to stringResource(
                R.string.generated_challenge_title,
                threePairsName,
                mediumDifficultyName
            ),
            GeneratedModes.FOUR_PAIRS_MEDIUM.id to stringResource(
                R.string.generated_challenge_title,
                fourPairsName,
                mediumDifficultyName
            ),
            GeneratedModes.EIGHT_PAIRS_MEDIUM.id to stringResource(
                R.string.generated_challenge_title,
                eightPairsName,
                mediumDifficultyName
            )
        )
    )
    return DailyChallengeNameFormatter().format(identity = this, copy = copy)
}
