package com.chronos.clock.ui.theme

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Shapes
import androidx.compose.ui.unit.dp

/**
 * Chronos M3 shapes. Geometry shifts intentionally between squircle-curved containers
 * and full capsule/pill shapes.
 */
val ChronosShapes = Shapes(
    extraSmall = RoundedCornerShape(8.dp),
    small = RoundedCornerShape(12.dp),
    medium = RoundedCornerShape(16.dp),
    large = RoundedCornerShape(32.dp),
    extraLarge = RoundedCornerShape(48.dp),
)

object ChronosRadius {
    /** Cards & sheets: `rounded-xl` in the design. */
    val Card = 32.dp

    /** Alarm cards, which the design pins at 28px. */
    val AlarmCard = 28.dp

    /** Standard FAB squircle. */
    val Fab = 24.dp

    /** The oversized play/pause squircle. */
    val PlayButton = 28.dp

    /** Secondary icon wells (bedtime mode, list row icons). */
    val IconWell = 16.dp

    val Pill = 9999.dp
}
