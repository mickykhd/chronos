package com.chronos.clock.service

import android.content.Context
import java.time.LocalDateTime

/**
 * The two things you can do to a ringing alarm.
 */
object AlarmActions {

    const val SNOOZE_MINUTES = 9L

    fun dismiss(context: Context) {
        AlarmService.stop(context)
        val manager = context.getSystemService(android.app.NotificationManager::class.java)
        manager?.cancel(ChronosNotifications.ALARM_NOTIFICATION_ID)
    }

    /**
     * Silences the alarm and re-books it [SNOOZE_MINUTES] from now, as a one-shot.
     */
    fun snooze(context: Context, alarmId: String) {
        AlarmService.stop(context)
        val manager = context.getSystemService(android.app.NotificationManager::class.java)
        manager?.cancel(ChronosNotifications.ALARM_NOTIFICATION_ID)

        val app = context.applicationContext as? com.chronos.clock.ChronosApplication ?: return
        val alarm = app.repository.alarms.value.firstOrNull { it.id == alarmId } ?: return

        val trigger = LocalDateTime.now().plusMinutes(SNOOZE_MINUTES)
        val snoozed = alarm.copy(
            hour = trigger.hour,
            minute = trigger.minute,
            days = emptySet(),
            enabled = true,
        )
        app.repository.updateAlarm(snoozed)
        AlarmScheduler.schedule(context, snoozed)
    }
}
