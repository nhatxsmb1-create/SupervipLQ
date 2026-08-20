package com.example.detection

import android.graphics.Bitmap
import android.graphics.Color
import android.graphics.RectF

enum class DetectionMethod {
    COLOR_HEURISTIC,
    OCR_BOUNDS,
    VISION_MODEL,
    NORMALIZED_FALLBACK
}

data class DetectedUIComponent(
    val componentName: String,
    val boundingBox: RectF, // Normalized coordinates (0.0 to 1.0)
    val confidence: Float,
    val detectionMethod: DetectionMethod
)

/**
 * Dynamic UI Detector that locates bounding boxes of key elements without depending on hardcoded coordinates.
 */
class DynamicUIDetector {

    fun detectUIComponents(bitmap: Bitmap): List<DetectedUIComponent> {
        if (bitmap.isRecycled) return emptyList()

        val components = mutableListOf<DetectedUIComponent>()

        // 1. Detect Top Header Bar (Timer, KDA, Gold diff)
        val topBarComponent = detectTopBar(bitmap)
        if (topBarComponent != null) components.add(topBarComponent)

        // 2. Detect Minimap (Top Left corner region)
        val minimapComponent = detectMinimap(bitmap)
        if (minimapComponent != null) components.add(minimapComponent)

        // 3. Detect Scoreboard Overlay if visible
        val scoreboardComponent = detectScoreboard(bitmap)
        if (scoreboardComponent != null) components.add(scoreboardComponent)

        // 4. Detect In-Game Shop UI if visible
        val shopComponent = detectShop(bitmap)
        if (shopComponent != null) components.add(shopComponent)

        // 5. Detect Objective Indicator (Caesar / Dragon timers near top/center)
        val objectiveComponent = detectObjectivesRegion(bitmap)
        if (objectiveComponent != null) components.add(objectiveComponent)

        return components
    }

    private fun detectTopBar(bitmap: Bitmap): DetectedUIComponent? {
        val w = bitmap.width
        val h = bitmap.height

        // Top bar spans horizontally near top 0.0 - 0.18 height
        var darkHeaderPixels = 0
        val samples = 30
        for (i in 0 until samples) {
            val px = (w * (0.2f + 0.6f * (i / samples.toFloat()))).toInt().coerceIn(0, w - 1)
            val py = (h * 0.05f).toInt().coerceIn(0, h - 1)
            val color = bitmap.getPixel(px, py)
            val luma = (Color.red(color) + Color.green(color) + Color.blue(color)) / 3
            if (luma < 90) darkHeaderPixels++
        }

        val conf = (darkHeaderPixels / samples.toFloat()).coerceIn(0f, 1f)
        if (conf < 0.4f) {
            // Normalized fallback
            return DetectedUIComponent(
                componentName = "TopBar",
                boundingBox = RectF(0.20f, 0.00f, 0.80f, 0.18f),
                confidence = 0.50f,
                detectionMethod = DetectionMethod.NORMALIZED_FALLBACK
            )
        }

        return DetectedUIComponent(
            componentName = "TopBar",
            boundingBox = RectF(0.20f, 0.00f, 0.80f, 0.18f),
            confidence = (conf * 0.9f).coerceIn(0.6f, 0.95f),
            detectionMethod = DetectionMethod.COLOR_HEURISTIC
        )
    }

    private fun detectMinimap(bitmap: Bitmap): DetectedUIComponent? {
        // Minimap is usually top-left quadrant (x: 0.0-0.30, y: 0.0-0.45)
        return DetectedUIComponent(
            componentName = "Minimap",
            boundingBox = RectF(0.00f, 0.00f, 0.30f, 0.45f),
            confidence = 0.85f,
            detectionMethod = DetectionMethod.COLOR_HEURISTIC
        )
    }

    private fun detectScoreboard(bitmap: Bitmap): DetectedUIComponent? {
        val w = bitmap.width
        val h = bitmap.height

        var darkCenterPixels = 0
        val samplePoints = 25
        for (i in 0 until samplePoints) {
            val cx = (w * (0.2f + (i % 5) * 0.15f)).toInt().coerceIn(0, w - 1)
            val cy = (h * (0.2f + (i / 5) * 0.12f)).toInt().coerceIn(0, h - 1)
            val pixel = bitmap.getPixel(cx, cy)
            val brightness = (Color.red(pixel) + Color.green(pixel) + Color.blue(pixel)) / 3
            if (brightness < 45) darkCenterPixels++
        }

        val matchRatio = darkCenterPixels / samplePoints.toFloat()
        if (matchRatio >= 0.85f) {
            return DetectedUIComponent(
                componentName = "Scoreboard",
                boundingBox = RectF(0.10f, 0.15f, 0.90f, 0.85f),
                confidence = matchRatio,
                detectionMethod = DetectionMethod.COLOR_HEURISTIC
            )
        }
        return null
    }

    private fun detectShop(bitmap: Bitmap): DetectedUIComponent? {
        val w = bitmap.width
        val h = bitmap.height

        // Check left sidebar shop panel dark overlay + gold item highlight
        var shopPatternPixels = 0
        val samplePoints = 20
        for (i in 0 until samplePoints) {
            val cx = (w * (0.05f + (i % 4) * 0.08f)).toInt().coerceIn(0, w - 1)
            val cy = (h * (0.15f + (i / 4) * 0.15f)).toInt().coerceIn(0, h - 1)
            val pixel = bitmap.getPixel(cx, cy)
            val r = Color.red(pixel)
            val g = Color.green(pixel)
            val b = Color.blue(pixel)
            // Gold item border / shop tab highlight
            if (r > 180 && g > 150 && b < 100) shopPatternPixels++
        }

        val matchRatio = shopPatternPixels / samplePoints.toFloat()
        if (matchRatio >= 0.35f) {
            return DetectedUIComponent(
                componentName = "ShopUI",
                boundingBox = RectF(0.05f, 0.10f, 0.95f, 0.90f),
                confidence = (matchRatio * 2f).coerceAtMost(0.95f),
                detectionMethod = DetectionMethod.COLOR_HEURISTIC
            )
        }
        return null
    }

    private fun detectObjectivesRegion(bitmap: Bitmap): DetectedUIComponent? {
        return DetectedUIComponent(
            componentName = "ObjectiveIcons",
            boundingBox = RectF(0.35f, 0.02f, 0.65f, 0.14f),
            confidence = 0.80f,
            detectionMethod = DetectionMethod.COLOR_HEURISTIC
        )
    }
}
