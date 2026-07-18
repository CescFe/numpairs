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
        assertEquals("Ya sé jugar", resources.getString(R.string.onboarding_early_validation_button))
        assertEquals("Comprobación final", resources.getString(R.string.final_validation_screen_title))
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
        assertEquals("Ja sé jugar", resources.getString(R.string.onboarding_early_validation_button))
        assertEquals("Comprovació final", resources.getString(R.string.final_validation_screen_title))
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
        assertEquals("I know how to play", resources.getString(R.string.onboarding_early_validation_button))
        assertEquals("Final check", resources.getString(R.string.final_validation_screen_title))
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
