package com.example.networkspeedmeter.ui.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Divider
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.networkspeedmeter.R
import com.example.networkspeedmeter.data.model.AppSettings
import com.example.networkspeedmeter.data.model.DisplayOption
import com.example.networkspeedmeter.data.model.FontStyleOption
import com.example.networkspeedmeter.data.model.IndicatorSizeOption
import com.example.networkspeedmeter.data.model.UnitFormat
import com.example.networkspeedmeter.service.BatteryOptimizationHelper
import com.example.networkspeedmeter.service.StatusIconRenderer
import com.example.networkspeedmeter.ui.theme.EmeraldGreen
import com.example.networkspeedmeter.ui.theme.SpeedDownloadColor
import com.example.networkspeedmeter.ui.theme.VividOrange
import kotlin.math.roundToInt

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    settings: AppSettings,
    onUpdateSettings: (AppSettings) -> Unit,
    modifier: Modifier = Modifier
) {
    val scrollState = rememberScrollState()
    val iconRenderer = remember { StatusIconRenderer() }

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(scrollState)
            .padding(horizontal = 16.dp, vertical = 8.dp),
        verticalArrangement = Arrangement.spacedBy(20.dp)
    ) {
        // Section: Live Status Bar Preview
        LiveStatusBarPreviewCard(settings = settings, iconRenderer = iconRenderer)

        // Section: Indicator Appearance
        SettingsSection(title = "STATUS BAR INDICATOR APPEARANCE") {
            // Text Color Swatches Header with Selected Color Name
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Indicator Text Color",
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = AppSettings.getColorName(settings.textColor),
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
            }
            Spacer(modifier = Modifier.height(10.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                AppSettings.PRESET_COLORS.forEach { colorInt ->
                    val isSelected = settings.textColor == colorInt
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .clip(CircleShape)
                            .background(Color(colorInt))
                            .border(
                                width = if (isSelected) 3.dp else 1.dp,
                                color = if (isSelected) MaterialTheme.colorScheme.primary else Color.Gray.copy(alpha = 0.5f),
                                shape = CircleShape
                            )
                            .clickable {
                                onUpdateSettings(settings.copy(textColor = colorInt))
                            },
                        contentAlignment = Alignment.Center
                    ) {
                        if (isSelected) {
                            Box(
                                modifier = Modifier
                                    .size(10.dp)
                                    .clip(CircleShape)
                                    .background(if (colorInt == 0xFFFFFFFF.toInt()) Color.Black else Color.White)
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))
            HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.4f))
            Spacer(modifier = Modifier.height(16.dp))

            // Font Style Picker
            Text(
                text = "Font Styling",
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSurface
            )
            Spacer(modifier = Modifier.height(8.dp))
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                FontStyleOption.values().forEach { fontOption ->
                    val isSelected = settings.fontStyle == fontOption
                    FilterChip(
                        selected = isSelected,
                        onClick = { onUpdateSettings(settings.copy(fontStyle = fontOption)) },
                        modifier = Modifier.wrapContentWidth(),
                        label = {
                            Text(
                                text = when (fontOption) {
                                    FontStyleOption.BOLD -> "Bold Modern"
                                    FontStyleOption.MONOSPACE -> "Monospace"
                                    FontStyleOption.COMPACT -> "Compact Sans"
                                },
                                maxLines = 1,
                                softWrap = false,
                                modifier = Modifier.padding(horizontal = 4.dp)
                            )
                        },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.2f),
                            selectedLabelColor = MaterialTheme.colorScheme.primary
                        )
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))
            HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.4f))
            Spacer(modifier = Modifier.height(16.dp))

            // Indicator Text Size Customization
            Text(
                text = "Indicator Text Size",
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSurface
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "Adjust status bar font size: Small (9sp), Normal (11sp), Large (13sp)",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.height(8.dp))
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                IndicatorSizeOption.values().forEach { sizeOption ->
                    val isSelected = settings.indicatorSize == sizeOption
                    FilterChip(
                        selected = isSelected,
                        onClick = { onUpdateSettings(settings.copy(indicatorSize = sizeOption)) },
                        modifier = Modifier.wrapContentWidth(),
                        label = {
                            Text(
                                text = when (sizeOption) {
                                    IndicatorSizeOption.SMALL -> "Small (9sp - compact)"
                                    IndicatorSizeOption.NORMAL -> "Normal (11sp - default)"
                                    IndicatorSizeOption.LARGE -> "Large (13sp - prominent)"
                                },
                                maxLines = 1,
                                softWrap = false,
                                modifier = Modifier.padding(horizontal = 4.dp)
                            )
                        },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.2f),
                            selectedLabelColor = MaterialTheme.colorScheme.primary
                        )
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))
            HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.4f))
            Spacer(modifier = Modifier.height(16.dp))

            // Unit Format Toggle
            Text(
                text = "Unit Format",
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSurface
            )
            Spacer(modifier = Modifier.height(8.dp))
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                FilterChip(
                    selected = settings.unitFormat == UnitFormat.BYTES,
                    onClick = { onUpdateSettings(settings.copy(unitFormat = UnitFormat.BYTES)) },
                    modifier = Modifier.wrapContentWidth(),
                    label = {
                        Text(
                            text = "Bytes (KB/s, MB/s)",
                            maxLines = 1,
                            softWrap = false,
                            modifier = Modifier.padding(horizontal = 4.dp)
                        )
                    },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.2f),
                        selectedLabelColor = MaterialTheme.colorScheme.primary
                    )
                )
                FilterChip(
                    selected = settings.unitFormat == UnitFormat.BITS,
                    onClick = { onUpdateSettings(settings.copy(unitFormat = UnitFormat.BITS)) },
                    modifier = Modifier.wrapContentWidth(),
                    label = {
                        Text(
                            text = "Bits (Kbps, Mbps)",
                            maxLines = 1,
                            softWrap = false,
                            modifier = Modifier.padding(horizontal = 4.dp)
                        )
                    },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.2f),
                        selectedLabelColor = MaterialTheme.colorScheme.primary
                    )
                )
            }

            Spacer(modifier = Modifier.height(16.dp))
            HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.4f))
            Spacer(modifier = Modifier.height(16.dp))

            // Display Mode
            Text(
                text = "Display Speed Target",
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSurface
            )
            Spacer(modifier = Modifier.height(8.dp))
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                DisplayOption.values().forEach { option ->
                    val isSelected = settings.displayOption == option
                    FilterChip(
                        selected = isSelected,
                        onClick = { onUpdateSettings(settings.copy(displayOption = option)) },
                        modifier = Modifier.wrapContentWidth(),
                        label = {
                            Text(
                                text = when (option) {
                                    DisplayOption.COMBINED -> "Combined (Total)"
                                    DisplayOption.DOWNLOAD_ONLY -> "Download Only"
                                    DisplayOption.UPLOAD_ONLY -> "Upload Only"
                                },
                                maxLines = 1,
                                softWrap = false,
                                modifier = Modifier.padding(horizontal = 4.dp)
                            )
                        },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.2f),
                            selectedLabelColor = MaterialTheme.colorScheme.primary
                        )
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))
            HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.4f))
            Spacer(modifier = Modifier.height(16.dp))

            // Hide When Idle Toggle
            SettingsToggleRow(
                title = "Hide When Idle",
                subtitle = "Hides the status bar icon when transfer speed drops below 1 KB/s",
                checked = settings.hideWhenIdle,
                onCheckedChange = { onUpdateSettings(settings.copy(hideWhenIdle = it)) }
            )
        }

        // Section: Battery & Background Engine
        SettingsSection(title = "BATTERY & SYSTEM PERSISTENCE") {
            SettingsToggleRow(
                title = "Screen-Off Smart Sleep (0% Drain)",
                subtitle = "Stops 1000ms polling when screen locks to achieve zero idle battery drain while retaining hardware counters",
                checked = settings.smartSleepEnabled,
                onCheckedChange = { onUpdateSettings(settings.copy(smartSleepEnabled = it)) }
            )

            Spacer(modifier = Modifier.height(16.dp))
            HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.4f))
            Spacer(modifier = Modifier.height(16.dp))

            SettingsToggleRow(
                title = "Auto-Restart on Boot",
                subtitle = "Starts the speed meter foreground service automatically when device boots",
                checked = settings.startOnBoot,
                onCheckedChange = { onUpdateSettings(settings.copy(startOnBoot = it)) }
            )

            Spacer(modifier = Modifier.height(16.dp))
            HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.4f))
            Spacer(modifier = Modifier.height(16.dp))

            val context = LocalContext.current
            var isIgnoringBatteryOpt by remember {
                mutableStateOf(BatteryOptimizationHelper.isIgnoringBatteryOptimizations(context))
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "Doze Mode Battery Exemption",
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = if (isIgnoringBatteryOpt)
                            "Exempted (Unrestricted) — Android OS will not kill the tracking service overnight"
                        else
                            "Optimized (Restricted) — Tap to exempt so tracking stays continuously active 24/7",
                        style = MaterialTheme.typography.bodySmall,
                        color = if (isIgnoringBatteryOpt) EmeraldGreen else MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                Spacer(modifier = Modifier.width(12.dp))
                Button(
                    onClick = {
                        BatteryOptimizationHelper.requestIgnoreBatteryOptimizations(context)
                        isIgnoringBatteryOpt = BatteryOptimizationHelper.isIgnoringBatteryOptimizations(context)
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (isIgnoringBatteryOpt)
                            EmeraldGreen.copy(alpha = 0.15f)
                        else
                            MaterialTheme.colorScheme.primary,
                        contentColor = if (isIgnoringBatteryOpt)
                            EmeraldGreen
                        else
                            MaterialTheme.colorScheme.onPrimary
                    ),
                    shape = RoundedCornerShape(12.dp),
                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp)
                ) {
                    Text(
                        text = if (isIgnoringBatteryOpt) "Active" else "Exempt",
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }

        // Section: Alerts & Theme
        SettingsSection(title = "ALERTS & THEME") {
            SettingsToggleRow(
                title = "Speed Spike Haptic Alert",
                subtitle = "Triggers a subtle vibration when transfer speed exceeds a threshold",
                checked = settings.speedAlertEnabled,
                onCheckedChange = { onUpdateSettings(settings.copy(speedAlertEnabled = it)) }
            )

            if (settings.speedAlertEnabled) {
                Spacer(modifier = Modifier.height(12.dp))
                Column {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = "Spike Alert Threshold",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            text = "${settings.speedAlertThresholdMbps.roundToInt()} MB/s",
                            style = MaterialTheme.typography.bodySmall,
                            fontWeight = FontWeight.Bold,
                            color = VividOrange
                        )
                    }
                    Slider(
                        value = settings.speedAlertThresholdMbps,
                        onValueChange = { onUpdateSettings(settings.copy(speedAlertThresholdMbps = it)) },
                        valueRange = 2f..50f,
                        steps = 23,
                        colors = SliderDefaults.colors(
                            thumbColor = VividOrange,
                            activeTrackColor = VividOrange
                        )
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))
            HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.4f))
            Spacer(modifier = Modifier.height(16.dp))

            SettingsToggleRow(
                title = "AMOLED Pure Black Theme",
                subtitle = "Uses true #000000 black surface to maximize battery savings on OLED screens",
                checked = settings.isAmoledDark,
                onCheckedChange = { onUpdateSettings(settings.copy(isAmoledDark = it)) }
            )
        }

        // Section: Privacy Guarantee Card
        PrivacyBadgeCard()

        Spacer(modifier = Modifier.height(16.dp))
    }
}

@Composable
private fun LiveStatusBarPreviewCard(
    settings: AppSettings,
    iconRenderer: StatusIconRenderer
) {
    val cardBg = MaterialTheme.colorScheme.surface
    val borderCol = MaterialTheme.colorScheme.outline

    val sampleBytes = 46L * 1024L * 1024L // 46 MB/s sample
    val previewBitmap = iconRenderer.renderPreviewBitmap(sampleBytes, settings)

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(20.dp))
            .background(cardBg)
            .border(1.dp, borderCol, RoundedCornerShape(20.dp))
            .padding(18.dp)
    ) {
        Column {
            Text(
                text = "LIVE STATUS BAR ICON PREVIEW",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                fontWeight = FontWeight.Bold,
                letterSpacing = 1.sp
            )
            Spacer(modifier = Modifier.height(12.dp))

            // Simulated Status Bar Mockup
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .background(Color(0xFF0F141C))
                    .border(1.dp, Color(0xFF232D3F), RoundedCornerShape(12.dp))
                    .padding(horizontal = 16.dp, vertical = 10.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Left Side: Clock + Dynamic Speed Indicator (matching Android status bar)
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text(
                            text = "12:00",
                            style = MaterialTheme.typography.bodySmall,
                            fontWeight = FontWeight.SemiBold,
                            color = Color.White
                        )

                        Box(
                            modifier = Modifier
                                .size(26.dp)
                                .clip(RoundedCornerShape(4.dp))
                                .background(Color.Black.copy(alpha = 0.3f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Image(
                                bitmap = previewBitmap.asImageBitmap(),
                                contentDescription = "Speed Icon Preview",
                                modifier = Modifier.size(24.dp)
                            )
                        }
                    }

                    // Right Side: System Status Icons (Wi-Fi, Signal, Battery)
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        // Wi-Fi System Icon
                        Icon(
                            painter = painterResource(id = R.drawable.ic_wifi),
                            contentDescription = "Wi-Fi",
                            tint = Color.White.copy(alpha = 0.8f),
                            modifier = Modifier.size(16.dp)
                        )

                        // Cellular System Icon
                        Icon(
                            painter = painterResource(id = R.drawable.ic_cellular),
                            contentDescription = "Signal",
                            tint = Color.White.copy(alpha = 0.8f),
                            modifier = Modifier.size(16.dp)
                        )

                        // Battery
                        Icon(
                            painter = painterResource(id = R.drawable.ic_battery_saver),
                            contentDescription = "Battery",
                            tint = EmeraldGreen,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun SettingsSection(
    title: String,
    content: @Composable () -> Unit
) {
    val cardBg = MaterialTheme.colorScheme.surface
    val borderCol = MaterialTheme.colorScheme.outline

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(20.dp))
            .background(cardBg)
            .border(1.dp, borderCol, RoundedCornerShape(20.dp))
            .padding(18.dp)
    ) {
        Column {
            Text(
                text = title,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                fontWeight = FontWeight.Bold,
                letterSpacing = 1.sp
            )
            Spacer(modifier = Modifier.height(16.dp))
            content()
        }
    }
}

@Composable
private fun SettingsToggleRow(
    title: String,
    subtitle: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSurface
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        Spacer(modifier = Modifier.width(16.dp))
        Switch(
            checked = checked,
            onCheckedChange = onCheckedChange,
            colors = SwitchDefaults.colors(
                checkedThumbColor = MaterialTheme.colorScheme.onPrimary,
                checkedTrackColor = EmeraldGreen
            )
        )
    }
}

@Composable
private fun PrivacyBadgeCard() {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(18.dp))
            .background(EmeraldGreen.copy(alpha = 0.08f))
            .border(1.dp, EmeraldGreen.copy(alpha = 0.3f), RoundedCornerShape(18.dp))
            .padding(16.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(
                painter = painterResource(id = R.drawable.ic_shield_check),
                contentDescription = "Strict Privacy",
                tint = EmeraldGreen,
                modifier = Modifier.size(30.dp)
            )
            Spacer(modifier = Modifier.width(14.dp))
            Column {
                Text(
                    text = "Strict Privacy Guarantee (100% Offline)",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    color = EmeraldGreen
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = "android.permission.INTERNET is completely absent from the app manifest. The application is physically incapable of transmitting data outside your device.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }
        }
    }
}
