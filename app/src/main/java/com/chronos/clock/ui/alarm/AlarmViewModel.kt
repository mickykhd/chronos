package com.chronos.clock.ui.alarm

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import com.chronos.clock.ChronosApplication
import com.chronos.clock.data.Alarm
import com.chronos.clock.data.ChronosRepository
import com.chronos.clock.data.SleepSchedule
import com.chronos.clock.service.AlarmScheduler

class AlarmViewModel(application: Application) : AndroidViewModel(application) {

    private val repository: ChronosRepository = (application as ChronosApplication).repository

    private val context = application.applicationContext

    val alarms = repository.alarms
    val sleepSchedule = repository.sleepSchedule

    fun toggle(id: String, enabled: Boolean) {
        repository.toggleAlarm(id, enabled)
        // Keep the platform alarm in step with the switch.
        val alarm = repository.alarms.value.firstOrNull { it.id == id } ?: return
        if (enabled) AlarmScheduler.schedule(context, alarm) else AlarmScheduler.cancel(context, alarm)
    }

    fun toggleDay(id: String, day: Int) {
        repository.toggleAlarmDay(id, day)
        val alarm = repository.alarms.value.firstOrNull { it.id == id } ?: return
        if (alarm.enabled) AlarmScheduler.schedule(context, alarm) else AlarmScheduler.cancel(context, alarm)
    }

    fun addAlarm(hour: Int, minute: Int, label: String) {
        val alarm = Alarm(
            id = ChronosRepository.newId(),
            hour = hour,
            minute = minute,
            label = label.ifBlank { "Alarm" },
            days = emptySet(),
            enabled = true,
        )
        repository.addAlarm(alarm)
        AlarmScheduler.schedule(context, alarm)
    }

    fun updateAlarm(alarm: Alarm) {
        repository.updateAlarm(alarm)
        if (alarm.enabled) AlarmScheduler.schedule(context, alarm) else AlarmScheduler.cancel(context, alarm)
    }

    fun removeAlarm(id: String) {
        val alarm = repository.alarms.value.firstOrNull { it.id == id }
        repository.removeAlarm(id)
        alarm?.let { AlarmScheduler.cancel(context, it) }
    }

    fun updateSleepSchedule(schedule: SleepSchedule) = repository.updateSleepSchedule(schedule)
}
