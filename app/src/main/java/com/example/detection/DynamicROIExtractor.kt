package com.example.detection

import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.ColorMatrix
import android.graphics.ColorMatrixColorFilter
import android.graphics.Paint
import android.graphics.RectF
import android.util.Log

/**
 * Dynamically calculates regions of interest (ROI) inside detected UI component bounding boxes
 * and produces cropped sub-bitmaps for target OCR / Vision processing.
 */
class DynamicROIExtractor {

    fun cropComponent(fullBitmap: Bitmap, component: DetectedUIComponent): Bitmap? {
        return cropNormalizedRegion(fullBitmap, component.boundingBox)
    }

    fun cropNormalizedRegion(fullBitmap: Bitmap, normalizedRect: RectF): Bitmap? {
        if (fullBitmap.isRecycled) return null

        val w = fullBitmap.width
        val h = fullBitmap.height

        val left = (normalizedRect.left * w).toInt().coerceIn(0, w - 1)
        val top = (normalizedRect.top * h).toInt().coerceIn(0, h - 1)
        val right = (normalizedRect.right * w).toInt().coerceIn(left + 1, w)
        val bottom = (normalizedRect.bottom * h).toInt().coerceIn(top + 1, h)

        val cropWidth = right - left
        val cropHeight = bottom - top

        if (cropWidth <= 0 || cropHeight <= 0) return null

        return try {
            Bitmap.createBitmap(fullBitmap, left, top, cropWidth, cropHeight)
        } catch (e: Exception) {
            Log.e("DynamicROIExtractor", "Crop failed for rect: $normalizedRect", e)
            null
        }
    }

    /**
     * Enhanced high-contrast upscale specifically tuned for digital HUD game fonts (Liên Quân clock & score).
     * Increases resolution 2x, boosts bright white/gold text luminance and suppresses dark backgrounds.
     */
    fun createEnhancedOcrBitmap(source: Bitmap): Bitmap? {
        if (source.isRecycled) return null
        return try {
            val targetW = (source.width * 2).coerceAtMost(1920)
            val targetH = (source.height * 2).coerceAtMost(1080)
            val scaled = Bitmap.createScaledBitmap(source, targetW, targetH, true)

            val enhanced = Bitmap.createBitmap(targetW, targetH, Bitmap.Config.ARGB_8888)
            val canvas = Canvas(enhanced)
            val paint = Paint(Paint.ANTI_ALIAS_FLAG or Paint.FILTER_BITMAP_FLAG)

            // High-contrast color matrix: Boost contrast and convert to grayscale
            val matrix = ColorMatrix(floatArrayOf(
                2.5f, 0f, 0f, 0f, -80f,
                0f, 2.5f, 0f, 0f, -80f,
                0f, 0f, 2.5f, 0f, -80f,
                0f, 0f, 0f, 1f, 0f
            ))
            paint.colorFilter = ColorMatrixColorFilter(matrix)
            canvas.drawBitmap(scaled, 0f, 0f, paint)
            scaled.recycle()
            enhanced
        } catch (e: Exception) {
            Log.e("DynamicROIExtractor", "Enhanced OCR bitmap failed", e)
            null
        }
    }

    /**
     * Helper to subdivide a component ROI into specific child sub-regions (e.g. Timer box inside Header)
     */
    fun extractSubROIs(
        headerBitmap: Bitmap,
        timerNormalizedChild: RectF = RectF(0.35f, 0.0f, 0.65f, 0.60f),
        scoreNormalizedChild: RectF = RectF(0.20f, 0.0f, 0.80f, 0.90f)
    ): Map<String, Bitmap?> {
        val timerCrop = cropNormalizedRegion(headerBitmap, timerNormalizedChild)
        val scoreCrop = cropNormalizedRegion(headerBitmap, scoreNormalizedChild)
        return mapOf(
            "timer" to timerCrop,
            "score" to scoreCrop
        )
    }
}

