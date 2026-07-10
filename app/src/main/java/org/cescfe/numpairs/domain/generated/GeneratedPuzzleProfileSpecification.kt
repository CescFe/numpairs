package org.cescfe.numpairs.domain.generated

internal object GeneratedPuzzleProfileSpecification {
    fun violationsFor(definition: GeneratedPuzzleProfileDefinition): List<GeneratedPuzzleProfileViolation> {
        val pairSpecification = GeneratedPuzzlePairSpecification(
            stripValuePolicy = definition.stripValuePolicy,
            resultConstraints = definition.resultConstraints
        )
        val eligiblePairs = pairSpecification.eligiblePairs()

        return buildList {
            addStripValueViolations(
                definition = definition,
                pairSpecification = pairSpecification,
                eligiblePairs = eligiblePairs
            )
            addMaskViolations(definition = definition)
            addProductAnchorViolations(
                definition = definition,
                pairSpecification = pairSpecification,
                eligiblePairs = eligiblePairs
            )
            addVarietyViolations(
                definition = definition,
                pairSpecification = pairSpecification,
                eligiblePairs = eligiblePairs
            )
        }
    }

    private fun MutableList<GeneratedPuzzleProfileViolation>.addStripValueViolations(
        definition: GeneratedPuzzleProfileDefinition,
        pairSpecification: GeneratedPuzzlePairSpecification,
        eligiblePairs: List<GeneratedPuzzlePairValues>
    ) {
        val stripEntryCount = definition.size.stripEntryCount
        val valuePolicy = definition.stripValuePolicy
        val distinctValueCount = valuePolicy.valueRange.last.toLong() - valuePolicy.valueRange.first + 1L
        val availableEntryCount = distinctValueCount * valuePolicy.maxOccurrencesPerValue

        if (availableEntryCount < stripEntryCount) {
            add(
                GeneratedPuzzleProfileViolation.InsufficientStripValueCapacity(
                    requiredEntryCount = stripEntryCount,
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

        val maximumPairCount = definition.maximumStructurallySelectablePairCount(
            pairs = eligiblePairs,
            pairSpecification = pairSpecification
        )
        if (availableEntryCount >= stripEntryCount && maximumPairCount < definition.size.pairCount) {
            add(
                GeneratedPuzzleProfileViolation.InsufficientEligibleValuePairCatalog(
                    requiredPairCount = definition.size.pairCount,
                    maximumStructurallyAvailableCount = maximumPairCount,
                    valueRange = valuePolicy.valueRange,
                    maxMultiplicationResult = definition.resultConstraints.maxMultiplicationResult
                )
            )
        }
    }

    private fun MutableList<GeneratedPuzzleProfileViolation>.addMaskViolations(
        definition: GeneratedPuzzleProfileDefinition
    ) {
        val stripEntryCount = definition.size.stripEntryCount
        val maskPolicy = definition.initialStripMaskPolicy
        val knownEntryCountRange = maskPolicy.knownEntryCountRange
        val requiredAnchorIds = definition.requiredKnownEntryIds()
        val distributionMaximum = definition.maximumKnownCountAllowedByDistribution()
        val maximumStructurallyAllowedKnownCount = minOf(
            stripEntryCount,
            knownEntryCountRange.last,
            distributionMaximum
        )

        if (knownEntryCountRange.last > stripEntryCount) {
            add(
                GeneratedPuzzleProfileViolation.KnownEntryRangeOutsidePuzzle(
                    configuredRange = knownEntryCountRange,
                    stripEntryCount = stripEntryCount
                )
            )
        }
        val requiredAnchorsFit = requiredAnchorIds.size <= maximumStructurallyAllowedKnownCount
        if (!requiredAnchorsFit) {
            add(
                GeneratedPuzzleProfileViolation.RequiredAnchorsExceedKnownCount(
                    requiredAnchorCount = requiredAnchorIds.size,
                    maximumKnownEntryCount = maximumStructurallyAllowedKnownCount
                )
            )
        }

        val maskFeasibility = maskFeasibility(
            stripEntryCount = stripEntryCount,
            forcedKnownEntryIds = requiredAnchorIds,
            forcedHiddenEntryIds = emptySet(),
            maxConsecutiveHiddenEntries = maskPolicy.maxConsecutiveHiddenEntries
        )
        if (requiredAnchorsFit &&
            (
                maskFeasibility.minimumKnownEntryCount == null ||
                    maskFeasibility.minimumKnownEntryCount > maximumStructurallyAllowedKnownCount
                )
        ) {
            add(
                GeneratedPuzzleProfileViolation.HiddenRunLimitInfeasible(
                    minimumKnownEntryCount = maskFeasibility.minimumKnownEntryCount ?: Int.MAX_VALUE,
                    configuredRange = knownEntryCountRange,
                    maxConsecutiveHiddenEntries = maskPolicy.maxConsecutiveHiddenEntries
                )
            )
        }

        if (knownEntryCountRange.first > distributionMaximum) {
            add(
                GeneratedPuzzleProfileViolation.SpreadDistributionExceedsPairCount(
                    minimumKnownEntryCount = knownEntryCountRange.first,
                    pairCount = definition.size.pairCount
                )
            )
        }
    }

    private fun MutableList<GeneratedPuzzleProfileViolation>.addProductAnchorViolations(
        definition: GeneratedPuzzleProfileDefinition,
        pairSpecification: GeneratedPuzzlePairSpecification,
        eligiblePairs: List<GeneratedPuzzlePairValues>
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

        val anchorPairs = eligiblePairs.filter { pair ->
            productAnchorMix.isAnchor(product = pair.product.toInt())
        }
        if (productAnchorMix.countRange.first > 0 && anchorPairs.isEmpty()) {
            add(
                GeneratedPuzzleProfileViolation.ProductAnchorThresholdUnreachable(
                    productResultGreaterThan = productAnchorMix.productResultGreaterThan,
                    maximumReachableProduct = eligiblePairs.maxOfOrNull { pair -> pair.product.toInt() },
                    minimumRequiredAnchorCount = productAnchorMix.countRange.first
                )
            )
        }

        val maximumAnchorCount = definition.maximumStructurallySelectablePairCount(
            pairs = anchorPairs,
            pairSpecification = pairSpecification
        )
        if (anchorPairs.isNotEmpty() && maximumAnchorCount < productAnchorMix.countRange.first) {
            add(
                GeneratedPuzzleProfileViolation.InsufficientProductAnchorCapacity(
                    minimumRequiredAnchorCount = productAnchorMix.countRange.first,
                    maximumStructurallyAvailableCount = maximumAnchorCount
                )
            )
        }

        val hasCompleteBaseSelection = pairSpecification.canSelectPairCount(
            candidatePairs = eligiblePairs,
            targetPairCount = definition.size.pairCount
        )
        val canCheckCombinedSelection = productAnchorMix.countRange.last <= definition.size.pairCount &&
            (productAnchorMix.countRange.first == 0 || anchorPairs.isNotEmpty()) &&
            maximumAnchorCount >= productAnchorMix.countRange.first &&
            hasCompleteBaseSelection
        if (canCheckCombinedSelection &&
            !pairSpecification.canSelectCompletePairSet(
                candidatePairs = eligiblePairs,
                targetPairCount = definition.size.pairCount
            )
        ) {
            add(
                GeneratedPuzzleProfileViolation.ProductAnchorSelectionInfeasible(
                    requiredPairCount = definition.size.pairCount,
                    productAnchorCountRange = productAnchorMix.countRange
                )
            )
        }
    }

    private fun MutableList<GeneratedPuzzleProfileViolation>.addVarietyViolations(
        definition: GeneratedPuzzleProfileDefinition,
        pairSpecification: GeneratedPuzzlePairSpecification,
        eligiblePairs: List<GeneratedPuzzlePairValues>
    ) {
        addHighValueTargetViolations(definition = definition)
        addPrimeDecoyTargetViolations(
            definition = definition,
            pairSpecification = pairSpecification,
            eligiblePairs = eligiblePairs
        )
    }

    private fun MutableList<GeneratedPuzzleProfileViolation>.addHighValueTargetViolations(
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
                return@forEach
            }

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

        if (highValueTargets.isEmpty()) {
            return
        }

        val structurallyValidTargets = highValueTargets.filter { target ->
            target.rankFromHighest in 1..stripEntryCount
        }
        val forcedKnownEntryIds = structurallyValidTargets
            .filter { target -> target.targetHiddenProbability.value == 0 }
            .mapTo(mutableSetOf()) { target -> stripEntryCount - target.rankFromHighest }
        val forcedHiddenEntryIds = structurallyValidTargets
            .filter { target -> target.targetHiddenProbability.value == 100 }
            .mapTo(mutableSetOf()) { target -> stripEntryCount - target.rankFromHighest }
        val feasibility = maskFeasibility(
            stripEntryCount = stripEntryCount,
            forcedKnownEntryIds = requiredAnchorIds + forcedKnownEntryIds,
            forcedHiddenEntryIds = forcedHiddenEntryIds,
            maxConsecutiveHiddenEntries = definition.initialStripMaskPolicy.maxConsecutiveHiddenEntries
        )
        val maximumKnownCount = minOf(
            definition.initialStripMaskPolicy.knownEntryCountRange.last,
            definition.maximumKnownCountAllowedByDistribution(),
            stripEntryCount - forcedHiddenEntryIds.size
        )
        val minimumAllowedKnownCount = maxOf(
            definition.initialStripMaskPolicy.knownEntryCountRange.first,
            (forcedKnownEntryIds + requiredAnchorIds).size
        )
        if (feasibility.minimumKnownEntryCount == null ||
            feasibility.minimumKnownEntryCount > maximumKnownCount ||
            minimumAllowedKnownCount > maximumKnownCount
        ) {
            add(
                GeneratedPuzzleProfileViolation.HighValueTargetMaskInfeasible(
                    forcedKnownEntryIds = forcedKnownEntryIds,
                    forcedHiddenEntryIds = forcedHiddenEntryIds,
                    knownEntryCountRange = definition.initialStripMaskPolicy.knownEntryCountRange,
                    maxConsecutiveHiddenEntries =
                    definition.initialStripMaskPolicy.maxConsecutiveHiddenEntries
                )
            )
        }
    }

    private fun MutableList<GeneratedPuzzleProfileViolation>.addPrimeDecoyTargetViolations(
        definition: GeneratedPuzzleProfileDefinition,
        pairSpecification: GeneratedPuzzlePairSpecification,
        eligiblePairs: List<GeneratedPuzzlePairValues>
    ) {
        val primeDecoyTarget = definition.varietyPolicy.primeProductDecoyTarget ?: return
        if (primeDecoyTarget.targetPairCount > definition.size.pairCount) {
            add(
                GeneratedPuzzleProfileViolation.PrimeDecoyTargetCountOutsidePuzzle(
                    targetPairCount = primeDecoyTarget.targetPairCount,
                    pairCount = definition.size.pairCount
                )
            )
        }
        if (primeDecoyTarget.targetPuzzlePercent.value == 0) {
            return
        }

        val decoyPairs = eligiblePairs.filter { pair ->
            primeDecoyTarget.pairPattern.matches(pair = pair)
        }
        val maximumDecoyCount = definition.maximumStructurallySelectablePairCount(
            pairs = decoyPairs,
            pairSpecification = pairSpecification
        )
        if (maximumDecoyCount < primeDecoyTarget.targetPairCount) {
            add(
                GeneratedPuzzleProfileViolation.PrimeDecoyTargetUnreachable(
                    pairPattern = primeDecoyTarget.pairPattern,
                    targetPairCount = primeDecoyTarget.targetPairCount,
                    maximumStructurallyAvailableCount = maximumDecoyCount
                )
            )
        }
    }

    private fun GeneratedPuzzleProfileDefinition.maximumStructurallySelectablePairCount(
        pairs: List<GeneratedPuzzlePairValues>,
        pairSpecification: GeneratedPuzzlePairSpecification
    ): Int {
        val looseUpperBound = if (resultConstraints.allowsDuplicateBoardResults) {
            pairs.sumOf { pair -> pairSpecification.maximumRepeatCount(pair = pair).toLong() }
        } else {
            pairs.size.toLong()
        }
        val maximumCandidateCount = minOf(
            size.pairCount.toLong(),
            looseUpperBound,
            Int.MAX_VALUE.toLong()
        ).toInt()

        return (maximumCandidateCount downTo 1).firstOrNull { candidateCount ->
            pairSpecification.canSelectPairCount(
                candidatePairs = pairs,
                targetPairCount = candidateCount
            )
        } ?: 0
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
}

private data class MaskFeasibility(val minimumKnownEntryCount: Int?)

private fun maskFeasibility(
    stripEntryCount: Int,
    forcedKnownEntryIds: Set<Int>,
    forcedHiddenEntryIds: Set<Int>,
    maxConsecutiveHiddenEntries: Int
): MaskFeasibility {
    if (forcedKnownEntryIds.any { entryId -> entryId !in 0 until stripEntryCount } ||
        forcedHiddenEntryIds.any { entryId -> entryId !in 0 until stripEntryCount } ||
        forcedKnownEntryIds.any { entryId -> entryId in forcedHiddenEntryIds }
    ) {
        return MaskFeasibility(minimumKnownEntryCount = null)
    }

    var minimumKnownCountByHiddenRun = mapOf(0 to 0)
    repeat(stripEntryCount) { entryId ->
        val next = mutableMapOf<Int, Int>()
        minimumKnownCountByHiddenRun.forEach { (hiddenRun, knownCount) ->
            if (entryId !in forcedHiddenEntryIds) {
                next.merge(0, knownCount + 1, ::minOf)
            }
            if (entryId !in forcedKnownEntryIds && hiddenRun < maxConsecutiveHiddenEntries) {
                next.merge(hiddenRun + 1, knownCount, ::minOf)
            }
        }
        minimumKnownCountByHiddenRun = next
    }

    return MaskFeasibility(
        minimumKnownEntryCount = minimumKnownCountByHiddenRun.values.minOrNull()
    )
}
