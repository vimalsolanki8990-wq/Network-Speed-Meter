package com.example.networkspeedmeter.ui.components

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
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
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.networkspeedmeter.R
import com.example.networkspeedmeter.data.model.SpeedData
import com.example.networkspeedmeter.data.model.UnitFormat
import com.example.networkspeedmeter.ui.theme.SpeedDownloadColor
import com.example.networkspeedmeter.ui.theme.SpeedUploadColor

@Composable
fun SpeedGauge(
    speedData: SpeedData,
    peakDownBytes: Long,
    peakUpBytes: Long,
    unitFormat: UnitFormat,
    modifier: Modifier = Modifier
) {
    val (downValue, downUnit) = SpeedData.formatSpeed(speedData.downBytesPerSec, unitFormat)
    val (upValue, upUnit) = SpeedData.formatSpeed(speedData.upBytesPerSec, unitFormat)
    val (peakDownVal, peakDownUnit) = SpeedData.formatSpeed(peakDownBytes, unitFormat)
    val (peakUpVal, peakUpUnit) = SpeedData.formatSpeed(peakUpBytes, unitFormat)

    val downColor = SpeedDownloadColor
    val upColor = SpeedUploadColor

    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // Download Speed Card
        SpeedTile(
            modifier = Modifier.weight(1f),
            title = "DOWNLOAD",
            iconRes = R.drawable.ic_download,
            accentColor = downColor,
            value = downValue,
            unit = downUnit,
            peakValue = "$peakDownVal $peakDownUnit"
        )

        // Upload Speed Card
        SpeedTile(
            modifier = Modifier.weight(1f),
            title = "UPLOAD",
            iconRes = R.drawable.ic_upload,
            accentColor = upColor,
            value = upValue,
            unit = upUnit,
            peakValue = "$peakUpVal $peakUpUnit"
        )
    }
}

@Composable
private fun SpeedTile(
    title: String,
    iconRes: Int,
    accentColor: Color,
    value: String,
    unit: String,
    peakValue: String,
    modifier: Modifier = Modifier
) {
    val cardBg = MaterialTheme.colorScheme.surface
    val borderCol = MaterialTheme.colorScheme.outline

    Box(
        modifier = modifier
            .clip(RoundedCornerShape(20.dp))
            .background(cardBg)
            .border(1.dp, borderCol, RoundedCornerShape(20.dp))
            .padding(16.dp)
    ) {
        Column {
            // Header Row: Icon + Label
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(32.dp)
                        .clip(CircleShape)
                        .background(accentColor.copy(alpha = 0.15f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        painter = painterResource(id = iconRes),
                        contentDescription = title,
                        tint = accentColor,
                        modifier = Modifier.size(18.dp)
                    )
                }
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = title,
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.sp
                )
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Primary Speed Metric
            Row(
                verticalAlignment = Alignment.Bottom
            ) {
                Text(
                    text = value,
                    style = MaterialTheme.typography.displayMedium,
                    fontWeight = FontWeight.Bold,
                    color = accentColor
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = unit,
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(bottom = 6.dp)
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Peak Indicator
            Text(
                text = "Peak: $peakValue",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.8f)
            )
        }
    }
}
