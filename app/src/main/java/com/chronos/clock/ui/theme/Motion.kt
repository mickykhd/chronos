package com.chronos.clock.ui.theme

import androidx.compose.animation.core.CubicBezierEasing
import androidx.compose.animation.core.Easing
import androidx.compose.animation.core.SpringSpec
import androidx.compose.animation.core.spring

/**
 * Material 3 Expressive motion tokens.
 *
 * The design system calls for "fluid & temporal" motion, so every interactive element
 * uses the emphasized curve with a spring for anything the user drags or toggles.
 */
object ChronosMotion {

    // --- Curves (Material 3 expressive set) ---
    val Emphasized: Easing = CubicBezierEasing(0.2f, 0f, 0f, 1f)
    val EmphasizedDecelerate: Easing = CubicBezierEasing(0.05f, 0.7f, 0.1f, 1f)
    val EmphasizedAccelerate: Easing = CubicBezierEasing(0.3f, 0f, 0.8f, 0.15f)
    val Standard: Easing = CubicBezierEasing(0.2f, 0f, 0f, 1f)
    val StandardDecelerate: Easing = CubicBezierEasing(0f, 0f, 0f, 1f)
    val StandardAccelerate: Easing = CubicBezierEasing(0.3f, 0f, 1f, 1f)

    // --- Durations (ms) ---
    const val Short1 = 50
    const val Short2 = 100
    const val Short3 = 150
    const val Medium1 = 250
    const val Medium2 = 300
    const val Medium3 = 350
    const val Long1 = 400
    const val Long2 = 500
    const val Long3 = 600
    const val ExtraLong = 800

    // --- Springs for tactile, state-driven movement ---
    val Spatial: SpringSpec<Float> = spring(dampingRatio = 0.9f, stiffness = 380f)
    val SpatialDp: SpringSpec<androidx.compose.ui.unit.Dp> =
        spring(dampingRatio = 0.9f, stiffness = 380f)
    val Effects: SpringSpec<Float> = spring(dampingRatio = 0.7f, stiffness = 500f)
    val Bouncy: SpringSpec<Float> = spring(dampingRatio = 0.55f, stiffness = 420f)
    val Snappy: SpringSpec<Float> = spring(dampingRatio = 0.85f, stiffness = 700f)
}
