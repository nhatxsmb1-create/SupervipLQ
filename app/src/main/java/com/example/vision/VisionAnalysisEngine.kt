package com.example.vision

import android.graphics.Bitmap
import android.graphics.Color
import android.util.Log
import com.example.model.DetectedScreenMode
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.text.TextRecognition
import com.google.mlkit.vision.text.latin.TextRecognizerOptions
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.withContext
import java.util.regex.Pattern
import kotlin.coroutines.resume

data class VisionScanResult(
    val detectedMode: DetectedScreenMode,
    val detectedTimeSeconds: Int? = null,
    val detectedGoldDiff: Int? = null,
    val detectedAllyKills: Int? = null,
    val detectedEnemyKills: Int? = null,
    val rawOcrSummary: String = "",
    val confidence: Float = 0.85f
)

interface IVisionEngine {
    suspend fun analyzeFrame(bitmap: Bitmap): VisionScanResult
    fun close()
}

class VisionAnalysisEngine : IVisionEngine {

    private val textRecognizer by lazy {
        TextRecognition.getClient(TextRecognizerOptions.DEFAULT_OPTIONS)
    }

    private val timePattern = Pattern.compile("(\\d{1,2})[:.](\\d{2})")
    private val scorePattern = Pattern.compile("(\\d{1,2})\\s*[-:]\\s*(\\d{1,2})")
    private val goldPattern = Pattern.compile("([\\d.]+)[kK]?\\s*vs\\s*([\\d.]+)[kK]?", Pattern.CASE_INSENSITIVE)

    override suspend fun analyzeFrame(bitmap: Bitmap): VisionScanResult = withContext(Dispatchers.Default) {
        try {
            // 1. Fast heuristic screen mode detection via pixel distribution & color checks
            val screenMode = detectScreenModeFast(bitmap)

            // 2. Crop top banner / header region for fast OCR (Timer, Kills, Gold)
            var ocrText = ""
            var headerCrop: Bitmap? = null
            try {
                headerCrop = cropHeaderRegion(bitmap)
                ocrText = recognizeText(headerCrop)
            } finally {
                headerCrop?.recycle()
            }

            // 3. Parse parsed OCR strings
            var parsedTimeSec: Int? = null
            var parsedAllyKills: Int? = null
            var parsedEnemyKills: Int? = null
            var parsedGoldDiff: Int? = null

            val timeMatcher = timePattern.matcher(ocrText)
            if (timeMatcher.find()) {
                val min = timeMatcher.group(1)?.toIntOrNull() ?: 0
                val sec = timeMatcher.group(2)?.toIntOrNull() ?: 0
                parsedTimeSec = (min * 60) + sec
            }

            val scoreMatcher = scorePattern.matcher(ocrText)
            if (scoreMatcher.find()) {
                parsedAllyKills = scoreMatcher.group(1)?.toIntOrNull()
                parsedEnemyKills = scoreMatcher.group(2)?.toIntOrNull()
            }

            val goldMatcher = goldPattern.matcher(ocrText)
            if (goldMatcher.find()) {
                val allyG = parseGoldValue(goldMatcher.group(1))
                val enemyG = parseGoldValue(goldMatcher.group(2))
                if (allyG != null && enemyG != null) {
                    parsedGoldDiff = allyG - enemyG
                }
            }

            VisionScanResult(
                detectedMode = screenMode,
                detectedTimeSeconds = parsedTimeSec,
                detectedGoldDiff = parsedGoldDiff,
                detectedAllyKills = parsedAllyKills,
                detectedEnemyKills = parsedEnemyKills,
                rawOcrSummary = ocrText.take(120).replace("\n", " "),
                confidence = 0.90f
            )
        } catch (e: Exception) {
            Log.e("VisionEngine", "Frame analysis failed", e)
            VisionScanResult(
                detectedMode = DetectedScreenMode.IDLE,
                rawOcrSummary = "Vision fallback mode: active"
            )
        }
    }

    private fun detectScreenModeFast(bitmap: Bitmap): DetectedScreenMode {
        if (bitmap.isRecycled) return DetectedScreenMode.IDLE
        val width = bitmap.width
        val height = bitmap.height

        var darkCenterPixels = 0
        var brightSkillPixels = 0
        val samplePoints = 20

        for (i in 0 until samplePoints) {
            val cx = (width * 0.3 + (i % 5) * (width * 0.1)).toInt().coerceIn(0, width - 1)
            val cy = (height * 0.3 + (i / 5) * (height * 0.1)).toInt().coerceIn(0, height - 1)
            val pixel = bitmap.getPixel(cx, cy)
            val r = Color.red(pixel)
            val g = Color.green(pixel)
            val b = Color.blue(pixel)
            val brightness = (r + g + b) / 3

            if (brightness < 40) darkCenterPixels++
            if (brightness > 210) brightSkillPixels++
        }

        return when {
            darkCenterPixels > 14 -> DetectedScreenMode.SCOREBOARD_OPEN
            brightSkillPixels > 8 -> DetectedScreenMode.COMBAT
            else -> DetectedScreenMode.IDLE
        }
    }

    private fun cropHeaderRegion(bitmap: Bitmap): Bitmap {
        val w = bitmap.width
        val h = bitmap.height
        val topH = (h * 0.18f).toInt().coerceIn(1, h)
        return Bitmap.createBitmap(bitmap, 0, 0, w, topH)
    }

    private suspend fun recognizeText(bitmap: Bitmap): String =
        suspendCancellableCoroutine { continuation ->
            if (bitmap.isRecycled) {
                continuation.resume("")
                return@suspendCancellableCoroutine
            }
            val inputImage = InputImage.fromBitmap(bitmap, 0)
            textRecognizer.process(inputImage)
                .addOnSuccessListener { visionText ->
                    continuation.resume(visionText.text)
                }
                .addOnFailureListener { e ->
                    Log.w("VisionEngine", "OCR textRecognizer error: ${e.message}")
                    continuation.resume("")
                }
        }

    private fun parseGoldValue(str: String?): Int? {
        if (str == null) return null
        val clean = str.trim().lowercase()
        return if (clean.endsWith("k")) {
            val num = clean.removeSuffix("k").toFloatOrNull() ?: return null
            (num * 1000).toInt()
        } else {
            clean.toIntOrNull()
        }
    }

    override fun close() {
        try {
            textRecognizer.close()
        } catch (_: Exception) {}
    }
}
