package com.example.networkspeedmeter

import com.example.networkspeedmeter.data.model.DailyUsage
import org.junit.Assert.assertEquals
import org.junit.Test

class DailyUsageTest {

    @Test
    fun testTotalBytesSum() {
        val usage = DailyUsage(
            date = "2026-09-26",
            mobileBytes = 500 * 1024 * 1024L,
            wifiBytes = 1500 * 1024 * 1024L
        )
        assertEquals(2000L * 1024 * 1024, usage.totalBytes)
    }

    @Test
    fun testFormatBytesMB() {
        val bytes = (450.5 * 1024 * 1024).toLong()
        assertEquals("450.5 MB", DailyUsage.formatBytes(bytes))
    }

    @Test
    fun testFormatBytesGB() {
        val bytes = (2.45 * 1024 * 1024 * 1024).toLong()
        assertEquals("2.45 GB", DailyUsage.formatBytes(bytes))
    }

    @Test
    fun testFormatBytesZero() {
        assertEquals("0 B", DailyUsage.formatBytes(0L))
    }
}
