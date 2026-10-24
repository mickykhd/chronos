package com.chronos.clock.widget

import android.app.AlarmManager
import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.BroadcastReceiver
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Paint
import android.widget.RemoteViews
import com.chronos.clock.MainActivity
import com.chronos.clock.R
import com.chronos.clock.core.describeZone
import java.time.LocalDateTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Locale
import kotlin.math.cos
import kotlin.math.sin

/**
 * The two home-screen widgets: a digital clock with date and weather, and an analog
 * dial.
 *
 * They are drawn with [RemoteViews] rather than Glance, so every piece of them is a
 * plain view a launcher can lay out and resize — no Compose host, no extra
 * dependency, and nothing that depends on which launcher is installed.
 *
 * A widget clock needs refreshing far more often than the system's own
 * `updatePeriodMillis` minimum (thirty minutes), so [scheduleWidgetTick] books an
 * alarm for just past each minute boundary and [ChronosWidgetTickReceiver] uses it
 * to redraw and book the next one.
 */

private val WidgetTimeFormat = DateTimeFormatter.ofPattern("h:mm")
private val WidgetTimeFormat24 = DateTimeFormatter.ofPattern("HH:mm")
private val WidgetDateFormat = DateTimeFormatter.ofPattern("EEE, MMM d")
private val WidgetWeekFormat = DateTimeFormatter.ofPattern("EEEE", Locale.US)

private const val WIDGET_TICK_REQUEST_CODE = 6200

/** Palette drawn for the widget dial, mirroring the Chronos M3 Ember theme. */
private val DialAccent = 0xFFFFB782u.toInt()
private val DialAccentSoft = 0xFFFFDCC5u.toInt()
private val DialBrass = 0xFFF5E1A7u.toInt()
private val DialTrack = 0xFF333537u.toInt()
private val DialPlate = 0xFF121416u.toInt()
private val DialOutline = 0xFF9F8D82u.toInt()
private val OnDialPivot = 0xFF4F2500u.toInt()

private fun openAppPendingIntent(context: Context): PendingIntent {
    val intent = Intent(context, MainActivity::class.java).apply {
        flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
    }
    return PendingIntent.getActivity(
        context,
        WIDGET_TICK_REQUEST_CODE + 1,
        intent,
        PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE,
    )
}

private fun repository(context: Context): com.chronos.clock.data.ChronosRepository? =
    (context.applicationContext as? com.chronos.clock.ChronosApplication)?.repository

/** The digital clock widget: local time, the date, the local city and the weather. */
class ChronosClockWidgetProvider : AppWidgetProvider() {

    override fun onUpdate(
        context: Context,
        appWidgetManager: AppWidgetManager,
        appWidgetIds: IntArray,
    ) = render(context, appWidgetIds)

    override fun onEnabled(context: Context) = scheduleWidgetTick(context)

    companion object {
        fun render(context: Context, appWidgetIds: IntArray? = null) {
            val manager = context.getSystemService(AppWidgetManager::class.java) ?: return
            val ids = appWidgetIds ?: manager.getAppWidgetIds(
                ComponentName(context, ChronosClockWidgetProvider::class.java),
            )
            if (ids.isEmpty()) return

            val repository = repository(context)
            val use24Hour = repository?.use24Hour?.value ?: false
            val zone = ZoneId.systemDefault()
            val now = LocalDateTime.now(zone)
            val weather = repository?.cachedWeather(zone.id)

            val views = RemoteViews(context.packageName, R.layout.widget_clock).apply {
                setOnClickPendingIntent(R.id.widget_root, openAppPendingIntent(context))
                setCharSequence(
                    R.id.widget_time,
                    "setText",
                    now.format(if (use24Hour) WidgetTimeFormat24 else WidgetTimeFormat),
                )
                if (use24Hour) {
                    setViewVisibility(R.id.widget_meridiem, android.view.View.GONE)
                } else {
                    setViewVisibility(R.id.widget_meridiem, android.view.View.VISIBLE)
                    setCharSequence(R.id.widget_meridiem, "setText", if (now.hour < 12) "AM" else "PM")
                }
                setCharSequence(R.id.widget_date, "setText", now.format(WidgetDateFormat))
                setCharSequence(R.id.widget_city, "setText", describeZone(zone).city)
                if (weather == null) {
                    setViewVisibility(R.id.widget_weather, android.view.View.GONE)
                } else {
                    setViewVisibility(R.id.widget_weather, android.view.View.VISIBLE)
                    setCharSequence(
                        R.id.widget_weather,
                        "setText",
                        "${weather.temperatureCelsius}° ${weather.condition}",
                    )
                }
            }

            manager.updateAppWidget(ids, views)
        }
    }
}

/** The analog clock widget: a dial drawn to a bitmap, refreshed every minute. */
class ChronosAnalogWidgetProvider : AppWidgetProvider() {

    override fun onUpdate(
        context: Context,
        appWidgetManager: AppWidgetManager,
        appWidgetIds: IntArray,
    ) = render(context, appWidgetIds)

    override fun onEnabled(context: Context) = scheduleWidgetTick(context)

    companion object {
        fun render(context: Context, appWidgetIds: IntArray? = null) {
            val manager = context.getSystemService(AppWidgetManager::class.java) ?: return
            val ids = appWidgetIds ?: manager.getAppWidgetIds(
                ComponentName(context, ChronosAnalogWidgetProvider::class.java),
            )
            if (ids.isEmpty()) return

            val now = LocalDateTime.now(ZoneId.systemDefault())
            val seconds = ((System.currentTimeMillis() % 60_000L) / 1000L).toInt()
            val dial = drawWidgetDial(
                sizePx = 512,
                hours = now.hour,
                minutes = now.minute,
                seconds = seconds,
            )

            val views = RemoteViews(context.packageName, R.layout.widget_analog).apply {
                setOnClickPendingIntent(R.id.widget_analog_root, openAppPendingIntent(context))
                setImageViewBitmap(R.id.widget_analog_dial, dial)
                setCharSequence(
                    R.id.widget_analog_label,
                    "setText",
                    now.format(WidgetWeekFormat),
                )
            }

            manager.updateAppWidget(ids, views)
        }
    }
}

/** Draws the widget's analog dial at [sizePx] pixels. */
fun drawWidgetDial(
    sizePx: Int,
    hours: Int,
    minutes: Int,
    seconds: Int,
): Bitmap {
    val bitmap = Bitmap.createBitmap(sizePx, sizePx, Bitmap.Config.ARGB_8888)
    val canvas = Canvas(bitmap)
    val centre = sizePx / 2f
    val radius = sizePx / 2f * 0.92f

    fun fill(argb: Int) = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = argb
        style = Paint.Style.FILL
    }

    fun stroke(color: Int, width: Float) = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        this.color = color
        this.strokeWidth = width
        style = Paint.Style.STROKE
        strokeCap = Paint.Cap.ROUND
    }

    // Dial plate and rim.
    val rimWidth = sizePx * 0.014f
    canvas.drawCircle(centre, centre, radius, fill(DialPlate))
    canvas.drawCircle(centre, centre, radius - rimWidth / 2f, stroke(DialTrack, rimWidth))
    canvas.drawCircle(
        centre,
        centre,
        radius * 0.48f,
        stroke(DialTrack, sizePx * 0.008f),
    )

    // Hour markers.
    val markerLength = radius * 0.26f
    val markerWidth = radius * 0.10f
    val inset = radius * 0.15f
    (0 until 12).forEach { index ->
        val angle = Math.toRadians(index * 30.0 - 90.0)
        val dirX = cos(angle).toFloat()
        val dirY = sin(angle).toFloat()
        val outer = radius - inset
        val inner = outer - if (index % 3 == 0) markerLength else markerLength * 0.40f
        canvas.drawLine(
            centre + dirX * inner,
            centre + dirY * inner,
            centre + dirX * outer,
            centre + dirY * outer,
            stroke(
                if (index == 0) DialAccent else DialOutline,
                markerWidth,
            ),
        )
    }

    fun drawHand(angleDeg: Double, length: Float, width: Float, color: Int) {
        val angle = Math.toRadians(angleDeg - 90.0)
        val dirX = cos(angle).toFloat()
        val dirY = sin(angle).toFloat()
        canvas.drawLine(
            centre - dirX * radius * 0.09f,
            centre - dirY * radius * 0.09f,
            centre + dirX * length,
            centre + dirY * length,
            stroke(color, width),
        )
    }

    val minuteValue = minutes + seconds / 60.0
    val hourValue = (hours % 12) + minuteValue / 60.0

    drawHand(hourValue * 30.0, radius * 0.56f, radius * 0.085f, DialAccentSoft)
    drawHand(minuteValue * 6.0, radius * 0.72f, radius * 0.055f, DialAccent)
    drawHand(seconds * 6.0, radius * 0.88f, radius * 0.026f, DialBrass)

    // Pivot nut.
    canvas.drawCircle(centre, centre, radius * 0.075f, fill(OnDialPivot))
    canvas.drawCircle(centre, centre, radius * 0.075f + rimWidth, stroke(DialAccent, sizePx * 0.006f))

    return bitmap
}

/** Refreshes both widgets now. */
fun refreshAllWidgets(context: Context) {
    ChronosClockWidgetProvider.render(context)
    ChronosAnalogWidgetProvider.render(context)
}

/** Refreshes both widgets, then books the next minute tick. */
class ChronosWidgetTickReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        refreshAllWidgets(context)
        scheduleWidgetTick(context)
    }
}

/**
 * Books an alarm for just past the next minute boundary, so the widgets are right
 * every minute without the app being open.
 */
fun scheduleWidgetTick(context: Context) {
    val manager = context.getSystemService(AlarmManager::class.java) ?: return
    val intent = Intent(context, ChronosWidgetTickReceiver::class.java)
    val pending = PendingIntent.getBroadcast(
        context,
        WIDGET_TICK_REQUEST_CODE,
        intent,
        PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE,
    )
    val now = System.currentTimeMillis()
    manager.cancel(pending)
    manager.setWindow(AlarmManager.RTC, now - (now % 60_000L) + 61_000L, 5_000L, pending)
}