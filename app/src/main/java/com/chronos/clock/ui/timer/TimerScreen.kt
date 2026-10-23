package com.chronos.clock.ui.timer

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.tween
import androidx.compose.animation.core.rememberInfiniteTransition
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
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.chronos.clock.data.RunningTimer
import com.chronos.clock.data.TimerPreset
import com.chronos.clock.ui.components.ChronosActionButton
import com.chronos.clock.ui.components.ChronosGauge
import com.chronos.clock.ui.components.ChronosIcon
import com.chronos.clock.ui.components.ChronosIconButton
import com.chronos.clock.ui.components.ChronosPill
import com.chronos.clock.ui.components.ChronosTopBar
import com.chronos.clock.ui.components.pressScale
import com.chronos.clock.ui.components.rememberPressState
import com.chronos.clock.ui.components.rememberRevealState
import com.chronos.clock.ui.components.reveal
import com.chronos.clock.ui.theme.ChronosColors
import com.chronos.clock.ui.theme.ChronosDimens
import com.chronos.clock.ui.theme.ChronosMotion
import com.chronos.clock.ui.theme.ChronosRadius
import com.chronos.clock.ui.theme.ChronosType
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter

private val EndTimeFormat: DateTimeFormatter = DateTimeFormatter.ofPattern("HH:mm")

@Composable
@OptIn(ExperimentalFoundationApi::class)
fun TimerScreen(
    viewModel: TimerViewModel,
    modifier: Modifier = Modifier,
) {
    val timers by viewModel.timers.collectAsStateWithLifecycle()
    val active = viewModel.activeTimer
    var showAll by remember { mutableStateOf(false) }

    val others = timers.filter { it.id != active?.id }

    LazyColumn(
        modifier = modifier.fillMaxWidth(),
        contentPadding = PaddingValues(bottom = 120.dp),
        verticalArrangement = Arrangement.spacedBy(24.dp),
    ) {
        stickyHeader(key = "topbar") {
            ChronosTopBar(
                actions = {
                    ChronosIconButton(
                        icon = "add",
                        contentDescription = "Add timer",
                        onClick = { viewModel.startTimer("Timer", "timer", 5 * 60_000L) },
                        tint = ChronosColors.OnSurface,
                    )
                    ChronosIconButton(
                        icon = "more_vert",
                        contentDescription = "More options",
                        onClick = { },
                        tint = ChronosColors.Outline,
                    )
                },
            )
        }

        item(key = "status") {
            val reveal = rememberRevealState(index = 0)
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp)
                    .reveal(reveal),
                contentAlignment = Alignment.Center,
            ) {
                TimerStatusCapsule(
                    running = timers.count { it.running },
                    total = timers.size,
                    onViewAll = { showAll = !showAll },
                )
            }
        }

        item(key = "gauge") {
            val reveal = rememberRevealState(index = 1)
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .reveal(reveal),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                TimerGauge(
                    timer = active,
                    onToggle = { timer ->
                        if (timer.running) viewModel.pause(timer) else viewModel.resume(timer)
                    },
                    onReset = { timer -> viewModel.reset(timer) },
                    onCancel = { timer -> viewModel.cancel(timer) },
                    onAddMinute = { timer, minutes -> viewModel.addMinutes(timer, minutes) },
                )
            }
        }

        item(key = "secondary") {
            if (others.isNotEmpty()) {
                val reveal = rememberRevealState(index = 2)
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp)
                        .reveal(reveal),
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                ) {
                    others.forEach { timer ->
                        SecondaryTimerRow(
                            timer = timer,
                            onToggle = { if (timer.running) viewModel.pause(timer) else viewModel.resume(timer) },
                            onCancel = { viewModel.cancel(timer) },
                        )
                    }
                }
            }
        }

        item(key = "presets-header") {
            val reveal = rememberRevealState(index = 3)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp)
                    .reveal(reveal),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    text = "Presets",
                    style = ChronosType.TitleMd,
                    color = ChronosColors.OnSurface,
                    fontWeight = FontWeight.Medium,
                    modifier = Modifier.weight(1f),
                )
                Row(
                    modifier = Modifier
                        .clip(RoundedCornerShape(ChronosRadius.Pill))
                        .clickable { }
                        .padding(horizontal = 6.dp, vertical = 4.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    ChronosIcon(
                        name = "add",
                        size = 16.dp,
                        tint = ChronosColors.Primary,
                    )
                    Spacer(Modifier.width(4.dp))
                    Text(
                        text = "New preset",
                        style = ChronosType.LabelMd,
                        color = ChronosColors.Primary,
                    )
                }
            }
        }

        item(key = "presets") {
            val reveal = rememberRevealState(index = 4)
            PresetGrid(
                presets = viewModel.presets,
                onStart = { viewModel.startPreset(it) },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp)
                    .reveal(reveal),
            )
        }
    }
}

@Composable
private fun TimerStatusCapsule(
    running: Int,
    total: Int,
    onViewAll: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val pulse = rememberInfiniteTransition(label = "timerPulse")
    val dotAlpha by pulse.animateFloat(
        initialValue = 0.35f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(1100, easing = ChronosMotion.Standard),
            repeatMode = RepeatMode.Reverse,
        ),
        label = "timerPulseDot",
    )

    Row(
        modifier = modifier
            .clip(RoundedCornerShape(ChronosRadius.Pill))
            .background(ChronosColors.SurfaceContainerLow)
            .border(1.dp, ChronosColors.SurfaceContainerHigh.copy(alpha = 0.40f), RoundedCornerShape(ChronosRadius.Pill))
            .padding(horizontal = 12.dp, vertical = 7.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.Center,
    ) {
        Box(
            modifier = Modifier
                .size(8.dp)
                .clip(CircleShape)
                .background(ChronosColors.PrimaryContainer.copy(alpha = dotAlpha)),
        )
        Spacer(Modifier.width(8.dp))
        Text(
            text = if (total == 0) {
                "No timers running"
            } else {
                "$running of $total Timers Running"
            },
            style = ChronosType.LabelSm,
            color = ChronosColors.OnSurfaceVariant,
        )
        if (total > 0) {
            Spacer(Modifier.width(8.dp))
            Text(
                text = "View all",
                style = ChronosType.LabelSm,
                color = ChronosColors.Primary,
                modifier = Modifier
                    .clickable(onClick = onViewAll)
                    .padding(vertical = 2.dp),
            )
        }
    }
}

@Composable
private fun TimerGauge(
    timer: RunningTimer?,
    onToggle: (RunningTimer) -> Unit,
    onReset: (RunningTimer) -> Unit,
    onCancel: (RunningTimer) -> Unit,
    onAddMinute: (RunningTimer, Long) -> Unit,
) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        ChronosGauge(
            progress = timer?.progress ?: 0f,
            modifier = Modifier.size(ChronosDimens.GaugeSize),
        ) {
            if (timer != null) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        ChronosIcon(
                            name = timer.icon,
                            size = 18.dp,
                            tint = ChronosColors.Primary,
                        )
                        Spacer(Modifier.width(6.dp))
                        Text(
                            text = timer.label,
                            style = ChronosType.LabelLg,
                            color = ChronosColors.Primary,
                        )
                    }
                    Text(
                        text = timer.remainingText(),
                        style = ChronosType.DisplayHeroMobile,
                        color = ChronosColors.OnSurface,
                    )
                    Row(
                        modifier = Modifier.padding(top = 4.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        ChronosIcon(
                            name = "notifications_active",
                            size = 14.dp,
                            tint = ChronosColors.Outline,
                        )
                        Spacer(Modifier.width(4.dp))
                        Text(
                            text = endTimeLabel(timer),
                            style = ChronosType.LabelSm,
                            color = ChronosColors.Outline,
                        )
                    }
                }
            } else {
                Text(
                    text = "No timer",
                    style = ChronosType.HeadlineLg,
                    color = ChronosColors.Outline,
                )
                Text(
                    text = "Pick a preset below to get started",
                    style = ChronosType.LabelSm,
                    color = ChronosColors.Outline,
                    modifier = Modifier.padding(top = 6.dp),
                )
            }
        }

        Row(
            modifier = Modifier.padding(top = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            ChronosPill(
                text = "+1:00",
                leadingIcon = "add",
                leadingIconTint = ChronosColors.Primary,
                container = ChronosColors.SurfaceContainerHigh,
                onClick = { timer?.let { onAddMinute(it, 1) } },
                enabled = timer != null,
            )
            ChronosPill(
                text = "+5:00",
                leadingIcon = "add",
                leadingIconTint = ChronosColors.Primary,
                container = ChronosColors.SurfaceContainerHigh,
                onClick = { timer?.let { onAddMinute(it, 5) } },
                enabled = timer != null,
            )
            Box(
                modifier = Modifier
                    .size(36.dp)
                    .clip(RoundedCornerShape(ChronosRadius.Pill))
                    .background(ChronosColors.SurfaceContainerLow)
                    .clickable(enabled = timer != null) { },
                contentAlignment = Alignment.Center,
            ) {
                ChronosIcon(
                    name = "edit",
                    size = 18.dp,
                    tint = ChronosColors.OnSurfaceVariant,
                )
            }
        }

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 24.dp, start = 12.dp, end = 12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            ChronosActionButton(
                icon = "replay",
                contentDescription = "Reset Timer",
                onClick = { timer?.let(onReset) },
                enabled = timer != null,
            )
            PlayPauseButton(
                running = timer?.running == true,
                enabled = timer != null,
                onClick = { timer?.let(onToggle) },
            )
            ChronosActionButton(
                icon = "close",
                contentDescription = "Delete Timer",
                onClick = { timer?.let(onCancel) },
                enabled = timer != null,
            )
        }
    }
}

@Composable
private fun PlayPauseButton(
    running: Boolean,
    enabled: Boolean,
    onClick: () -> Unit,
) {
    val press = rememberPressState()
    val scale by androidx.compose.animation.core.animateFloatAsState(
        targetValue = if (press.isPressed) 0.94f else 1f,
        animationSpec = androidx.compose.animation.core.spring(dampingRatio = 0.5f, stiffness = 700f),
        label = "playPauseScale",
    )

    Box(
        modifier = Modifier
            .size(ChronosDimens.PlayButtonSize)
            .scale(scale)
            .clip(RoundedCornerShape(ChronosRadius.PlayButton))
            .background(if (enabled) ChronosColors.PrimaryContainer else ChronosColors.SurfaceContainerHigh)
            .clickable(
                interactionSource = press.interaction,
                indication = null,
                enabled = enabled,
                onClick = {
                    press.hapticTick()
                    onClick()
                },
            ),
        contentAlignment = Alignment.Center,
    ) {
        ChronosIcon(
            name = if (running) "pause" else "play_arrow",
            size = 36.dp,
            tint = if (enabled) ChronosColors.OnPrimaryContainer else ChronosColors.Outline,
            filled = true,
        )
    }
}

@Composable
private fun SecondaryTimerRow(
    timer: RunningTimer,
    onToggle: () -> Unit,
    onCancel: () -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(ChronosRadius.Card))
            .background(ChronosColors.SurfaceContainerLow)
            .border(1.dp, ChronosColors.GhostBorder, RoundedCornerShape(ChronosRadius.Card))
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Box(
            modifier = Modifier
                .size(40.dp)
                .clip(CircleShape)
                .background(ChronosColors.SecondaryContainer),
            contentAlignment = Alignment.Center,
        ) {
            ChronosIcon(
                name = timer.icon,
                size = 20.dp,
                tint = ChronosColors.Primary,
            )
        }
        Spacer(Modifier.width(12.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = timer.label,
                style = ChronosType.BodyLg,
                color = ChronosColors.OnSurface,
                fontWeight = FontWeight.Medium,
            )
            Text(
                text = "${timer.remainingText()} remaining",
                style = ChronosType.LabelSm,
                color = ChronosColors.Outline,
            )
        }
        ChronosActionButton(
            icon = if (timer.running) "pause" else "play_arrow",
            contentDescription = "Pause ${timer.label}",
            onClick = onToggle,
            size = 36.dp,
            iconSize = 18.dp,
        )
        Spacer(Modifier.width(8.dp))
        ChronosActionButton(
            icon = "close",
            contentDescription = "Stop ${timer.label}",
            onClick = onCancel,
            size = 36.dp,
            iconSize = 18.dp,
            content = ChronosColors.Outline,
        )
    }
}

@Composable
private fun PresetGrid(
    presets: List<TimerPreset>,
    onStart: (TimerPreset) -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        presets.chunked(2).forEachIndexed { rowIndex, row ->
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                row.forEachIndexed { columnIndex, preset ->
                    PresetCard(
                        preset = preset,
                        onStart = { onStart(preset) },
                        modifier = Modifier.weight(1f),
                    )
                }
                if (row.size == 1) Spacer(Modifier.weight(1f))
            }
        }
    }
}

@Composable
private fun PresetCard(
    preset: TimerPreset,
    onStart: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val press = rememberPressState()
    val iconTint = when (preset.icon) {
        "local_cafe" -> ChronosColors.TertiaryFixed
        "egg" -> ChronosColors.Secondary
        "self_improvement" -> ChronosColors.Primary
        else -> ChronosColors.TertiaryFixedDim
    }

    Column(
        modifier = modifier
            .clip(RoundedCornerShape(ChronosRadius.Card))
            .background(ChronosColors.SurfaceContainerLow)
            .border(1.dp, ChronosColors.GhostBorder, RoundedCornerShape(ChronosRadius.Card))
            .pressScale(press.interaction, pressedScale = 0.97f)
            .clickable(
                interactionSource = press.interaction,
                indication = null,
                onClick = {
                    press.hapticTick()
                    onStart()
                },
            )
            .padding(16.dp),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.Top,
        ) {
            ChronosIcon(
                name = preset.icon,
                size = 22.dp,
                tint = iconTint,
            )
            Spacer(Modifier.weight(1f))
            Box(
                modifier = Modifier
                    .size(32.dp)
                    .clip(CircleShape)
                    .background(ChronosColors.SurfaceContainerHigh)
                    .clickable(
                        interactionSource = press.interaction,
                        indication = null,
                        onClick = {
                            press.hapticTick()
                            onStart()
                        },
                    ),
                contentAlignment = Alignment.Center,
            ) {
                ChronosIcon(
                    name = "play_arrow",
                    size = 18.dp,
                    tint = ChronosColors.Primary,
                    filled = true,
                )
            }
        }
        Text(
            text = "%d:%02d".format(preset.durationMillis / 60_000L, (preset.durationMillis / 1000L) % 60),
            style = ChronosType.HeadlineMd,
            color = ChronosColors.OnSurface,
            fontWeight = FontWeight.Light,
            modifier = Modifier.padding(top = 16.dp),
        )
        Text(
            text = preset.label,
            style = ChronosType.LabelMd,
            color = ChronosColors.Outline,
        )
    }
}

private fun endTimeLabel(timer: RunningTimer): String {
    val endAt = if (timer.endAtMillis > 0L) {
        timer.endAtMillis
    } else {
        System.currentTimeMillis() + timer.remainingMillis
    }
    return Instant.ofEpochMilli(endAt)
        .atZone(ZoneId.systemDefault())
        .format(EndTimeFormat)
}
