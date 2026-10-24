package com.chronos.clock.ui.clock

import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.chronos.clock.core.SunTimes
import com.chronos.clock.core.Weather
import com.chronos.clock.core.ZoneDescription
import com.chronos.clock.core.formatCoarseDuration
import com.chronos.clock.core.rememberNow
import com.chronos.clock.data.WorldCity
import com.chronos.clock.ui.components.AnalogClock
import com.chronos.clock.ui.components.ChronosFab
import com.chronos.clock.ui.components.ChronosIcon
import com.chronos.clock.ui.components.ChronosIconButton
import com.chronos.clock.ui.components.ChronosOverflowMenu
import com.chronos.clock.ui.components.ChronosPill
import com.chronos.clock.ui.components.IconWell
import com.chronos.clock.ui.components.OverflowItem
import com.chronos.clock.ui.components.ChronosTextButton
import com.chronos.clock.ui.components.ChronosTopBar
import com.chronos.clock.ui.components.rememberRevealState
import com.chronos.clock.ui.components.reveal
import com.chronos.clock.ui.theme.ChronosColors
import com.chronos.clock.ui.theme.ChronosMotion
import com.chronos.clock.ui.theme.ChronosRadius
import com.chronos.clock.ui.theme.ChronosType
import java.time.LocalDate
import java.time.ZoneId
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter
import java.time.temporal.ChronoUnit
import kotlin.math.ceil
import kotlin.math.floor

private val HeroTimeFormat: DateTimeFormatter = DateTimeFormatter.ofPattern("h:mm")
private val HeroTime24Format: DateTimeFormatter = DateTimeFormatter.ofPattern("HH:mm")
private val CityTimeFormat: DateTimeFormatter = DateTimeFormatter.ofPattern("hh:mm")
private val CityTime24Format: DateTimeFormatter = DateTimeFormatter.ofPattern("HH:mm")
private val DateFormat: DateTimeFormatter = DateTimeFormatter.ofPattern("EEE, MMM d")

@Composable
@OptIn(ExperimentalFoundationApi::class)
fun ClockScreen(
    viewModel: ClockViewModel,
    onAddCity: () -> Unit,
    onCompareTimes: () -> Unit,
    onOpenSettings: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val now by rememberNow(viewModel.localZone)
    val cities by viewModel.cities.collectAsStateWithLifecycle()
    val use24Hour by viewModel.use24Hour.collectAsStateWithLifecycle()

    var weather by remember { mutableStateOf<Map<String, Weather.Current>>(emptyMap()) }
    LaunchedEffect(cities) {
        val zones = listOf(viewModel.localZone) + cities.map { ZoneId.of(it.zoneId) }
        weather = Weather.currentFor(zones)
    }

    ClockScreenContent(
        now = now,
        zoneDescription = viewModel.zoneDescription,
        sunTimes = viewModel.sunTimes,
        cities = cities,
        weather = weather,
        localZone = viewModel.localZone,
        use24Hour = use24Hour,
        onAddCity = onAddCity,
        onCompareTimes = onCompareTimes,
        onOpenSettings = onOpenSettings,
        modifier = modifier,
    )
}

@Composable
private fun ClockScreenContent(
    now: ZonedDateTime,
    zoneDescription: ZoneDescription,
    sunTimes: SunTimes.Result,
    cities: List<WorldCity>,
    weather: Map<String, Weather.Current>,
    localZone: ZoneId,
    use24Hour: Boolean,
    onAddCity: () -> Unit,
    onCompareTimes: () -> Unit,
    onOpenSettings: () -> Unit,
    modifier: Modifier = Modifier,
) {
    var searchActive by remember { mutableStateOf(false) }
    var query by remember { mutableStateOf("") }
    var overflowOpen by remember { mutableStateOf(false) }

    val visibleCities = remember(cities, query) {
        if (query.isBlank()) {
            cities
        } else {
            cities.filter {
                it.city.contains(query, ignoreCase = true) ||
                    it.country.contains(query, ignoreCase = true)
            }
        }
    }
    BoxWithConstraints(modifier = modifier.fillMaxSize()) {
        val isWide = maxWidth >= 600.dp

        LazyColumn(
            modifier = Modifier.fillMaxWidth(),
            // Extra clearance so the last card can scroll clear of the floating
            // action button rather than being trapped underneath it.
            contentPadding = PaddingValues(bottom = 200.dp),
            verticalArrangement = Arrangement.spacedBy(24.dp),
        ) {
            stickyHeader(key = "topbar") {
                ChronosTopBar(
                    searchQuery = if (searchActive) query else null,
                    onSearchQueryChange = { query = it },
                    onSearchClose = {
                        searchActive = false
                        query = ""
                    },
                    actions = {
                        ChronosIconButton(
                            icon = "search",
                            contentDescription = "Search cities",
                            onClick = { searchActive = true },
                            tint = ChronosColors.Outline,
                        )
                        Box {
                            ChronosIconButton(
                                icon = "more_vert",
                                contentDescription = "More options",
                                onClick = { overflowOpen = true },
                                tint = ChronosColors.Outline,
                            )
                            ChronosOverflowMenu(
                                expanded = overflowOpen,
                                onDismiss = { overflowOpen = false },
                                items = listOf(
                                    OverflowItem("Add city", "add") { onAddCity() },
                                    OverflowItem("Compare times", "compare_arrows", onCompareTimes),
                                    OverflowItem("Settings", "settings", onOpenSettings),
                                ),
                            )
                        }
                    },
                )
            }

            item(key = "hero") {
                val reveal = rememberRevealState(index = 0)
                BoxWithConstraints(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp)
                        .reveal(reveal),
                ) {
                    if (isWide) {
                        Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                            HeroCard(
                                now = now,
                                zoneDescription = zoneDescription,
                                sunTimes = sunTimes,
                                weather = weather[localZone.id],
                                use24Hour = use24Hour,
                                modifier = Modifier.weight(7f),
                            )
                            AnalogCard(
                                modifier = Modifier.weight(5f),
                            )
                        }
                    } else {
                        Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                            HeroCard(
                                now = now,
                                zoneDescription = zoneDescription,
                                sunTimes = sunTimes,
                                weather = weather[localZone.id],
                                use24Hour = use24Hour,
                                modifier = Modifier.fillMaxWidth(),
                            )
                            AnalogCard(modifier = Modifier.fillMaxWidth())
                        }
                    }
                }
            }

            item(key = "world-header") {
                val reveal = rememberRevealState(index = 1)
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp)
                        .reveal(reveal),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "World Clocks",
                            style = ChronosType.TitleLg,
                            color = ChronosColors.OnSurface,
                        )
                        Text(
                            text = when (cities.size) {
                                0 -> "No cities pinned yet"
                                1 -> "1 pinned international hub"
                                else -> "${cities.size} pinned international hubs"
                            },
                            style = ChronosType.BodyMd,
                            color = ChronosColors.Outline,
                        )
                    }
                    if (isWide) {
                        ChronosTextButton(
                            text = "Add City",
                            leadingIcon = "add",
                            onClick = onAddCity,
                        )
                    }
                }
            }

            if (cities.isEmpty()) {
                item(key = "empty-cities") {
                    val reveal = rememberRevealState(index = 2)
                    EmptyCitiesCard(
                        onAddCity = onAddCity,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp)
                            .reveal(reveal),
                    )
                }
            } else if (visibleCities.isEmpty()) {
                item(key = "no-results") {
                    Text(
                        text = "No cities match your search",
                        style = ChronosType.BodyLg,
                        color = ChronosColors.Outline,
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 24.dp),
                    )
                }
            }

            items(visibleCities, key = { it.id }) { city ->
                val reveal = rememberRevealState(index = cities.indexOf(city) + 2)
                WorldClockCard(
                    city = city,
                    now = now,
                    weather = weather[city.zoneId],
                    use24Hour = use24Hour,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp)
                        .reveal(reveal),
                )
            }

            item(key = "compare") {
                val reveal = rememberRevealState(index = cities.size + 2)
                CoordinateStrip(
                    cityCount = cities.size + 1,
                    onClick = onCompareTimes,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp)
                        .reveal(reveal),
                )
            }
        }

        ChronosFab(
            icon = "add",
            contentDescription = "Add city to world clock",
            rotateOnTap = true,
            onClick = onAddCity,
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(end = 20.dp, bottom = 96.dp),
        )

    }
}

@Composable
private fun HeroCard(
    now: ZonedDateTime,
    zoneDescription: ZoneDescription,
    sunTimes: SunTimes.Result,
    weather: Weather.Current?,
    use24Hour: Boolean,
    modifier: Modifier = Modifier,
) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(ChronosRadius.Card))
            .background(ChronosColors.SurfaceContainerLow)
            .border(1.dp, ChronosColors.GhostBorder, RoundedCornerShape(ChronosRadius.Card)),
    ) {
        // Ambient glow pill.
        Box(
            modifier = Modifier
                .align(Alignment.TopEnd)
                .size(192.dp)
                .offset(x = 48.dp, y = (-48).dp)
                .background(
                    Brush.radialGradient(
                        colors = listOf(
                            MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.10f),
                            Color.Transparent,
                        ),
                    ),
                    CircleShape,
                ),
        )

        Column(modifier = Modifier.padding(24.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                InfoPill(
                    icon = "home",
                    text = "Home",
                    filled = true,
                    container = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.40f),
                    content = MaterialTheme.colorScheme.secondary,
                    border = MaterialTheme.colorScheme.secondary.copy(alpha = 0.20f),
                )
                weather?.let {
                    InfoPill(
                        icon = it.icon,
                        text = "${it.temperatureFahrenheit}°F ${it.condition}",
                        filled = true,
                        container = ChronosColors.SurfaceContainerHigh,
                        content = ChronosColors.OnSurfaceVariant,
                        border = Color.Transparent,
                    )
                }
            }

            Row(
                modifier = Modifier.padding(top = 16.dp, bottom = 4.dp),
                verticalAlignment = Alignment.Bottom,
            ) {
                Text(
                    text = now.format(if (use24Hour) HeroTime24Format else HeroTimeFormat),
                    style = ChronosType.DisplayHeroMobile,
                    color = ChronosColors.OnSurface,
                    modifier = Modifier.alignByBaseline(),
                )
                if (!use24Hour) {
                    Spacer(Modifier.width(8.dp))
                    MeridiemBadge(now.hour, Modifier.alignByBaseline())
                }
            }

            Row(
                modifier = Modifier.padding(top = 4.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                ChronosIcon(
                    name = "calendar_today",
                    size = 18.dp,
                    tint = ChronosColors.OnSurfaceVariant,
                )
                Spacer(Modifier.width(8.dp))
                Text(
                    text = now.format(DateFormat),
                    style = ChronosType.BodyLg,
                    color = ChronosColors.OnSurfaceVariant,
                )
                Text(
                    text = "  •  ",
                    style = ChronosType.BodyMd,
                    color = ChronosColors.Outline,
                )
                Text(
                    text = zoneDescription.city,
                    style = ChronosType.BodyMd,
                    color = ChronosColors.Outline,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
            }

            HairlineDivider(modifier = Modifier.padding(top = 16.dp))

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 12.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                ChronosIcon(
                    name = "schedule",
                    size = 16.dp,
                    tint = ChronosColors.Outline,
                )
                Spacer(Modifier.width(6.dp))
                Text(
                    text = zoneDescription.timezoneLine,
                    style = ChronosType.LabelMd,
                    color = ChronosColors.Outline,
                    modifier = Modifier.weight(1f),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
                sunTimes.untilSunrise(now)?.let {
                    Text(
                        text = "Sunrise in ${formatCoarseDuration(it)}",
                        style = ChronosType.LabelMd,
                        color = MaterialTheme.colorScheme.tertiary,
                        fontWeight = FontWeight.Medium,
                    )
                }
            }
        }
    }
}

@Composable
private fun AnalogCard(modifier: Modifier = Modifier) {
    val pulse = rememberInfiniteTransition(label = "liveSync")
    val dotAlpha by pulse.animateFloat(
        initialValue = 0.35f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(1100, easing = ChronosMotion.Standard),
            repeatMode = RepeatMode.Reverse,
        ),
        label = "liveSyncDot",
    )

    Column(
        modifier = modifier
            .clip(RoundedCornerShape(ChronosRadius.Card))
            .background(ChronosColors.SurfaceContainerLow)
            .border(1.dp, ChronosColors.GhostBorder, RoundedCornerShape(ChronosRadius.Card))
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        AnalogClock(size = 176.dp)
        Row(
            modifier = Modifier.padding(top = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Box(
                modifier = Modifier
                    .size(8.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.primaryContainer.copy(alpha = dotAlpha)),
            )
            Spacer(Modifier.width(8.dp))
            Text(
                text = "LIVE SYNC",
                style = ChronosType.LabelSm,
                color = ChronosColors.Outline,
                fontWeight = FontWeight.Medium,
            )
        }
    }
}

@Composable
private fun CoordinateStrip(
    cityCount: Int,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier
            .clip(RoundedCornerShape(ChronosRadius.Card))
            .background(ChronosColors.SurfaceContainerHigh.copy(alpha = 0.60f))
            .border(1.dp, ChronosColors.GhostBorder, RoundedCornerShape(ChronosRadius.Card))
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Box(
            modifier = Modifier
                .size(40.dp)
                .clip(CircleShape)
                .background(MaterialTheme.colorScheme.secondaryContainer),
            contentAlignment = Alignment.Center,
        ) {
            ChronosIcon(
                name = "public",
                size = 22.dp,
                tint = MaterialTheme.colorScheme.primary,
            )
        }
        Spacer(Modifier.width(10.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = "Coordinate Meeting",
                style = ChronosType.TitleMd,
                color = ChronosColors.OnSurface,
                maxLines = 1,
                overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis,
            )
            Text(
                text = "Find overlapping waking hours across $cityCount cities",
                style = ChronosType.BodyMd,
                color = ChronosColors.Outline,
            )
        }
        Spacer(Modifier.width(8.dp))
        ChronosTextButton(
            text = "Compare Times",
            onClick = onClick,
            horizontalPadding = 14.dp,
        )
    }
}

@Composable
private fun WorldClockCard(
    city: WorldCity,
    now: ZonedDateTime,
    weather: Weather.Current?,
    use24Hour: Boolean,
    modifier: Modifier = Modifier,
) {
    val zone = remember(city.zoneId) { ZoneId.of(city.zoneId) }
    val cityTime = now.withZoneSameInstant(zone)
    val dayPart = dayPartOf(cityTime.hour)

    Column(
        modifier = modifier
            .clip(RoundedCornerShape(ChronosRadius.Card))
            .background(ChronosColors.SurfaceContainerLow)
            .border(1.dp, ChronosColors.GhostBorder, RoundedCornerShape(ChronosRadius.Card))
            .padding(16.dp),
    ) {
        Row(verticalAlignment = Alignment.Top) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = city.city,
                    style = ChronosType.TitleLg,
                    color = ChronosColors.OnSurface,
                )
                Text(
                    text = city.country,
                    style = ChronosType.BodyMd,
                    color = ChronosColors.Outline,
                )
            }
            DayNightBadge(
                label = dayPart.label,
                icon = dayPart.icon,
                container = dayPart.container,
                content = dayPart.content,
            )
        }

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 16.dp, bottom = 4.dp),
            verticalAlignment = Alignment.Bottom,
        ) {
            Text(
                text = cityTime.format(if (use24Hour) CityTime24Format else CityTimeFormat),
                style = ChronosType.HeadlineLg,
                color = ChronosColors.OnSurface,
            )
            Spacer(Modifier.weight(1f))
            if (!use24Hour) {
                Text(
                    text = if (cityTime.hour < 12) "AM" else "PM",
                    style = ChronosType.LabelMd,
                    color = ChronosColors.Outline,
                    fontWeight = FontWeight.SemiBold,
                    modifier = Modifier.padding(bottom = 6.dp),
                )
            }
        }

        HairlineDivider(modifier = Modifier.padding(top = 8.dp))

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                text = offsetLabel(now, cityTime),
                style = ChronosType.LabelMd,
                color = MaterialTheme.colorScheme.secondary,
            )
            Spacer(Modifier.weight(1f))
            weather?.let {
                ChronosIcon(
                    name = it.icon,
                    size = 16.dp,
                    tint = ChronosColors.Outline,
                )
                Spacer(Modifier.width(4.dp))
                Text(
                    text = "${it.temperatureLabel} ${it.condition}",
                    style = ChronosType.LabelMd,
                    color = ChronosColors.OnSurfaceVariant,
                )
            }
        }
    }
}

/**
 * Shown when no cities are pinned yet — the app starts clean, so the list is only
 * populated by the user.
 */
@Composable
private fun EmptyCitiesCard(
    onAddCity: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .clip(RoundedCornerShape(ChronosRadius.Card))
            .background(ChronosColors.SurfaceContainerLow)
            .border(1.dp, ChronosColors.GhostBorder, RoundedCornerShape(ChronosRadius.Card))
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        IconWell(
            icon = "public",
            size = 56.dp,
            iconSize = 28.dp,
            container = MaterialTheme.colorScheme.secondaryContainer,
            content = MaterialTheme.colorScheme.primary,
        )
        Spacer(Modifier.height(16.dp))
        Text(
            text = "No world clocks yet",
            style = ChronosType.TitleLg,
            color = ChronosColors.OnSurface,
        )
        Spacer(Modifier.height(4.dp))
        Text(
            text = "Pin a city to see the local time, weather and day/night there.",
            style = ChronosType.BodyMd,
            color = ChronosColors.Outline,
            textAlign = TextAlign.Center,
        )
        Spacer(Modifier.height(20.dp))
        ChronosPill(
            text = "Add a city",
            leadingIcon = "add",
            leadingIconTint = MaterialTheme.colorScheme.onPrimaryContainer,
            container = MaterialTheme.colorScheme.primaryContainer,
            contentColor = MaterialTheme.colorScheme.onPrimaryContainer,
            textStyle = ChronosType.LabelLg,
            height = 48.dp,
            onClick = onAddCity,
        )
    }
}

/** The 1px interior hairline the design uses to separate card regions. */
@Composable
private fun HairlineDivider(modifier: Modifier = Modifier) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(1.dp)
            .background(ChronosColors.OnSurface.copy(alpha = 0.05f)),
    )
}

@Composable
private fun DayNightBadge(
    label: String,
    icon: String,
    container: Color,
    content: Color,
) {
    Row(
        modifier = Modifier
            .clip(RoundedCornerShape(ChronosRadius.Pill))
            .background(container)
            .padding(horizontal = 10.dp, vertical = 3.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        ChronosIcon(name = icon, size = 14.dp, tint = content)
        Spacer(Modifier.width(4.dp))
        Text(
            text = label,
            style = ChronosType.LabelSm,
            color = content,
        )
    }
}

@Composable
private fun InfoPill(
    icon: String,
    text: String,
    filled: Boolean,
    container: Color,
    content: Color,
    border: Color,
) {
    Row(
        modifier = Modifier
            .clip(RoundedCornerShape(ChronosRadius.Pill))
            .background(container)
            .border(1.dp, border, RoundedCornerShape(ChronosRadius.Pill))
            .padding(horizontal = 12.dp, vertical = 5.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        ChronosIcon(name = icon, size = 16.dp, tint = content, filled = filled)
        Spacer(Modifier.width(6.dp))
        Text(
            text = text,
            style = ChronosType.LabelMd,
            color = content,
            maxLines = 1,
        )
    }
}

@Composable
private fun MeridiemBadge(hour: Int, modifier: Modifier = Modifier) {
    Text(
        text = if (hour < 12) "AM" else "PM",
        style = ChronosType.LabelMd,
        color = MaterialTheme.colorScheme.onPrimaryContainer,
        fontWeight = FontWeight.Bold,
        modifier = modifier
            .clip(RoundedCornerShape(ChronosRadius.Pill))
            .background(MaterialTheme.colorScheme.primaryContainer)
            .padding(horizontal = 8.dp, vertical = 2.dp),
    )
}

private data class DayPart(
    val label: String,
    val icon: String,
    val container: Color,
    val content: Color,
)

@Composable
private fun dayPartOf(hour: Int): DayPart {
    val scheme = MaterialTheme.colorScheme
    return when (hour) {
        in 0..4 -> DayPart("Night", "bedtime", scheme.surfaceContainerHighest, scheme.onSurfaceVariant)
        in 5..7 -> DayPart("Dawn", "wb_twilight", scheme.secondaryContainer.copy(alpha = 0.30f), scheme.secondary)
        in 8..16 -> DayPart("Day", "wb_sunny", scheme.tertiaryContainer.copy(alpha = 0.20f), scheme.tertiary)
        in 17..21 -> DayPart("Evening", "wb_twilight", scheme.secondaryContainer.copy(alpha = 0.30f), scheme.secondary)
        else -> DayPart("Night", "bedtime", scheme.surfaceContainerHighest, scheme.onSurfaceVariant)
    }
}

/** `Today, +8 hrs` / `Tomorrow, +16 hrs` */
private fun offsetLabel(now: ZonedDateTime, cityTime: ZonedDateTime): String {
    val localOffset = now.offset.totalSeconds
    val cityOffset = cityTime.offset.totalSeconds
    val rawHours = (cityOffset - localOffset) / 3600.0
    val hours = if (rawHours > 0) ceil(rawHours).toInt() else floor(rawHours).toInt()

    val dayDelta = ChronoUnit.DAYS.between(
        LocalDate.ofInstant(now.toInstant(), now.zone),
        LocalDate.ofInstant(cityTime.toInstant(), cityTime.zone),
    )

    return when {
        dayDelta > 0 -> "Tomorrow, +$hours hrs"
        dayDelta < 0 -> "Yesterday, $hours hrs"
        hours > 0 -> "Today, +$hours hrs"
        hours < 0 -> "Today, $hours hrs"
        else -> "Today, same time"
    }
}
