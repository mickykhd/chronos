package com.chronos.clock.ui

import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

/**
 * The four-tab bottom navigation dock: Alarm, Clock, Timer, Stopwatch.
 */
enum class ChronosDestination(
    val label: String,
    val icon: String,
    val order: Int,
) {
    Alarm("Alarm", "alarm", 0),
    Clock("Clock", "schedule", 1),
    Timer("Timer", "timer", 2),
    Stopwatch("Stopwatch", "timer_10", 3);

    companion object {
        val ordered: List<ChronosDestination> = entries.sortedBy { it.order }
    }
}

/** Convenience accessor for the M3 content colour at a given destination's state. */
@Composable
fun destinationColor(selected: Boolean): Color = if (selected) {
    MaterialTheme.colorScheme.primaryContainer
} else {
    MaterialTheme.colorScheme.outline
}
