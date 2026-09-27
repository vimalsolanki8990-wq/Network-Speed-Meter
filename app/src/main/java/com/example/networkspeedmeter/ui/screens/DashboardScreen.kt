package com.example.networkspeedmeter.ui.screens

import android.os.Build
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.networkspeedmeter.R
import com.example.networkspeedmeter.data.model.AppSettings
import com.example.networkspeedmeter.data.model.DailyUsage
import com.example.networkspeedmeter.data.model.SpeedData
import com.example.networkspeedmeter.ui.components.DataUsageCard
import com.example.networkspeedmeter.ui.components.LiveSpeedGraph
import com.example.networkspeedmeter.ui.components.SpeedGauge
import com.example.networkspeedmeter.ui.theme.EmeraldGreen
import com.example.networkspeedmeter.ui.theme.VividOrange

@Composable
fun DashboardScreen(
    isServiceRunning: Boolean,
    currentSpeed: SpeedData,
    todayUsage: DailyUsage,
    speedHistory: List<SpeedData>,
    peakDownBytes: Long,
    peakUpBytes: Long,
    settings: AppSettings,
    hasNotificationPermission: Boolean,
    onRequestNotificationPermission: () -> Unit,
    onToggleService: (Boolean) -> Unit,
    modifier: Modifier = Modifier
) {
    val scrollState = rememberScrollState()

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(scrollState)
            .padding(horizontal = 16.dp, vertical = 8.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Notification Permission Alert Card (Android 13+)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU && !hasNotificationPermission) {
            PermissionCard(onRequestPermission = onRequestNotificationPermission)
        }

        // Master Service Status Card
        ServiceStatusCard(
            isRunning = isServiceRunning,
            onToggle = onToggleService
        )

        // Live Speed Metric Tiles (Download & Upload)
        SpeedGauge(
            speedData = currentSpeed,
            peakDownBytes = peakDownBytes,
            peakUpBytes = peakUpBytes,
            unitFormat = settings.unitFormat
        )

        // Real-Time Canvas Traffic Sparkline
        LiveSpeedGraph(
            history = speedHistory,
            unitFormat = settings.unitFormat
        )

        // Today's Mobile vs Wi-Fi Usage
        DataUsageCard(
            todayUsage = todayUsage
        )

        // Ultra Battery Saver Notice
        if (settings.smartSleepEnabled) {
            SmartSleepBanner()
        }

        Spacer(modifier = Modifier.height(16.dp))
    }
}

@Composable
private fun ServiceStatusCard(
    isRunning: Boolean,
    onToggle: (Boolean) -> Unit
) {
    val cardBg = MaterialTheme.colorScheme.surface
    val borderCol = MaterialTheme.colorScheme.outline
    val activeColor = EmeraldGreen
    val inactiveColor = Color(0xFF64748B)

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(20.dp))
            .background(cardBg)
            .border(1.dp, borderCol, RoundedCornerShape(20.dp))
            .padding(18.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                // Glowing Pulse Dot
                Box(
                    modifier = Modifier
                        .size(14.dp)
                        .clip(CircleShape)
                        .background(if (isRunning) activeColor else inactiveColor)
                )

                Spacer(modifier = Modifier.width(14.dp))

                Column {
                    Text(
                        text = if (isRunning) "Speed Meter Active" else "Speed Meter Paused",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = if (isRunning) "Displaying real-time icon in status bar" else "Foreground service stopped",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            Switch(
                checked = isRunning,
                onCheckedChange = onToggle,
                colors = SwitchDefaults.colors(
                    checkedThumbColor = MaterialTheme.colorScheme.onPrimary,
                    checkedTrackColor = activeColor
                )
            )
        }
    }
}

@Composable
private fun SmartSleepBanner() {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
            .border(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.5f), RoundedCornerShape(16.dp))
            .padding(14.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(
                painter = painterResource(id = R.drawable.ic_battery_saver),
                contentDescription = "Battery Saver",
                tint = EmeraldGreen,
                modifier = Modifier.size(22.dp)
            )
            Spacer(modifier = Modifier.width(12.dp))
            Column {
                Text(
                    text = "Smart Sleep Active (0% Screen-off Drain)",
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = "Polling is halted when screen is locked. Data is reconciled instantly upon wake.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@Composable
private fun PermissionCard(onRequestPermission: () -> Unit) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(VividOrange.copy(alpha = 0.12f))
            .border(1.dp, VividOrange.copy(alpha = 0.4f), RoundedCornerShape(16.dp))
            .padding(16.dp)
    ) {
        Column {
            Text(
                text = "Notification Permission Required",
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold,
                color = VividOrange
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "Android 13+ requires notification permission to render the live speed meter in the status bar.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurface
            )
            Spacer(modifier = Modifier.height(10.dp))
            Button(
                onClick = onRequestPermission,
                colors = ButtonDefaults.buttonColors(containerColor = VividOrange)
            ) {
                Text(
                    text = "Grant Permission",
                    color = Color.White,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}
