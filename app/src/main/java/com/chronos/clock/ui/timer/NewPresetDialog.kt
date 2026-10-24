package com.chronos.clock.ui.timer

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.chronos.clock.ui.components.ChronosActionButton
import com.chronos.clock.ui.theme.ChronosColors
import com.chronos.clock.ui.theme.ChronosRadius
import com.chronos.clock.ui.theme.ChronosType

/**
 * Creates a timer preset: a name and a length in minutes.
 *
 * The length is set with a stepper so the dialog never depends on the keyboard
 * being open or on a numeric layout the user cannot reach.
 */
@Composable
fun NewPresetDialog(
    onDismiss: () -> Unit,
    onConfirm: (label: String, minutes: Long) -> Unit,
) {
    var label by remember { mutableStateOf("") }
    var minutes by remember { mutableStateOf(5L) }

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = ChronosColors.SurfaceContainerHigh,
        titleContentColor = ChronosColors.OnSurface,
        textContentColor = ChronosColors.OnSurface,
        title = {
            Text(text = "New preset", style = ChronosType.TitleLg, color = ChronosColors.OnSurface)
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                OutlinedTextField(
                    value = label,
                    onValueChange = { label = it.take(24) },
                    label = { Text("Preset name", style = ChronosType.BodyMd) },
                    singleLine = true,
                    shape = RoundedCornerShape(ChronosRadius.IconWell),
                    keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = MaterialTheme.colorScheme.primaryContainer,
                        unfocusedBorderColor = ChronosColors.OutlineVariant,
                        focusedTextColor = ChronosColors.OnSurface,
                        unfocusedTextColor = ChronosColors.OnSurface,
                        focusedLabelColor = MaterialTheme.colorScheme.primaryContainer,
                        unfocusedLabelColor = ChronosColors.Outline,
                        cursorColor = MaterialTheme.colorScheme.primaryContainer,
                    ),
                    modifier = Modifier.fillMaxWidth(),
                )

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = "Length",
                        style = ChronosType.BodyLg,
                        color = ChronosColors.OnSurface,
                        modifier = Modifier.weight(1f),
                    )
                    ChronosActionButton(
                        icon = "remove",
                        contentDescription = "One minute less",
                        size = 40.dp,
                        iconSize = 22.dp,
                        onClick = { minutes = (minutes - 1).coerceAtLeast(1L) },
                    )
                    Spacer(Modifier.padding(horizontal = 8.dp))
                    Text(
                        text = "$minutes min",
                        style = ChronosType.HeadlineSm,
                        color = ChronosColors.OnSurface,
                        modifier = Modifier.padding(horizontal = 8.dp),
                    )
                    ChronosActionButton(
                        icon = "add",
                        contentDescription = "One minute more",
                        size = 40.dp,
                        iconSize = 22.dp,
                        onClick = { minutes = (minutes + 1).coerceAtMost(720L) },
                    )
                }
            }
        },
        confirmButton = {
            TextButton(onClick = { onConfirm(label, minutes) }) {
                Text(text = "Save", style = ChronosType.LabelLg, color = MaterialTheme.colorScheme.primary)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(text = "Cancel", style = ChronosType.LabelLg, color = ChronosColors.Outline)
            }
        },
    )
}