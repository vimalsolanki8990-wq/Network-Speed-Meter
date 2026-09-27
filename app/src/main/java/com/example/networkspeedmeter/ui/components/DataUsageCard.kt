package com.example.networkspeedmeter.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
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
import com.example.networkspeedmeter.data.model.DailyUsage
import com.example.networkspeedmeter.ui.theme.MobileDataColor
import com.example.networkspeedmeter.ui.theme.WifiDataColor

@Composable
fun DataUsageCard(
    todayUsage: DailyUsage,
    modifier: Modifier = Modifier
) {
    val cardBg = MaterialTheme.colorScheme.surface
    val borderCol = MaterialTheme.colorScheme.outline

    val mobileStr = DailyUsage.formatBytes(todayUsage.mobileBytes)
    val wifiStr = DailyUsage.formatBytes(todayUsage.wifiBytes)
    val totalStr = DailyUsage.formatBytes(todayUsage.totalBytes)

    val totalBytes = todayUsage.totalBytes.toFloat()
    val mobileWeight = if (totalBytes > 0) (todayUsage.mobileBytes.toFloat() / totalBytes).coerceIn(0f, 1f) else 0.5f
    val wifiWeight = 1f - mobileWeight

    Box(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(20.dp))
            .background(cardBg)
            .border(1.dp, borderCol, RoundedCornerShape(20.dp))
            .padding(18.dp)
    ) {
        Column {
            // Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "TODAY'S USAGE",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.sp
                )
                // Total Badge
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .background(MaterialTheme.colorScheme.surfaceVariant)
                        .padding(horizontal = 10.dp, vertical = 4.dp)
                ) {
                    Text(
                        text = "Total: $totalStr",
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            }

            Spacer(modifier = Modifier.height(18.dp))

            // Two-column stat breakdown
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                // Mobile Data
                UsageItem(
                    label = "Mobile Data",
                    amount = mobileStr,
                    iconRes = R.drawable.ic_cellular,
                    color = MobileDataColor
                )

                // Wi-Fi Data
                UsageItem(
                    label = "Wi-Fi Data",
                    amount = wifiStr,
                    iconRes = R.drawable.ic_wifi,
                    color = WifiDataColor
                )
            }

            Spacer(modifier = Modifier.height(18.dp))

            // Proportional split progress bar
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(8.dp)
                    .clip(RoundedCornerShape(4.dp))
                    .background(MaterialTheme.colorScheme.surfaceVariant)
            ) {
                if (todayUsage.totalBytes > 0) {
                    Row(modifier = Modifier.matchParentSize()) {
                        if (mobileWeight > 0.01f) {
                            Box(
                                modifier = Modifier
                                    .weight(mobileWeight)
                                    .fillMaxWidth()
                                    .background(MobileDataColor)
                            )
                        }
                        if (wifiWeight > 0.01f) {
                            Box(
                                modifier = Modifier
                                    .weight(wifiWeight)
                                    .fillMaxWidth()
                                    .background(WifiDataColor)
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun UsageItem(
    label: String,
    amount: String,
    iconRes: Int,
    color: Color
) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Box(
            modifier = Modifier
                .size(36.dp)
                .clip(CircleShape)
                .background(color.copy(alpha = 0.15f)),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                painter = painterResource(id = iconRes),
                contentDescription = label,
                tint = color,
                modifier = Modifier.size(20.dp)
            )
        }
        Spacer(modifier = Modifier.width(12.dp))
        Column {
            Text(
                text = label,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Text(
                text = amount,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )
        }
    }
}
