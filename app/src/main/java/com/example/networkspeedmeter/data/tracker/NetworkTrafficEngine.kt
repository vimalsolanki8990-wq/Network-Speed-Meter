package com.example.networkspeedmeter.data.tracker

import android.net.TrafficStats
import android.os.SystemClock
import com.example.networkspeedmeter.data.model.SpeedData

data class TrafficSampleResult(
    val speedData: SpeedData,
    val mobileBytesDelta: Long,
    val wifiBytesDelta: Long
)

class NetworkTrafficEngine {

    private var lastTotalRxBytes: Long = -1L
    private var lastTotalTxBytes: Long = -1L
    private var lastMobileRxBytes: Long = -1L
    private var lastMobileTxBytes: Long = -1L
    private var lastSampleTimeMs: Long = 0L

    init {
        resetBaseline()
    }

    /**
     * Resets baseline counters to current hardware readings without generating deltas.
     */
    fun resetBaseline() {
        lastTotalRxBytes = TrafficStats.getTotalRxBytes()
        lastTotalTxBytes = TrafficStats.getTotalTxBytes()
        lastMobileRxBytes = TrafficStats.getMobileRxBytes()
        lastMobileTxBytes = TrafficStats.getMobileTxBytes()
        lastSampleTimeMs = SystemClock.elapsedRealtime()
    }

    /**
     * Samples traffic statistics and computes instantaneous transfer speeds (bytes/sec)
     * as well as cumulative Mobile and Wi-Fi data consumed in this interval.
     */
    fun sample(): TrafficSampleResult {
        val currentTotalRx = TrafficStats.getTotalRxBytes()
        val currentTotalTx = TrafficStats.getTotalTxBytes()
        val currentMobileRx = TrafficStats.getMobileRxBytes()
        val currentMobileTx = TrafficStats.getMobileTxBytes()
        val currentTimeMs = SystemClock.elapsedRealtime()

        // If this is the initial sample or stats are unsupported
        if (lastTotalRxBytes == -1L || currentTotalRx < 0 || currentTotalTx < 0) {
            lastTotalRxBytes = if (currentTotalRx >= 0) currentTotalRx else 0L
            lastTotalTxBytes = if (currentTotalTx >= 0) currentTotalTx else 0L
            lastMobileRxBytes = if (currentMobileRx >= 0) currentMobileRx else 0L
            lastMobileTxBytes = if (currentMobileTx >= 0) currentMobileTx else 0L
            lastSampleTimeMs = currentTimeMs
            return TrafficSampleResult(SpeedData(), 0L, 0L)
        }

        // Handle reboot or counter overflow
        if (currentTotalRx < lastTotalRxBytes || currentTotalTx < lastTotalTxBytes) {
            lastTotalRxBytes = currentTotalRx
            lastTotalTxBytes = currentTotalTx
            lastMobileRxBytes = if (currentMobileRx >= 0) currentMobileRx else 0L
            lastMobileTxBytes = if (currentMobileTx >= 0) currentMobileTx else 0L
            lastSampleTimeMs = currentTimeMs
            return TrafficSampleResult(SpeedData(), 0L, 0L)
        }

        val elapsedMs = (currentTimeMs - lastSampleTimeMs).coerceAtLeast(1L)
        val rxDelta = (currentTotalRx - lastTotalRxBytes).coerceAtLeast(0L)
        val txDelta = (currentTotalTx - lastTotalTxBytes).coerceAtLeast(0L)

        // Transfer speeds (bytes per second)
        val downSpeed = (rxDelta * 1000L) / elapsedMs
        val upSpeed = (txDelta * 1000L) / elapsedMs

        // Mobile vs Wi-Fi calculation
        val mRxDelta = if (currentMobileRx >= 0 && lastMobileRxBytes >= 0 && currentMobileRx >= lastMobileRxBytes) {
            currentMobileRx - lastMobileRxBytes
        } else {
            0L
        }
        val mTxDelta = if (currentMobileTx >= 0 && lastMobileTxBytes >= 0 && currentMobileTx >= lastMobileTxBytes) {
            currentMobileTx - lastMobileTxBytes
        } else {
            0L
        }

        val mobileDelta = mRxDelta + mTxDelta
        val totalDelta = rxDelta + txDelta
        val wifiDelta = (totalDelta - mobileDelta).coerceAtLeast(0L)

        // Advance baseline
        lastTotalRxBytes = currentTotalRx
        lastTotalTxBytes = currentTotalTx
        lastMobileRxBytes = if (currentMobileRx >= 0) currentMobileRx else 0L
        lastMobileTxBytes = if (currentMobileTx >= 0) currentMobileTx else 0L
        lastSampleTimeMs = currentTimeMs

        return TrafficSampleResult(
            speedData = SpeedData(
                downBytesPerSec = downSpeed,
                upBytesPerSec = upSpeed,
                timestamp = System.currentTimeMillis()
            ),
            mobileBytesDelta = mobileDelta,
            wifiBytesDelta = wifiDelta
        )
    }
}
