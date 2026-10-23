package com.chronos.clock.ui.alarm

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.chronos.clock.core.rememberNow
import com.chronos.clock.data.Alarm
import com.chronos.clock.ui.components.ChronosFab
import com.chronos.clock.ui.components.ChronosIcon
import com.chronos.clock.ui.components.ChronosIconButton
import com.chronos.clock.ui.components.ChronosPill
import com.chronos.clock.ui.components.ChronosTopBar
import com.chronos.clock.ui.components.IconWell
import com.chronos.clock.ui.components.M3Switch
import com.chronos.clock.ui.components.pressScale
import com.chronos.clock.ui.components.rememberPressState
import com.chronos.clock.ui.components.rememberRevealState
import com.chronos.clock.ui.components.reveal
import com.chronos.clock.ui.theme.ChronosColors
import com.chronos.clock.ui.theme.ChronosDimens
import com.chronos.clock.ui.theme.ChronosMotion
import com.chronos.clock.ui.theme.ChronosRadius
import com.chronos.clock.ui.theme.ChronosType
import java.time.Duration
import java.time.LocalDateTime
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter
import java.time.temporal.ChronoUnit

private val DayLetters = listOf("M", "T", "W", "T", "F", "S", "S")
private val IsoDays = listOf(1, 2, 3, 4, 5, 6, 7)

@Composable
@OptIn(ExperimentalFoundationApi::class)
fun AlarmScreen(
    viewModel: AlarmViewModel,
    modifier: Modifier = Modifier,
) {
    val alarms by viewModel.alarms.collectAsStateWithLifecycle()
    val sleepSchedule by viewModel.sleepSchedule.collectAsStateWithLifecycle()
    val now by rememberNow()

    var showAddDialog by remember { mutableStateOf(false) }

    Box(modifier = modifier.fillMaxWidth()) {
    LazyColumn(
        modifier = Modifier.fillMaxWidth(),
        contentPadding = PaddingValues(bottom = 120.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp),
    ) {
        stickyHeader(key = "topbar") {
            ChronosTopBar(
                actions = {
                    ChronosIconButton(
                        icon = "more_vert",
                        contentDescription = "More options",
                        onClick = { },
                        tint = ChronosColors.Outline,
                    )
                },
            )
        }

        item(key = "banner") {
            val reveal = rememberRevealState(index = 0)
            UpcomingAlarmBanner(
                alarms = alarms,
                now = now,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp)
                    .reveal(reveal),
            )
        }

        items(alarms, key = { it.id }) { alarm ->
            val reveal = rememberRevealState(index = alarms.indexOf(alarm) + 1)
            AlarmCard(
                alarm = alarm,
                now = now,
                onToggle = { enabled -> viewModel.toggle(alarm.id, enabled) },
                onToggleDay = { day -> viewModel.toggleDay(alarm.id, day) },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp)
                    .reveal(reveal),
            )
        }

        item(key = "bedtime") {
            val reveal = rememberRevealState(index = alarms.size + 1)
            BedtimeCard(
                bedtime = sleepSchedule.bedtimeText(),
                wake = sleepSchedule.wakeText(),
                rest = sleepSchedule.restHoursText(),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp)
                    .reveal(reveal),
            )
        }
    }

        ChronosFab(
            icon = "add",
            contentDescription = "Add new alarm",
            onClick = { showAddDialog = true },
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(end = 20.dp, bottom = 96.dp),
        )
    }

    if (showAddDialog) {
        AddAlarmDialog(
            onDismiss = { showAddDialog = false },
            onConfirm = { hour, minute ->
                viewModel.addAlarm(hour, minute, "Alarm")
                showAddDialog = false
            },
        )
    }
}

@Composable
private fun UpcomingAlarmBanner(
    alarms: List<Alarm>,
    now: ZonedDateTime,
    modifier: Modifier = Modifier,
) {
    val next = remember(alarms, now.toLocalDate()) {
        val localNow = now.toLocalDateTime()
        alarms.filter { it.enabled }
            .map { alarm ->
                val trigger = com.chronos.clock.core.nextAlarmTrigger(
                    now = localNow,
                    hour = alarm.hour,
                    minute = alarm.minute,
                    days = alarm.days,
                )
                alarm to trigger
            }
            .minByOrNull { it.second }
    }

    val until = next?.let { Duration.between(now.toLocalDateTime(), it.second) }

    Row(
        modifier = modifier
            .clip(RoundedCornerShape(ChronosRadius.Card))
            .background(ChronosColors.SurfaceContainerHigh.copy(alpha = 0.80f))
            .border(1.dp, ChronosColors.GhostBorder, RoundedCornerShape(ChronosRadius.Card))
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        IconWell(
            icon = "notifications_active",
            size = 40.dp,
            iconSize = 20.dp,
            container = ChronosColors.SecondaryContainer.copy(alpha = 0.60f),
            content = ChronosColors.PrimaryContainer,
            filled = true,
        )
        Spacer(Modifier.width(14.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = "UPCOMING ALARM",
                style = ChronosType.Eyebrow,
                color = ChronosColors.Outline,
            )
            Text(
                text = if (next != null && until != null) {
                    "Alarm in ${until.toHours()}h ${until.toMinutes() % 60}m"
                } else {
                    "No upcoming alarm"
                },
                style = ChronosType.TitleMd,
                color = ChronosColors.OnSurface,
                maxLines = 1,
                overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis,
            )
        }
        Spacer(Modifier.width(8.dp))
        Text(
            text = "Dismiss",
            style = ChronosType.LabelMd,
            color = ChronosColors.Primary,
            modifier = Modifier
                .clip(RoundedCornerShape(ChronosRadius.Pill))
                .clickable { }
                .padding(horizontal = 12.dp, vertical = 6.dp),
        )
        Text(
            text = "Change",
            style = ChronosType.LabelMd,
            color = ChronosColors.OnSurface,
            modifier = Modifier
                .clip(RoundedCornerShape(ChronosRadius.Pill))
                .background(ChronosColors.SurfaceContainer)
                .clickable { }
                .padding(horizontal = 12.dp, vertical = 6.dp),
        )
    }
}

@Composable
private fun AlarmCard(
    alarm: Alarm,
    now: ZonedDateTime,
    onToggle: (Boolean) -> Unit,
    onToggleDay: (Int) -> Unit,
    modifier: Modifier = Modifier,
) {
    var expanded by remember(alarm.id) { mutableStateOf(alarm.enabled) }

    val container by animateColorAsState(
        targetValue = if (alarm.enabled) {
            ChronosColors.SurfaceContainerHigh
        } else {
            ChronosColors.SurfaceContainerLow
        },
        animationSpec = spring(dampingRatio = 0.8f, stiffness = 400f),
        label = "alarmCardContainer",
    )
    val borderColor by animateColorAsState(
        targetValue = if (alarm.enabled) {
            Color.White.copy(alpha = 0.06f)
        } else {
            Color.White.copy(alpha = 0.03f)
        },
        animationSpec = spring(dampingRatio = 0.8f, stiffness = 400f),
        label = "alarmCardBorder",
    )

    val timeColor = if (alarm.enabled) ChronosColors.Primary else ChronosColors.Outline.copy(alpha = 0.60f)
    val meridiemColor = if (alarm.enabled) ChronosColors.Secondary else ChronosColors.Outline.copy(alpha = 0.40f)
    val labelColor = if (alarm.enabled) ChronosColors.OnSurface else ChronosColors.Outline
    val metaColor = if (alarm.enabled) ChronosColors.Outline else ChronosColors.Outline.copy(alpha = 0.70f)

    Column(
        modifier = modifier
            .clip(RoundedCornerShape(ChronosRadius.AlarmCard))
            .background(container)
            .border(1.dp, borderColor, RoundedCornerShape(ChronosRadius.AlarmCard))
            .padding(16.dp),
    ) {
        Row(verticalAlignment = Alignment.Top) {
            Column(modifier = Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.Bottom) {
                    Text(
                        text = alarm.timeText(),
                        style = ChronosType.HeadlineLgMobile,
                        color = timeColor,
                        fontWeight = if (alarm.enabled) FontWeight.Medium else FontWeight.Light,
                        modifier = Modifier.alignByBaseline(),
                    )
                    Spacer(Modifier.width(8.dp))
                    Text(
                        text = alarm.meridiem(),
                        style = ChronosType.TitleMd,
                        color = meridiemColor,
                        fontWeight = FontWeight.Medium,
                        modifier = Modifier.alignByBaseline(),
                    )
                }
                Row(
                    modifier = Modifier.padding(top = 2.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Text(
                        text = alarm.label,
                        style = ChronosType.TitleMd,
                        color = labelColor,
                        fontWeight = FontWeight.Medium,
                        maxLines = 1,
                    )
                    Spacer(Modifier.width(6.dp))
                    Box(
                        modifier = Modifier
                            .size(4.dp)
                            .clip(CircleShape)
                            .background(
                                if (alarm.enabled) ChronosColors.Outline else ChronosColors.OutlineVariant,
                            ),
                    )
                    Spacer(Modifier.width(6.dp))
                    Text(
                        text = nextOccurrenceLabel(alarm, now),
                        style = ChronosType.BodyMd,
                        color = metaColor,
                        maxLines = 1,
                    )
                }
            }
            Spacer(Modifier.width(12.dp))
            M3Switch(checked = alarm.enabled, onCheckedChange = onToggle)
        }

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(6.dp),
        ) {
            IsoDays.forEachIndexed { index, iso ->
                DayPill(
                    letter = DayLetters[index],
                    selected = iso in alarm.days,
                    alarmEnabled = alarm.enabled,
                    onClick = { onToggleDay(iso) },
                )
            }
            Spacer(Modifier.weight(1f))
            ChronosIconButton(
                icon = if (expanded) "expand_less" else "expand_more",
                contentDescription = if (expanded) "Collapse alarm details" else "Expand alarm details",
                onClick = { expanded = !expanded },
                size = 28.dp,
                iconSize = 20.dp,
                tint = if (alarm.enabled) ChronosColors.Outline else ChronosColors.Outline.copy(alpha = 0.60f),
            )
        }

        AnimatedVisibility(
            visible = expanded,
            enter = fadeIn(animationSpec = androidx.compose.animation.core.tween(ChronosMotion.Medium1)) +
                expandVertically(animationSpec = spring(dampingRatio = 0.85f, stiffness = 320f)),
            exit = fadeOut(animationSpec = androidx.compose.animation.core.tween(ChronosMotion.Short3)) +
                shrinkVertically(animationSpec = spring(dampingRatio = 0.85f, stiffness = 380f)),
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 14.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(1.dp)
                        .background(ChronosColors.OutlineVariant.copy(alpha = 0.30f)),
                )
            }
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 12.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                ChronosPill(
                    text = alarm.sound,
                    leadingIcon = "graphic_eq",
                    leadingIconTint = ChronosColors.TertiaryContainer,
                    container = ChronosColors.SurfaceContainer,
                    borderColor = Color.Transparent,
                    onClick = { },
                )
                Spacer(Modifier.width(8.dp))
                alarm.routine?.let { routine ->
                    ChronosPill(
                        text = routine,
                        leadingIcon = "assistant",
                        leadingIconTint = ChronosColors.Secondary,
                        iconFilled = true,
                        container = ChronosColors.SurfaceContainer,
                        borderColor = Color.Transparent,
                        onClick = { },
                    )
                    Spacer(Modifier.width(8.dp))
                }
                if (alarm.vibrate) {
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .clip(CircleShape)
                            .background(ChronosColors.SurfaceContainer),
                        contentAlignment = Alignment.Center,
                    ) {
                        ChronosIcon(
                            name = "vibration",
                            size = 16.dp,
                            tint = ChronosColors.Outline,
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun DayPill(
    letter: String,
    selected: Boolean,
    alarmEnabled: Boolean,
    onClick: () -> Unit,
) {
    val press = rememberPressState()

    val container by animateColorAsState(
        targetValue = when {
            selected && alarmEnabled -> ChronosColors.PrimaryContainer
            selected -> ChronosColors.SurfaceContainerHigh
            else -> if (alarmEnabled) ChronosColors.SurfaceContainer else ChronosColors.SurfaceContainerLowest
        },
        animationSpec = spring(dampingRatio = 0.7f, stiffness = 520f),
        label = "dayPillContainer",
    )
    val content by animateColorAsState(
        targetValue = when {
            selected && alarmEnabled -> ChronosColors.OnPrimaryContainer
            selected -> ChronosColors.SecondaryFixedDim
            else -> ChronosColors.Outline.copy(alpha = if (alarmEnabled) 1f else 0.40f)
        },
        animationSpec = spring(dampingRatio = 0.7f, stiffness = 520f),
        label = "dayPillContent",
    )

    Box(
        modifier = Modifier
            .size(ChronosDimens.DayPillSize)
            .pressScale(press.interaction, pressedScale = 0.88f)
            .clip(CircleShape)
            .background(container)
            .clickable(
                interactionSource = press.interaction,
                indication = null,
                onClick = {
                    press.hapticTick()
                    onClick()
                },
            ),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text = letter,
            style = ChronosType.LabelMd,
            color = content,
            fontWeight = if (selected) FontWeight.Bold else FontWeight.Medium,
        )
    }
}

@Composable
private fun BedtimeCard(
    bedtime: String,
    wake: String,
    rest: String,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .clip(RoundedCornerShape(ChronosRadius.AlarmCard))
            .background(ChronosColors.SurfaceContainerLow)
            .border(1.dp, ChronosColors.GhostBorder, RoundedCornerShape(ChronosRadius.AlarmCard))
            .padding(16.dp),
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .clip(RoundedCornerShape(ChronosRadius.IconWell))
                    .background(ChronosColors.SecondaryContainer.copy(alpha = 0.50f)),
                contentAlignment = Alignment.Center,
            ) {
                ChronosIcon(
                    name = "bedtime",
                    size = 26.dp,
                    tint = ChronosColors.SecondaryFixedDim,
                    filled = true,
                )
            }
            Spacer(Modifier.width(14.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "SLEEP SCHEDULE",
                    style = ChronosType.Eyebrow,
                    color = ChronosColors.Tertiary,
                    fontWeight = FontWeight.SemiBold,
                )
                Text(
                    text = "Bedtime Mode",
                    style = ChronosType.TitleLg,
                    color = ChronosColors.OnSurface,
                    fontWeight = FontWeight.SemiBold,
                )
                Text(
                    text = "Bedtime $bedtime • Wake up $wake",
                    style = ChronosType.BodyMd,
                    color = ChronosColors.Outline,
                )
            }
            ChronosIconButton(
                icon = "arrow_forward_ios",
                contentDescription = "Configure sleep schedule",
                onClick = { },
                size = 36.dp,
                iconSize = 20.dp,
                tint = ChronosColors.Outline,
            )
        }

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 12.dp)
                .height(1.dp)
                .background(ChronosColors.OutlineVariant.copy(alpha = 0.20f)),
        )

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            ChronosIcon(
                name = "hotel",
                size = 16.dp,
                tint = ChronosColors.Tertiary,
            )
            Spacer(Modifier.width(8.dp))
            Text(
                text = rest,
                style = ChronosType.LabelSm,
                color = ChronosColors.Outline,
            )
            Spacer(Modifier.weight(1f))
            Text(
                text = "Change bedtime",
                style = ChronosType.LabelSm,
                color = ChronosColors.Secondary,
                fontWeight = FontWeight.Medium,
                modifier = Modifier.clickable { },
            )
        }
    }
}

/** `Tomorrow` for a repeating alarm, `Today` when it is still due today. */
private fun nextOccurrenceLabel(alarm: Alarm, now: ZonedDateTime): String {
    val localNow = now.toLocalDateTime()
    if (alarm.days.isEmpty()) {
        val today = localNow.toLocalDate().atTime(alarm.hour, alarm.minute)
        return if (today.isAfter(localNow)) "Today" else "Tomorrow"
    }
    val trigger = com.chronos.clock.core.nextAlarmTrigger(
        now = localNow,
        hour = alarm.hour,
        minute = alarm.minute,
        days = alarm.days,
    )
    val days = ChronoUnit.DAYS.between(localNow.toLocalDate(), trigger.toLocalDate())
    return when (days) {
        0L -> "Today"
        1L -> "Tomorrow"
        else -> "In $days days"
    }
}
