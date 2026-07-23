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
            objective = "Bienvenido a NumPairs. Tu objetivo es descubrir todos los números y " +
                "completar todas las sumas y multiplicaciones ocultas.",
            strip = "La secuencia de la parte superior del puzle es una lista de números ordenados " +
                "de menor a mayor. Pueden repetirse y, al principio, algunos están ocultos.",
            tile = "Las casillas están debajo. Cada una tiene dos partes: abajo aparece un resultado " +
                "conocido y arriba hay una suma o multiplicación cuyos números y signo debes deducir.",
            pair = "¿Cómo se deducen? Forma parejas con los números de la secuencia superior. " +
                "Cada pareja completa dos casillas: una suma y una multiplicación.",
            previous = "Atrás",
            next = "Siguiente"
        )
        assertTutorialCopy(
            resources = resources,
            expectedCopy = listOf(
                "Presta atención: esta será la primera y última vez que resolveremos un puzle por ti.",
                "4 × 5 = 20, así que 20 es la casilla de multiplicación de esta pareja.",
                "La misma pareja también resuelve la suma: 4 + 5 = 9.",
                "Quedan por completar las casillas con resultados 5 y 6. Junto al 2, " +
                    "el número oculto solo puede ser 3.",
                "2 × 3 = 6 completa la multiplicación que falta.",
                "2 + 3 = 5 completa la suma y resuelve el puzle.",
                "Ahora tú: resuelve el puzle. Toca cualquier ? para empezar. " +
                    "Los números pueden repetirse. Consejo: las multiplicaciones suelen ser " +
                    "un buen punto de partida."
            )
        )
        assertEquals("Saltar tutorial", resources.getString(R.string.onboarding_skip_tutorial_action))
        assertEquals(
            "Si es la primera vez que juegas a NumPairs, te recomendamos continuar.",
            resources.getString(R.string.onboarding_skip_tutorial_message)
        )
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
            objective = "Benvingut a NumPairs. L’objectiu és descobrir tots els nombres i completar " +
                "totes les sumes i multiplicacions ocultes.",
            strip = "La seqüència de la part superior del puzle és una llista de nombres ordenats " +
                "de menor a major. Es poden repetir i, al principi, alguns estan ocults.",
            tile = "Davall hi ha les caselles. Cada una té dues parts: baix apareix un resultat " +
                "conegut i dalt hi ha una suma o multiplicació amb els nombres i el signe que has " +
                "de deduir.",
            pair = "Com els pots deduir? Forma parelles amb els nombres de la seqüència superior. " +
                "Cada parella completa dues caselles: una suma i una multiplicació.",
            previous = "Enrere",
            next = "Següent"
        )
        assertTutorialCopy(
            resources = resources,
            expectedCopy = listOf(
                "Para atenció: esta serà la primera i última vegada que resoldrem un puzle per tu.",
                "4 × 5 = 20, així que 20 és la casella de multiplicació d’esta parella.",
                "La mateixa parella també resol la suma: 4 + 5 = 9.",
                "Queden per completar les caselles amb els resultats 5 i 6. Junt amb el 2, " +
                    "el nombre ocult només pot ser 3.",
                "2 × 3 = 6 completa la multiplicació que falta.",
                "2 + 3 = 5 completa la suma i resol el puzle.",
                "Ara tu: resol el puzle. Toca qualsevol ? per començar. Els nombres es poden repetir. " +
                    "Consell: les multiplicacions solen ser un bon punt de partida."
            )
        )
        assertEquals("Omet el tutorial", resources.getString(R.string.onboarding_skip_tutorial_action))
        assertEquals(
            "Si és la primera vegada que jugues a NumPairs, et recomanem continuar.",
            resources.getString(R.string.onboarding_skip_tutorial_message)
        )
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
            objective = "Welcome to NumPairs. Your goal is to find every number and complete all " +
                "the hidden additions and multiplications.",
            strip = "The sequence at the top of the puzzle is a list of numbers ordered from lowest " +
                "to highest. Numbers may repeat, and some are hidden at first.",
            tile = "The tiles are below. Each has two parts: a known result at the bottom and an " +
                "addition or multiplication at the top whose numbers and symbol you must work out.",
            pair = "How do you work them out? Pair up the numbers in the sequence above. Each pair " +
                "completes two tiles: one addition and one multiplication.",
            previous = "Back",
            next = "Next"
        )
        assertTutorialCopy(
            resources = resources,
            expectedCopy = listOf(
                "Watch closely: this is the first and last time we’ll solve a puzzle for you.",
                "4 × 5 = 20, so 20 is this pair’s multiplication tile.",
                "The same pair also completes the addition: 4 + 5 = 9.",
                "The tiles showing 5 and 6 are still incomplete. Paired with 2, " +
                    "the hidden number can only be 3.",
                "2 × 3 = 6 completes the remaining multiplication.",
                "2 + 3 = 5 completes the addition and solves the puzzle.",
                "Your turn: solve the puzzle. Tap any ? to begin. Numbers may repeat. " +
                    "Tip: multiplication is often a good place to start."
            )
        )
        assertEquals("Skip tutorial", resources.getString(R.string.onboarding_skip_tutorial_action))
        assertEquals(
            "If this is your first time playing NumPairs, we recommend continuing.",
            resources.getString(R.string.onboarding_skip_tutorial_message)
        )
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
