package com.chronos.clock.ui.timer

import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.chronos.clock.data.ChronosRepository
import com.chronos.clock.data.RunningTimer
import com.chronos.clock.data.TimerPreset
import com.chronos.clock.ui.components.ChronosActionButton
import com.chronos.clock.ui.components.ChronosGauge
import com.chronos.clock.ui.components.ChronosIcon
import com.chronos.clock.ui.components.ChronosIconButton
import com.chronos.clock.ui.components.ChronosOverflowMenu
import com.chronos.clock.ui.components.ChronosPill
import com.chronos.clock.ui.components.ChronosTopBar
import com.chronos.clock.ui.components.OverflowItem
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

/** Durations offered on the empty state as one-tap starts. */
private val QuickStarts = listOf(
    1 * 60_000L,
    3 * 60_000L,
    5 * 60_000L,
    10 * 60_000L,
)

/**
 * The timer screen.
 *
 * With no timer running the screen is a start surface: a compact empty card with
 * one-tap durations and the presets. Dead, disabled controls are not rendered —
 * there is nothing to pause, reset or extend until a timer exists.
 */
@OptIn(androidx.compose.foundation.ExperimentalFoundationApi::class)
@Composable
fun TimerScreen(
    viewModel: TimerViewModel,
    onOpenSettings: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val timers by viewModel.timers.collectAsStateWithLifecycle()
    val presets by viewModel.presets.collectAsStateWithLifecycle()
    val active = viewModel.activeTimer
    var overflowOpen by remember { mutableStateOf(false) }
    var showAllSheet by remember { mutableStateOf(false) }
    var showNewPresetDialog by remember { mutableStateOf(false) }

    val others = timers.filter { it.id != active?.id }

    LazyColumn(
        modifier = modifier.fillMaxWidth(),
        contentPadding = PaddingValues(bottom = 140.dp),
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
                                OverflowItem("Add 5 minute timer", "add") {
                                    viewModel.startTimer("Timer", "timer", 5 * 60_000L)
                                },
                                OverflowItem("Add 10 minute timer", "add") {
                                    viewModel.startTimer("Timer", "timer", 10 * 60_000L)
                                },
                                OverflowItem("Settings", "settings", onOpenSettings),
                            ),
                        )
                    }
                },
            )
        }

        if (timers.isEmpty()) {
            item(key = "empty") {
                val reveal = rememberRevealState(index = 0)
                EmptyTimerCard(
                    onQuickStart = { millis -> viewModel.startTimer("Timer", "timer", millis) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp)
                        .reveal(reveal),
                )
            }
        } else {
            if (timers.size > 1) {
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
                            onViewAll = { showAllSheet = true },
                        )
                    }
                }
            }

            item(key = "gauge") {
                val reveal = rememberRevealState(index = if (timers.size > 1) 1 else 0)
                active?.let { current ->
                    TimerGauge(
                        timer = current,
                        onToggle = viewModel::pauseOrResume,
                        onReset = viewModel::reset,
                        onCancel = viewModel::cancel,
                        onAddMinute = viewModel::addMinutes,
                        modifier = Modifier
                            .fillMaxWidth()
                            .reveal(reveal),
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
                Text(
                    text = "New preset",
                    style = ChronosType.LabelMd,
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier
                        .clip(RoundedCornerShape(ChronosRadius.Pill))
                        .clickable { showNewPresetDialog = true }
                        .padding(horizontal = 6.dp, vertical = 4.dp),
                )
            }
        }

        item(key = "presets") {
            val reveal = rememberRevealState(index = 4)
            PresetGrid(
                presets = presets,
                onStart = { viewModel.startPreset(it) },
                onDelete = { viewModel.removeCustomPreset(it.id) },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp)
                    .reveal(reveal),
            )
        }
    }

    if (showNewPresetDialog) {
        NewPresetDialog(
            onDismiss = { showNewPresetDialog = false },
            onConfirm = { label, minutes ->
                viewModel.addCustomPreset(
                    TimerPreset(
                        id = ChronosRepository.newId(),
                        label = label.ifBlank { "Timer" },
                        icon = "timer",
                        durationMillis = minutes * 60_000L,
                    ),
                )
                showNewPresetDialog = false
            },
        )
    }

    if (showAllSheet) {
        AllTimersSheet(
            timers = timers,
            onPause = { viewModel.pause(it) },
            onResume = { viewModel.resume(it) },
            onCancel = { viewModel.cancel(it) },
            onDismiss = { showAllSheet = false },
        )
    }
}

/**
 * Shown when nothing is running. Offers one-tap durations instead of disabled
 * controls, which is the same approach a phone's clock app takes.
 */
@Composable
private fun EmptyTimerCard(
    onQuickStart: (Long) -> Unit,
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
        Box(
            modifier = Modifier
                .size(56.dp)
                .clip(CircleShape)
                .background(MaterialTheme.colorScheme.secondaryContainer),            contentAlignment = Alignment.Center,
        ) {
            ChronosIcon(
                name = "timer",
                size = 28.dp,
                tint = MaterialTheme.colorScheme.primaryContainer,
                filled = true,
            )
        }
        Spacer(Modifier.height(16.dp))
        Text(
            text = "No timer running",
            style = ChronosType.TitleLg,
            color = ChronosColors.OnSurface,
        )
        Spacer(Modifier.height(4.dp))
        Text(
            text = "Tap a duration to start one, or pick a preset below.",
            style = ChronosType.BodyMd,
            color = ChronosColors.Outline,
            textAlign = TextAlign.Center,
        )
        Spacer(Modifier.height(20.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            QuickStarts.forEach { millis ->
                QuickStartChip(
                    label = "+${millis / 60_000L}m",
                    onClick = { onQuickStart(millis) },
                )
            }
        }
    }
}

/** The design system's chip: 36 dp tall, `surface-container-high`, a tick on press. */
@Composable
private fun QuickStartChip(
    label: String,
    onClick: () -> Unit,
) {
    val press = rememberPressState()
    val container by androidx.compose.animation.animateColorAsState(
        targetValue = if (press.isPressed) ChronosColors.SurfaceBright else ChronosColors.SurfaceContainerHigh,
        animationSpec = tween(ChronosMotion.Short2),
        label = "quickStartContainer",
    )
    Row(
        modifier = Modifier
            .pressScale(press.interaction, pressedScale = 0.96f)
            .clip(RoundedCornerShape(ChronosRadius.Pill))
            .height(36.dp)
            .background(container)
            .border(1.dp, ChronosColors.OutlineVariant.copy(alpha = 0.30f), RoundedCornerShape(ChronosRadius.Pill))
            .clickable(
                interactionSource = press.interaction,
                indication = null,
                onClick = {
                    press.hapticTick()
                    onClick()
                },
            )
            .padding(horizontal = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        ChronosIcon(
            name = "add",
            size = 14.dp,
            tint = MaterialTheme.colorScheme.primary,
        )
        Spacer(Modifier.width(4.dp))
        Text(
            text = label,
            style = ChronosType.LabelMd,
            color = ChronosColors.OnSurface,
        )
    }
}

@Composable
private fun TimerStatusCapsule(
    running: Int,
    total: Int,
    onViewAll: () -> Unit,
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
        modifier = Modifier
            .clip(RoundedCornerShape(ChronosRadius.Pill))
            .background(ChronosColors.SurfaceContainerLow)
            .border(1.dp, ChronosColors.SurfaceContainerHigh.copy(alpha = 0.40f), RoundedCornerShape(ChronosRadius.Pill))
            .padding(horizontal = 12.dp, vertical = 7.dp),
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
            text = "$running of $total timers running",
            style = ChronosType.LabelSm,
            color = ChronosColors.OnSurfaceVariant,
        )
        Spacer(Modifier.width(8.dp))
        Text(
            text = "View all",
            style = ChronosType.LabelSm,
            color = MaterialTheme.colorScheme.primary,
            modifier = Modifier
                .clickable(onClick = onViewAll)
                .padding(vertical = 2.dp),
        )
    }
}

@Composable
private fun TimerGauge(
    timer: RunningTimer,
    onToggle: (RunningTimer) -> Unit,
    onReset: (RunningTimer) -> Unit,
    onCancel: (RunningTimer) -> Unit,
    onAddMinute: (RunningTimer, Long) -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        ChronosGauge(
            progress = timer?.progress ?: 0f,
            modifier = Modifier.size(ChronosDimens.GaugeSize),
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    ChronosIcon(
                        name = timer.icon,
                        size = 18.dp,
                        tint = MaterialTheme.colorScheme.primary,
                    )
                    Spacer(Modifier.width(6.dp))
                    Text(
                        text = timer.label,
                        style = ChronosType.LabelLg,
                        color = MaterialTheme.colorScheme.primary,
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
        }

        Row(
            modifier = Modifier.padding(top = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            ChronosPill(
                text = "+1:00",
                leadingIcon = "add",
                leadingIconTint = MaterialTheme.colorScheme.primary,
                container = ChronosColors.SurfaceContainerHigh,
                onClick = { onAddMinute(timer, 1L) },
            )
            ChronosPill(
                text = "+5:00",
                leadingIcon = "add",
                leadingIconTint = MaterialTheme.colorScheme.primary,
                container = ChronosColors.SurfaceContainerHigh,
                onClick = { onAddMinute(timer, 5L) },
            )
            ChronosActionButton(
                icon = "edit",
                contentDescription = "Add a custom length",
                onClick = { onAddMinute(timer, 10) },
                size = 36.dp,
                iconSize = 18.dp,
                container = ChronosColors.SurfaceContainerLow,
                content = ChronosColors.OnSurfaceVariant,
            )
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
                onClick = { onReset(timer) },
            )
            PlayPauseButton(
                running = timer?.running == true,
                onPress = { onToggle(timer) },
            )
            ChronosActionButton(
                icon = "close",
                contentDescription = "Delete Timer",
                onClick = { onCancel(timer) },
            )
        }
    }
}

@Composable
private fun PlayPauseButton(
    running: Boolean,
    onPress: () -> Unit,
) {
    val press = rememberPressState()
    val scale by androidx.compose.animation.core.animateFloatAsState(
        targetValue = if (press.isPressed) 0.94f else 1f,
        animationSpec = spring(dampingRatio = 0.5f, stiffness = 700f),
        label = "playPauseScale",
    )

    Box(
        modifier = Modifier
            .size(ChronosDimens.PlayButtonSize)
            .scale(scale)
            .clip(RoundedCornerShape(ChronosRadius.PlayButton))
            .background(MaterialTheme.colorScheme.primaryContainer)
            .clickable(
                interactionSource = press.interaction,
                indication = null,
                onClick = {
                    press.hapticTick()
                    onPress()
                },
            ),
        contentAlignment = Alignment.Center,
    ) {
        ChronosIcon(
            name = if (running) "pause" else "play_arrow",
            size = 36.dp,
            tint = MaterialTheme.colorScheme.onPrimaryContainer,
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
                .background(MaterialTheme.colorScheme.secondaryContainer),
            contentAlignment = Alignment.Center,
        ) {
            ChronosIcon(
                name = timer.icon,
                size = 20.dp,
                tint = MaterialTheme.colorScheme.primary,
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
            contentDescription = if (timer.running) "Pause ${timer.label}" else "Resume ${timer.label}",
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
            container = ChronosColors.SurfaceContainer,
            content = ChronosColors.Outline,
        )
    }
}

@Composable
private fun PresetGrid(
    presets: List<TimerPreset>,
    onStart: (TimerPreset) -> Unit,
    onDelete: (TimerPreset) -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        presets.chunked(2).forEach { row ->
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                row.forEach { preset ->
                    PresetCard(
                        preset = preset,
                        deletable = preset.id !in BuiltinPresetIds,
                        onStart = { onStart(preset) },
                        onDelete = { onDelete(preset) },
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
    deletable: Boolean,
    onStart: () -> Unit,
    onDelete: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val press = rememberPressState()
    val iconTint = when (preset.icon) {
        "local_cafe" -> MaterialTheme.colorScheme.tertiaryContainer
        "egg" -> MaterialTheme.colorScheme.secondary
        "self_improvement" -> MaterialTheme.colorScheme.primary
        else -> MaterialTheme.colorScheme.primary
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
        Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.Top) {
            ChronosIcon(
                name = preset.icon,
                size = 22.dp,
                tint = iconTint,
            )
            Spacer(Modifier.weight(1f))
            if (deletable) {
                ChronosIconButton(
                    icon = "delete",
                    contentDescription = "Delete the ${'$'}{preset.label} preset",
                    onClick = onDelete,
                    size = 24.dp,
                    iconSize = 14.dp,
                    tint = ChronosColors.Outline,
                )
                Spacer(Modifier.width(4.dp))
            }
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
                    tint = MaterialTheme.colorScheme.primary,
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

/**
 * Every timer at once, for the "View all" action.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun AllTimersSheet(
    timers: List<RunningTimer>,
    onPause: (RunningTimer) -> Unit,
    onResume: (RunningTimer) -> Unit,
    onCancel: (RunningTimer) -> Unit,
    onDismiss: () -> Unit,
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = ChronosColors.SurfaceContainerHigh,
        contentColor = ChronosColors.OnSurface,
    ) {
        Column(modifier = Modifier.navigationBarsPadding()) {
            Text(
                text = "All timers",
                style = ChronosType.TitleLg,
                color = ChronosColors.OnSurface,
                modifier = Modifier.padding(start = 24.dp, end = 24.dp, bottom = 12.dp),
            )
            LazyColumn(modifier = Modifier.heightIn(max = 420.dp)) {
                items(timers, key = { it.id }) { timer ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 24.dp, vertical = 10.dp),
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
                                name = timer.icon,
                                size = 20.dp,
                                tint = MaterialTheme.colorScheme.primary,
                            )
                        }
                        Spacer(Modifier.width(12.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = timer.label,
                                style = ChronosType.BodyLg,
                                color = ChronosColors.OnSurface,
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
                            onClick = { if (timer.running) onPause(timer) else onResume(timer) },
                            size = 36.dp,
                            iconSize = 18.dp,
                        )
                        Spacer(Modifier.width(8.dp))
                        ChronosActionButton(
                            icon = "close",
                            contentDescription = "Stop ${timer.label}",
                            onClick = { onCancel(timer) },
                            size = 36.dp,
                            iconSize = 18.dp,
                            container = ChronosColors.SurfaceContainer,
                            content = ChronosColors.Outline,
                        )
                    }
                }
            }
        }
    }
}

private fun endTimeLabel(timer: RunningTimer): String {
    val endAt = if (timer.endAtMillis > 0L) timer.endAtMillis else System.currentTimeMillis() + timer.remainingMillis
    return Instant.ofEpochMilli(endAt)
        .atZone(ZoneId.systemDefault())
        .format(EndTimeFormat)
}
/** The ids of the four presets that ship with the app, which are not deletable. */
private val BuiltinPresetIds = ChronosRepository.TimerPresets.map { it.id }.toSet()
