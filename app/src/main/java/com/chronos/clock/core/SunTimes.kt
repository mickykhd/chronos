package com.chronos.clock.core

import java.time.Duration
import java.time.LocalDate
import java.time.ZoneId
import java.time.ZonedDateTime
import java.util.TimeZone
import kotlin.math.acos
import kotlin.math.asin
import kotlin.math.atan
import kotlin.math.cos
import kotlin.math.floor
import kotlin.math.sin
import kotlin.math.tan

/**
 * Approximate geographic coordinates for the time zones the app knows about.
 *
 * `java.time.ZoneId` deliberately carries no geography, so sunrise/sunset needs a
 * coordinate lookup. Zones outside the table fall back to a longitude derived from
 * their UTC offset (which is accurate to a few degrees) and a neutral latitude.
 */
object ZoneCoordinates {

    data class LatLon(val lat: Double, val lon: Double)

    private val table: Map<String, LatLon> = mapOf(
        "Pacific/Honolulu" to LatLon(21.3069, -157.8583),
        "America/Anchorage" to LatLon(61.2181, -149.9003),
        "America/Los_Angeles" to LatLon(34.0522, -118.2437),
        "America/Denver" to LatLon(39.7392, -104.9903),
        "America/Phoenix" to LatLon(33.4484, -112.0740),
        "America/Chicago" to LatLon(41.8781, -87.6298),
        "America/Mexico_City" to LatLon(19.4326, -99.1332),
        "America/New_York" to LatLon(40.7128, -74.0060),
        "America/Toronto" to LatLon(43.6532, -79.3832),
        "America/Bogota" to LatLon(4.7110, -74.0721),
        "America/Sao_Paulo" to LatLon(-23.5505, -46.6333),
        "America/Argentina/Buenos_Aires" to LatLon(-34.6037, -58.3816),
        "Atlantic/Reykjavik" to LatLon(64.1466, -21.9426),
        "Europe/London" to LatLon(51.5074, -0.1278),
        "Europe/Dublin" to LatLon(53.3498, -6.2603),
        "Europe/Lisbon" to LatLon(38.7223, -9.1393),
        "Europe/Madrid" to LatLon(40.4168, -3.7038),
        "Europe/Paris" to LatLon(48.8566, 2.3522),
        "Europe/Berlin" to LatLon(52.5200, 13.4050),
        "Europe/Rome" to LatLon(41.9028, 12.4964),
        "Europe/Amsterdam" to LatLon(52.3676, 4.9041),
        "Europe/Stockholm" to LatLon(59.3293, 18.0686),
        "Europe/Oslo" to LatLon(59.9139, 10.7522),
        "Europe/Warsaw" to LatLon(52.2297, 21.0122),
        "Europe/Athens" to LatLon(37.9838, 23.7275),
        "Europe/Istanbul" to LatLon(41.0082, 28.9784),
        "Europe/Kyiv" to LatLon(50.4501, 30.5234),
        "Europe/Moscow" to LatLon(55.7558, 37.6173),
        "Africa/Cairo" to LatLon(30.0444, 31.2357),
        "Africa/Lagos" to LatLon(6.5244, 3.3792),
        "Africa/Nairobi" to LatLon(-1.2921, 36.8219),
        "Africa/Johannesburg" to LatLon(-26.2041, 28.0473),
        "Asia/Jerusalem" to LatLon(31.7683, 35.2137),
        "Asia/Dubai" to LatLon(25.2048, 55.2708),
        "Asia/Karachi" to LatLon(24.8607, 67.0011),
        "Asia/Kolkata" to LatLon(19.0760, 72.8777),
        "Asia/Dhaka" to LatLon(23.8103, 90.4125),
        "Asia/Bangkok" to LatLon(13.7563, 100.5018),
        "Asia/Jakarta" to LatLon(-6.2088, 106.8456),
        "Asia/Singapore" to LatLon(1.3521, 103.8198),
        "Asia/Hong_Kong" to LatLon(22.3193, 114.1694),
        "Asia/Shanghai" to LatLon(31.2304, 121.4737),
        "Asia/Taipei" to LatLon(25.0330, 121.5654),
        "Asia/Seoul" to LatLon(37.5665, 126.9780),
        "Asia/Tokyo" to LatLon(35.6762, 139.6503),
        "Australia/Perth" to LatLon(-31.9505, 115.8605),
        "Australia/Sydney" to LatLon(-33.8688, 151.2093),
        "Australia/Melbourne" to LatLon(-37.8136, 144.9631),
        "Australia/Brisbane" to LatLon(-27.4698, 153.0251),
        "Pacific/Auckland" to LatLon(-36.8485, 174.7633),
        "UTC" to LatLon(51.4779, 0.0015),
    )

    fun forZone(zoneId: ZoneId): LatLon {
        table[zoneId.id]?.let { return it }
        // Fall back to a longitude implied by the UTC offset.
        val offset = TimeZone.getTimeZone(zoneId).getOffset(System.currentTimeMillis())
        return LatLon(lat = 30.0, lon = offset / 3_600_000.0 * 15.0)
    }
}

/**
 * Sunrise and sunset, computed with the standard NOAA solar-position algorithm.
 *
 * The clock screen's "Sunrise in 8h 12m" line is derived from this, so it stays
 * truthful for wherever the device actually is.
 */
object SunTimes {

    data class Result(
        val sunrise: ZonedDateTime?,
        val sunset: ZonedDateTime?,
        val polarDay: Boolean = false,
        val polarNight: Boolean = false,
    ) {
        /** Time until the next sunrise, or `null` when the sun never rises today. */
        fun untilSunrise(now: ZonedDateTime): Duration? {
            val rise = sunrise ?: return null
            val target = if (rise.isAfter(now)) rise else rise.plusDays(1)
            return Duration.between(now, target)
        }

        fun untilSunset(now: ZonedDateTime): Duration? {
            val set = sunset ?: return null
            val target = if (set.isAfter(now)) set else set.plusDays(1)
            return Duration.between(now, target)
        }

        fun isDaylight(now: ZonedDateTime): Boolean {
            val rise = sunrise ?: return !polarNight
            val set = sunset ?: return true
            return !now.isBefore(rise) && !now.isAfter(set)
        }
    }

    fun calculate(zoneId: ZoneId, date: LocalDate = LocalDate.now(zoneId)): Result {
        val coords = ZoneCoordinates.forZone(zoneId)
        return calculate(coords.lat, coords.lon, date, zoneId)
    }

    private fun calculate(
        latitude: Double,
        longitude: Double,
        date: LocalDate,
        zoneId: ZoneId,
    ): Result {
        val dayOfYear = date.dayOfYear
        val longitudeHour = longitude / 15.0

        val sunrise = solarEvent(dayOfYear, latitude, longitudeHour, sunrise = true)
        val sunset = solarEvent(dayOfYear, latitude, longitudeHour, sunrise = false)

        if (sunrise.isNaN() && sunset.isNaN()) {
            val polarDay = isPolarDay(dayOfYear, latitude)
            return Result(null, null, polarDay = polarDay, polarNight = !polarDay)
        }
        if (sunrise.isNaN()) return Result(null, toZoned(sunset, date, zoneId))
        if (sunset.isNaN()) return Result(toZoned(sunrise, date, zoneId), null)

        return Result(toZoned(sunrise, date, zoneId), toZoned(sunset, date, zoneId))
    }

    private fun toZoned(hours: Double, date: LocalDate, zoneId: ZoneId): ZonedDateTime {
        val totalMinutes = (hours * 60).toLong()
        return date.atStartOfDay().plusMinutes(totalMinutes).atZone(zoneId)
    }

    private fun isPolarDay(dayOfYear: Int, latitude: Double): Boolean {
        val declination = 23.44 * sunDeclinationApprox(dayOfYear)
        return latitude > 0 && declination > 90 - latitude
    }

    private fun sunDeclinationApprox(dayOfYear: Int): Double {
        val angle = 2 * Math.PI / 365.0 * (dayOfYear - 81)
        return Math.toDegrees(asin(0.39779 * sin(angle)))
    }

    /**
     * Returns the event hour in UTC, or `Double.NaN` when the sun never reaches the
     * required altitude on that date.
     */
    private fun solarEvent(
        dayOfYear: Int,
        latitude: Double,
        longitudeHour: Double,
        sunrise: Boolean,
    ): Double {
        val t = dayOfYear + ((if (sunrise) 6.0 else 18.0) - longitudeHour) / 24.0

        val meanAnomaly = 0.9856 * t - 3.289
        var trueLongitude = meanAnomaly +
            1.916 * sin(Math.toRadians(meanAnomaly)) +
            0.020 * sin(Math.toRadians(2 * meanAnomaly)) +
            282.634
        trueLongitude = normalise(trueLongitude)

        var rightAscension = Math.toDegrees(
            atan(0.91764 * tan(Math.toRadians(trueLongitude)))
        )
        rightAscension = normalise(rightAscension)
        val longitudeQuadrant = floor(trueLongitude / 90.0) * 90.0
        val ascensionQuadrant = floor(rightAscension / 90.0) * 90.0
        rightAscension = (rightAscension + (longitudeQuadrant - ascensionQuadrant)) / 15.0

        val sinDeclination = 0.39782 * sin(Math.toRadians(trueLongitude))
        val cosDeclination = cos(asin(sinDeclination))

        val cosHourAngle = (cos(Math.toRadians(90.833)) -
            sinDeclination * sin(Math.toRadians(latitude))) /
            (cosDeclination * cos(Math.toRadians(latitude)))

        if (cosHourAngle > 1.0 || cosHourAngle < -1.0) return Double.NaN

        var hourAngle = Math.toDegrees(acos(cosHourAngle))
        if (sunrise) hourAngle = 360.0 - hourAngle
        hourAngle /= 15.0

        val localMeanTime = hourAngle + rightAscension - 0.06571 * t - 6.622
        return normalise(localMeanTime - longitudeHour)
    }

    private fun normalise(value: Double): Double {
        val result = value % 360.0
        return if (result < 0) result + 360.0 else result
    }
}

/** The device's zone, resolved to a friendly city + region + offset label. */
/**
 * Legacy / alias zone ids still emitted by some Android builds, mapped to the city
 * name people actually recognise.
 */
private val zoneIdAliases = mapOf(
    "Asia/Calcutta" to "Kolkata",
    "Asia/Saigon" to "Ho Chi Minh City",
    "Asia/Katmandu" to "Kathmandu",
    "Asia/Rangoon" to "Yangon",
    "Europe/Kiev" to "Kyiv",
    "America/Buenos_Aires" to "Buenos Aires",
    "US/Pacific" to "Los Angeles",
    "US/Eastern" to "New York",
    "US/Central" to "Chicago",
    "US/Mountain" to "Denver",
    "GMT" to "Greenwich",
)

data class ZoneDescription(
    val city: String,
    val region: String,
    val longName: String,
    val offsetLabel: String,
    val abbreviation: String,
) {
    /** e.g. `Pacific Daylight Time (GMT-7)` */
    val timezoneLine: String get() = "$longName ($offsetLabel)"
}

fun describeZone(zoneId: ZoneId): ZoneDescription {
    val id = zoneId.id
    val parts = id.split("/")
    val city = (zoneIdAliases[id] ?: parts.last()).replace("_", " ")
    val region = if (parts.size >= 2) parts[parts.size - 2].replace("_", " ") else ""

    val timeZone = TimeZone.getTimeZone(zoneId)
    val longName = timeZone.getDisplayName(false, TimeZone.LONG)
    val abbreviation = timeZone.getDisplayName(false, TimeZone.SHORT)
    val offsetMillis = timeZone.getOffset(System.currentTimeMillis())

    return ZoneDescription(
        city = city,
        region = region,
        longName = longName,
        offsetLabel = formatOffset(offsetMillis),
        abbreviation = abbreviation,
    )
}

fun formatOffset(offsetMillis: Int): String {
    val sign = if (offsetMillis < 0) "-" else "+"
    val totalMinutes = kotlin.math.abs(offsetMillis) / 60_000
    val hours = totalMinutes / 60
    val minutes = totalMinutes % 60
    return if (minutes == 0) {
        "GMT$sign$hours"
    } else {
        "GMT$sign$hours:${minutes.toString().padStart(2, '0')}"
    }
}

/** Formats a duration as `8h 12m`. */
fun formatCoarseDuration(duration: Duration): String {
    val totalMinutes = duration.toMinutes().coerceAtLeast(0)
    val hours = totalMinutes / 60
    val minutes = totalMinutes % 60
    return when {
        hours > 0 && minutes > 0 -> "${hours}h ${minutes}m"
        hours > 0 -> "${hours}h"
        else -> "${minutes}m"
    }
}
