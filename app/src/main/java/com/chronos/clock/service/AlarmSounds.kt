package com.chronos.clock.service

import android.content.Context
import android.database.Cursor
import android.media.AudioAttributes
import android.media.AudioManager
import android.media.MediaPlayer
import android.media.Ringtone
import android.media.RingtoneManager
import android.net.Uri
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import com.chronos.clock.R
import com.chronos.clock.data.VibrationPattern
import kotlin.math.abs

/**
 * The alarm, ringtone and notification sounds installed on the device.
 *
 * The list comes from [RingtoneManager]'s own cursor rather than a media-store
 * query: that cursor is what the system Sounds settings shows, so it includes every
 * tone the device vendor ships (including OEM tone packs) — those are frequently
 * not tagged with the media-store ringtone/alarm flags and are invisible to a
 * media query.
 *
 * A fresh install has whatever the phone already ships with; the app bundles
 * nothing and assumes nothing.
 */
object AlarmSounds {

    enum class Kind(val label: String) {
        Alarm("Alarm sounds"),
        Ringtone("Ringtones"),
        Notification("Notifications"),
    }

    data class Sound(
        val uri: String,
        val title: String,
        val kind: Kind,
    )

    /** The device's default alarm tone, or `null` when the device has none. */
    fun defaultAlarmSound(context: Context): Sound? {
        val uri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_ALARM)
            ?: RingtoneManager.getDefaultUri(RingtoneManager.TYPE_RINGTONE)
            ?: return null
        return Sound(
            uri = uri.toString(),
            title = titleFor(context, uri) ?: "Default",
            kind = Kind.Alarm,
        )
    }

    /** Every sound the user can pick, grouped by kind. */
    fun load(context: Context): List<Sound> {
        val result = mutableListOf<Sound>()
        Kind.entries.forEach { kind ->
            val type = when (kind) {
                Kind.Alarm -> RingtoneManager.TYPE_ALARM
                Kind.Ringtone -> RingtoneManager.TYPE_RINGTONE
                Kind.Notification -> RingtoneManager.TYPE_NOTIFICATION
            }
            result += collectType(context, type, kind)
        }

        // Some devices expose an alarm slot but list nothing under it; fall back to
        // the default so there is always something selectable.
        if (result.none { it.kind == Kind.Alarm }) {
            defaultAlarmSound(context)?.let { result.add(0, it) }
        }
        return result
    }

    private fun collectType(context: Context, type: Int, kind: Kind): List<Sound> {
        val sounds = mutableListOf<Sound>()
        runCatching {
            val manager = RingtoneManager(context).apply { setType(type) }
            val cursor: Cursor = manager.cursor
            cursor.use {
                while (it.moveToNext()) {
                    val uri = runCatching { manager.getRingtoneUri(it.position) }.getOrNull()
                        ?: continue
                    val title = it.getString(RingtoneManager.TITLE_COLUMN_INDEX)
                        ?: titleFor(context, uri)
                        ?: continue
                    sounds += Sound(uri.toString(), title, kind)
                }
            }
        }
        return sounds
    }

    private fun titleFor(context: Context, uri: Uri): String? = runCatching {
        RingtoneManager.getRingtone(context, uri)?.getTitle(context)
    }.getOrNull()
}

/**
 * Plays an alarm tone on the alarm stream with proper audio attributes and focus.
 *
 * `MediaPlayer` is used directly rather than [android.media.Ringtone] because it
 * gives explicit control over the stream, looping and audio focus — which is what
 * makes the tone actually audible when the phone is in Doze or the screen is off.
 */
class RingtonePlayer(context: Context) {

    private val appContext = context.applicationContext
    private val audioManager = appContext.getSystemService(Context.AUDIO_SERVICE) as? AudioManager

    private var player: MediaPlayer? = null
    private var ringtone: Ringtone? = null
    private var focusRequest: android.media.AudioFocusRequest? = null

    /**
     * Starts looping [uri] on the alarm stream.
     *
     * If the chosen sound cannot be played — the system alarm URI
     * (`content://settings/system/alarm_alert`) is not directly openable, and some
     * devices have no alarm sound installed at all — this falls back to the tone
     * bundled with the app, so an alarm is never silent.
     *
     * @return true when something is actually playing.
     */
    fun play(uri: Uri?): Boolean {
        if (player?.isPlaying == true || ringtone?.isPlaying == true) return true

        release()
        requestFocus()

        if (uri != null && playUri(uri)) return true
        return playBundledTone()
    }

    private fun playUri(uri: Uri): Boolean {
        // The system default alarm URI cannot be opened by MediaPlayer; the
        // Ringtone API resolves it to the underlying media item.
        if (uri.scheme == "content" && uri.authority == "settings") {
            val tone = runCatching { RingtoneManager.getRingtone(appContext, uri) }.getOrNull()
                ?: return false
            tone.audioAttributes = alarmAttributes()
            tone.isLooping = true
            runCatching { tone.play() }
            ringtone = tone
            return true
        }

        return runCatching {
            MediaPlayer().apply {
                setAudioAttributes(alarmAttributes())
                setDataSource(appContext, uri)
                isLooping = true
                prepare()
                start()
            }
        }.fold(
            onSuccess = { player = it; true },
            onFailure = { false },
        )
    }

    /** The tone shipped with Chronos, used whenever the chosen sound is unusable. */
    private fun playBundledTone(): Boolean = runCatching {
        appContext.resources.openRawResourceFd(R.raw.chronos_alarm).use { afd ->
            MediaPlayer().apply {
                setAudioAttributes(alarmAttributes())
                setDataSource(afd.fileDescriptor, afd.startOffset, afd.declaredLength)
                isLooping = true
                prepare()
                start()
            }
        }
    }.fold(
        onSuccess = { player = it; true },
        onFailure = { false },
    )

    private fun alarmAttributes(): AudioAttributes = AudioAttributes.Builder()
        .setUsage(AudioAttributes.USAGE_ALARM)
        .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
        .setLegacyStreamType(AudioManager.STREAM_ALARM)
        .build()

    fun stop() {
        runCatching { player?.stop() }
        runCatching { ringtone?.stop() }
        release()
    }

    private fun release() {
        runCatching { player?.release() }
        player = null
        ringtone = null
        abandonFocus()
    }

    private fun requestFocus() {
        val manager = audioManager ?: return
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) {
            @Suppress("DEPRECATION")
            manager.requestAudioFocus(
                AudioManager.OnAudioFocusChangeListener { },
                AudioManager.STREAM_ALARM,
                AudioManager.AUDIOFOCUS_GAIN_TRANSIENT,
            )
            return
        }
        val request = android.media.AudioFocusRequest
            .Builder(AudioManager.AUDIOFOCUS_GAIN_TRANSIENT)
            .setAudioAttributes(alarmAttributes())
            .setOnAudioFocusChangeListener { }
            .build()
        focusRequest = request
        manager.requestAudioFocus(request)
    }

    private fun abandonFocus() {
        val manager = audioManager ?: return
        val request = focusRequest
        if (request != null && Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            manager.abandonAudioFocusRequest(request)
        } else {
            @Suppress("DEPRECATION")
            manager.abandonAudioFocus(AudioManager.OnAudioFocusChangeListener { })
        }
        focusRequest = null
    }
}

/**
 * Fires the vibration pattern an alarm is configured with.
 */
object AlarmVibrator {

    fun vibrate(context: Context, pattern: VibrationPattern) {
        if (pattern == VibrationPattern.Off) return
        val vibrator = vibrator(context) ?: return
        if (!vibrator.hasVibrator()) return

        val effect = when (pattern) {
            VibrationPattern.Off -> return
            VibrationPattern.Standard ->
                VibrationEffect.createWaveform(longArrayOf(0, 500, 500), -1)
            VibrationPattern.Double ->
                VibrationEffect.createWaveform(longArrayOf(0, 400, 300, 400, 800), -1)
            VibrationPattern.Continuous ->
                VibrationEffect.createWaveform(longArrayOf(0, 1000), 0)
        }
        runCatching { vibrator.vibrate(effect) }
    }

    private fun vibrator(context: Context): Vibrator? = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
        val manager = context.getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as? VibratorManager
        manager?.defaultVibrator
    } else {
        @Suppress("DEPRECATION")
        context.getSystemService(Context.VIBRATOR_SERVICE) as? Vibrator
    }
}