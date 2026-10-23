package com.chronos.clock.service

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.chronos.clock.ChronosApplication

/**
 * Re-books every enabled alarm after a reboot or an app update.
 */
class BootReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        val action = intent.action ?: return
        if (action != Intent.ACTION_BOOT_COMPLETED && action != Intent.ACTION_MY_PACKAGE_REPLACED) return

        ChronosNotifications.ensureChannels(context)

        val app = context.applicationContext as? ChronosApplication ?: return
        app.repository.alarms.value
            .filter { it.enabled }
            .forEach { AlarmScheduler.schedule(context, it) }

        app.repository.timers.value
            .filter { it.running && it.endAtMillis > 0L }
            .forEach { AlarmScheduler.scheduleTimer(context, it.id, it.endAtMillis) }
    }
}
