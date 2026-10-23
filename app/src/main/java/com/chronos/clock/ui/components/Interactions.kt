package com.chronos.clock.ui.components

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback

/**
 * Expressive press feedback: every interactive surface in the design shrinks slightly
 * under the finger with a spring, and chips additionally fire a haptic tick.
 */
@Immutable
class PressState(
    val interaction: MutableInteractionSource,
    val isPressed: Boolean,
    val hapticTick: () -> Unit,
)

@Composable
fun rememberPressState(
    onPress: (() -> Unit)? = null,
): PressState {
    val interaction = remember { MutableInteractionSource() }
    val isPressed by interaction.collectIsPressedAsState()
    val haptics = LocalHapticFeedback.current
    return remember(interaction, isPressed, onPress) {
        PressState(
            interaction = interaction,
            isPressed = isPressed,
            hapticTick = {
                haptics.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                onPress?.invoke()
            },
        )
    }
}

/**
 * Scales the receiver down while it is pressed, then springs back.
 */
@Composable
fun Modifier.pressScale(
    interaction: MutableInteractionSource,
    pressedScale: Float = 0.95f,
    enabled: Boolean = true,
): Modifier {
    val pressed by interaction.collectIsPressedAsState()
    val scale by animateFloatAsState(
        targetValue = if (pressed && enabled) pressedScale else 1f,
        animationSpec = spring(dampingRatio = 0.55f, stiffness = 700f),
        label = "pressScale",
    )
    return this.graphicsLayer {
        scaleX = scale
        scaleY = scale
    }
}
