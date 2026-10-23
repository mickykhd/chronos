package com.chronos.clock.ui.components

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.AnimationVector1D
import androidx.compose.animation.core.tween
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.chronos.clock.ui.theme.ChronosMotion
import kotlinx.coroutines.delay

/**
 * A one-shot entrance animation: content fades up and settles into place.
 *
 * Screens stagger their sections with [step] so the layout assembles itself in
 * sequence rather than appearing all at once.
 */
class RevealState internal constructor(
    private val progress: Animatable<Float, AnimationVector1D>,
) {
    val value: Float get() = progress.value
}

@Composable
fun rememberRevealState(
    index: Int = 0,
    step: Int = 55,
    durationMillis: Int = ChronosMotion.Medium2,
    enabled: Boolean = true,
): RevealState {
    val progress = remember { Animatable(if (enabled) 0f else 1f) }
    androidx.compose.runtime.LaunchedEffect(index, enabled) {
        if (!enabled) {
            progress.snapTo(1f)
            return@LaunchedEffect
        }
        delay(index * step.toLong())
        progress.animateTo(
            targetValue = 1f,
            animationSpec = tween(
                durationMillis = durationMillis,
                easing = ChronosMotion.EmphasizedDecelerate,
            ),
        )
    }
    return RevealState(progress)
}

/**
 * Fades and lifts its content into place. [offset] is the travel distance in dp.
 */
fun Modifier.reveal(
    state: RevealState,
    offset: Dp = 24.dp,
): Modifier = this.graphicsLayer {
    val p = state.value
    alpha = p
    translationY = (1f - p) * offset.toPx()
}
