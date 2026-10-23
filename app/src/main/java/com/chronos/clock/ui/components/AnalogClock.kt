package com.chronos.clock.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.withFrameMillis
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.chronos.clock.ui.theme.ChronosColors
import java.time.Instant
import java.time.ZoneId
import java.time.ZonedDateTime
import kotlin.math.cos
import kotlin.math.sin

/**
 * The live Material 3 Expressive analog clock widget.
 *
 * The second hand sweeps continuously rather than ticking — the design's
 * `smoothRotate 60s linear infinite` keyframe — so it always lands on the true
 * second while reading as a smooth, kinetic motion metaphor.
 */
@Composable
fun AnalogClock(
    modifier: Modifier = Modifier,
    size: Dp = 176.dp,
    zoneId: ZoneId = ZoneId.systemDefault(),
) {
    val nowMillis = remember { mutableLongStateOf(System.currentTimeMillis()) }

    LaunchedEffect(zoneId) {
        while (true) {
            withFrameMillis {
                nowMillis.longValue = System.currentTimeMillis()
            }
        }
    }

    Box(modifier = modifier.size(size)) {
        Canvas(modifier = Modifier.size(size)) {
            val radius = this.size.minDimension / 2f
            val center = Offset(radius, radius)
            val stroke = radius * 0.03f

            // Dial plate.
            drawCircle(
                color = ChronosColors.SurfaceContainerLowest.copy(alpha = 0.60f),
                radius = radius - stroke,
                center = center,
            )
            drawCircle(
                color = ChronosColors.SurfaceContainerHighest,
                radius = radius - stroke,
                center = center,
                style = Stroke(width = stroke),
            )

            // Dial ambient centre ring.
            drawCircle(
                color = ChronosColors.SurfaceContainer.copy(alpha = 0.30f),
                radius = radius * 0.48f,
                center = center,
            )
            drawCircle(
                color = ChronosColors.SurfaceContainerHigh.copy(alpha = 0.60f),
                radius = radius * 0.48f,
                center = center,
                style = Stroke(width = stroke * 0.4f),
            )

            // Hour markers: cardinal pills.
            val markerLength = radius * 0.28f
            val markerWidth = radius * 0.12f
            val inset = radius * 0.16f
            drawMarker(center, radius, 0f, markerLength, markerWidth, inset, ChronosColors.PrimaryContainer.copy(alpha = 0.80f))
            drawMarker(center, radius, 90f, markerLength, markerWidth, inset, ChronosColors.OutlineVariant)
            drawMarker(center, radius, 180f, markerLength, markerWidth, inset, ChronosColors.OutlineVariant)
            drawMarker(center, radius, 270f, markerLength, markerWidth, inset, ChronosColors.OutlineVariant)

            val time = ZonedDateTime.ofInstant(Instant.ofEpochMilli(nowMillis.longValue), zoneId)
            val seconds = time.second + time.nano / 1_000_000_000.0
            val minutes = time.minute + seconds / 60.0
            val hours = (time.hour % 12) + minutes / 60.0

            // Hands. Angles are measured clockwise from 12 o'clock.
            val hourAngle = Math.toRadians(hours * 30.0 - 90.0)
            val minuteAngle = Math.toRadians(minutes * 6.0 - 90.0)
            val secondAngle = Math.toRadians(seconds * 6.0 - 90.0)

            drawHand(
                center = center,
                angle = hourAngle,
                length = radius * 0.58f,
                color = ChronosColors.Primary,
                width = radius * 0.09f,
                tailLength = radius * 0.10f,
            )
            drawHand(
                center = center,
                angle = minuteAngle,
                length = radius * 0.72f,
                color = ChronosColors.PrimaryContainer,
                width = radius * 0.06f,
                tailLength = radius * 0.10f,
            )
            drawHand(
                center = center,
                angle = secondAngle,
                length = radius * 0.88f,
                color = ChronosColors.Tertiary,
                width = radius * 0.028f,
                tailLength = radius * 0.22f,
            )

            // Central pivot nut.
            drawCircle(
                color = ChronosColors.OnPrimary,
                radius = radius * 0.075f,
                center = center,
            )
            drawCircle(
                color = ChronosColors.PrimaryContainer,
                radius = radius * 0.075f,
                center = center,
                style = Stroke(width = radius * 0.045f),
            )
        }
    }
}

private fun DrawScope.drawMarker(
    center: Offset,
    radius: Float,
    angleDeg: Float,
    length: Float,
    width: Float,
    inset: Float,
    color: Color,
) {
    val angle = Math.toRadians(angleDeg.toDouble() - 90.0)
    val dirX = cos(angle).toFloat()
    val dirY = sin(angle).toFloat()

    val outer = radius - inset
    val inner = outer - length

    drawLine(
        color = color,
        start = Offset(center.x + dirX * inner, center.y + dirY * inner),
        end = Offset(center.x + dirX * outer, center.y + dirY * outer),
        strokeWidth = width,
        cap = StrokeCap.Round,
    )
}

private fun DrawScope.drawHand(
    center: Offset,
    angle: Double,
    length: Float,
    color: Color,
    width: Float,
    tailLength: Float,
) {
    val dirX = cos(angle).toFloat()
    val dirY = sin(angle).toFloat()
    val tip = Offset(center.x + dirX * length, center.y + dirY * length)
    val tail = Offset(center.x - dirX * tailLength, center.y - dirY * tailLength)
    drawLine(
        color = color,
        start = tail,
        end = tip,
        strokeWidth = width,
        cap = StrokeCap.Round,
    )
}
