package com.example.networkspeedmeter.service

import android.app.Service
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.ServiceInfo
import android.net.ConnectivityManager
import android.net.Network
import android.net.NetworkCapabilities
import android.net.NetworkRequest
import android.os.Build
import android.os.IBinder
import android.os.PowerManager
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import androidx.core.app.ServiceCompat
import com.example.networkspeedmeter.data.local.SettingsRepository
import com.example.networkspeedmeter.data.local.UsageDatabaseHelper
import com.example.networkspeedmeter.data.model.AppSettings
import com.example.networkspeedmeter.data.model.DailyUsage
import com.example.networkspeedmeter.data.model.SpeedData
import com.example.networkspeedmeter.data.tracker.NetworkTrafficEngine
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.cancel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch

class SpeedMeterService : Service() {

    private val serviceScope = CoroutineScope(Dispatchers.Default + Job())

    private lateinit var settingsRepository: SettingsRepository
    private lateinit var dbHelper: UsageDatabaseHelper
    private lateinit var notificationManager: SpeedNotificationManager
    private lateinit var connectivityManager: ConnectivityManager
    private val trafficEngine = NetworkTrafficEngine()

    private var trackingJob: Job? = null
    private var isScreenOff = false
    private var lastAlertTimeMs = 0L
    private var isNetworkCallbackRegistered = false

    private val screenReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            when (intent?.action) {
                Intent.ACTION_SCREEN_OFF -> {
                    isScreenOff = true
                }
                Intent.ACTION_SCREEN_ON -> {
                    isScreenOff = false
                    // Wake tracking loop immediately to refresh UI
                    restartTrackingLoop()
                }
            }
        }
    }

    private val networkCallback = object : ConnectivityManager.NetworkCallback() {
        override fun onAvailable(network: Network) {
            super.onAvailable(network)
            // Immediately wake and refresh tracking loop when internet becomes available
            trafficEngine.resetBaseline()
            restartTrackingLoop()
        }

        override fun onCapabilitiesChanged(
            network: Network,
            networkCapabilities: NetworkCapabilities
        ) {
            super.onCapabilitiesChanged(network, networkCapabilities)
            restartTrackingLoop()
        }

        override fun onLost(network: Network) {
            super.onLost(network)
            restartTrackingLoop()
        }
    }

    override fun onCreate() {
        super.onCreate()
        settingsRepository = SettingsRepository.getInstance(this)
        dbHelper = UsageDatabaseHelper.getInstance(this)
        notificationManager = SpeedNotificationManager(this)

        _isServiceRunning.value = true

        // Register dynamic screen off / on receiver for battery optimization
        val filter = IntentFilter().apply {
            addAction(Intent.ACTION_SCREEN_OFF)
            addAction(Intent.ACTION_SCREEN_ON)
        }
        registerReceiver(screenReceiver, filter)

        // Check initial screen state
        val powerManager = getSystemService(Context.POWER_SERVICE) as PowerManager
        isScreenOff = !powerManager.isInteractive

        // Register ConnectivityManager.NetworkCallback to automatically bind and refresh
        // speed tracking when connection is enabled/restored in the morning
        connectivityManager = getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                connectivityManager.registerDefaultNetworkCallback(networkCallback)
                isNetworkCallbackRegistered = true
            } else {
                val networkRequest = NetworkRequest.Builder()
                    .addCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
                    .build()
                connectivityManager.registerNetworkCallback(networkRequest, networkCallback)
                isNetworkCallbackRegistered = true
            }
        } catch (_: Exception) {}

        // Watch settings changes dynamically
        serviceScope.launch {
            settingsRepository.settings.collect { newSettings ->
                // Refresh notification with current settings
                val currentSpeed = _currentSpeed.value
                val todayUsage = dbHelper.todayUsage.value
                notificationManager.updateNotification(
                    SpeedNotificationManager.NOTIFICATION_ID,
                    currentSpeed,
                    todayUsage,
                    newSettings
                )
            }
        }
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        if (intent?.action == ACTION_STOP_SERVICE) {
            stopSelf()
            return START_NOT_STICKY
        }

        // Start Foreground Service
        startForegroundWithNotification()

        // Start the sampling loop
        restartTrackingLoop()

        return START_STICKY
    }

    private fun startForegroundWithNotification() {
        val initialSpeed = SpeedData()
        val initialUsage = dbHelper.getUsageForDate(dbHelper.todayDateString())
        val settings = settingsRepository.settings.value

        val notification = notificationManager.buildNotification(initialSpeed, initialUsage, settings)

        val fgsType = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
            ServiceInfo.FOREGROUND_SERVICE_TYPE_SPECIAL_USE or ServiceInfo.FOREGROUND_SERVICE_TYPE_DATA_SYNC
        } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            ServiceInfo.FOREGROUND_SERVICE_TYPE_DATA_SYNC
        } else {
            0
        }

        ServiceCompat.startForeground(
            this,
            SpeedNotificationManager.NOTIFICATION_ID,
            notification,
            fgsType
        )
    }

    private fun restartTrackingLoop() {
        trackingJob?.cancel()
        trafficEngine.resetBaseline()

        trackingJob = serviceScope.launch {
            while (isActive) {
                val settings = settingsRepository.settings.value

                if (isScreenOff && settings.smartSleepEnabled) {
                    // Ultralight polling during screen-off to reconcile SQLite data usage without waking UI
                    delay(5000L)
                    val sample = trafficEngine.sample()
                    val todayDate = dbHelper.todayDateString()
                    val todayUsage = dbHelper.addUsage(
                        todayDate,
                        sample.mobileBytesDelta,
                        sample.wifiBytesDelta
                    )
                    _currentSpeed.value = sample.speedData
                    notificationManager.updateNotification(
                        SpeedNotificationManager.NOTIFICATION_ID,
                        sample.speedData,
                        todayUsage,
                        settings
                    )
                } else {
                    val sample = trafficEngine.sample()
                    val todayDate = dbHelper.todayDateString()

                    // Accumulate data usage in local SQLite DB
                    val todayUsage = dbHelper.addUsage(
                        todayDate,
                        sample.mobileBytesDelta,
                        sample.wifiBytesDelta
                    )

                    _currentSpeed.value = sample.speedData

                    // Update persistent status bar icon and notification
                    notificationManager.updateNotification(
                        SpeedNotificationManager.NOTIFICATION_ID,
                        sample.speedData,
                        todayUsage,
                        settings
                    )

                    // Check speed spike alert threshold
                    checkSpeedAlert(sample.speedData, settings)

                    delay(1000L)
                }
            }
        }
    }

    private fun checkSpeedAlert(speed: SpeedData, settings: AppSettings) {
        if (!settings.speedAlertEnabled) return

        val speedMbps = (speed.totalBytesPerSec * 8.0) / 1_000_000.0
        val now = System.currentTimeMillis()

        if (speedMbps >= settings.speedAlertThresholdMbps && now - lastAlertTimeMs > ALERT_COOLDOWN_MS) {
            lastAlertTimeMs = now
            triggerHapticAlert()
        }
    }

    private fun triggerHapticAlert() {
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                val vibratorManager = getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as? VibratorManager
                vibratorManager?.defaultVibrator?.vibrate(
                    VibrationEffect.createOneShot(80, VibrationEffect.DEFAULT_AMPLITUDE)
                )
            } else {
                @Suppress("DEPRECATION")
                val vibrator = getSystemService(Context.VIBRATOR_SERVICE) as? Vibrator
                @Suppress("DEPRECATION")
                vibrator?.vibrate(80)
            }
        } catch (_: Exception) {
            // Graceful fallback if vibrator is unavailable
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        _isServiceRunning.value = false
        _currentSpeed.value = SpeedData()
        try {
            unregisterReceiver(screenReceiver)
        } catch (_: Exception) {}
        if (isNetworkCallbackRegistered) {
            try {
                connectivityManager.unregisterNetworkCallback(networkCallback)
            } catch (_: Exception) {}
            isNetworkCallbackRegistered = false
        }
        trackingJob?.cancel()
        serviceScope.cancel()
    }

    override fun onBind(intent: Intent?): IBinder? = null

    companion object {
        const val ACTION_START_SERVICE = "com.example.networkspeedmeter.START_SERVICE"
        const val ACTION_STOP_SERVICE = "com.example.networkspeedmeter.STOP_SERVICE"
        private const val ALERT_COOLDOWN_MS = 15_000L

        private val _isServiceRunning = MutableStateFlow(false)
        val isServiceRunning: StateFlow<Boolean> = _isServiceRunning.asStateFlow()

        private val _currentSpeed = MutableStateFlow(SpeedData())
        val currentSpeed: StateFlow<SpeedData> = _currentSpeed.asStateFlow()

        fun start(context: Context) {
            val intent = Intent(context, SpeedMeterService::class.java).apply {
                action = ACTION_START_SERVICE
            }
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                context.startForegroundService(intent)
            } else {
                context.startService(intent)
            }
        }

        fun stop(context: Context) {
            val intent = Intent(context, SpeedMeterService::class.java).apply {
                action = ACTION_STOP_SERVICE
            }
            context.startService(intent)
        }
    }
}
