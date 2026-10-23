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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ExperimentalMaterial3Api
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
import com.chronos.clock.ui.alarm.AlarmScreen
import com.chronos.clock.ui.alarm.AlarmViewModel
import com.chronos.clock.ui.clock.ClockScreen
import com.chronos.clock.ui.clock.ClockViewModel
import com.chronos.clock.ui.components.ChronosBottomBar
import com.chronos.clock.ui.components.ChronosIcon
import com.chronos.clock.ui.components.ChronosTextButton
import com.chronos.clock.ui.theme.ChronosColors
import com.chronos.clock.ui.theme.ChronosMotion
import com.chronos.clock.ui.theme.ChronosRadius
import com.chronos.clock.ui.theme.ChronosType
import com.chronos.clock.ui.stopwatch.StopwatchScreen
import com.chronos.clock.ui.stopwatch.StopwatchViewModel
import com.chronos.clock.ui.timer.TimerScreen
import com.chronos.clock.ui.timer.TimerViewModel
import java.time.ZoneId
import java.time.format.DateTimeFormatter

@Composable
fun ChronosApp() {
    var destination by rememberSaveable { mutableStateOf(ChronosDestination.Alarm) }

    val clockViewModel: ClockViewModel = viewModel(factory = ChronosViewModelFactory.Factory)
    val alarmViewModel: AlarmViewModel = viewModel(factory = ChronosViewModelFactory.Factory)
    val timerViewModel: TimerViewModel = viewModel(factory = ChronosViewModelFactory.Factory)
    val stopwatchViewModel: StopwatchViewModel = viewModel(factory = ChronosViewModelFactory.Factory)

    val pinnedCities by clockViewModel.cities.collectAsStateWithLifecycle()

    var showCitySheet by rememberSaveable { mutableStateOf(false) }
    var showCompareSheet by rememberSaveable { mutableStateOf(false) }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(ChronosColors.Surface),
    ) {
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
                ChronosDestination.Alarm -> AlarmScreen(viewModel = alarmViewModel)
                ChronosDestination.Clock -> ClockScreen(
                    viewModel = clockViewModel,
                    onAddCity = { showCitySheet = true },
                    onCompareTimes = { showCompareSheet = true },
                )
                ChronosDestination.Timer -> TimerScreen(viewModel = timerViewModel)
                ChronosDestination.Stopwatch -> StopwatchScreen(viewModel = stopwatchViewModel)
            }
        }

        ChronosBottomBar(
            selected = destination,
            onSelect = { target ->
                if (target != destination) destination = target
            },
            modifier = Modifier.align(Alignment.BottomCenter),
        )
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
                                .background(ChronosColors.SecondaryContainer),
                            contentAlignment = Alignment.Center,
                        ) {
                            ChronosIcon(
                                name = "public",
                                size = 20.dp,
                                tint = ChronosColors.Primary,
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
                            contentColor = ChronosColors.Primary,
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
                            color = ChronosColors.PrimaryContainer,
                            fontWeight = FontWeight.Medium,
                        )
                    }
                }
            }
            Spacer(Modifier.height(24.dp))
        }
    }
}
