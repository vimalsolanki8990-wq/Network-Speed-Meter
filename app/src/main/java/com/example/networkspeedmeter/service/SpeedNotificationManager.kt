package com.example.networkspeedmeter.service

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import android.graphics.Typeface
import android.text.SpannableString
import android.text.Spanned
import android.text.style.ForegroundColorSpan
import android.text.style.StyleSpan
import com.example.networkspeedmeter.MainActivity
import com.example.networkspeedmeter.R
import com.example.networkspeedmeter.data.model.AppSettings
import com.example.networkspeedmeter.data.model.DailyUsage
import com.example.networkspeedmeter.data.model.SpeedData

class SpeedNotificationManager(private val context: Context) {

    private val notificationManager =
        context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

    private val iconRenderer = StatusIconRenderer()

    init {
        createNotificationChannel()
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                CHANNEL_NAME,
                NotificationManager.IMPORTANCE_LOW
            ).apply {
                description = "Displays live network transfer speed and daily data usage stats."
                setShowBadge(false)
                enableLights(false)
                enableVibration(false)
                setSound(null, null)
                lockscreenVisibility = Notification.VISIBILITY_PUBLIC
            }
            notificationManager.createNotificationChannel(channel)
        }
    }

    fun buildNotification(
        speedData: SpeedData,
        todayUsage: DailyUsage,
        settings: AppSettings
    ): Notification {
        val (downVal, downUnit) = SpeedData.formatSpeed(speedData.downBytesPerSec, settings.unitFormat)
        val (upVal, upUnit) = SpeedData.formatSpeed(speedData.upBytesPerSec, settings.unitFormat)

        val rawTitle = "↓ $downVal $downUnit    ↑ $upVal $upUnit"
        val title = SpannableString(rawTitle).apply {
            setSpan(ForegroundColorSpan(settings.textColor), 0, length, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)
            setSpan(StyleSpan(Typeface.BOLD), 0, length, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)
        }

        val mobileStr = DailyUsage.formatBytes(todayUsage.mobileBytes)
        val wifiStr = DailyUsage.formatBytes(todayUsage.wifiBytes)
        val totalStr = DailyUsage.formatBytes(todayUsage.totalBytes)

        val shortText = "Mobile: $mobileStr  •  Wi-Fi: $wifiStr"

        val expandedText = buildString {
            append("📱 Mobile Data: ").append(mobileStr).append("\n")
            append("📶 Wi-Fi Data:  ").append(wifiStr).append("\n")
            append("📊 Total Today: ").append(totalStr).append("\n\n")
            append("↓ Download: ").append(downVal).append(" ").append(downUnit).append("\n")
            append("↑ Upload:   ").append(upVal).append(" ").append(upUnit)
        }

        val openAppIntent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_SINGLE_TOP or Intent.FLAG_ACTIVITY_CLEAR_TOP
        }
        val pendingIntent = PendingIntent.getActivity(
            context,
            0,
            openAppIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        // Generate dynamic Canvas status bar icon and colored large icon badge
        val dynamicIcon = iconRenderer.renderIcon(speedData, settings)
        val displayBytes = speedData.getDisplayBytes(settings.displayOption)
        val largeIconBitmap = iconRenderer.renderPreviewBitmap(displayBytes, settings)

        return NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(dynamicIcon)
            .setLargeIcon(largeIconBitmap)
            .setColor(settings.textColor)
            .setContentTitle(title)
            .setContentText(shortText)
            .setStyle(
                NotificationCompat.BigTextStyle()
                    .setBigContentTitle("Network Speed & Daily Usage")
                    .bigText(expandedText)
            )
            .setContentIntent(pendingIntent)
            .setOngoing(true)
            .setOnlyAlertOnce(true)
            .setShowWhen(false)
            .setForegroundServiceBehavior(NotificationCompat.FOREGROUND_SERVICE_IMMEDIATE)
            .setCategory(NotificationCompat.CATEGORY_SERVICE)
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
            .build()
    }

    fun updateNotification(
        notificationId: Int,
        speedData: SpeedData,
        todayUsage: DailyUsage,
        settings: AppSettings
    ) {
        val notification = buildNotification(speedData, todayUsage, settings)
        notificationManager.notify(notificationId, notification)
    }

    companion object {
        const val CHANNEL_ID = "network_speed_meter_channel"
        const val CHANNEL_NAME = "Network Speed Meter"
        const val NOTIFICATION_ID = 1001
    }
}
