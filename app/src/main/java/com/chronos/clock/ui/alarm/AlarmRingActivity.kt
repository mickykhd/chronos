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
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.chronos.clock.ChronosApplication
import com.chronos.clock.core.rememberNow
import com.chronos.clock.service.AlarmActions
import com.chronos.clock.service.AlarmService
import com.chronos.clock.ui.components.ChronosIcon
import com.chronos.clock.ui.components.ChronosPill
import com.chronos.clock.ui.theme.ChronosColors
import com.chronos.clock.ui.theme.ChronosRadius
import com.chronos.clock.ui.theme.ChronosTheme
import com.chronos.clock.ui.theme.ChronosType
import java.time.format.DateTimeFormatter

/**
 * The full-screen alarm UI.
 *
 * It is brought to the front by [AlarmService] with a full-screen intent, and shows
 * over the lock screen with the screen woken — the way a phone clock behaves.
 *
 * Layout, top to bottom: the ringing bell with its halo, the live time, the alarm
 * label, then Snooze and Dismiss pinned to the bottom.
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
            val repository = (application as ChronosApplication).repository
            val theme by repository.theme.collectAsStateWithLifecycle()

            ChronosTheme(theme = theme) {
                AlarmRingScreen(
                    alarmTime = hour,
                    alarmLabel = label.ifBlank { "Alarm" },
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
    alarmTime: Int,
    alarmLabel: String,
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
            animation = tween(700, easing = ChronosMotionStandard),
            repeatMode = RepeatMode.Reverse,
        ),
        label = "bellScale",
    )
    val haloAlpha by pulse.animateFloat(
        initialValue = 0.16f,
        targetValue = 0.44f,
        animationSpec = infiniteRepeatable(
            animation = tween(700, easing = ChronosMotionStandard),
            repeatMode = RepeatMode.Reverse,
        ),
        label = "haloAlpha",
    )
    val haloScale by pulse.animateFloat(
        initialValue = 1.06f,
        targetValue = 0.92f,
        animationSpec = infiniteRepeatable(
            animation = tween(700, easing = ChronosMotionStandard),
            repeatMode = RepeatMode.Reverse,
        ),
        label = "haloScale",
    )

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(ChronosColors.Surface)
            .statusBarsPadding(),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Spacer(Modifier.height(64.dp))

        // The bell and its halo are a single unit, so the glow is dead-centre on the
        // bell instead of drifting below it.
        Box(
            modifier = Modifier.size(200.dp),
            contentAlignment = Alignment.Center,
        ) {
            Box(
                modifier = Modifier
                    .size(180.dp)
                    .scale(haloScale)
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
            ChronosIcon(
                name = "alarm",
                size = 72.dp,
                tint = MaterialTheme.colorScheme.primaryContainer,
                filled = true,
                modifier = Modifier.scale(bellScale),
            )
        }

        Spacer(Modifier.height(16.dp))

        Text(
            text = now.format(DateTimeFormatter.ofPattern("h:mm")),
            style = ChronosType.DisplayHero,
            color = MaterialTheme.colorScheme.onSurface,
        )
        Text(
            text = now.format(DateTimeFormatter.ofPattern("EEEE, MMM d")),
            style = ChronosType.BodyLg,
            color = ChronosColors.OnSurfaceVariant,
        )

        Spacer(Modifier.height(28.dp))

        // Which alarm is ringing.
        Text(
            text = alarmLabel,
            style = ChronosType.HeadlineMd,
            color = MaterialTheme.colorScheme.primaryContainer,
            textAlign = TextAlign.Center,
        )

        Spacer(Modifier.weight(1f))

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .navigationBarsPadding(),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            if (snoozeEnabled) {
                ChronosPill(
                    text = "Snooze ${AlarmActions.SNOOZE_MINUTES}m",
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
    }
}

private val ChronosMotionStandard = androidx.compose.animation.core.CubicBezierEasing(0.2f, 0f, 0f, 1f)

private val UnusedWidth = width(0.dp)
private val UnusedAlign = Alignment.CenterEnd

private fun width(value: androidx.compose.ui.unit.Dp) = value

private val UnusedRadius = RoundedCornerShape(ChronosRadius.Card)