package com.chronos.clock.ui.timer

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.chronos.clock.data.ChronosRepository
import com.chronos.clock.data.RunningTimer
import com.chronos.clock.data.TimerPreset
import com.chronos.clock.service.TimerScheduler
import com.chronos.clock.service.TimerService
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch

class TimerViewModel(application: Application) : AndroidViewModel(application) {

    private val repository: ChronosRepository =
        (application as com.chronos.clock.ChronosApplication).repository

    private val context = application.applicationContext

    val timers = repository.timers

    /** Built-in presets plus any the user has created. */
    val presets: StateFlow<List<TimerPreset>> =
        repository.customPresets.map { custom -> ChronosRepository.TimerPresets + custom }
            .stateIn(viewModelScope, SharingStarted.Eagerly, ChronosRepository.TimerPresets)

    /** A preset added through the "New preset" dialog. */
    fun addCustomPreset(preset: TimerPreset) = repository.addCustomPreset(preset)

    fun removeCustomPreset(id: String) = repository.removeCustomPreset(id)

    /** The timer the hero gauge is bound to: the first running one, else the first. */
    val activeTimer: RunningTimer?
        get() = timers.value.firstOrNull { it.running } ?: timers.value.firstOrNull()

    init {
        viewModelScope.launch {
            while (isActive) {
                refreshCountdowns()
                delay(200)
            }
        }
    }

    private fun refreshCountdowns() {
        val now = System.currentTimeMillis()
        var changed = false
        val updated = timers.value.map { timer ->
            if (timer.running && timer.endAtMillis > 0L) {
                val remaining = (timer.endAtMillis - now).coerceAtLeast(0L)
                if (remaining != timer.remainingMillis) {
                    changed = true
                    timer.copy(remainingMillis = remaining)
                } else {
                    timer
                }
            } else {
                timer
            }
        }
        if (changed) repository.updateTimersTransient(updated)
    }

    fun startPreset(preset: TimerPreset) {
        startTimer(preset.label, preset.icon, preset.durationMillis)
    }

    fun startTimer(label: String, icon: String, durationMillis: Long) {
        val timer = RunningTimer(
            id = ChronosRepository.newId(),
            label = label,
            icon = icon,
            totalMillis = durationMillis,
            remainingMillis = durationMillis,
            endAtMillis = System.currentTimeMillis() + durationMillis,
            running = true,
        )
        repository.addTimer(timer)
        TimerScheduler.schedule(context, timer)
        TimerService.start(context)
    }

    /** Convenient single entry for the play/pause button. */
    fun pauseOrResume(timer: RunningTimer) {
        if (timer.running) pause(timer) else resume(timer)
    }

    fun pause(timer: RunningTimer) {
        val remaining = if (timer.endAtMillis > 0L) {
            (timer.endAtMillis - System.currentTimeMillis()).coerceAtLeast(0L)
        } else {
            timer.remainingMillis
        }
        repository.updateTimer(timer.copy(running = false, endAtMillis = 0L, remainingMillis = remaining))
        TimerScheduler.cancel(context, timer.id)
        TimerService.refresh(context)
    }

    fun resume(timer: RunningTimer) {
        if (timer.remainingMillis <= 0L) return
        val updated = timer.copy(
            running = true,
            endAtMillis = System.currentTimeMillis() + timer.remainingMillis,
        )
        repository.updateTimer(updated)
        TimerScheduler.schedule(context, updated)
        TimerService.start(context)
    }

    fun reset(timer: RunningTimer) {
        val updated = timer.copy(
            running = false,
            endAtMillis = 0L,
            remainingMillis = timer.totalMillis,
        )
        repository.updateTimer(updated)
        TimerScheduler.cancel(context, timer.id)
        TimerService.refresh(context)
    }

    fun cancel(timer: RunningTimer) {
        repository.removeTimer(timer.id)
        TimerScheduler.cancel(context, timer.id)
        TimerService.refresh(context)
    }

    fun addMinutes(timer: RunningTimer, minutes: Long) {
        val added = minutes * 60_000L
        val newTotal = timer.totalMillis + added
        val newRemaining = timer.remainingMillis + added
        val updated = timer.copy(
            totalMillis = newTotal,
            remainingMillis = newRemaining,
            endAtMillis = if (timer.running) System.currentTimeMillis() + newRemaining else 0L,
        )
        repository.updateTimer(updated)
        if (timer.running) TimerScheduler.schedule(context, updated)
    }
}
