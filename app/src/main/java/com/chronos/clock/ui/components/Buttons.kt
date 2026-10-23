package com.chronos.clock.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.chronos.clock.ui.theme.ChronosColors
import com.chronos.clock.ui.theme.ChronosMotion
import com.chronos.clock.ui.theme.ChronosRadius
import com.chronos.clock.ui.theme.ChronosType

/**
 * The circular secondary action button used for reset / cancel / lap.
 */
@Composable
fun ChronosActionButton(
    icon: String,
    contentDescription: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    size: Dp = 56.dp,
    iconSize: Dp = 24.dp,
    container: Color = ChronosColors.SurfaceContainerHigh,
    content: Color = ChronosColors.OnSurface,
    enabled: Boolean = true,
) {
    val press = rememberPressState()
    val containerColor by animateColorAsState(
        targetValue = if (press.isPressed) ChronosColors.SurfaceBright else container,
        animationSpec = tween(ChronosMotion.Short2),
        label = "actionContainer",
    )
    Box(
        modifier = modifier
            .size(size)
            .pressScale(press.interaction, pressedScale = 0.90f, enabled = enabled)
            .shadow(
                elevation = 4.dp,
                shape = CircleShape,
                ambientColor = Color.Black,
                spotColor = Color.Black,
            )
            .clip(CircleShape)
            .background(containerColor)
            .clickable(
                interactionSource = press.interaction,
                indication = null,
                enabled = enabled,
                role = Role.Button,
                onClick = {
                    press.hapticTick()
                    onClick()
                },
            ),
        contentAlignment = Alignment.Center,
    ) {
        ChronosIcon(
            name = icon,
            size = iconSize,
            tint = if (enabled) content else ChronosColors.Outline.copy(alpha = 0.4f),
        )
    }
}

/**
 * The 64dp squircle FAB from the design system, with the dual warm ambient shadow.
 */
@Composable
fun ChronosFab(
    icon: String,
    contentDescription: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    size: Dp = 64.dp,
    iconSize: Dp = 30.dp,
    container: Color = ChronosColors.PrimaryContainer,
    content: Color = ChronosColors.OnPrimaryContainer,
    rotateOnTap: Boolean = false,
) {
    val press = rememberPressState()
    val rotation by animateFloatAsState(
        targetValue = if (rotateOnTap && press.isPressed) 90f else 0f,
        animationSpec = spring(dampingRatio = 0.6f, stiffness = 400f),
        label = "fabRotation",
    )
    Box(
        modifier = modifier
            .size(size)
            .pressScale(press.interaction, pressedScale = 0.92f)
            .shadow(
                elevation = 12.dp,
                shape = RoundedCornerShape(ChronosRadius.Fab),
                ambientColor = ChronosColors.PrimaryContainer.copy(alpha = 0.10f),
                spotColor = Color.Black.copy(alpha = 0.7f),
            )
            .clip(RoundedCornerShape(ChronosRadius.Fab))
            .background(container)
            .clickable(
                interactionSource = press.interaction,
                indication = null,
                role = Role.Button,
                onClick = {
                    press.hapticTick()
                    onClick()
                },
            ),
        contentAlignment = Alignment.Center,
    ) {
        ChronosIcon(
            name = icon,
            size = iconSize,
            tint = content,
            modifier = Modifier.rotate(rotation),
        )
    }
}

/**
 * A compact pill chip — used for the quick-add time buttons, alarm sound chips and
 * the day-of-week selectors.
 */
@Composable
fun ChronosPill(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    leadingIcon: String? = null,
    iconFilled: Boolean = false,
    leadingIconTint: Color = ChronosColors.Primary,
    leadingIconSize: Dp = 16.dp,
    container: Color = ChronosColors.SurfaceContainerHigh,
    contentColor: Color = ChronosColors.OnSurface,
    borderColor: Color = ChronosColors.OutlineVariant.copy(alpha = 0.30f),
    textStyle: androidx.compose.ui.text.TextStyle = ChronosType.LabelMd,
    height: Dp = 36.dp,
    enabled: Boolean = true,
) {
    val press = rememberPressState()
    val containerColor by animateColorAsState(
        targetValue = if (press.isPressed) ChronosColors.SurfaceBright else container,
        animationSpec = tween(ChronosMotion.Short2),
        label = "pillContainer",
    )
    Row(
        modifier = modifier
            .height(height)
            .pressScale(press.interaction, pressedScale = 0.96f, enabled = enabled)
            .clip(RoundedCornerShape(ChronosRadius.Pill))
            .background(containerColor)
            .border(1.dp, borderColor, RoundedCornerShape(ChronosRadius.Pill))
            .clickable(
                interactionSource = press.interaction,
                indication = null,
                enabled = enabled,
                role = Role.Button,
                onClick = {
                    press.hapticTick()
                    onClick()
                },
            )
            .padding(horizontal = 14.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(6.dp),
    ) {
        if (leadingIcon != null) {
            ChronosIcon(
                name = leadingIcon,
                size = leadingIconSize,
                tint = leadingIconTint,
                filled = iconFilled,
            )
        }
        Text(
            text = text,
            style = textStyle,
            color = contentColor,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
        )
    }
}

/**
 * A plain icon button for the top app bar.
 */
@Composable
fun ChronosIconButton(
    icon: String,
    contentDescription: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    size: Dp = 40.dp,
    iconSize: Dp = 22.dp,
    tint: Color = ChronosColors.OnSurface,
    filled: Boolean = false,
) {
    val press = rememberPressState()
    val container by animateColorAsState(
        targetValue = if (press.isPressed) ChronosColors.SurfaceContainerHigh else Color.Transparent,
        animationSpec = tween(ChronosMotion.Short2),
        label = "iconButtonContainer",
    )
    Box(
        modifier = modifier
            .size(size)
            .pressScale(press.interaction, pressedScale = 0.90f)
            .clip(CircleShape)
            .background(container)
            .clickable(
                interactionSource = press.interaction,
                indication = null,
                role = Role.Button,
                onClick = {
                    press.hapticTick()
                    onClick()
                },
            ),
        contentAlignment = Alignment.Center,
    ) {
        ChronosIcon(
            name = icon,
            size = iconSize,
            tint = tint,
            filled = filled,
        )
    }
}

/**
 * A pill-shaped navigation-style button used for "Add City", "Compare Times",
 * "New preset" and "Share Laps".
 */
@Composable
fun ChronosTextButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    leadingIcon: String? = null,
    container: Color = ChronosColors.SurfaceContainerHigh,
    contentColor: Color = ChronosColors.OnSurface,
    horizontalPadding: Dp = 20.dp,
) {
    val press = rememberPressState()
    val containerColor by animateColorAsState(
        targetValue = if (press.isPressed) ChronosColors.SurfaceBright else container,
        animationSpec = tween(ChronosMotion.Short2),
        label = "textButtonContainer",
    )
    Row(
        modifier = modifier
            .pressScale(press.interaction, pressedScale = 0.95f)
            .clip(RoundedCornerShape(ChronosRadius.Pill))
            .background(containerColor)
            .clickable(
                interactionSource = press.interaction,
                indication = null,
                role = Role.Button,
                onClick = {
                    press.hapticTick()
                    onClick()
                },
            )
            .defaultMinSize(minHeight = 44.dp)
            .padding(horizontal = horizontalPadding, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        if (leadingIcon != null) {
            ChronosIcon(
                name = leadingIcon,
                size = 18.dp,
                tint = contentColor,
            )
        }
        Text(text = text, style = ChronosType.LabelLg, color = contentColor, maxLines = 1)
    }
}

/** A shape helper so components can swap between a circle and a squircle. */
val CircleClip: Shape = CircleShape
