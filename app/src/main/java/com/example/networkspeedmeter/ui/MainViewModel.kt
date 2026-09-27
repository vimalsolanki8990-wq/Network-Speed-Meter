package com.example.networkspeedmeter.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.networkspeedmeter.data.local.SettingsRepository
import com.example.networkspeedmeter.data.local.UsageDatabaseHelper
import com.example.networkspeedmeter.data.model.AppSettings
import com.example.networkspeedmeter.data.model.DailyUsage
import com.example.networkspeedmeter.data.model.SpeedData
import com.example.networkspeedmeter.service.SpeedMeterService
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class MainViewModel(application: Application) : AndroidViewModel(application) {

    private val settingsRepo = SettingsRepository.getInstance(application)
    private val dbHelper = UsageDatabaseHelper.getInstance(application)

    val settings: StateFlow<AppSettings> = settingsRepo.settings

    val isServiceRunning: StateFlow<Boolean> = SpeedMeterService.isServiceRunning
    val currentSpeed: StateFlow<SpeedData> = SpeedMeterService.currentSpeed
    val todayUsage: StateFlow<DailyUsage> = dbHelper.todayUsage

    private val _weeklyUsage = MutableStateFlow<List<DailyUsage>>(emptyList())
    val weeklyUsage: StateFlow<List<DailyUsage>> = _weeklyUsage.asStateFlow()

    private val _monthlyUsage = MutableStateFlow<List<DailyUsage>>(emptyList())
    val monthlyUsage: StateFlow<List<DailyUsage>> = _monthlyUsage.asStateFlow()

    private val _speedHistory = MutableStateFlow<List<SpeedData>>(emptyList())
    val speedHistory: StateFlow<List<SpeedData>> = _speedHistory.asStateFlow()

    private val _peakDownBytes = MutableStateFlow(0L)
    val peakDownBytes: StateFlow<Long> = _peakDownBytes.asStateFlow()

    private val _peakUpBytes = MutableStateFlow(0L)
    val peakUpBytes: StateFlow<Long> = _peakUpBytes.asStateFlow()

    init {
        loadHistoricalUsage()

        // Track live speed stream to build 30-second rolling sparkline and track session peaks
        viewModelScope.launch {
            SpeedMeterService.currentSpeed.collect { speed ->
                // Update peaks
                if (speed.downBytesPerSec > _peakDownBytes.value) {
                    _peakDownBytes.value = speed.downBytesPerSec
                }
                if (speed.upBytesPerSec > _peakUpBytes.value) {
                    _peakUpBytes.value = speed.upBytesPerSec
                }

                // Add to rolling history (max 30 points)
                val current = _speedHistory.value.toMutableList()
                current.add(speed)
                if (current.size > 30) {
                    current.removeAt(0)
                }
                _speedHistory.value = current
            }
        }

        // Periodic refresh of weekly & monthly usage from DB
        viewModelScope.launch(Dispatchers.IO) {
            while (true) {
                loadHistoricalUsage()
                delay(10_000L) // Refresh every 10s
            }
        }
    }

    fun loadHistoricalUsage() {
        viewModelScope.launch(Dispatchers.IO) {
            _weeklyUsage.value = dbHelper.getLast7DaysUsage()
            _monthlyUsage.value = dbHelper.getLast30DaysUsage()
        }
    }

    fun toggleService(enable: Boolean) {
        val app = getApplication<Application>()
        val updated = settings.value.copy(isServiceEnabled = enable)
        settingsRepo.updateSettings(updated)

        if (enable) {
            SpeedMeterService.start(app)
        } else {
            SpeedMeterService.stop(app)
        }
    }

    fun updateSettings(newSettings: AppSettings) {
        settingsRepo.updateSettings(newSettings)
    }

    fun clearAllHistory() {
        viewModelScope.launch(Dispatchers.IO) {
            dbHelper.clearAllHistory()
            loadHistoricalUsage()
        }
    }

    fun resetSessionPeaks() {
        _peakDownBytes.value = 0L
        _peakUpBytes.value = 0L
    }
}
