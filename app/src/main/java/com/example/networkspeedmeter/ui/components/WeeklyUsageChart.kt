package com.example.networkspeedmeter.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
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
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.networkspeedmeter.data.model.DailyUsage
import com.example.networkspeedmeter.ui.theme.MobileDataColor
import com.example.networkspeedmeter.ui.theme.WifiDataColor
import java.text.SimpleDateFormat
import java.util.Locale

@Composable
fun WeeklyUsageChart(
    weeklyData: List<DailyUsage>,
    modifier: Modifier = Modifier
) {
    val cardBg = MaterialTheme.colorScheme.surface
    val borderCol = MaterialTheme.colorScheme.outline

    var selectedIndex by remember { mutableIntStateOf(weeklyData.size - 1) }

    val maxTotal = weeklyData.maxOfOrNull { it.totalBytes } ?: 0L
    val peakChartBytes = maxOf(maxTotal, 1024L * 1024L * 100L) // Minimum 100MB scale

    val parseFormat = remember { SimpleDateFormat("yyyy-MM-dd", Locale.US) }
    val dayNameFormat = remember { SimpleDateFormat("EEE", Locale.US) }

    val totalWeeklyBytes = weeklyData.sumOf { it.totalBytes }
    val selectedDayUsage = weeklyData.getOrNull(selectedIndex)

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
                    text = "LAST 7 DAYS ACTIVITY",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.sp
                )
                Text(
                    text = "7-Day: ${DailyUsage.formatBytes(totalWeeklyBytes)}",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.SemiBold
                )
            }

            Spacer(modifier = Modifier.height(18.dp))

            // Interactive Canvas Stacked Bar Chart
            Canvas(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(150.dp)
            ) {
                val canvasWidth = size.width
                val canvasHeight = size.height
                val count = weeklyData.size
                if (count == 0) return@Canvas

                val slotWidth = canvasWidth / count
                val barWidth = slotWidth * 0.45f
                val cornerRadius = CornerRadius(6.dp.toPx(), 6.dp.toPx())

                weeklyData.forEachIndexed { i, usage ->
                    val centerX = i * slotWidth + slotWidth / 2f
                    val left = centerX - barWidth / 2f

                    val totalH = ((usage.totalBytes.toFloat() / peakChartBytes.toFloat()) * (canvasHeight - 10f))
                        .coerceAtLeast(4f)

                    val mobileRatio = if (usage.totalBytes > 0) usage.mobileBytes.toFloat() / usage.totalBytes.toFloat() else 0f
                    val mobileH = totalH * mobileRatio
                    val wifiH = totalH - mobileH

                    val isSelected = (i == selectedIndex)

                    // Draw Wi-Fi (Top segment)
                    if (wifiH > 0f) {
                        drawRoundRect(
                            color = if (isSelected) WifiDataColor else WifiDataColor.copy(alpha = 0.7f),
                            topLeft = Offset(left, canvasHeight - totalH),
                            size = Size(barWidth, wifiH),
                            cornerRadius = cornerRadius
                        )
                    }

                    // Draw Mobile (Bottom segment)
                    if (mobileH > 0f) {
                        drawRoundRect(
                            color = if (isSelected) MobileDataColor else MobileDataColor.copy(alpha = 0.7f),
                            topLeft = Offset(left, canvasHeight - mobileH),
                            size = Size(barWidth, mobileH),
                            cornerRadius = cornerRadius
                        )
                    }

                    // Draw Selection Highlight ring if selected
                    if (isSelected) {
                        drawRoundRect(
                            color = androidx.compose.ui.graphics.Color.White.copy(alpha = 0.5f),
                            topLeft = Offset(left - 2.dp.toPx(), canvasHeight - totalH - 2.dp.toPx()),
                            size = Size(barWidth + 4.dp.toPx(), totalH + 4.dp.toPx()),
                            cornerRadius = CornerRadius(8.dp.toPx(), 8.dp.toPx()),
                            style = androidx.compose.ui.graphics.drawscope.Stroke(width = 2.dp.toPx())
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Day Labels Row with Click interaction
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceAround
            ) {
                weeklyData.forEachIndexed { index, usage ->
                    val dayName = try {
                        val d = parseFormat.parse(usage.date)
                        if (d != null) dayNameFormat.format(d) else usage.date.takeLast(2)
                    } catch (_: Exception) {
                        usage.date.takeLast(2)
                    }

                    val isSelected = (index == selectedIndex)

                    Box(
                        modifier = Modifier
                            .clickable(
                                interactionSource = remember { MutableInteractionSource() },
                                indication = null
                            ) {
                                selectedIndex = index
                            }
                            .padding(horizontal = 4.dp, vertical = 2.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = dayName,
                            style = MaterialTheme.typography.labelSmall,
                            color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Selected Day Breakdown Box
            if (selectedDayUsage != null) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f))
                        .padding(horizontal = 14.dp, vertical = 10.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = selectedDayUsage.date,
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Text(
                                text = "Total: ${DailyUsage.formatBytes(selectedDayUsage.totalBytes)}",
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }

                        Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                            // Mobile stat
                            Column(horizontalAlignment = Alignment.End) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Box(
                                        modifier = Modifier
                                            .size(6.dp)
                                            .clip(CircleShape)
                                            .background(MobileDataColor)
                                    )
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text(
                                        text = "Mobile",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                                Text(
                                    text = DailyUsage.formatBytes(selectedDayUsage.mobileBytes),
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = FontWeight.SemiBold,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                            }

                            // Wi-Fi stat
                            Column(horizontalAlignment = Alignment.End) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Box(
                                        modifier = Modifier
                                            .size(6.dp)
                                            .clip(CircleShape)
                                            .background(WifiDataColor)
                                    )
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text(
                                        text = "Wi-Fi",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                                Text(
                                    text = DailyUsage.formatBytes(selectedDayUsage.wifiBytes),
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = FontWeight.SemiBold,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
