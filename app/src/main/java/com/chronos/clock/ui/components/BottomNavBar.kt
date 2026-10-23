package com.chronos.clock.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.windowInsetsBottomHeight
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.chronos.clock.ui.ChronosDestination
import com.chronos.clock.ui.theme.ChronosColors
import com.chronos.clock.ui.theme.ChronosDimens
import com.chronos.clock.ui.theme.ChronosRadius
import com.chronos.clock.ui.theme.ChronosType

/**
 * The fluid bottom tab dock.
 *
 * The active state is a filled pill capsule (`#E7BDB1` at reduced opacity over the
 * `#422A23` container tone) encapsulating the active icon in `#FFB782`, accompanied by
 * a `label-md` caption. Inactive tabs are unadorned glyphs in the outline colour.
 *
 * The pill is a single element that *slides* between tabs rather than four separate
 * indicators cross-fading, which is what gives the dock its fluid, kinetic feel.
 */
@Composable
fun ChronosBottomBar(
    selected: ChronosDestination,
    onSelect: (ChronosDestination) -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .background(ChronosColors.SurfaceContainer),
    ) {
    BoxWithConstraints(
        modifier = Modifier
            .fillMaxWidth()
            .height(ChronosDimens.NavBarHeight),
    ) {
        val itemWidth = maxWidth / ChronosDestination.ordered.size
        val pillOffset by animateDpAsState(
            targetValue = itemWidth * selected.order + (itemWidth - ChronosDimens.NavPillWidth) / 2,
            animationSpec = spring(dampingRatio = 0.72f, stiffness = 420f),
            label = "navPillOffset",
        )
        val pillAlpha by animateFloatAsState(
            targetValue = 1f,
            animationSpec = spring(dampingRatio = 0.7f, stiffness = 500f),
            label = "navPillAlpha",
        )

        // The travelling indicator sits behind the tab row.
        Box(
            modifier = Modifier
                .offset(x = pillOffset)
                .padding(top = 14.dp)
                .size(ChronosDimens.NavPillWidth, ChronosDimens.NavPillHeight)
                .scale(1f, pillAlpha)
                .clip(RoundedCornerShape(ChronosDimens.NavPillHeight / 2))
                .background(ChronosColors.SecondaryContainer),
        )

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .fillMaxHeight(),
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            ChronosDestination.ordered.forEach { destination ->
                ChronosNavItem(
                    destination = destination,
                    selected = destination == selected,
                    onClick = { onSelect(destination) },
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxHeight(),
                )
            }
        }
    }
    Spacer(
        modifier = Modifier.windowInsetsBottomHeight(WindowInsets.navigationBars),
    )
    }
}

@Composable
private fun ChronosNavItem(
    destination: ChronosDestination,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val press = rememberPressState()
    val interaction: MutableInteractionSource = press.interaction

    val iconColor by animateColorAsState(
        targetValue = if (selected) ChronosColors.PrimaryContainer else ChronosColors.Outline,
        animationSpec = spring(dampingRatio = 0.7f, stiffness = 500f),
        label = "navIconColor",
    )
    val labelColor by animateColorAsState(
        targetValue = if (selected) ChronosColors.PrimaryContainer else ChronosColors.Outline,
        animationSpec = spring(dampingRatio = 0.7f, stiffness = 500f),
        label = "navLabelColor",
    )
    val iconScale by animateFloatAsState(
        targetValue = if (selected) 1.06f else 1f,
        animationSpec = spring(dampingRatio = 0.55f, stiffness = 480f),
        label = "navIconScale",
    )
    val labelWeight = if (selected) {
        androidx.compose.ui.text.font.FontWeight.Medium
    } else {
        androidx.compose.ui.text.font.FontWeight.Medium
    }

    Column(
        modifier = modifier
            .pressScale(interaction, pressedScale = 0.92f)
            .clickable(
                interactionSource = interaction,
                indication = null,
                role = Role.Tab,
                onClick = {
                    press.hapticTick()
                    onClick()
                },
            ),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        Box(
            modifier = Modifier.size(24.dp, 24.dp),
            contentAlignment = Alignment.Center,
        ) {
            ChronosIcon(
                name = destination.icon,
                size = 24.dp,
                tint = iconColor,
                filled = selected,
                modifier = Modifier.scale(iconScale),
            )
        }
        Text(
            text = destination.label,
            style = ChronosType.LabelMd,
            color = labelColor,
            fontWeight = labelWeight,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(top = 4.dp),
        )
    }
}

/** Kept for symmetry with the design's `--rounded-full` token. */
internal val NavPillShape = RoundedCornerShape(ChronosRadius.Pill)
internal val Transparent = Color.Transparent
