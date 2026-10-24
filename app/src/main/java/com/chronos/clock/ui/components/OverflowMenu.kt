package com.chronos.clock.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.chronos.clock.ui.theme.ChronosColors
import com.chronos.clock.ui.theme.ChronosType

/**
 * One entry in an overflow menu.
 */
data class OverflowItem(
    val label: String,
    val icon: String? = null,
    val onClick: () -> Unit,
)

/**
 * The kebab menu behind the top-bar overflow button.
 */
@Composable
fun ChronosOverflowMenu(
    expanded: Boolean,
    onDismiss: () -> Unit,
    items: List<OverflowItem>,
) {
    DropdownMenu(
        expanded = expanded,
        onDismissRequest = onDismiss,
        containerColor = ChronosColors.SurfaceContainerHigh,
    ) {
        items.forEach { item ->
            DropdownMenuItem(
                text = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        item.icon?.let { icon ->
                            ChronosIcon(
                                name = icon,
                                size = 20.dp,
                                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                            )
                            Spacer(Modifier.width(12.dp))
                        }
                        Text(
                            text = item.label,
                            style = ChronosType.BodyLg,
                            color = MaterialTheme.colorScheme.onSurface,
                        )
                    }
                },
                onClick = {
                    onDismiss()
                    item.onClick()
                },
            )
        }
    }
}
