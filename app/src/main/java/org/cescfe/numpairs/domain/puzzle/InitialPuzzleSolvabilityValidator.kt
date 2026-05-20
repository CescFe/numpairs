package org.cescfe.numpairs.domain.puzzle

object InitialPuzzleSolvabilityValidator {
    fun validate(puzzle: Puzzle): InitialPuzzleValidationResult {
        if (!puzzle.board.hasOnlyHiddenTileExpressions) {
            return InitialPuzzleValidationResult.UNSUPPORTED_INPUT_SHAPE
        }

        val stripEntries = puzzle.strip.knownEntriesOrNull()
            ?: return InitialPuzzleValidationResult.UNSUPPORTED_INPUT_SHAPE
        val resultCounts = puzzle.board.tiles
            .map(Tile::result)
            .countsByValue()

        return if (stripEntries.canProduce(resultCounts)) {
            InitialPuzzleValidationResult.VALID
        } else {
            InitialPuzzleValidationResult.INVALID
        }
    }
}

enum class InitialPuzzleValidationResult {
    VALID,
    INVALID,
    UNSUPPORTED_INPUT_SHAPE
}

val InitialPuzzleValidationResult.isValid: Boolean
    get() = this == InitialPuzzleValidationResult.VALID

private data class KnownStripEntry(val id: Int, val value: Int)

private val Board.hasOnlyHiddenTileExpressions: Boolean
    get() = tiles.all { tile -> tile.expression.isFullyHidden }

private val Expression.isFullyHidden: Boolean
    get() = leftOperand == Expression.Operand.Hidden &&
        operator == Operator.Hidden &&
        rightOperand == Expression.Operand.Hidden

private fun Strip.knownEntriesOrNull(): List<KnownStripEntry>? {
    val knownEntries = mutableListOf<KnownStripEntry>()

    entries.forEach { stripEntry ->
        val value = stripEntry.item.knownValueForSolvability ?: return null
        knownEntries += KnownStripEntry(
            id = stripEntry.id,
            value = value
        )
    }

    return knownEntries
}

private val StripItem.knownValueForSolvability: Int?
    get() = when (this) {
        StripItem.Hidden -> null
        is StripItem.Known -> value
        is StripItem.PlayerEntered -> value
    }

private fun List<KnownStripEntry>.canProduce(resultCounts: Map<Int, Int>): Boolean {
    if (isEmpty()) {
        return resultCounts.isEmpty()
    }

    val leftEntry = first()
    return drop(1).any { rightEntry ->
        val remainingResults = resultCounts.withoutResultsFor(
            leftEntry = leftEntry,
            rightEntry = rightEntry
        ) ?: return@any false

        withoutEntries(
            leftEntry = leftEntry,
            rightEntry = rightEntry
        ).canProduce(remainingResults)
    }
}

private fun List<KnownStripEntry>.withoutEntries(
    leftEntry: KnownStripEntry,
    rightEntry: KnownStripEntry
): List<KnownStripEntry> = filter { entry ->
    entry.id != leftEntry.id && entry.id != rightEntry.id
}

private fun Map<Int, Int>.withoutResultsFor(leftEntry: KnownStripEntry, rightEntry: KnownStripEntry): Map<Int, Int>? =
    withoutOne(
        Operator.ADDITION.apply(
            leftOperand = leftEntry.value,
            rightOperand = rightEntry.value
        )
    )?.withoutOne(
        Operator.MULTIPLICATION.apply(
            leftOperand = leftEntry.value,
            rightOperand = rightEntry.value
        )
    )

private fun Map<Int, Int>.withoutOne(result: Int): Map<Int, Int>? {
    val count = get(result) ?: return null

    return if (count == 1) {
        this - result
    } else {
        this + (result to count - 1)
    }
}

private fun List<Int>.countsByValue(): Map<Int, Int> = groupingBy { value -> value }.eachCount()
