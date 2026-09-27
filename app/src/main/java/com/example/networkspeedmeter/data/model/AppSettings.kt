package com.example.networkspeedmeter.data.model

enum class IndicatorSizeOption(val label: String, val scale: Float) {
    SMALL("Small", 0.82f),
    NORMAL("Normal", 1.0f),
    LARGE("Large", 1.18f)
}

data class AppSettings(
    val isServiceEnabled: Boolean = true,
    val textColor: Int = 0xFFFFFFFF.toInt(),
    val fontStyle: FontStyleOption = FontStyleOption.BOLD,
    val indicatorSize: IndicatorSizeOption = IndicatorSizeOption.NORMAL,
    val unitFormat: UnitFormat = UnitFormat.BYTES,
    val displayOption: DisplayOption = DisplayOption.COMBINED,
    val hideWhenIdle: Boolean = false,
    val idleThresholdKbps: Float = 1.0f,
    val smartSleepEnabled: Boolean = true,
    val speedAlertEnabled: Boolean = false,
    val speedAlertThresholdMbps: Float = 10.0f,
    val startOnBoot: Boolean = true,
    val isAmoledDark: Boolean = false
) {
    companion object {
        // Predefined color palette for status bar indicator
        val PRESET_COLORS = listOf(
            0xFFFFFFFF.toInt(), // Pure White
            0xFF00E5FF.toInt(), // Neon Cyan
            0xFF00E676.toInt(), // Emerald Green
            0xFFFFEA00.toInt(), // Electric Yellow
            0xFFFF9100.toInt(), // Vivid Orange
            0xFFFF1744.toInt(), // Crimson Red
            0xFFD500F9.toInt(), // Cyberpunk Purple
            0xFF448AFF.toInt()  // Ice Blue
        )

        fun getColorName(colorInt: Int): String = when (colorInt) {
            0xFFFFFFFF.toInt() -> "Pure White"
            0xFF00E5FF.toInt() -> "Neon Cyan"
            0xFF00E676.toInt() -> "Emerald Green"
            0xFFFFEA00.toInt() -> "Electric Yellow"
            0xFFFF9100.toInt() -> "Vivid Orange"
            0xFFFF1744.toInt() -> "Crimson Red"
            0xFFD500F9.toInt() -> "Cyberpunk Purple"
            0xFF448AFF.toInt() -> "Ice Blue"
            else -> "Custom Color"
        }
    }
}
