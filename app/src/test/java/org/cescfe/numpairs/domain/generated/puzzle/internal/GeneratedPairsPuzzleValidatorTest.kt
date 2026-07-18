package org.cescfe.numpairs.domain.generated.puzzle.internal

import org.cescfe.numpairs.domain.generated.generation.generatedPuzzle
import org.cescfe.numpairs.domain.generated.profile.GeneratedPuzzleProfile
import org.cescfe.numpairs.domain.generated.profile.GeneratedPuzzleProfiles
import org.cescfe.numpairs.domain.generated.puzzle.GeneratedPairsPuzzle
import org.cescfe.numpairs.domain.generated.puzzle.GeneratedPairsPuzzleCreation
import org.cescfe.numpairs.domain.generated.puzzle.GeneratedPairsPuzzleValidationRuleId
import org.cescfe.numpairs.domain.generated.puzzle.GeneratedPairsPuzzleValidationViolation
import org.cescfe.numpairs.domain.puzzle.assignment.StripEntryId
import org.cescfe.numpairs.domain.puzzle.assignment.resolvedTileAssignments
import org.cescfe.numpairs.domain.puzzle.model.Board
import org.cescfe.numpairs.domain.puzzle.model.Expression
import org.cescfe.numpairs.domain.puzzle.model.Operator
import org.cescfe.numpairs.domain.puzzle.model.Puzzle
import org.cescfe.numpairs.domain.puzzle.model.PuzzleCompletionState
import org.cescfe.numpairs.domain.puzzle.model.Strip
import org.cescfe.numpairs.domain.puzzle.model.StripItem
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class GeneratedPairsPuzzleValidatorTest {

    @Test
    fun valid_generated_puzzle_has_an_empty_validation_report() {
        val fixture = generatedPuzzleFixture()
        val creation = GeneratedPairsPuzzle.create(
            profile = fixture.profile,
            initialPuzzle = fixture.generatedPuzzle.initialPuzzle,
            solvedPuzzle = fixture.generatedPuzzle.solvedPuzzle
        )

        assertEquals(
            fixture.generatedPuzzle,
            (creation as GeneratedPairsPuzzleCreation.Created).puzzle
        )
    }

    @Test
    fun validation_report_collects_independent_typed_violations_without_short_circuiting() {
        val fixture = generatedPuzzleFixture()
        val report = rejectedCreation(
            profile = fixture.profile,
            initialPuzzle = fixture.generatedPuzzle.solvedPuzzle,
            solvedPuzzle = fixture.generatedPuzzle.initialPuzzle
        )

        assertEquals(
            PuzzleCompletionState.INCOMPLETE,
            report.violations
                .filterIsInstance<GeneratedPairsPuzzleValidationViolation.SolvedPuzzleNotSolved>()
                .single()
                .completionState
        )
        assertTrue(
            report.violations.any {
                it is GeneratedPairsPuzzleValidationViolation.InitialTileExpressionsNotHidden
            }
        )
        assertTrue(
            report.violations.any {
                it is GeneratedPairsPuzzleValidationViolation.SolvedStripValuesNotFullyKnown
            }
        )
        assertTrue(
            report.violations.any {
                it is GeneratedPairsPuzzleValidationViolation.SolvedTileAssignmentsIncomplete
            }
        )
        assertTrue(
            report.violations.any {
                it is GeneratedPairsPuzzleValidationViolation.KnownStripEntryCountOutsideRange
            }
        )
        assertTrue(
            report.violations.any {
                it is GeneratedPairsPuzzleValidationViolation.HiddenStripEntryCountOutsideRange
            }
        )
        assertTrue(
            report.violations.any {
                it is GeneratedPairsPuzzleValidationViolation.KnownStripEntryDistributionMismatch
            }
        )
    }

    @Test
    fun initial_board_result_reordering_is_rejected_at_aggregate_creation() {
        val fixture = generatedPuzzleFixture()
        val initialPuzzle = fixture.generatedPuzzle.initialPuzzle
        val reorderedInitialPuzzle = initialPuzzle.copy(
            board = Board(tiles = initialPuzzle.board.tiles.reversed())
        )

        val rejection = rejectedCreation(
            profile = fixture.profile,
            initialPuzzle = reorderedInitialPuzzle,
            solvedPuzzle = fixture.generatedPuzzle.solvedPuzzle
        )

        assertTrue(
            rejection.violations.any {
                it is GeneratedPairsPuzzleValidationViolation.BoardResultsMismatch
            }
        )
    }

    @Test
    fun incomplete_solved_puzzle_is_rejected_before_initial_state_derivation() {
        val fixture = generatedPuzzleFixture()
        val creation = GeneratedPairsPuzzle.fromSolvedPuzzle(
            profile = fixture.profile,
            solvedPuzzle = fixture.generatedPuzzle.initialPuzzle,
            knownEntryIds = emptySet()
        )
        val rejection = creation as GeneratedPairsPuzzleCreation.Rejected

        assertTrue(
            rejection.violations.any {
                it is GeneratedPairsPuzzleValidationViolation.SolvedPuzzleNotSolved
            }
        )
    }

    @Test
    fun value_and_result_rules_are_evaluated_from_the_assembled_solved_puzzle() {
        val fixture = generatedPuzzleFixture()
        val solvedPuzzle = fixture.generatedPuzzle.solvedPuzzle
        val multiplicationTileIndex = solvedPuzzle.resolvedTileAssignments()
            .first { assignment -> assignment.operator == Operator.MULTIPLICATION }
            .tileIndex
        val additionTileIndex = solvedPuzzle.resolvedTileAssignments()
            .first { assignment -> assignment.operator == Operator.ADDITION }
            .tileIndex
        val excessiveResult = fixture.profile.resultConstraints.maxMultiplicationResult + 1
        val changedTiles = solvedPuzzle.board.tiles.mapIndexed { tileIndex, tile ->
            when (tileIndex) {
                multiplicationTileIndex, additionTileIndex ->
                    tile.copy(result = excessiveResult)

                else -> tile
            }
        }
        val changedEntries = solvedPuzzle.strip.entries.mapIndexed { entryIndex, entry ->
            if (entryIndex >= solvedPuzzle.strip.entries.lastIndex - 1) {
                entry.copy(item = StripItem.Known(1_000))
            } else {
                entry
            }
        }
        val report = rejectedCreation(
            profile = fixture.profile,
            initialPuzzle = fixture.generatedPuzzle.initialPuzzle,
            solvedPuzzle = Puzzle(
                board = Board(tiles = changedTiles),
                strip = Strip.fromEntries(entries = changedEntries)
            )
        )

        assertEquals(
            setOf(1_000),
            report.violations
                .filterIsInstance<GeneratedPairsPuzzleValidationViolation.StripValueOutsideRange>()
                .single()
                .observedValues
        )
        assertEquals(
            mapOf(1_000 to 2),
            report.violations
                .filterIsInstance<GeneratedPairsPuzzleValidationViolation.StripValueOccurrenceExceeded>()
                .single()
                .observedOccurrencesByValue
        )
        assertEquals(
            mapOf(multiplicationTileIndex to excessiveResult),
            report.violations
                .filterIsInstance<GeneratedPairsPuzzleValidationViolation.MultiplicationResultExceeded>()
                .single()
                .observedResultsByTileIndex
        )
        assertEquals(
            setOf(excessiveResult),
            report.violations
                .filterIsInstance<GeneratedPairsPuzzleValidationViolation.DuplicateBoardResults>()
                .single()
                .duplicateResults
        )
        assertEquals(
            setOf(multiplicationTileIndex, additionTileIndex),
            report.violations
                .filterIsInstance<GeneratedPairsPuzzleValidationViolation.BoardResultsMismatch>()
                .single()
                .mismatchedTileIndexes
                .toSet()
        )
    }

    @Test
    fun profile_shape_mismatches_return_typed_expected_and_observed_counts() {
        val lowFixture = generatedPuzzleFixture()
        val mediumPuzzle = generatedPuzzleFixture(
            profile = GeneratedPuzzleProfiles.EIGHT_PAIRS_MEDIUM
        ).generatedPuzzle
        val report = rejectedCreation(
            profile = lowFixture.profile,
            initialPuzzle = mediumPuzzle.initialPuzzle,
            solvedPuzzle = mediumPuzzle.solvedPuzzle
        )

        assertEquals(
            GeneratedPairsPuzzleValidationViolation.SolvedBoardTileCountMismatch(
                expectedCount = 8,
                observedCount = 16
            ),
            report.violations
                .filterIsInstance<GeneratedPairsPuzzleValidationViolation.SolvedBoardTileCountMismatch>()
                .single()
        )
        assertEquals(
            GeneratedPairsPuzzleValidationViolation.InitialStripEntryCountMismatch(
                expectedCount = 8,
                observedCount = 16
            ),
            report.violations
                .filterIsInstance<GeneratedPairsPuzzleValidationViolation.InitialStripEntryCountMismatch>()
                .single()
        )
    }

    @Test
    fun product_anchor_rule_is_evaluated_from_solved_multiplication_tiles() {
        val fixture = generatedPuzzleFixture(profile = GeneratedPuzzleProfiles.EIGHT_PAIRS_MEDIUM)
        val solvedPuzzle = fixture.generatedPuzzle.solvedPuzzle
        val multiplicationTileIndexes = solvedPuzzle.resolvedTileAssignments()
            .filter { assignment -> assignment.operator == Operator.MULTIPLICATION }
            .map { assignment -> assignment.tileIndex }
            .toSet()
        val changedTiles = solvedPuzzle.board.tiles.mapIndexed { tileIndex, tile ->
            if (tileIndex in multiplicationTileIndexes) {
                tile.copy(result = tileIndex + 1)
            } else {
                tile
            }
        }
        val report = rejectedCreation(
            profile = fixture.profile,
            initialPuzzle = fixture.generatedPuzzle.initialPuzzle,
            solvedPuzzle = solvedPuzzle.copy(board = Board(tiles = changedTiles))
        )
        val violation = report.violations
            .filterIsInstance<GeneratedPairsPuzzleValidationViolation.ProductAnchorMixOutsideRange>()
            .single()

        assertEquals(2..4, violation.expectedRange)
        assertEquals(0, violation.observedCount)
    }

    @Test
    fun solved_to_initial_identity_and_visible_values_are_verified() {
        val fixture = generatedPuzzleFixture()
        val initialPuzzle = fixture.generatedPuzzle.initialPuzzle
        val hiddenEntry = initialPuzzle.strip.entries.first { entry -> entry.item == StripItem.Hidden }
        val highestKnownEntry = initialPuzzle.strip.entries.last()
        val highestKnownValue = (highestKnownEntry.item as StripItem.Known).value
        val unknownEntryId = fixture.profile.size.stripEntryCount + 10
        val changedEntries = initialPuzzle.strip.entries.map { entry ->
            when (entry.id) {
                hiddenEntry.id -> entry.copy(id = unknownEntryId)
                highestKnownEntry.id -> entry.copy(item = StripItem.Known(highestKnownValue + 1))
                else -> entry
            }
        }
        val report = rejectedCreation(
            profile = fixture.profile,
            initialPuzzle = initialPuzzle.copy(strip = Strip.fromEntries(entries = changedEntries)),
            solvedPuzzle = fixture.generatedPuzzle.solvedPuzzle
        )

        assertTrue(
            report.violations.any {
                it is GeneratedPairsPuzzleValidationViolation.InitialStripEntryIdentitiesMismatch
            }
        )
        assertEquals(
            setOf(StripEntryId(highestKnownEntry.id)),
            report.violations
                .filterIsInstance<GeneratedPairsPuzzleValidationViolation.InitialVisibleStripValueMismatch>()
                .single()
                .entryIds
        )
    }

    @Test
    fun solved_assignments_are_checked_against_authoritative_strip_identities_and_values() {
        val fixture = generatedPuzzleFixture()
        val solvedPuzzle = fixture.generatedPuzzle.solvedPuzzle
        val assignment = solvedPuzzle.resolvedTileAssignments().first()
        val tile = solvedPuzzle.board.tiles[assignment.tileIndex]
        val leftOperand = tile.expression.leftOperand as Expression.Operand.Known
        val unknownEntryId = fixture.profile.size.stripEntryCount + 10
        val changedTile = tile.copy(
            expression = tile.expression.copy(
                leftOperand = leftOperand.copy(stripEntryId = unknownEntryId)
            )
        )
        val changedTiles = solvedPuzzle.board.tiles.toMutableList().apply {
            this[assignment.tileIndex] = changedTile
        }
        val report = rejectedCreation(
            profile = fixture.profile,
            initialPuzzle = fixture.generatedPuzzle.initialPuzzle,
            solvedPuzzle = solvedPuzzle.copy(board = Board(tiles = changedTiles))
        )
        val invalidReference = report.violations
            .filterIsInstance<GeneratedPairsPuzzleValidationViolation.SolvedOperandEntryReferenceInvalid>()
            .single()

        assertEquals(
            mapOf(assignment.tileIndex to setOf(StripEntryId(unknownEntryId))),
            invalidReference.unknownEntryIdsByTileIndex
        )
        assertTrue(
            report.violations.any {
                it is GeneratedPairsPuzzleValidationViolation.SolvedOperandValueMismatch
            }
        )
        assertTrue(
            report.violations.any {
                it is GeneratedPairsPuzzleValidationViolation.SolvedStripEntryUsageMismatch
            }
        )
        val pairingMismatch = report.violations
            .filterIsInstance<GeneratedPairsPuzzleValidationViolation.SolvedSumProductPairingMismatch>()
            .single()
        assertTrue(pairingMismatch.additionPairs != pairingMismatch.multiplicationPairs)
        assertTrue(
            (pairingMismatch.additionPairs + pairingMismatch.multiplicationPairs).any { pair ->
                StripEntryId(unknownEntryId) == pair.firstEntryId ||
                    StripEntryId(unknownEntryId) == pair.secondEntryId
            }
        )
    }

    @Test
    fun hard_mask_rules_share_typed_context_in_the_final_report() {
        val fixture = generatedPuzzleFixture()
        val initialPuzzle = fixture.generatedPuzzle.initialPuzzle
        val solvedEntries = fixture.generatedPuzzle.solvedPuzzle.strip.entries
        val changedEntries = initialPuzzle.strip.entries.map { entry ->
            if (entry.id in setOf(0, 1, 2)) {
                entry.copy(item = solvedEntries[entry.id].item)
            } else {
                entry.copy(item = StripItem.Hidden)
            }
        }
        val report = rejectedCreation(
            profile = fixture.profile,
            initialPuzzle = initialPuzzle.copy(strip = Strip.fromEntries(entries = changedEntries)),
            solvedPuzzle = fixture.generatedPuzzle.solvedPuzzle
        )

        assertEquals(
            setOf(StripEntryId(fixture.profile.size.stripEntryCount - 1)),
            report.violations
                .filterIsInstance<GeneratedPairsPuzzleValidationViolation.RequiredKnownStripAnchorMissing>()
                .single()
                .missingEntryIds
        )
        assertEquals(
            5,
            report.violations
                .filterIsInstance<GeneratedPairsPuzzleValidationViolation.HiddenStripRunExceeded>()
                .single()
                .maximumObserved
        )
    }

    @Test
    fun player_entered_initial_values_are_not_accepted_as_a_generated_mask() {
        val fixture = generatedPuzzleFixture()
        val initialPuzzle = fixture.generatedPuzzle.initialPuzzle
        val hiddenEntry = initialPuzzle.strip.entries.first { entry -> entry.item == StripItem.Hidden }
        val solvedValue = (
            fixture.generatedPuzzle.solvedPuzzle.strip.entries
                .single { entry -> entry.id == hiddenEntry.id }
                .item as StripItem.Known
            ).value
        val changedEntries = initialPuzzle.strip.entries.map { entry ->
            if (entry.id == hiddenEntry.id) {
                entry.copy(item = StripItem.PlayerEntered(solvedValue))
            } else {
                entry
            }
        }
        val report = rejectedCreation(
            profile = fixture.profile,
            initialPuzzle = initialPuzzle.copy(strip = Strip.fromEntries(entries = changedEntries)),
            solvedPuzzle = fixture.generatedPuzzle.solvedPuzzle
        )

        assertEquals(
            setOf(StripEntryId(hiddenEntry.id)),
            report.violations
                .filterIsInstance<GeneratedPairsPuzzleValidationViolation.InitialStripItemsNotMasked>()
                .single()
                .entryIds
        )
    }

    @Test
    fun validation_rule_codes_are_stable() {
        assertEquals(
            listOf(
                "generated.solved-puzzle-not-solved",
                "generated.solved-board-tile-count-mismatch",
                "generated.solved-strip-entry-count-mismatch",
                "generated.initial-board-tile-count-mismatch",
                "generated.initial-strip-entry-count-mismatch",
                "generated.initial-tile-expressions-not-hidden",
                "generated.board-results-mismatch",
                "generated.initial-strip-entry-identities-mismatch",
                "generated.initial-visible-strip-value-mismatch",
                "generated.initial-strip-items-not-masked",
                "generated.solved-strip-values-not-fully-known",
                "generated.solved-strip-values-not-sorted",
                "generated.solved-tile-assignments-incomplete",
                "generated.solved-operand-entry-reference-invalid",
                "generated.solved-operand-value-mismatch",
                "generated.solved-strip-entry-usage-mismatch",
                "generated.solved-sum-product-pairing-mismatch",
                "generated.strip-value-outside-range",
                "generated.strip-value-occurrence-exceeded",
                "generated.repeated-strip-value-group-count-exceeded",
                "generated.repeated-strip-value-group-count-below-minimum",
                "generated.duplicate-board-results",
                "generated.multiplication-result-exceeded",
                "generated.product-anchor-mix-outside-range",
                "generated.known-strip-entry-count-outside-range",
                "generated.hidden-strip-entry-count-outside-range",
                "generated.required-known-strip-anchor-missing",
                "generated.hidden-strip-run-exceeded",
                "generated.known-strip-entry-distribution-mismatch"
            ),
            GeneratedPairsPuzzleValidationRuleId.entries.map(GeneratedPairsPuzzleValidationRuleId::code)
        )
    }
}

private data class GeneratedPuzzleValidationFixture(
    val profile: GeneratedPuzzleProfile,
    val generatedPuzzle: GeneratedPairsPuzzle
)

private fun generatedPuzzleFixture(
    profile: GeneratedPuzzleProfile = GeneratedPuzzleProfiles.FOUR_PAIRS_LOW
): GeneratedPuzzleValidationFixture {
    val generatedPuzzle = generatedPuzzle(profile = profile, seed = 42)

    return GeneratedPuzzleValidationFixture(
        profile = profile,
        generatedPuzzle = generatedPuzzle
    )
}

private fun rejectedCreation(
    profile: GeneratedPuzzleProfile,
    initialPuzzle: Puzzle,
    solvedPuzzle: Puzzle
): GeneratedPairsPuzzleCreation.Rejected = GeneratedPairsPuzzle.create(
    profile = profile,
    initialPuzzle = initialPuzzle,
    solvedPuzzle = solvedPuzzle
) as GeneratedPairsPuzzleCreation.Rejected
