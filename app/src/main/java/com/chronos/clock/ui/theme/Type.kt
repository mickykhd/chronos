package com.chronos.clock.ui.theme

import androidx.compose.ui.text.ExperimentalTextApi
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontVariation
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import com.chronos.clock.R

/**
 * Roboto Flex, bundled as the variable font so the full axis set stays available:
 * `wght` (weight), `opsz` (optical size), `GRAD`, `wdth`, `slnt` and the parametric
 * `XOPQ`/`XTRA`/`YOPQ`/`YTAS`/`YTDE`/`YTFI`/`YTLC`/`YTUC` axes.
 *
 * Two families are exposed: a text-tuned instance (`opsz` 14, matching the design's
 * body copy) and a display-tuned instance (`opsz` 60) for the oversized hero numerals,
 * where the optical size axis changes stroke contrast noticeably.
 */
@OptIn(ExperimentalTextApi::class)
private fun robotoFlex(weight: Int, opsz: Float) = Font(
    resId = R.font.roboto_flex,
    weight = FontWeight(weight),
    style = FontStyle.Normal,
    variationSettings = FontVariation.Settings(
        FontVariation.weight(weight),
        FontVariation.Setting("opsz", opsz),
    ),
)

private fun robotoFlexFamily(opsz: Float) = FontFamily(
    robotoFlex(300, opsz),
    robotoFlex(400, opsz),
    robotoFlex(500, opsz),
    robotoFlex(600, opsz),
    robotoFlex(700, opsz),
)

val RobotoFlex = robotoFlexFamily(opsz = 14f)
val RobotoFlexDisplay = robotoFlexFamily(opsz = 60f)

/**
 * OpenType tabular figures. The design system requires `font-variant-numeric: tabular-nums`
 * on every numeric readout so rapidly incrementing millisecond/second values never
 * cause horizontal jitter.
 */
const val TabularFigures = "tnum"

private fun style(
    family: FontFamily,
    size: Int,
    lineHeight: Int,
    weight: FontWeight,
    letterSpacingEm: Double,
    tabular: Boolean = false,
) = TextStyle(
    fontFamily = family,
    fontSize = size.sp,
    lineHeight = lineHeight.sp,
    fontWeight = weight,
    letterSpacing = (letterSpacingEm * size).sp,
    fontFeatureSettings = if (tabular) TabularFigures else null,
)

/**
 * The Chronos M3 type scale, exactly as declared in `chronos_m3/DESIGN.md`.
 */
object ChronosType {

    /** Reserved for the central timer countdown and primary alarm clocks. */
    val DisplayHero = style(RobotoFlexDisplay, 96, 96, FontWeight.Light, -0.04, tabular = true)
    val DisplayHeroMobile = style(RobotoFlexDisplay, 64, 68, FontWeight.Light, -0.03, tabular = true)

    val HeadlineLg = style(RobotoFlexDisplay, 44, 52, FontWeight.Normal, -0.02, tabular = true)
    val HeadlineLgMobile = style(RobotoFlexDisplay, 36, 44, FontWeight.Normal, -0.02, tabular = true)

    val HeadlineMd = style(RobotoFlex, 28, 36, FontWeight.Medium, 0.0, tabular = true)
    val HeadlineSm = style(RobotoFlex, 22, 28, FontWeight.Medium, 0.0)

    val TitleLg = style(RobotoFlex, 18, 24, FontWeight.SemiBold, 0.0)
    val TitleMd = style(RobotoFlex, 16, 22, FontWeight.Medium, 0.01)

    val BodyLg = style(RobotoFlex, 16, 24, FontWeight.Normal, 0.01)
    val BodyMd = style(RobotoFlex, 14, 20, FontWeight.Normal, 0.02)

    val LabelLg = style(RobotoFlex, 14, 20, FontWeight.SemiBold, 0.02)
    val LabelMd = style(RobotoFlex, 12, 16, FontWeight.Medium, 0.04)
    val LabelSm = style(RobotoFlex, 11, 14, FontWeight.Medium, 0.05)

    /** All-caps eyebrow treatment used for section labels. */
    val Eyebrow = LabelSm.copy(letterSpacing = 1.1.sp)
}

/** The Chronos scale mapped onto Material's `Typography` slots. */
val ChronosTypography = androidx.compose.material3.Typography(
    displayLarge = ChronosType.DisplayHero,
    displayMedium = ChronosType.DisplayHeroMobile,
    displaySmall = ChronosType.HeadlineLg,
    headlineLarge = ChronosType.HeadlineLg,
    headlineMedium = ChronosType.HeadlineLgMobile,
    headlineSmall = ChronosType.HeadlineMd,
    titleLarge = ChronosType.TitleLg,
    titleMedium = ChronosType.TitleMd,
    titleSmall = ChronosType.TitleMd,
    bodyLarge = ChronosType.BodyLg,
    bodyMedium = ChronosType.BodyMd,
    bodySmall = ChronosType.BodyMd,
    labelLarge = ChronosType.LabelLg,
    labelMedium = ChronosType.LabelMd,
    labelSmall = ChronosType.LabelSm,
)
