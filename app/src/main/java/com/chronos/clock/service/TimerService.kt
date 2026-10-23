package com.chronos.clock.service

import android.app.Service
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.IBinder
import androidx.core.app.ServiceCompat
import com.chronos.clock.ChronosApplication
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch

/**
 * Keeps an ongoing notification alive while a countdown timer is running, and
 * refreshes it every second with the live remaining time.
 */
class TimerService : Service() {

    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Default)
    private var ticker: Job? = null

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onCreate() {
        super.onCreate()
        ChronosNotifications.ensureChannels(this)
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        ChronosNotifications.ensureChannels(this)
        val running = runningTimer()

        if (running == null) {
            stopSelf()
            return START_NOT_STICKY
        }

        startForegroundCompat(running)

        if (ticker?.isActive != true) {
            ticker = scope.launch {
                while (isActive) {
                    val current = runningTimer()
                    if (current == null) {
                        stopSelf()
                        break
                    }
                    val manager = getSystemService(android.app.NotificationManager::class.java)
                    manager?.notify(
                        ChronosNotifications.TIMER_NOTIFICATION_ID,
                        ChronosNotifications.timerNotification(this@TimerService, current, ongoing = true),
                    )
                    delay(1_000)
                }
            }
        }

        return START_STICKY
    }

    private fun startForegroundCompat(timer: com.chronos.clock.data.RunningTimer) {
        val notification = ChronosNotifications.timerNotification(this, timer, ongoing = true)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            ServiceCompat.startForeground(
                this,
                ChronosNotifications.TIMER_NOTIFICATION_ID,
                notification,
                android.content.pm.ServiceInfo.FOREGROUND_SERVICE_TYPE_SPECIAL_USE,
            )
        } else {
            startForeground(ChronosNotifications.TIMER_NOTIFICATION_ID, notification)
        }
    }

    private fun runningTimer(): com.chronos.clock.data.RunningTimer? {
        val app = applicationContext as? ChronosApplication ?: return null
        return app.repository.timers.value.firstOrNull { it.running }
    }

    override fun onDestroy() {
        ticker?.cancel()
        scope.cancel()
        super.onDestroy()
    }

    companion object {
        fun start(context: Context) {
            val intent = Intent(context, TimerService::class.java)
            runCatching { context.startForegroundService(intent) }
        }

        /** Re-evaluates whether an ongoing notification is still warranted. */
        fun refresh(context: Context) {
            val app = context.applicationContext as? ChronosApplication ?: return
            val running = app.repository.timers.value.firstOrNull { it.running }
            if (running == null) {
                context.stopService(Intent(context, TimerService::class.java))
            } else {
                start(context)
            }
        }
    }
}
