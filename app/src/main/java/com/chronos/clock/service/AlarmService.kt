package com.chronos.clock.service

import android.app.Notification
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.IBinder
import android.os.PowerManager
import androidx.core.app.NotificationCompat
import androidx.core.app.ServiceCompat
import com.chronos.clock.data.Alarm
import com.chronos.clock.data.VibrationPattern
import com.chronos.clock.ui.alarm.AlarmRingActivity

/**
 * Rings the alarm.
 *
 * The tone is played with an explicit [MediaPlayer] on the alarm stream (with audio
 * focus), the configured vibration pattern is fired, and the full-screen alarm UI is
 * brought to the front. The service keeps running until the alarm is dismissed or
 * snoozed, so the tone does not stop after a few seconds.
 */
class AlarmService : Service() {

    private var player: RingtonePlayer? = null
    private var wakeLock: PowerManager.WakeLock? = null

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onCreate() {
        super.onCreate()
        ChronosNotifications.ensureChannels(this)
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        ChronosNotifications.ensureChannels(this)

        val alarm = Alarm(
            id = intent?.getStringExtra(EXTRA_ID).orEmpty(),
            hour = intent?.getIntExtra(EXTRA_HOUR, 0) ?: 0,
            minute = intent?.getIntExtra(EXTRA_MINUTE, 0) ?: 0,
            label = intent?.getStringExtra(EXTRA_LABEL) ?: "Alarm",
            soundUri = intent?.getStringExtra(EXTRA_SOUND_URI),
            soundTitle = intent?.getStringExtra(EXTRA_SOUND_TITLE) ?: "Default",
            vibrate = intent?.getBooleanExtra(EXTRA_VIBRATE, true) ?: true,
            vibrationPattern = VibrationPattern.fromId(intent?.getStringExtra(EXTRA_VIBRATION)),
            snoozeEnabled = intent?.getBooleanExtra(EXTRA_SNOOZE, true) ?: true,
        )

        startForegroundCompat(alarm)
        startRinging(alarm)
        return START_STICKY
    }

    private fun startForegroundCompat(alarm: Alarm) {
        val notification = ChronosNotifications.alarmNotification(this, alarm)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            ServiceCompat.startForeground(
                this,
                ChronosNotifications.ALARM_NOTIFICATION_ID,
                notification,
                android.content.pm.ServiceInfo.FOREGROUND_SERVICE_TYPE_MEDIA_PLAYBACK,
            )
        } else {
            startForeground(ChronosNotifications.ALARM_NOTIFICATION_ID, notification)
        }
    }

    private fun startRinging(alarm: Alarm) {
        if (player == null) player = RingtonePlayer(this)

        val uri = alarm.soundUri?.let { runCatching { Uri.parse(it) }.getOrNull() }
            ?: AlarmSounds.defaultAlarmSound(this)?.let { runCatching { Uri.parse(it.uri) }.getOrNull() }

        // `play` falls back to the bundled tone, so the alarm is never silent.
        player?.play(uri)

        if (alarm.vibrate) {
            AlarmVibrator.vibrate(this, alarm.vibrationPattern)
        }

        // Keep the CPU awake so the tone keeps playing with the screen off.
        val powerManager = getSystemService(PowerManager::class.java)
        wakeLock = powerManager?.newWakeLock(
            PowerManager.PARTIAL_WAKE_LOCK,
            "Chronos::AlarmService",
        )?.apply {
            setReferenceCounted(false)
            acquire(MAX_RING_MILLIS)
        }

        launchRingScreen(alarm)
    }

    private fun launchRingScreen(alarm: Alarm) {
        val intent = Intent(this, AlarmRingActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or
                Intent.FLAG_ACTIVITY_CLEAR_TOP or
                Intent.FLAG_ACTIVITY_NO_USER_ACTION
            putExtra(EXTRA_ID, alarm.id)
            putExtra(EXTRA_HOUR, alarm.hour)
            putExtra(EXTRA_MINUTE, alarm.minute)
            putExtra(EXTRA_LABEL, alarm.label)
            putExtra(EXTRA_SOUND_TITLE, alarm.soundTitle)
            putExtra(EXTRA_SNOOZE, alarm.snoozeEnabled)
        }
        runCatching { startActivity(intent) }
    }

    override fun onDestroy() {
        player?.stop()
        player = null
        wakeLock?.let { if (it.isHeld) it.release() }
        wakeLock = null
        super.onDestroy()
    }

    companion object {
        private const val MAX_RING_MILLIS = 10 * 60_000L

        const val EXTRA_ID = "extra_id"
        const val EXTRA_HOUR = "extra_hour"
        const val EXTRA_MINUTE = "extra_minute"
        const val EXTRA_LABEL = "extra_label"
        const val EXTRA_SOUND_URI = "extra_sound_uri"
        const val EXTRA_SOUND_TITLE = "extra_sound_title"
        const val EXTRA_VIBRATE = "extra_vibrate"
        const val EXTRA_VIBRATION = "extra_vibration"
        const val EXTRA_SNOOZE = "extra_snooze"

        fun start(context: Context, alarm: Alarm) {
            val intent = Intent(context, AlarmService::class.java).apply {
                putExtra(EXTRA_ID, alarm.id)
                putExtra(EXTRA_HOUR, alarm.hour)
                putExtra(EXTRA_MINUTE, alarm.minute)
                putExtra(EXTRA_LABEL, alarm.label)
                putExtra(EXTRA_SOUND_URI, alarm.soundUri)
                putExtra(EXTRA_SOUND_TITLE, alarm.soundTitle)
                putExtra(EXTRA_VIBRATE, alarm.vibrate)
                putExtra(EXTRA_VIBRATION, alarm.vibrationPattern.id)
                putExtra(EXTRA_SNOOZE, alarm.snoozeEnabled)
            }
            runCatching { context.startForegroundService(intent) }
        }

        fun stop(context: Context) {
            context.stopService(Intent(context, AlarmService::class.java))
        }
    }
}

/**
 * Handles the Dismiss and Snooze notification actions.
 */
class AlarmActionReceiver : android.content.BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        val id = intent.getStringExtra(AlarmService.EXTRA_ID).orEmpty()
        when (intent.action) {
            ACTION_DISMISS -> {
                AlarmActions.dismiss(context)
                dismissNotification(context)
            }
            ACTION_SNOOZE -> {
                AlarmActions.snooze(context, id)
                dismissNotification(context)
            }
        }
    }

    private fun dismissNotification(context: Context) {
        val manager = context.getSystemService(android.app.NotificationManager::class.java)
        manager?.cancel(ChronosNotifications.ALARM_NOTIFICATION_ID)
    }

    companion object {
        const val ACTION_DISMISS = "com.chronos.clock.action.DISMISS"
        const val ACTION_SNOOZE = "com.chronos.clock.action.SNOOZE"

        fun dismissIntent(context: Context, alarmId: String): PendingIntent {
            val intent = Intent(context, AlarmActionReceiver::class.java).apply {
                action = ACTION_DISMISS
                putExtra(AlarmService.EXTRA_ID, alarmId)
            }
            return PendingIntent.getBroadcast(
                context,
                alarmId.hashCode() + 1,
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE,
            )
        }

        fun snoozeIntent(context: Context, alarmId: String): PendingIntent {
            val intent = Intent(context, AlarmActionReceiver::class.java).apply {
                action = ACTION_SNOOZE
                putExtra(AlarmService.EXTRA_ID, alarmId)
            }
            return PendingIntent.getBroadcast(
                context,
                alarmId.hashCode() + 2,
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE,
            )
        }
    }
}
