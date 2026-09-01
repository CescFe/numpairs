package org.cescfe.numpairs.data.daily.session

import java.time.LocalDate
import org.cescfe.numpairs.domain.daily.DailyCandidateIndex
import org.cescfe.numpairs.domain.daily.DailyChallengeId
import org.cescfe.numpairs.domain.daily.DailyCompletion
import org.cescfe.numpairs.domain.daily.DailyElapsedTime
import org.cescfe.numpairs.domain.daily.DailyMovementCount
import org.cescfe.numpairs.domain.daily.DailyRecipeContracts
import org.cescfe.numpairs.domain.daily.DailyRecipeVersion
import org.cescfe.numpairs.domain.daily.DailyTimingStartInstant
import org.cescfe.numpairs.domain.generated.generation.GeneratedPairsPuzzleGenerationOutcome
import org.cescfe.numpairs.domain.generated.generation.GeneratedPairsPuzzleGenerator
import org.cescfe.numpairs.domain.generated.generation.GeneratedPuzzleGenerationRequest
import org.cescfe.numpairs.domain.generated.profile.GeneratedPuzzleProfile
import org.cescfe.numpairs.domain.generated.puzzle.GeneratedPairsPuzzle
import org.cescfe.numpairs.domain.puzzle.model.Board
import org.cescfe.numpairs.domain.puzzle.model.Puzzle
import org.cescfe.numpairs.domain.puzzle.model.Strip
import org.cescfe.numpairs.domain.puzzle.model.StripItem

internal data class GeneratedDailyFixture(
    val identity: DailyChallengeId,
    val candidateIndex: DailyCandidateIndex,
    val seed: Int,
    val generatedPuzzle: GeneratedPairsPuzzle
) {
    fun snapshot(
        sessionId: String = "daily-session-${identity.canonicalLocalDate}",
        currentPuzzle: Puzzle = generatedPuzzle.initialPuzzle,
        timingStartInstant: DailyTimingStartInstant? = null,
        movementCount: DailyMovementCount? = DailyMovementCount.ZERO
    ): DailySessionSnapshot = DailySessionSnapshot(
        sessionId = DailySessionId(sessionId),
        dailyChallengeId = identity,
        candidateIndex = candidateIndex,
        seed = seed,
        initialPuzzle = generatedPuzzle.initialPuzzle,
        currentPuzzle = currentPuzzle,
        timingStartInstant = timingStartInstant,
        movementCount = movementCount
    )

    fun progressPuzzle(): Puzzle {
        val solvedValuesByEntryId = generatedPuzzle.solvedPuzzle.strip.entries.associate { entry ->
            entry.id to (entry.item as StripItem.Known).value
        }
        return generatedPuzzle.initialPuzzle.copy(
            board = Board(
                tiles = generatedPuzzle.initialPuzzle.board.tiles.mapIndexed { index, tile ->
                    if (index == 0) generatedPuzzle.solvedPuzzle.board.tiles[index] else tile
                }
            ),
            strip = Strip.fromEntries(
                generatedPuzzle.initialPuzzle.strip.entries.map { entry ->
                    if (entry.item == StripItem.Hidden) {
                        entry.copy(item = StripItem.PlayerEntered(solvedValuesByEntryId.getValue(entry.id)))
                    } else {
                        entry
                    }
                }
            )
        )
    }

    fun solvedProgressPuzzle(): Puzzle {
        val solvedValuesByEntryId = generatedPuzzle.solvedPuzzle.strip.entries.associate { entry ->
            entry.id to (entry.item as StripItem.Known).value
        }
        return Puzzle(
            board = generatedPuzzle.solvedPuzzle.board,
            strip = Strip.fromEntries(
                generatedPuzzle.initialPuzzle.strip.entries.map { entry ->
                    if (entry.item == StripItem.Hidden) {
                        entry.copy(item = StripItem.PlayerEntered(solvedValuesByEntryId.getValue(entry.id)))
                    } else {
                        entry
                    }
                }
            )
        )
    }
}

internal fun generatedDailyFixture(
    date: LocalDate = LocalDate.of(2027, 4, 18),
    candidateIndex: DailyCandidateIndex = DailyCandidateIndex(0)
): GeneratedDailyFixture {
    val recipe = DailyRecipeContracts.FOUR_PAIRS_LOW_V1
    val identity = recipe.identityFor(date)
    val seed = recipe.seedFor(identity, candidateIndex)
    return GeneratedDailyFixture(
        identity = identity,
        candidateIndex = candidateIndex,
        seed = seed,
        generatedPuzzle = generatedPuzzleFixture(
            profile = recipe.profile,
            seed = seed
        )
    )
}

internal fun dailyChallengeId(
    date: LocalDate,
    recipeVersion: DailyRecipeVersion = DailyRecipeContracts.FOUR_PAIRS_LOW_V1.version
): DailyChallengeId = DailyChallengeId(
    localDate = date,
    recipeVersion = recipeVersion
)

internal fun dailyCompletion(
    identity: DailyChallengeId,
    elapsedMilliseconds: Long? = null,
    movementCount: Long? = null
): DailyCompletion = DailyCompletion(
    identity = identity,
    elapsedTime = elapsedMilliseconds?.let(::DailyElapsedTime),
    movementCount = movementCount?.let(::DailyMovementCount)
)

internal fun generatedPuzzleFixture(profile: GeneratedPuzzleProfile, seed: Int): GeneratedPairsPuzzle {
    val outcome = GeneratedPairsPuzzleGenerator(profile).generate(
        GeneratedPuzzleGenerationRequest(profile = profile, seed = seed)
    )
    return (outcome as GeneratedPairsPuzzleGenerationOutcome.Generated).puzzle
}
