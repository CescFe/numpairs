package org.cescfe.numpairs.feature.daily.share

import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.time.format.FormatStyle
import java.util.Locale
import org.cescfe.numpairs.domain.daily.DailyChallengeId
import org.cescfe.numpairs.domain.daily.DailyCompletion
import org.cescfe.numpairs.domain.daily.DailyElapsedTime
import org.cescfe.numpairs.domain.daily.DailyMovementCount
import org.cescfe.numpairs.domain.daily.DailyRecipeVersion
import org.cescfe.numpairs.feature.daily.DailyChallengeNameCopy
import org.cescfe.numpairs.feature.daily.DailyRecipes
import org.cescfe.numpairs.feature.generated.GeneratedModes
import org.junit.Assert.assertEquals
import org.junit.Assert.assertThrows
import org.junit.Test

class DailyCompletionShareTextFormatterTest {
    @Test
    fun timed_result_with_movements_contains_the_shared_frozen_completion_metrics() {
        val identity = DailyRecipes.FOUR_PAIRS_LOW_V1.identityFor(
            LocalDate.of(2026, 7, 25)
        )
        val locale = Locale.US

        val text = DailyCompletionShareTextFormatter().format(
            completion = completion(
                identity = identity,
                elapsedMilliseconds = 125_999,
                movementCount = 23
            ),
            copy = englishCopy(),
            locale = locale
        )
        val expectedDate = localizedDate(identity, locale)

        assertEquals(
            "NumPairs Daily · $expectedDate\n4 Pairs · Low · Completed in 02:05 · 23 moves",
            text.value
        )
    }

    @Test
    fun timed_legacy_completion_without_movements_keeps_unbounded_minutes() {
        val identity = DailyRecipes.FOUR_PAIRS_LOW_V1.identityFor(
            LocalDate.of(2026, 7, 25)
        )

        val text = DailyCompletionShareTextFormatter().format(
            completion = completion(identity, elapsedMilliseconds = 3_661_999),
            copy = englishCopy(),
            locale = Locale.US
        )

        assertEquals(
            "NumPairs Daily · Jul 25, 2026\n4 Pairs · Low · Completed in 61:01",
            text.value
        )
    }

    @Test
    fun formatter_localizes_display_copy_without_changing_the_canonical_identity() {
        val identity = DailyRecipes.FOUR_PAIRS_LOW_V1.identityFor(
            LocalDate.of(2028, 2, 29)
        )
        val canonicalDate = identity.canonicalLocalDate
        val locale = Locale.forLanguageTag("es-ES")

        val text = DailyCompletionShareTextFormatter().format(
            completion = completion(identity, elapsedMilliseconds = 65_432),
            copy = DailyCompletionShareCopy(
                dailyName = "NumPairs Daily",
                challengeNames = challengeNames(fourPairsLow = "4 pares · Baja"),
                completedStatus = "Completado",
                completedResultStatusFormat = "Completado en %1\$s",
                movementSingularFormat = "%1\$s movimiento",
                movementPluralFormat = "%1\$s movimientos"
            ),
            locale = locale
        )
        val expectedDate = localizedDate(identity, locale)

        assertEquals(
            "NumPairs Daily · $expectedDate\n4 pares · Baja · Completado en 01:05",
            text.value
        )
        assertEquals("2028-02-29", canonicalDate)
        assertEquals(canonicalDate, identity.canonicalLocalDate)
    }

    @Test
    fun formatter_shares_the_authoritative_scheduled_size_and_difficulty_for_every_weekday() {
        val scenarios = listOf(
            LocalDate.of(2026, 8, 31) to "3 Pairs · Low",
            LocalDate.of(2026, 9, 1) to "4 Pairs · Low",
            LocalDate.of(2026, 9, 2) to "3 Pairs · Medium",
            LocalDate.of(2026, 9, 3) to "4 Pairs · Medium",
            LocalDate.of(2026, 9, 4) to "8 Pairs · Medium",
            LocalDate.of(2026, 9, 5) to "3 Pairs · Medium",
            LocalDate.of(2026, 9, 6) to "4 Pairs · Low"
        )

        scenarios.forEach { (date, expectedChallengeName) ->
            val identity = DailyRecipes.WEEKLY_SCHEDULE_V2.identityFor(date)
            val text = DailyCompletionShareTextFormatter().format(
                completion = completion(identity, elapsedMilliseconds = 65_432),
                copy = englishCopy(),
                locale = Locale.US
            )

            assertEquals(
                "NumPairs Daily · ${localizedDate(identity, Locale.US)}\n" +
                    "$expectedChallengeName · Completed in 01:05",
                text.value
            )
        }
    }

    @Test
    fun supported_locales_use_their_singular_and_plural_movement_copy() {
        val identity = DailyRecipes.FOUR_PAIRS_LOW_V1.identityFor(
            LocalDate.of(2026, 7, 25)
        )
        val scenarios = listOf(
            LocalizedMovementScenario(
                locale = Locale.US,
                completedResultStatusFormat = "Completed in %1\$s",
                movementSingularFormat = "%1\$s move",
                movementPluralFormat = "%1\$s moves",
                expectedSingularStatus = "Completed in 00:01 · 1 move",
                expectedPluralStatus = "Completed in 00:01 · 0 moves"
            ),
            LocalizedMovementScenario(
                locale = Locale.forLanguageTag("es-ES"),
                completedResultStatusFormat = "Completado en %1\$s",
                movementSingularFormat = "%1\$s movimiento",
                movementPluralFormat = "%1\$s movimientos",
                expectedSingularStatus = "Completado en 00:01 · 1 movimiento",
                expectedPluralStatus = "Completado en 00:01 · 0 movimientos"
            ),
            LocalizedMovementScenario(
                locale = Locale.forLanguageTag("ca-ES-valencia"),
                completedResultStatusFormat = "Completat en %1\$s",
                movementSingularFormat = "%1\$s moviment",
                movementPluralFormat = "%1\$s moviments",
                expectedSingularStatus = "Completat en 00:01 · 1 moviment",
                expectedPluralStatus = "Completat en 00:01 · 0 moviments"
            ),
            LocalizedMovementScenario(
                locale = Locale.GERMANY,
                completedResultStatusFormat = "Abgeschlossen in %1\$s",
                movementSingularFormat = "%1\$s Zug",
                movementPluralFormat = "%1\$s Züge",
                expectedSingularStatus = "Abgeschlossen in 00:01 · 1 Zug",
                expectedPluralStatus = "Abgeschlossen in 00:01 · 0 Züge"
            )
        )

        scenarios.forEach { scenario ->
            val copy = localizedMovementCopy(scenario)
            val localizedDate = localizedDate(identity, scenario.locale)

            assertEquals(
                "Daily · $localizedDate\nChallenge · ${scenario.expectedSingularStatus}",
                DailyCompletionShareTextFormatter().format(
                    completion = completion(identity, elapsedMilliseconds = 1_999, movementCount = 1),
                    copy = copy,
                    locale = scenario.locale
                ).value
            )
            assertEquals(
                "Daily · $localizedDate\nChallenge · ${scenario.expectedPluralStatus}",
                DailyCompletionShareTextFormatter().format(
                    completion = completion(identity, elapsedMilliseconds = 1_999, movementCount = 0),
                    copy = copy,
                    locale = scenario.locale
                ).value
            )
        }
    }

    @Test
    fun movement_only_result_preserves_the_full_authoritative_count() {
        val identity = DailyRecipes.FOUR_PAIRS_LOW_V1.identityFor(
            LocalDate.of(2026, 7, 25)
        )
        val locale = Locale.US

        val text = DailyCompletionShareTextFormatter().format(
            completion = completion(
                identity = identity,
                elapsedMilliseconds = null,
                movementCount = Long.MAX_VALUE
            ),
            copy = englishCopy(),
            locale = locale
        )

        assertEquals(
            "NumPairs Daily · ${localizedDate(identity, locale)}\n" +
                "4 Pairs · Low · Completed in ${Long.MAX_VALUE} moves",
            text.value
        )
    }

    @Test
    fun legacy_completion_without_duration_keeps_the_existing_share_copy() {
        val identity = DailyRecipes.FOUR_PAIRS_LOW_V1.identityFor(
            LocalDate.of(2026, 7, 25)
        )
        val locale = Locale.US

        val text = DailyCompletionShareTextFormatter().format(
            completion = completion(identity, elapsedMilliseconds = null),
            copy = englishCopy(),
            locale = locale
        )
        val expectedDate = localizedDate(identity, locale)

        assertEquals(
            "NumPairs Daily · $expectedDate\n4 Pairs · Low · Completed",
            text.value
        )
    }

    @Test
    fun unknown_recipe_completion_cannot_be_shared() {
        val identity = DailyChallengeId(
            localDate = LocalDate.of(2026, 7, 25),
            recipeVersion = DailyRecipeVersion("unknown-daily-recipe")
        )

        assertThrows(IllegalArgumentException::class.java) {
            DailyCompletionShareTextFormatter().format(
                completion = completion(identity, elapsedMilliseconds = 65_432, movementCount = 23),
                copy = englishCopy(),
                locale = Locale.US
            )
        }
    }

    @Test
    fun localized_copy_requires_every_visible_field() {
        assertThrows(IllegalArgumentException::class.java) {
            englishCopy(dailyName = " ")
        }
        assertThrows(IllegalArgumentException::class.java) {
            DailyChallengeNameCopy(
                namesByChallengeId = mapOf(GeneratedModes.FOUR_PAIRS_LOW.id to "")
            )
        }
        assertThrows(IllegalArgumentException::class.java) {
            englishCopy(completedStatus = "")
        }
        assertThrows(IllegalArgumentException::class.java) {
            englishCopy(completedResultStatusFormat = " ")
        }
        assertThrows(IllegalArgumentException::class.java) {
            englishCopy(movementSingularFormat = "")
        }
        assertThrows(IllegalArgumentException::class.java) {
            englishCopy(movementPluralFormat = " ")
        }
    }
}

private fun completion(
    identity: DailyChallengeId,
    elapsedMilliseconds: Long?,
    movementCount: Long? = null
): DailyCompletion = DailyCompletion(
    identity = identity,
    elapsedTime = elapsedMilliseconds?.let(::DailyElapsedTime),
    movementCount = movementCount?.let(::DailyMovementCount)
)

private fun englishCopy(
    dailyName: String = "NumPairs Daily",
    challengeNames: DailyChallengeNameCopy = challengeNames(),
    completedStatus: String = "Completed",
    completedResultStatusFormat: String = "Completed in %1\$s",
    movementSingularFormat: String = "%1\$s move",
    movementPluralFormat: String = "%1\$s moves"
): DailyCompletionShareCopy = DailyCompletionShareCopy(
    dailyName = dailyName,
    challengeNames = challengeNames,
    completedStatus = completedStatus,
    completedResultStatusFormat = completedResultStatusFormat,
    movementSingularFormat = movementSingularFormat,
    movementPluralFormat = movementPluralFormat
)

private data class LocalizedMovementScenario(
    val locale: Locale,
    val completedResultStatusFormat: String,
    val movementSingularFormat: String,
    val movementPluralFormat: String,
    val expectedSingularStatus: String,
    val expectedPluralStatus: String
)

private fun localizedMovementCopy(scenario: LocalizedMovementScenario): DailyCompletionShareCopy =
    DailyCompletionShareCopy(
        dailyName = "Daily",
        challengeNames = DailyChallengeNameCopy(
            namesByChallengeId = mapOf(GeneratedModes.FOUR_PAIRS_LOW.id to "Challenge")
        ),
        completedStatus = "Completed",
        completedResultStatusFormat = scenario.completedResultStatusFormat,
        movementSingularFormat = scenario.movementSingularFormat,
        movementPluralFormat = scenario.movementPluralFormat
    )

private fun localizedDate(identity: DailyChallengeId, locale: Locale): String = identity.localDate.format(
    DateTimeFormatter.ofLocalizedDate(FormatStyle.MEDIUM).withLocale(locale)
)

private fun challengeNames(
    threePairsLow: String = "3 Pairs · Low",
    fourPairsLow: String = "4 Pairs · Low",
    threePairsMedium: String = "3 Pairs · Medium",
    fourPairsMedium: String = "4 Pairs · Medium",
    eightPairsMedium: String = "8 Pairs · Medium"
): DailyChallengeNameCopy = DailyChallengeNameCopy(
    namesByChallengeId = mapOf(
        GeneratedModes.THREE_PAIRS_LOW.id to threePairsLow,
        GeneratedModes.FOUR_PAIRS_LOW.id to fourPairsLow,
        GeneratedModes.THREE_PAIRS_MEDIUM.id to threePairsMedium,
        GeneratedModes.FOUR_PAIRS_MEDIUM.id to fourPairsMedium,
        GeneratedModes.EIGHT_PAIRS_MEDIUM.id to eightPairsMedium
    )
)
