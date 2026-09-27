package com.example.networkspeedmeter.data.local

import android.content.Context
import android.content.SharedPreferences
import com.example.networkspeedmeter.data.model.AppSettings
import com.example.networkspeedmeter.data.model.DisplayOption
import com.example.networkspeedmeter.data.model.FontStyleOption
import com.example.networkspeedmeter.data.model.IndicatorSizeOption
import com.example.networkspeedmeter.data.model.UnitFormat
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class SettingsRepository(context: Context) {

    private val prefs: SharedPreferences =
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    private val _settings = MutableStateFlow(loadSettings())
    val settings: StateFlow<AppSettings> = _settings.asStateFlow()

    private fun loadSettings(): AppSettings {
        return AppSettings(
            isServiceEnabled = prefs.getBoolean(KEY_SERVICE_ENABLED, true),
            textColor = prefs.getInt(KEY_TEXT_COLOR, 0xFFFFFFFF.toInt()),
            fontStyle = FontStyleOption.valueOf(
                prefs.getString(KEY_FONT_STYLE, FontStyleOption.BOLD.name) ?: FontStyleOption.BOLD.name
            ),
            indicatorSize = IndicatorSizeOption.valueOf(
                prefs.getString(KEY_INDICATOR_SIZE, IndicatorSizeOption.NORMAL.name) ?: IndicatorSizeOption.NORMAL.name
            ),
            unitFormat = UnitFormat.valueOf(
                prefs.getString(KEY_UNIT_FORMAT, UnitFormat.BYTES.name) ?: UnitFormat.BYTES.name
            ),
            displayOption = DisplayOption.valueOf(
                prefs.getString(KEY_DISPLAY_OPTION, DisplayOption.COMBINED.name) ?: DisplayOption.COMBINED.name
            ),
            hideWhenIdle = prefs.getBoolean(KEY_HIDE_WHEN_IDLE, false),
            idleThresholdKbps = prefs.getFloat(KEY_IDLE_THRESHOLD, 1.0f),
            smartSleepEnabled = prefs.getBoolean(KEY_SMART_SLEEP, true),
            speedAlertEnabled = prefs.getBoolean(KEY_SPEED_ALERT, false),
            speedAlertThresholdMbps = prefs.getFloat(KEY_ALERT_THRESHOLD, 10.0f),
            startOnBoot = prefs.getBoolean(KEY_START_ON_BOOT, true),
            isAmoledDark = prefs.getBoolean(KEY_AMOLED_DARK, false)
        )
    }

    fun updateSettings(newSettings: AppSettings) {
        prefs.edit().apply {
            putBoolean(KEY_SERVICE_ENABLED, newSettings.isServiceEnabled)
            putInt(KEY_TEXT_COLOR, newSettings.textColor)
            putString(KEY_FONT_STYLE, newSettings.fontStyle.name)
            putString(KEY_INDICATOR_SIZE, newSettings.indicatorSize.name)
            putString(KEY_UNIT_FORMAT, newSettings.unitFormat.name)
            putString(KEY_DISPLAY_OPTION, newSettings.displayOption.name)
            putBoolean(KEY_HIDE_WHEN_IDLE, newSettings.hideWhenIdle)
            putFloat(KEY_IDLE_THRESHOLD, newSettings.idleThresholdKbps)
            putBoolean(KEY_SMART_SLEEP, newSettings.smartSleepEnabled)
            putBoolean(KEY_SPEED_ALERT, newSettings.speedAlertEnabled)
            putFloat(KEY_ALERT_THRESHOLD, newSettings.speedAlertThresholdMbps)
            putBoolean(KEY_START_ON_BOOT, newSettings.startOnBoot)
            putBoolean(KEY_AMOLED_DARK, newSettings.isAmoledDark)
            apply()
        }
        _settings.value = newSettings
    }

    fun isServiceEnabled(): Boolean = prefs.getBoolean(KEY_SERVICE_ENABLED, true)
    fun isStartOnBootEnabled(): Boolean = prefs.getBoolean(KEY_START_ON_BOOT, true)

    companion object {
        private const val PREFS_NAME = "network_speed_meter_prefs"

        private const val KEY_SERVICE_ENABLED = "service_enabled"
        private const val KEY_TEXT_COLOR = "text_color"
        private const val KEY_FONT_STYLE = "font_style"
        private const val KEY_INDICATOR_SIZE = "indicator_size"
        private const val KEY_UNIT_FORMAT = "unit_format"
        private const val KEY_DISPLAY_OPTION = "display_option"
        private const val KEY_HIDE_WHEN_IDLE = "hide_when_idle"
        private const val KEY_IDLE_THRESHOLD = "idle_threshold"
        private const val KEY_SMART_SLEEP = "smart_sleep"
        private const val KEY_SPEED_ALERT = "speed_alert"
        private const val KEY_ALERT_THRESHOLD = "alert_threshold"
        private const val KEY_START_ON_BOOT = "start_on_boot"
        private const val KEY_AMOLED_DARK = "amoled_dark"

        @Volatile
        private var INSTANCE: SettingsRepository? = null

        fun getInstance(context: Context): SettingsRepository {
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: SettingsRepository(context.applicationContext).also { INSTANCE = it }
            }
        }
    }
}
