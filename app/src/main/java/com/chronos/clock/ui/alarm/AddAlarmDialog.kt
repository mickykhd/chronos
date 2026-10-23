package com.chronos.clock.ui.alarm

import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TimePicker
import androidx.compose.material3.rememberTimePickerState
import androidx.compose.runtime.Composable
import com.chronos.clock.ui.theme.ChronosColors
import com.chronos.clock.ui.theme.ChronosType

/**
 * The Material 3 time picker used to add a new alarm.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddAlarmDialog(
    onDismiss: () -> Unit,
    onConfirm: (hour: Int, minute: Int) -> Unit,
) {
    val state = rememberTimePickerState(
        initialHour = 7,
        initialMinute = 0,
        is24Hour = false,
    )

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = ChronosColors.SurfaceContainerHigh,
        titleContentColor = ChronosColors.OnSurface,
        textContentColor = ChronosColors.OnSurface,
        title = {
            Text(
                text = "New alarm",
                style = ChronosType.TitleLg,
                color = ChronosColors.OnSurface,
            )
        },
        text = {
            TimePicker(state = state)
        },
        confirmButton = {
            TextButton(onClick = { onConfirm(state.hour, state.minute) }) {
                Text(
                    text = "Save",
                    style = ChronosType.LabelLg,
                    color = ChronosColors.Primary,
                )
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(
                    text = "Cancel",
                    style = ChronosType.LabelLg,
                    color = ChronosColors.Outline,
                )
            }
        },
    )
}
