package com.chronos.clock.ui.components

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.chronos.clock.ui.theme.ChronosColors

/**
 * The dual-ring circular progress indicator from the design system.
 *
 * - Background ring in `#282A2C` at 8dp stroke weight.
 * - Foreground ring in `#FFB782` (or a gradient toward `#E7BDB1`) with a rounded
 *   stroke termination.
 * - Optional concentric tick ring, used by the stopwatch gauge.
 */
@Composable
fun ChronosGauge(
    progress: Float,
    modifier: Modifier = Modifier,
    trackColor: Color = ChronosColors.SurfaceContainerHigh,
    indicatorColor: Color = ChronosColors.PrimaryContainer,
    trackWidth: Dp = 8.dp,
    indicatorWidth: Dp = 9.dp,
    showTicks: Boolean = false,
    glow: Boolean = true,
    content: @Composable BoxScope.() -> Unit,
) {
    val animated by animateFloatAsState(
        targetValue = progress.coerceIn(0f, 1f),
        animationSpec = spring(dampingRatio = 0.9f, stiffness = 120f),
        label = "gaugeProgress",
    )

    Box(modifier = modifier, contentAlignment = Alignment.Center) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val strokePx = trackWidth.toPx()
            val indicatorPx = indicatorWidth.toPx()
            val radius = (size.minDimension - indicatorPx) / 2f
            val center = Offset(size.width / 2f, size.height / 2f)

            if (glow) {
                drawCircle(
                    brush = Brush.radialGradient(
                        colors = listOf(
                            indicatorColor.copy(alpha = 0.13f),
                            Color.Transparent,
                        ),
                        center = center,
                        radius = radius,
                    ),
                    radius = radius,
                    center = center,
                )
            }

            if (showTicks) {
                val tickRadius = radius + (strokePx * 0.85f)
                drawCircle(
                    color = ChronosColors.SurfaceContainerHighest.copy(alpha = 0.6f),
                    radius = tickRadius,
                    center = center,
                    style = Stroke(
                        width = strokePx * 0.5f,
                        pathEffect = androidx.compose.ui.graphics.PathEffect.dashPathEffect(
                            floatArrayOf(strokePx * 0.35f, strokePx * 1.7f),
                        ),
                    ),
                )
            }

            drawCircle(
                color = trackColor,
                radius = radius,
                center = center,
                style = Stroke(width = strokePx),
            )

            if (animated > 0f) {
                drawArc(
                    color = indicatorColor,
                    startAngle = -90f,
                    sweepAngle = 360f * animated,
                    useCenter = false,
                    topLeft = Offset(center.x - radius, center.y - radius),
                    size = androidx.compose.ui.geometry.Size(radius * 2, radius * 2),
                    style = Stroke(width = indicatorPx, cap = StrokeCap.Round),
                )
            }
        }
        content()
    }
}
