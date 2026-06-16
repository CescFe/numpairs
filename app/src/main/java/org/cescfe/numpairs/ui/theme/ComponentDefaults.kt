package org.cescfe.numpairs.ui.theme

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.ButtonColors
import androidx.compose.material3.IconButtonColors
import androidx.compose.material3.TopAppBarColors
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.unit.dp

object ComponentDefaults {
    val SmallRadius = 10.dp
    val MediumRadius = 14.dp
    val LargeRadius = 20.dp

    val ThinBorderWidth = 1.dp
    val StrongBorderWidth = 2.dp
    val FocusBorderWidth = 3.dp

    val ButtonHeight = 52.dp
    val IconButtonSize = 44.dp

    val MediumShape: Shape
        get() = RoundedCornerShape(MediumRadius)

    val LargeShape: Shape
        get() = RoundedCornerShape(LargeRadius)

    @Composable
    fun primaryButtonColors(): ButtonColors =
        ButtonDefaults.buttonColors(
            containerColor = MaterialTheme.colorScheme.primary,
            contentColor = MaterialTheme.colorScheme.onPrimary,
        )

    @Composable
    fun secondaryButtonColors(): ButtonColors =
        ButtonDefaults.outlinedButtonColors(
            containerColor = MaterialTheme.colorScheme.surface,
            contentColor = MaterialTheme.colorScheme.onSurface,
        )

    @Composable
    fun secondaryButtonBorder(): BorderStroke =
        BorderStroke(
            width = ThinBorderWidth,
            color = MaterialTheme.colorScheme.outline,
        )

    @Composable
    fun iconButtonColors(): IconButtonColors =
        IconButtonDefaults.filledTonalIconButtonColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant,
            contentColor = MaterialTheme.colorScheme.onSurfaceVariant,
        )

    @Composable
    fun raisedSurfaceColor() =
        MaterialTheme.colorScheme.surfaceContainerHigh

    @Composable
    fun subtleSurfaceColor() =
        MaterialTheme.colorScheme.surfaceVariant

    @Composable
    fun defaultBorder(): BorderStroke =
        BorderStroke(
            width = ThinBorderWidth,
            color = MaterialTheme.colorScheme.outline,
        )

    @Composable
    fun subtleBorder(): BorderStroke =
        BorderStroke(
            width = ThinBorderWidth,
            color = MaterialTheme.colorScheme.outlineVariant,
        )

    @Composable
    fun focusBorder(): BorderStroke =
        BorderStroke(
            width = FocusBorderWidth,
            color = MaterialTheme.colorScheme.primary,
        )

    @Composable
    fun errorBorder(): BorderStroke =
        BorderStroke(
            width = StrongBorderWidth,
            color = MaterialTheme.colorScheme.error,
        )

    @Composable
    fun successContainerColor() =
        MaterialTheme.colorScheme.primaryContainer

    @Composable
    fun successContentColor() =
        MaterialTheme.colorScheme.onPrimaryContainer

    @Composable
    fun errorContainerColor() =
        MaterialTheme.colorScheme.errorContainer

    @Composable
    fun errorContentColor() =
        MaterialTheme.colorScheme.onErrorContainer

    @Composable
    fun hiddenContainerColor() =
        MaterialTheme.colorScheme.surfaceVariant

    @Composable
    fun hiddenContentColor() =
        MaterialTheme.colorScheme.onSurfaceVariant

    @Composable
    fun topAppBarColors(): TopAppBarColors =
        TopAppBarDefaults.topAppBarColors(
            containerColor = MaterialTheme.colorScheme.background,
            titleContentColor = MaterialTheme.colorScheme.onBackground,
            navigationIconContentColor = MaterialTheme.colorScheme.onBackground,
            actionIconContentColor = MaterialTheme.colorScheme.onBackground,
        )
}