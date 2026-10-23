package com.chronos.clock.service

import android.app.Service
import android.content.Context
import android.content.Intent
import android.media.AudioAttributes
import android.media.Ringtone
import android.media.RingtoneManager
import android.os.Build
import android.os.IBinder
import android.os.PowerManager
import androidx.core.app.ServiceCompat
import com.chronos.clock.data.Alarm
import com.chronos.clock.ui.alarm.AlarmViewModel

/**
 * Rings the alarm: plays the system alarm tone, posts a full-screen notification and
 * holds a partial wake lock so the sound survives a sleeping device.
 */
class AlarmService : Service() {

    private var ringtone: Ringtone? = null
    private var wakeLock: PowerManager.WakeLock? = null

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        ChronosNotifications.ensureChannels(this)

        val id = intent?.getStringExtra(EXTRA_ID).orEmpty()
        val hour = intent?.getIntExtra(EXTRA_HOUR, 0) ?: 0
        val minute = intent?.getIntExtra(EXTRA_MINUTE, 0) ?: 0
        val label = intent?.getStringExtra(EXTRA_LABEL).orEmpty()

        val alarm = Alarm(
            id = id.ifBlank { "ringing" },
            hour = hour,
            minute = minute,
            label = label.ifBlank { "Alarm" },
        )

        startForegroundCompat(alarm)

        startRinging()
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

    private fun startRinging() {
        if (ringtone?.isPlaying == true) return

        val powerManager = getSystemService(PowerManager::class.java)
        wakeLock = powerManager?.newWakeLock(
            PowerManager.PARTIAL_WAKE_LOCK,
            "Chronos::AlarmService",
        )?.apply {
            setReferenceCounted(false)
            acquire(MAX_RING_MILLIS)
        }

        val uri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_ALARM)
            ?: RingtoneManager.getDefaultUri(RingtoneManager.TYPE_RINGTONE)
        ringtone = RingtoneManager.getRingtone(this, uri)?.apply {
            audioAttributes = AudioAttributes.Builder()
                .setUsage(AudioAttributes.USAGE_ALARM)
                .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                .build()
            isLooping = true
            play()
        }
    }

    override fun onDestroy() {
        ringtone?.stop()
        ringtone = null
        wakeLock?.let { if (it.isHeld) it.release() }
        wakeLock = null
        super.onDestroy()
    }

    companion object {
        private const val MAX_RING_MILLIS = 90_000L

        const val EXTRA_ID = "extra_id"
        const val EXTRA_HOUR = "extra_hour"
        const val EXTRA_MINUTE = "extra_minute"
        const val EXTRA_LABEL = "extra_label"

        fun start(context: Context, id: String, hour: Int, minute: Int, label: String) {
            val intent = Intent(context, AlarmService::class.java).apply {
                putExtra(EXTRA_ID, id)
                putExtra(EXTRA_HOUR, hour)
                putExtra(EXTRA_MINUTE, minute)
                putExtra(EXTRA_LABEL, label)
            }
            context.startForegroundService(intent)
        }

        fun stop(context: Context) {
            context.stopService(Intent(context, AlarmService::class.java))
        }
    }
}
