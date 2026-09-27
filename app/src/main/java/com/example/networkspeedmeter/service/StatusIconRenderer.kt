package com.example.networkspeedmeter.service

import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.PorterDuff
import android.graphics.Rect
import android.graphics.Typeface
import androidx.core.graphics.drawable.IconCompat
import com.example.networkspeedmeter.data.model.AppSettings
import com.example.networkspeedmeter.data.model.FontStyleOption
import com.example.networkspeedmeter.data.model.IndicatorSizeOption
import com.example.networkspeedmeter.data.model.SpeedData

class StatusIconRenderer {

    private val iconSize = 96

    private val textPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        textAlign = Paint.Align.CENTER
        isSubpixelText = true
        isDither = true
        isFilterBitmap = true
    }

    private val unitPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        textAlign = Paint.Align.CENTER
        isSubpixelText = true
        isDither = true
        isFilterBitmap = true
    }

    private val textBounds = Rect()

    // Transparent 1x1 bitmap for hidden state
    private val transparentIcon: IconCompat by lazy {
        val emptyBitmap = Bitmap.createBitmap(1, 1, Bitmap.Config.ARGB_8888)
        IconCompat.createWithBitmap(emptyBitmap)
    }

    /**
     * Generates a dynamic status bar IconCompat displaying current speed and unit
     * using the user's selected ARGB text color on a transparent ARGB_8888 Canvas.
     */
    @Synchronized
    fun renderIcon(speedData: SpeedData, settings: AppSettings): IconCompat {
        val displayBytes = speedData.getDisplayBytes(settings.displayOption)

        // Check "Hide when Idle" condition (< 1 KB/s or configured threshold)
        val thresholdBytes = (settings.idleThresholdKbps * 1024).toLong()
        if (settings.hideWhenIdle && displayBytes < thresholdBytes) {
            return transparentIcon
        }

        val bitmap = Bitmap.createBitmap(iconSize, iconSize, Bitmap.Config.ARGB_8888)
        bitmap.setHasAlpha(true)
        val canvas = Canvas(bitmap)

        // Format speed: e.g. ("45", "K"), ("2.1", "M"), ("120", "K"), ("0", "B")
        val (valueStr, unitStr) = SpeedData.formatForStatusBar(displayBytes, settings.unitFormat)

        // Configure font style
        val typeface = when (settings.fontStyle) {
            FontStyleOption.BOLD -> Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            FontStyleOption.MONOSPACE -> Typeface.create(Typeface.MONOSPACE, Typeface.BOLD)
            FontStyleOption.COMPACT -> Typeface.create(Typeface.SANS_SERIF, Typeface.NORMAL)
        }

        val chosenColor = settings.textColor

        textPaint.typeface = typeface
        textPaint.color = chosenColor

        unitPaint.typeface = typeface
        unitPaint.color = chosenColor

        // Dynamic text size based on character count and indicator size setting
        val baseValueTextSize = when {
            valueStr.length <= 2 -> 46f
            valueStr.length == 3 -> 38f
            else -> 32f
        }
        val scale = settings.indicatorSize.scale
        textPaint.textSize = baseValueTextSize * scale
        unitPaint.textSize = 32f * scale

        val centerX = iconSize / 2f

        // Draw top row (speed value)
        textPaint.getTextBounds(valueStr, 0, valueStr.length, textBounds)
        val valueBaseline = when (settings.indicatorSize) {
            IndicatorSizeOption.SMALL -> 44f
            IndicatorSizeOption.NORMAL -> 46f
            IndicatorSizeOption.LARGE -> 48f
        }
        canvas.drawText(valueStr, centerX, valueBaseline, textPaint)

        // Draw bottom row (unit letter e.g. "K", "M")
        val unitBaseline = when (settings.indicatorSize) {
            IndicatorSizeOption.SMALL -> 84f
            IndicatorSizeOption.NORMAL -> 86f
            IndicatorSizeOption.LARGE -> 88f
        }
        canvas.drawText(unitStr, centerX, unitBaseline, unitPaint)

        return IconCompat.createWithBitmap(bitmap)
    }

    /**
     * Renders a preview Bitmap for the Compose settings screen and large notification badge.
     */
    @Synchronized
    fun renderPreviewBitmap(speedBytes: Long, settings: AppSettings): Bitmap {
        val previewBitmap = Bitmap.createBitmap(iconSize, iconSize, Bitmap.Config.ARGB_8888)
        previewBitmap.setHasAlpha(true)
        val previewCanvas = Canvas(previewBitmap)

        val (valueStr, unitStr) = SpeedData.formatForStatusBar(speedBytes, settings.unitFormat)

        val typeface = when (settings.fontStyle) {
            FontStyleOption.BOLD -> Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            FontStyleOption.MONOSPACE -> Typeface.create(Typeface.MONOSPACE, Typeface.BOLD)
            FontStyleOption.COMPACT -> Typeface.create(Typeface.SANS_SERIF, Typeface.NORMAL)
        }

        val chosenColor = settings.textColor

        textPaint.typeface = typeface
        textPaint.color = chosenColor

        unitPaint.typeface = typeface
        unitPaint.color = chosenColor

        val baseValueTextSize = when {
            valueStr.length <= 2 -> 46f
            valueStr.length == 3 -> 38f
            else -> 32f
        }
        val scale = settings.indicatorSize.scale
        textPaint.textSize = baseValueTextSize * scale
        unitPaint.textSize = 32f * scale

        val centerX = iconSize / 2f
        val valueBaseline = when (settings.indicatorSize) {
            IndicatorSizeOption.SMALL -> 44f
            IndicatorSizeOption.NORMAL -> 46f
            IndicatorSizeOption.LARGE -> 48f
        }
        val unitBaseline = when (settings.indicatorSize) {
            IndicatorSizeOption.SMALL -> 84f
            IndicatorSizeOption.NORMAL -> 86f
            IndicatorSizeOption.LARGE -> 88f
        }
        previewCanvas.drawText(valueStr, centerX, valueBaseline, textPaint)
        previewCanvas.drawText(unitStr, centerX, unitBaseline, unitPaint)

        return previewBitmap
    }
}
