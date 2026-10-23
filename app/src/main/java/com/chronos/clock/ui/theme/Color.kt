package com.chronos.clock.ui.theme

import androidx.compose.ui.graphics.Color

/**
 * Chronos M3 palette.
 *
 * Taken verbatim from `chronos_m3/DESIGN.md`. A radiant amber-coral dynamic key colour
 * interacting with layered obsidian-slate dark neutrals: zero optical fatigue in
 * pitch-black conditions, punchy urgency for alarms and timers.
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
    val SurfaceTint = Color(0xFFFFB782)

    // --- Primary: warm amber / apricot ---
    val Primary = Color(0xFFFFDCC5)
    val OnPrimary = Color(0xFF4F2500)
    val PrimaryContainer = Color(0xFFFFB782)
    val OnPrimaryContainer = Color(0xFF79461B)
    val InversePrimary = Color(0xFF875125)

    // --- Secondary: soft terracotta / coral ---
    val Secondary = Color(0xFFE7BDB1)
    val OnSecondary = Color(0xFF442A22)
    val SecondaryContainer = Color(0xFF604239)
    val OnSecondaryContainer = Color(0xFFD8AFA4)

    // --- Tertiary: sunlit brass ---
    val Tertiary = Color(0xFFF5E1A7)
    val OnTertiary = Color(0xFF3B2F05)
    val TertiaryContainer = Color(0xFFD8C58D)
    val OnTertiaryContainer = Color(0xFF5F5124)

    // --- Error ---
    val Error = Color(0xFFFFB4AB)
    val OnError = Color(0xFF690005)
    val ErrorContainer = Color(0xFF93000A)
    val OnErrorContainer = Color(0xFFFFDAD6)

    // --- Fixed variants (used by the "inactive alarm" day pills) ---
    val PrimaryFixed = Color(0xFFFFDCC5)
    val PrimaryFixedDim = Color(0xFFFFB782)
    val OnPrimaryFixed = Color(0xFF301400)
    val OnPrimaryFixedVariant = Color(0xFF6B3A10)
    val SecondaryFixed = Color(0xFFFFDBD0)
    val SecondaryFixedDim = Color(0xFFE7BDB1)
    val OnSecondaryFixed = Color(0xFF2C150E)
    val OnSecondaryFixedVariant = Color(0xFF5D4037)
    val TertiaryFixed = Color(0xFFF5E1A7)
    val TertiaryFixedDim = Color(0xFFD8C58D)
    val OnTertiaryFixed = Color(0xFF231B00)
    val OnTertiaryFixedVariant = Color(0xFF53461A)

    // --- Semantic helpers used by the Expressive "inactive" treatments ---
    val DisabledText = Color(0xFF6E727A)
    val SlowestLap = Color(0xFFE06C6C)
    val Transparent = Color(0x00000000)

    /** The 1px interior ghost outline used to keep Tier-1 edges defined against the canvas. */
    val GhostBorder = Color(0x0DFFFFFF)
}
