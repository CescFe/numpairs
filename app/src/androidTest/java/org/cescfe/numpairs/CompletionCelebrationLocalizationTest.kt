package org.cescfe.numpairs

import android.content.res.Resources
import android.os.LocaleList
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import java.util.Locale
import org.junit.Assert.assertEquals
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class CompletionCelebrationLocalizationTest {
    @Test
    fun catalog_and_accessible_check_are_localized_in_every_supported_locale() {
        EXPECTED_CATALOGS.forEach { expected ->
            val resources = resourcesFor(expected.languageTag)

            assertEquals(
                expected.checkDescription,
                resources.getString(R.string.success_overlay_badge_content_description)
            )
            assertEquals(expected.copy, CELEBRATION_RESOURCE_PAIRS.map { pair -> pair.localizedWith(resources) })
        }
    }

    @Test
    fun standalone_spanish_daily_completion_uses_feminine_agreement() {
        val resources = resourcesFor("es")

        assertEquals("Daily completada", resources.getString(R.string.daily_completion_screen_title))
        assertEquals("¡Daily completada!", resources.getString(R.string.daily_completion_message))
    }

    private fun ResourcePair.localizedWith(resources: Resources): Pair<String, String> =
        resources.getString(title) to resources.getString(supportingText)

    private fun resourcesFor(languageTag: String): Resources {
        val targetContext = InstrumentationRegistry.getInstrumentation().targetContext
        val localizedConfiguration = android.content.res.Configuration(targetContext.resources.configuration).apply {
            setLocales(LocaleList(Locale.forLanguageTag(languageTag)))
        }
        return targetContext.createConfigurationContext(localizedConfiguration).resources
    }

    private data class ResourcePair(val title: Int, val supportingText: Int)

    private data class ExpectedCatalog(
        val languageTag: String,
        val checkDescription: String,
        val copy: List<Pair<String, String>>
    )

    private companion object {
        val CELEBRATION_RESOURCE_PAIRS = listOf(
            ResourcePair(
                R.string.completion_celebration_great_work_title,
                R.string.completion_celebration_great_work_supporting_text
            ),
            ResourcePair(
                R.string.completion_celebration_excellent_title,
                R.string.completion_celebration_excellent_supporting_text
            ),
            ResourcePair(
                R.string.completion_celebration_you_rock_title,
                R.string.completion_celebration_you_rock_supporting_text
            ),
            ResourcePair(
                R.string.completion_celebration_nailed_it_title,
                R.string.completion_celebration_nailed_it_supporting_text
            ),
            ResourcePair(
                R.string.completion_celebration_brilliant_title,
                R.string.completion_celebration_brilliant_supporting_text
            ),
            ResourcePair(
                R.string.completion_celebration_keep_it_up_title,
                R.string.completion_celebration_keep_it_up_supporting_text
            ),
            ResourcePair(
                R.string.completion_celebration_correction_free_title,
                R.string.completion_celebration_correction_free_supporting_text
            ),
            ResourcePair(
                R.string.completion_celebration_impressive_title,
                R.string.completion_celebration_impressive_supporting_text
            ),
            ResourcePair(
                R.string.completion_celebration_unstoppable_title,
                R.string.completion_celebration_unstoppable_supporting_text
            )
        )
        val EXPECTED_CATALOGS = listOf(
            ExpectedCatalog(
                languageTag = "en",
                checkDescription = "Puzzle completed",
                copy = listOf(
                    "Great work!" to "Your logic paid off.",
                    "Excellent!" to "You found the complete solution.",
                    "You rock!" to "That was an impressive solve.",
                    "You nailed it!" to "The whole puzzle checks out.",
                    "Brilliant!" to "Your solution fits perfectly.",
                    "Keep it up!" to "Ready for a tougher challenge?",
                    "Flawless!" to "You solved it without correcting a single move.",
                    "Impressive!" to "That took some serious thinking.",
                    "Nothing can stop you!" to "And I thought this difficulty was impossible!"
                )
            ),
            ExpectedCatalog(
                languageTag = "es",
                checkDescription = "Puzle completado",
                copy = listOf(
                    "¡Buen trabajo!" to "Tu lógica ha dado sus frutos.",
                    "¡Excelente!" to "Has dado con la solución completa.",
                    "¡Eres increíble!" to "Menuda forma de resolverlo.",
                    "¡Lo has clavado!" to "Todo el puzle encaja.",
                    "¡Brillante!" to "Tu solución encaja a la perfección.",
                    "¡Sigue así!" to "¿Te atreves a subir la dificultad?",
                    "¡Impecable!" to "Lo resolviste sin corregir una sola jugada.",
                    "¡Im-presionante!" to "En dos palabras.",
                    "¡No hay quien te pare!" to "¡Y yo que pensaba que esta dificultad era imposible!"
                )
            ),
            ExpectedCatalog(
                languageTag = "ca",
                checkDescription = "Puzle completat",
                copy = listOf(
                    "Molt bon treball!" to "La teua lògica ha donat resultat.",
                    "Excel·lent!" to "Has trobat la solució completa.",
                    "Eres increïble!" to "Quina manera de resoldre’l.",
                    "Ho has clavat!" to "Tot el puzle encaixa.",
                    "Brillant!" to "La teua solució encaixa a la perfecció.",
                    "Continua així!" to "T’atrevixes a pujar la dificultat?",
                    "Impecable!" to "L’has resolt sense corregir ni una sola jugada.",
                    "Impressionant!" to "Això sí que ha requerit pensar.",
                    "No hi ha qui et pare!" to "I jo que pensava que esta dificultat era impossible!"
                )
            ),
            ExpectedCatalog(
                languageTag = "de",
                checkDescription = "Puzzle abgeschlossen",
                copy = listOf(
                    "Gut gemacht!" to "Deine Logik hat sich ausgezahlt.",
                    "Hervorragend!" to "Du hast die vollständige Lösung gefunden.",
                    "Du bist spitze!" to "Das war wirklich stark gelöst.",
                    "Volltreffer!" to "Das ganze Puzzle geht auf.",
                    "Großartig!" to "Deine Lösung passt perfekt.",
                    "Weiter so!" to "Bereit für einen höheren Schwierigkeitsgrad?",
                    "Makellos!" to "Du hast es gelöst, ohne einen einzigen Zug zu korrigieren.",
                    "Beeindruckend!" to "Dafür war echtes Köpfchen gefragt.",
                    "Dich hält nichts auf!" to "Und ich dachte schon, das wäre unlösbar!"
                )
            )
        )
    }
}
