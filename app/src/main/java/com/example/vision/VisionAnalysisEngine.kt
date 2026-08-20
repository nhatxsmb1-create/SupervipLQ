package com.example.vision

import android.graphics.Bitmap
import android.graphics.RectF
import android.util.Log
import com.example.autocapture.FrameChangeDetector
import com.example.detection.DynamicROIExtractor
import com.example.detection.DynamicUIDetector
import com.example.detection.ScreenStateDetector
import com.example.model.DetectedScreenMode
import com.example.model.GameState
import com.example.model.ScreenState
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
    val confidence: Float = 0.85f,
    val gameState: GameState? = null
)

interface IVisionEngine {
    suspend fun analyzeFrame(bitmap: Bitmap): VisionScanResult
    suspend fun analyzeFrameToGameState(
        bitmap: Bitmap,
        captureIntervalMs: Long = 1000L
    ): GameState
    fun close()
}

class VisionAnalysisEngine : IVisionEngine {

    private val textRecognizer by lazy {
        TextRecognition.getClient(TextRecognizerOptions.DEFAULT_OPTIONS)
    }

    private val frameChangeDetector = FrameChangeDetector()
    private val dynamicUIDetector = DynamicUIDetector()
    private val dynamicROIExtractor = DynamicROIExtractor()
    private val screenStateDetector = ScreenStateDetector()

    private val timePattern = Pattern.compile("(\\d{1,2})[:.](\\d{2})")
    private val scorePattern = Pattern.compile("(\\d{1,2})\\s*[-:]\\s*(\\d{1,2})")
    private val goldPattern = Pattern.compile("([\\d.]+)[kK]?\\s*vs\\s*([\\d.]+)[kK]?", Pattern.CASE_INSENSITIVE)

    private var cachedGameState: GameState = GameState()

    private var lastFullAnalysisTimeMs: Long = 0L

    override suspend fun analyzeFrameToGameState(
        bitmap: Bitmap,
        captureIntervalMs: Long
    ): GameState = withContext(Dispatchers.Default) {
        val startTime = System.currentTimeMillis()

        if (bitmap.isRecycled) {
            return@withContext GameState(screenState = ScreenState.OUTSIDE_GAME)
        }

        // 1. Frame Change Detection
        val changeResult = frameChangeDetector.checkFrameChange(bitmap)
        val timeSinceLastFull = startTime - lastFullAnalysisTimeMs
        val shouldForceAnalysis = timeSinceLastFull > 3000L || cachedGameState.screenState == ScreenState.OUTSIDE_GAME

        if (!changeResult.hasChanged && !shouldForceAnalysis) {
            // Screen has not changed significantly, return cached state with updated stats
            return@withContext cachedGameState.copy(
                frameProcessingTimeMs = System.currentTimeMillis() - startTime,
                captureIntervalMs = captureIntervalMs,
                frameChangedPercent = changeResult.differencePercent
            )
        }

        lastFullAnalysisTimeMs = startTime

        try {
            // 2. Dynamic UI Component Detection
            val detectedComponents = dynamicUIDetector.detectUIComponents(bitmap)

            // 3. Multi-ROI Targeted Crop for High-Speed High-Accuracy OCR
            val ocrTextBuilder = StringBuilder()

            // ROI A: Top Right Header (Timer 00:45, KDA 1 vs 0, Ping)
            var timerCrop: Bitmap? = null
            try {
                timerCrop = dynamicROIExtractor.cropNormalizedRegion(bitmap, RectF(0.65f, 0.00f, 0.92f, 0.20f))
                if (timerCrop != null && !timerCrop.isRecycled) {
                    val t = recognizeText(timerCrop)
                    if (t.isNotBlank()) ocrTextBuilder.append(t).append("\n")
                }
            } catch (_: Exception) {
            } finally {
                timerCrop?.recycle()
            }

            // ROI B: Bottom Spell Buttons (Biến về, Hồi máu, Tốc biến, Trừng trị)
            var spellsCrop: Bitmap? = null
            try {
                spellsCrop = dynamicROIExtractor.cropNormalizedRegion(bitmap, RectF(0.35f, 0.75f, 0.65f, 1.00f))
                if (spellsCrop != null && !spellsCrop.isRecycled) {
                    val t = recognizeText(spellsCrop)
                    if (t.isNotBlank()) ocrTextBuilder.append(t).append("\n")
                }
            } catch (_: Exception) {
            } finally {
                spellsCrop?.recycle()
            }

            // ROI C: Left Shop & Gold (Gold 39051)
            var goldCrop: Bitmap? = null
            try {
                goldCrop = dynamicROIExtractor.cropNormalizedRegion(bitmap, RectF(0.00f, 0.25f, 0.22f, 0.55f))
                if (goldCrop != null && !goldCrop.isRecycled) {
                    val t = recognizeText(goldCrop)
                    if (t.isNotBlank()) ocrTextBuilder.append(t).append("\n")
                }
            } catch (_: Exception) {
            } finally {
                goldCrop?.recycle()
            }

            var ocrText = ocrTextBuilder.toString().trim()

            // Fallback: If ROIs missed timer digits, perform OCR on full frame
            if (ocrText.isBlank() || !ocrText.contains(Regex("\\d"))) {
                val fullText = recognizeText(bitmap)
                if (fullText.isNotBlank()) {
                    ocrText = if (ocrText.isBlank()) fullText else "$ocrText\n$fullText"
                }
            }

            // 4. Screen State Classification
            val screenState = screenStateDetector.detectScreenState(bitmap, ocrText, detectedComponents)

            // 5. OCR parsing
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

            val isMatchActive = screenState in listOf(
                ScreenState.IN_MATCH,
                ScreenState.SCOREBOARD_OPEN,
                ScreenState.SHOP_OPEN,
                ScreenState.COMBAT
            )

            // Dynamic time calculation: sync with OCR if parsed, or tick continuously when active
            val cachedTime = cachedGameState.matchTimeSeconds ?: 0
            val finalTimeSec = when {
                parsedTimeSec != null -> parsedTimeSec
                isMatchActive && cachedTime > 0 -> {
                    val elapsedSec = (captureIntervalMs / 1000L).toInt().coerceAtLeast(1)
                    cachedTime + elapsedSec
                }
                isMatchActive -> 1
                else -> 0
            }

            val confidence = when {
                parsedTimeSec != null -> 0.95f
                isMatchActive -> 0.85f
                screenState == ScreenState.HERO_SELECTION -> 0.90f
                screenState == ScreenState.GAME_MENU -> 0.80f
                else -> 0.50f
            }

            val processingTime = System.currentTimeMillis() - startTime

            val newGameState = GameState(
                matchActive = isMatchActive,
                screenState = screenState,
                matchTimeSeconds = finalTimeSec,
                allyKills = parsedAllyKills ?: cachedGameState.allyKills,
                enemyKills = parsedEnemyKills ?: cachedGameState.enemyKills,
                goldDifference = parsedGoldDiff ?: cachedGameState.goldDifference,
                scoreboardOpen = (screenState == ScreenState.SCOREBOARD_OPEN),
                shopOpen = (screenState == ScreenState.SHOP_OPEN),
                overallConfidence = confidence,
                frameProcessingTimeMs = processingTime,
                captureIntervalMs = captureIntervalMs,
                frameChangedPercent = changeResult.differencePercent,
                detectedComponents = detectedComponents,
                rawOcrSummary = ocrText.take(150).replace("\n", " ")
            )

            cachedGameState = newGameState
            newGameState
        } catch (e: Exception) {
            Log.e("VisionEngine", "GameState analysis failed", e)
            cachedGameState
        }
    }

    override suspend fun analyzeFrame(bitmap: Bitmap): VisionScanResult {
        val gs = analyzeFrameToGameState(bitmap)
        val mode = when (gs.screenState) {
            ScreenState.SCOREBOARD_OPEN -> DetectedScreenMode.SCOREBOARD_OPEN
            ScreenState.SHOP_OPEN -> DetectedScreenMode.SHOP_OPEN
            ScreenState.COMBAT -> DetectedScreenMode.COMBAT
            ScreenState.IN_MATCH -> DetectedScreenMode.IDLE
            else -> DetectedScreenMode.IDLE
        }
        return VisionScanResult(
            detectedMode = mode,
            detectedTimeSeconds = gs.matchTimeSeconds,
            detectedGoldDiff = gs.goldDifference,
            detectedAllyKills = gs.allyKills,
            detectedEnemyKills = gs.enemyKills,
            rawOcrSummary = gs.rawOcrSummary,
            confidence = gs.overallConfidence,
            gameState = gs
        )
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
