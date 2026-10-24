package com.chronos.clock.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.chronos.clock.ui.theme.ChronosColors
import com.chronos.clock.ui.theme.ChronosRadius
import com.chronos.clock.ui.theme.ChronosType

/**
 * Tier-1 surface: a standard non-elevated card with the 1px interior ghost outline
 * that keeps its edge defined against the pitch canvas.
 */
@Composable
fun GhostCard(
    modifier: Modifier = Modifier,
    container: Color = ChronosColors.SurfaceContainerLow,
    borderColor: Color = ChronosColors.GhostBorder,
    borderWidth: Dp = 1.dp,
    radius: Dp = ChronosRadius.Card,
    content: @Composable androidx.compose.foundation.layout.ColumnScope.() -> Unit,
) {
    Column(
        modifier = modifier
            .clip(RoundedCornerShape(radius))
            .background(container)
            .border(borderWidth, borderColor, RoundedCornerShape(radius))
            .padding(16.dp),
        content = content,
    )
}

/**
 * The shared top app bar. Every screen in the design keeps the same anchor:
 * a primary-coloured alarm glyph, the `headline-sm` "Clock" title, and trailing
 * actions on the right.
 */
@Composable
fun ChronosTopBar(
    modifier: Modifier = Modifier,
    title: String = "Clock",
    onLeadingClick: (() -> Unit)? = null,
    leadingIcon: String = "alarm",
    leadingTint: Color = MaterialTheme.colorScheme.primary,
    searchQuery: String? = null,
    onSearchQueryChange: ((String) -> Unit)? = null,
    onSearchClose: (() -> Unit)? = null,
    actions: @Composable RowScope.() -> Unit = {},
) {
    val searching = searchQuery != null

    Row(
        modifier = modifier
            .fillMaxWidth()
            .background(ChronosColors.Surface)
            .statusBarsPadding()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        if (searching) {
            Row(
                modifier = Modifier
                    .weight(1f)
                    .height(44.dp)
                    .clip(RoundedCornerShape(ChronosRadius.Pill))
                    .background(ChronosColors.SurfaceContainerLow)
                    .padding(horizontal = 14.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                ChronosIcon(
                    name = "search",
                    size = 20.dp,
                    tint = ChronosColors.Outline,
                )
                Spacer(Modifier.width(10.dp))
                androidx.compose.foundation.text.BasicTextField(
                    value = searchQuery.orEmpty(),
                    onValueChange = { onSearchQueryChange?.invoke(it) },
                    singleLine = true,
                    textStyle = ChronosType.BodyLg.copy(color = ChronosColors.OnSurface),
                    cursorBrush = androidx.compose.ui.graphics.SolidColor(
                        MaterialTheme.colorScheme.primaryContainer,
                    ),
                    modifier = Modifier.weight(1f),
                    decorationBox = { inner ->
                        if (searchQuery.orEmpty().isEmpty()) {
                            Text(
                                text = "Search cities",
                                style = ChronosType.BodyLg,
                                color = ChronosColors.Outline,
                            )
                        }
                        inner()
                    },
                )
            }
            Spacer(Modifier.width(4.dp))
            ChronosIconButton(
                icon = "close",
                contentDescription = "Close search",
                onClick = { onSearchClose?.invoke() },
                tint = ChronosColors.OnSurface,
            )
        } else {
            ChronosIconButton(
                icon = leadingIcon,
                contentDescription = leadingIcon,
                onClick = { onLeadingClick?.invoke() },
                tint = leadingTint,
            )
            Text(
                text = title,
                style = ChronosType.HeadlineSm,
                color = ChronosColors.OnSurface,
                modifier = Modifier
                    .padding(start = 8.dp)
                    .weight(1f),
            )
            Row(
                horizontalArrangement = Arrangement.spacedBy(2.dp),
                verticalAlignment = Alignment.CenterVertically,
                content = actions,
            )
        }
    }
}

/**
 * A circular icon well — the ambient badge that fronts list rows and the
 * "upcoming alarm" banner.
 */
@Composable
fun IconWell(
    icon: String,
    modifier: Modifier = Modifier,
    size: Dp = 40.dp,
    iconSize: Dp = 20.dp,
    container: Color = MaterialTheme.colorScheme.secondaryContainer,
    content: Color = MaterialTheme.colorScheme.primaryContainer,
    filled: Boolean = false,
) {
    Box(
        modifier = modifier
            .size(size)
            .clip(RoundedCornerShape(size / 2))
            .background(container),
        contentAlignment = Alignment.Center,
    ) {
        ChronosIcon(
            name = icon,
            size = iconSize,
            tint = content,
            filled = filled,
        )
    }
}
