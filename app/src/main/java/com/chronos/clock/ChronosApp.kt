package com.chronos.clock

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.chronos.clock.core.rememberNow
import com.chronos.clock.data.ChronosRepository
import com.chronos.clock.data.WorldCity
import com.chronos.clock.ui.ChronosDestination
import com.chronos.clock.ui.ChronosViewModelFactory
import com.chronos.clock.ui.alarm.AlarmEditScreen
import com.chronos.clock.ui.alarm.AlarmScreen
import com.chronos.clock.ui.alarm.AlarmViewModel
import com.chronos.clock.ui.clock.ClockScreen
import com.chronos.clock.ui.clock.ClockViewModel
import com.chronos.clock.ui.components.ChronosBottomBar
import com.chronos.clock.ui.components.ChronosIcon
import com.chronos.clock.ui.components.ChronosTextButton
import com.chronos.clock.ui.settings.SettingsScreen
import com.chronos.clock.ui.stopwatch.StopwatchScreen
import com.chronos.clock.ui.stopwatch.StopwatchViewModel
import com.chronos.clock.ui.theme.ChronosColors
import com.chronos.clock.ui.theme.ChronosMotion
import com.chronos.clock.ui.theme.ChronosType
import com.chronos.clock.ui.timer.TimerScreen
import com.chronos.clock.ui.timer.TimerViewModel
import java.time.ZoneId
import java.time.format.DateTimeFormatter

/**
 * The screens Chronos can show. Tabs sit at the root; settings and the alarm editor
 * are pushed on top of them.
 */
private sealed interface ChronosScreen {
    data object Tabs : ChronosScreen
    data object Settings : ChronosScreen
    data class EditAlarm(val alarmId: String?) : ChronosScreen
}

@Composable
fun ChronosApp() {
    val clockViewModel: ClockViewModel = viewModel(factory = ChronosViewModelFactory.Factory)
    val alarmViewModel: AlarmViewModel = viewModel(factory = ChronosViewModelFactory.Factory)
    val timerViewModel: TimerViewModel = viewModel(factory = ChronosViewModelFactory.Factory)
    val stopwatchViewModel: StopwatchViewModel = viewModel(factory = ChronosViewModelFactory.Factory)

    var destination by rememberSaveable { mutableStateOf(ChronosDestination.Alarm) }
    var screen by remember { mutableStateOf<ChronosScreen>(ChronosScreen.Tabs) }
    var showCitySheet by rememberSaveable { mutableStateOf(false) }
    var showCompareSheet by rememberSaveable { mutableStateOf(false) }

    val pinnedCities by clockViewModel.cities.collectAsStateWithLifecycle()
    val alarms by alarmViewModel.alarms.collectAsStateWithLifecycle()
    val theme by clockViewModel.theme.collectAsStateWithLifecycle()
    val use24Hour by clockViewModel.use24Hour.collectAsStateWithLifecycle()

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(ChronosColors.Surface),
    ) {
        AnimatedContent(
            targetState = screen,
            transitionSpec = {
                val isPush = targetState !is ChronosScreen.Tabs
                val enter = slideInHorizontally(
                    animationSpec = tween(ChronosMotion.Medium2, easing = ChronosMotion.EmphasizedDecelerate),
                ) { width -> if (isPush) width / 3 else 0 } +
                    fadeIn(animationSpec = tween(ChronosMotion.Medium1))
                val exit = slideOutHorizontally(
                    animationSpec = tween(ChronosMotion.Medium2, easing = ChronosMotion.EmphasizedAccelerate),
                ) { width -> if (isPush) -width / 8 else -width / 3 } +
                    fadeOut(animationSpec = tween(ChronosMotion.Short3))
                enter togetherWith exit
            },
            label = "appScreen",
        ) { current ->
            when (current) {
                is ChronosScreen.Tabs -> TabsContent(
                    destination = destination,
                    onDestinationChange = { destination = it },
                    clockViewModel = clockViewModel,
                    alarmViewModel = alarmViewModel,
                    timerViewModel = timerViewModel,
                    stopwatchViewModel = stopwatchViewModel,
                    onOpenSettings = { screen = ChronosScreen.Settings },
                    onEditAlarm = { id -> screen = ChronosScreen.EditAlarm(id) },
                    onAddCity = { showCitySheet = true },
                    onCompareTimes = { showCompareSheet = true },
                )

                is ChronosScreen.Settings -> SettingsScreen(
                    theme = theme,
                    use24Hour = use24Hour,
                    onThemeChange = clockViewModel::setTheme,
                    onUse24HourChange = clockViewModel::setUse24Hour,
                    onClose = { screen = ChronosScreen.Tabs },
                )

                is ChronosScreen.EditAlarm -> {
                    val existing = alarms.firstOrNull { it.id == current.alarmId }
                    AlarmEditScreen(
                        existing = existing,
                        use24Hour = use24Hour,
                        onSave = { alarm ->
                            if (existing == null) {
                                alarmViewModel.addAlarm(alarm)
                            } else {
                                alarmViewModel.updateAlarm(alarm)
                            }
                            screen = ChronosScreen.Tabs
                        },
                        onDelete = existing?.let {
                            {
                                alarmViewModel.removeAlarm(it.id)
                                screen = ChronosScreen.Tabs
                            }
                        },
                        onClose = { screen = ChronosScreen.Tabs },
                    )
                }
            }
        }
    }

    if (showCitySheet) {
        AddCitySheet(
            onDismiss = { showCitySheet = false },
            onPick = { city ->
                clockViewModel.addCity(city)
                showCitySheet = false
            },
        )
    }

    if (showCompareSheet) {
        CompareTimesSheet(
            cities = pinnedCities,
            onDismiss = { showCompareSheet = false },
        )
    }
}

@Composable
private fun TabsContent(
    destination: ChronosDestination,
    onDestinationChange: (ChronosDestination) -> Unit,
    clockViewModel: ClockViewModel,
    alarmViewModel: AlarmViewModel,
    timerViewModel: TimerViewModel,
    stopwatchViewModel: StopwatchViewModel,
    onOpenSettings: () -> Unit,
    onEditAlarm: (String?) -> Unit,
    onAddCity: () -> Unit,
    onCompareTimes: () -> Unit,
) {
    Box(modifier = Modifier.fillMaxSize()) {
        AnimatedContent(
            targetState = destination,
            transitionSpec = {
                val forward = targetState.order >= initialState.order
                val shift = if (forward) 1 else -1
                val enter = slideInHorizontally(
                    animationSpec = tween(ChronosMotion.Medium2, easing = ChronosMotion.EmphasizedDecelerate),
                ) { width -> shift * width / 10 } +
                    fadeIn(animationSpec = tween(ChronosMotion.Medium1)) +
                    scaleIn(
                        animationSpec = tween(ChronosMotion.Medium2, easing = ChronosMotion.EmphasizedDecelerate),
                        initialScale = 0.94f,
                    )
                val exit = slideOutHorizontally(
                    animationSpec = tween(ChronosMotion.Medium2, easing = ChronosMotion.EmphasizedAccelerate),
                ) { width -> -shift * width / 14 } +
                    fadeOut(animationSpec = tween(ChronosMotion.Short3)) +
                    scaleOut(
                        animationSpec = tween(ChronosMotion.Medium1, easing = ChronosMotion.EmphasizedAccelerate),
                        targetScale = 0.96f,
                    )
                enter togetherWith exit
            },
            label = "destinationContent",
        ) { current ->
            when (current) {
                ChronosDestination.Alarm -> AlarmScreen(
                    viewModel = alarmViewModel,
                    onAddAlarm = { onEditAlarm(null) },
                    onEditAlarm = { onEditAlarm(it) },
                    onOpenSettings = onOpenSettings,
                )
                ChronosDestination.Clock -> ClockScreen(
                    viewModel = clockViewModel,
                    onAddCity = onAddCity,
                    onCompareTimes = onCompareTimes,
                    onOpenSettings = onOpenSettings,
                )
                ChronosDestination.Timer -> TimerScreen(
                    viewModel = timerViewModel,
                    onOpenSettings = onOpenSettings,
                )
                ChronosDestination.Stopwatch -> StopwatchScreen(
                    viewModel = stopwatchViewModel,
                    onOpenSettings = onOpenSettings,
                )
            }
        }

        ChronosBottomBar(
            selected = destination,
            onSelect = { target ->
                if (target != destination) onDestinationChange(target)
            },
            modifier = Modifier.align(Alignment.BottomCenter),
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun AddCitySheet(
    onDismiss: () -> Unit,
    onPick: (WorldCity) -> Unit,
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
                text = "Add City",
                style = ChronosType.TitleLg,
                color = ChronosColors.OnSurface,
                modifier = Modifier.padding(start = 24.dp, end = 24.dp, bottom = 4.dp),
            )
            Text(
                text = "Pin a city to the world clock list",
                style = ChronosType.BodyMd,
                color = ChronosColors.Outline,
                modifier = Modifier.padding(start = 24.dp, end = 24.dp, bottom = 12.dp),
            )
            LazyColumn(modifier = Modifier.height(400.dp)) {
                items(ChronosRepository.AvailableCities, key = { it.id }) { city ->
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
                                name = "public",
                                size = 20.dp,
                                tint = MaterialTheme.colorScheme.primary,
                            )
                        }
                        Spacer(Modifier.width(12.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = city.city,
                                style = ChronosType.TitleMd,
                                color = ChronosColors.OnSurface,
                            )
                            Text(
                                text = city.country,
                                style = ChronosType.BodyMd,
                                color = ChronosColors.Outline,
                            )
                        }
                        ChronosTextButton(
                            text = "Add",
                            onClick = { onPick(city) },
                            container = ChronosColors.SurfaceContainer,
                            contentColor = MaterialTheme.colorScheme.primary,
                        )
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun CompareTimesSheet(
    cities: List<WorldCity>,
    onDismiss: () -> Unit,
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val now by rememberNow()
    val format = DateTimeFormatter.ofPattern("HH:mm")

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = ChronosColors.SurfaceContainerHigh,
        contentColor = ChronosColors.OnSurface,
    ) {
        Column(
            modifier = Modifier
                .navigationBarsPadding()
                .padding(horizontal = 24.dp),
        ) {
            Text(
                text = "Coordinate Meeting",
                style = ChronosType.TitleLg,
                color = ChronosColors.OnSurface,
            )
            Text(
                text = "Overlapping waking hours across your pinned cities",
                style = ChronosType.BodyMd,
                color = ChronosColors.Outline,
                modifier = Modifier.padding(top = 4.dp, bottom = 12.dp),
            )
            if (cities.isEmpty()) {
                Text(
                    text = "Pin a few cities to compare their working hours.",
                    style = ChronosType.BodyMd,
                    color = ChronosColors.Outline,
                    modifier = Modifier.padding(vertical = 16.dp),
                )
            } else {
                cities.forEach { city ->
                    val cityTime = now.withZoneSameInstant(ZoneId.of(city.zoneId))
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = city.city,
                                style = ChronosType.BodyLg,
                                color = ChronosColors.OnSurface,
                            )
                            Text(
                                text = city.country,
                                style = ChronosType.BodyMd,
                                color = ChronosColors.Outline,
                            )
                        }
                        Text(
                            text = cityTime.format(format),
                            style = ChronosType.TitleMd,
                            color = MaterialTheme.colorScheme.primaryContainer,
                            fontWeight = FontWeight.Medium,
                        )
                    }
                }
            }
            Spacer(Modifier.height(24.dp))
        }
    }
}
