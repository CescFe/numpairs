package org.cescfe.numpairs.domain.generated.assessment

import org.cescfe.numpairs.domain.generated.generation.internal.requiredKnownEntryIds
import org.cescfe.numpairs.domain.generated.profile.GeneratedPuzzleProfile
import org.cescfe.numpairs.domain.puzzle.assignment.StripEntryId
import org.cescfe.numpairs.domain.puzzle.model.Expression
import org.cescfe.numpairs.domain.puzzle.model.Operator
import org.cescfe.numpairs.domain.puzzle.model.Puzzle
import org.cescfe.numpairs.domain.puzzle.model.StripItem

class GeneratedPairsDifficultyAssessor {
    fun assess(
        initialPuzzle: Puzzle,
        profile: GeneratedPuzzleProfile,
        executionPolicy: GeneratedPuzzleDifficultyAssessmentExecutionPolicy =
            GeneratedPuzzleDifficultyAssessmentExecutionPolicy(),
        cancellation: GeneratedPuzzleDifficultyAssessmentCancellation =
            GeneratedPuzzleDifficultyAssessmentCancellation.None
    ): GeneratedPuzzleDifficultyAssessmentOutcome {
        val knownEntryCount = initialPuzzle.strip.entries.count { entry -> entry.item is StripItem.Known }
        val longestHiddenRun = initialPuzzle.longestHiddenRun()
        if (cancellation.isCancelled()) {
            return GeneratedPuzzleDifficultyAssessmentOutcome.Cancelled(workConsumed = 0)
        }
        if (!initialPuzzle.hasExpectedAssessmentShape(profile = profile)) {
            return GeneratedPuzzleDifficultyAssessmentOutcome.Unsatisfiable(
                initialPlausibleCandidateCount = 0,
                knownEntryCount = knownEntryCount,
                longestHiddenRun = longestHiddenRun,
                workConsumed = 0
            )
        }

        val resultCounts = initialPuzzle.board.tiles
            .map { tile -> tile.result }
            .groupingBy { result -> result }
            .eachCount()
        val workControl = AssessmentWorkControl(
            executionPolicy = executionPolicy,
            cancellation = cancellation
        )
        val candidates = candidatesFor(
            initialPuzzle = initialPuzzle,
            profile = profile,
            resultCounts = resultCounts,
            workControl = workControl
        )
        when (workControl.termination) {
            AssessmentTermination.CANCELLED -> {
                return GeneratedPuzzleDifficultyAssessmentOutcome.Cancelled(
                    workConsumed = workControl.workConsumed
                )
            }

            AssessmentTermination.WORK_LIMIT -> {
                return GeneratedPuzzleDifficultyAssessmentOutcome.WorkLimitReached(
                    maximumCandidateExpansions = executionPolicy.maxCandidateExpansions,
                    workConsumed = workControl.workConsumed
                )
            }

            null -> Unit
        }
        if (candidates.isEmpty()) {
            return GeneratedPuzzleDifficultyAssessmentOutcome.Unsatisfiable(
                initialPlausibleCandidateCount = 0,
                knownEntryCount = knownEntryCount,
                longestHiddenRun = longestHiddenRun,
                workConsumed = workControl.workConsumed
            )
        }

        val search = AssessmentSearch(
            initialPuzzle = initialPuzzle,
            profile = profile,
            candidates = candidates,
            initialResultCounts = resultCounts,
            workControl = workControl
        )
        search.run()

        return when (search.termination) {
            AssessmentTermination.CANCELLED -> GeneratedPuzzleDifficultyAssessmentOutcome.Cancelled(
                workConsumed = search.workConsumed
            )

            AssessmentTermination.WORK_LIMIT -> GeneratedPuzzleDifficultyAssessmentOutcome.WorkLimitReached(
                maximumCandidateExpansions = executionPolicy.maxCandidateExpansions,
                workConsumed = search.workConsumed
            )

            null -> {
                if (search.canonicalSolutionCount == 0) {
                    GeneratedPuzzleDifficultyAssessmentOutcome.Unsatisfiable(
                        initialPlausibleCandidateCount = candidates.size,
                        knownEntryCount = knownEntryCount,
                        longestHiddenRun = longestHiddenRun,
                        workConsumed = search.workConsumed
                    )
                } else {
                    GeneratedPuzzleDifficultyAssessmentOutcome.Assessed(
                        report = GeneratedPuzzleDifficultyAssessmentReport(
                            initialPlausibleCandidateCount = candidates.size,
                            initialForcedDeductionCount = search.initialForcedFacts.size,
                            forcedDeductionCount = search.forcedFacts.size,
                            firstForcedDeductionDepth = search.firstForcedDeductionDepth,
                            maximumBranchingFactor = search.maximumBranchingFactor,
                            exploredAmbiguousStateCount = search.exploredAmbiguousStateCount,
                            boundedValidSolutionCount = minOf(
                                search.canonicalSolutionCount,
                                executionPolicy.validSolutionCountLimit
                            ),
                            isValidSolutionCountLimitReached =
                            search.canonicalSolutionCount > executionPolicy.validSolutionCountLimit,
                            structuralObservations = GeneratedPuzzleStructuralObservations(
                                knownEntryCount = knownEntryCount,
                                longestHiddenRun = longestHiddenRun,
                                knownStripAnchorCount = initialPuzzle.knownStripAnchorCount(profile = profile),
                                unambiguousResultAnchorCount =
                                initialPuzzle.unambiguousResultAnchorCount(candidates = candidates),
                                repeatedValueGroupCountRange =
                                search.minimumRepeatedValueGroupCount..search.maximumRepeatedValueGroupCount,
                                plausibleDecoyCount = candidates.count { candidate ->
                                    candidate.fact !in search.solutionFacts
                                }
                            )
                        ),
                        workConsumed = search.workConsumed
                    )
                }
            }
        }
    }
}

private class AssessmentSearch(
    private val initialPuzzle: Puzzle,
    private val profile: GeneratedPuzzleProfile,
    private val candidates: List<PairCandidate>,
    private val initialResultCounts: Map<Int, Int>,
    private val workControl: AssessmentWorkControl
) {
    val termination: AssessmentTermination?
        get() = workControl.termination
    val workConsumed: Int
        get() = workControl.workConsumed
    var maximumBranchingFactor: Int = 0
        private set
    var exploredAmbiguousStateCount: Int = 0
        private set
    val forcedFacts: MutableSet<GeneratedPairAssessmentFact> = linkedSetOf()
    val initialForcedFacts: MutableSet<GeneratedPairAssessmentFact> = linkedSetOf()
    var firstForcedDeductionDepth: Int? = null
        private set
    val solutionFacts: MutableSet<GeneratedPairAssessmentFact> = linkedSetOf()
    var canonicalSolutionCount: Int = 0
        private set
    var minimumRepeatedValueGroupCount: Int = Int.MAX_VALUE
        private set
    var maximumRepeatedValueGroupCount: Int = Int.MIN_VALUE
        private set

    private val canonicalSolutions = mutableSetOf<CanonicalSolution>()

    fun run() {
        search(
            state = AssessmentSearchState(
                remainingResultCounts = initialResultCounts,
                selectedValueCounts = emptyMap(),
                selectedFacts = emptyList()
            ),
            hasSpeculativeBranch = false
        )
    }

    private fun search(state: AssessmentSearchState, hasSpeculativeBranch: Boolean): Boolean {
        if (checkTermination()) {
            return false
        }
        if (state.remainingResultCounts.values.all { count -> count == 0 }) {
            return recordSolutionIfValid(state = state)
        }

        val viableCandidates = candidates.filter { candidate ->
            candidate.canConsume(results = state.remainingResultCounts) &&
                state.canAdd(candidate = candidate, profile = profile, initialPuzzle = initialPuzzle)
        }
        val choices = state.remainingResultCounts
            .filterValues { count -> count > 0 }
            .keys
            .sorted()
            .map { result ->
                viableCandidates.filter { candidate -> candidate.consumes(result = result) }
            }
            .minWithOrNull(
                compareBy<List<PairCandidate>> { resultChoices -> resultChoices.size }
                    .thenBy { resultChoices -> resultChoices.firstOrNull()?.fact }
            )
            .orEmpty()
        if (choices.isEmpty()) {
            return false
        }

        if (choices.size > 1) {
            maximumBranchingFactor = maxOf(maximumBranchingFactor, choices.size)
            exploredAmbiguousStateCount++
        }

        var foundSolution = false
        choices.forEach { candidate ->
            if (!workControl.consumeCandidateExpansion()) {
                return foundSolution
            }
            val childFoundSolution = search(
                state = state.with(candidate = candidate),
                hasSpeculativeBranch = hasSpeculativeBranch || choices.size > 1
            )
            foundSolution = foundSolution || childFoundSolution
            if (termination != null) {
                return foundSolution
            }
        }

        if (choices.size == 1 && foundSolution) {
            val forcedFact = choices.single().fact
            forcedFacts += forcedFact
            if (!hasSpeculativeBranch) {
                initialForcedFacts += forcedFact
                val depth = state.selectedFacts.size + 1
                firstForcedDeductionDepth = firstForcedDeductionDepth?.let { currentDepth ->
                    minOf(currentDepth, depth)
                } ?: depth
            }
        }
        return foundSolution
    }

    private fun recordSolutionIfValid(state: AssessmentSearchState): Boolean {
        val sortedValues = state.selectedValueCounts.entries.flatMap { (value, count) ->
            List(count) { value }
        }.sorted()
        if (!initialPuzzle.matchesCompletedStrip(values = sortedValues)) {
            return false
        }

        val solution = CanonicalSolution(
            facts = state.selectedFacts.sorted(),
            sortedValues = sortedValues
        )
        if (!canonicalSolutions.add(solution)) {
            return true
        }

        canonicalSolutionCount++
        solutionFacts += solution.facts
        val repeatedGroupCount = sortedValues
            .groupingBy { value -> value }
            .eachCount()
            .count { (_, count) -> count > 1 }
        minimumRepeatedValueGroupCount = minOf(minimumRepeatedValueGroupCount, repeatedGroupCount)
        maximumRepeatedValueGroupCount = maxOf(maximumRepeatedValueGroupCount, repeatedGroupCount)
        return true
    }

    private fun checkTermination(): Boolean = workControl.checkTermination()
}

private data class AssessmentSearchState(
    val remainingResultCounts: Map<Int, Int>,
    val selectedValueCounts: Map<Int, Int>,
    val selectedFacts: List<GeneratedPairAssessmentFact>
) {
    fun canAdd(candidate: PairCandidate, profile: GeneratedPuzzleProfile, initialPuzzle: Puzzle): Boolean {
        val updatedValueCounts = selectedValueCounts
            .withIncrement(candidate.fact.firstValue)
            .withIncrement(candidate.fact.secondValue)
        return updatedValueCounts.values.all { count -> count <= profile.stripValuePolicy.maxOccurrencesPerValue } &&
            initialPuzzle.canStillFitKnownEntries(
                selectedValueCounts = updatedValueCounts,
                allowedValueRange = profile.stripValuePolicy.valueRange
            )
    }

    fun with(candidate: PairCandidate): AssessmentSearchState = copy(
        remainingResultCounts = candidate.consume(results = remainingResultCounts),
        selectedValueCounts = selectedValueCounts
            .withIncrement(candidate.fact.firstValue)
            .withIncrement(candidate.fact.secondValue),
        selectedFacts = selectedFacts + candidate.fact
    )
}

private data class PairCandidate(val fact: GeneratedPairAssessmentFact) {
    private val resultConsumption: Map<Int, Int> = listOf(fact.sumResult, fact.productResult)
        .groupingBy { result -> result }
        .eachCount()

    fun canConsume(results: Map<Int, Int>): Boolean = resultConsumption.all { (result, count) ->
        results.getOrDefault(result, 0) >= count
    }

    fun consume(results: Map<Int, Int>): Map<Int, Int> = results.toMutableMap().apply {
        resultConsumption.forEach { (result, count) ->
            this[result] = getValue(result) - count
        }
    }

    fun consumes(result: Int): Boolean = result in resultConsumption
}

private data class CanonicalSolution(val facts: List<GeneratedPairAssessmentFact>, val sortedValues: List<Int>)

private enum class AssessmentTermination {
    CANCELLED,
    WORK_LIMIT
}

private class AssessmentWorkControl(
    private val executionPolicy: GeneratedPuzzleDifficultyAssessmentExecutionPolicy,
    private val cancellation: GeneratedPuzzleDifficultyAssessmentCancellation
) {
    var termination: AssessmentTermination? = null
        private set
    var workConsumed: Int = 0
        private set

    fun consumeCandidateExpansion(): Boolean {
        if (checkTermination()) {
            return false
        }
        if (workConsumed >= executionPolicy.maxCandidateExpansions) {
            termination = AssessmentTermination.WORK_LIMIT
            return false
        }
        workConsumed++
        return true
    }

    fun checkTermination(): Boolean {
        if (termination != null) {
            return true
        }
        if (cancellation.isCancelled()) {
            termination = AssessmentTermination.CANCELLED
            return true
        }
        return false
    }
}

private fun candidatesFor(
    initialPuzzle: Puzzle,
    profile: GeneratedPuzzleProfile,
    resultCounts: Map<Int, Int>,
    workControl: AssessmentWorkControl
): List<PairCandidate> {
    val candidates = mutableListOf<PairCandidate>()
    profile.stripValuePolicy.valueRange.forEach { firstValue ->
        (firstValue..profile.stripValuePolicy.valueRange.last).forEach { secondValue ->
            if (!workControl.consumeCandidateExpansion()) {
                return candidates
            }
            val sum = firstValue.toLong() + secondValue.toLong()
            val product = firstValue.toLong() * secondValue.toLong()
            if (
                sum <= Int.MAX_VALUE &&
                product <= minOf(Int.MAX_VALUE.toLong(), profile.resultConstraints.maxMultiplicationResult.toLong())
            ) {
                val candidate = PairCandidate(
                    fact = GeneratedPairAssessmentFact.canonical(
                        firstOperand = firstValue,
                        secondOperand = secondValue,
                        sumResult = sum.toInt(),
                        productResult = product.toInt()
                    )
                )
                val valueCounts = emptyMap<Int, Int>()
                    .withIncrement(firstValue)
                    .withIncrement(secondValue)
                if (
                    candidate.canConsume(results = resultCounts) &&
                    valueCounts.values.all { count ->
                        count <= profile.stripValuePolicy.maxOccurrencesPerValue
                    } &&
                    initialPuzzle.canStillFitKnownEntries(
                        selectedValueCounts = valueCounts,
                        allowedValueRange = profile.stripValuePolicy.valueRange
                    )
                ) {
                    candidates += candidate
                }
            }
        }
    }
    return candidates.distinct().sortedBy(PairCandidate::fact)
}

private fun Puzzle.hasExpectedAssessmentShape(profile: GeneratedPuzzleProfile): Boolean =
    board.tiles.size == profile.size.boardTileCount &&
        strip.entries.size == profile.size.stripEntryCount &&
        board.tiles.all { tile ->
            tile.expression.leftOperand == Expression.Operand.Hidden &&
                tile.expression.operator == Operator.Hidden &&
                tile.expression.rightOperand == Expression.Operand.Hidden
        } &&
        strip.entries.all { entry ->
            entry.item == StripItem.Hidden || entry.item is StripItem.Known
        }

private fun Puzzle.matchesCompletedStrip(values: List<Int>): Boolean = values.size == strip.entries.size &&
    strip.entries.indices.all { index ->
        when (val item = strip.entries[index].item) {
            StripItem.Hidden -> true
            is StripItem.Known -> item.value == values[index]
            is StripItem.PlayerEntered -> false
        }
    }

private fun Puzzle.canStillFitKnownEntries(selectedValueCounts: Map<Int, Int>, allowedValueRange: IntRange): Boolean {
    if (selectedValueCounts.keys.any { value -> value !in allowedValueRange }) {
        return false
    }
    val totalEntryCount = strip.entries.size
    return strip.entries.withIndex().all { (index, entry) ->
        val knownValue = (entry.item as? StripItem.Known)?.value ?: return@all true
        val selectedBelow = selectedValueCounts
            .filterKeys { value -> value < knownValue }
            .values
            .sum()
        val selectedAbove = selectedValueCounts
            .filterKeys { value -> value > knownValue }
            .values
            .sum()
        selectedBelow <= index && selectedAbove <= totalEntryCount - index - 1
    }
}

private fun Puzzle.longestHiddenRun(): Int {
    var longest = 0
    var current = 0
    strip.entries.forEach { entry ->
        if (entry.item == StripItem.Hidden) {
            current++
            longest = maxOf(longest, current)
        } else {
            current = 0
        }
    }
    return longest
}

private fun Puzzle.knownStripAnchorCount(profile: GeneratedPuzzleProfile): Int {
    val requiredAnchorIds = profile.requiredKnownEntryIds()
    return strip.entries.count { entry ->
        entry.item is StripItem.Known && StripEntryId(entry.id) in requiredAnchorIds
    }
}

private fun Puzzle.unambiguousResultAnchorCount(candidates: List<PairCandidate>): Int = board.tiles.count { tile ->
    candidates.count { candidate -> candidate.consumes(result = tile.result) } == 1
}

private fun Map<Int, Int>.withIncrement(value: Int): Map<Int, Int> = toMutableMap().apply {
    this[value] = getOrDefault(value, 0) + 1
}
