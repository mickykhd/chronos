package com.chronos.clock.ui.stopwatch

import android.app.Application
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.chronos.clock.ChronosApplication
import com.chronos.clock.data.Lap
import com.chronos.clock.data.StopwatchState
import com.chronos.clock.service.StopwatchService
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch

/**
 * The stopwatch.
 *
 * The elapsed time is derived from [StopwatchState] in the repository rather than
 * held only in the UI: it is what the notification service reads, so the shade shows
 * the same number the screen does, and it survives a process death.
 */
class StopwatchViewModel(application: Application) : ViewModel() {

    private val appContext: Application = application

    private val repository = (application as ChronosApplication).repository

    val stopwatch = repository.stopwatch
    val laps = repository.laps

    /** Elapsed time, recomputed from the clock here so the UI ticks. */
    var elapsedMillis by mutableStateOf(0L)
        private set

    val lapCount: Int get() = laps.value.size

    init {
        elapsedMillis = repository.stopwatch.value.elapsed(System.currentTimeMillis())
        viewModelScope.launch {
            while (isActive) {
                elapsedMillis = repository.stopwatch.value.elapsed(System.currentTimeMillis())
                delay(16)
            }
        }
        if (repository.stopwatch.value.running) StopwatchService.refresh(appContext)
    }

    fun toggle() {
        val state = repository.stopwatch.value
        val now = System.currentTimeMillis()
        repository.updateStopwatch(
            if (state.running) {
                StopwatchState(
                    running = false,
                    runningSinceMillis = 0L,
                    accumulatedMillis = state.elapsed(now),
                )
            } else {
                StopwatchState(
                    running = true,
                    runningSinceMillis = now,
                    accumulatedMillis = state.accumulatedMillis,
                )
            },
        )
        StopwatchService.refresh(appContext)
    }

    fun reset() {
        repository.updateStopwatch(StopwatchState())
        repository.clearLaps()
        StopwatchService.refresh(appContext)
    }

    fun lap() {
        val elapsed = repository.stopwatch.value.elapsed(System.currentTimeMillis())
        if (elapsed <= 0L) return
        val previousTotal = laps.value.lastOrNull()?.totalMillis ?: 0L
        repository.addLap(
            Lap(
                index = laps.value.size + 1,
                splitMillis = elapsed - previousTotal,
                totalMillis = elapsed,
            ),
        )
    }

    /** The fastest lap, highlighted in tertiary brass. */
    val fastestLap: Lap?
        get() = laps.value.minByOrNull { it.splitMillis }

    /** The slowest lap, marked in muted crimson. */
    val slowestLap: Lap?
        get() = laps.value.maxByOrNull { it.splitMillis }
}