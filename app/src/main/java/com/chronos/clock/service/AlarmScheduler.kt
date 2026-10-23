package com.chronos.clock.service

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import com.chronos.clock.core.nextAlarmTrigger
import com.chronos.clock.data.Alarm
import java.time.LocalDateTime
import java.time.ZoneId

/**
 * Schedules alarms and timers with [AlarmManager] so they fire exactly, even when
 * the app is not running.
 */
object AlarmScheduler {

    private const val ALARM_PREFIX = "chronos_alarm_"
    private const val TIMER_PREFIX = "chronos_timer_"

    // ------------------------------------------------------------- alarms

    fun schedule(context: Context, alarm: Alarm) {
        if (!alarm.enabled) {
            cancel(context, alarm)
            return
        }
        val manager = context.getSystemService(AlarmManager::class.java) ?: return
        val triggerAt = nextAlarmTrigger(
            now = LocalDateTime.now(),
            hour = alarm.hour,
            minute = alarm.minute,
            days = alarm.days,
        ).atZone(ZoneId.systemDefault()).toInstant().toEpochMilli()

        val intent = Intent(context, AlarmReceiver::class.java).apply {
            action = "$ALARM_PREFIX${alarm.id}"
            putExtra(EXTRA_ALARM_ID, alarm.id)
            putExtra(EXTRA_ALARM_HOUR, alarm.hour)
            putExtra(EXTRA_ALARM_MINUTE, alarm.minute)
            putExtra(EXTRA_ALARM_LABEL, alarm.label)
        }

        val pending = PendingIntent.getBroadcast(
            context,
            alarm.id.hashCode(),
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE,
        )

        runCatching {
            if (canScheduleExact(manager)) {
                manager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, triggerAt, pending)
            } else {
                manager.setWindow(
                    AlarmManager.RTC_WAKEUP,
                    triggerAt,
                    60_000L,
                    pending,
                )
            }
        }
    }

    fun cancel(context: Context, alarm: Alarm) {
        val manager = context.getSystemService(AlarmManager::class.java) ?: return
        val intent = Intent(context, AlarmReceiver::class.java).apply {
            action = "$ALARM_PREFIX${alarm.id}"
        }
        val pending = PendingIntent.getBroadcast(
            context,
            alarm.id.hashCode(),
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE,
        )
        manager.cancel(pending)
    }

    // ------------------------------------------------------------- timers

    fun scheduleTimer(context: Context, timerId: String, endAtMillis: Long) {
        val manager = context.getSystemService(AlarmManager::class.java) ?: return
        val intent = Intent(context, TimerReceiver::class.java).apply {
            action = "$TIMER_PREFIX$timerId"
            putExtra(EXTRA_TIMER_ID, timerId)
        }
        val pending = PendingIntent.getBroadcast(
            context,
            timerId.hashCode(),
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE,
        )
        runCatching {
            if (canScheduleExact(manager)) {
                manager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, endAtMillis, pending)
            } else {
                manager.setWindow(AlarmManager.RTC_WAKEUP, endAtMillis, 30_000L, pending)
            }
        }
    }

    fun cancelTimer(context: Context, timerId: String) {
        val manager = context.getSystemService(AlarmManager::class.java) ?: return
        val intent = Intent(context, TimerReceiver::class.java).apply {
            action = "$TIMER_PREFIX$timerId"
        }
        val pending = PendingIntent.getBroadcast(
            context,
            timerId.hashCode(),
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE,
        )
        manager.cancel(pending)
    }

    private fun canScheduleExact(manager: AlarmManager): Boolean =
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            manager.canScheduleExactAlarms()
        } else {
            true
        }

    const val EXTRA_ALARM_ID = "alarm_id"
    const val EXTRA_ALARM_HOUR = "alarm_hour"
    const val EXTRA_ALARM_MINUTE = "alarm_minute"
    const val EXTRA_ALARM_LABEL = "alarm_label"
    const val EXTRA_TIMER_ID = "timer_id"
}
