package com.chronos.clock.ui.components

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.spring
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.size
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.drawText
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.chronos.clock.ui.theme.ChronosColors
import com.chronos.clock.ui.theme.ChronosType
import kotlin.math.PI
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.roundToInt
import kotlin.math.sin

/** Which field of the time the dial is currently editing. */
enum class TimePickerField { Hour, Minute }

/**
 * A hand-drawn, animated clock dial for picking a time.
 *
 * The Material 3 `TimePicker` renders a very large display row that overflows the
 * space it is given inside a dialog on a phone, so Chronos uses its own dial: the
 * numbers never clip, the selection slides between positions, and the hand springs
 * to wherever the finger lands.
 */
@Composable
fun ChronosTimePicker(
    hour: Int,
    minute: Int,
    onTimeChange: (hour: Int, minute: Int) -> Unit,
    modifier: Modifier = Modifier,
    is24Hour: Boolean = false,
    diameter: Dp = 268.dp,
) {
    var field by remember { mutableStateOf(TimePickerField.Hour) }
    val accent = MaterialTheme.colorScheme.primaryContainer
    val onAccent = MaterialTheme.colorScheme.onPrimaryContainer
    val outline = ChronosColors.Outline
    val track = ChronosColors.SurfaceContainerHigh

    // The value the hand is currently pointing at, animated for smooth movement.
    val hand = remember { Animatable(selectionFor(hour, minute, field, is24Hour).toFloat()) }
    val highlight = remember { Animatable(selectionFor(hour, minute, field, is24Hour).toFloat()) }

    LaunchedEffect(hour, minute, field, is24Hour) {
        val target = selectionFor(hour, minute, field, is24Hour).toFloat()
        hand.animateTo(target, spring(dampingRatio = 0.75f, stiffness = 420f))
        highlight.animateTo(target, spring(dampingRatio = 0.7f, stiffness = 380f))
    }

    val labels = remember(field, is24Hour) { labelsFor(field, is24Hour) }
    val textMeasurer = rememberTextMeasurer()

    // The label the live value is sitting on top of, so it is not drawn twice.
    val currentValue = selectionFor(hour, minute, field, is24Hour)
    val hiddenLabel = remember(currentValue, labels, field, is24Hour) {
        labels.minByOrNull { label ->
            val delta = kotlin.math.abs(label.selection - currentValue)
            minOf(delta, 60 - delta)
        }?.selection
    }

    fun valueAt(position: Offset, size: Float): Int {
        val centre = Offset(size / 2f, size / 2f)
        val vec = position - centre
        var degrees = Math.toDegrees(atan2(vec.y.toDouble(), vec.x.toDouble())).toFloat() + 90f
        if (degrees < 0f) degrees += 360f
        return when (field) {
            TimePickerField.Hour ->
                if (is24Hour) (degrees / 15f).roundToInt() % 24
                else (degrees / 30f).roundToInt() % 12
            TimePickerField.Minute -> (degrees / 6f).roundToInt() % 60
        }
    }

    fun apply(value: Int) {
        when (field) {
            TimePickerField.Hour -> {
                val normalised = if (is24Hour) {
                    value
                } else {
                    val current12 = hour % 12
                    val picked = if (value == 0) 12 else value
                    // Keep the AM/PM half the user was on.
                    val candidate = if (hour < 12) picked else picked + 12
                    if (candidate == 0) 12 else candidate
                }
                onTimeChange(normalised, minute)
                field = TimePickerField.Minute
            }
            TimePickerField.Minute -> onTimeChange(hour, value)
        }
    }

    Box(
        modifier = modifier
            .size(diameter)
            .pointerInput(field, is24Hour) {
                val sizePx = size.width.toFloat()
                awaitEachGesture {
                    val down = awaitFirstDown(requireUnconsumed = false)
                    apply(valueAt(down.position, sizePx))
                    down.consume()
                    do {
                        val event = awaitPointerEvent()
                        val change = event.changes.firstOrNull { it.id == down.id } ?: break
                        if (change.pressed) {
                            apply(valueAt(change.position, sizePx))
                            change.consume()
                        }
                    } while (change.pressed)
                }
            },
        contentAlignment = Alignment.Center,
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val centre = Offset(size.width / 2f, size.height / 2f)
            val outer = size.minDimension / 2f
            val labelRadius = outer * 0.72f

            // Dial plate.
            drawCircle(
                color = ChronosColors.SurfaceContainerLowest.copy(alpha = 0.55f),
                radius = outer - strokePx(3f),
                center = centre,
            )
            drawCircle(
                color = track,
                radius = outer - strokePx(3f),
                center = centre,
                style = Stroke(width = strokePx(3f)),
            )

            // Hour ticks around the rim.
            val ticks = if (is24Hour) 48 else 24
            repeat(ticks) { index ->
                val angle = Math.toRadians(index * 360.0 / ticks - 90.0)
                val major = index % (ticks / 12) == 0
                val outerEnd = outer - strokePx(8f)
                val innerEnd = outerEnd - strokePx(if (major) 9f else 5f)
                val dirX = cos(angle).toFloat()
                val dirY = sin(angle).toFloat()
                drawLine(
                    color = if (major) outline.copy(alpha = 0.55f) else outline.copy(alpha = 0.22f),
                    start = Offset(centre.x + dirX * innerEnd, centre.y + dirY * innerEnd),
                    end = Offset(centre.x + dirX * outerEnd, centre.y + dirY * outerEnd),
                    strokeWidth = strokePx(if (major) 2.5f else 1.5f),
                    cap = StrokeCap.Round,
                )
            }

            // The sliding selection highlight.
            val highlightAngle = Math.toRadians(highlight.value * stepDegrees(field, is24Hour) - 90.0)
            drawCircle(
                color = accent,
                radius = strokePx(19f),
                center = Offset(
                    centre.x + cos(highlightAngle).toFloat() * labelRadius,
                    centre.y + sin(highlightAngle).toFloat() * labelRadius,
                ),
            )

            // The hand.
            val handAngle = Math.toRadians(hand.value * stepDegrees(field, is24Hour) - 90.0)
            val handEnd = Offset(
                centre.x + cos(handAngle).toFloat() * (labelRadius - strokePx(20f)),
                centre.y + sin(handAngle).toFloat() * (labelRadius - strokePx(20f)),
            )
            drawLine(
                color = accent,
                start = centre,
                end = handEnd,
                strokeWidth = strokePx(3f),
                cap = StrokeCap.Round,
            )
            drawCircle(color = accent, radius = strokePx(5f), center = centre)

            // The live value, drawn inside the selection circle.
            val valueText = liveText(currentValue, field)
            val valueStyle = (
                if (field == TimePickerField.Hour) {
                    ChronosType.TitleMd
                } else {
                    ChronosType.BodyMd
                }
                ).copy(color = onAccent)
            val valueSize = textMeasurer.measure(valueText, valueStyle).size
            drawText(
                textMeasurer = textMeasurer,
                text = valueText,
                topLeft = Offset(
                    centre.x + cos(highlightAngle).toFloat() * labelRadius - valueSize.width / 2f,
                    centre.y + sin(highlightAngle).toFloat() * labelRadius - valueSize.height / 2f,
                ),
                style = valueStyle,
            )
        }

        labels.forEach { label ->
            if (label.selection == hiddenLabel) return@forEach
            val angle = Math.toRadians(label.selection * stepDegrees(field, is24Hour) - 90.0)
            val radius = (diameter.value * 0.36f).dp
            Text(
                text = label.text,
                style = if (field == TimePickerField.Hour) {
                    ChronosType.TitleMd
                } else {
                    ChronosType.BodyMd
                },
                color = ChronosColors.OnSurfaceVariant,
                textAlign = TextAlign.Center,
                modifier = Modifier
                    .align(Alignment.Center)
                    .offset(
                        x = (cos(angle).toFloat() * radius.value).dp,
                        y = (sin(angle).toFloat() * radius.value).dp,
                    ),
            )
        }
    }
}

private data class DialLabel(val selection: Int, val text: String)

private fun labelsFor(field: TimePickerField, is24Hour: Boolean): List<DialLabel> = when (field) {
    TimePickerField.Hour ->
        if (is24Hour) {
            (0 until 24).map { DialLabel(it, it.toString()) }
        } else {
            (0 until 12).map { DialLabel(it, if (it == 0) "12" else it.toString()) }
        }
    TimePickerField.Minute ->
        (0 until 12).map { index ->
            val minutes = index * 5
            DialLabel(minutes, if (minutes == 0) "00" else minutes.toString().padStart(2, '0'))
        }
}

/** The text shown inside the selection circle. */
private fun liveText(value: Int, field: TimePickerField): String = when (field) {
    TimePickerField.Hour -> if (value == 0) "12" else value.toString()
    TimePickerField.Minute -> value.toString().padStart(2, '0')
}

/** How many degrees one step represents on the dial. */
private fun stepDegrees(field: TimePickerField, is24Hour: Boolean): Double = when (field) {
    TimePickerField.Hour -> if (is24Hour) 15.0 else 30.0
    TimePickerField.Minute -> 6.0
}

/** The dial position the hand should point at for the current value. */
private fun selectionFor(hour: Int, minute: Int, field: TimePickerField, is24Hour: Boolean): Int =
    when (field) {
        TimePickerField.Hour -> if (is24Hour) hour % 24 else hour % 12
        TimePickerField.Minute -> minute
    }

/** Converts dp to px inside a draw scope. */
private fun androidx.compose.ui.graphics.drawscope.DrawScope.strokePx(dp: Float): Float =
    dp * density
