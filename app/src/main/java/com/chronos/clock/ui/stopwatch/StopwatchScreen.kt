package com.chronos.clock.ui.stopwatch

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.spring
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.border
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
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.chronos.clock.data.Lap
import com.chronos.clock.ui.components.ChronosActionButton
import com.chronos.clock.ui.components.ChronosGauge
import com.chronos.clock.ui.components.ChronosIcon
import com.chronos.clock.ui.components.ChronosIconButton
import com.chronos.clock.ui.components.ChronosOverflowMenu
import com.chronos.clock.ui.components.OverflowItem
import com.chronos.clock.ui.components.ChronosTextButton
import com.chronos.clock.ui.components.ChronosTopBar
import com.chronos.clock.ui.components.rememberPressState
import com.chronos.clock.ui.components.rememberRevealState
import com.chronos.clock.ui.components.reveal
import com.chronos.clock.ui.theme.ChronosColors
import com.chronos.clock.ui.theme.ChronosDimens
import com.chronos.clock.ui.theme.ChronosMotion
import com.chronos.clock.ui.theme.ChronosRadius
import com.chronos.clock.ui.theme.ChronosType

@Composable
@OptIn(ExperimentalFoundationApi::class)
fun StopwatchScreen(
    viewModel: StopwatchViewModel,
    onOpenSettings: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val laps by viewModel.laps.collectAsStateWithLifecycle()
    val context = LocalContext.current
    var overflowOpen by remember { mutableStateOf(false) }

    LazyColumn(
        modifier = modifier.fillMaxWidth(),
        contentPadding = PaddingValues(bottom = 120.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        stickyHeader(key = "topbar") {
            ChronosTopBar(
                actions = {
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
                                OverflowItem("Reset stopwatch", "replay") { viewModel.reset() },
                                OverflowItem("Settings", "settings", onOpenSettings),
                            ),
                        )
                    }
                },
            )
        }

        item(key = "gauge") {
            val reveal = rememberRevealState(index = 0)
            StopwatchGauge(
                elapsedMillis = viewModel.elapsedMillis,
                running = viewModel.running,
                lapCount = laps.size,
                onToggle = viewModel::toggle,
                onLap = viewModel::lap,
                onReset = viewModel::reset,
                modifier = Modifier
                    .fillMaxWidth()
                    .reveal(reveal),
            )
        }

        item(key = "lap-header") {
            val reveal = rememberRevealState(index = 1)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp)
                    .padding(top = 8.dp)
                    .reveal(reveal),
            ) {
                Text(
                    text = "LAP",
                    style = ChronosType.LabelMd,
                    color = ChronosColors.Outline,
                    modifier = Modifier.weight(1f),
                )
                Text(
                    text = "SPLIT",
                    style = ChronosType.LabelMd,
                    color = ChronosColors.Outline,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.weight(1f),
                )
                Text(
                    text = "OVERALL",
                    style = ChronosType.LabelMd,
                    color = ChronosColors.Outline,
                    textAlign = TextAlign.End,
                    modifier = Modifier.weight(1f),
                )
            }
        }

        items(laps.reversed(), key = { it.index }) { lap ->
            val reveal = rememberRevealState(index = 2)
            LapRow(
                lap = lap,
                isFastest = viewModel.fastestLap?.index == lap.index,
                isSlowest = viewModel.slowestLap?.index == lap.index,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp)
                    .reveal(reveal),
            )
        }

        item(key = "share") {
            val reveal = rememberRevealState(index = 3)
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 8.dp)
                    .reveal(reveal),
                contentAlignment = Alignment.Center,
            ) {
                ChronosTextButton(
                    text = "Share Laps",
                    leadingIcon = "share",
                    onClick = {
                        val share = android.content.Intent(android.content.Intent.ACTION_SEND).apply {
                            type = "text/plain"
                            putExtra(
                                android.content.Intent.EXTRA_TEXT,
                                buildShareText(laps),
                            )
                        }
                        context.startActivity(
                            android.content.Intent.createChooser(share, "Share laps"),
                        )
                    },
                )
            }
        }
    }
}

@Composable
private fun StopwatchGauge(
    elapsedMillis: Long,
    running: Boolean,
    lapCount: Int,
    onToggle: () -> Unit,
    onLap: () -> Unit,
    onReset: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val totalSeconds = elapsedMillis / 1000
    val minutes = totalSeconds / 60
    val seconds = totalSeconds % 60
    val hundredths = (elapsedMillis % 1000) / 10

    // The arc sweeps one revolution per minute.
    val sweep = ((elapsedMillis % 60_000L).toFloat() / 60_000f).coerceIn(0f, 1f)

    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        ChronosGauge(
            progress = sweep,
            modifier = Modifier.size(ChronosDimens.GaugeSize),
            trackWidth = 4.dp,
            indicatorWidth = 6.dp,
            showTicks = true,
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    text = if (running) "STOPWATCH RUNNING" else "STOPWATCH STOPPED",
                    style = ChronosType.Eyebrow,
                    color = ChronosColors.Outline,
                    modifier = Modifier.padding(bottom = 4.dp),
                )
                Row(verticalAlignment = Alignment.Bottom) {
                    Text(
                        text = "%02d:%02d".format(minutes, seconds),
                        style = ChronosType.DisplayHeroMobile,
                        color = ChronosColors.OnSurface,
                        fontWeight = FontWeight.Medium,
                    )
                    Text(
                        text = ".%02d".format(hundredths),
                        style = ChronosType.HeadlineMd,
                        color = MaterialTheme.colorScheme.primaryContainer,
                        fontWeight = FontWeight.SemiBold,
                        modifier = Modifier.padding(start = 2.dp, bottom = 8.dp),
                    )
                }
                Text(
                    text = "Lap $lapCount",
                    style = ChronosType.LabelMd,
                    color = MaterialTheme.colorScheme.secondary,
                )
            }
        }

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 24.dp, start = 24.dp, end = 24.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.Top,
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                ChronosActionButton(
                    icon = "flag",
                    contentDescription = "Lap",
                    onClick = onLap,
                    enabled = elapsedMillis > 0L,
                )
                Text(
                    text = "Lap",
                    style = ChronosType.LabelSm,
                    color = ChronosColors.Outline,
                    modifier = Modifier.padding(top = 6.dp),
                )
            }

            PlayPausePill(running = running, onClick = onToggle)

            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                ChronosActionButton(
                    icon = "replay",
                    contentDescription = "Reset",
                    onClick = onReset,
                )
                Text(
                    text = "Reset",
                    style = ChronosType.LabelSm,
                    color = ChronosColors.Outline,
                    modifier = Modifier.padding(top = 6.dp),
                )
            }
        }
    }
}

@Composable
private fun PlayPausePill(running: Boolean, onClick: () -> Unit) {
    val press = rememberPressState()
    val scale by androidx.compose.animation.core.animateFloatAsState(
        targetValue = if (press.isPressed) 0.95f else 1f,
        animationSpec = androidx.compose.animation.core.spring(dampingRatio = 0.5f, stiffness = 700f),
        label = "stopwatchPlayScale",
    )

    Box(
        modifier = Modifier
            .scale(scale)
            .height(80.dp)
            .clip(RoundedCornerShape(ChronosRadius.Pill))
            .background(MaterialTheme.colorScheme.primaryContainer)
            .clickable(
                interactionSource = press.interaction,
                indication = null,
                onClick = {
                    press.hapticTick()
                    onClick()
                },
            )
            .padding(horizontal = 40.dp),
        contentAlignment = Alignment.Center,
    ) {
        ChronosIcon(
            name = if (running) "pause" else "play_arrow",
            size = 36.dp,
            tint = MaterialTheme.colorScheme.onPrimary,
            filled = true,
        )
    }
}

@Composable
private fun LapRow(
    lap: Lap,
    isFastest: Boolean,
    isSlowest: Boolean,
    modifier: Modifier = Modifier,
) {
    val splitColor = when {
        isFastest -> MaterialTheme.colorScheme.tertiaryContainer
        isSlowest -> ChronosColors.SlowestLap
        else -> ChronosColors.OnSurface
    }

    Row(
        modifier = modifier
            .clip(RoundedCornerShape(ChronosRadius.Card))
            .padding(vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Row(
            modifier = Modifier.weight(1f),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                text = "%02d".format(lap.index),
                style = ChronosType.BodyMd,
                color = ChronosColors.OnSurface,
                fontWeight = FontWeight.SemiBold,
            )
            if (isFastest) {
                Spacer(Modifier.width(8.dp))
                LapBadge(
                    text = "Fastest Lap",
                    container = MaterialTheme.colorScheme.tertiaryContainer.copy(alpha = 0.20f),
                    content = MaterialTheme.colorScheme.tertiaryContainer,
                    border = MaterialTheme.colorScheme.tertiaryContainer.copy(alpha = 0.30f),
                )
            } else if (isSlowest) {
                Spacer(Modifier.width(8.dp))
                LapBadge(
                    text = "Slowest Lap",
                    container = ChronosColors.ErrorContainer.copy(alpha = 0.30f),
                    content = ChronosColors.Error,
                    border = ChronosColors.Error.copy(alpha = 0.30f),
                )
            }
        }
        Text(
            text = lap.splitText(),
            style = ChronosType.BodyLg,
            color = splitColor,
            fontWeight = if (isFastest || isSlowest) FontWeight.SemiBold else FontWeight.Medium,
            textAlign = TextAlign.Center,
            modifier = Modifier.weight(1f),
        )
        Text(
            text = lap.totalText(),
            style = ChronosType.BodyMd,
            color = ChronosColors.OnSurfaceVariant,
            textAlign = TextAlign.End,
            modifier = Modifier.weight(1f),
        )
    }
}

@Composable
private fun LapBadge(
    text: String,
    container: Color,
    content: Color,
    border: Color,
) {
    Text(
        text = text,
        style = ChronosType.LabelSm,
        color = content,
        fontWeight = FontWeight.Medium,
        modifier = Modifier
            .clip(RoundedCornerShape(ChronosRadius.Pill))
            .background(container)
            .border(1.dp, border, RoundedCornerShape(ChronosRadius.Pill))
            .padding(horizontal = 8.dp, vertical = 3.dp),
    )
}

private fun buildShareText(laps: List<Lap>): String = buildString {
    appendLine("Chronos stopwatch laps")
    appendLine()
    appendLine("Lap      Split        Overall")
    laps.forEach { lap ->
        appendLine(
            "%02d    %s    %s".format(
                lap.index,
                lap.splitText(),
                lap.totalText(),
            ),
        )
    }
}
