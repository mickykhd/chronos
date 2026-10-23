package com.chronos.clock

import android.Manifest
import android.content.Intent
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import com.chronos.clock.service.AlarmService
import com.chronos.clock.service.ChronosNotifications
import com.chronos.clock.ui.theme.ChronosColors
import com.chronos.clock.ui.theme.ChronosTheme
import com.chronos.clock.ui.theme.ChronosType

class MainActivity : ComponentActivity() {

    private var ringing by mutableStateOf(false)

    private val notificationPermissionLauncher =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) { }

    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdge(
            statusBarStyle = androidx.activity.SystemBarStyle.dark(
                android.graphics.Color.TRANSPARENT,
            ),
            navigationBarStyle = androidx.activity.SystemBarStyle.dark(
                android.graphics.Color.TRANSPARENT,
            ),
        )
        super.onCreate(savedInstanceState)

        ChronosNotifications.ensureChannels(this)
        requestNotificationPermission()

        ringing = isRinging(intent)

        setContent {
            ChronosTheme {
                ChronosApp()

                if (ringing) {
                    AlarmRingDialog(
                        onDismiss = {
                            AlarmService.stop(this)
                            ringing = false
                        },
                    )
                }
            }
        }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        setIntent(intent)
        ringing = isRinging(intent)
    }

    private fun isRinging(intent: Intent?): Boolean =
        intent?.getBooleanExtra(ChronosNotifications.EXTRA_RINGING, false) == true

    private fun requestNotificationPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            notificationPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
        }
    }
}

@Composable
private fun AlarmRingDialog(onDismiss: () -> Unit) {
    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = ChronosColors.SurfaceContainerHigh,
        titleContentColor = ChronosColors.OnSurface,
        textContentColor = ChronosColors.OnSurface,
        title = {
            Text(
                text = "Alarm",
                style = ChronosType.TitleLg,
                color = ChronosColors.OnSurface,
            )
        },
        text = {
            Text(
                text = "Your alarm is ringing.",
                style = ChronosType.BodyLg,
                color = ChronosColors.OnSurface,
            )
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text(
                    text = "Dismiss",
                    style = ChronosType.LabelLg,
                    color = ChronosColors.Primary,
                )
            }
        },
    )
}
