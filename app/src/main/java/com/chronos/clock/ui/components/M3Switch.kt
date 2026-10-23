package com.chronos.clock.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.stateDescription
import androidx.compose.ui.semantics.toggleableState
import androidx.compose.ui.state.ToggleableState
import androidx.compose.ui.unit.dp
import com.chronos.clock.ui.theme.ChronosColors
import com.chronos.clock.ui.theme.ChronosDimens

/**
 * Material 3 switch, built to the spec in the design system:
 *
 * - Track: 52x32dp pill. Active `#FFB782`; inactive `#282A2C` with a 2dp `#484B50` stroke.
 * - Thumb: 24dp circle. Active `#4D2700` carrying a check glyph; inactive `#8D9199`.
 * - The thumb contracts slightly under touch-down.
 */
@Composable
fun M3Switch(
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
) {
    val press = rememberPressState()

    val trackColor by animateColorAsState(
        targetValue = if (checked) ChronosColors.PrimaryContainer else ChronosColors.SurfaceContainerHigh,
        animationSpec = spring(dampingRatio = 0.7f, stiffness = 500f),
        label = "switchTrack",
    )
    val borderColor by animateColorAsState(
        targetValue = if (checked) Color.Transparent else Color(0xFF484B50),
        animationSpec = spring(dampingRatio = 0.7f, stiffness = 500f),
        label = "switchBorder",
    )
    val borderWidth by animateDpAsState(
        targetValue = if (checked) 0.dp else 2.dp,
        animationSpec = spring(dampingRatio = 0.7f, stiffness = 500f),
        label = "switchBorderWidth",
    )
    val thumbOffset by animateFloatAsState(
        targetValue = if (checked) 1f else 0f,
        animationSpec = spring(dampingRatio = 0.65f, stiffness = 420f),
        label = "switchThumb",
    )
    val thumbColor by animateColorAsState(
        targetValue = if (checked) ChronosColors.OnPrimary else ChronosColors.Outline,
        animationSpec = spring(dampingRatio = 0.7f, stiffness = 500f),
        label = "switchThumbColor",
    )
    val thumbScale by animateFloatAsState(
        targetValue = if (press.isPressed) 0.88f else 1f,
        animationSpec = spring(dampingRatio = 0.5f, stiffness = 800f),
        label = "switchThumbScale",
    )

    Box(
        modifier = modifier
            .size(ChronosDimens.SwitchWidth, ChronosDimens.SwitchHeight)
            .clip(RoundedCornerShape(ChronosDimens.SwitchHeight / 2))
            .background(trackColor)
            .border(borderWidth, borderColor, RoundedCornerShape(ChronosDimens.SwitchHeight / 2))
            .clickable(
                interactionSource = press.interaction,
                indication = null,
                enabled = enabled,
                role = Role.Switch,
                onClick = {
                    press.hapticTick()
                    onCheckedChange(!checked)
                },
            )
            .semantics {
                this.toggleableState = if (checked) ToggleableState.On else ToggleableState.Off
                this.stateDescription = if (checked) "On" else "Off"
            },
        contentAlignment = Alignment.CenterStart,
    ) {
        Box(
            modifier = Modifier
                .padding(start = 4.dp)
                .offset(x = (20 * thumbOffset).dp)
                .graphicsLayer {
                    scaleX = thumbScale
                    scaleY = thumbScale
                }
                .size(ChronosDimens.SwitchThumbSize)
                .clip(CircleShape)
                .background(thumbColor),
            contentAlignment = Alignment.Center,
        ) {
            if (checked) {
                ChronosIcon(
                    name = "check",
                    size = 14.dp,
                    tint = ChronosColors.PrimaryContainer,
                )
            }
        }
    }
}
