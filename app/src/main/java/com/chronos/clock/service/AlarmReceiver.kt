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
        val hour = intent.getIntExtra(AlarmScheduler.EXTRA_ALARM_HOUR, 0)
        val minute = intent.getIntExtra(AlarmScheduler.EXTRA_ALARM_MINUTE, 0)
        val label = intent.getStringExtra(AlarmScheduler.EXTRA_ALARM_LABEL).orEmpty()

        ChronosNotifications.ensureChannels(context)

        AlarmService.start(context, id, hour, minute, label)

        // Book the next occurrence so repeating alarms keep going.
        val app = context.applicationContext as? com.chronos.clock.ChronosApplication
        val alarm = app?.repository?.alarms?.value?.firstOrNull { it.id == id }
        if (alarm != null && alarm.enabled) {
            AlarmScheduler.schedule(context, alarm)
        }
    }
}
