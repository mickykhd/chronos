package com.chronos.clock.ui.components

import androidx.compose.material3.LocalContentColor
import androidx.compose.ui.text.ExperimentalTextApi
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontVariation
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.LineHeightStyle
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.chronos.clock.R

/**
 * Material Symbols, bundled as a variable font so the `FILL` axis can be driven at
 * runtime: outlined by default, filled for the icons the design marks as filled
 * (`home`, `wb_sunny`, `assistant`, `notifications_active`, `bedtime`, …).
 *
 * The `opsz` axis is switched to 40 for large glyphs so stroke weight stays optically
 * balanced at display sizes.
 */
@OptIn(ExperimentalTextApi::class)
object MaterialSymbols {

    private fun family(fill: Int, opsz: Int) = FontFamily(
        Font(
            resId = R.font.material_symbols,
            weight = FontWeight.Normal,
            style = FontStyle.Normal,
            variationSettings = FontVariation.Settings(
                FontVariation.weight(400),
                FontVariation.Setting("FILL", fill.toFloat()),
                FontVariation.Setting("opsz", opsz.toFloat()),
            ),
        ),
    )

    val Outlined = family(fill = 0, opsz = 24)
    val Filled = family(fill = 1, opsz = 24)
    val OutlinedLarge = family(fill = 0, opsz = 40)
    val FilledLarge = family(fill = 1, opsz = 40)
}

/**
 * A Material Symbols glyph rendered as an icon.
 *
 * @param name one of the names in [ChronosGlyphs], e.g. `"alarm"`.
 * @param filled drives the `FILL` axis to 1.
 */
@Composable
fun ChronosIcon(
    name: String,
    modifier: Modifier = Modifier,
    size: Dp = 24.dp,
    tint: Color = LocalContentColor.current,
    filled: Boolean = false,
) {
    val family = when {
        filled && size.value >= 30f -> MaterialSymbols.FilledLarge
        filled -> MaterialSymbols.Filled
        size.value >= 30f -> MaterialSymbols.OutlinedLarge
        else -> MaterialSymbols.Outlined
    }
    Text(
        text = ChronosGlyphs.codepoint(name).toChar().toString(),
        modifier = modifier,
        color = tint,
        maxLines = 1,
        style = TextStyle(
            fontFamily = family,
            fontSize = size.value.sp,
            lineHeight = size.value.sp,
            fontWeight = FontWeight.Normal,
            letterSpacing = 0.sp,
            textAlign = TextAlign.Center,
            lineHeightStyle = LineHeightStyle(
                alignment = LineHeightStyle.Alignment.Center,
                trim = LineHeightStyle.Trim.None,
            ),
        ),
    )
}
