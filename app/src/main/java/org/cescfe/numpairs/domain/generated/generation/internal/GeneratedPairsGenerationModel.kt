package org.cescfe.numpairs.domain.generated.generation.internal

import org.cescfe.numpairs.domain.generated.profile.GeneratedPuzzlePairValues
import org.cescfe.numpairs.domain.generated.profile.PrimeProductDecoyPairPattern
import org.cescfe.numpairs.domain.generated.profile.matches
import org.cescfe.numpairs.domain.puzzle.assignment.StripEntryId
import org.cescfe.numpairs.domain.puzzle.assignment.UnorderedStripEntryPair

internal data class GeneratedPairsStripEntry(val id: StripEntryId, val value: Int)

internal data class GeneratedPairsSolvedCandidate(
    val entries: List<GeneratedPairsStripEntry>,
    val pairs: List<GeneratedPairsEntryPair>
)

internal data class GeneratedPairsVariationPlan(
    val primeProductDecoyDirective: GeneratedPairsPrimeProductDecoyDirective,
    val repeatedValueGroupDirective: GeneratedPairsRepeatedValueGroupDirective =
        GeneratedPairsRepeatedValueGroupDirective.Unrestricted,
    val stripEntryVisibilityDirectives: Map<StripEntryId, GeneratedPairsStripEntryVisibilityDirective>
)

internal sealed interface GeneratedPairsPrimeProductDecoyDirective {
    data object Unrestricted : GeneratedPairsPrimeProductDecoyDirective

    data object Exclude : GeneratedPairsPrimeProductDecoyDirective

    data class Include(val pairCount: Int) : GeneratedPairsPrimeProductDecoyDirective {
        init {
            require(pairCount > 0) {
                "A prime-product decoy inclusion directive requires a positive pair count."
            }
        }
    }
}

internal sealed interface GeneratedPairsRepeatedValueGroupDirective {
    data object Unrestricted : GeneratedPairsRepeatedValueGroupDirective

    data object Exclude : GeneratedPairsRepeatedValueGroupDirective

    data class Include(val groupCount: Int) : GeneratedPairsRepeatedValueGroupDirective {
        init {
            require(groupCount > 0) {
                "A repeated-value group inclusion directive requires a positive group count."
            }
        }
    }

    data class IncludeRange(val groupCountRange: IntRange) : GeneratedPairsRepeatedValueGroupDirective {
        init {
            require(!groupCountRange.isEmpty() && groupCountRange.first > 0) {
                "A repeated-value group inclusion range must contain positive counts."
            }
        }
    }
}

internal enum class GeneratedPairsStripEntryVisibilityDirective {
    KNOWN,
    HIDDEN
}

internal data class GeneratedPairsStripMaskSelection(
    val knownEntryIds: Set<StripEntryId>,
    val variationPlanOutcome: GeneratedPairsVariationPlanOutcome
)

internal enum class GeneratedPairsVariationPlanOutcome {
    HONORED,
    FALLBACK
}

internal interface GeneratedPairsPairValues {
    val firstValue: Int
    val secondValue: Int

    val sum: Int
        get() = firstValue + secondValue

    val product: Int
        get() = firstValue * secondValue

    val resultValues: Set<Int>
        get() = setOf(sum, product)

    fun requiredOccurrencesByValue(): Map<Int, Int> = listOf(firstValue, secondValue)
        .groupingBy { value -> value }
        .eachCount()

    fun primeProductDecoyIncrement(): Int = if (isPrimeProductDecoy()) 1 else 0

    fun isPrimeProductDecoy(): Boolean = PrimeProductDecoyPairPattern.ONE_AND_PRIME.matches(
        pair = GeneratedPuzzlePairValues(
            firstValue = firstValue,
            secondValue = secondValue
        )
    )
}

internal data class GeneratedPairsValuePair(override val firstValue: Int, override val secondValue: Int) :
    GeneratedPairsPairValues

internal data class GeneratedPairsEntryPair(
    val firstEntry: GeneratedPairsStripEntry,
    val secondEntry: GeneratedPairsStripEntry
) : GeneratedPairsPairValues {
    override val firstValue: Int = firstEntry.value
    override val secondValue: Int = secondEntry.value
    val solutionPair: UnorderedStripEntryPair = UnorderedStripEntryPair.of(
        firstEntryId = firstEntry.id,
        secondEntryId = secondEntry.id
    )
}
