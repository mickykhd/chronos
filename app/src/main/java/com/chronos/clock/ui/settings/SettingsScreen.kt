package com.chronos.clock.ui.settings

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.chronos.clock.ui.components.ChronosIcon
import com.chronos.clock.ui.components.ChronosIconButton
import com.chronos.clock.ui.components.M3Switch
import com.chronos.clock.ui.theme.ChronosColors
import com.chronos.clock.ui.theme.ChronosRadius
import com.chronos.clock.ui.theme.ChronosTheme
import com.chronos.clock.ui.theme.ChronosType

/**
 * App settings: the colour theme, the clock format and a little about the app.
 */
@Composable
fun SettingsScreen(
    theme: ChronosTheme,
    use24Hour: Boolean,
    onThemeChange: (ChronosTheme) -> Unit,
    onUse24HourChange: (Boolean) -> Unit,
    onClose: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .background(ChronosColors.Surface)
            .statusBarsPadding(),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 8.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            ChronosIconButton(
                icon = "arrow_back",
                contentDescription = "Back",
                onClick = onClose,
                tint = ChronosColors.OnSurface,
            )
            Text(
                text = "Settings",
                style = ChronosType.HeadlineSm,
                color = ChronosColors.OnSurface,
                modifier = Modifier
                    .padding(start = 8.dp)
                    .weight(1f),
            )
        }

        Column(
            modifier = Modifier
                .weight(1f)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 16.dp),
        ) {
            SettingsGroup(title = "Colour theme") {
                ChronosTheme.entries.forEach { option ->
                    ThemeRow(
                        theme = option,
                        selected = option == theme,
                        onClick = { onThemeChange(option) },
                    )
                }
            }

            SettingsGroup(title = "Clock") {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 10.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "24-hour format",
                            style = ChronosType.BodyLg,
                            color = ChronosColors.OnSurface,
                        )
                        Text(
                            text = if (use24Hour) "Times show as 18:42" else "Times show as 6:42 PM",
                            style = ChronosType.BodyMd,
                            color = ChronosColors.Outline,
                        )
                    }
                    M3Switch(checked = use24Hour, onCheckedChange = onUse24HourChange)
                }
            }

            SettingsGroup(title = "About") {
                AboutRow("Chronos", "Version 1.1.0")
                AboutRow("Design system", "Chronos M3")
                AboutRow("Weather", "Open-Meteo")
                AboutRow("Licence", "MIT")
            }

            Spacer(Modifier.height(32.dp))
        }
    }
}

@Composable
private fun SettingsGroup(
    title: String,
    content: @Composable () -> Unit,
) {
    Column(modifier = Modifier.padding(top = 16.dp)) {
        Text(
            text = title.uppercase(),
            style = ChronosType.Eyebrow,
            color = MaterialTheme.colorScheme.primaryContainer,
            fontWeight = FontWeight.SemiBold,
        )
        Column(
            modifier = Modifier
                .padding(top = 8.dp)
                .clip(RoundedCornerShape(ChronosRadius.Card))
                .background(ChronosColors.SurfaceContainerLow)
                .border(1.dp, ChronosColors.GhostBorder, RoundedCornerShape(ChronosRadius.Card))
                .padding(horizontal = 16.dp, vertical = 8.dp),
            content = { content() },
        )
    }
}

@Composable
private fun ThemeRow(
    theme: ChronosTheme,
    selected: Boolean,
    onClick: () -> Unit,
) {
    val container by animateColorAsState(
        targetValue = if (selected) {
            MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.12f)
        } else {
            Color.Transparent
        },
        animationSpec = spring(dampingRatio = 0.8f, stiffness = 420f),
        label = "themeRowContainer",
    )

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(container)
            .clickable(onClick = onClick)
            .padding(horizontal = 12.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        // Swatch preview of the theme's three accent roles.
        Row(horizontalArrangement = Arrangement.spacedBy(0.dp)) {
            Swatch(theme.accents.primaryContainer, size = 26.dp, offset = 0.dp)
            Swatch(theme.accents.secondaryContainer, size = 26.dp, offset = (-8).dp)
            Swatch(theme.accents.tertiaryContainer, size = 26.dp, offset = (-16).dp)
        }
        Spacer(Modifier.width(20.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = theme.label,
                style = ChronosType.BodyLg,
                color = if (selected) {
                    MaterialTheme.colorScheme.primaryContainer
                } else {
                    ChronosColors.OnSurface
                },
                fontWeight = if (selected) FontWeight.SemiBold else FontWeight.Normal,
            )
            Text(
                text = theme.blurb,
                style = ChronosType.BodyMd,
                color = ChronosColors.Outline,
            )
        }
        if (selected) {
            ChronosIcon(
                name = "check",
                size = 20.dp,
                tint = MaterialTheme.colorScheme.primaryContainer,
            )
        }
    }
}

@Composable
private fun Swatch(color: Color, size: androidx.compose.ui.unit.Dp, offset: androidx.compose.ui.unit.Dp) {
    Box(
        modifier = Modifier
            .offset(x = offset)
            .size(size)
            .clip(CircleShape)
            .background(color)
            .border(1.dp, ChronosColors.GhostBorder, CircleShape),
    )
}

@Composable
private fun AboutRow(label: String, value: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            text = label,
            style = ChronosType.BodyLg,
            color = ChronosColors.OnSurface,
            modifier = Modifier.weight(1f),
        )
        Text(
            text = value,
            style = ChronosType.BodyMd,
            color = ChronosColors.Outline,
        )
    }
}


