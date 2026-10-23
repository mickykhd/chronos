package com.chronos.clock.data

import android.content.Context
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.serialization.json.Json
import java.util.UUID

/**
 * Single source of truth for the app's persisted state.
 *
 * Everything lives in [android.content.SharedPreferences] as JSON so the process can
 * be killed at any time without losing the user's alarms, timers, pins or laps.
 */
class ChronosRepository(context: Context) {

    private val prefs = context.applicationContext
        .getSharedPreferences("chronos_state", Context.MODE_PRIVATE)

    private val json = Json {
        ignoreUnknownKeys = true
        encodeDefaults = true
    }

    private val _alarms = MutableStateFlow(read("alarms", seedAlarms()))
    val alarms: StateFlow<List<Alarm>> = _alarms.asStateFlow()

    private val _cities = MutableStateFlow(read("cities", seedCities()))
    val cities: StateFlow<List<WorldCity>> = _cities.asStateFlow()

    private val _timers = MutableStateFlow(read("timers", emptyList<RunningTimer>()))
    val timers: StateFlow<List<RunningTimer>> = _timers.asStateFlow()

    private val _laps = MutableStateFlow(read("laps", emptyList<Lap>()))
    val laps: StateFlow<List<Lap>> = _laps.asStateFlow()

    private val _sleepSchedule = MutableStateFlow(read("sleep", SleepSchedule()))
    val sleepSchedule: StateFlow<SleepSchedule> = _sleepSchedule.asStateFlow()

    // ---------------------------------------------------------------- alarms

    fun addAlarm(alarm: Alarm) {
        _alarms.value = _alarms.value + alarm
        persist("alarms", _alarms.value)
    }

    fun updateAlarm(alarm: Alarm) {
        _alarms.value = _alarms.value.map { if (it.id == alarm.id) alarm else it }
        persist("alarms", _alarms.value)
    }

    fun removeAlarm(id: String) {
        _alarms.value = _alarms.value.filterNot { it.id == id }
        persist("alarms", _alarms.value)
    }

    fun toggleAlarm(id: String, enabled: Boolean) {
        _alarms.value = _alarms.value.map { if (it.id == id) it.copy(enabled = enabled) else it }
        persist("alarms", _alarms.value)
    }

    fun toggleAlarmDay(id: String, day: Int) {
        _alarms.value = _alarms.value.map { alarm ->
            if (alarm.id != id) {
                alarm
            } else {
                val days = alarm.days.toMutableSet()
                if (!days.add(day)) days.remove(day)
                alarm.copy(days = days)
            }
        }
        persist("alarms", _alarms.value)
    }

    // ------------------------------------------------------------- cities

    fun addCity(city: WorldCity) {
        if (_cities.value.any { it.zoneId == city.zoneId }) return
        _cities.value = _cities.value + city
        persist("cities", _cities.value)
    }

    fun removeCity(id: String) {
        _cities.value = _cities.value.filterNot { it.id == id }
        persist("cities", _cities.value)
    }

    // ------------------------------------------------------------- timers

    fun addTimer(timer: RunningTimer) {
        _timers.value = _timers.value + timer
        persist("timers", _timers.value)
    }

    fun updateTimer(timer: RunningTimer) {
        _timers.value = _timers.value.map { if (it.id == timer.id) timer else it }
        persist("timers", _timers.value)
    }

    fun removeTimer(id: String) {
        _timers.value = _timers.value.filterNot { it.id == id }
        persist("timers", _timers.value)
    }

    /**
     * Updates the countdown without touching disk. The persisted record keeps the
     * absolute `endAtMillis`, so a process death can still reconstruct the correct
     * remaining time on restore.
     */
    fun updateTimersTransient(timers: List<RunningTimer>) {
        _timers.value = timers
    }

    // --------------------------------------------------------------- laps

    fun addLap(lap: Lap) {
        _laps.value = _laps.value + lap
        persist("laps", _laps.value)
    }

    fun clearLaps() {
        _laps.value = emptyList()
        persist("laps", _laps.value)
    }

    // ------------------------------------------------------- sleep schedule

    fun updateSleepSchedule(schedule: SleepSchedule) {
        _sleepSchedule.value = schedule
        persist("sleep", _sleepSchedule.value)
    }

    // ------------------------------------------------------------ plumbing

    private inline fun <reified T> read(key: String, fallback: T): T {
        val raw = prefs.getString(key, null) ?: return fallback
        return runCatching { json.decodeFromString<T>(raw) }.getOrElse { fallback }
    }

    private inline fun <reified T> persist(key: String, value: T) {
        val raw = runCatching { json.encodeToString(value) }.getOrNull() ?: return
        prefs.edit().putString(key, raw).apply()
    }

    companion object {
        val TimerPresets: List<TimerPreset> = listOf(
            TimerPreset("preset-tea", "Tea", "local_cafe", 3 * 60_000L),
            TimerPreset("preset-eggs", "Eggs", "egg", 5 * 60_000L),
            TimerPreset("preset-meditation", "Meditation", "self_improvement", 10 * 60_000L),
            TimerPreset("preset-pomodoro", "Pomodoro", "hourglass_top", 25 * 60_000L),
        )

        /** Cities offered by the "Add City" sheet, beyond the three pinned hubs. */
        val AvailableCities: List<WorldCity> = listOf(
            WorldCity("city-sydney", "Sydney", "Australia", "Australia/Sydney", 19, "Clear", "sunny"),
            WorldCity("city-dubai", "Dubai", "UAE", "Asia/Dubai", 33, "Hazy", "wb_twilight"),
            WorldCity("city-paris", "Paris", "France", "Europe/Paris", 16, "Cloudy", "cloud"),
            WorldCity("city-berlin", "Berlin", "Germany", "Europe/Berlin", 14, "Rain", "rainy"),
            WorldCity("city-sao-paulo", "São Paulo", "Brazil", "America/Sao_Paulo", 24, "Thunder", "wb_sunny"),
            WorldCity("city-mumbai", "Mumbai", "India", "Asia/Kolkata", 29, "Humid", "wb_twilight"),
            WorldCity("city-singapore", "Singapore", "Singapore", "Asia/Singapore", 30, "Storm", "rainy"),
            WorldCity("city-toronto", "Toronto", "Canada", "America/Toronto", 12, "Clear", "sunny"),
            WorldCity("city-mexico", "Mexico City", "Mexico", "America/Mexico_City", 21, "Clear", "sunny"),
            WorldCity("city-cape-town", "Cape Town", "South Africa", "Africa/Johannesburg", 17, "Windy", "cloud"),
        )

        private fun seedAlarms(): List<Alarm> = listOf(
            Alarm(
                id = "alarm-work",
                hour = 7,
                minute = 0,
                label = "Work / Weekdays",
                days = setOf(1, 2, 3, 4, 5),
                enabled = true,
                sound = "Dawn Chorus",
                routine = "Google Assistant",
                vibrate = true,
            ),
            Alarm(
                id = "alarm-brunch",
                hour = 8,
                minute = 30,
                label = "Weekend Brunch",
                days = setOf(6, 7),
                enabled = false,
                sound = "Marimba",
                routine = null,
                vibrate = true,
            ),
            Alarm(
                id = "alarm-run",
                hour = 6,
                minute = 15,
                label = "Morning Run",
                days = setOf(1, 3, 5),
                enabled = false,
                sound = "Beacon",
                routine = null,
                vibrate = true,
            ),
        )

        private fun seedCities(): List<WorldCity> = listOf(
            WorldCity("city-tokyo", "Tokyo", "Japan", "Asia/Tokyo", 18, "Rain", "rainy"),
            WorldCity("city-london", "London", "United Kingdom", "Europe/London", 14, "Cloudy", "cloud"),
            WorldCity("city-new-york", "New York", "USA", "America/New_York", 21, "Clear", "sunny"),
        )

        fun newId(): String = UUID.randomUUID().toString()
    }
}
