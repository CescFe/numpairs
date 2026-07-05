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
        assertEquals("Serie", resources.getString(R.string.strip_content_description))
        assertEquals("Paso 1 de 2", resources.getString(R.string.tutorial_step_indicator, 1, 2))
    }

    @Test
    fun catalanDeviceLanguageUsesValencianResources() {
        val resources = resourcesFor(languageTag = "ca")

        assertEquals("Juga a 4 parelles", resources.getString(R.string.menu_four_pairs_button))
        assertEquals("Sèrie", resources.getString(R.string.strip_content_description))
        assertEquals("Pas 1 de 2", resources.getString(R.string.tutorial_step_indicator, 1, 2))
    }

    @Test
    fun unsupportedDeviceLanguageFallsBackToEnglishResources() {
        val resources = resourcesFor(languageTag = "de")

        assertEquals("Play 4 pairs", resources.getString(R.string.menu_four_pairs_button))
        assertEquals("Strip", resources.getString(R.string.strip_content_description))
        assertEquals("Step 1 of 2", resources.getString(R.string.tutorial_step_indicator, 1, 2))
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
