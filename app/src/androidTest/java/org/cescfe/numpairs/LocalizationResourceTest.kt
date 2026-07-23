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
class LocalizationResourceTest {
    @Test
    fun spanishDeviceLanguageUsesSpanishResources() {
        val resources = resourcesFor(languageTag = "es")

        assertEquals("Jugar 4 pares", resources.getString(R.string.menu_four_pairs_button))
        assertEquals("Jugar 8 pares", resources.getString(R.string.menu_eight_pairs_button))
        assertEquals("Cómo jugar", resources.getString(R.string.menu_tutorial_button))
        assertEquals("Personalización", resources.getString(R.string.menu_personalization_button))
        assertEquals("8 pares", resources.getString(R.string.eight_pairs_screen_title))
        assertEquals("Alta", resources.getString(R.string.generated_difficulty_hard))
        assertEquals(
            "8 pares · Alta",
            resources.getString(R.string.generated_challenge_title, "8 pares", "Alta")
        )
        assertEquals("Serie", resources.getString(R.string.strip_content_description))
        assertEquals("Paso 1 de 2", resources.getString(R.string.tutorial_step_indicator, 1, 2))
        assertTutorialExplanationCopy(
            resources = resources,
            objective = "Bienvenido a NumPairs. Tu objetivo es descubrir todos los números y símbolos ocultos.",
            strip = "Esta es la serie. Sus números están ordenados de menor a mayor, " +
                "pueden repetirse y algunos están ocultos.",
            tile = "Cada casilla muestra un resultado. Completa la parte superior con los dos números " +
                "y el signo que producen ese resultado.",
            pair = "Agrupa los números de la serie por parejas. Cada pareja completa dos casillas: " +
                "una suma y una multiplicación.",
            previous = "Atrás",
            next = "Siguiente"
        )
        assertTutorialCopy(
            resources = resources,
            expectedCopy = listOf(
                "Presta atención: vamos a resolver este puzle por ti una sola vez.",
                "4 × 5 = 20, así que 20 es la casilla de multiplicación de esta pareja.",
                "La misma pareja también da 4 + 5 = 9.",
                "Quedan los resultados 5 y 6. Junto al 2, el número oculto tiene que ser 3.",
                "2 × 3 = 6 completa la multiplicación que falta.",
                "2 + 3 = 5 completa el puzle.",
                "Ahora tú: resuelve el puzle. Toca cualquier ? para empezar. " +
                    "Los números pueden repetirse. Consejo: las multiplicaciones suelen ser " +
                    "un buen punto de partida."
            )
        )
        assertEquals("Saltar tutorial", resources.getString(R.string.onboarding_skip_tutorial_action))
        assertEquals("Continuar tutorial", resources.getString(R.string.onboarding_continue_tutorial_button))
        assertEquals("Saltar igualmente", resources.getString(R.string.onboarding_skip_anyway_button))
        assertEquals("No se puede abrir NumPairs", resources.getString(R.string.onboarding_startup_failure_title))
        assertEquals("Reintentar", resources.getString(R.string.onboarding_startup_retry_button))
    }

    @Test
    fun catalanDeviceLanguageUsesValencianResources() {
        val resources = resourcesFor(languageTag = "ca")

        assertEquals("Juga a 4 parelles", resources.getString(R.string.menu_four_pairs_button))
        assertEquals("Juga a 8 parelles", resources.getString(R.string.menu_eight_pairs_button))
        assertEquals("Com jugar", resources.getString(R.string.menu_tutorial_button))
        assertEquals("Personalització", resources.getString(R.string.menu_personalization_button))
        assertEquals("8 parelles", resources.getString(R.string.eight_pairs_screen_title))
        assertEquals("Alta", resources.getString(R.string.generated_difficulty_hard))
        assertEquals(
            "8 parelles · Alta",
            resources.getString(R.string.generated_challenge_title, "8 parelles", "Alta")
        )
        assertEquals("Sèrie", resources.getString(R.string.strip_content_description))
        assertEquals("Pas 1 de 2", resources.getString(R.string.tutorial_step_indicator, 1, 2))
        assertTutorialExplanationCopy(
            resources = resources,
            objective = "Benvingut a NumPairs. L’objectiu és descobrir tots els nombres, sumes i multiplicacions ocultes.",
            strip = "Aquesta és la sèrie. Els nombres estan ordenats de menor a major, " +
                "es poden repetir i alguns estan ocults.",
            tile = "Cada casella mostra un resultat. Completa la part superior amb els dos nombres " +
                "i el signe que produeixen aquest resultat.",
            pair = "Agrupa els nombres de la sèrie per parelles. Cada parella completa dues caselles: " +
                "una suma i una multiplicació.",
            previous = "Enrere",
            next = "Següent"
        )
        assertTutorialCopy(
            resources = resources,
            expectedCopy = listOf(
                "Para atenció: resoldrem este puzle per tu una sola vegada.",
                "4 × 5 = 20, així que 20 és la casella de multiplicació d’esta parella.",
                "La mateixa parella també dona 4 + 5 = 9.",
                "Queden els resultats 5 i 6. Junt amb el 2, el nombre ocult ha de ser 3.",
                "2 × 3 = 6 completa la multiplicació que falta.",
                "2 + 3 = 5 completa el puzle.",
                "Ara tu: resol el puzle. Toca qualsevol ? per començar. Els nombres es poden repetir. " +
                    "Consell: les multiplicacions solen ser un bon punt de partida."
            )
        )
        assertEquals("Omet el tutorial", resources.getString(R.string.onboarding_skip_tutorial_action))
        assertEquals("Continua el tutorial", resources.getString(R.string.onboarding_continue_tutorial_button))
        assertEquals("Omet-lo igualment", resources.getString(R.string.onboarding_skip_anyway_button))
        assertEquals("No es pot obrir NumPairs", resources.getString(R.string.onboarding_startup_failure_title))
        assertEquals("Torna-ho a provar", resources.getString(R.string.onboarding_startup_retry_button))
    }

    @Test
    fun unsupportedDeviceLanguageFallsBackToEnglishResources() {
        val resources = resourcesFor(languageTag = "de")

        assertEquals("Play 4 pairs", resources.getString(R.string.menu_four_pairs_button))
        assertEquals("Play 8 pairs", resources.getString(R.string.menu_eight_pairs_button))
        assertEquals("How to play", resources.getString(R.string.menu_tutorial_button))
        assertEquals("Personalization", resources.getString(R.string.menu_personalization_button))
        assertEquals("8 pairs", resources.getString(R.string.eight_pairs_screen_title))
        assertEquals("Hard", resources.getString(R.string.generated_difficulty_hard))
        assertEquals(
            "8 pairs · Hard",
            resources.getString(R.string.generated_challenge_title, "8 pairs", "Hard")
        )
        assertEquals("Strip", resources.getString(R.string.strip_content_description))
        assertEquals("Step 1 of 2", resources.getString(R.string.tutorial_step_indicator, 1, 2))
        assertTutorialExplanationCopy(
            resources = resources,
            objective = "Welcome to NumPairs. Your goal is to discover every hidden number, addition and multiplication.",
            strip = "This is the number strip. Its numbers are ordered from lowest to highest, " +
                "may repeat, and some are hidden.",
            tile = "Each tile shows a result. Complete its top row with the two numbers and symbol " +
                "that produce that result.",
            pair = "Group the strip numbers into pairs. Each pair completes two tiles: " +
                "one addition and one multiplication.",
            previous = "Back",
            next = "Next"
        )
        assertTutorialCopy(
            resources = resources,
            expectedCopy = listOf(
                "Watch closely: we’ll solve this puzzle for you once.",
                "4 × 5 = 20, so 20 is this pair’s multiplication tile.",
                "The same pair also gives 4 + 5 = 9.",
                "The remaining results are 5 and 6. Paired with 2, the hidden number must be 3.",
                "2 × 3 = 6 completes the remaining multiplication.",
                "2 + 3 = 5 completes the puzzle.",
                "Your turn: solve the puzzle. Tap any ? to begin. Numbers may repeat. " +
                    "Tip: multiplication is often a good place to start."
            )
        )
        assertEquals("Skip tutorial", resources.getString(R.string.onboarding_skip_tutorial_action))
        assertEquals("Continue tutorial", resources.getString(R.string.onboarding_continue_tutorial_button))
        assertEquals("Skip anyway", resources.getString(R.string.onboarding_skip_anyway_button))
        assertEquals("Unable to open NumPairs", resources.getString(R.string.onboarding_startup_failure_title))
        assertEquals("Retry", resources.getString(R.string.onboarding_startup_retry_button))
    }

    private fun assertTutorialCopy(resources: Resources, expectedCopy: List<String>) {
        val copyResIds = listOf(
            R.string.tutorial_worked_example_introduction_copy,
            R.string.tutorial_worked_example_product_four_five_copy,
            R.string.tutorial_worked_example_sum_four_five_copy,
            R.string.tutorial_worked_example_reveal_three_copy,
            R.string.tutorial_worked_example_product_two_three_copy,
            R.string.tutorial_worked_example_sum_two_three_copy,
            R.string.tutorial_repeated_value_practice_copy
        )

        assertEquals(expectedCopy, copyResIds.map(resources::getString))
    }

    private fun assertTutorialExplanationCopy(
        resources: Resources,
        objective: String,
        strip: String,
        tile: String,
        pair: String,
        previous: String,
        next: String
    ) {
        assertEquals(objective, resources.getString(R.string.tutorial_objective_explanation_copy))
        assertEquals(strip, resources.getString(R.string.tutorial_strip_explanation_copy))
        assertEquals(tile, resources.getString(R.string.tutorial_tile_explanation_copy))
        assertEquals(pair, resources.getString(R.string.tutorial_pair_explanation_copy))
        assertEquals(previous, resources.getString(R.string.tutorial_previous_step_action))
        assertEquals(next, resources.getString(R.string.tutorial_next_step_action))
    }

    private fun resourcesFor(languageTag: String): Resources {
        val targetContext = InstrumentationRegistry.getInstrumentation().targetContext
        val configuration = targetContext.resources.configuration
        val localizedConfiguration = android.content.res.Configuration(configuration).apply {
            setLocales(LocaleList(Locale.forLanguageTag(languageTag)))
        }

        return targetContext.createConfigurationContext(localizedConfiguration).resources
    }
}
