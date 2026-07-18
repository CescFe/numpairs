package org.cescfe.numpairs.domain.generated.profile

import org.junit.Assert.assertEquals
import org.junit.Assert.assertThrows
import org.junit.Assert.assertTrue
import org.junit.Test

class GeneratedPuzzleProfileTest {

    @Test
    fun generated_puzzle_size_derives_strip_entries_and_board_tiles_from_pair_count() {
        val size = GeneratedPuzzleSize(pairCount = 6)

        assertEquals(12, size.stripEntryCount)
        assertEquals(12, size.boardTileCount)
    }

    @Test
    fun factory_creates_a_profile_only_after_cross_policy_validation() {
        val creation = GeneratedPuzzleProfile.create(definition = validDefinition())
        val profile = (creation as GeneratedPuzzleProfileCreation.Created).profile

        assertEquals(GeneratedPuzzleProfileId("valid-test-profile"), profile.id)
        assertEquals(2..2, profile.hiddenEntryCountRange)
    }

    @Test
    fun factory_accepts_a_definition_at_each_configured_validation_limit() {
        val creation = GeneratedPuzzleProfile.create(
            definition = smallestBoundedDefinition(),
            validationLimits = GeneratedPuzzleProfileValidationLimits(
                maxCatalogExpansions = 2,
                maxPairSelectionStates = 1,
                maxMaskStates = 3
            )
        )

        assertTrue(creation is GeneratedPuzzleProfileCreation.Created)
    }

    @Test
    fun first_catalog_expansion_beyond_the_limit_returns_typed_context() {
        val rejected = GeneratedPuzzleProfile.create(
            definition = smallestBoundedDefinition(),
            validationLimits = GeneratedPuzzleProfileValidationLimits(
                maxCatalogExpansions = 1,
                maxPairSelectionStates = 100,
                maxMaskStates = 100
            )
        ) as GeneratedPuzzleProfileCreation.Rejected

        val violation = rejected.violations
            .filterIsInstance<GeneratedPuzzleProfileViolation.ValidationWorkLimitExceeded>()
            .single()
        assertEquals(GeneratedPuzzleProfileValidationWorkKind.CATALOG_EXPANSION, violation.workKind)
        assertEquals(1, violation.configuredLimit)
        assertEquals(1, violation.consumedWork)
    }

    @Test
    fun pair_selection_and_mask_state_limits_return_their_own_work_kind() {
        val pairSelectionRejected = GeneratedPuzzleProfile.create(
            definition = validDefinition(),
            validationLimits = GeneratedPuzzleProfileValidationLimits(
                maxCatalogExpansions = 100,
                maxPairSelectionStates = 1,
                maxMaskStates = 100
            )
        ) as GeneratedPuzzleProfileCreation.Rejected
        val maskRejected = GeneratedPuzzleProfile.create(
            definition = smallestBoundedDefinition(),
            validationLimits = GeneratedPuzzleProfileValidationLimits(
                maxCatalogExpansions = 2,
                maxPairSelectionStates = 1,
                maxMaskStates = 2
            )
        ) as GeneratedPuzzleProfileCreation.Rejected

        assertEquals(
            GeneratedPuzzleProfileValidationWorkKind.PAIR_SELECTION_STATE,
            pairSelectionRejected.singleLimitViolation().workKind
        )
        assertEquals(
            GeneratedPuzzleProfileValidationWorkKind.MASK_STATE,
            maskRejected.singleLimitViolation().workKind
        )
    }

    @Test(timeout = 1_000)
    fun unsupported_large_catalog_is_rejected_without_materializing_the_candidate_space() {
        val definition = smallestBoundedDefinition().copy(
            stripValuePolicy = StripValuePolicy(valueRange = 1..46_340, maxOccurrencesPerValue = 1),
            resultConstraints = ResultConstraints(
                maxMultiplicationResult = Int.MAX_VALUE,
                allowsDuplicateBoardResults = false
            )
        )

        val rejected = GeneratedPuzzleProfile.create(
            definition = definition,
            validationLimits = GeneratedPuzzleProfileValidationLimits(
                maxCatalogExpansions = 5,
                maxPairSelectionStates = 100,
                maxMaskStates = 100
            )
        ) as GeneratedPuzzleProfileCreation.Rejected

        val violation = rejected.singleLimitViolation()
        assertEquals(GeneratedPuzzleProfileValidationWorkKind.CATALOG_EXPANSION, violation.workKind)
        assertEquals(5, violation.consumedWork)
    }

    @Test
    fun factory_rejects_each_structural_contradiction_with_a_stable_rule_id() {
        val cases = listOf(
            InvalidProfileCase(
                name = "strip value capacity",
                definition = validDefinition().copy(
                    stripValuePolicy = StripValuePolicy(valueRange = 2..3, maxOccurrencesPerValue = 1)
                ),
                expectedRuleId = GeneratedPuzzleProfileRuleId.STRIP_VALUE_CAPACITY
            ),
            InvalidProfileCase(
                name = "known entry range",
                definition = validDefinition().copy(
                    initialStripMaskPolicy = validMaskPolicy().copy(knownEntryCountRange = 2..5)
                ),
                expectedRuleId = GeneratedPuzzleProfileRuleId.KNOWN_ENTRY_RANGE
            ),
            InvalidProfileCase(
                name = "required anchor capacity",
                definition = validDefinition().copy(
                    initialStripMaskPolicy = validMaskPolicy().copy(
                        knownEntryCountRange = 0..0,
                        maxConsecutiveHiddenEntries = 4
                    )
                ),
                expectedRuleId = GeneratedPuzzleProfileRuleId.REQUIRED_ANCHOR_CAPACITY
            ),
            InvalidProfileCase(
                name = "hidden run feasibility",
                definition = validDefinition().copy(
                    initialStripMaskPolicy = validMaskPolicy().copy(
                        knownEntryCountRange = 1..1,
                        requiredAnchors = emptySet(),
                        maxConsecutiveHiddenEntries = 1
                    )
                ),
                expectedRuleId = GeneratedPuzzleProfileRuleId.HIDDEN_RUN_FEASIBILITY
            ),
            InvalidProfileCase(
                name = "spread distribution capacity",
                definition = validDefinition().copy(
                    initialStripMaskPolicy = validMaskPolicy().copy(
                        knownEntryCountRange = 3..3,
                        distributionPolicy =
                        StripKnownEntryDistributionPolicy.SpreadAcrossStripAndPairsWhenPossible
                    )
                ),
                expectedRuleId = GeneratedPuzzleProfileRuleId.SPREAD_DISTRIBUTION_CAPACITY
            ),
            InvalidProfileCase(
                name = "high value target rank",
                definition = validDefinition().copy(
                    varietyPolicy = GeneratedPuzzleVarietyPolicy(
                        highValueMaskTargets = listOf(highValueTarget(rankFromHighest = 5))
                    )
                ),
                expectedRuleId = GeneratedPuzzleProfileRuleId.HIGH_VALUE_TARGET_RANK
            ),
            InvalidProfileCase(
                name = "duplicate high value target rank",
                definition = validDefinition().copy(
                    varietyPolicy = GeneratedPuzzleVarietyPolicy(
                        highValueMaskTargets = listOf(
                            highValueTarget(rankFromHighest = 2),
                            highValueTarget(rankFromHighest = 2)
                        )
                    )
                ),
                expectedRuleId = GeneratedPuzzleProfileRuleId.DUPLICATE_HIGH_VALUE_TARGET_RANK
            ),
            InvalidProfileCase(
                name = "high value target anchor conflict",
                definition = validDefinition().copy(
                    varietyPolicy = GeneratedPuzzleVarietyPolicy(
                        highValueMaskTargets = listOf(
                            highValueTarget(rankFromHighest = 1, hiddenPercentage = 20)
                        )
                    )
                ),
                expectedRuleId = GeneratedPuzzleProfileRuleId.HIGH_VALUE_TARGET_ANCHOR_CONFLICT
            ),
            InvalidProfileCase(
                name = "deterministic high value target mask feasibility",
                definition = validDefinition().copy(
                    initialStripMaskPolicy = validMaskPolicy().copy(
                        knownEntryCountRange = 1..1,
                        requiredAnchors = emptySet(),
                        maxConsecutiveHiddenEntries = 3
                    ),
                    varietyPolicy = GeneratedPuzzleVarietyPolicy(
                        highValueMaskTargets = listOf(
                            highValueTarget(rankFromHighest = 1, hiddenPercentage = 0),
                            highValueTarget(rankFromHighest = 2, hiddenPercentage = 0)
                        )
                    )
                ),
                expectedRuleId = GeneratedPuzzleProfileRuleId.HIGH_VALUE_TARGET_MASK_FEASIBILITY
            ),
            InvalidProfileCase(
                name = "product anchor count range",
                definition = validDefinition().copy(
                    resultConstraints = validResultConstraints().copy(
                        productAnchorMix = ProductAnchorMix(
                            productResultGreaterThan = 10,
                            countRange = 0..3
                        )
                    )
                ),
                expectedRuleId = GeneratedPuzzleProfileRuleId.PRODUCT_ANCHOR_COUNT_RANGE
            ),
            InvalidProfileCase(
                name = "product anchor threshold",
                definition = validDefinition().copy(
                    resultConstraints = ResultConstraints(
                        maxMultiplicationResult = 20,
                        allowsDuplicateBoardResults = false,
                        productAnchorMix = ProductAnchorMix(
                            productResultGreaterThan = 20,
                            countRange = 1..1
                        )
                    )
                ),
                expectedRuleId = GeneratedPuzzleProfileRuleId.PRODUCT_ANCHOR_THRESHOLD
            ),
            InvalidProfileCase(
                name = "product anchor threshold beyond reachable catalog",
                definition = validDefinition().copy(
                    resultConstraints = ResultConstraints(
                        maxMultiplicationResult = 100,
                        allowsDuplicateBoardResults = false,
                        productAnchorMix = ProductAnchorMix(
                            productResultGreaterThan = 99,
                            countRange = 1..1
                        )
                    )
                ),
                expectedRuleId = GeneratedPuzzleProfileRuleId.PRODUCT_ANCHOR_THRESHOLD
            ),
            InvalidProfileCase(
                name = "product anchor capacity",
                definition = validDefinition().copy(
                    stripValuePolicy = StripValuePolicy(valueRange = 2..4, maxOccurrencesPerValue = 2),
                    resultConstraints = ResultConstraints(
                        maxMultiplicationResult = 12,
                        allowsDuplicateBoardResults = false,
                        productAnchorMix = ProductAnchorMix(
                            productResultGreaterThan = 8,
                            countRange = 2..2
                        )
                    )
                ),
                expectedRuleId = GeneratedPuzzleProfileRuleId.PRODUCT_ANCHOR_CAPACITY
            ),
            InvalidProfileCase(
                name = "product anchor complete selection feasibility",
                definition = validDefinition().copy(
                    size = GeneratedPuzzleSize(pairCount = 1),
                    stripValuePolicy = StripValuePolicy(valueRange = 2..3, maxOccurrencesPerValue = 1),
                    resultConstraints = ResultConstraints(
                        maxMultiplicationResult = 6,
                        allowsDuplicateBoardResults = false,
                        productAnchorMix = ProductAnchorMix(
                            productResultGreaterThan = 0,
                            countRange = 0..0
                        )
                    ),
                    initialStripMaskPolicy = validMaskPolicy().copy(
                        knownEntryCountRange = 1..1,
                        maxConsecutiveHiddenEntries = 1
                    )
                ),
                expectedRuleId = GeneratedPuzzleProfileRuleId.PRODUCT_ANCHOR_SELECTION_FEASIBILITY
            ),
            InvalidProfileCase(
                name = "product anchors cannot coexist with a complete selection",
                definition = validDefinition().copy(
                    size = GeneratedPuzzleSize(pairCount = 3),
                    stripValuePolicy = StripValuePolicy(valueRange = 1..3, maxOccurrencesPerValue = 3),
                    resultConstraints = ResultConstraints(
                        maxMultiplicationResult = 6,
                        allowsDuplicateBoardResults = false,
                        productAnchorMix = ProductAnchorMix(
                            productResultGreaterThan = 1,
                            countRange = 1..1
                        )
                    ),
                    initialStripMaskPolicy = validMaskPolicy().copy(
                        knownEntryCountRange = 2..2,
                        maxConsecutiveHiddenEntries = 3
                    )
                ),
                expectedRuleId = GeneratedPuzzleProfileRuleId.PRODUCT_ANCHOR_SELECTION_FEASIBILITY
            ),
            InvalidProfileCase(
                name = "prime decoy count",
                definition = validDefinition().copy(
                    varietyPolicy = GeneratedPuzzleVarietyPolicy(
                        primeProductDecoyTarget = primeDecoyTarget(
                            targetPairCount = 3,
                            targetPercentage = 0
                        )
                    )
                ),
                expectedRuleId = GeneratedPuzzleProfileRuleId.PRIME_DECOY_TARGET_COUNT
            ),
            InvalidProfileCase(
                name = "prime decoy feasibility",
                definition = validDefinition().copy(
                    varietyPolicy = GeneratedPuzzleVarietyPolicy(
                        primeProductDecoyTarget = primeDecoyTarget()
                    )
                ),
                expectedRuleId = GeneratedPuzzleProfileRuleId.PRIME_DECOY_TARGET_FEASIBILITY
            ),
            InvalidProfileCase(
                name = "prime decoy target pair capacity",
                definition = validDefinition().copy(
                    stripValuePolicy = StripValuePolicy(valueRange = 1..3, maxOccurrencesPerValue = 2),
                    resultConstraints = validResultConstraints().copy(maxMultiplicationResult = 6),
                    varietyPolicy = GeneratedPuzzleVarietyPolicy(
                        primeProductDecoyTarget = primeDecoyTarget(targetPairCount = 2)
                    )
                ),
                expectedRuleId = GeneratedPuzzleProfileRuleId.PRIME_DECOY_TARGET_FEASIBILITY
            ),
            InvalidProfileCase(
                name = "eligible value pair catalog",
                definition = validDefinition().copy(
                    resultConstraints = validResultConstraints().copy(maxMultiplicationResult = 1)
                ),
                expectedRuleId = GeneratedPuzzleProfileRuleId.ELIGIBLE_VALUE_PAIR_CATALOG
            ),
            InvalidProfileCase(
                name = "eligible value pair catalog capacity",
                definition = validDefinition().copy(
                    stripValuePolicy = StripValuePolicy(valueRange = 2..3, maxOccurrencesPerValue = 2),
                    resultConstraints = validResultConstraints().copy(maxMultiplicationResult = 6)
                ),
                expectedRuleId = GeneratedPuzzleProfileRuleId.ELIGIBLE_VALUE_PAIR_CATALOG
            ),
            InvalidProfileCase(
                name = "arithmetic result range",
                definition = validDefinition().copy(
                    stripValuePolicy = StripValuePolicy(valueRange = 1..50_000, maxOccurrencesPerValue = 1)
                ),
                expectedRuleId = GeneratedPuzzleProfileRuleId.ARITHMETIC_RESULT_RANGE
            )
        )

        cases.forEach { case ->
            val rejected = GeneratedPuzzleProfile.create(case.definition) as GeneratedPuzzleProfileCreation.Rejected

            assertEquals(
                case.name,
                setOf(case.expectedRuleId),
                rejected.violations.map(GeneratedPuzzleProfileViolation::ruleId).toSet()
            )
        }
    }

    @Test
    fun factory_accumulates_all_cross_policy_violations_with_typed_context() {
        val definition = validDefinition().copy(
            stripValuePolicy = StripValuePolicy(valueRange = 2..3, maxOccurrencesPerValue = 1),
            initialStripMaskPolicy = validMaskPolicy().copy(
                knownEntryCountRange = 3..5,
                distributionPolicy = StripKnownEntryDistributionPolicy.SpreadAcrossStripAndPairsWhenPossible
            ),
            resultConstraints = ResultConstraints(
                maxMultiplicationResult = 10,
                allowsDuplicateBoardResults = false,
                productAnchorMix = ProductAnchorMix(
                    productResultGreaterThan = 10,
                    countRange = 1..3
                )
            ),
            varietyPolicy = GeneratedPuzzleVarietyPolicy(
                highValueMaskTargets = listOf(
                    highValueTarget(rankFromHighest = 5),
                    highValueTarget(rankFromHighest = 5)
                )
            )
        )
        val rejected = GeneratedPuzzleProfile.create(definition) as GeneratedPuzzleProfileCreation.Rejected
        val ruleIds = rejected.violations.map(GeneratedPuzzleProfileViolation::ruleId).toSet()

        assertTrue(ruleIds.size >= 7)
        assertTrue(GeneratedPuzzleProfileRuleId.STRIP_VALUE_CAPACITY in ruleIds)
        assertTrue(GeneratedPuzzleProfileRuleId.KNOWN_ENTRY_RANGE in ruleIds)
        assertTrue(GeneratedPuzzleProfileRuleId.SPREAD_DISTRIBUTION_CAPACITY in ruleIds)
        assertTrue(GeneratedPuzzleProfileRuleId.PRODUCT_ANCHOR_COUNT_RANGE in ruleIds)
        assertTrue(GeneratedPuzzleProfileRuleId.PRODUCT_ANCHOR_THRESHOLD in ruleIds)
        assertTrue(GeneratedPuzzleProfileRuleId.HIGH_VALUE_TARGET_RANK in ruleIds)
        assertTrue(GeneratedPuzzleProfileRuleId.DUPLICATE_HIGH_VALUE_TARGET_RANK in ruleIds)

        val capacityViolation = rejected.violations
            .filterIsInstance<GeneratedPuzzleProfileViolation.InsufficientStripValueCapacity>()
            .single()
        assertEquals(4, capacityViolation.requiredEntryCount)
        assertEquals(2L, capacityViolation.availableEntryCount)
    }

    @Test
    fun invalid_profile_creation_cannot_be_unwrapped_as_a_usable_profile() {
        val rejected = GeneratedPuzzleProfile.create(
            definition = validDefinition().copy(
                stripValuePolicy = StripValuePolicy(valueRange = 2..3, maxOccurrencesPerValue = 1)
            )
        )

        assertThrows(IllegalArgumentException::class.java) {
            rejected.getOrThrow()
        }
    }

    @Test
    fun created_profile_is_not_affected_by_mutating_definition_collections() {
        val anchors = mutableSetOf(RequiredKnownStripAnchor.HIGHEST_STRIP_ENTRY)
        val targets = mutableListOf(highValueTarget(rankFromHighest = 2))
        val definition = validDefinition().copy(
            initialStripMaskPolicy = validMaskPolicy().copy(requiredAnchors = anchors),
            varietyPolicy = GeneratedPuzzleVarietyPolicy(highValueMaskTargets = targets)
        )
        val profile = GeneratedPuzzleProfile.create(definition).getOrThrow()

        anchors.clear()
        targets += highValueTarget(rankFromHighest = 3)

        assertEquals(
            setOf(RequiredKnownStripAnchor.HIGHEST_STRIP_ENTRY),
            profile.initialStripMaskPolicy.requiredAnchors
        )
        assertEquals(listOf(highValueTarget(rankFromHighest = 2)), profile.varietyPolicy.highValueMaskTargets)
    }

    @Test
    fun profile_rule_codes_are_stable() {
        assertEquals(
            listOf(
                "profile.strip-value-capacity",
                "profile.known-entry-range",
                "profile.required-anchor-capacity",
                "profile.hidden-run-feasibility",
                "profile.spread-distribution-capacity",
                "profile.distinct-solution-pair-distribution-feasibility",
                "profile.high-value-target-rank",
                "profile.duplicate-high-value-target-rank",
                "profile.high-value-target-anchor-conflict",
                "profile.high-value-target-mask-feasibility",
                "profile.product-anchor-count-range",
                "profile.product-anchor-threshold",
                "profile.product-anchor-capacity",
                "profile.product-anchor-selection-feasibility",
                "profile.prime-decoy-target-count",
                "profile.prime-decoy-target-feasibility",
                "profile.repeated-value-group-target-feasibility",
                "profile.eligible-value-pair-catalog",
                "profile.arithmetic-result-range",
                "profile.validation-work-limit"
            ),
            GeneratedPuzzleProfileRuleId.entries.map(GeneratedPuzzleProfileRuleId::code)
        )
    }

    @Test
    fun primitive_value_objects_reject_invalid_local_state() {
        assertThrows(IllegalArgumentException::class.java) {
            GeneratedPuzzleSize(pairCount = 0)
        }
        assertThrows(IllegalArgumentException::class.java) {
            StripValuePolicy(valueRange = IntRange.EMPTY, maxOccurrencesPerValue = 1)
        }
        assertThrows(IllegalArgumentException::class.java) {
            StripValuePolicy(valueRange = 0..2, maxOccurrencesPerValue = 1)
        }
        assertThrows(IllegalArgumentException::class.java) {
            ResultConstraints(maxMultiplicationResult = 0, allowsDuplicateBoardResults = false)
        }
        assertThrows(IllegalArgumentException::class.java) {
            ProductAnchorMix(productResultGreaterThan = 10, countRange = -1..2)
        }
        assertThrows(IllegalArgumentException::class.java) {
            InitialStripMaskPolicy(
                knownEntryCountRange = IntRange.EMPTY,
                requiredAnchors = emptySet(),
                distributionPolicy = StripKnownEntryDistributionPolicy.Unrestricted,
                maxConsecutiveHiddenEntries = 2
            )
        }
        assertThrows(IllegalArgumentException::class.java) {
            ProbabilityPercent(-1)
        }
        assertThrows(IllegalArgumentException::class.java) {
            ProbabilityPercent(101)
        }
        assertThrows(IllegalArgumentException::class.java) {
            GeneratedPuzzleProfileValidationLimits(maxCatalogExpansions = 0)
        }
        assertThrows(IllegalArgumentException::class.java) {
            GeneratedPuzzleProfileValidationLimits(maxPairSelectionStates = 0)
        }
        assertThrows(IllegalArgumentException::class.java) {
            GeneratedPuzzleProfileValidationLimits(maxMaskStates = 0)
        }
    }
}

private data class InvalidProfileCase(
    val name: String,
    val definition: GeneratedPuzzleProfileDefinition,
    val expectedRuleId: GeneratedPuzzleProfileRuleId
)

private fun GeneratedPuzzleProfileCreation.Rejected.singleLimitViolation():
    GeneratedPuzzleProfileViolation.ValidationWorkLimitExceeded =
    violations
        .filterIsInstance<GeneratedPuzzleProfileViolation.ValidationWorkLimitExceeded>()
        .single()

private fun smallestBoundedDefinition(): GeneratedPuzzleProfileDefinition = GeneratedPuzzleProfileDefinition(
    id = GeneratedPuzzleProfileId("smallest-bounded-profile"),
    difficulty = DifficultyTier.LOW,
    size = GeneratedPuzzleSize(pairCount = 1),
    stripValuePolicy = StripValuePolicy(
        valueRange = 2..3,
        maxOccurrencesPerValue = 1
    ),
    resultConstraints = ResultConstraints(
        maxMultiplicationResult = 6,
        allowsDuplicateBoardResults = false
    ),
    initialStripMaskPolicy = InitialStripMaskPolicy(
        knownEntryCountRange = 1..1,
        requiredAnchors = setOf(RequiredKnownStripAnchor.HIGHEST_STRIP_ENTRY),
        distributionPolicy = StripKnownEntryDistributionPolicy.Unrestricted,
        maxConsecutiveHiddenEntries = 1
    ),
    generationPolicy = GenerationPolicy(isBoardTileShufflingEnabled = true)
)

private fun validDefinition(): GeneratedPuzzleProfileDefinition = GeneratedPuzzleProfileDefinition(
    id = GeneratedPuzzleProfileId("valid-test-profile"),
    difficulty = DifficultyTier.LOW,
    size = GeneratedPuzzleSize(pairCount = 2),
    stripValuePolicy = StripValuePolicy(
        valueRange = 2..10,
        maxOccurrencesPerValue = 1
    ),
    resultConstraints = validResultConstraints(),
    initialStripMaskPolicy = validMaskPolicy(),
    generationPolicy = GenerationPolicy(isBoardTileShufflingEnabled = true)
)

private fun validResultConstraints(): ResultConstraints = ResultConstraints(
    maxMultiplicationResult = 50,
    allowsDuplicateBoardResults = false
)

private fun validMaskPolicy(): InitialStripMaskPolicy = InitialStripMaskPolicy(
    knownEntryCountRange = 2..2,
    requiredAnchors = setOf(RequiredKnownStripAnchor.HIGHEST_STRIP_ENTRY),
    distributionPolicy = StripKnownEntryDistributionPolicy.Unrestricted,
    maxConsecutiveHiddenEntries = 2
)

private fun highValueTarget(rankFromHighest: Int, hiddenPercentage: Int = 0): HighValueMaskTarget = HighValueMaskTarget(
    rankFromHighest = rankFromHighest,
    targetHiddenProbability = ProbabilityPercent(hiddenPercentage)
)

private fun primeDecoyTarget(targetPairCount: Int = 1, targetPercentage: Int = 30): PrimeProductDecoyTarget =
    PrimeProductDecoyTarget(
        targetPuzzlePercent = ProbabilityPercent(targetPercentage),
        targetPairCount = targetPairCount,
        pairPattern = PrimeProductDecoyPairPattern.ONE_AND_PRIME
    )
