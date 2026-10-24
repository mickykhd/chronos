package com.chronos.clock.data

import kotlinx.serialization.Serializable
import java.time.DayOfWeek
import java.time.format.TextStyle
import java.util.Locale

/**
 * The vibration pattern an alarm uses when it fires.
 */
@Serializable
enum class VibrationPattern(val id: String, val label: String) {
    Off("off", "Off"),
    Standard("standard", "Standard"),
    Double("double", "Twice"),
    Continuous("continuous", "Continuous");

    companion object {
        fun fromId(id: String?): VibrationPattern =
            entries.firstOrNull { it.id == id } ?: Standard
    }
}

/**
 * A single alarm.
 *
 * @param days ISO day-of-week numbers, 1 (Monday) through 7 (Sunday). Empty means
 *             a one-shot alarm that fires once and then switches itself off.
 * @param soundUri the ringtone to play; `null` means the device's default alarm sound.
 */
@Serializable
data class Alarm(
    val id: String,
    val hour: Int,
    val minute: Int,
    val label: String = "Alarm",
    val days: Set<Int> = emptySet(),
    val enabled: Boolean = true,
    val soundUri: String? = null,
    val soundTitle: String = "Default",
    val vibrate: Boolean = true,
    val vibrationPattern: VibrationPattern = VibrationPattern.Standard,
    val snoozeEnabled: Boolean = true,
) {
    val dayOfWeeks: List<DayOfWeek>
        get() = days.map { DayOfWeek.of(it) }.sortedBy { it.value }

    /** e.g. `Mon, Wed, Fri` — or `Tomorrow` / `Today` for one-shot alarms. */
    fun repeatSummary(): String = when {
        days.isEmpty() -> "One time"
        days.size == 7 -> "Every day"
        days == setOf(6, 7) -> "Sat, Sun"
        days == setOf(1, 2, 3, 4, 5) -> "Weekdays"
        days == setOf(1, 2, 3, 4, 5, 6) -> "Mon – Sat"
        else -> dayOfWeeks.joinToString(", ") { it.getDisplayName(TextStyle.SHORT, Locale.US) }
    }

    /** `07:00` */
    fun timeText(): String = "%02d:%02d".format(hour, minute)

    fun meridiem(): String = if (hour < 12) "AM" else "PM"

    /** `7:00 AM` — the twelve-hour form used in the alarm banner. */
    fun timeText12(): String {
        val h = when (hour % 12) {
            0 -> 12
            else -> hour % 12
        }
        return "$h:%02d ${meridiem()}".format(minute)
    }

    /** `07:00` in 24-hour form, for the 24-hour clock setting. */
    fun timeText24(): String = "%02d:%02d".format(hour, minute)

    /** The date this alarm next fires, relative to [now]. */
    fun nextTrigger(now: java.time.LocalDateTime): java.time.LocalDateTime =
        com.chronos.clock.core.nextAlarmTrigger(now, hour, minute, days)
}

/** A pinned city on the world clock. */
@Serializable
data class WorldCity(
    val id: String,
    val city: String,
    val country: String,
    val zoneId: String,
)

/** A countdown timer. */
@Serializable
data class RunningTimer(
    val id: String,
    val label: String,
    val icon: String,
    val totalMillis: Long,
    val remainingMillis: Long,
    /** Epoch millis at which the timer fires; `0` while paused or idle. */
    val endAtMillis: Long = 0L,
    val running: Boolean = false,
) {
    val progress: Float
        get() = if (totalMillis <= 0L) 0f else (remainingMillis.toFloat() / totalMillis).coerceIn(0f, 1f)

    fun remainingText(): String {
        val total = (remainingMillis / 1000).coerceAtLeast(0)
        val m = total / 60
        val s = total % 60
        return "%02d:%02d".format(m, s)
    }
}

/** A preset launch option on the timer screen. */
@Serializable
data class TimerPreset(
    val id: String,
    val label: String,
    val icon: String,
    val durationMillis: Long,
)

/** One recorded lap. */
@Serializable
data class Lap(
    val index: Int,
    val splitMillis: Long,
    val totalMillis: Long,
) {
    fun splitText(): String = formatSplit(splitMillis)
    fun totalText(): String = formatSplit(totalMillis)

    companion object {
        fun formatSplit(millis: Long): String {
            val total = millis / 1000
            val minutes = total / 60
            val seconds = total % 60
            val hundredths = (millis % 1000) / 10
            return "%02d:%02d.%02d".format(minutes, seconds, hundredths)
        }
    }
}

/** The bedtime / sleep-schedule card. */
@Serializable
data class SleepSchedule(
    val enabled: Boolean = false,
    val bedtimeHour: Int = 23,
    val bedtimeMinute: Int = 0,
    val wakeHour: Int = 7,
    val wakeMinute: Int = 0,
) {
    fun bedtimeText(): String = "%d:%02d %s".format(
        hour12(bedtimeHour),
        bedtimeMinute,
        if (bedtimeHour < 12) "AM" else "PM",
    )

    fun wakeText(): String = "%d:%02d %s".format(
        hour12(wakeHour),
        wakeMinute,
        if (wakeHour < 12) "AM" else "PM",
    )

    fun restHoursText(): String {
        val minutes = ((wakeHour * 60 + wakeMinute) - (bedtimeHour * 60 + bedtimeMinute) + 24 * 60) % (24 * 60)
        return "%d hr%s of rest planned".format(minutes / 60, if (minutes % 60 == 0) "" else " ${minutes % 60} min")
    }

    private fun hour12(hour: Int): Int {
        val mod = hour % 12
        return if (mod == 0) 12 else mod
    }
}
