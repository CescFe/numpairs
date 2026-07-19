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
        assertTutorialCopy(
            resources = resources,
            stepOne = "La lista de números va de menor a mayor. " +
                "Un mismo número puede aparecer más de una vez.",
            stepOneGuidance = "Introduce 3 para completar la serie del tutorial.",
            stepTwo = "Cada casilla muestra un resultado. " +
                "Elige los números y el símbolo que dan ese resultado. " +
                "Usa los mismos dos números una vez con + y otra con ×.",
            stepThree = "Resuelve el puzle. Recuerda: la lista de números puede incluir " +
                "el mismo número más de una vez."
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
        assertTutorialCopy(
            resources = resources,
            stepOne = "La llista de números va de menor a major. " +
                "Un mateix número pot aparéixer més d’una vegada.",
            stepOneGuidance = "Introdueix 3 per completar la sèrie del tutorial.",
            stepTwo = "Cada casella mostra un resultat. " +
                "Tria els números i el símbol que donen eixe resultat. " +
                "Usa els mateixos dos números una vegada amb + i una altra amb ×.",
            stepThree = "Resol el puzle. Recorda: la llista de números pot incloure " +
                "el mateix número més d’una vegada."
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
        assertTutorialCopy(
            resources = resources,
            stepOne = "The number list goes from smallest to largest. " +
                "The same number can appear more than once.",
            stepOneGuidance = "Enter 3 to complete the Tutorial strip.",
            stepTwo = "Each tile shows a result. Choose the numbers and symbol that make it. " +
                "Use the same two numbers once with + and once with ×.",
            stepThree = "Solve the puzzle. Remember: the number list can include " +
                "the same number more than once."
        )
        assertEquals("Skip tutorial", resources.getString(R.string.onboarding_skip_tutorial_action))
        assertEquals("Continue tutorial", resources.getString(R.string.onboarding_continue_tutorial_button))
        assertEquals("Skip anyway", resources.getString(R.string.onboarding_skip_anyway_button))
        assertEquals("Unable to open NumPairs", resources.getString(R.string.onboarding_startup_failure_title))
        assertEquals("Retry", resources.getString(R.string.onboarding_startup_retry_button))
    }

    private fun assertTutorialCopy(
        resources: Resources,
        stepOne: String,
        stepOneGuidance: String,
        stepTwo: String,
        stepThree: String
    ) {
        assertEquals(stepOne, resources.getString(R.string.tutorial_strip_introduction_copy))
        assertEquals(stepOneGuidance, resources.getString(R.string.tutorial_strip_entry_guidance))
        assertEquals(stepTwo, resources.getString(R.string.tutorial_tiles_introduction_copy))
        assertEquals(stepThree, resources.getString(R.string.tutorial_repeated_value_practice_copy))
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
