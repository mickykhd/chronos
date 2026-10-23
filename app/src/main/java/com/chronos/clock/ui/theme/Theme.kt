package com.chronos.clock.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

/**
 * The app is a single, permanently-dark OLED experience: the design system is built
 * around true black surfaces to prevent glare in bedroom environments, so the light
 * scheme intentionally mirrors the dark one.
 */
private val ChronosColorScheme = darkColorScheme(
    primary = ChronosColors.Primary,
    onPrimary = ChronosColors.OnPrimary,
    primaryContainer = ChronosColors.PrimaryContainer,
    onPrimaryContainer = ChronosColors.OnPrimaryContainer,
    inversePrimary = ChronosColors.InversePrimary,

    secondary = ChronosColors.Secondary,
    onSecondary = ChronosColors.OnSecondary,
    secondaryContainer = ChronosColors.SecondaryContainer,
    onSecondaryContainer = ChronosColors.OnSecondaryContainer,

    tertiary = ChronosColors.Tertiary,
    onTertiary = ChronosColors.OnTertiary,
    tertiaryContainer = ChronosColors.TertiaryContainer,
    onTertiaryContainer = ChronosColors.OnTertiaryContainer,

    error = ChronosColors.Error,
    onError = ChronosColors.OnError,
    errorContainer = ChronosColors.ErrorContainer,
    onErrorContainer = ChronosColors.OnErrorContainer,

    background = ChronosColors.Surface,
    onBackground = ChronosColors.OnSurface,
    surface = ChronosColors.Surface,
    onSurface = ChronosColors.OnSurface,
    surfaceVariant = ChronosColors.SurfaceBright,
    onSurfaceVariant = ChronosColors.OnSurfaceVariant,

    surfaceBright = ChronosColors.SurfaceBright,
    surfaceDim = ChronosColors.SurfaceDim,
    surfaceContainerLowest = ChronosColors.SurfaceContainerLowest,
    surfaceContainerLow = ChronosColors.SurfaceContainerLow,
    surfaceContainer = ChronosColors.SurfaceContainer,
    surfaceContainerHigh = ChronosColors.SurfaceContainerHigh,
    surfaceContainerHighest = ChronosColors.SurfaceContainerHighest,

    outline = ChronosColors.Outline,
    outlineVariant = ChronosColors.OutlineVariant,

    inverseSurface = ChronosColors.InverseSurface,
    inverseOnSurface = ChronosColors.InverseOnSurface,

    surfaceTint = ChronosColors.SurfaceTint,
    scrim = Color.Black,
)

@Composable
fun ChronosTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = ChronosColorScheme,
        typography = ChronosTypography,
        shapes = ChronosShapes,
        content = content,
    )
}
