package com.chronos.clock.core

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.jsonArray
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import java.net.HttpURLConnection
import java.net.URL
import java.time.ZoneId
import java.util.concurrent.ConcurrentHashMap

/**
 * Live weather from the Open-Meteo API (no key required).
 *
 * The design's weather pills and card footers are populated from this so they
 * reflect real conditions rather than placeholder text.
 */
object Weather {

    data class Current(
        val temperatureCelsius: Int,
        val condition: String,
        val icon: String,
    ) {
        val temperatureLabel: String get() = "$temperatureCelsius°C"

        /** The hero card uses the imperial scale, matching the design. */
        val temperatureFahrenheit: Int
            get() = Math.round(temperatureCelsius * 9f / 5f + 32f)

        /**
         * A serialisable copy, so the home-screen widgets can show conditions
         * without fetching them again.
         */
        fun snapshot(): com.chronos.clock.data.WeatherSnapshot =
            com.chronos.clock.data.WeatherSnapshot(
                temperatureCelsius = temperatureCelsius,
                condition = condition,
                icon = icon,
            )
    }

    private val cache = ConcurrentHashMap<String, Pair<Long, Current>>()
    private const val TTL_MILLIS = 30 * 60 * 1000L

    suspend fun current(zoneId: ZoneId): Current? {
        val key = zoneId.id
        val now = System.currentTimeMillis()
        cache[key]?.let { (fetchedAt, value) ->
            if (now - fetchedAt < TTL_MILLIS) return value
        }

        return withContext(Dispatchers.IO) {
            val coords = ZoneCoordinates.forZone(zoneId)
            val fetched = runCatching { fetch(coords.lat, coords.lon) }.getOrNull()
            if (fetched != null) cache[key] = now to fetched
            fetched
        }
    }

    private fun fetch(latitude: Double, longitude: Double): Current? {
        val url = URL(
            "https://api.open-meteo.com/v1/forecast" +
                "?latitude=$latitude&longitude=$longitude" +
                "&current=temperature_2m,is_day,weather_code&timezone=auto",
        )
        val connection = (url.openConnection() as HttpURLConnection).apply {
            requestMethod = "GET"
            connectTimeout = 5_000
            readTimeout = 5_000
        }
        try {
            if (connection.responseCode !in 200..299) return null
            val body = connection.inputStream.bufferedReader().use { it.readText() }

            val root = Json.parseToJsonElement(body) as? JsonObject ?: return null
            val current = root["current"]?.jsonObject ?: return null

            val temperature = current["temperature_2m"]?.jsonPrimitive?.content?.toDoubleOrNull()?.toInt()
                ?: return null
            val code = current["weather_code"]?.jsonPrimitive?.content?.toIntOrNull() ?: 0
            val isDay = (current["is_day"]?.jsonPrimitive?.content?.toIntOrNull() ?: 1) == 1

            return Current(
                temperatureCelsius = temperature,
                condition = describe(code),
                icon = iconFor(code, isDay),
            )
        } finally {
            connection.disconnect()
        }
    }

    private fun describe(code: Int): String = when (code) {
        0 -> "Clear"
        1 -> "Mainly clear"
        2 -> "Partly cloudy"
        3 -> "Overcast"
        45, 48 -> "Foggy"
        in 51..57 -> "Drizzle"
        in 61..67 -> "Rain"
        in 71..77 -> "Snow"
        in 80..82 -> "Showers"
        85, 86 -> "Snow showers"
        in 95..99 -> "Thunderstorm"
        else -> "Clear"
    }

    private fun iconFor(code: Int, isDay: Boolean): String = when (code) {
        0, 1 -> if (isDay) "wb_sunny" else "bedtime"
        2, 3 -> "cloud"
        45, 48 -> "cloud"
        in 51..67, in 80..82 -> "rainy"
        in 71..77, 85, 86 -> "cloud"
        in 95..99 -> "wb_twilight"
        else -> if (isDay) "wb_sunny" else "bedtime"
    }

    /** Map of zone id to the current conditions, for screens that show several cities. */
    suspend fun currentFor(zones: List<ZoneId>): Map<String, Current> {
        val result = HashMap<String, Current>()
        zones.forEach { zone ->
            current(zone)?.let { result[zone.id] = it }
        }
        return result
    }
}
