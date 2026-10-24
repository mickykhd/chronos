package com.chronos.clock.data

import android.content.Context
import com.chronos.clock.ui.theme.ChronosTheme
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
 *
 * A fresh install starts completely empty — no demo alarms, no pinned cities. The
 * only seeded data is the *catalogue* of world cities you can choose to pin, which
 * is reference data rather than the user's own.
 */
class ChronosRepository(context: Context) {

    private val prefs = context.applicationContext
        .getSharedPreferences("chronos_state", Context.MODE_PRIVATE)

    private val json = Json {
        ignoreUnknownKeys = true
        encodeDefaults = true
    }

    private val _alarms = MutableStateFlow(read("alarms", emptyList<Alarm>()))
    val alarms: StateFlow<List<Alarm>> = _alarms.asStateFlow()

    private val _cities = MutableStateFlow(read("cities", emptyList<WorldCity>()))
    val cities: StateFlow<List<WorldCity>> = _cities.asStateFlow()

    private val _timers = MutableStateFlow(read("timers", emptyList<RunningTimer>()))
    val timers: StateFlow<List<RunningTimer>> = _timers.asStateFlow()

    private val _customPresets = MutableStateFlow(read("presets", emptyList<TimerPreset>()))
    val customPresets: StateFlow<List<TimerPreset>> = _customPresets.asStateFlow()

    private val _stopwatch = MutableStateFlow(read("stopwatch", StopwatchState()))
    val stopwatch: StateFlow<StopwatchState> = _stopwatch.asStateFlow()

    private val _laps = MutableStateFlow(read("laps", emptyList<Lap>()))
    val laps: StateFlow<List<Lap>> = _laps.asStateFlow()

    private val _sleepSchedule = MutableStateFlow(read("sleep", SleepSchedule()))
    val sleepSchedule: StateFlow<SleepSchedule> = _sleepSchedule.asStateFlow()

    // The fallback type has to be explicit: a bare `null` makes the compiler infer
    // `Nothing?`, which silently fails to deserialise and loses the saved theme.
    private val _theme = MutableStateFlow(ChronosTheme.fromId(read("theme", null as String?)))
    val theme: StateFlow<ChronosTheme> = _theme.asStateFlow()

    private val _use24Hour = MutableStateFlow(read("use24h", false))

    private val _weatherCache = MutableStateFlow(read("weather", emptyMap<String, WeatherSnapshot>()) as Map<String, WeatherSnapshot>)
    val use24Hour: StateFlow<Boolean> = _use24Hour.asStateFlow()

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

    // ------------------------------------------------------------- presets

    /** The built-in presets followed by anything the user has added. */
    fun allPresets(): List<TimerPreset> = TimerPresets + _customPresets.value

    fun addCustomPreset(preset: TimerPreset) {
        _customPresets.value = _customPresets.value + preset
        persist("presets", _customPresets.value)
    }

    fun removeCustomPreset(id: String) {
        _customPresets.value = _customPresets.value.filterNot { it.id == id }
        persist("presets", _customPresets.value)
    }

    // --------------------------------------------------------------- laps

    fun updateStopwatch(state: StopwatchState) {
        _stopwatch.value = state
        persist("stopwatch", state)
    }

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

    // ------------------------------------------------------------ weather

    /**
     * Last conditions seen for each zone, so the home-screen widgets can show
     * weather without a network round trip on every refresh.
     */
    fun recordWeather(zoneId: String, snapshot: WeatherSnapshot) {
        _weatherCache.value = _weatherCache.value + (zoneId to snapshot)
        persist("weather", _weatherCache.value)
    }

    fun cachedWeather(zoneId: String): WeatherSnapshot? = _weatherCache.value[zoneId]

    // ------------------------------------------------------------ settings

    fun setTheme(theme: ChronosTheme) {
        _theme.value = theme
        persist("theme", theme.id)
    }

    fun setUse24Hour(use24Hour: Boolean) {
        _use24Hour.value = use24Hour
        persist("use24h", use24Hour)
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

        /**
         * The catalogue of cities you can pin to the world clock. This is reference
         * data, not pre-populated user state — nothing is pinned until you choose it.
         */
        val AvailableCities: List<WorldCity> = listOf(
            WorldCity("city-tokyo", "Tokyo", "Japan", "Asia/Tokyo"),
            WorldCity("city-london", "London", "United Kingdom", "Europe/London"),
            WorldCity("city-new-york", "New York", "USA", "America/New_York"),
            WorldCity("city-sydney", "Sydney", "Australia", "Australia/Sydney"),
            WorldCity("city-dubai", "Dubai", "UAE", "Asia/Dubai"),
            WorldCity("city-paris", "Paris", "France", "Europe/Paris"),
            WorldCity("city-berlin", "Berlin", "Germany", "Europe/Berlin"),
            WorldCity("city-sao-paulo", "São Paulo", "Brazil", "America/Sao_Paulo"),
            WorldCity("city-mumbai", "Mumbai", "India", "Asia/Kolkata"),
            WorldCity("city-singapore", "Singapore", "Singapore", "Asia/Singapore"),
            WorldCity("city-toronto", "Toronto", "Canada", "America/Toronto"),
            WorldCity("city-mexico", "Mexico City", "Mexico", "America/Mexico_City"),
            WorldCity("city-cape-town", "Cape Town", "South Africa", "Africa/Johannesburg"),
            WorldCity("city-lagos", "Lagos", "Nigeria", "Africa/Lagos"),
            WorldCity("city-istanbul", "Istanbul", "Türkiye", "Europe/Istanbul"),
            WorldCity("city-seoul", "Seoul", "South Korea", "Asia/Seoul"),
            WorldCity("city-hong-kong", "Hong Kong", "China", "Asia/Hong_Kong"),
            WorldCity("city-los-angeles", "Los Angeles", "USA", "America/Los_Angeles"),
            WorldCity("city-chicago", "Chicago", "USA", "America/Chicago"),
            WorldCity("city-amsterdam", "Amsterdam", "Netherlands", "Europe/Amsterdam"),
            WorldCity("city-zurich", "Zurich", "Switzerland", "Europe/Zurich"),
            WorldCity("city-vienna", "Vienna", "Austria", "Europe/Vienna"),
            WorldCity("city-madrid", "Madrid", "Spain", "Europe/Madrid"),
            WorldCity("city-lisbon", "Lisbon", "Portugal", "Europe/Lisbon"),
            WorldCity("city-stockholm", "Stockholm", "Sweden", "Europe/Stockholm"),
            WorldCity("city-oslo", "Oslo", "Norway", "Europe/Oslo"),
            WorldCity("city-copenhagen", "Copenhagen", "Denmark", "Europe/Copenhagen"),
            WorldCity("city-helsinki", "Helsinki", "Finland", "Europe/Helsinki"),
            WorldCity("city-warsaw", "Warsaw", "Poland", "Europe/Warsaw"),
            WorldCity("city-athens", "Athens", "Greece", "Europe/Athens"),
            WorldCity("city-moscow", "Moscow", "Russia", "Europe/Moscow"),
            WorldCity("city-kyiv", "Kyiv", "Ukraine", "Europe/Kyiv"),
            WorldCity("city-cairo", "Cairo", "Egypt", "Africa/Cairo"),
            WorldCity("city-nairobi", "Nairobi", "Kenya", "Africa/Nairobi"),
            WorldCity("city-johannesburg", "Johannesburg", "South Africa", "Africa/Johannesburg"),
            WorldCity("city-karachi", "Karachi", "Pakistan", "Asia/Karachi"),
            WorldCity("city-dhaka", "Dhaka", "Bangladesh", "Asia/Dhaka"),
            WorldCity("city-bangkok", "Bangkok", "Thailand", "Asia/Bangkok"),
            WorldCity("city-jakarta", "Jakarta", "Indonesia", "Asia/Jakarta"),
            WorldCity("city-manila", "Manila", "Philippines", "Asia/Manila"),
            WorldCity("city-taipei", "Taipei", "Taiwan", "Asia/Taipei"),
            WorldCity("city-shanghai", "Shanghai", "China", "Asia/Shanghai"),
            WorldCity("city-perth", "Perth", "Australia", "Australia/Perth"),
            WorldCity("city-melbourne", "Melbourne", "Australia", "Australia/Melbourne"),
            WorldCity("city-auckland", "Auckland", "New Zealand", "Pacific/Auckland"),
            WorldCity("city-honolulu", "Honolulu", "USA", "Pacific/Honolulu"),
            WorldCity("city-vancouver", "Vancouver", "Canada", "America/Vancouver"),
            WorldCity("city-montevideo", "Montevideo", "Uruguay", "America/Montevideo"),
            WorldCity("city-buenos-aires", "Buenos Aires", "Argentina", "America/Argentina/Buenos_Aires"),
            WorldCity("city-santiago", "Santiago", "Chile", "America/Santiago"),
            WorldCity("city-bogota", "Bogotá", "Colombia", "America/Bogota"),
            WorldCity("city-lima", "Lima", "Peru", "America/Lima"),
            WorldCity("city-reykjavik", "Reykjavík", "Iceland", "Atlantic/Reykjavik"),
        )

        fun newId(): String = UUID.randomUUID().toString()
    }
}
