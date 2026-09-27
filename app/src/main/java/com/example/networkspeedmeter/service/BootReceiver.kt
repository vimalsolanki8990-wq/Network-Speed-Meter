package com.example.networkspeedmeter.service

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.example.networkspeedmeter.data.local.SettingsRepository

class BootReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context?, intent: Intent?) {
        if (context == null || intent == null) return

        val action = intent.action
        if (action == Intent.ACTION_BOOT_COMPLETED ||
            action == Intent.ACTION_LOCKED_BOOT_COMPLETED ||
            action == Intent.ACTION_MY_PACKAGE_REPLACED ||
            action == "android.intent.action.QUICKBOOT_POWERON" ||
            action == "com.htc.intent.action.QUICKBOOT_POWERON" ||
            action == Intent.ACTION_REBOOT ||
            action == Intent.ACTION_USER_PRESENT
        ) {
            val settingsRepo = SettingsRepository.getInstance(context)
            if (settingsRepo.isServiceEnabled() && settingsRepo.isStartOnBootEnabled()) {
                SpeedMeterService.start(context)
            }
        }
    }
}
