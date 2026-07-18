package org.cescfe.numpairs.feature.generated

import org.cescfe.numpairs.domain.generated.profile.DifficultyTier
import org.cescfe.numpairs.domain.generated.profile.GeneratedPuzzleProfile
import org.cescfe.numpairs.domain.generated.profile.GeneratedPuzzleProfileId
import org.cescfe.numpairs.domain.generated.profile.GeneratedPuzzleProfiles
import org.cescfe.numpairs.domain.generated.profile.GeneratedPuzzleSize

@JvmInline
value class GeneratedModeId(val value: String) {
    init {
        require(value.isNotBlank()) {
            "Generated mode id must not be blank."
        }
    }
}

@JvmInline
value class GeneratedChallengeId(val value: String) {
    init {
        require(value.isNotBlank()) {
            "Generated challenge id must not be blank."
        }
    }
}

data class GeneratedChallenge(
    val id: GeneratedChallengeId,
    val modeId: GeneratedModeId,
    val difficulty: DifficultyTier,
    val profile: GeneratedPuzzleProfile
) {
    init {
        require(profile.difficulty == difficulty) {
            "Generated challenge ${id.value} difficulty must match profile ${profile.id.value}."
        }
    }
}

class GeneratedModeConfiguration(
    val id: GeneratedModeId,
    val size: GeneratedPuzzleSize,
    challenges: Collection<GeneratedChallenge>
) {
    val challenges: List<GeneratedChallenge> = challenges.toList()

    init {
        require(this.challenges.isNotEmpty()) {
            "Generated mode ${id.value} must expose at least one challenge."
        }
        require(this.challenges.all { challenge -> challenge.modeId == id }) {
            "Every generated challenge must belong to mode ${id.value}."
        }
        require(this.challenges.all { challenge -> challenge.profile.size == size }) {
            "Every generated challenge in mode ${id.value} must use the mode puzzle size."
        }
        require(this.challenges.map(GeneratedChallenge::id).distinct().size == this.challenges.size) {
            "Generated challenge ids must be unique within mode ${id.value}."
        }
        require(this.challenges.map(GeneratedChallenge::difficulty).distinct().size == this.challenges.size) {
            "Generated difficulties must be unique within mode ${id.value}."
        }
        require(this.challenges.map { challenge -> challenge.profile.id }.distinct().size == this.challenges.size) {
            "Generated profile ids must be unique within mode ${id.value}."
        }
    }
}

class GeneratedChallengeCatalog(configurations: Collection<GeneratedModeConfiguration>) {
    val all: List<GeneratedModeConfiguration> = configurations.toList()
    val allChallenges: List<GeneratedChallenge> = all.flatMap(GeneratedModeConfiguration::challenges)
    private val configurationsById: Map<GeneratedModeId, GeneratedModeConfiguration> = all.associateBy(
        GeneratedModeConfiguration::id
    )
    private val challengesById: Map<GeneratedChallengeId, GeneratedChallenge> = allChallenges.associateBy(
        GeneratedChallenge::id
    )
    private val challengesByModeAndDifficulty: Map<Pair<GeneratedModeId, DifficultyTier>, GeneratedChallenge> =
        allChallenges.associateBy { challenge -> challenge.modeId to challenge.difficulty }
    private val challengesByModeAndProfile: Map<Pair<GeneratedModeId, GeneratedPuzzleProfileId>, GeneratedChallenge> =
        allChallenges.associateBy { challenge -> challenge.modeId to challenge.profile.id }

    init {
        require(all.isNotEmpty()) {
            "At least one generated mode must be configured."
        }
        require(configurationsById.size == all.size) {
            "Generated mode ids must be unique."
        }
        require(challengesById.size == allChallenges.size) {
            "Generated challenge ids must be unique."
        }
        require(challengesByModeAndDifficulty.size == allChallenges.size) {
            "A generated mode can expose only one challenge per difficulty."
        }
        require(challengesByModeAndProfile.size == allChallenges.size) {
            "A generated profile can belong to only one challenge in a mode."
        }
        require(allChallenges.map { challenge -> challenge.profile.id }.distinct().size == allChallenges.size) {
            "A generated profile can belong to only one configured challenge."
        }
    }

    fun resolve(id: GeneratedModeId): GeneratedModeConfiguration = requireNotNull(configurationsById[id]) {
        "No generated mode is configured for id ${id.value}."
    }

    fun resolveChallenge(id: GeneratedChallengeId): GeneratedChallenge = requireNotNull(challengesById[id]) {
        "No generated challenge is configured for id ${id.value}."
    }

    fun resolveChallenge(modeId: GeneratedModeId, difficulty: DifficultyTier): GeneratedChallenge =
        requireNotNull(challengesByModeAndDifficulty[modeId to difficulty]) {
            "No generated challenge is configured for mode ${modeId.value} and difficulty ${difficulty.name}."
        }

    fun resolveChallenge(modeId: GeneratedModeId, profileId: GeneratedPuzzleProfileId): GeneratedChallenge =
        requireNotNull(challengesByModeAndProfile[modeId to profileId]) {
            "No generated challenge is configured for mode ${modeId.value} and profile ${profileId.value}."
        }

    fun resolveChallengeOrNull(modeId: String, profileId: String): GeneratedChallenge? =
        allChallenges.singleOrNull { challenge ->
            challenge.modeId.value == modeId && challenge.profile.id.value == profileId
        }

    fun modeFor(challenge: GeneratedChallenge): GeneratedModeConfiguration {
        require(resolveChallenge(id = challenge.id) == challenge) {
            "Generated challenge ${challenge.id.value} is not configured by this catalog."
        }
        return resolve(id = challenge.modeId)
    }
}

object GeneratedModes {
    private val fourPairsId = GeneratedModeId("four-pairs")
    private val eightPairsId = GeneratedModeId("eight-pairs")

    val FOUR_PAIRS_LOW: GeneratedChallenge = GeneratedChallenge(
        id = GeneratedChallengeId("four-pairs-low"),
        modeId = fourPairsId,
        difficulty = DifficultyTier.LOW,
        profile = GeneratedPuzzleProfiles.FOUR_PAIRS_LOW
    )
    val FOUR_PAIRS_MEDIUM: GeneratedChallenge = GeneratedChallenge(
        id = GeneratedChallengeId("four-pairs-medium"),
        modeId = fourPairsId,
        difficulty = DifficultyTier.MEDIUM,
        profile = GeneratedPuzzleProfiles.FOUR_PAIRS_MEDIUM
    )
    val EIGHT_PAIRS_MEDIUM: GeneratedChallenge = GeneratedChallenge(
        id = GeneratedChallengeId("eight-pairs-medium"),
        modeId = eightPairsId,
        difficulty = DifficultyTier.MEDIUM,
        profile = GeneratedPuzzleProfiles.EIGHT_PAIRS_MEDIUM
    )
    val FOUR_PAIRS: GeneratedModeConfiguration = GeneratedModeConfiguration(
        id = fourPairsId,
        size = GeneratedPuzzleProfiles.FOUR_PAIRS_LOW.size,
        challenges = listOf(FOUR_PAIRS_LOW, FOUR_PAIRS_MEDIUM)
    )
    val EIGHT_PAIRS: GeneratedModeConfiguration = GeneratedModeConfiguration(
        id = eightPairsId,
        size = GeneratedPuzzleProfiles.EIGHT_PAIRS_MEDIUM.size,
        challenges = listOf(EIGHT_PAIRS_MEDIUM)
    )
    val catalog: GeneratedChallengeCatalog = GeneratedChallengeCatalog(
        configurations = listOf(FOUR_PAIRS, EIGHT_PAIRS)
    )
}
