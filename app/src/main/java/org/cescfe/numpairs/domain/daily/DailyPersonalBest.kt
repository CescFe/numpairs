package org.cescfe.numpairs.domain.daily

@JvmInline
value class DailyPersonalBestCategory(val generatedChallengeId: String) {
    init {
        require(generatedChallengeId.isNotBlank()) {
            "Daily personal-best category challenge id must not be blank."
        }
    }
}

fun interface DailyPersonalBestCategoryResolver {
    fun categoryFor(identity: DailyChallengeId): DailyPersonalBestCategory?
}

enum class DailyPersonalBestOutcome {
    BASELINE,
    PERSONAL_RECORD,
    NOT_RECORD
}

data class DailyPersonalBestResult(
    val category: DailyPersonalBestCategory?,
    val currentElapsedTime: DailyElapsedTime?,
    val previousBestElapsedTime: DailyElapsedTime?,
    val bestElapsedTime: DailyElapsedTime?,
    val outcome: DailyPersonalBestOutcome
) {
    init {
        when (outcome) {
            DailyPersonalBestOutcome.BASELINE -> {
                require(category != null && currentElapsedTime != null) {
                    "A Daily personal-best baseline requires a timed completion in a supported category."
                }
                require(previousBestElapsedTime == null && bestElapsedTime == currentElapsedTime) {
                    "A Daily personal-best baseline must establish its category best."
                }
            }

            DailyPersonalBestOutcome.PERSONAL_RECORD -> {
                require(category != null && currentElapsedTime != null && previousBestElapsedTime != null) {
                    "A Daily personal record requires a timed completion and a previous category best."
                }
                require(
                    currentElapsedTime.milliseconds < previousBestElapsedTime.milliseconds &&
                        bestElapsedTime == currentElapsedTime
                ) {
                    "A Daily personal record must strictly improve its category best."
                }
            }

            DailyPersonalBestOutcome.NOT_RECORD -> {
                if (category == null) {
                    require(previousBestElapsedTime == null && bestElapsedTime == null) {
                        "An unresolved Daily category cannot expose a personal best."
                    }
                } else if (currentElapsedTime == null) {
                    require(bestElapsedTime == previousBestElapsedTime) {
                        "An untimed Daily completion cannot change its category best."
                    }
                } else {
                    require(previousBestElapsedTime != null) {
                        "A first timed Daily completion in a supported category must be a baseline."
                    }
                    require(
                        currentElapsedTime.milliseconds >= previousBestElapsedTime.milliseconds &&
                            bestElapsedTime == previousBestElapsedTime
                    ) {
                        "A non-record Daily completion cannot improve its category best."
                    }
                }
            }
        }
    }
}

class DailyPersonalBestHistory(
    completions: Collection<DailyCompletion>,
    private val categoryResolver: DailyPersonalBestCategoryResolver
) {
    private val entries: List<Entry> = completions.map { completion ->
        Entry(
            completion = completion,
            category = categoryResolver.categoryFor(completion.identity)
        )
    }

    fun bestElapsedTimeFor(category: DailyPersonalBestCategory): DailyElapsedTime? =
        entries.bestElapsedTimeFor(category = category)

    fun resultFor(completion: DailyCompletion): DailyPersonalBestResult {
        val category = categoryResolver.categoryFor(completion.identity)
        if (category == null) {
            return DailyPersonalBestResult(
                category = null,
                currentElapsedTime = completion.elapsedTime,
                previousBestElapsedTime = null,
                bestElapsedTime = null,
                outcome = DailyPersonalBestOutcome.NOT_RECORD
            )
        }

        val previousBest = entries
            .asSequence()
            .filter { entry -> entry.completion.identity.localDate < completion.identity.localDate }
            .toList()
            .bestElapsedTimeFor(category = category)
        val currentElapsedTime = completion.elapsedTime
        return when {
            currentElapsedTime == null -> DailyPersonalBestResult(
                category = category,
                currentElapsedTime = null,
                previousBestElapsedTime = previousBest,
                bestElapsedTime = previousBest,
                outcome = DailyPersonalBestOutcome.NOT_RECORD
            )

            previousBest == null -> DailyPersonalBestResult(
                category = category,
                currentElapsedTime = currentElapsedTime,
                previousBestElapsedTime = null,
                bestElapsedTime = currentElapsedTime,
                outcome = DailyPersonalBestOutcome.BASELINE
            )

            currentElapsedTime.milliseconds < previousBest.milliseconds -> DailyPersonalBestResult(
                category = category,
                currentElapsedTime = currentElapsedTime,
                previousBestElapsedTime = previousBest,
                bestElapsedTime = currentElapsedTime,
                outcome = DailyPersonalBestOutcome.PERSONAL_RECORD
            )

            else -> DailyPersonalBestResult(
                category = category,
                currentElapsedTime = currentElapsedTime,
                previousBestElapsedTime = previousBest,
                bestElapsedTime = previousBest,
                outcome = DailyPersonalBestOutcome.NOT_RECORD
            )
        }
    }

    private data class Entry(val completion: DailyCompletion, val category: DailyPersonalBestCategory?)

    private fun Iterable<Entry>.bestElapsedTimeFor(category: DailyPersonalBestCategory): DailyElapsedTime? =
        asSequence()
            .filter { entry -> entry.category == category }
            .mapNotNull { entry -> entry.completion.elapsedTime }
            .minByOrNull(DailyElapsedTime::milliseconds)
}
