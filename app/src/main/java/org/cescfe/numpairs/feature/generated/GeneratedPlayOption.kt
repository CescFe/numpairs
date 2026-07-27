package org.cescfe.numpairs.feature.generated

import java.util.concurrent.ThreadLocalRandom
import org.cescfe.numpairs.domain.generated.profile.DifficultyTier

@JvmInline
value class GeneratedPlayOptionId(val value: String) {
    init {
        require(value.isNotBlank()) {
            "Generated play option id must not be blank."
        }
    }
}

data class GeneratedPlayOptionConfiguration(val id: GeneratedPlayOptionId, val difficulties: List<DifficultyTier>) {
    init {
        require(difficulties.isNotEmpty()) {
            "Generated play option ${id.value} must expose at least one difficulty."
        }
        require(difficulties.distinct().size == difficulties.size) {
            "Generated play option ${id.value} difficulties must be unique."
        }
    }

    fun supports(difficulty: DifficultyTier): Boolean = difficulty in difficulties
}

data class GeneratedPlayRequest(val optionId: GeneratedPlayOptionId, val difficulty: DifficultyTier) {
    init {
        val option = GeneratedPlayOptions.resolve(optionId)
        require(option.supports(difficulty)) {
            "Difficulty ${difficulty.name} is not supported for generated play option ${option.id.value}."
        }
    }
}

object GeneratedPlayOptions {
    val QUICK: GeneratedPlayOptionConfiguration = GeneratedPlayOptionConfiguration(
        id = GeneratedPlayOptionId("quick"),
        difficulties = listOf(DifficultyTier.LOW, DifficultyTier.MEDIUM)
    )
    val CLASSIC: GeneratedPlayOptionConfiguration = GeneratedPlayOptionConfiguration(
        id = GeneratedPlayOptionId("classic"),
        difficulties = listOf(DifficultyTier.MEDIUM, DifficultyTier.HARD)
    )
    val ALL: List<GeneratedPlayOptionConfiguration> = listOf(QUICK, CLASSIC)

    private val byId = ALL.associateBy(GeneratedPlayOptionConfiguration::id)

    init {
        require(byId.size == ALL.size) {
            "Generated play option ids must be unique."
        }
    }

    fun resolve(id: GeneratedPlayOptionId): GeneratedPlayOptionConfiguration = requireNotNull(byId[id]) {
        "No generated play option is configured for id ${id.value}."
    }
}

fun interface GeneratedQuickSelectionBucketSource {
    fun nextBucket(): Int
}

internal object ThreadLocalGeneratedQuickSelectionBucketSource : GeneratedQuickSelectionBucketSource {
    override fun nextBucket(): Int = ThreadLocalRandom.current().nextInt(QUICK_SELECTION_BUCKET_COUNT)
}

class GeneratedPlayChallengeSelector(
    private val challengeCatalog: GeneratedChallengeCatalog = GeneratedModes.catalog,
    private val quickBucketSource: GeneratedQuickSelectionBucketSource =
        ThreadLocalGeneratedQuickSelectionBucketSource
) {
    init {
        GeneratedPlayOptions.QUICK.difficulties.forEach { difficulty ->
            challengeCatalog.resolveChallenge(
                modeId = GeneratedModes.THREE_PAIRS.id,
                difficulty = difficulty
            )
            challengeCatalog.resolveChallenge(
                modeId = GeneratedModes.FOUR_PAIRS.id,
                difficulty = difficulty
            )
        }
        GeneratedPlayOptions.CLASSIC.difficulties.forEach { difficulty ->
            challengeCatalog.resolveChallenge(
                modeId = GeneratedModes.EIGHT_PAIRS.id,
                difficulty = difficulty
            )
        }
    }

    fun select(optionId: GeneratedPlayOptionId, difficulty: DifficultyTier): GeneratedChallenge {
        val option = GeneratedPlayOptions.resolve(optionId)
        require(option.supports(difficulty)) {
            "Difficulty ${difficulty.name} is not supported for generated play option ${option.id.value}."
        }

        return when (option.id) {
            GeneratedPlayOptions.QUICK.id -> selectQuick(difficulty)

            GeneratedPlayOptions.CLASSIC.id -> challengeCatalog.resolveChallenge(
                modeId = GeneratedModes.EIGHT_PAIRS.id,
                difficulty = difficulty
            )

            else -> error("Unsupported configured generated play option ${option.id.value}.")
        }
    }

    fun optionFor(challenge: GeneratedChallenge): GeneratedPlayOptionConfiguration {
        require(challengeCatalog.resolveChallenge(id = challenge.id) == challenge) {
            "Generated challenge ${challenge.id.value} is not configured by this selector."
        }

        return when (challenge.modeId) {
            GeneratedModes.THREE_PAIRS.id,
            GeneratedModes.FOUR_PAIRS.id -> GeneratedPlayOptions.QUICK

            GeneratedModes.EIGHT_PAIRS.id -> GeneratedPlayOptions.CLASSIC

            else -> error("No generated play option owns challenge ${challenge.id.value}.")
        }
    }

    private fun selectQuick(difficulty: DifficultyTier): GeneratedChallenge {
        val bucket = quickBucketSource.nextBucket()
        require(bucket in 0 until QUICK_SELECTION_BUCKET_COUNT) {
            "Quick selection bucket must be between 0 and ${QUICK_SELECTION_BUCKET_COUNT - 1}."
        }
        val modeId = if (bucket < QUICK_THREE_PAIRS_BUCKET_COUNT) {
            GeneratedModes.THREE_PAIRS.id
        } else {
            GeneratedModes.FOUR_PAIRS.id
        }

        return challengeCatalog.resolveChallenge(modeId = modeId, difficulty = difficulty)
    }
}

private const val QUICK_SELECTION_BUCKET_COUNT = 100
private const val QUICK_THREE_PAIRS_BUCKET_COUNT = 35
