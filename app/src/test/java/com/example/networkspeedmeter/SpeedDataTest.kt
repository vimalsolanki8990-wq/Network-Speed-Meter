package com.example.networkspeedmeter

import com.example.networkspeedmeter.data.model.DisplayOption
import com.example.networkspeedmeter.data.model.SpeedData
import com.example.networkspeedmeter.data.model.UnitFormat
import org.junit.Assert.assertEquals
import org.junit.Test

class SpeedDataTest {

    @Test
    fun testBytesFormatZero() {
        val (value, unit) = SpeedData.formatSpeed(0L, UnitFormat.BYTES)
        assertEquals("0", value)
        assertEquals("B/s", unit)
    }

    @Test
    fun testBytesFormatKilobytes() {
        val (value, unit) = SpeedData.formatSpeed(45 * 1024L, UnitFormat.BYTES)
        assertEquals("45.0", value)
        assertEquals("KB/s", unit)

        val (val120, unit120) = SpeedData.formatSpeed(120 * 1024L, UnitFormat.BYTES)
        assertEquals("120", val120)
        assertEquals("KB/s", unit120)
    }

    @Test
    fun testBytesFormatMegabytes() {
        val (value, unit) = SpeedData.formatSpeed((2.5 * 1024 * 1024).toLong(), UnitFormat.BYTES)
        assertEquals("2.50", value)
        assertEquals("MB/s", unit)
    }

    @Test
    fun testBitsFormatMegabits() {
        // 1 MB/s = 8 Mbps
        val (value, unit) = SpeedData.formatSpeed(1024 * 1024L, UnitFormat.BITS)
        assertEquals("8.4", value) // 1024*1024*8 = 8,388,608 bits = 8.4 Mbps
        assertEquals("Mbps", unit)
    }

    @Test
    fun testStatusBarCompactFormatting() {
        // 45 KB/s -> ("45", "K")
        val (v1, u1) = SpeedData.formatForStatusBar(45 * 1024L, UnitFormat.BYTES)
        assertEquals("45", v1)
        assertEquals("K", u1)

        // 2.1 MB/s -> ("2.1", "M")
        val (v2, u2) = SpeedData.formatForStatusBar((2.1 * 1024 * 1024).toLong(), UnitFormat.BYTES)
        assertEquals("2.1", v2)
        assertEquals("M", u2)

        // 0 B/s -> ("0", "B")
        val (v3, u3) = SpeedData.formatForStatusBar(0L, UnitFormat.BYTES)
        assertEquals("0", v3)
        assertEquals("B", u3)
    }

    @Test
    fun testDisplayOptionTargeting() {
        val data = SpeedData(downBytesPerSec = 5000L, upBytesPerSec = 1000L)
        assertEquals(5000L, data.getDisplayBytes(DisplayOption.DOWNLOAD_ONLY))
        assertEquals(1000L, data.getDisplayBytes(DisplayOption.UPLOAD_ONLY))
        assertEquals(6000L, data.getDisplayBytes(DisplayOption.COMBINED))
    }
}
