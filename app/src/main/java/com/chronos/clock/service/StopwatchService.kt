package com.chronos.clock.service

import android.app.Service
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.IBinder
import android.os.SystemClock
import androidx.core.app.ServiceCompat
import com.chronos.clock.ChronosApplication
import kotlinx.coroutines.cancel
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.Job
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch

/**
 * Keeps the running stopwatch in the notification area.
 *
 * The elapsed time is the repository's [com.chronos.clock.data.StopwatchState], so
 * whatever the UI is showing, the notification shows the same thing — even when the
 * app has been backgrounded and its UI is no longer ticking.
 */
class StopwatchService : Service() {

    private val scope = kotlinx.coroutines.CoroutineScope(
        kotlinx.coroutines.SupervisorJob() + kotlinx.coroutines.Dispatchers.Default,
    )
    private var ticker: kotlinx.coroutines.Job? = null

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onCreate() {
        super.onCreate()
        ChronosNotifications.ensureChannels(this)
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        ChronosNotifications.ensureChannels(this)
        val repository = (applicationContext as? ChronosApplication)?.repository
        val state = repository?.stopwatch?.value

        if (state == null || (!state.running && state.elapsed(System.currentTimeMillis()) <= 0L)) {
            stopSelf()
            return START_NOT_STICKY
        }

        startForegroundCompat(state)

        if (ticker?.isActive != true) {
            ticker = scope.launch {
                var lastElapsed = Long.MIN_VALUE
                while (isActive) {
                    val current = repository?.stopwatch?.value
                    if (current == null || (!current.running && current.accumulatedMillis <= 0L)) {
                        stopSelf()
                        break
                    }
                    val elapsed = current.elapsed(System.currentTimeMillis())
                    if (elapsed != lastElapsed) {
                        lastElapsed = elapsed
                        val manager = getSystemService(android.app.NotificationManager::class.java)
                        manager?.notify(
                            ChronosNotifications.STOPWATCH_NOTIFICATION_ID,
                            ChronosNotifications.stopwatchNotification(
                                this@StopwatchService,
                                elapsed,
                                repository.laps.value.size,
                            ),
                        )
                    }
                    kotlinx.coroutines.delay(250)
                }
            }
        }
        return START_STICKY
    }

    private fun startForegroundCompat(state: com.chronos.clock.data.StopwatchState) {
        val notification = ChronosNotifications.stopwatchNotification(
            this,
            state.elapsed(System.currentTimeMillis()),
            (applicationContext as? ChronosApplication)?.repository?.laps?.value?.size ?: 0,
        )
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            ServiceCompat.startForeground(
                this,
                ChronosNotifications.STOPWATCH_NOTIFICATION_ID,
                notification,
                android.content.pm.ServiceInfo.FOREGROUND_SERVICE_TYPE_SPECIAL_USE,
            )
        } else {
            startForeground(ChronosNotifications.STOPWATCH_NOTIFICATION_ID, notification)
        }
    }

    override fun onDestroy() {
        ticker?.cancel()
        scope.cancel()
        super.onDestroy()
    }

    companion object {
        fun refresh(context: Context) {
            val app = context.applicationContext as? ChronosApplication ?: return
            val state = app.repository.stopwatch.value
            if (state.running || state.elapsed(System.currentTimeMillis()) > 0L) {
                val intent = Intent(context, StopwatchService::class.java)
                runCatching { context.startForegroundService(intent) }
            } else {
                context.stopService(Intent(context, StopwatchService::class.java))
            }
        }
    }
}