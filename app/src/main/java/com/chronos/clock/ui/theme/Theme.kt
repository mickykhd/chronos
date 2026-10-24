package com.chronos.clock.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color

/**
 * The surface half of the palette. These tiers never change with the theme: the
 * design system is built around true OLED black so the app stays readable and
 * battery-friendly in a dark bedroom.
 */
private val ChronosSurfaces = darkColorScheme(
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

    error = ChronosColors.Error,
    onError = ChronosColors.OnError,
    errorContainer = ChronosColors.ErrorContainer,
    onErrorContainer = ChronosColors.OnErrorContainer,

    scrim = Color.Black,
)

/**
 * Builds the full scheme for a theme: fixed surfaces plus the theme's accent roles.
 */
private fun schemeFor(theme: ChronosTheme) = ChronosSurfaces.copy(
    primary = theme.accents.primary,
    onPrimary = theme.accents.onPrimary,
    primaryContainer = theme.accents.primaryContainer,
    onPrimaryContainer = theme.accents.onPrimaryContainer,
    inversePrimary = theme.accents.surfaceTint,

    secondary = theme.accents.secondary,
    onSecondary = theme.accents.onSecondary,
    secondaryContainer = theme.accents.secondaryContainer,
    onSecondaryContainer = theme.accents.onSecondaryContainer,

    tertiary = theme.accents.tertiary,
    onTertiary = theme.accents.onTertiary,
    tertiaryContainer = theme.accents.tertiaryContainer,
    onTertiaryContainer = theme.accents.onTertiaryContainer,

    surfaceTint = theme.accents.surfaceTint,
)

/** The active theme, readable from any composable. */
val LocalChronosTheme = staticCompositionLocalOf { ChronosTheme.Default }

@Composable
fun ChronosTheme(
    theme: ChronosTheme = ChronosTheme.Default,
    content: @Composable () -> Unit,
) {
    CompositionLocalProvider(LocalChronosTheme provides theme) {
        MaterialTheme(
            colorScheme = schemeFor(theme),
            typography = ChronosTypography,
            shapes = ChronosShapes,
            content = content,
        )
    }
}
