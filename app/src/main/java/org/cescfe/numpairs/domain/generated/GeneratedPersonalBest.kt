package org.cescfe.numpairs.domain.generated

enum class GeneratedPersonalBestCategory(val generatedChallengeId: String) {
    THREE_PAIRS_LOW("three-pairs-low"),
    FOUR_PAIRS_LOW("four-pairs-low"),
    THREE_PAIRS_MEDIUM("three-pairs-medium"),
    FOUR_PAIRS_MEDIUM("four-pairs-medium"),
    EIGHT_PAIRS_MEDIUM("eight-pairs-medium"),
    EIGHT_PAIRS_HARD("eight-pairs-hard");

    companion object {
        fun fromGeneratedChallengeIdOrNull(generatedChallengeId: String): GeneratedPersonalBestCategory? =
            entries.singleOrNull { category -> category.generatedChallengeId == generatedChallengeId }
    }
}

fun interface GeneratedPersonalBestCategoryResolver {
    fun categoryFor(modeId: String, profileId: String): GeneratedPersonalBestCategory?
}

enum class GeneratedPersonalBestOutcome {
    BASELINE,
    PERSONAL_RECORD,
    NOT_RECORD
}

data class GeneratedPersonalBestResult(
    val category: GeneratedPersonalBestCategory?,
    val currentElapsedTime: GeneratedElapsedTime?,
    val previousBestElapsedTime: GeneratedElapsedTime?,
    val bestElapsedTime: GeneratedElapsedTime?,
    val outcome: GeneratedPersonalBestOutcome
) {
    init {
        when (outcome) {
            GeneratedPersonalBestOutcome.BASELINE -> {
                require(category != null && currentElapsedTime != null) {
                    "A generated personal-best baseline requires a timed completion in a supported category."
                }
                require(previousBestElapsedTime == null && bestElapsedTime == currentElapsedTime) {
                    "A generated personal-best baseline must establish its category best."
                }
            }

            GeneratedPersonalBestOutcome.PERSONAL_RECORD -> {
                require(category != null && currentElapsedTime != null && previousBestElapsedTime != null) {
                    "A generated personal record requires a timed completion and a previous category best."
                }
                require(
                    currentElapsedTime.milliseconds < previousBestElapsedTime.milliseconds &&
                        bestElapsedTime == currentElapsedTime
                ) {
                    "A generated personal record must strictly improve its category best."
                }
            }

            GeneratedPersonalBestOutcome.NOT_RECORD -> {
                if (category == null) {
                    require(previousBestElapsedTime == null && bestElapsedTime == null) {
                        "An unresolved generated category cannot expose a personal best."
                    }
                } else if (currentElapsedTime == null) {
                    require(bestElapsedTime == previousBestElapsedTime) {
                        "An untimed generated completion cannot change its category best."
                    }
                } else {
                    require(previousBestElapsedTime != null) {
                        "A first timed generated completion in a supported category must be a baseline."
                    }
                    require(
                        currentElapsedTime.milliseconds >= previousBestElapsedTime.milliseconds &&
                            bestElapsedTime == previousBestElapsedTime
                    ) {
                        "A non-record generated completion cannot improve its category best."
                    }
                }
            }
        }
    }

    companion object {
        fun classify(
            category: GeneratedPersonalBestCategory?,
            currentElapsedTime: GeneratedElapsedTime?,
            previousBestElapsedTime: GeneratedElapsedTime?
        ): GeneratedPersonalBestResult = when {
            category == null -> GeneratedPersonalBestResult(
                category = null,
                currentElapsedTime = currentElapsedTime,
                previousBestElapsedTime = null,
                bestElapsedTime = null,
                outcome = GeneratedPersonalBestOutcome.NOT_RECORD
            )

            currentElapsedTime == null -> GeneratedPersonalBestResult(
                category = category,
                currentElapsedTime = null,
                previousBestElapsedTime = previousBestElapsedTime,
                bestElapsedTime = previousBestElapsedTime,
                outcome = GeneratedPersonalBestOutcome.NOT_RECORD
            )

            previousBestElapsedTime == null -> GeneratedPersonalBestResult(
                category = category,
                currentElapsedTime = currentElapsedTime,
                previousBestElapsedTime = null,
                bestElapsedTime = currentElapsedTime,
                outcome = GeneratedPersonalBestOutcome.BASELINE
            )

            currentElapsedTime.milliseconds < previousBestElapsedTime.milliseconds -> GeneratedPersonalBestResult(
                category = category,
                currentElapsedTime = currentElapsedTime,
                previousBestElapsedTime = previousBestElapsedTime,
                bestElapsedTime = currentElapsedTime,
                outcome = GeneratedPersonalBestOutcome.PERSONAL_RECORD
            )

            else -> GeneratedPersonalBestResult(
                category = category,
                currentElapsedTime = currentElapsedTime,
                previousBestElapsedTime = previousBestElapsedTime,
                bestElapsedTime = previousBestElapsedTime,
                outcome = GeneratedPersonalBestOutcome.NOT_RECORD
            )
        }
    }
}
