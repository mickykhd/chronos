package com.chronos.clock.service

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent

/**
 * Fires when a countdown timer reaches zero.
 */
class TimerReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        val id = intent.getStringExtra(AlarmScheduler.EXTRA_TIMER_ID) ?: return
        ChronosNotifications.ensureChannels(context)

        val app = context.applicationContext as? com.chronos.clock.ChronosApplication
        val timer = app?.repository?.timers?.value?.firstOrNull { it.id == id }

        if (timer != null) {
            ChronosNotifications.showTimerFinished(context, timer)
            app?.repository?.removeTimer(id)
        } else {
            ChronosNotifications.showTimerFinished(
                context,
                com.chronos.clock.data.RunningTimer(
                    id = id,
                    label = "Timer",
                    icon = "timer",
                    totalMillis = 0L,
                    remainingMillis = 0L,
                ),
            )
        }
        TimerService.refresh(context)
    }
}
