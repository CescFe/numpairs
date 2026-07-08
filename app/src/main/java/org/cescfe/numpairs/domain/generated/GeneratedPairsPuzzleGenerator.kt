package org.cescfe.numpairs.domain.generated

import kotlin.random.Random
import org.cescfe.numpairs.domain.puzzle.assignment.ResolvedOperandAssignment
import org.cescfe.numpairs.domain.puzzle.construction.resolvedTile
import org.cescfe.numpairs.domain.puzzle.construction.withHiddenExpression
import org.cescfe.numpairs.domain.puzzle.construction.withKnownEntriesOnly
import org.cescfe.numpairs.domain.puzzle.model.Board
import org.cescfe.numpairs.domain.puzzle.model.Expression
import org.cescfe.numpairs.domain.puzzle.model.Operator
import org.cescfe.numpairs.domain.puzzle.model.Puzzle
import org.cescfe.numpairs.domain.puzzle.model.PuzzleCompletionState
import org.cescfe.numpairs.domain.puzzle.model.Strip
import org.cescfe.numpairs.domain.puzzle.model.StripItem
import org.cescfe.numpairs.domain.puzzle.model.Tile

class GeneratedPairsPuzzleGenerator(
    private val profile: GeneratedPuzzleProfile,
    private val random: Random = Random.Default,
    private val maxAttempts: Int = DEFAULT_MAX_ATTEMPTS
) {
    init {
        require(maxAttempts > 0) {
            "Maximum generation attempts must be positive."
        }
    }

    constructor(
        profile: GeneratedPuzzleProfile,
        seed: Int,
        maxAttempts: Int = DEFAULT_MAX_ATTEMPTS
    ) : this(
        profile = profile,
        random = Random(seed),
        maxAttempts = maxAttempts
    )

    fun generate(): Puzzle = generateWithSolution().initialPuzzle

    fun generateWithSolution(): GeneratedPairsPuzzle {
        repeat(maxAttempts) {
            val entries = generateSolvedEntries() ?: return@repeat
            val pairs = generatePairs(entries = entries) ?: return@repeat
            val knownEntryIds = selectKnownEntryIds(pairs = pairs) ?: return@repeat
            val solvedPuzzle = buildSolvedPuzzle(
                entries = entries,
                pairs = pairs
            )
            val generatedPuzzle = GeneratedPairsPuzzle(
                initialPuzzle = buildInitialPuzzle(
                    solvedPuzzle = solvedPuzzle,
                    knownEntryIds = knownEntryIds
                ),
                solvedPuzzle = solvedPuzzle
            )

            if (generatedPuzzle.isValidGeneratedPuzzle(pairs = pairs)) {
                return generatedPuzzle
            }
        }

        error(
            "Unable to generate a generated pairs puzzle for profile ${profile.id.value} after $maxAttempts attempts."
        )
    }

    private fun generateSolvedEntries(): List<GeneratedPairsStripEntry>? {
        val values = profile.stripValuePolicy.valueRange
            .flatMap { value ->
                List(profile.stripValuePolicy.maxOccurrencesPerValue) { value }
            }
            .shuffled(random)
            .take(profile.size.stripEntryCount)
            .sorted()

        return values
            .takeIf { selectedValues -> selectedValues.size == profile.size.stripEntryCount }
            ?.mapIndexed { index, value ->
                GeneratedPairsStripEntry(
                    id = index,
                    value = value
                )
            }
    }

    private fun generatePairs(entries: List<GeneratedPairsStripEntry>): List<GeneratedPairsEntryPair>? = choosePairs(
        remainingEntries = entries,
        selectedPairs = emptyList(),
        usedResults = emptySet(),
        productAnchorCount = 0
    )

    private fun choosePairs(
        remainingEntries: List<GeneratedPairsStripEntry>,
        selectedPairs: List<GeneratedPairsEntryPair>,
        usedResults: Set<Int>,
        productAnchorCount: Int
    ): List<GeneratedPairsEntryPair>? {
        if (!canStillSatisfyProductAnchorMix(
                productAnchorCount = productAnchorCount,
                remainingPairSlots = remainingEntries.size / 2
            )
        ) {
            return null
        }

        if (remainingEntries.isEmpty()) {
            return selectedPairs.takeIf { pairs ->
                pairs.size == profile.size.pairCount && pairs.hasExpectedProductAnchorMix()
            }
        }

        return candidatePairsFor(entries = remainingEntries).firstNotNullOfOrNull { candidatePair ->
            if (
                !candidatePair.canBeAddedTo(
                    usedResults = usedResults,
                    resultConstraints = profile.resultConstraints
                )
            ) {
                return@firstNotNullOfOrNull null
            }

            choosePairs(
                remainingEntries = remainingEntries.without(candidatePair),
                selectedPairs = selectedPairs + candidatePair,
                usedResults = usedResults + candidatePair.resultValues,
                productAnchorCount = productAnchorCount + candidatePair.productAnchorIncrement()
            )
        }
    }

    private fun canStillSatisfyProductAnchorMix(productAnchorCount: Int, remainingPairSlots: Int): Boolean {
        val productAnchorMix = profile.resultConstraints.productAnchorMix ?: return true

        return productAnchorCount <= productAnchorMix.countRange.last &&
            productAnchorCount + remainingPairSlots >= productAnchorMix.countRange.first
    }

    private fun List<GeneratedPairsEntryPair>.hasExpectedProductAnchorMix(): Boolean {
        val productAnchorMix = profile.resultConstraints.productAnchorMix ?: return true
        val productAnchorCount = count { pair ->
            pair.product > productAnchorMix.productResultGreaterThan
        }

        return productAnchorCount in productAnchorMix.countRange
    }

    private fun GeneratedPairsEntryPair.productAnchorIncrement(): Int {
        val productAnchorMix = profile.resultConstraints.productAnchorMix ?: return 0

        return if (product > productAnchorMix.productResultGreaterThan) 1 else 0
    }

    private fun candidatePairsFor(entries: List<GeneratedPairsStripEntry>): List<GeneratedPairsEntryPair> {
        val firstEntry = entries.first()

        return entries
            .drop(1)
            .shuffled(random)
            .map { secondEntry ->
                GeneratedPairsEntryPair(
                    firstEntry = firstEntry,
                    secondEntry = secondEntry
                )
            }
    }

    private fun buildSolvedPuzzle(
        entries: List<GeneratedPairsStripEntry>,
        pairs: List<GeneratedPairsEntryPair>
    ): Puzzle {
        val solvedTiles = pairs.flatMap { pair -> pair.solvedTiles() }

        return Puzzle(
            board = Board(
                tiles = if (profile.generationPolicy.isBoardTileShufflingEnabled) {
                    solvedTiles.shuffled(random)
                } else {
                    solvedTiles
                }
            ),
            strip = Strip.fromItems(items = entries.map { entry -> StripItem.Known(entry.value) })
        )
    }

    private fun buildInitialPuzzle(solvedPuzzle: Puzzle, knownEntryIds: Set<Int>): Puzzle = Puzzle(
        board = Board(
            tiles = solvedPuzzle.board.tiles.map(Tile::withHiddenExpression)
        ),
        strip = solvedPuzzle.strip.withKnownEntriesOnly(knownEntryIds = knownEntryIds)
    )

    private fun selectKnownEntryIds(pairs: List<GeneratedPairsEntryPair>): Set<Int>? {
        val knownEntryCount = selectKnownEntryCount()
        val preferredHiddenEntryIds = selectPreferredHiddenEntryIds()
        val preferredCandidates = knownEntryIdCandidates(
            knownEntryCount = knownEntryCount,
            pairs = pairs,
            preferredHiddenEntryIds = preferredHiddenEntryIds
        )
        val candidates = preferredCandidates.ifEmpty {
            knownEntryIdCandidates(
                knownEntryCount = knownEntryCount,
                pairs = pairs,
                preferredHiddenEntryIds = emptySet()
            )
        }

        return candidates.randomOrNull()
    }

    private fun selectKnownEntryCount(): Int {
        val knownEntryCountRange = profile.initialStripMaskPolicy.knownEntryCountRange

        return if (knownEntryCountRange.first == knownEntryCountRange.last) {
            knownEntryCountRange.first
        } else {
            random.nextInt(
                from = knownEntryCountRange.first,
                until = knownEntryCountRange.last + 1
            )
        }
    }

    private fun selectPreferredHiddenEntryIds(): Set<Int> = profile.initialStripMaskPolicy.highValueMaskTargets
        .mapNotNull { target ->
            val shouldHideEntry = random.nextInt(PROBABILITY_PERCENT_UPPER_BOUND) < target.targetHiddenProbability.value

            if (shouldHideEntry) {
                profile.size.stripEntryCount - target.rankFromHighest
            } else {
                null
            }
        }
        .toSet()

    private fun knownEntryIdCandidates(
        knownEntryCount: Int,
        pairs: List<GeneratedPairsEntryPair>,
        preferredHiddenEntryIds: Set<Int>
    ): List<Set<Int>> {
        val pairKeyByEntryId = pairs.flatMap { pair ->
            pair.entryIds.map { entryId -> entryId to pair.key }
        }.toMap()

        val candidates = knownEntryIdCandidatesFor(knownEntryCount = knownEntryCount)
            .filter { knownEntryIds ->
                knownEntryIds.none { entryId -> entryId in preferredHiddenEntryIds }
            }
            .filter { knownEntryIds ->
                knownEntryIds.maxConsecutiveHiddenEntries(totalEntryCount = profile.size.stripEntryCount) <=
                    profile.initialStripMaskPolicy.maxConsecutiveHiddenEntries
            }

        return when (profile.initialStripMaskPolicy.distributionPolicy) {
            StripKnownEntryDistributionPolicy.SPREAD_ACROSS_STRIP_AND_PAIRS_WHEN_POSSIBLE -> {
                candidates.filter { knownEntryIds ->
                    knownEntryIds
                        .map { entryId -> pairKeyByEntryId.getValue(entryId) }
                        .toSet()
                        .size == knownEntryIds.size
                }
            }

            StripKnownEntryDistributionPolicy.UNRESTRICTED -> candidates
        }
    }

    private fun knownEntryIdCandidatesFor(knownEntryCount: Int): List<Set<Int>> {
        val requiredKnownEntryIds = requiredKnownEntryIds()

        if (requiredKnownEntryIds.size > knownEntryCount) {
            return emptyList()
        }

        val additionalKnownEntryCount = knownEntryCount - requiredKnownEntryIds.size
        val optionalEntryIds = (0 until profile.size.stripEntryCount).filterNot { entryId ->
            entryId in requiredKnownEntryIds
        }

        return optionalEntryIds
            .combinations(size = additionalKnownEntryCount)
            .map { additionalKnownEntryIds -> additionalKnownEntryIds + requiredKnownEntryIds }
    }

    private fun requiredKnownEntryIds(): Set<Int> = profile.initialStripMaskPolicy.requiredAnchors.map { anchor ->
        when (anchor) {
            RequiredKnownStripAnchor.HIGHEST_STRIP_ENTRY -> profile.size.stripEntryCount - 1
        }
    }.toSet()

    private fun GeneratedPairsPuzzle.isValidGeneratedPuzzle(pairs: List<GeneratedPairsEntryPair>): Boolean =
        solvedPuzzle.completionState == PuzzleCompletionState.SOLVED &&
            solvedPuzzle.board.tiles.size == profile.size.boardTileCount &&
            solvedPuzzle.strip.entries.size == profile.size.stripEntryCount &&
            initialPuzzle.hasHiddenTileExpressions() &&
            hasMatchingBoardResults() &&
            solvedPuzzle.hasExpectedStripValues() &&
            solvedPuzzle.hasExpectedBoardResults() &&
            pairs.all { pair -> pair.hasExpectedProduct() } &&
            pairs.hasExpectedProductAnchorMix() &&
            initialPuzzle.hasExpectedStripMask(pairs = pairs)

    private fun GeneratedPairsPuzzle.hasMatchingBoardResults(): Boolean =
        initialPuzzle.board.tiles.map(Tile::result) == solvedPuzzle.board.tiles.map(Tile::result)

    private fun Puzzle.hasHiddenTileExpressions(): Boolean = board.tiles.all { tile ->
        tile.hasHiddenExpression()
    }

    private fun Puzzle.hasExpectedStripValues(): Boolean {
        val values = knownStripValues()

        return values.size == profile.size.stripEntryCount &&
            values == values.sorted() &&
            values.all { value -> value in profile.stripValuePolicy.valueRange } &&
            values.groupingBy { value -> value }
                .eachCount()
                .all { (_, occurrenceCount) ->
                    occurrenceCount <= profile.stripValuePolicy.maxOccurrencesPerValue
                }
    }

    private fun Puzzle.knownStripValues(): List<Int> = strip.entries.mapNotNull { entry ->
        (entry.item as? StripItem.Known)?.value
    }

    private fun Puzzle.hasExpectedBoardResults(): Boolean {
        if (profile.resultConstraints.allowsDuplicateBoardResults) {
            return true
        }

        return board.tiles.map(Tile::result).toSet().size == profile.size.boardTileCount
    }

    private fun GeneratedPairsEntryPair.hasExpectedProduct(): Boolean =
        product <= profile.resultConstraints.maxMultiplicationResult

    private fun Puzzle.hasExpectedStripMask(pairs: List<GeneratedPairsEntryPair>): Boolean {
        val knownEntryIds = knownStripEntryIds()

        return knownEntryIds.size in profile.initialStripMaskPolicy.knownEntryCountRange &&
            strip.entries.count { entry -> entry.item == StripItem.Hidden } in
            profile.initialStripMaskPolicy.hiddenEntryCountRange &&
            knownEntryIds.containsAll(requiredKnownEntryIds()) &&
            knownEntryIds.maxConsecutiveHiddenEntries(totalEntryCount = profile.size.stripEntryCount) <=
            profile.initialStripMaskPolicy.maxConsecutiveHiddenEntries &&
            knownEntryIds.hasExpectedDistribution(pairs = pairs)
    }

    private fun Set<Int>.hasExpectedDistribution(pairs: List<GeneratedPairsEntryPair>): Boolean =
        when (profile.initialStripMaskPolicy.distributionPolicy) {
            StripKnownEntryDistributionPolicy.SPREAD_ACROSS_STRIP_AND_PAIRS_WHEN_POSSIBLE -> {
                val pairKeyByEntryId = pairs.flatMap { pair ->
                    pair.entryIds.map { entryId -> entryId to pair.key }
                }.toMap()

                map { entryId -> pairKeyByEntryId.getValue(entryId) }.toSet().size == size
            }

            StripKnownEntryDistributionPolicy.UNRESTRICTED -> true
        }

    private fun Puzzle.knownStripEntryIds(): Set<Int> = strip.entries
        .filter { entry -> entry.item is StripItem.Known }
        .map { entry -> entry.id }
        .toSet()

    private fun GeneratedPairsEntryPair.solvedTiles(): List<Tile> = listOf(
        solvedTile(operator = Operator.ADDITION),
        solvedTile(operator = Operator.MULTIPLICATION)
    )

    private fun GeneratedPairsEntryPair.solvedTile(operator: Operator): Tile = resolvedTile(
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
        private const val DEFAULT_MAX_ATTEMPTS = 50
        private const val PROBABILITY_PERCENT_UPPER_BOUND = 100
    }
}

data class GeneratedPairsPuzzle(val initialPuzzle: Puzzle, val solvedPuzzle: Puzzle)

private data class GeneratedPairsStripEntry(val id: Int, val value: Int)

private data class GeneratedPairsEntryPair(
    val firstEntry: GeneratedPairsStripEntry,
    val secondEntry: GeneratedPairsStripEntry
) {
    val sum: Int = firstEntry.value + secondEntry.value
    val product: Int = firstEntry.value * secondEntry.value
    val resultValues: Set<Int> = setOf(sum, product)
    val entryIds: Set<Int> = setOf(firstEntry.id, secondEntry.id)
    val key: GeneratedPairsEntryPairKey = GeneratedPairsEntryPairKey(
        firstEntryId = minOf(firstEntry.id, secondEntry.id),
        secondEntryId = maxOf(firstEntry.id, secondEntry.id)
    )

    fun contains(entry: GeneratedPairsStripEntry): Boolean = entry.id == firstEntry.id || entry.id == secondEntry.id
}

private fun GeneratedPairsEntryPair.canBeAddedTo(usedResults: Set<Int>, resultConstraints: ResultConstraints): Boolean {
    if (product > resultConstraints.maxMultiplicationResult) {
        return false
    }

    return resultConstraints.allowsDuplicateBoardResults ||
        resultValues.size == 2 &&
        resultValues.none { result -> result in usedResults }
}

private fun List<GeneratedPairsStripEntry>.without(pair: GeneratedPairsEntryPair): List<GeneratedPairsStripEntry> =
    filterNot { entry ->
        pair.contains(entry)
    }

private data class GeneratedPairsEntryPairKey(val firstEntryId: Int, val secondEntryId: Int)

private fun Set<Int>.maxConsecutiveHiddenEntries(totalEntryCount: Int): Int {
    var currentHiddenCount = 0
    var maxHiddenCount = 0

    repeat(totalEntryCount) { entryId ->
        if (entryId in this) {
            currentHiddenCount = 0
        } else {
            currentHiddenCount++
            maxHiddenCount = maxOf(maxHiddenCount, currentHiddenCount)
        }
    }

    return maxHiddenCount
}

private fun List<Int>.combinations(size: Int): List<Set<Int>> {
    if (size == 0) {
        return listOf(emptySet())
    }
    if (size > this.size) {
        return emptyList()
    }

    val combinations = mutableListOf<Set<Int>>()

    val lastStartIndex = this.size - size

    for (index in 0..lastStartIndex) {
        val firstValue = this[index]
        val remainingValues = drop(index + 1)

        remainingValues.combinations(size = size - 1).forEach { remainingCombination ->
            combinations += remainingCombination + firstValue
        }
    }

    return combinations
}
