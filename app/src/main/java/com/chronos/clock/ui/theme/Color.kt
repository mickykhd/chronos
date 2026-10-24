package com.chronos.clock.ui.theme

import androidx.compose.ui.graphics.Color

/**
 * The neutral half of the Chronos M3 palette: surfaces, content and error roles.
 *
 * Taken verbatim from `chronos_m3/DESIGN.md`. The accent roles live in
 * [ChronosTheme] because they are selectable; these tiers never change, so the app
 * stays true OLED black in every theme.
 */
object ChronosColors {

    // --- Surfaces: tonal containers, no drop shadows (M3 Expressive) ---
    val Surface = Color(0xFF121416)
    val SurfaceDim = Color(0xFF121416)
    val SurfaceBright = Color(0xFF37393B)
    val SurfaceContainerLowest = Color(0xFF0C0E10)
    val SurfaceContainerLow = Color(0xFF1A1C1E)
    val SurfaceContainer = Color(0xFF1E2022)
    val SurfaceContainerHigh = Color(0xFF282A2C)
    val SurfaceContainerHighest = Color(0xFF333537)

    // --- Content ---
    val OnSurface = Color(0xFFE2E2E5)
    val OnSurfaceVariant = Color(0xFFD7C3B6)
    val InverseSurface = Color(0xFFE2E2E5)
    val InverseOnSurface = Color(0xFF2F3133)
    val Outline = Color(0xFF9F8D82)
    val OutlineVariant = Color(0xFF52443B)

    // --- Error ---
    val Error = Color(0xFFFFB4AB)
    val OnError = Color(0xFF690005)
    val ErrorContainer = Color(0xFF93000A)
    val OnErrorContainer = Color(0xFFFFDAD6)

    // --- Semantic helpers used by the Expressive "inactive" treatments ---
    val DisabledText = Color(0xFF6E727A)
    val SlowestLap = Color(0xFFE06C6C)
    val Transparent = Color(0x00000000)

    /** The 1px interior ghost outline used to keep Tier-1 edges defined against the canvas. */
    val GhostBorder = Color(0x0DFFFFFF)
}
