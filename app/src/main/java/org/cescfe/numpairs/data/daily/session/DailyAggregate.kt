package org.cescfe.numpairs.data.daily.session

import org.cescfe.numpairs.domain.daily.DailyCandidateIndex
import org.cescfe.numpairs.domain.daily.DailyChallengeId
import org.cescfe.numpairs.domain.daily.DailyCompletion
import org.cescfe.numpairs.domain.daily.DailyMovementCount
import org.cescfe.numpairs.domain.daily.DailyRecipeContract
import org.cescfe.numpairs.domain.daily.DailyRecipeContracts
import org.cescfe.numpairs.domain.daily.DailyTimingStartInstant
import org.cescfe.numpairs.domain.puzzle.model.Expression
import org.cescfe.numpairs.domain.puzzle.model.Operator
import org.cescfe.numpairs.domain.puzzle.model.Puzzle
import org.cescfe.numpairs.domain.puzzle.model.StripItem

const val DAILY_AGGREGATE_SCHEMA_VERSION: Int = 3
internal const val INITIAL_DAILY_AGGREGATE_SCHEMA_VERSION: Int = 1
internal const val TIMED_DAILY_AGGREGATE_SCHEMA_VERSION: Int = 2
internal const val MAX_DAILY_COMPLETION_COUNT: Int = 10_000

@JvmInline
value class DailySessionId(val value: String) {
    init {
        require(value.isNotBlank()) {
            "Daily Session id must not be blank."
        }
    }
}

data class DailySessionSnapshot(
    val sessionId: DailySessionId,
    val dailyChallengeId: DailyChallengeId,
    val candidateIndex: DailyCandidateIndex,
    val seed: Int,
    val initialPuzzle: Puzzle,
    val currentPuzzle: Puzzle,
    val timingStartInstant: DailyTimingStartInstant? = null,
    val movementCount: DailyMovementCount? = DailyMovementCount.ZERO
) {
    val recipeContract: DailyRecipeContract = requireNotNull(
        DailyRecipeContracts.catalog.resolveOrNull(dailyChallengeId.recipeVersion)
    ) {
        "Daily Session recipe ${dailyChallengeId.recipeVersion.value} is unsupported."
    }

    init {
        require(candidateIndex in recipeContract.candidateIndices) {
            "Daily Session candidate index must belong to its recipe."
        }
        require(seed == recipeContract.seedFor(dailyChallengeId, candidateIndex)) {
            "Daily Session seed must match its recipe candidate."
        }
        requirePuzzleMatchesRecipe(initialPuzzle, recipeContract)
        require(initialPuzzle.isIncomplete) {
            "Initial Daily Session puzzle must be incomplete."
        }
        require(
            initialPuzzle.board.tiles.all { tile ->
                tile.expression.leftOperand == Expression.Operand.Hidden &&
                    tile.expression.operator == Operator.Hidden &&
                    tile.expression.rightOperand == Expression.Operand.Hidden
            }
        ) {
            "Initial Daily Session tile expressions must be hidden."
        }
        require(initialPuzzle.strip.entries.none { entry -> entry.item is StripItem.PlayerEntered }) {
            "Initial Daily Session strip entries cannot be player-entered."
        }
        requireInitialStripMatchesRecipe(initialPuzzle, recipeContract)
        requireValidActivePuzzle(currentPuzzle)
    }
}

internal fun DailySessionSnapshot.requireValidActivePuzzle(activePuzzle: Puzzle) {
    requirePuzzleMatchesRecipe(activePuzzle, recipeContract)
    require(!activePuzzle.isSolved) {
        "An active Daily Session puzzle must be unsolved."
    }
    requireConsistentProgress(initialPuzzle = initialPuzzle, currentPuzzle = activePuzzle)
    requireValidCurrentAssignments(activePuzzle)
}

internal fun DailySessionSnapshot.requireValidSolvedPuzzle(solvedPuzzle: Puzzle) {
    requirePuzzleMatchesRecipe(solvedPuzzle, recipeContract)
    require(solvedPuzzle.isSolved) {
        "Daily completion puzzle must be solved."
    }
    requireConsistentProgress(initialPuzzle = initialPuzzle, currentPuzzle = solvedPuzzle)
    requireValidCurrentAssignments(solvedPuzzle)
}

data class DailyAggregate(
    val schemaVersion: Int = DAILY_AGGREGATE_SCHEMA_VERSION,
    val activeSession: DailySessionSnapshot? = null,
    val completions: List<DailyCompletion> = emptyList()
) {
    init {
        require(schemaVersion == DAILY_AGGREGATE_SCHEMA_VERSION) {
            "Daily aggregate schema version is unsupported."
        }
        require(completions.size <= MAX_DAILY_COMPLETION_COUNT) {
            "Daily aggregate completion count exceeds the supported bound."
        }
        require(completions == completions.sortedWith(DAILY_COMPLETION_COMPARATOR)) {
            "Daily aggregate completions must use canonical date and recipe order."
        }
        require(completions.map(DailyCompletion::identity).distinct().size == completions.size) {
            "Daily aggregate completion identities must be unique."
        }
        require(completions.map { completion -> completion.identity.localDate }.distinct().size == completions.size) {
            "Daily aggregate can contain at most one completion per local date."
        }
        require(
            activeSession?.dailyChallengeId?.localDate !in
                completions.map { completion -> completion.identity.localDate }
        ) {
            "Daily aggregate cannot keep an active session for a completed local date."
        }
    }
}

internal val DAILY_CHALLENGE_ID_COMPARATOR: Comparator<DailyChallengeId> =
    compareBy(DailyChallengeId::localDate, { identity -> identity.recipeVersion.value })

internal val DAILY_COMPLETION_COMPARATOR: Comparator<DailyCompletion> =
    compareBy(DAILY_CHALLENGE_ID_COMPARATOR, DailyCompletion::identity)

private fun requirePuzzleMatchesRecipe(puzzle: Puzzle, recipeContract: DailyRecipeContract) {
    require(puzzle.board.tiles.size == recipeContract.profile.size.boardTileCount) {
        "Daily Session board shape must match its recipe profile."
    }
    require(puzzle.strip.entries.size == recipeContract.profile.size.stripEntryCount) {
        "Daily Session strip shape must match its recipe profile."
    }
    require(
        puzzle.strip.entries.map { entry -> entry.id }.toSet() ==
            (0 until recipeContract.profile.size.stripEntryCount).toSet()
    ) {
        "Daily Session strip identities must match its recipe shape."
    }
}

private fun requireInitialStripMatchesRecipe(puzzle: Puzzle, recipeContract: DailyRecipeContract) {
    val knownValues = puzzle.strip.entries.mapNotNull { entry ->
        (entry.item as? StripItem.Known)?.value
    }
    require(knownValues.size in recipeContract.profile.initialStripMaskPolicy.knownEntryCountRange) {
        "Initial Daily Session known-entry count must match its recipe profile."
    }
    require(knownValues.all { value -> value in recipeContract.profile.stripValuePolicy.valueRange }) {
        "Initial Daily Session known values must match its recipe profile."
    }
    require(
        knownValues.groupingBy { value -> value }
            .eachCount()
            .all { (_, count) -> count <= recipeContract.profile.stripValuePolicy.maxOccurrencesPerValue }
    ) {
        "Initial Daily Session known-value occurrences must match its recipe profile."
    }
}

private fun requireConsistentProgress(initialPuzzle: Puzzle, currentPuzzle: Puzzle) {
    require(
        initialPuzzle.board.tiles.map { tile -> tile.result } ==
            currentPuzzle.board.tiles.map { tile -> tile.result }
    ) {
        "Daily Session puzzle results must remain unchanged."
    }
    require(
        initialPuzzle.strip.entries.map { entry -> entry.id }.toSet() ==
            currentPuzzle.strip.entries.map { entry -> entry.id }.toSet()
    ) {
        "Daily Session strip entry identities must remain unchanged."
    }

    val currentItemsByEntryId = currentPuzzle.strip.entries.associate { entry ->
        entry.id to entry.item
    }
    initialPuzzle.strip.entries.forEach { initialEntry ->
        val currentItem = currentItemsByEntryId.getValue(initialEntry.id)
        when (val initialItem = initialEntry.item) {
            StripItem.Hidden -> require(currentItem !is StripItem.Known) {
                "Hidden Daily Session strip entries cannot become known entries."
            }

            is StripItem.Known -> require(currentItem == initialItem) {
                "Known Daily Session strip entries must remain unchanged."
            }

            is StripItem.PlayerEntered -> error(
                "Initial Daily Session strip entries cannot be player-entered."
            )
        }
    }
}

private fun requireValidCurrentAssignments(currentPuzzle: Puzzle) {
    val visibleValuesByEntryId = currentPuzzle.strip.entries.associate { entry ->
        entry.id to when (val item = entry.item) {
            StripItem.Hidden -> null
            is StripItem.Known -> item.value
            is StripItem.PlayerEntered -> item.value
        }
    }
    currentPuzzle.board.tiles.forEach { tile ->
        listOf(tile.expression.leftOperand, tile.expression.rightOperand).forEach { operand ->
            when (operand) {
                Expression.Operand.Hidden -> Unit

                is Expression.Operand.Known -> require(
                    operand.stripEntryId != null &&
                        visibleValuesByEntryId[operand.stripEntryId] == operand.value
                ) {
                    "Daily Session operands must reference matching visible strip entries."
                }
            }
        }
    }
}
