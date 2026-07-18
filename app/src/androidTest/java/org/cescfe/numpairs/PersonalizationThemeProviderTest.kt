package org.cescfe.numpairs

import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.test.junit4.v2.createComposeRule
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import org.cescfe.numpairs.data.preferences.PersonalizationPreferences
import org.cescfe.numpairs.data.preferences.PersonalizationPreferencesRepository
import org.cescfe.numpairs.data.preferences.PersonalizationTheme
import org.junit.Assert.assertEquals
import org.junit.Assert.assertSame
import org.junit.Rule
import org.junit.Test

class PersonalizationThemeProviderTest {
    @get:Rule
    val composeTestRule = createComposeRule()

    @Test
    fun changingPreferenceUpdatesThemeWithoutForgettingChildState() {
        val repository = FakePersonalizationPreferencesRepository()
        var firstRememberedValue: Any? = null
        var latestRememberedValue: Any? = null
        var latestPrimary = Color.Unspecified

        composeTestRule.setContent {
            PersonalizationThemeProvider(repository) {
                val rememberedValue = remember { Any() }
                val primary = MaterialTheme.colorScheme.primary
                SideEffect {
                    firstRememberedValue = firstRememberedValue ?: rememberedValue
                    latestRememberedValue = rememberedValue
                    latestPrimary = primary
                }
            }
        }

        composeTestRule.runOnIdle {
            assertEquals(Color(0xFF9CBD7B), latestPrimary)
            repository.state.value = PersonalizationPreferences(
                selectedTheme = PersonalizationTheme.FROST
            )
        }

        composeTestRule.runOnIdle {
            assertEquals(Color(0xFF215EA8), latestPrimary)
            assertSame(firstRememberedValue, latestRememberedValue)
        }
    }

    private class FakePersonalizationPreferencesRepository : PersonalizationPreferencesRepository {
        val state = MutableStateFlow(PersonalizationPreferences())

        override val preferences: Flow<PersonalizationPreferences> = state

        override suspend fun selectTheme(theme: PersonalizationTheme) {
            state.value = state.value.copy(selectedTheme = theme)
        }

        override suspend fun setGeneratedGameHapticsEnabled(enabled: Boolean) {
            state.value = state.value.copy(generatedGameHapticsEnabled = enabled)
        }
    }
}
