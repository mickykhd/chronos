package com.chronos.clock.ui.alarm

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
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
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.chronos.clock.core.rememberNow
import com.chronos.clock.data.Alarm
import com.chronos.clock.data.VibrationPattern
import com.chronos.clock.service.AlarmSounds
import com.chronos.clock.ui.components.ChronosFab
import com.chronos.clock.ui.components.ChronosIcon
import com.chronos.clock.ui.components.ChronosIconButton
import com.chronos.clock.ui.components.ChronosPill
import com.chronos.clock.ui.components.ChronosTimePicker
import com.chronos.clock.ui.components.TimePickerField
import com.chronos.clock.ui.theme.ChronosColors
import com.chronos.clock.ui.theme.ChronosDimens
import com.chronos.clock.ui.theme.ChronosRadius
import com.chronos.clock.ui.theme.ChronosType
import java.time.format.DateTimeFormatter
import java.util.Locale

private val DayLetters = listOf("M", "T", "W", "T", "F", "S", "S")
private val IsoDays = listOf(1, 2, 3, 4, 5, 6, 7)

/**
 * The full-screen alarm editor.
 *
 * Everything an alarm can be configured with lives here: the time, the repeat days,
 * the sound, the vibration pattern and the label — the same set of options a phone
 * clock exposes.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AlarmEditScreen(
    existing: Alarm?,
    use24Hour: Boolean,
    onSave: (Alarm) -> Unit,
    onDelete: (() -> Unit)?,
    onClose: () -> Unit,
    modifier: Modifier = Modifier,
) {
    var hour by remember { mutableStateOf(existing?.hour ?: nextDefaultHour()) }
    var minute by remember { mutableStateOf(existing?.minute ?: 0) }
    var days by remember { mutableStateOf(existing?.days ?: setOf<Int>()) }
    var label by remember { mutableStateOf(existing?.label ?: "Alarm") }
    var soundUri by remember { mutableStateOf(existing?.soundUri) }
    var soundTitle by remember { mutableStateOf(existing?.soundTitle ?: "Default") }
    var vibrate by remember { mutableStateOf(existing?.vibrate ?: true) }
    var vibration by remember { mutableStateOf(existing?.vibrationPattern ?: VibrationPattern.Standard) }
    var snooze by remember { mutableStateOf(existing?.snoozeEnabled ?: true) }

    var showTimePicker by remember { mutableStateOf(false) }
    var showSoundPicker by remember { mutableStateOf(false) }
    var showVibrationPicker by remember { mutableStateOf(false) }

    val now by rememberNow()
    val draft = existing?.copy(
        hour = hour,
        minute = minute,
        days = days,
        label = label,
        soundUri = soundUri,
        soundTitle = soundTitle,
        vibrate = vibrate,
        vibrationPattern = vibration,
        snoozeEnabled = snooze,
        enabled = true,
    ) ?: Alarm(
        id = com.chronos.clock.data.ChronosRepository.newId(),
        hour = hour,
        minute = minute,
        days = days,
        label = label,
        soundUri = soundUri,
        soundTitle = soundTitle,
        vibrate = vibrate,
        vibrationPattern = vibration,
        snoozeEnabled = snooze,
    )

    val trigger = draft.nextTrigger(now.toLocalDateTime())

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(ChronosColors.Surface)
            .statusBarsPadding(),
    ) {
        // --- App bar ---
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 8.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            ChronosIconButton(
                icon = "close",
                contentDescription = "Close",
                onClick = onClose,
                tint = ChronosColors.OnSurface,
            )
            Spacer(Modifier.weight(1f))
            ChronosPill(
                text = "Save",
                leadingIcon = "check",
                leadingIconTint = MaterialTheme.colorScheme.onPrimaryContainer,
                container = MaterialTheme.colorScheme.primaryContainer,
                contentColor = MaterialTheme.colorScheme.onPrimaryContainer,
                textStyle = ChronosType.LabelLg,
                height = 40.dp,
                onClick = { onSave(draft) },
            )
        }

        Column(
            modifier = Modifier
                .weight(1f)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 16.dp),
        ) {
            // --- The time ---
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 12.dp, bottom = 4.dp)
                    .clip(RoundedCornerShape(ChronosRadius.Card))
                    .clickable { showTimePicker = true }
                    .padding(vertical = 20.dp),
                contentAlignment = Alignment.Center,
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Row(verticalAlignment = Alignment.Bottom) {
                        Text(
                            text = formatTime(hour, minute, use24Hour),
                            style = ChronosType.DisplayHero,
                            color = MaterialTheme.colorScheme.onSurface,
                        )
                        if (!use24Hour) {
                            Spacer(Modifier.width(10.dp))
                            Text(
                                text = if (hour < 12) "AM" else "PM",
                                style = ChronosType.LabelMd,
                                color = MaterialTheme.colorScheme.onPrimaryContainer,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier
                                    .clip(RoundedCornerShape(ChronosRadius.Pill))
                                    .background(MaterialTheme.colorScheme.primaryContainer)
                                    .padding(horizontal = 10.dp, vertical = 4.dp)
                                    .padding(bottom = 14.dp),
                            )
                        }
                    }
                    Text(
                        text = trigger.format(DateTimeFormatter.ofPattern("EEE, MMM d", Locale.US)),
                        style = ChronosType.BodyLg,
                        color = ChronosColors.OnSurfaceVariant,
                        modifier = Modifier.padding(top = 4.dp),
                    )
                }
            }

            // --- Repeat ---
            SettingsSection(title = "Repeat") {
                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    IsoDays.forEachIndexed { index, iso ->
                        EditDayPill(
                            letter = DayLetters[index],
                            selected = iso in days,
                            onClick = {
                                days = if (iso in days) days - iso else days + iso
                            },
                        )
                    }
                }
                Text(
                    text = repeatSummary(days),
                    style = ChronosType.BodyMd,
                    color = ChronosColors.Outline,
                    modifier = Modifier.padding(top = 10.dp),
                )
            }

            // --- Alert ---
            SettingsSection(title = "Alert") {
                SettingsRow(
                    icon = "volume_up",
                    title = "Sound",
                    value = soundTitle,
                    onClick = { showSoundPicker = true },
                )
                SettingsRow(
                    icon = "vibration",
                    title = "Vibrate",
                    value = if (!vibrate) "Off" else vibration.label,
                    onClick = { showVibrationPicker = true },
                )
                SettingsRow(
                    icon = "snooze",
                    title = "Snooze",
                    value = if (snooze) "${com.chronos.clock.service.AlarmActions.SNOOZE_MINUTES} min" else "Off",
                    onClick = { snooze = !snooze },
                )
            }

            // --- More ---
            SettingsSection(title = "More") {
                OutlinedTextField(
                    value = label,
                    onValueChange = { label = it.take(40) },
                    label = { Text("Alarm label", style = ChronosType.BodyMd) },
                    singleLine = true,
                    shape = RoundedCornerShape(ChronosRadius.IconWell),
                    keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = MaterialTheme.colorScheme.primaryContainer,
                        unfocusedBorderColor = ChronosColors.OutlineVariant,
                        focusedTextColor = MaterialTheme.colorScheme.onSurface,
                        unfocusedTextColor = MaterialTheme.colorScheme.onSurface,
                        focusedLabelColor = MaterialTheme.colorScheme.primaryContainer,
                        unfocusedLabelColor = ChronosColors.Outline,
                        cursorColor = MaterialTheme.colorScheme.primaryContainer,
                    ),
                    modifier = Modifier.fillMaxWidth(),
                )
            }

            if (onDelete != null) {
                Spacer(Modifier.height(12.dp))
                ChronosPill(
                    text = "Delete alarm",
                    leadingIcon = "delete",
                    leadingIconTint = MaterialTheme.colorScheme.error,
                    container = ChronosColors.SurfaceContainerLow,
                    contentColor = MaterialTheme.colorScheme.error,
                    textStyle = ChronosType.LabelLg,
                    height = 52.dp,
                    onClick = onDelete,
                    modifier = Modifier.fillMaxWidth(),
                )
            }

            Spacer(Modifier.height(32.dp))
        }
    }

    // --- Time picker ---
    if (showTimePicker) {
        Dialog(
            onDismissRequest = { showTimePicker = false },
            properties = DialogProperties(usePlatformDefaultWidth = false),
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(ChronosColors.Surface.copy(alpha = 0.92f)),
                contentAlignment = Alignment.Center,
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    // Big, comfortable readout of the value being edited.
                    Row(
                        modifier = Modifier
                            .clip(RoundedCornerShape(ChronosRadius.Pill))
                            .background(ChronosColors.SurfaceContainerLow)
                            .padding(horizontal = 20.dp, vertical = 10.dp),
                        verticalAlignment = Alignment.Bottom,
                    ) {
                        Text(
                            text = formatTime(hour, minute, use24Hour),
                            style = ChronosType.DisplayHeroMobile,
                            color = MaterialTheme.colorScheme.onSurface,
                        )
                        if (!use24Hour) {
                            Spacer(Modifier.width(8.dp))
                            Text(
                                text = if (hour < 12) "AM" else "PM",
                                style = ChronosType.LabelMd,
                                color = MaterialTheme.colorScheme.onPrimaryContainer,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier
                                    .clip(RoundedCornerShape(ChronosRadius.Pill))
                                    .background(MaterialTheme.colorScheme.primaryContainer)
                                    .padding(horizontal = 8.dp, vertical = 2.dp)
                                    .padding(bottom = 10.dp),
                            )
                        }
                    }

                    Spacer(Modifier.height(16.dp))

                    ChronosTimePicker(
                        hour = hour,
                        minute = minute,
                        onTimeChange = { h, m ->
                            hour = h
                            minute = m
                        },
                        is24Hour = use24Hour,
                    )

                    Spacer(Modifier.height(20.dp))

                    ChronosPill(
                        text = "Done",
                        container = MaterialTheme.colorScheme.primaryContainer,
                        contentColor = MaterialTheme.colorScheme.onPrimaryContainer,
                        textStyle = ChronosType.LabelLg,
                        height = 48.dp,
                        onClick = { showTimePicker = false },
                        modifier = Modifier.fillMaxWidth(0.5f),
                    )
                }
            }
        }
    }

    // --- Sound picker ---
    if (showSoundPicker) {
        SoundPickerSheet(
            selectedUri = soundUri,
            onSelect = { sound ->
                soundUri = sound?.uri
                soundTitle = sound?.title ?: "Default"
                showSoundPicker = false
            },
            onDismiss = { showSoundPicker = false },
        )
    }

    // --- Vibration picker ---
    if (showVibrationPicker) {
        VibrationPickerSheet(
            selected = if (vibrate) vibration else VibrationPattern.Off,
            onSelect = { pattern ->
                vibrate = pattern != VibrationPattern.Off
                if (vibrate) vibration = pattern
                showVibrationPicker = false
            },
            onDismiss = { showVibrationPicker = false },
        )
    }
}

@Composable
private fun SettingsSection(
    title: String,
    content: @Composable () -> Unit,
) {
    Column(modifier = Modifier.padding(top = 20.dp)) {
        Text(
            text = title.uppercase(Locale.US),
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
                .padding(16.dp),
            content = { content() },
        )
    }
}

@Composable
private fun SettingsRow(
    icon: String,
    title: String,
    value: String,
    onClick: () -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .clickable(onClick = onClick)
            .padding(vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        ChronosIcon(
            name = icon,
            size = 22.dp,
            tint = MaterialTheme.colorScheme.primaryContainer,
        )
        Spacer(Modifier.width(14.dp))
        Text(
            text = title,
            style = ChronosType.BodyLg,
            color = MaterialTheme.colorScheme.onSurface,
            modifier = Modifier.weight(1f),
        )
        Text(
            text = value,
            style = ChronosType.BodyMd,
            color = ChronosColors.Outline,
        )
        Spacer(Modifier.width(6.dp))
        ChronosIcon(
            name = "arrow_forward_ios",
            size = 16.dp,
            tint = ChronosColors.Outline,
        )
    }
}

@Composable
private fun EditDayPill(
    letter: String,
    selected: Boolean,
    onClick: () -> Unit,
) {
    val container by androidx.compose.animation.animateColorAsState(
        targetValue = if (selected) {
            MaterialTheme.colorScheme.primaryContainer
        } else {
            ChronosColors.SurfaceContainerHigh
        },
        animationSpec = androidx.compose.animation.core.spring(dampingRatio = 0.7f, stiffness = 520f),
        label = "editDayContainer",
    )
    val content by androidx.compose.animation.animateColorAsState(
        targetValue = if (selected) {
            MaterialTheme.colorScheme.onPrimaryContainer
        } else {
            ChronosColors.Outline
        },
        animationSpec = androidx.compose.animation.core.spring(dampingRatio = 0.7f, stiffness = 520f),
        label = "editDayContent",
    )

    Box(
        modifier = Modifier
            .size(ChronosDimens.DayPillSize)
            .clip(CircleShape)
            .background(container)
            .clickable(onClick = onClick),
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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun SoundPickerSheet(
    selectedUri: String?,
    onSelect: (AlarmSounds.Sound?) -> Unit,
    onDismiss: () -> Unit,
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val context = androidx.compose.ui.platform.LocalContext.current
    var sounds by remember { mutableStateOf(emptyList<AlarmSounds.Sound>()) }

    androidx.compose.runtime.LaunchedEffect(context) {
        sounds = AlarmSounds.load(context)
    }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = ChronosColors.SurfaceContainerHigh,
        contentColor = ChronosColors.OnSurface,
    ) {
        Column(modifier = Modifier.navigationBarsPadding()) {
            Text(
                text = "Alarm sound",
                style = ChronosType.TitleLg,
                color = ChronosColors.OnSurface,
                modifier = Modifier.padding(start = 24.dp, end = 24.dp, bottom = 4.dp),
            )
            Text(
                text = "Sounds installed on this device",
                style = ChronosType.BodyMd,
                color = ChronosColors.Outline,
                modifier = Modifier.padding(start = 24.dp, end = 24.dp, bottom = 12.dp),
            )

            // The list is scrollable: a phone can have dozens of installed sounds.
            LazyColumn(modifier = Modifier.heightIn(max = 420.dp)) {
                item {
                    SoundRow(
                        title = "Default",
                        selected = selectedUri == null,
                        onClick = { onSelect(null) },
                    )
                }
                sounds.groupBy { it.kind }.forEach { (kind, group) ->
                    item {
                        Text(
                            text = kind.label.uppercase(Locale.US),
                            style = ChronosType.Eyebrow,
                            color = MaterialTheme.colorScheme.primaryContainer,
                            fontWeight = FontWeight.SemiBold,
                            modifier = Modifier.padding(
                                start = 24.dp,
                                end = 24.dp,
                                top = 16.dp,
                                bottom = 4.dp,
                            ),
                        )
                    }
                    items(group, key = { it.uri }) { sound ->
                        SoundRow(
                            title = sound.title,
                            selected = sound.uri == selectedUri,
                            onClick = { onSelect(sound) },
                        )
                    }
                }
            }

            Spacer(Modifier.height(16.dp))
        }
    }
}

@Composable
private fun SoundRow(
    title: String,
    selected: Boolean,
    onClick: () -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(horizontal = 24.dp, vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            text = title,
            style = ChronosType.BodyLg,
            color = if (selected) MaterialTheme.colorScheme.primaryContainer else ChronosColors.OnSurface,
            fontWeight = if (selected) FontWeight.SemiBold else FontWeight.Normal,
            modifier = Modifier.weight(1f),
        )
        if (selected) {
            ChronosIcon(
                name = "check",
                size = 20.dp,
                tint = MaterialTheme.colorScheme.primaryContainer,
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun VibrationPickerSheet(
    selected: VibrationPattern,
    onSelect: (VibrationPattern) -> Unit,
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
                text = "Vibrate",
                style = ChronosType.TitleLg,
                color = ChronosColors.OnSurface,
                modifier = Modifier.padding(start = 24.dp, end = 24.dp, bottom = 4.dp),
            )
            Text(
                text = "Choose the pattern used when the alarm rings",
                style = ChronosType.BodyMd,
                color = ChronosColors.Outline,
                modifier = Modifier.padding(start = 24.dp, end = 24.dp, bottom = 12.dp),
            )
            VibrationPattern.entries.forEach { pattern ->
                SoundRow(
                    title = pattern.label,
                    selected = pattern == selected,
                    onClick = { onSelect(pattern) },
                )
            }
            Spacer(Modifier.height(16.dp))
        }
    }
}

private fun formatTime(hour: Int, minute: Int, use24Hour: Boolean): String =
    if (use24Hour) {
        "%02d:%02d".format(hour, minute)
    } else {
        val h = when (hour % 12) {
            0 -> 12
            else -> hour % 12
        }
        "%d:%02d".format(h, minute)
    }

private fun repeatSummary(days: Set<Int>): String = when {
    days.isEmpty() -> "One time"
    days.size == 7 -> "Every day"
    days == setOf(6, 7) -> "Sat, Sun"
    days == setOf(1, 2, 3, 4, 5) -> "Weekdays"
    days == setOf(1, 2, 3, 4, 5, 6) -> "Mon – Sat"
    else -> days.sorted().joinToString(", ") {
        java.time.DayOfWeek.of(it).getDisplayName(java.time.format.TextStyle.SHORT, Locale.US)
    }
}

private fun nextDefaultHour(): Int = (java.time.LocalTime.now().hour + 1) % 24
