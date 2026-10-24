package com.chronos.clock.ui.alarm

import android.os.Build
import android.os.Bundle
import android.view.WindowManager
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.chronos.clock.core.rememberNow
import com.chronos.clock.service.AlarmActions
import com.chronos.clock.service.AlarmService
import com.chronos.clock.ui.components.ChronosIcon
import com.chronos.clock.ui.components.ChronosPill
import com.chronos.clock.ui.theme.ChronosColors
import com.chronos.clock.ui.theme.ChronosDimens
import com.chronos.clock.ui.theme.ChronosMotion
import com.chronos.clock.ui.theme.ChronosRadius
import com.chronos.clock.ui.theme.ChronosTheme
import com.chronos.clock.ui.theme.ChronosType
import java.time.format.DateTimeFormatter

/**
 * The full-screen alarm UI.
 *
 * It is brought to the front by [AlarmService] with a full-screen intent, and shows
 * over the lock screen with the screen woken — the way a phone clock behaves.
 */
class AlarmRingActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        showOverLockScreen()

        val id = intent.getStringExtra(AlarmService.EXTRA_ID).orEmpty()
        val hour = intent.getIntExtra(AlarmService.EXTRA_HOUR, 0)
        val minute = intent.getIntExtra(AlarmService.EXTRA_MINUTE, 0)
        val label = intent.getStringExtra(AlarmService.EXTRA_LABEL).orEmpty()
        val soundTitle = intent.getStringExtra(AlarmService.EXTRA_SOUND_TITLE).orEmpty()
        val snoozeEnabled = intent.getBooleanExtra(AlarmService.EXTRA_SNOOZE, true)

        setContent {
            val repository = (application as com.chronos.clock.ChronosApplication).repository
            val theme by repository.theme.collectAsStateWithLifecycle()
            ChronosTheme(theme = theme) {
                AlarmRingScreen(
                    alarmTime = "%02d:%02d".format(hour, minute),
                    label = label.ifBlank { "Alarm" },
                    soundTitle = soundTitle,
                    snoozeEnabled = snoozeEnabled,
                    onDismiss = {
                        AlarmActions.dismiss(this)
                        finish()
                    },
                    onSnooze = {
                        AlarmActions.snooze(this, id)
                        finish()
                    },
                )
            }
        }
    }

    private fun showOverLockScreen() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O_MR1) {
            setShowWhenLocked(true)
            setTurnScreenOn(true)
        } else {
            @Suppress("DEPRECATION")
            window.addFlags(
                WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED or
                    WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON,
            )
        }
        window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
    }
}

@Composable
private fun AlarmRingScreen(
    alarmTime: String,
    label: String,
    soundTitle: String,
    snoozeEnabled: Boolean,
    onDismiss: () -> Unit,
    onSnooze: () -> Unit,
) {
    val now by rememberNow()
    val pulse = rememberInfiniteTransition(label = "ring")
    val bellScale by pulse.animateFloat(
        initialValue = 0.94f,
        targetValue = 1.06f,
        animationSpec = infiniteRepeatable(
            animation = tween(700, easing = ChronosMotion.Standard),
            repeatMode = RepeatMode.Reverse,
        ),
        label = "bellScale",
    )
    val haloAlpha by pulse.animateFloat(
        initialValue = 0.18f,
        targetValue = 0.42f,
        animationSpec = infiniteRepeatable(
            animation = tween(700, easing = ChronosMotion.Standard),
            repeatMode = RepeatMode.Reverse,
        ),
        label = "haloAlpha",
    )

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(ChronosColors.Surface),
    ) {
        // A soft halo behind the bell.
        Box(
            modifier = Modifier
                .align(Alignment.TopCenter)
                .padding(top = 72.dp)
                .size(220.dp)
                .scale(1f + (1f - bellScale) * 0.4f)
                .background(
                    Brush.radialGradient(
                        colors = listOf(
                            MaterialTheme.colorScheme.primaryContainer.copy(alpha = haloAlpha),
                            Color.Transparent,
                        ),
                    ),
                    CircleShape,
                ),
        )

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Spacer(Modifier.height(96.dp))

            Box(
                modifier = Modifier.scale(bellScale),
                contentAlignment = Alignment.Center,
            ) {
                ChronosIcon(
                    name = "alarm",
                    size = 64.dp,
                    tint = MaterialTheme.colorScheme.primaryContainer,
                    filled = true,
                )
            }

            Spacer(Modifier.height(32.dp))

            Text(
                text = now.format(DateTimeFormatter.ofPattern("h:mm")),
                style = ChronosType.DisplayHero,
                color = MaterialTheme.colorScheme.onSurface,
            )
            Text(
                text = now.format(DateTimeFormatter.ofPattern("EEEE, MMM d")),
                style = ChronosType.BodyLg,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )

            Spacer(Modifier.height(40.dp))

            Text(
                text = label,
                style = ChronosType.HeadlineMd,
                color = MaterialTheme.colorScheme.primaryContainer,
                textAlign = TextAlign.Center,
            )
            if (soundTitle.isNotBlank() && soundTitle != "Default") {
                Spacer(Modifier.height(4.dp))
                Text(
                    text = soundTitle,
                    style = ChronosType.BodyMd,
                    color = ChronosColors.Outline,
                    textAlign = TextAlign.Center,
                )
            }

            Spacer(Modifier.weight(1f))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                if (snoozeEnabled) {
                    ChronosPill(
                        text = "Snooze ${AlarmActions.SNOOZE_MINUTES} min",
                        leadingIcon = "snooze",
                        leadingIconTint = MaterialTheme.colorScheme.primaryContainer,
                        container = ChronosColors.SurfaceContainerHigh,
                        textStyle = ChronosType.LabelLg,
                        height = 56.dp,
                        onClick = onSnooze,
                        modifier = Modifier.weight(1f),
                    )
                }
                ChronosPill(
                    text = "Dismiss",
                    leadingIcon = "close",
                    leadingIconTint = MaterialTheme.colorScheme.onPrimaryContainer,
                    container = MaterialTheme.colorScheme.primaryContainer,
                    contentColor = MaterialTheme.colorScheme.onPrimaryContainer,
                    textStyle = ChronosType.LabelLg,
                    height = 56.dp,
                    onClick = onDismiss,
                    modifier = Modifier.weight(1f),
                )
            }

            Spacer(Modifier.height(48.dp))
        }
    }
}
