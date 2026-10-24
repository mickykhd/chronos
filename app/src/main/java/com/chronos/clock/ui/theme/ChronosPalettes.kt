package com.chronos.clock.ui.theme

import androidx.compose.ui.graphics.Color

/**
 * The accent roles of a Chronos theme.
 *
 * The surface tiers stay exactly as the design system defines them — true OLED black
 * and its layered containers — so a theme changes the *key colour* of the app, not
 * its night-optimised foundation.
 */
data class ChronosAccents(
    val primary: Color,
    val onPrimary: Color,
    val primaryContainer: Color,
    val onPrimaryContainer: Color,
    val secondary: Color,
    val onSecondary: Color,
    val secondaryContainer: Color,
    val onSecondaryContainer: Color,
    val tertiary: Color,
    val onTertiary: Color,
    val tertiaryContainer: Color,
    val onTertiaryContainer: Color,
    val surfaceTint: Color,
)

/**
 * The selectable colour themes.
 *
 * [Ember] is the original Chronos M3 key colour; the rest are siblings built on the
 * same structure, so every accent role keeps its tonal relationship.
 */
enum class ChronosTheme(
    val id: String,
    val label: String,
    val blurb: String,
    val accents: ChronosAccents,
) {
    Ember(
        id = "ember",
        label = "Ember",
        blurb = "Warm amber & coral",
        accents = ChronosAccents(
            primary = Color(0xFFFFDCC5),
            onPrimary = Color(0xFF4F2500),
            primaryContainer = Color(0xFFFFB782),
            onPrimaryContainer = Color(0xFF79461B),
            secondary = Color(0xFFE7BDB1),
            onSecondary = Color(0xFF442A22),
            secondaryContainer = Color(0xFF604239),
            onSecondaryContainer = Color(0xFFD8AFA4),
            tertiary = Color(0xFFF5E1A7),
            onTertiary = Color(0xFF3B2F05),
            tertiaryContainer = Color(0xFFD8C58D),
            onTertiaryContainer = Color(0xFF5F5124),
            surfaceTint = Color(0xFFFFB782),
        ),
    ),

    Aurora(
        id = "aurora",
        label = "Aurora",
        blurb = "Sea green & teal",
        accents = ChronosAccents(
            primary = Color(0xFFC9F2E6),
            onPrimary = Color(0xFF00382E),
            primaryContainer = Color(0xFF6FE0C4),
            onPrimaryContainer = Color(0xFF00382C),
            secondary = Color(0xFFA9E2D2),
            onSecondary = Color(0xFF0B3A30),
            secondaryContainer = Color(0xFF235E52),
            onSecondaryContainer = Color(0xFFBCEFE2),
            tertiary = Color(0xFFBFE9C0),
            onTertiary = Color(0xFF14381A),
            tertiaryContainer = Color(0xFF93D495),
            onTertiaryContainer = Color(0xFF123A16),
            surfaceTint = Color(0xFF6FE0C4),
        ),
    ),

    Orchid(
        id = "orchid",
        label = "Orchid",
        blurb = "Violet & magenta",
        accents = ChronosAccents(
            primary = Color(0xFFF2DEFF),
            onPrimary = Color(0xFF34005C),
            primaryContainer = Color(0xFFDBA9FF),
            onPrimaryContainer = Color(0xFF3B0063),
            secondary = Color(0xFFE7C8F6),
            onSecondary = Color(0xFF3A1A4B),
            secondaryContainer = Color(0xFF5B3A6C),
            onSecondaryContainer = Color(0xFFEFD6FF),
            tertiary = Color(0xFFF7C9E9),
            onTertiary = Color(0xFF4A2340),
            tertiaryContainer = Color(0xFFDA9DCB),
            onTertiaryContainer = Color(0xFF4F2545),
            surfaceTint = Color(0xFFDBA9FF),
        ),
    ),

    Solar(
        id = "solar",
        label = "Solar",
        blurb = "Amber & crimson",
        accents = ChronosAccents(
            primary = Color(0xFFFFE1C4),
            onPrimary = Color(0xFF4A1D00),
            primaryContainer = Color(0xFFFF9C50),
            onPrimaryContainer = Color(0xFF5A2500),
            secondary = Color(0xFFF6C7A9),
            onSecondary = Color(0xFF4C2712),
            secondaryContainer = Color(0xFF6E3C22),
            onSecondaryContainer = Color(0xFFFFDCC0),
            tertiary = Color(0xFFFFE08F),
            onTertiary = Color(0xFF3F2A00),
            tertiaryContainer = Color(0xFFE5B24A),
            onTertiaryContainer = Color(0xFF3E2C00),
            surfaceTint = Color(0xFFFF9C50),
        ),
    ),

    Frost(
        id = "frost",
        label = "Frost",
        blurb = "Ice blue & cyan",
        accents = ChronosAccents(
            primary = Color(0xFFCFE9FF),
            onPrimary = Color(0xFF00344E),
            primaryContainer = Color(0xFF82C9F0),
            onPrimaryContainer = Color(0xFF00364F),
            secondary = Color(0xFFB7DAEC),
            onSecondary = Color(0xFF123A4C),
            secondaryContainer = Color(0xFF2A5670),
            onSecondaryContainer = Color(0xFFC9E8FC),
            tertiary = Color(0xFFBDEAE3),
            onTertiary = Color(0xFF0A3A33),
            tertiaryContainer = Color(0xFF85D0C7),
            onTertiaryContainer = Color(0xFF0B3C34),
            surfaceTint = Color(0xFF82C9F0),
        ),
    ),

    Graphite(
        id = "graphite",
        label = "Graphite",
        blurb = "Neutral monochrome",
        accents = ChronosAccents(
            primary = Color(0xFFE2E2E5),
            onPrimary = Color(0xFF1A1C1E),
            primaryContainer = Color(0xFF8F9195),
            onPrimaryContainer = Color(0xFF1A1C1E),
            secondary = Color(0xFFC9C9CE),
            onSecondary = Color(0xFF303236),
            secondaryContainer = Color(0xFF4A4C51),
            onSecondaryContainer = Color(0xFFDFE0E4),
            tertiary = Color(0xFFD6D6DB),
            onTertiary = Color(0xFF26282B),
            tertiaryContainer = Color(0xFF8C8E93),
            onTertiaryContainer = Color(0xFF222426),
            surfaceTint = Color(0xFFC9C9CE),
        ),
    );

    companion object {
        val Default = Ember

        fun fromId(id: String?): ChronosTheme =
            entries.firstOrNull { it.id == id } ?: Default
    }
}
