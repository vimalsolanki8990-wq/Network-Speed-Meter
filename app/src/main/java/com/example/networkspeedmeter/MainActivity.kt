package com.example.networkspeedmeter

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.networkspeedmeter.service.SpeedMeterService
import com.example.networkspeedmeter.ui.MainViewModel
import com.example.networkspeedmeter.ui.screens.DashboardScreen
import com.example.networkspeedmeter.ui.screens.HistoryScreen
import com.example.networkspeedmeter.ui.screens.SettingsScreen
import com.example.networkspeedmeter.ui.theme.EmeraldGreen
import com.example.networkspeedmeter.ui.theme.NetworkSpeedMeterTheme

class MainActivity : ComponentActivity() {

    private val viewModel: MainViewModel by viewModels()

    @OptIn(ExperimentalMaterial3Api::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            val settings by viewModel.settings.collectAsStateWithLifecycle()
            val isServiceRunning by viewModel.isServiceRunning.collectAsStateWithLifecycle()
            val currentSpeed by viewModel.currentSpeed.collectAsStateWithLifecycle()
            val todayUsage by viewModel.todayUsage.collectAsStateWithLifecycle()
            val weeklyUsage by viewModel.weeklyUsage.collectAsStateWithLifecycle()
            val monthlyUsage by viewModel.monthlyUsage.collectAsStateWithLifecycle()
            val speedHistory by viewModel.speedHistory.collectAsStateWithLifecycle()
            val peakDownBytes by viewModel.peakDownBytes.collectAsStateWithLifecycle()
            val peakUpBytes by viewModel.peakUpBytes.collectAsStateWithLifecycle()

            var hasNotificationPermission by remember {
                mutableStateOf(
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                        ContextCompat.checkSelfPermission(
                            this@MainActivity,
                            Manifest.permission.POST_NOTIFICATIONS
                        ) == PackageManager.PERMISSION_GRANTED
                    } else {
                        true
                    }
                )
            }

            val permissionLauncher = rememberLauncherForActivityResult(
                contract = ActivityResultContracts.RequestPermission()
            ) { isGranted ->
                hasNotificationPermission = isGranted
                if (isGranted && settings.isServiceEnabled) {
                    SpeedMeterService.start(this@MainActivity)
                }
            }

            LaunchedEffect(Unit) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU && !hasNotificationPermission) {
                    permissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                } else if (settings.isServiceEnabled && !isServiceRunning) {
                    SpeedMeterService.start(this@MainActivity)
                }
            }

            NetworkSpeedMeterTheme(isAmoled = settings.isAmoledDark) {
                var selectedTab by remember { mutableIntStateOf(0) }

                Scaffold(
                    topBar = {
                        TopAppBar(
                            title = {
                                Text(
                                    text = "Network Speed Meter",
                                    fontWeight = FontWeight.Bold,
                                    style = MaterialTheme.typography.titleLarge
                                )
                            },
                            actions = {
                                // Service Status Indicator Pill
                                Box(
                                    modifier = Modifier
                                        .padding(end = 16.dp)
                                        .size(10.dp)
                                        .clip(CircleShape)
                                        .background(
                                            if (isServiceRunning) EmeraldGreen
                                            else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
                                        )
                                )
                            },
                            colors = TopAppBarDefaults.topAppBarColors(
                                containerColor = MaterialTheme.colorScheme.background,
                                titleContentColor = MaterialTheme.colorScheme.onBackground
                            )
                        )
                    },
                    bottomBar = {
                        NavigationBar(
                            containerColor = MaterialTheme.colorScheme.surface,
                            contentColor = MaterialTheme.colorScheme.onSurface
                        ) {
                            NavigationBarItem(
                                selected = selectedTab == 0,
                                onClick = { selectedTab = 0 },
                                icon = {
                                    Icon(
                                        painter = painterResource(id = R.drawable.ic_speedometer),
                                        contentDescription = "Dashboard",
                                        modifier = Modifier.size(24.dp)
                                    )
                                },
                                label = { Text("Dashboard") },
                                colors = NavigationBarItemDefaults.colors(
                                    selectedIconColor = EmeraldGreen,
                                    selectedTextColor = EmeraldGreen,
                                    indicatorColor = EmeraldGreen.copy(alpha = 0.15f)
                                )
                            )

                            NavigationBarItem(
                                selected = selectedTab == 1,
                                onClick = {
                                    selectedTab = 1
                                    viewModel.loadHistoricalUsage()
                                },
                                icon = {
                                    Icon(
                                        painter = painterResource(id = R.drawable.ic_history),
                                        contentDescription = "History",
                                        modifier = Modifier.size(24.dp)
                                    )
                                },
                                label = { Text("History") },
                                colors = NavigationBarItemDefaults.colors(
                                    selectedIconColor = EmeraldGreen,
                                    selectedTextColor = EmeraldGreen,
                                    indicatorColor = EmeraldGreen.copy(alpha = 0.15f)
                                )
                            )

                            NavigationBarItem(
                                selected = selectedTab == 2,
                                onClick = { selectedTab = 2 },
                                icon = {
                                    Icon(
                                        painter = painterResource(id = R.drawable.ic_settings),
                                        contentDescription = "Settings",
                                        modifier = Modifier.size(24.dp)
                                    )
                                },
                                label = { Text("Settings") },
                                colors = NavigationBarItemDefaults.colors(
                                    selectedIconColor = EmeraldGreen,
                                    selectedTextColor = EmeraldGreen,
                                    indicatorColor = EmeraldGreen.copy(alpha = 0.15f)
                                )
                            )
                        }
                    },
                    modifier = Modifier.fillMaxSize()
                ) { innerPadding ->
                    Box(modifier = Modifier.padding(innerPadding)) {
                        when (selectedTab) {
                            0 -> DashboardScreen(
                                isServiceRunning = isServiceRunning,
                                currentSpeed = currentSpeed,
                                todayUsage = todayUsage,
                                speedHistory = speedHistory,
                                peakDownBytes = peakDownBytes,
                                peakUpBytes = peakUpBytes,
                                settings = settings,
                                hasNotificationPermission = hasNotificationPermission,
                                onRequestNotificationPermission = {
                                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                                        permissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                                    }
                                },
                                onToggleService = { enable ->
                                    viewModel.toggleService(enable)
                                }
                            )
                            1 -> HistoryScreen(
                                weeklyUsage = weeklyUsage,
                                monthlyUsage = monthlyUsage,
                                onClearHistory = {
                                    viewModel.clearAllHistory()
                                }
                            )
                            2 -> SettingsScreen(
                                settings = settings,
                                onUpdateSettings = { updated ->
                                    viewModel.updateSettings(updated)
                                }
                            )
                        }
                    }
                }
            }
        }
    }
}