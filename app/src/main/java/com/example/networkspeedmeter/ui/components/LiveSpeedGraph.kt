package com.example.networkspeedmeter.ui.components

import androidx.compose.foundation.Canvas
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
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.networkspeedmeter.data.model.SpeedData
import com.example.networkspeedmeter.data.model.UnitFormat
import com.example.networkspeedmeter.ui.theme.SpeedDownloadColor
import com.example.networkspeedmeter.ui.theme.SpeedUploadColor

@Composable
fun LiveSpeedGraph(
    history: List<SpeedData>,
    unitFormat: UnitFormat,
    modifier: Modifier = Modifier
) {
    val cardBg = MaterialTheme.colorScheme.surface
    val borderCol = MaterialTheme.colorScheme.outline
    val gridColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.3f)

    // Calculate max scale based on history (minimum 100 KB/s for stable baseline)
    val maxDownload = history.maxOfOrNull { it.downBytesPerSec } ?: 0L
    val maxUpload = history.maxOfOrNull { it.upBytesPerSec } ?: 0L
    val peakOverall = maxOf(maxDownload, maxUpload, 100L * 1024L)

    val (peakValStr, peakUnitStr) = SpeedData.formatSpeed(peakOverall, unitFormat)

    Box(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(20.dp))
            .background(cardBg)
            .border(1.dp, borderCol, RoundedCornerShape(20.dp))
            .padding(16.dp)
    ) {
        Column {
            // Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "REAL-TIME TRAFFIC (30s)",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.sp
                )
                Text(
                    text = "Scale: $peakValStr $peakUnitStr",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Canvas Line Chart
            Canvas(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(130.dp)
            ) {
                val width = size.width
                val height = size.height

                // Reference Grid lines
                for (i in 1..3) {
                    val y = height * (i / 4f)
                    drawLine(
                        color = gridColor,
                        start = Offset(0f, y),
                        end = Offset(width, y),
                        strokeWidth = 1f
                    )
                }

                if (history.size < 2) return@Canvas

                val stepX = width / (history.size - 1).coerceAtLeast(1)

                // Build Download Path & Fill
                val downPath = Path()
                val downFillPath = Path()
                downFillPath.moveTo(0f, height)

                history.forEachIndexed { index, sample ->
                    val x = index * stepX
                    val ratio = (sample.downBytesPerSec.toFloat() / peakOverall.toFloat()).coerceIn(0f, 1f)
                    val y = height - (ratio * (height - 8f))

                    if (index == 0) {
                        downPath.moveTo(x, y)
                        downFillPath.lineTo(x, y)
                    } else {
                        downPath.lineTo(x, y)
                        downFillPath.lineTo(x, y)
                    }
                }
                downFillPath.lineTo((history.size - 1) * stepX, height)
                downFillPath.close()

                // Draw gradient under download line
                drawPath(
                    path = downFillPath,
                    brush = Brush.verticalGradient(
                        colors = listOf(
                            SpeedDownloadColor.copy(alpha = 0.25f),
                            SpeedDownloadColor.copy(alpha = 0.02f)
                        ),
                        startY = 0f,
                        endY = height
                    )
                )

                // Draw Download line
                drawPath(
                    path = downPath,
                    color = SpeedDownloadColor,
                    style = Stroke(width = 3.dp.toPx(), cap = StrokeCap.Round)
                )

                // Build Upload Path
                val upPath = Path()
                history.forEachIndexed { index, sample ->
                    val x = index * stepX
                    val ratio = (sample.upBytesPerSec.toFloat() / peakOverall.toFloat()).coerceIn(0f, 1f)
                    val y = height - (ratio * (height - 8f))

                    if (index == 0) {
                        upPath.moveTo(x, y)
                    } else {
                        upPath.lineTo(x, y)
                    }
                }

                // Draw Upload line
                drawPath(
                    path = upPath,
                    color = SpeedUploadColor,
                    style = Stroke(width = 2.dp.toPx(), cap = StrokeCap.Round)
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Legend
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Download legend
                Box(
                    modifier = Modifier
                        .size(8.dp)
                        .clip(CircleShape)
                        .background(SpeedDownloadColor)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = "Download",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Spacer(modifier = Modifier.width(24.dp))

                // Upload legend
                Box(
                    modifier = Modifier
                        .size(8.dp)
                        .clip(CircleShape)
                        .background(SpeedUploadColor)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = "Upload",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}
