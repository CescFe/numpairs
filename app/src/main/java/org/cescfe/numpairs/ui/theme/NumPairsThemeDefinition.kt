package org.cescfe.numpairs.ui.theme

import androidx.compose.material3.ColorScheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Immutable
import androidx.compose.ui.graphics.Color
import org.cescfe.numpairs.data.preferences.PersonalizationTheme

@Immutable
internal data class NumPairsSemanticColors(
    val success: Color,
    val onSuccess: Color,
    val successContainer: Color,
    val onSuccessContainer: Color,
    val error: Color,
    val onError: Color,
    val errorContainer: Color,
    val onErrorContainer: Color,
    val selection: Color,
    val onSelection: Color,
    val selectionContainer: Color,
    val onSelectionContainer: Color,
    val tutorialHighlight: Color,
    val onTutorialHighlight: Color,
    val hiddenContainer: Color,
    val onHiddenContainer: Color,
    val hiddenBorder: Color
)

@Immutable
internal data class NumPairsThemeDefinition(
    val id: PersonalizationTheme,
    val appearanceColors: ColorScheme,
    val semanticColors: NumPairsSemanticColors
)

internal val WarmThemeDefinition = NumPairsThemeDefinition(
    id = PersonalizationTheme.WARM,
    appearanceColors = darkColorScheme(
        primary = NumPairsGreen,
        onPrimary = NumPairsOnGreen,
        primaryContainer = NumPairsGreenSoft,
        onPrimaryContainer = NumPairsOnGreenSoft,
        secondary = NumPairsFocus,
        onSecondary = NumPairsOnFocus,
        secondaryContainer = NumPairsSurfaceSubtle,
        onSecondaryContainer = NumPairsOnSurface,
        tertiary = NumPairsSand,
        onTertiary = NumPairsOnSand,
        tertiaryContainer = NumPairsSurfaceSubtle,
        onTertiaryContainer = NumPairsOnSurface,
        background = NumPairsBackground,
        onBackground = NumPairsOnSurface,
        surface = NumPairsSurface,
        onSurface = NumPairsOnSurface,
        surfaceVariant = NumPairsSurfaceSubtle,
        onSurfaceVariant = NumPairsOnSurfaceVariant,
        surfaceContainer = NumPairsSurface,
        surfaceContainerHigh = NumPairsSurfaceRaised,
        surfaceContainerHighest = NumPairsSurfaceRaised,
        outline = NumPairsOutline,
        outlineVariant = NumPairsOutlineVariant,
        error = NumPairsError,
        onError = NumPairsBackground,
        errorContainer = NumPairsErrorSoft,
        onErrorContainer = NumPairsOnErrorSoft,
        scrim = NumPairsScrim
    ),
    semanticColors = NumPairsSemanticColors(
        success = NumPairsGreen,
        onSuccess = NumPairsOnGreen,
        successContainer = NumPairsGreenSoft,
        onSuccessContainer = NumPairsOnGreenSoft,
        error = NumPairsError,
        onError = NumPairsBackground,
        errorContainer = NumPairsErrorSoft,
        onErrorContainer = NumPairsOnErrorSoft,
        selection = NumPairsFocus,
        onSelection = NumPairsOnFocus,
        selectionContainer = NumPairsSurfaceSubtle,
        onSelectionContainer = NumPairsOnSurface,
        tutorialHighlight = NumPairsSand,
        onTutorialHighlight = NumPairsOnSand,
        hiddenContainer = NumPairsSurfaceSubtle,
        onHiddenContainer = NumPairsOnSurfaceVariant,
        hiddenBorder = NumPairsOnSurfaceVariant
    )
)

internal val FrostThemeDefinition = NumPairsThemeDefinition(
    id = PersonalizationTheme.FROST,
    appearanceColors = lightColorScheme(
        primary = Color(0xFF215EA8),
        onPrimary = Color(0xFFFFFFFF),
        primaryContainer = Color(0xFFD5E5FF),
        onPrimaryContainer = Color(0xFF0A2C55),
        secondary = Color(0xFF745B14),
        onSecondary = Color(0xFFFFFFFF),
        secondaryContainer = Color(0xFFF7E5AD),
        onSecondaryContainer = Color(0xFF2B2100),
        tertiary = Color(0xFF65558F),
        onTertiary = Color(0xFFFFFFFF),
        tertiaryContainer = Color(0xFFE9DDFF),
        onTertiaryContainer = Color(0xFF251A3F),
        background = Color(0xFFF4F8FD),
        onBackground = Color(0xFF17202C),
        surface = Color(0xFFFFFFFF),
        onSurface = Color(0xFF17202C),
        surfaceVariant = Color(0xFFEDF3FA),
        onSurfaceVariant = Color(0xFF435466),
        surfaceContainer = Color(0xFFF4F8FD),
        surfaceContainerHigh = Color(0xFFE6EEF8),
        surfaceContainerHighest = Color(0xFFDCE7F3),
        outline = Color(0xFF65788B),
        outlineVariant = Color(0xFFB7C5D3),
        error = Color(0xFFB3261E),
        onError = Color(0xFFFFFFFF),
        errorContainer = Color(0xFFF9DEDC),
        onErrorContainer = Color(0xFF410E0B),
        scrim = Color(0x99081728)
    ),
    semanticColors = NumPairsSemanticColors(
        success = Color(0xFF2E6B3F),
        onSuccess = Color(0xFFFFFFFF),
        successContainer = Color(0xFFD8F0DE),
        onSuccessContainer = Color(0xFF153A20),
        error = Color(0xFFB3261E),
        onError = Color(0xFFFFFFFF),
        errorContainer = Color(0xFFF9DEDC),
        onErrorContainer = Color(0xFF410E0B),
        selection = Color(0xFF65558F),
        onSelection = Color(0xFFFFFFFF),
        selectionContainer = Color(0xFFE9DDFF),
        onSelectionContainer = Color(0xFF251A3F),
        tutorialHighlight = Color(0xFF8A4F00),
        onTutorialHighlight = Color(0xFFFFFFFF),
        hiddenContainer = Color(0xFFE3E9F0),
        onHiddenContainer = Color(0xFF3F4B58),
        hiddenBorder = Color(0xFF586777)
    )
)

internal val ObsidianThemeDefinition = NumPairsThemeDefinition(
    id = PersonalizationTheme.OBSIDIAN,
    appearanceColors = darkColorScheme(
        primary = Color(0xFFD7A66A),
        onPrimary = Color(0xFF38230B),
        primaryContainer = Color(0xFF4E3822),
        onPrimaryContainer = Color(0xFFFFE0B8),
        secondary = Color(0xFFC5B3E6),
        onSecondary = Color(0xFF2E2340),
        secondaryContainer = Color(0xFF3A3048),
        onSecondaryContainer = Color(0xFFEBDDFF),
        tertiary = Color(0xFFE7BC71),
        onTertiary = Color(0xFF392A08),
        tertiaryContainer = Color(0xFF493B1F),
        onTertiaryContainer = Color(0xFFFFE2AA),
        background = Color(0xFF161412),
        onBackground = Color(0xFFF1E9DF),
        surface = Color(0xFF201D1A),
        onSurface = Color(0xFFF1E9DF),
        surfaceVariant = Color(0xFF26221F),
        onSurfaceVariant = Color(0xFFCFC2B4),
        surfaceContainer = Color(0xFF201D1A),
        surfaceContainerHigh = Color(0xFF2E2925),
        surfaceContainerHighest = Color(0xFF39332E),
        outline = Color(0xFF88796D),
        outlineVariant = Color(0xFF51483F),
        error = Color(0xFFFFB4AB),
        onError = Color(0xFF690005),
        errorContainer = Color(0xFF5F2523),
        onErrorContainer = Color(0xFFFFDAD6),
        scrim = Color(0xCC000000)
    ),
    semanticColors = NumPairsSemanticColors(
        success = Color(0xFF8BCF9B),
        onSuccess = Color(0xFF0E3A1C),
        successContainer = Color(0xFF24452D),
        onSuccessContainer = Color(0xFFC7F3CF),
        error = Color(0xFFFFB4AB),
        onError = Color(0xFF690005),
        errorContainer = Color(0xFF5F2523),
        onErrorContainer = Color(0xFFFFDAD6),
        selection = Color(0xFFA9C7FF),
        onSelection = Color(0xFF14315A),
        selectionContainer = Color(0xFF273A55),
        onSelectionContainer = Color(0xFFD7E2FF),
        tutorialHighlight = Color(0xFFEFC56A),
        onTutorialHighlight = Color(0xFF3B2D00),
        hiddenContainer = Color(0xFF2A2724),
        onHiddenContainer = Color(0xFFCFC2B4),
        hiddenBorder = Color(0xFF9A8C7F)
    )
)

internal val TerminalThemeDefinition = NumPairsThemeDefinition(
    id = PersonalizationTheme.TERMINAL,
    appearanceColors = darkColorScheme(
        primary = Color(0xFF65D46E),
        onPrimary = Color(0xFF06250A),
        primaryContainer = Color(0xFF1F5428),
        onPrimaryContainer = Color(0xFFBFFFC6),
        secondary = Color(0xFF7AD9D2),
        onSecondary = Color(0xFF073431),
        secondaryContainer = Color(0xFF123E3B),
        onSecondaryContainer = Color(0xFFB8F1EC),
        tertiary = Color(0xFFE2C66F),
        onTertiary = Color(0xFF342B00),
        tertiaryContainer = Color(0xFF473D13),
        onTertiaryContainer = Color(0xFFFFE793),
        background = Color(0xFF050805),
        onBackground = Color(0xFFD8F7DC),
        surface = Color(0xFF0A100B),
        onSurface = Color(0xFFD8F7DC),
        surfaceVariant = Color(0xFF0D170F),
        onSurfaceVariant = Color(0xFFA9CFAE),
        surfaceContainer = Color(0xFF0A100B),
        surfaceContainerHigh = Color(0xFF132016),
        surfaceContainerHighest = Color(0xFF1A2A1D),
        outline = Color(0xFF527A58),
        outlineVariant = Color(0xFF2C4B31),
        error = Color(0xFFFFB4AB),
        onError = Color(0xFF690005),
        errorContainer = Color(0xFF5F2523),
        onErrorContainer = Color(0xFFFFDAD6),
        scrim = Color(0xE6000000)
    ),
    semanticColors = NumPairsSemanticColors(
        success = Color(0xFFA6E3A1),
        onSuccess = Color(0xFF123A16),
        successContainer = Color(0xFF224226),
        onSuccessContainer = Color(0xFFC6F5C2),
        error = Color(0xFFFFB4AB),
        onError = Color(0xFF690005),
        errorContainer = Color(0xFF5F2523),
        onErrorContainer = Color(0xFFFFDAD6),
        selection = Color(0xFF7AD9D2),
        onSelection = Color(0xFF073431),
        selectionContainer = Color(0xFF123E3B),
        onSelectionContainer = Color(0xFFB8F1EC),
        tutorialHighlight = Color(0xFFE2C66F),
        onTutorialHighlight = Color(0xFF342B00),
        hiddenContainer = Color(0xFF101B12),
        onHiddenContainer = Color(0xFFA9CFAE),
        hiddenBorder = Color(0xFF6D9873)
    )
)

internal val EmberThemeDefinition = NumPairsThemeDefinition(
    id = PersonalizationTheme.EMBER,
    appearanceColors = lightColorScheme(
        primary = Color(0xFFA33A00),
        onPrimary = Color(0xFFFFFFFF),
        primaryContainer = Color(0xFFFFDCC7),
        onPrimaryContainer = Color(0xFF3A1100),
        secondary = Color(0xFF8C3B24),
        onSecondary = Color(0xFFFFFFFF),
        secondaryContainer = Color(0xFFFFDAD0),
        onSecondaryContainer = Color(0xFF3B0A02),
        tertiary = Color(0xFF745B14),
        onTertiary = Color(0xFFFFFFFF),
        tertiaryContainer = Color(0xFFF7E5AD),
        onTertiaryContainer = Color(0xFF2B2100),
        background = Color(0xFFFFF7ED),
        onBackground = Color(0xFF2D1B12),
        surface = Color(0xFFFFFCF7),
        onSurface = Color(0xFF2D1B12),
        surfaceVariant = Color(0xFFF8EBDD),
        onSurfaceVariant = Color(0xFF5D493D),
        surfaceContainer = Color(0xFFFFF7ED),
        surfaceContainerHigh = Color(0xFFF3E2D3),
        surfaceContainerHighest = Color(0xFFEAD6C6),
        outline = Color(0xFF806B5D),
        outlineVariant = Color(0xFFD6C2B3),
        error = Color(0xFFA10F2B),
        onError = Color(0xFFFFFFFF),
        errorContainer = Color(0xFFFFD9DE),
        onErrorContainer = Color(0xFF3F0010),
        scrim = Color(0x99301A0F)
    ),
    semanticColors = NumPairsSemanticColors(
        success = Color(0xFF326B3C),
        onSuccess = Color(0xFFFFFFFF),
        successContainer = Color(0xFFD9EFD9),
        onSuccessContainer = Color(0xFF153A1C),
        error = Color(0xFFA10F2B),
        onError = Color(0xFFFFFFFF),
        errorContainer = Color(0xFFFFD9DE),
        onErrorContainer = Color(0xFF3F0010),
        selection = Color(0xFF3D5F91),
        onSelection = Color(0xFFFFFFFF),
        selectionContainer = Color(0xFFD9E5FF),
        onSelectionContainer = Color(0xFF102B51),
        tutorialHighlight = Color(0xFF7B5500),
        onTutorialHighlight = Color(0xFFFFFFFF),
        hiddenContainer = Color(0xFFEDE2D7),
        onHiddenContainer = Color(0xFF58483D),
        hiddenBorder = Color(0xFF716054)
    )
)

internal val NumPairsThemeDefinitions = listOf(
    WarmThemeDefinition,
    FrostThemeDefinition,
    ObsidianThemeDefinition,
    TerminalThemeDefinition,
    EmberThemeDefinition
).associateBy(NumPairsThemeDefinition::id)

internal fun PersonalizationTheme.definition(): NumPairsThemeDefinition = NumPairsThemeDefinitions.getValue(this)
