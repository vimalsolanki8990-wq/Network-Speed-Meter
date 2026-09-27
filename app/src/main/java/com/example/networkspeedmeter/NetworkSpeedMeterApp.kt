package com.example.networkspeedmeter

import android.app.Application
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.content.ContextCompat
import com.example.networkspeedmeter.data.local.SettingsRepository
import com.example.networkspeedmeter.data.local.UsageDatabaseHelper
import com.example.networkspeedmeter.service.SpeedMeterService

class NetworkSpeedMeterApp : Application() {

    lateinit var settingsRepository: SettingsRepository
        private set

    lateinit var databaseHelper: UsageDatabaseHelper
        private set

    override fun onCreate() {
        super.onCreate()
        settingsRepository = SettingsRepository.getInstance(this)
        databaseHelper = UsageDatabaseHelper.getInstance(this)

        // Automatically start service on app launch if enabled and permitted
        if (settingsRepository.isServiceEnabled()) {
            val canPostNotifications = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                ContextCompat.checkSelfPermission(
                    this,
                    android.Manifest.permission.POST_NOTIFICATIONS
                ) == PackageManager.PERMISSION_GRANTED
            } else {
                true
            }

            if (canPostNotifications) {
                SpeedMeterService.start(this)
            }
        }
    }
}
