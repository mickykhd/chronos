package com.chronos.clock.ui.theme

import androidx.compose.ui.unit.dp

/**
 * Spacing tokens from the design system. Layouts follow a fluid, device-adapting
 * single-canvas model anchored by bottom-weighted thumb-zone ergonomics.
 */
object ChronosDimens {
    val Gutter = 16.dp
    val GutterMobile = 12.dp
    val Margin = 24.dp
    val MarginMobile = 16.dp

    val SpaceXs = 4.dp
    val SpaceSm = 8.dp
    val SpaceMd = 16.dp
    val SpaceLg = 24.dp
    val SpaceXl = 40.dp

    /** Fixed-height bottom navigation dock. */
    val NavBarHeight = 80.dp

    /** The floating active-tab indicator that wraps the nav icon. */
    val NavPillWidth = 64.dp
    val NavPillHeight = 32.dp

    /** Standard FAB. */
    val FabSize = 64.dp

    /** The oversized play/pause button on the timer and stopwatch. */
    val PlayButtonSize = 80.dp

    /** Secondary circular action buttons. */
    val ActionButtonSize = 56.dp
    val ActionButtonSizeSmall = 36.dp

    /** Hero circular gauges. */
    val GaugeSize = 288.dp
    val AnalogClockSize = 176.dp

    /** Day-of-week selector pills. */
    val DayPillSize = 32.dp

    /** Material 3 switch. */
    val SwitchWidth = 52.dp
    val SwitchHeight = 32.dp
    val SwitchThumbSize = 24.dp
}
