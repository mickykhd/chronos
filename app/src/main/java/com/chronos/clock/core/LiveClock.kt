package com.chronos.clock.core

import androidx.compose.runtime.Composable
import androidx.compose.runtime.State
import androidx.compose.runtime.produceState
import kotlinx.coroutines.delay
import java.time.LocalDateTime
import java.time.ZoneId
import java.time.ZonedDateTime

/**
 * A state that ticks on the wall clock, aligned to the second boundary so the
 * displayed value never lags behind the true time.
 */
@Composable
fun rememberNow(
    zoneId: ZoneId = ZoneId.systemDefault(),
    periodMillis: Long = 1_000L,
): State<ZonedDateTime> = produceState(
    initialValue = ZonedDateTime.now(zoneId),
    zoneId,
    periodMillis,
) {
    while (true) {
        val now = ZonedDateTime.now(zoneId)
        value = now
        val elapsed = System.currentTimeMillis() % periodMillis
        delay((periodMillis - elapsed).coerceAtLeast(1L))
    }
}

/**
 * The next time an alarm with the given hour/minute/day-set should fire.
 */
fun nextAlarmTrigger(
    now: LocalDateTime,
    hour: Int,
    minute: Int,
    days: Set<Int>,
): LocalDateTime {
    val candidateToday = now.toLocalDate().atTime(hour, minute)
    if (days.isEmpty()) {
        return if (candidateToday.isAfter(now)) candidateToday else candidateToday.plusDays(1)
    }

    // ISO day-of-week: Monday = 1.
    val todayIso = now.dayOfWeek.value
    for (offset in 0..7) {
        val date = now.toLocalDate().plusDays(offset.toLong())
        val iso = date.dayOfWeek.value
        if (iso !in days) continue
        val candidate = date.atTime(hour, minute)
        if (candidate.isAfter(now)) return candidate
    }
    return candidateToday.plusDays(7)
}

/**
 * The next time a one-off or repeating alarm fires, expressed as a duration from now.
 */
fun timeUntil(
    now: LocalDateTime,
    hour: Int,
    minute: Int,
    days: Set<Int>,
): java.time.Duration {
    val trigger = nextAlarmTrigger(now, hour, minute, days)
    return java.time.Duration.between(now, trigger)
}
