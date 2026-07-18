package org.cescfe.numpairs.domain.generated.profile

internal data class GeneratedPuzzlePairValues(val firstValue: Int, val secondValue: Int) {
    val values: List<Int> = listOf(firstValue, secondValue)
    val sum: Long = firstValue.toLong() + secondValue
    val product: Long = firstValue.toLong() * secondValue
    val results: List<Long> = listOf(sum, product)
}

internal class GeneratedPuzzlePairSpecification(
    private val stripValuePolicy: StripValuePolicy,
    private val resultConstraints: ResultConstraints
) {
    fun eligiblePairs(
        workTracker: GeneratedPuzzleProfileValidationWorkTracker
    ): GeneratedPuzzleProfileBoundedEvaluation<List<GeneratedPuzzlePairValues>> {
        val pairs = mutableListOf<GeneratedPuzzlePairValues>()

        for (firstValue in stripValuePolicy.valueRange) {
            if (!resultConstraints.acceptsMultiplicationResult(firstValue.toLong() * firstValue)) {
                break
            }
            for (secondValue in firstValue..stripValuePolicy.valueRange.last) {
                workTracker.consume(GeneratedPuzzleProfileValidationWorkKind.CATALOG_EXPANSION)?.let { violation ->
                    return GeneratedPuzzleProfileBoundedEvaluation.LimitExceeded(violation = violation)
                }

                val pair = GeneratedPuzzlePairValues(
                    firstValue = firstValue,
                    secondValue = secondValue
                )
                if (!resultConstraints.acceptsMultiplicationResult(result = pair.product)) {
                    break
                }
                if (isLocallyEligible(pair = pair)) {
                    pairs += pair
                }
            }
        }

        return GeneratedPuzzleProfileBoundedEvaluation.Completed(value = pairs)
    }

    fun isLocallyEligible(pair: GeneratedPuzzlePairValues): Boolean = stripValuePolicy.accepts(values = pair.values) &&
        resultConstraints.acceptsMultiplicationResult(result = pair.product) &&
        resultConstraints.acceptsBoardResults(results = pair.results)

    fun canBeAdded(pair: GeneratedPuzzlePairValues, valueOccurrences: Map<Int, Int>, usedResults: Set<Int>): Boolean =
        isLocallyEligible(pair = pair) &&
            stripValuePolicy.accepts(
                occurrencesByValue = valueOccurrences.with(pair = pair)
            ) &&
            resultConstraints.acceptsAdditionalBoardResults(
                usedResults = usedResults,
                newResults = pair.results.mapTo(mutableSetOf(), Long::toInt)
            )

    fun maximumRepeatCount(pair: GeneratedPuzzlePairValues): Int = pair.values
        .groupingBy { value -> value }
        .eachCount()
        .minOf { (_, occurrencesPerPair) ->
            stripValuePolicy.maxOccurrencesPerValue / occurrencesPerPair
        }

    fun canSelectPairCount(
        candidatePairs: List<GeneratedPuzzlePairValues>,
        targetPairCount: Int,
        workTracker: GeneratedPuzzleProfileValidationWorkTracker
    ): GeneratedPuzzleProfileBoundedEvaluation<Boolean> = canSelectPairs(
        candidatePairs = candidatePairs,
        targetPairCount = targetPairCount,
        selectedPairCount = 0,
        valueOccurrences = emptyMap(),
        usedResults = emptySet(),
        productAnchorCount = 0,
        productAnchorMix = null,
        failedStates = mutableSetOf(),
        workTracker = workTracker
    )

    fun canSelectCompletePairSet(
        candidatePairs: List<GeneratedPuzzlePairValues>,
        targetPairCount: Int,
        workTracker: GeneratedPuzzleProfileValidationWorkTracker
    ): GeneratedPuzzleProfileBoundedEvaluation<Boolean> = canSelectPairs(
        candidatePairs = candidatePairs,
        targetPairCount = targetPairCount,
        selectedPairCount = 0,
        valueOccurrences = emptyMap(),
        usedResults = emptySet(),
        productAnchorCount = 0,
        productAnchorMix = resultConstraints.productAnchorMix,
        failedStates = mutableSetOf(),
        workTracker = workTracker
    )

    private fun canSelectPairs(
        candidatePairs: List<GeneratedPuzzlePairValues>,
        targetPairCount: Int,
        selectedPairCount: Int,
        valueOccurrences: Map<Int, Int>,
        usedResults: Set<Int>,
        productAnchorCount: Int,
        productAnchorMix: ProductAnchorMix?,
        failedStates: MutableSet<PairSelectionState>,
        workTracker: GeneratedPuzzleProfileValidationWorkTracker
    ): GeneratedPuzzleProfileBoundedEvaluation<Boolean> {
        val remainingPairCount = targetPairCount - selectedPairCount
        if (productAnchorMix != null &&
            (
                productAnchorCount > productAnchorMix.countRange.last ||
                    productAnchorCount + remainingPairCount < productAnchorMix.countRange.first
                )
        ) {
            return GeneratedPuzzleProfileBoundedEvaluation.Completed(value = false)
        }
        if (selectedPairCount == targetPairCount) {
            return GeneratedPuzzleProfileBoundedEvaluation.Completed(
                value = productAnchorMix == null || productAnchorCount in productAnchorMix.countRange
            )
        }

        val state = PairSelectionState(
            selectedPairCount = selectedPairCount,
            valueOccurrences = valueOccurrences,
            usedResults = usedResults,
            productAnchorCount = productAnchorCount
        )
        if (state in failedStates) {
            return GeneratedPuzzleProfileBoundedEvaluation.Completed(value = false)
        }
        workTracker.consume(GeneratedPuzzleProfileValidationWorkKind.PAIR_SELECTION_STATE)?.let { violation ->
            return GeneratedPuzzleProfileBoundedEvaluation.LimitExceeded(violation = violation)
        }
        failedStates += state

        candidatePairs.forEach { candidatePair ->
            if (!canBeAdded(
                    pair = candidatePair,
                    valueOccurrences = valueOccurrences,
                    usedResults = usedResults
                )
            ) {
                return@forEach
            }

            when (
                val selection = canSelectPairs(
                    candidatePairs = candidatePairs,
                    targetPairCount = targetPairCount,
                    selectedPairCount = selectedPairCount + 1,
                    valueOccurrences = valueOccurrences.with(pair = candidatePair),
                    usedResults = usedResults + candidatePair.results.map(Long::toInt),
                    productAnchorCount = productAnchorCount + if (
                        productAnchorMix?.isAnchor(product = candidatePair.product.toInt()) == true
                    ) {
                        1
                    } else {
                        0
                    },
                    productAnchorMix = productAnchorMix,
                    failedStates = failedStates,
                    workTracker = workTracker
                )
            ) {
                is GeneratedPuzzleProfileBoundedEvaluation.LimitExceeded -> return selection
                is GeneratedPuzzleProfileBoundedEvaluation.Completed -> if (selection.value) {
                    return selection
                }
            }
        }

        return GeneratedPuzzleProfileBoundedEvaluation.Completed(value = false)
    }
}

private data class PairSelectionState(
    val selectedPairCount: Int,
    val valueOccurrences: Map<Int, Int>,
    val usedResults: Set<Int>,
    val productAnchorCount: Int
)

private fun Map<Int, Int>.with(pair: GeneratedPuzzlePairValues): Map<Int, Int> {
    val updatedOccurrences = toMutableMap()
    pair.values.groupingBy { value -> value }.eachCount().forEach { (value, addedOccurrences) ->
        updatedOccurrences[value] = updatedOccurrences.getOrDefault(value, 0) + addedOccurrences
    }
    return updatedOccurrences
}

internal fun StripValuePolicy.accepts(values: List<Int>): Boolean = values.all { value -> value in valueRange } &&
    accepts(occurrencesByValue = values.groupingBy { value -> value }.eachCount())

internal fun StripValuePolicy.accepts(occurrencesByValue: Map<Int, Int>): Boolean =
    occurrencesByValue.all { (value, occurrenceCount) ->
        value in valueRange && occurrenceCount <= maxOccurrencesPerValue
    } &&
        maxRepeatedValueGroupCount?.let { maximumGroupCount ->
            occurrencesByValue.values.count { occurrenceCount -> occurrenceCount > 1 } <= maximumGroupCount
        } != false

internal fun ResultConstraints.acceptsMultiplicationResult(result: Long): Boolean = result <= maxMultiplicationResult

internal fun ResultConstraints.acceptsBoardResults(results: Collection<Long>): Boolean =
    allowsDuplicateBoardResults || results.toSet().size == results.size

internal fun ResultConstraints.acceptsAdditionalBoardResults(usedResults: Set<Int>, newResults: Set<Int>): Boolean =
    allowsDuplicateBoardResults ||
        (newResults.size == 2 && newResults.none { result -> result in usedResults })

internal fun ProductAnchorMix.isAnchor(product: Int): Boolean = product > productResultGreaterThan

internal fun PrimeProductDecoyPairPattern.matches(pair: GeneratedPuzzlePairValues): Boolean = when (this) {
    PrimeProductDecoyPairPattern.ONE_AND_PRIME ->
        pair.firstValue == 1 &&
            pair.secondValue.isPrime() ||
            pair.secondValue == 1 &&
            pair.firstValue.isPrime()
}

private fun Int.isPrime(): Boolean {
    if (this < 2) {
        return false
    }

    var candidateDivisor = 2
    while (candidateDivisor.toLong() * candidateDivisor <= this) {
        if (this % candidateDivisor == 0) {
            return false
        }
        candidateDivisor++
    }

    return true
}
