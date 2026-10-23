package com.chronos.clock.service

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import com.chronos.clock.MainActivity
import com.chronos.clock.R
import com.chronos.clock.data.Alarm
import com.chronos.clock.data.RunningTimer

/**
 * Notification channels and builders for alarms and running timers.
 */
object ChronosNotifications {

    const val ALARM_CHANNEL = "chronos_alarms"
    const val TIMER_CHANNEL = "chronos_timers"

    const val TIMER_NOTIFICATION_ID = 4201
    const val ALARM_NOTIFICATION_ID = 4202

    fun ensureChannels(context: Context) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) return
        val manager = context.getSystemService(NotificationManager::class.java) ?: return

        val alarms = NotificationChannel(
            ALARM_CHANNEL,
            context.getString(R.string.channel_alarms),
            NotificationManager.IMPORTANCE_HIGH,
        ).apply {
            description = context.getString(R.string.channel_alarms_desc)
            enableVibration(true)
            lockscreenVisibility = Notification.VISIBILITY_PUBLIC
            setBypassDnd(true)
        }

        val timers = NotificationChannel(
            TIMER_CHANNEL,
            context.getString(R.string.channel_timers),
            NotificationManager.IMPORTANCE_LOW,
        ).apply {
            description = context.getString(R.string.channel_timers_desc)
            setShowBadge(false)
        }

        manager.createNotificationChannel(alarms)
        manager.createNotificationChannel(timers)
    }

    fun openAppIntent(context: Context, requestCode: Int, extra: String? = null): PendingIntent {
        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
            if (extra != null) putExtra(extra, true)
        }
        return PendingIntent.getActivity(
            context,
            requestCode,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE,
        )
    }

    fun timerNotification(
        context: Context,
        timer: RunningTimer,
        ongoing: Boolean,
    ): Notification {
        val title = if (ongoing) "${timer.label} · ${timer.remainingText()}" else "${timer.label} finished"
        val text = if (ongoing) "Time remaining" else "Your ${timer.label} timer has finished"

        return NotificationCompat.Builder(context, TIMER_CHANNEL)
            .setSmallIcon(R.drawable.ic_notification)
            .setContentTitle(title)
            .setContentText(text)
            .setContentIntent(openAppIntent(context, timer.id.hashCode()))
            .setOngoing(ongoing)
            .setSilent(ongoing)
            .setShowWhen(false)
            .setCategory(if (ongoing) Notification.CATEGORY_PROGRESS else Notification.CATEGORY_ALARM)
            .setPriority(if (ongoing) NotificationCompat.PRIORITY_LOW else NotificationCompat.PRIORITY_HIGH)
            .build()
    }

    fun showTimerFinished(context: Context, timer: RunningTimer) {
        val manager = context.getSystemService(NotificationManager::class.java) ?: return
        manager.notify(TIMER_NOTIFICATION_ID, timerNotification(context, timer, ongoing = false))
    }

    fun alarmNotification(context: Context, alarm: Alarm): Notification {
        val fullScreen = openAppIntent(context, alarm.id.hashCode(), EXTRA_RINGING)
        return NotificationCompat.Builder(context, ALARM_CHANNEL)
            .setSmallIcon(R.drawable.ic_notification)
            .setContentTitle(alarm.timeText12())
            .setContentText(alarm.label)
            .setPriority(NotificationCompat.PRIORITY_MAX)
            .setCategory(Notification.CATEGORY_ALARM)
            .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
            .setAutoCancel(false)
            .setOngoing(true)
            .setContentIntent(fullScreen)
            .setFullScreenIntent(fullScreen, true)
            .setVibrate(longArrayOf(0, 500, 500, 500, 500, 500, 500, 500))
            .build()
    }

    const val EXTRA_RINGING = "com.chronos.clock.RINGING"
}
