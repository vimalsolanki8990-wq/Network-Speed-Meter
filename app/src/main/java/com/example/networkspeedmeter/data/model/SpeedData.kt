package com.example.networkspeedmeter.data.model

import java.util.Locale

enum class UnitFormat {
    BYTES, // KB/s, MB/s, GB/s
    BITS   // Kbps, Mbps, Gbps
}

enum class DisplayOption {
    COMBINED,      // Shows total of down + up or toggles/combines
    DOWNLOAD_ONLY, // Shows download only
    UPLOAD_ONLY    // Shows upload only
}

enum class FontStyleOption {
    BOLD,
    MONOSPACE,
    COMPACT
}

data class SpeedData(
    val downBytesPerSec: Long = 0L,
    val upBytesPerSec: Long = 0L,
    val timestamp: Long = System.currentTimeMillis()
) {
    val totalBytesPerSec: Long
        get() = downBytesPerSec + upBytesPerSec

    fun getDisplayBytes(displayOption: DisplayOption): Long {
        return when (displayOption) {
            DisplayOption.DOWNLOAD_ONLY -> downBytesPerSec
            DisplayOption.UPLOAD_ONLY -> upBytesPerSec
            DisplayOption.COMBINED -> totalBytesPerSec
        }
    }

    companion object {
        fun formatSpeed(bytesPerSec: Long, unit: UnitFormat): Pair<String, String> {
            val safeBytes = if (bytesPerSec < 0) 0L else bytesPerSec

            return if (unit == UnitFormat.BITS) {
                val bits = safeBytes * 8.0
                when {
                    bits >= 1_000_000_000 -> String.format(Locale.US, "%.1f", bits / 1_000_000_000) to "Gbps"
                    bits >= 1_000_000 -> String.format(Locale.US, "%.1f", bits / 1_000_000) to "Mbps"
                    bits >= 1_000 -> String.format(Locale.US, "%.0f", bits / 1_000) to "Kbps"
                    else -> String.format(Locale.US, "%.0f", bits) to "bps"
                }
            } else {
                when {
                    safeBytes >= 1024L * 1024 * 1024 -> String.format(Locale.US, "%.2f", safeBytes.toDouble() / (1024.0 * 1024 * 1024)) to "GB/s"
                    safeBytes >= 1024L * 1024 -> {
                        val mb = safeBytes.toDouble() / (1024.0 * 1024)
                        if (mb >= 10.0) String.format(Locale.US, "%.1f", mb) to "MB/s"
                        else String.format(Locale.US, "%.2f", mb) to "MB/s"
                    }
                    safeBytes >= 1024L -> {
                        val kb = safeBytes.toDouble() / 1024.0
                        if (kb >= 100.0) String.format(Locale.US, "%.0f", kb) to "KB/s"
                        else String.format(Locale.US, "%.1f", kb) to "KB/s"
                    }
                    else -> "$safeBytes" to "B/s"
                }
            }
        }

        /**
         * Compact formatting specifically engineered for small status bar icons (e.g. 24dp/48-96px Canvas)
         * Returns (Value, UnitLetter) such as ("45", "K"), ("2.1", "M"), ("105", "K"), ("0", "B")
         */
        fun formatForStatusBar(bytesPerSec: Long, unit: UnitFormat): Pair<String, String> {
            val safeBytes = if (bytesPerSec < 0) 0L else bytesPerSec

            return if (unit == UnitFormat.BITS) {
                val bits = safeBytes * 8.0
                when {
                    bits >= 1_000_000_000 -> String.format(Locale.US, "%.1f", bits / 1_000_000_000) to "G"
                    bits >= 100_000_000 -> String.format(Locale.US, "%.0f", bits / 1_000_000) to "M"
                    bits >= 1_000_000 -> String.format(Locale.US, "%.1f", bits / 1_000_000) to "M"
                    bits >= 100_000 -> String.format(Locale.US, "%.0f", bits / 1_000) to "K"
                    bits >= 1_000 -> String.format(Locale.US, "%.0f", bits / 1_000) to "K"
                    else -> String.format(Locale.US, "%.0f", bits) to "b"
                }
            } else {
                when {
                    safeBytes >= 1024L * 1024 * 1024 -> String.format(Locale.US, "%.1f", safeBytes.toDouble() / (1024.0 * 1024 * 1024)) to "G"
                    safeBytes >= 100L * 1024 * 1024 -> String.format(Locale.US, "%.0f", safeBytes.toDouble() / (1024.0 * 1024)) to "M"
                    safeBytes >= 1024L * 1024 -> String.format(Locale.US, "%.1f", safeBytes.toDouble() / (1024.0 * 1024)) to "M"
                    safeBytes >= 100L * 1024 -> String.format(Locale.US, "%.0f", safeBytes.toDouble() / 1024.0) to "K"
                    safeBytes >= 1024L -> String.format(Locale.US, "%.0f", safeBytes.toDouble() / 1024.0) to "K"
                    else -> "$safeBytes" to "B"
                }
            }
        }
    }
}
