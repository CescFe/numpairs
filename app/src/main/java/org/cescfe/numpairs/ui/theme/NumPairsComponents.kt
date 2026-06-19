package org.cescfe.numpairs.ui.theme

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ButtonColors
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.IconButtonColors
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.TopAppBarColors
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.lerp
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp

object NumPairsComponents {
    val MediumRadius = 14.dp
    val LargeRadius = 20.dp

    val ThinBorderWidth = 1.dp
    val StrongBorderWidth = 2.dp
    val FocusBorderWidth = 3.dp

    val ButtonHeight = 52.dp

    val MediumShape: Shape
        get() = RoundedCornerShape(MediumRadius)

    val LargeShape: Shape
        get() = RoundedCornerShape(LargeRadius)

    @Composable
    fun PrimaryCtaButton(
        onClick: () -> Unit,
        modifier: Modifier = Modifier,
        enabled: Boolean = true,
        contentPadding: PaddingValues = ButtonDefaults.ContentPadding,
        content: @Composable () -> Unit
    ) {
        val shape = MediumShape
        val background = if (enabled) {
            Brush.verticalGradient(
                colors = listOf(
                    lerp(
                        MaterialTheme.colorScheme.primary,
                        MaterialTheme.colorScheme.onPrimaryContainer,
                        PRIMARY_BUTTON_TOP_TINT
                    ),
                    MaterialTheme.colorScheme.primary,
                    lerp(
                        MaterialTheme.colorScheme.primary,
                        MaterialTheme.colorScheme.primaryContainer,
                        PRIMARY_BUTTON_BOTTOM_SHADE
                    )
                )
            )
        } else {
            Brush.verticalGradient(
                colors = listOf(
                    MaterialTheme.colorScheme.surfaceVariant,
                    MaterialTheme.colorScheme.surfaceVariant
                )
            )
        }
        val contentColor = if (enabled) {
            MaterialTheme.colorScheme.onPrimary
        } else {
            MaterialTheme.colorScheme.onSurfaceVariant
        }
        val borderColor = if (enabled) {
            MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = PRIMARY_BUTTON_BORDER_ALPHA)
        } else {
            MaterialTheme.colorScheme.outlineVariant
        }

        Surface(
            onClick = onClick,
            modifier = modifier.defaultMinSize(
                minWidth = ButtonDefaults.MinWidth,
                minHeight = ButtonDefaults.MinHeight
            ).semantics { role = Role.Button },
            enabled = enabled,
            shape = shape,
            color = Color.Transparent,
            contentColor = contentColor,
            border = BorderStroke(
                width = ThinBorderWidth,
                color = borderColor
            ),
            shadowElevation = if (enabled) PRIMARY_BUTTON_SHADOW_ELEVATION else 0.dp
        ) {
            Box(
                modifier = Modifier.background(background, shape),
                contentAlignment = Alignment.Center
            ) {
                if (enabled) {
                    Box(
                        modifier = Modifier
                            .align(Alignment.TopCenter)
                            .matchParentSize()
                            .background(
                                Brush.verticalGradient(
                                    colors = listOf(
                                        MaterialTheme.colorScheme.onPrimaryContainer.copy(
                                            alpha = PRIMARY_BUTTON_INNER_HIGHLIGHT_ALPHA
                                        ),
                                        Color.Transparent
                                    )
                                ),
                                shape
                            )
                    )
                }
                Box(
                    modifier = Modifier.padding(contentPadding),
                    contentAlignment = Alignment.Center
                ) {
                    content()
                }
            }
        }
    }

    @Composable
    fun secondaryButtonColors(): ButtonColors = ButtonDefaults.outlinedButtonColors(
        containerColor = MaterialTheme.colorScheme.surface,
        contentColor = MaterialTheme.colorScheme.onSurface
    )

    @Composable
    fun secondaryButtonBorder(): BorderStroke = BorderStroke(
        width = ThinBorderWidth,
        color = MaterialTheme.colorScheme.outline
    )

    @Composable
    fun iconButtonColors(): IconButtonColors = IconButtonDefaults.filledTonalIconButtonColors(
        containerColor = MaterialTheme.colorScheme.surfaceVariant,
        contentColor = MaterialTheme.colorScheme.onSurfaceVariant
    )

    @Composable
    fun raisedSurfaceColor() = MaterialTheme.colorScheme.surfaceContainerHigh

    @Composable
    fun subtleSurfaceColor() = MaterialTheme.colorScheme.surfaceVariant

    @Composable
    fun defaultBorder(): BorderStroke = BorderStroke(
        width = ThinBorderWidth,
        color = MaterialTheme.colorScheme.outline
    )

    @Composable
    fun subtleBorder(): BorderStroke = BorderStroke(
        width = ThinBorderWidth,
        color = MaterialTheme.colorScheme.outlineVariant
    )

    @Composable
    fun focusBorder(): BorderStroke = BorderStroke(
        width = FocusBorderWidth,
        color = MaterialTheme.colorScheme.primary
    )

    @Composable
    fun errorBorder(): BorderStroke = BorderStroke(
        width = StrongBorderWidth,
        color = MaterialTheme.colorScheme.error
    )

    @Composable
    fun successContainerColor() = MaterialTheme.colorScheme.primaryContainer

    @Composable
    fun successContentColor() = MaterialTheme.colorScheme.onPrimaryContainer

    @Composable
    fun errorContainerColor() = MaterialTheme.colorScheme.errorContainer

    @Composable
    fun errorContentColor() = MaterialTheme.colorScheme.onErrorContainer

    @Composable
    fun hiddenContainerColor() = MaterialTheme.colorScheme.surfaceVariant

    @Composable
    fun hiddenContentColor() = MaterialTheme.colorScheme.onSurfaceVariant

    @Composable
    fun topAppBarColors(): TopAppBarColors = TopAppBarDefaults.topAppBarColors(
        containerColor = MaterialTheme.colorScheme.background,
        titleContentColor = MaterialTheme.colorScheme.onSurfaceVariant,
        navigationIconContentColor = MaterialTheme.colorScheme.onSurfaceVariant,
        actionIconContentColor = MaterialTheme.colorScheme.onSurfaceVariant
    )

    private val PRIMARY_BUTTON_SHADOW_ELEVATION = 2.dp
    private const val PRIMARY_BUTTON_TOP_TINT = 0.10f
    private const val PRIMARY_BUTTON_BOTTOM_SHADE = 0.10f
    private const val PRIMARY_BUTTON_BORDER_ALPHA = 0.28f
    private const val PRIMARY_BUTTON_INNER_HIGHLIGHT_ALPHA = 0.18f
}
