package com.example.networkspeedmeter.data.model

import java.util.Locale

data class DailyUsage(
    val date: String, // Format: YYYY-MM-DD
    val mobileBytes: Long = 0L,
    val wifiBytes: Long = 0L
) {
    val totalBytes: Long
        get() = mobileBytes + wifiBytes

    companion object {
        fun formatBytes(bytes: Long): String {
            val safeBytes = if (bytes < 0) 0L else bytes
            return when {
                safeBytes >= 1024L * 1024 * 1024 * 1024 -> String.format(Locale.US, "%.2f TB", safeBytes.toDouble() / (1024.0 * 1024 * 1024 * 1024))
                safeBytes >= 1024L * 1024 * 1024 -> String.format(Locale.US, "%.2f GB", safeBytes.toDouble() / (1024.0 * 1024 * 1024))
                safeBytes >= 1024L * 1024 -> String.format(Locale.US, "%.1f MB", safeBytes.toDouble() / (1024.0 * 1024))
                safeBytes >= 1024L -> String.format(Locale.US, "%.1f KB", safeBytes.toDouble() / 1024.0)
                else -> "$safeBytes B"
            }
        }
    }
}
