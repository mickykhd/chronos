package com.chronos.clock.service

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent

/**
 * Fires when an alarm is due. Starts the ringing service and, for repeating alarms,
 * books the next occurrence.
 */
class AlarmReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        val id = intent.getStringExtra(AlarmScheduler.EXTRA_ALARM_ID) ?: return

        ChronosNotifications.ensureChannels(context)

        val app = context.applicationContext as? com.chronos.clock.ChronosApplication
        val alarm = app?.repository?.alarms?.value?.firstOrNull { it.id == id }

        if (alarm != null) {
            AlarmService.start(context, alarm)
            // Book the next occurrence so repeating alarms keep going.
            if (alarm.enabled) {
                AlarmScheduler.schedule(context, alarm)
            }
        } else {
            // Fall back to the extras if the alarm was deleted between scheduling
            // and firing.
            AlarmService.start(
                context,
                com.chronos.clock.data.Alarm(
                    id = id,
                    hour = intent.getIntExtra(AlarmScheduler.EXTRA_ALARM_HOUR, 0),
                    minute = intent.getIntExtra(AlarmScheduler.EXTRA_ALARM_MINUTE, 0),
                    label = intent.getStringExtra(AlarmScheduler.EXTRA_ALARM_LABEL).orEmpty(),
                ),
            )
        }
    }
}
