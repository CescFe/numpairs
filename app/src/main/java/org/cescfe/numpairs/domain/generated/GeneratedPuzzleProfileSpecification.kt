package org.cescfe.numpairs.domain.generated

internal object GeneratedPuzzleProfileSpecification {
    fun violationsFor(
        definition: GeneratedPuzzleProfileDefinition,
        validationLimits: GeneratedPuzzleProfileValidationLimits
    ): List<GeneratedPuzzleProfileViolation> {
        val structuralViolations = GeneratedPuzzleProfileStructuralEvaluator.violationsFor(definition = definition)
        if (structuralViolations.isNotEmpty()) {
            return structuralViolations
        }

        val workTracker = GeneratedPuzzleProfileValidationWorkTracker(limits = validationLimits)
        val pairSpecification = GeneratedPuzzlePairSpecification(
            stripValuePolicy = definition.stripValuePolicy,
            resultConstraints = definition.resultConstraints
        )
        val eligiblePairs = when (val catalog = pairSpecification.eligiblePairs(workTracker = workTracker)) {
            is GeneratedPuzzleProfileBoundedEvaluation.Completed -> catalog.value
            is GeneratedPuzzleProfileBoundedEvaluation.LimitExceeded -> return listOf(catalog.violation)
        }
        val context = GeneratedPuzzleProfileFeasibilityContext(
            definition = definition,
            pairSpecification = pairSpecification,
            eligiblePairs = eligiblePairs,
            workTracker = workTracker
        )
        val violations = mutableListOf<GeneratedPuzzleProfileViolation>()
        val evaluations = listOf(
            GeneratedPuzzlePairSelectionFeasibilityEvaluator::violationsFor,
            GeneratedPuzzleMaskFeasibilityEvaluator::violationsFor,
            GeneratedPuzzleProductAnchorEvaluator::violationsFor,
            GeneratedPuzzleVarietyTargetEvaluator::violationsFor
        )

        evaluations.forEach { evaluate ->
            when (val evaluation = evaluate(context)) {
                is GeneratedPuzzleProfileBoundedEvaluation.Completed -> violations += evaluation.value
                is GeneratedPuzzleProfileBoundedEvaluation.LimitExceeded -> {
                    violations += evaluation.violation
                    return violations
                }
            }
        }

        return violations
    }
}

private data class GeneratedPuzzleProfileFeasibilityContext(
    val definition: GeneratedPuzzleProfileDefinition,
    val pairSpecification: GeneratedPuzzlePairSpecification,
    val eligiblePairs: List<GeneratedPuzzlePairValues>,
    val workTracker: GeneratedPuzzleProfileValidationWorkTracker
)

private object GeneratedPuzzleProfileStructuralEvaluator {
    fun violationsFor(definition: GeneratedPuzzleProfileDefinition): List<GeneratedPuzzleProfileViolation> = buildList {
        addStripCapacityViolations(definition = definition)
        addMaskStructureViolations(definition = definition)
        addProductAnchorStructureViolations(definition = definition)
        addVarietyStructureViolations(definition = definition)
    }

    private fun MutableList<GeneratedPuzzleProfileViolation>.addStripCapacityViolations(
        definition: GeneratedPuzzleProfileDefinition
    ) {
        val valuePolicy = definition.stripValuePolicy
        val distinctValueCount = valuePolicy.valueRange.last.toLong() - valuePolicy.valueRange.first + 1L
        val availableEntryCount = distinctValueCount * valuePolicy.maxOccurrencesPerValue

        if (availableEntryCount < definition.size.stripEntryCount) {
            add(
                GeneratedPuzzleProfileViolation.InsufficientStripValueCapacity(
                    requiredEntryCount = definition.size.stripEntryCount,
                    availableEntryCount = availableEntryCount
                )
            )
        }

        val maximumValue = valuePolicy.valueRange.last
        if (maximumValue.toLong() * maximumValue > Int.MAX_VALUE) {
            add(
                GeneratedPuzzleProfileViolation.ArithmeticResultMayOverflow(
                    maximumStripValue = maximumValue
                )
            )
        }
    }

    private fun MutableList<GeneratedPuzzleProfileViolation>.addMaskStructureViolations(
        definition: GeneratedPuzzleProfileDefinition
    ) {
        val stripEntryCount = definition.size.stripEntryCount
        val maskPolicy = definition.initialStripMaskPolicy
        val distributionMaximum = definition.maximumKnownCountAllowedByDistribution()
        val maximumStructurallyAllowedKnownCount = minOf(
            stripEntryCount,
            maskPolicy.knownEntryCountRange.last,
            distributionMaximum
        )

        if (maskPolicy.knownEntryCountRange.last > stripEntryCount) {
            add(
                GeneratedPuzzleProfileViolation.KnownEntryRangeOutsidePuzzle(
                    configuredRange = maskPolicy.knownEntryCountRange,
                    stripEntryCount = stripEntryCount
                )
            )
        }
        val requiredAnchorCount = definition.requiredKnownEntryIds().size
        if (requiredAnchorCount > maximumStructurallyAllowedKnownCount) {
            add(
                GeneratedPuzzleProfileViolation.RequiredAnchorsExceedKnownCount(
                    requiredAnchorCount = requiredAnchorCount,
                    maximumKnownEntryCount = maximumStructurallyAllowedKnownCount
                )
            )
        }
        if (maskPolicy.knownEntryCountRange.first > distributionMaximum) {
            add(
                GeneratedPuzzleProfileViolation.SpreadDistributionExceedsPairCount(
                    minimumKnownEntryCount = maskPolicy.knownEntryCountRange.first,
                    pairCount = definition.size.pairCount
                )
            )
        }
    }

    private fun MutableList<GeneratedPuzzleProfileViolation>.addProductAnchorStructureViolations(
        definition: GeneratedPuzzleProfileDefinition
    ) {
        val productAnchorMix = definition.resultConstraints.productAnchorMix ?: return
        if (productAnchorMix.countRange.last > definition.size.pairCount) {
            add(
                GeneratedPuzzleProfileViolation.ProductAnchorCountOutsidePuzzle(
                    configuredRange = productAnchorMix.countRange,
                    pairCount = definition.size.pairCount
                )
            )
        }
        if (productAnchorMix.countRange.first > 0 &&
            productAnchorMix.productResultGreaterThan >= definition.resultConstraints.maxMultiplicationResult
        ) {
            add(
                GeneratedPuzzleProfileViolation.ProductAnchorThresholdUnreachable(
                    productResultGreaterThan = productAnchorMix.productResultGreaterThan,
                    maximumReachableProduct = null,
                    minimumRequiredAnchorCount = productAnchorMix.countRange.first
                )
            )
        }
    }

    private fun MutableList<GeneratedPuzzleProfileViolation>.addVarietyStructureViolations(
        definition: GeneratedPuzzleProfileDefinition
    ) {
        val stripEntryCount = definition.size.stripEntryCount
        val requiredAnchorIds = definition.requiredKnownEntryIds()
        val highValueTargets = definition.varietyPolicy.highValueMaskTargets

        highValueTargets.forEach { target ->
            if (target.rankFromHighest !in 1..stripEntryCount) {
                add(
                    GeneratedPuzzleProfileViolation.HighValueTargetRankOutsidePuzzle(
                        rankFromHighest = target.rankFromHighest,
                        stripEntryCount = stripEntryCount
                    )
                )
            } else {
                val targetedEntryId = stripEntryCount - target.rankFromHighest
                if (target.targetHiddenProbability.value > 0 && targetedEntryId in requiredAnchorIds) {
                    add(
                        GeneratedPuzzleProfileViolation.HighValueTargetConflictsWithRequiredAnchor(
                            rankFromHighest = target.rankFromHighest,
                            targetHiddenProbability = target.targetHiddenProbability
                        )
                    )
                }
            }
        }
        highValueTargets.groupingBy(HighValueMaskTarget::rankFromHighest)
            .eachCount()
            .filterValues { occurrenceCount -> occurrenceCount > 1 }
            .keys
            .forEach { duplicateRank ->
                add(
                    GeneratedPuzzleProfileViolation.DuplicateHighValueTargetRank(
                        rankFromHighest = duplicateRank
                    )
                )
            }

        definition.varietyPolicy.primeProductDecoyTarget?.let { target ->
            if (target.targetPairCount > definition.size.pairCount) {
                add(
                    GeneratedPuzzleProfileViolation.PrimeDecoyTargetCountOutsidePuzzle(
                        targetPairCount = target.targetPairCount,
                        pairCount = definition.size.pairCount
                    )
                )
            }
        }
    }
}

private object GeneratedPuzzlePairSelectionFeasibilityEvaluator {
    fun violationsFor(
        context: GeneratedPuzzleProfileFeasibilityContext
    ): GeneratedPuzzleProfileBoundedEvaluation<List<GeneratedPuzzleProfileViolation>> = when (
        val maximumPairCount = context.maximumStructurallySelectablePairCount(pairs = context.eligiblePairs)
    ) {
        is GeneratedPuzzleProfileBoundedEvaluation.LimitExceeded -> maximumPairCount
        is GeneratedPuzzleProfileBoundedEvaluation.Completed -> {
            val violations = if (maximumPairCount.value < context.definition.size.pairCount) {
                listOf(
                    GeneratedPuzzleProfileViolation.InsufficientEligibleValuePairCatalog(
                        requiredPairCount = context.definition.size.pairCount,
                        maximumStructurallyAvailableCount = maximumPairCount.value,
                        valueRange = context.definition.stripValuePolicy.valueRange,
                        maxMultiplicationResult = context.definition.resultConstraints.maxMultiplicationResult
                    )
                )
            } else {
                emptyList()
            }
            GeneratedPuzzleProfileBoundedEvaluation.Completed(value = violations)
        }
    }
}

private object GeneratedPuzzleMaskFeasibilityEvaluator {
    fun violationsFor(
        context: GeneratedPuzzleProfileFeasibilityContext
    ): GeneratedPuzzleProfileBoundedEvaluation<List<GeneratedPuzzleProfileViolation>> {
        val definition = context.definition
        val maskPolicy = definition.initialStripMaskPolicy
        val maximumKnownCount = minOf(
            definition.size.stripEntryCount,
            maskPolicy.knownEntryCountRange.last,
            definition.maximumKnownCountAllowedByDistribution()
        )
        val baseFeasibility = maskFeasibility(
            stripEntryCount = definition.size.stripEntryCount,
            forcedKnownEntryIds = definition.requiredKnownEntryIds(),
            forcedHiddenEntryIds = emptySet(),
            maxConsecutiveHiddenEntries = maskPolicy.maxConsecutiveHiddenEntries,
            workTracker = context.workTracker
        )
        val base = when (baseFeasibility) {
            is GeneratedPuzzleProfileBoundedEvaluation.LimitExceeded -> return baseFeasibility
            is GeneratedPuzzleProfileBoundedEvaluation.Completed -> baseFeasibility.value
        }
        val violations = mutableListOf<GeneratedPuzzleProfileViolation>()
        if (base.minimumKnownEntryCount == null || base.minimumKnownEntryCount > maximumKnownCount) {
            violations += GeneratedPuzzleProfileViolation.HiddenRunLimitInfeasible(
                minimumKnownEntryCount = base.minimumKnownEntryCount ?: Int.MAX_VALUE,
                configuredRange = maskPolicy.knownEntryCountRange,
                maxConsecutiveHiddenEntries = maskPolicy.maxConsecutiveHiddenEntries
            )
        }

        val highValueTargets = definition.varietyPolicy.highValueMaskTargets
        if (highValueTargets.isEmpty()) {
            return GeneratedPuzzleProfileBoundedEvaluation.Completed(value = violations)
        }
        val forcedKnownEntryIds = highValueTargets
            .filter { target -> target.targetHiddenProbability.value == 0 }
            .mapTo(mutableSetOf()) { target -> definition.size.stripEntryCount - target.rankFromHighest }
        val forcedHiddenEntryIds = highValueTargets
            .filter { target -> target.targetHiddenProbability.value == 100 }
            .mapTo(mutableSetOf()) { target -> definition.size.stripEntryCount - target.rankFromHighest }
        val targetFeasibility = maskFeasibility(
            stripEntryCount = definition.size.stripEntryCount,
            forcedKnownEntryIds = definition.requiredKnownEntryIds() + forcedKnownEntryIds,
            forcedHiddenEntryIds = forcedHiddenEntryIds,
            maxConsecutiveHiddenEntries = maskPolicy.maxConsecutiveHiddenEntries,
            workTracker = context.workTracker
        )
        val target = when (targetFeasibility) {
            is GeneratedPuzzleProfileBoundedEvaluation.LimitExceeded -> return targetFeasibility
            is GeneratedPuzzleProfileBoundedEvaluation.Completed -> targetFeasibility.value
        }
        val targetMaximumKnownCount = minOf(
            maskPolicy.knownEntryCountRange.last,
            definition.maximumKnownCountAllowedByDistribution(),
            definition.size.stripEntryCount - forcedHiddenEntryIds.size
        )
        val targetMinimumKnownCount = maxOf(
            maskPolicy.knownEntryCountRange.first,
            (forcedKnownEntryIds + definition.requiredKnownEntryIds()).size
        )
        if (target.minimumKnownEntryCount == null ||
            target.minimumKnownEntryCount > targetMaximumKnownCount ||
            targetMinimumKnownCount > targetMaximumKnownCount
        ) {
            violations += GeneratedPuzzleProfileViolation.HighValueTargetMaskInfeasible(
                forcedKnownEntryIds = forcedKnownEntryIds,
                forcedHiddenEntryIds = forcedHiddenEntryIds,
                knownEntryCountRange = maskPolicy.knownEntryCountRange,
                maxConsecutiveHiddenEntries = maskPolicy.maxConsecutiveHiddenEntries
            )
        }

        return GeneratedPuzzleProfileBoundedEvaluation.Completed(value = violations)
    }
}

private object GeneratedPuzzleProductAnchorEvaluator {
    fun violationsFor(
        context: GeneratedPuzzleProfileFeasibilityContext
    ): GeneratedPuzzleProfileBoundedEvaluation<List<GeneratedPuzzleProfileViolation>> {
        val productAnchorMix = context.definition.resultConstraints.productAnchorMix
            ?: return GeneratedPuzzleProfileBoundedEvaluation.Completed(value = emptyList())
        val violations = mutableListOf<GeneratedPuzzleProfileViolation>()
        val anchorPairs = context.eligiblePairs.filter { pair ->
            productAnchorMix.isAnchor(product = pair.product.toInt())
        }
        if (productAnchorMix.countRange.first > 0 && anchorPairs.isEmpty()) {
            violations += GeneratedPuzzleProfileViolation.ProductAnchorThresholdUnreachable(
                productResultGreaterThan = productAnchorMix.productResultGreaterThan,
                maximumReachableProduct = context.eligiblePairs.maxOfOrNull { pair -> pair.product.toInt() },
                minimumRequiredAnchorCount = productAnchorMix.countRange.first
            )
        }

        val maximumAnchorCount = when (
            val maximum = context.maximumStructurallySelectablePairCount(pairs = anchorPairs)
        ) {
            is GeneratedPuzzleProfileBoundedEvaluation.LimitExceeded -> return maximum
            is GeneratedPuzzleProfileBoundedEvaluation.Completed -> maximum.value
        }
        if (anchorPairs.isNotEmpty() && maximumAnchorCount < productAnchorMix.countRange.first) {
            violations += GeneratedPuzzleProfileViolation.InsufficientProductAnchorCapacity(
                minimumRequiredAnchorCount = productAnchorMix.countRange.first,
                maximumStructurallyAvailableCount = maximumAnchorCount
            )
        }

        val hasCompleteBaseSelection = when (
            val selection = context.pairSpecification.canSelectPairCount(
                candidatePairs = context.eligiblePairs,
                targetPairCount = context.definition.size.pairCount,
                workTracker = context.workTracker
            )
        ) {
            is GeneratedPuzzleProfileBoundedEvaluation.LimitExceeded -> return selection
            is GeneratedPuzzleProfileBoundedEvaluation.Completed -> selection.value
        }
        val canCheckCombinedSelection =
            (productAnchorMix.countRange.first == 0 || anchorPairs.isNotEmpty()) &&
                maximumAnchorCount >= productAnchorMix.countRange.first &&
                hasCompleteBaseSelection
        if (canCheckCombinedSelection) {
            when (
                val selection = context.pairSpecification.canSelectCompletePairSet(
                    candidatePairs = context.eligiblePairs,
                    targetPairCount = context.definition.size.pairCount,
                    workTracker = context.workTracker
                )
            ) {
                is GeneratedPuzzleProfileBoundedEvaluation.LimitExceeded -> return selection
                is GeneratedPuzzleProfileBoundedEvaluation.Completed -> if (!selection.value) {
                    violations += GeneratedPuzzleProfileViolation.ProductAnchorSelectionInfeasible(
                        requiredPairCount = context.definition.size.pairCount,
                        productAnchorCountRange = productAnchorMix.countRange
                    )
                }
            }
        }

        return GeneratedPuzzleProfileBoundedEvaluation.Completed(value = violations)
    }
}

private object GeneratedPuzzleVarietyTargetEvaluator {
    fun violationsFor(
        context: GeneratedPuzzleProfileFeasibilityContext
    ): GeneratedPuzzleProfileBoundedEvaluation<List<GeneratedPuzzleProfileViolation>> {
        val target = context.definition.varietyPolicy.primeProductDecoyTarget
            ?: return GeneratedPuzzleProfileBoundedEvaluation.Completed(value = emptyList())
        if (target.targetPuzzlePercent.value == 0) {
            return GeneratedPuzzleProfileBoundedEvaluation.Completed(value = emptyList())
        }

        val decoyPairs = context.eligiblePairs.filter { pair -> target.pairPattern.matches(pair = pair) }
        return when (val maximum = context.maximumStructurallySelectablePairCount(pairs = decoyPairs)) {
            is GeneratedPuzzleProfileBoundedEvaluation.LimitExceeded -> maximum
            is GeneratedPuzzleProfileBoundedEvaluation.Completed -> {
                val violations = if (maximum.value < target.targetPairCount) {
                    listOf(
                        GeneratedPuzzleProfileViolation.PrimeDecoyTargetUnreachable(
                            pairPattern = target.pairPattern,
                            targetPairCount = target.targetPairCount,
                            maximumStructurallyAvailableCount = maximum.value
                        )
                    )
                } else {
                    emptyList()
                }
                GeneratedPuzzleProfileBoundedEvaluation.Completed(value = violations)
            }
        }
    }
}

private fun GeneratedPuzzleProfileFeasibilityContext.maximumStructurallySelectablePairCount(
    pairs: List<GeneratedPuzzlePairValues>
): GeneratedPuzzleProfileBoundedEvaluation<Int> {
    val looseUpperBound = if (definition.resultConstraints.allowsDuplicateBoardResults) {
        pairs.sumOf { pair -> pairSpecification.maximumRepeatCount(pair = pair).toLong() }
    } else {
        pairs.size.toLong()
    }
    val maximumCandidateCount = minOf(
        definition.size.pairCount.toLong(),
        looseUpperBound,
        Int.MAX_VALUE.toLong()
    ).toInt()

    for (candidateCount in maximumCandidateCount downTo 1) {
        when (
            val selection = pairSpecification.canSelectPairCount(
                candidatePairs = pairs,
                targetPairCount = candidateCount,
                workTracker = workTracker
            )
        ) {
            is GeneratedPuzzleProfileBoundedEvaluation.LimitExceeded -> return selection
            is GeneratedPuzzleProfileBoundedEvaluation.Completed -> if (selection.value) {
                return GeneratedPuzzleProfileBoundedEvaluation.Completed(value = candidateCount)
            }
        }
    }

    return GeneratedPuzzleProfileBoundedEvaluation.Completed(value = 0)
}

private data class MaskFeasibility(val minimumKnownEntryCount: Int?)

private fun maskFeasibility(
    stripEntryCount: Int,
    forcedKnownEntryIds: Set<Int>,
    forcedHiddenEntryIds: Set<Int>,
    maxConsecutiveHiddenEntries: Int,
    workTracker: GeneratedPuzzleProfileValidationWorkTracker
): GeneratedPuzzleProfileBoundedEvaluation<MaskFeasibility> {
    if (forcedKnownEntryIds.any { entryId -> entryId !in 0 until stripEntryCount } ||
        forcedHiddenEntryIds.any { entryId -> entryId !in 0 until stripEntryCount } ||
        forcedKnownEntryIds.any { entryId -> entryId in forcedHiddenEntryIds }
    ) {
        return GeneratedPuzzleProfileBoundedEvaluation.Completed(
            value = MaskFeasibility(minimumKnownEntryCount = null)
        )
    }

    var minimumKnownCountByHiddenRun = mapOf(0 to 0)
    for (entryId in 0 until stripEntryCount) {
        val next = mutableMapOf<Int, Int>()
        minimumKnownCountByHiddenRun.forEach { (hiddenRun, knownCount) ->
            workTracker.consume(GeneratedPuzzleProfileValidationWorkKind.MASK_STATE)?.let { violation ->
                return GeneratedPuzzleProfileBoundedEvaluation.LimitExceeded(violation = violation)
            }
            if (entryId !in forcedHiddenEntryIds) {
                next.merge(0, knownCount + 1, ::minOf)
            }
            if (entryId !in forcedKnownEntryIds && hiddenRun < maxConsecutiveHiddenEntries) {
                next.merge(hiddenRun + 1, knownCount, ::minOf)
            }
        }
        if (next.isEmpty()) {
            return GeneratedPuzzleProfileBoundedEvaluation.Completed(
                value = MaskFeasibility(minimumKnownEntryCount = null)
            )
        }
        minimumKnownCountByHiddenRun = next
    }

    return GeneratedPuzzleProfileBoundedEvaluation.Completed(
        value = MaskFeasibility(
            minimumKnownEntryCount = minimumKnownCountByHiddenRun.values.minOrNull()
        )
    )
}

private fun GeneratedPuzzleProfileDefinition.requiredKnownEntryIds(): Set<Int> =
    initialStripMaskPolicy.requiredAnchors.resolveEntryIds(
        stripEntryCount = size.stripEntryCount
    )

private fun GeneratedPuzzleProfileDefinition.maximumKnownCountAllowedByDistribution(): Int =
    when (initialStripMaskPolicy.distributionPolicy) {
        StripKnownEntryDistributionPolicy.SPREAD_ACROSS_STRIP_AND_PAIRS_WHEN_POSSIBLE -> size.pairCount
        StripKnownEntryDistributionPolicy.UNRESTRICTED -> size.stripEntryCount
    }
