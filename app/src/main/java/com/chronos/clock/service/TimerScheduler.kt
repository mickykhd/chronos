package com.chronos.clock.service

import android.content.Context
import com.chronos.clock.data.RunningTimer

/**
 * Thin wrapper that schedules a timer's completion event.
 */
object TimerScheduler {

    fun schedule(context: Context, timer: RunningTimer) {
        if (!timer.running || timer.endAtMillis <= 0L) return
        AlarmScheduler.scheduleTimer(context, timer.id, timer.endAtMillis)
    }

    fun cancel(context: Context, timerId: String) = AlarmScheduler.cancelTimer(context, timerId)
}
