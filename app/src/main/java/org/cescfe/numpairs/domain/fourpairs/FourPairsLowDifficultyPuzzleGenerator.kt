package org.cescfe.numpairs.domain.fourpairs

import kotlin.random.Random
import org.cescfe.numpairs.domain.puzzle.Board
import org.cescfe.numpairs.domain.puzzle.Expression
import org.cescfe.numpairs.domain.puzzle.Operator
import org.cescfe.numpairs.domain.puzzle.Puzzle
import org.cescfe.numpairs.domain.puzzle.PuzzleCompletionState
import org.cescfe.numpairs.domain.puzzle.ResolvedOperandAssignment
import org.cescfe.numpairs.domain.puzzle.Strip
import org.cescfe.numpairs.domain.puzzle.StripItem
import org.cescfe.numpairs.domain.puzzle.Tile
import org.cescfe.numpairs.domain.puzzle.resolvedTile
import org.cescfe.numpairs.domain.puzzle.withHiddenExpression
import org.cescfe.numpairs.domain.puzzle.withKnownEntriesOnly

class FourPairsLowDifficultyPuzzleGenerator(
    private val random: Random = Random.Default,
    private val maxAttempts: Int = DEFAULT_MAX_ATTEMPTS
) {
    init {
        require(maxAttempts > 0) {
            "Maximum generation attempts must be positive."
        }
    }

    constructor(seed: Int) : this(random = Random(seed))

    fun generate(): Puzzle = generateWithSolution().initialPuzzle

    fun generateWithSolution(): FourPairsGeneratedPuzzle {
        repeat(maxAttempts) {
            val entries = generateSolvedEntries()
            val pairs = generatePairs(entries = entries) ?: return@repeat
            val knownAnchorEntryIds = selectKnownAnchorEntryIds(pairs = pairs) ?: return@repeat
            val solvedPuzzle = buildSolvedPuzzle(
                entries = entries,
                pairs = pairs
            )
            val generatedPuzzle = FourPairsGeneratedPuzzle(
                initialPuzzle = buildInitialPuzzle(
                    solvedPuzzle = solvedPuzzle,
                    knownEntryIds = knownAnchorEntryIds
                ),
                solvedPuzzle = solvedPuzzle
            )

            if (generatedPuzzle.isValidLowDifficultyPuzzle(pairs = pairs)) {
                return generatedPuzzle
            }
        }

        error("Unable to generate a low-difficulty 4 Pairs puzzle after $maxAttempts attempts.")
    }

    private fun generateSolvedEntries(): List<FourPairsStripEntry> = FourPairsLowDifficultyRules.stripValueRange
        .toList()
        .shuffled(random)
        .take(Strip.NUMBER_COUNT)
        .sorted()
        .mapIndexed { index, value ->
            FourPairsStripEntry(
                id = index,
                value = value
            )
        }

    private fun generatePairs(entries: List<FourPairsStripEntry>): List<FourPairsEntryPair>? = choosePairs(
        remainingEntries = entries,
        selectedPairs = emptyList(),
        usedResults = emptySet()
    )

    private fun choosePairs(
        remainingEntries: List<FourPairsStripEntry>,
        selectedPairs: List<FourPairsEntryPair>,
        usedResults: Set<Int>
    ): List<FourPairsEntryPair>? {
        if (remainingEntries.isEmpty()) {
            return selectedPairs.takeIf { pairs -> pairs.size == FourPairsLowDifficultyRules.PAIR_COUNT }
        }

        val firstEntry = remainingEntries.first()
        val candidatePairs = remainingEntries
            .drop(1)
            .shuffled(random)
            .map { secondEntry ->
                FourPairsEntryPair(
                    firstEntry = firstEntry,
                    secondEntry = secondEntry
                )
            }

        candidatePairs.forEach { candidatePair ->
            if (!candidatePair.canBeAddedTo(usedResults = usedResults)) {
                return@forEach
            }

            val nextPairs = choosePairs(
                remainingEntries = remainingEntries.filterNot { entry ->
                    entry.id == candidatePair.firstEntry.id || entry.id == candidatePair.secondEntry.id
                },
                selectedPairs = selectedPairs + candidatePair,
                usedResults = usedResults + candidatePair.resultValues
            )

            if (nextPairs != null) {
                return nextPairs
            }
        }

        return null
    }

    private fun buildSolvedPuzzle(entries: List<FourPairsStripEntry>, pairs: List<FourPairsEntryPair>): Puzzle = Puzzle(
        board = Board(
            tiles = pairs
                .flatMap { pair -> pair.solvedTiles() }
                .shuffled(random)
        ),
        strip = Strip.fromItems(items = entries.map { entry -> StripItem.Known(entry.value) })
    )

    private fun buildInitialPuzzle(solvedPuzzle: Puzzle, knownEntryIds: Set<Int>): Puzzle = Puzzle(
        board = Board(
            tiles = solvedPuzzle.board.tiles.map(Tile::withHiddenExpression)
        ),
        strip = solvedPuzzle.strip.withKnownEntriesOnly(knownEntryIds = knownEntryIds)
    )

    private fun selectKnownAnchorEntryIds(pairs: List<FourPairsEntryPair>): Set<Int>? {
        val pairKeyByEntryId = pairs.flatMap { pair ->
            pair.entryIds.map { entryId -> entryId to pair.key }
        }.toMap()

        val candidates = knownAnchorCandidates().filter { knownAnchorEntryIds ->
            knownAnchorEntryIds
                .map { entryId -> pairKeyByEntryId.getValue(entryId) }
                .toSet()
                .size == FourPairsLowDifficultyRules.KNOWN_STRIP_ENTRY_COUNT
        }

        return candidates.randomOrNull()
    }

    private fun knownAnchorCandidates(): List<Set<Int>> {
        val highestEntryId = Strip.NUMBER_COUNT - 1
        val candidates = mutableListOf<Set<Int>>()

        for (firstKnownEntryId in 0 until highestEntryId) {
            for (secondKnownEntryId in firstKnownEntryId + 1 until highestEntryId) {
                val knownAnchorEntryIds = setOf(firstKnownEntryId, secondKnownEntryId, highestEntryId)

                if (knownAnchorEntryIds.maxConsecutiveHiddenEntries() <=
                    FourPairsLowDifficultyRules.MAX_CONSECUTIVE_HIDDEN_ENTRIES
                ) {
                    candidates += knownAnchorEntryIds
                }
            }
        }

        return candidates
    }

    private fun FourPairsGeneratedPuzzle.isValidLowDifficultyPuzzle(pairs: List<FourPairsEntryPair>): Boolean =
        solvedPuzzle.completionState == PuzzleCompletionState.SOLVED &&
            initialPuzzle.hasHiddenTileExpressions() &&
            hasMatchingBoardResults() &&
            solvedPuzzle.hasLowDifficultyStripValues() &&
            solvedPuzzle.hasDistinctBoardResults() &&
            pairs.all { pair -> pair.hasLowDifficultyProduct() } &&
            initialPuzzle.hasExpectedStripMask()

    private fun FourPairsGeneratedPuzzle.hasMatchingBoardResults(): Boolean =
        initialPuzzle.board.tiles.map(Tile::result) == solvedPuzzle.board.tiles.map(Tile::result)

    private fun Puzzle.hasHiddenTileExpressions(): Boolean = board.tiles.all { tile ->
        tile.hasHiddenExpression()
    }

    private fun Puzzle.hasLowDifficultyStripValues(): Boolean {
        val values = knownStripValues()

        return values.size == Strip.NUMBER_COUNT &&
            values == values.sorted() &&
            values.toSet().size == Strip.NUMBER_COUNT &&
            values.all { value -> value in FourPairsLowDifficultyRules.stripValueRange }
    }

    private fun Puzzle.knownStripValues(): List<Int> = strip.entries.mapNotNull { entry ->
        (entry.item as? StripItem.Known)?.value
    }

    private fun Puzzle.hasDistinctBoardResults(): Boolean =
        board.tiles.map(Tile::result).toSet().size == Board.TILE_COUNT

    private fun FourPairsEntryPair.hasLowDifficultyProduct(): Boolean =
        product <= FourPairsLowDifficultyRules.MAX_MULTIPLICATION_RESULT

    private fun Puzzle.hasExpectedStripMask(): Boolean {
        val knownAnchorEntryIds = knownStripEntryIds()

        return knownAnchorEntryIds.size == FourPairsLowDifficultyRules.KNOWN_STRIP_ENTRY_COUNT &&
            strip.entries.count { entry -> entry.item == StripItem.Hidden } ==
            FourPairsLowDifficultyRules.HIDDEN_STRIP_ENTRY_COUNT &&
            knownAnchorEntryIds.contains(Strip.NUMBER_COUNT - 1) &&
            knownAnchorEntryIds.maxConsecutiveHiddenEntries() <=
            FourPairsLowDifficultyRules.MAX_CONSECUTIVE_HIDDEN_ENTRIES
    }

    private fun Puzzle.knownStripEntryIds(): Set<Int> = strip.entries
        .filter { entry -> entry.item is StripItem.Known }
        .map { entry -> entry.id }
        .toSet()

    private fun FourPairsEntryPair.solvedTiles(): List<Tile> = listOf(
        solvedTile(operator = Operator.ADDITION),
        solvedTile(operator = Operator.MULTIPLICATION)
    )

    private fun FourPairsEntryPair.solvedTile(operator: Operator): Tile = resolvedTile(
        leftOperand = ResolvedOperandAssignment(
            stripEntryId = firstEntry.id,
            value = firstEntry.value
        ),
        operator = operator,
        rightOperand = ResolvedOperandAssignment(
            stripEntryId = secondEntry.id,
            value = secondEntry.value
        )
    )

    private fun Tile.hasHiddenExpression(): Boolean = expression.leftOperand == Expression.Operand.Hidden &&
        expression.operator == Operator.Hidden &&
        expression.rightOperand == Expression.Operand.Hidden

    private fun <T> List<T>.randomOrNull(): T? = takeIf { it.isNotEmpty() }?.let { values ->
        values[random.nextInt(values.size)]
    }

    companion object {
        private const val DEFAULT_MAX_ATTEMPTS = 1_000
    }
}

private data class FourPairsStripEntry(val id: Int, val value: Int)

private data class FourPairsEntryPair(val firstEntry: FourPairsStripEntry, val secondEntry: FourPairsStripEntry) {
    val sum: Int = firstEntry.value + secondEntry.value
    val product: Int = firstEntry.value * secondEntry.value
    val resultValues: Set<Int> = setOf(sum, product)
    val entryIds: Set<Int> = setOf(firstEntry.id, secondEntry.id)
    val key: FourPairsEntryPairKey = FourPairsEntryPairKey(
        firstEntryId = minOf(firstEntry.id, secondEntry.id),
        secondEntryId = maxOf(firstEntry.id, secondEntry.id)
    )

    fun canBeAddedTo(usedResults: Set<Int>): Boolean =
        product <= FourPairsLowDifficultyRules.MAX_MULTIPLICATION_RESULT &&
            resultValues.size == 2 &&
            resultValues.none { result -> result in usedResults }
}

private data class FourPairsEntryPairKey(val firstEntryId: Int, val secondEntryId: Int)

private fun Set<Int>.maxConsecutiveHiddenEntries(): Int {
    var currentHiddenCount = 0
    var maxHiddenCount = 0

    repeat(Strip.NUMBER_COUNT) { entryId ->
        if (entryId in this) {
            currentHiddenCount = 0
        } else {
            currentHiddenCount++
            maxHiddenCount = maxOf(maxHiddenCount, currentHiddenCount)
        }
    }

    return maxHiddenCount
}
