package com.chronos.clock.ui.stopwatch

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.chronos.clock.data.ChronosRepository
import com.chronos.clock.data.Lap
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch

class StopwatchViewModel(private val repository: ChronosRepository) : ViewModel() {

    val laps = repository.laps

    var running by mutableStateOf(false)
        private set

    /** Elapsed time accumulated across previous run segments. */
    private var accumulatedMillis by mutableLongStateOf(0L)

    /** Wall-clock instant at which the current run segment started. */
    private var segmentStartedAt by mutableLongStateOf(0L)

    var elapsedMillis by mutableLongStateOf(0L)
        private set

    val lapCount: Int get() = laps.value.size

    init {
        viewModelScope.launch {
            while (isActive) {
                if (running) {
                    elapsedMillis = accumulatedMillis + (System.currentTimeMillis() - segmentStartedAt)
                }
                delay(16)
            }
        }
    }

    fun toggle() {
        if (running) {
            accumulatedMillis = elapsedMillis
            running = false
        } else {
            segmentStartedAt = System.currentTimeMillis()
            running = true
        }
    }

    fun reset() {
        running = false
        accumulatedMillis = 0L
        segmentStartedAt = 0L
        elapsedMillis = 0L
        repository.clearLaps()
    }

    fun lap() {
        if (elapsedMillis <= 0L) return
        val previousTotal = laps.value.lastOrNull()?.totalMillis ?: 0L
        val total = elapsedMillis
        repository.addLap(
            Lap(
                index = laps.value.size + 1,
                splitMillis = total - previousTotal,
                totalMillis = total,
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
