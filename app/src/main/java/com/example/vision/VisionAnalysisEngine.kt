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
import com.example.model.HeroScoreboardEntry
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

    private val scorePattern = Pattern.compile("(\\d{1,2})\\s*(?:vs|v|VS|Vs|[-:])\\s*(\\d{1,2})")
    private val goldVsPattern = Pattern.compile("([\\d.]+)[kK]?\\s*vs\\s*([\\d.]+)[kK]?", Pattern.CASE_INSENSITIVE)
    private val goldNumberPattern = Pattern.compile("(\\d{3,5})")

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
        val shouldForceAnalysis = timeSinceLastFull > 800L || cachedGameState.screenState == ScreenState.OUTSIDE_GAME

        if (!changeResult.hasChanged && !shouldForceAnalysis) {
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

            val ocrTextBuilder = StringBuilder()

            // Targeted ROI 1: Dedicated Clock & Ping Area (Timer 00:48, Ping 24ms, KDA)
            var timerCrop: Bitmap? = null
            var enhancedTimerCrop: Bitmap? = null
            try {
                // In landscape Liên Quân Mobile, clock is situated between 0.65f and 0.95f X, 0.00f and 0.16f Y
                timerCrop = dynamicROIExtractor.cropNormalizedRegion(bitmap, RectF(0.65f, 0.00f, 0.95f, 0.16f))
                if (timerCrop != null && !timerCrop.isRecycled) {
                    val rawText = recognizeText(timerCrop)
                    if (rawText.isNotBlank()) ocrTextBuilder.append(rawText).append("\n")

                    // High contrast 2x upscaled version for digital clock
                    enhancedTimerCrop = dynamicROIExtractor.createEnhancedOcrBitmap(timerCrop)
                    if (enhancedTimerCrop != null && !enhancedTimerCrop.isRecycled) {
                        val enhancedText = recognizeText(enhancedTimerCrop)
                        if (enhancedText.isNotBlank()) ocrTextBuilder.append(enhancedText).append("\n")
                    }
                }
            } catch (_: Exception) {
            } finally {
                timerCrop?.recycle()
                enhancedTimerCrop?.recycle()
            }

            // Targeted ROI 2: Broader Top Header (Score 1 vs 0, Gold, Timer)
            var topHeaderCrop: Bitmap? = null
            try {
                topHeaderCrop = dynamicROIExtractor.cropNormalizedRegion(bitmap, RectF(0.40f, 0.00f, 0.98f, 0.20f))
                if (topHeaderCrop != null && !topHeaderCrop.isRecycled) {
                    val t = recognizeText(topHeaderCrop)
                    if (t.isNotBlank()) ocrTextBuilder.append(t).append("\n")
                }
            } catch (_: Exception) {
            } finally {
                topHeaderCrop?.recycle()
            }

            // Targeted ROI 3: Bottom Spells (Biến về, Hồi máu, Tốc biến, Trừng trị)
            var spellsCrop: Bitmap? = null
            try {
                spellsCrop = dynamicROIExtractor.cropNormalizedRegion(bitmap, RectF(0.30f, 0.70f, 0.70f, 1.00f))
                if (spellsCrop != null && !spellsCrop.isRecycled) {
                    val t = recognizeText(spellsCrop)
                    if (t.isNotBlank()) ocrTextBuilder.append(t).append("\n")
                }
            } catch (_: Exception) {
            } finally {
                spellsCrop?.recycle()
            }

            // Targeted ROI 4: Left Gold Shop button (e.g. 9135)
            var goldCrop: Bitmap? = null
            try {
                goldCrop = dynamicROIExtractor.cropNormalizedRegion(bitmap, RectF(0.02f, 0.25f, 0.18f, 0.60f))
                if (goldCrop != null && !goldCrop.isRecycled) {
                    val t = recognizeText(goldCrop)
                    if (t.isNotBlank()) ocrTextBuilder.append(t).append("\n")
                }
            } catch (_: Exception) {
            } finally {
                goldCrop?.recycle()
            }

            // Full-frame OCR fallback if targeted didn't find sufficient cues
            var fullText = ""
            if (ocrTextBuilder.length < 15) {
                fullText = recognizeText(bitmap)
                if (fullText.isNotBlank()) {
                    ocrTextBuilder.append(fullText).append("\n")
                }
            }

            val ocrText = ocrTextBuilder.toString().trim()

            // 4. Screen State Classification
            val screenState = screenStateDetector.detectScreenState(bitmap, ocrText, detectedComponents)

            // Specialized deep parsing for Scoreboard & Shop
            var parsedShopGold: Int? = null
            var allyRoster = mutableListOf<HeroScoreboardEntry>()
            var enemyRoster = mutableListOf<HeroScoreboardEntry>()
            var allyTotalGold: Int? = null
            var enemyTotalGold: Int? = null
            var allyTowers: Int? = null
            var enemyTowers: Int? = null

            if (screenState == ScreenState.SHOP_OPEN) {
                var shopGoldCrop: Bitmap? = null
                try {
                    shopGoldCrop = dynamicROIExtractor.cropNormalizedRegion(bitmap, RectF(0.18f, 0.01f, 0.35f, 0.12f))
                    if (shopGoldCrop != null && !shopGoldCrop.isRecycled) {
                        val goldOcr = recognizeText(shopGoldCrop)
                        parsedShopGold = goldNumberPattern.matcher(goldOcr).let { if (it.find()) it.group(1)?.toIntOrNull() else null }
                    }
                } catch (_: Exception) {
                } finally {
                    shopGoldCrop?.recycle()
                }
            } else if (screenState == ScreenState.SCOREBOARD_OPEN) {
                var leftTeamCrop: Bitmap? = null
                var rightTeamCrop: Bitmap? = null
                var headerStatCrop: Bitmap? = null
                try {
                    leftTeamCrop = dynamicROIExtractor.cropNormalizedRegion(bitmap, RectF(0.08f, 0.20f, 0.49f, 0.90f))
                    if (leftTeamCrop != null && !leftTeamCrop.isRecycled) {
                        val leftText = recognizeText(leftTeamCrop)
                        ocrTextBuilder.append("\n[TEAM_ALLY]\n").append(leftText)
                        allyRoster = parseTeamRoster(leftText, isAlly = true)
                    }

                    rightTeamCrop = dynamicROIExtractor.cropNormalizedRegion(bitmap, RectF(0.51f, 0.20f, 0.95f, 0.90f))
                    if (rightTeamCrop != null && !rightTeamCrop.isRecycled) {
                        val rightText = recognizeText(rightTeamCrop)
                        ocrTextBuilder.append("\n[TEAM_ENEMY]\n").append(rightText)
                        enemyRoster = parseTeamRoster(rightText, isAlly = false)
                    }

                    headerStatCrop = dynamicROIExtractor.cropNormalizedRegion(bitmap, RectF(0.10f, 0.10f, 0.90f, 0.25f))
                    if (headerStatCrop != null && !headerStatCrop.isRecycled) {
                        val headerText = recognizeText(headerStatCrop)
                        ocrTextBuilder.append("\n[HEADER_STATS]\n").append(headerText)
                        val numbers = goldNumberPattern.matcher(headerText)
                        val golds = mutableListOf<Int>()
                        while (numbers.find()) {
                            numbers.group(1)?.toIntOrNull()?.let { if (it > 1000) golds.add(it) }
                        }
                        if (golds.size >= 2) {
                            allyTotalGold = golds[0]
                            enemyTotalGold = golds[1]
                        }
                    }
                } catch (_: Exception) {
                } finally {
                    leftTeamCrop?.recycle()
                    rightTeamCrop?.recycle()
                    headerStatCrop?.recycle()
                }
            }

            // 5. General OCR parsing
            val finalOcrText = ocrTextBuilder.toString().trim()
            val parsedTimeSec: Int? = parseAoVTime(finalOcrText)
            var parsedAllyKills: Int? = null
            var parsedEnemyKills: Int? = null
            var parsedGoldDiff: Int? = null

            val scoreMatcher = scorePattern.matcher(finalOcrText)
            if (scoreMatcher.find()) {
                parsedAllyKills = scoreMatcher.group(1)?.toIntOrNull()
                parsedEnemyKills = scoreMatcher.group(2)?.toIntOrNull()
            }

            if (allyTotalGold != null && enemyTotalGold != null) {
                parsedGoldDiff = allyTotalGold - enemyTotalGold
            } else {
                val goldVsMatcher = goldVsPattern.matcher(finalOcrText)
                if (goldVsMatcher.find()) {
                    val allyG = parseGoldValue(goldVsMatcher.group(1))
                    val enemyG = parseGoldValue(goldVsMatcher.group(2))
                    if (allyG != null && enemyG != null) {
                        parsedGoldDiff = allyG - enemyG
                    }
                }
            }

            val isMatchActive = screenState in listOf(
                ScreenState.IN_MATCH,
                ScreenState.SCOREBOARD_OPEN,
                ScreenState.SHOP_OPEN,
                ScreenState.COMBAT
            )

            // Dynamic continuous time sync
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
                screenState == ScreenState.SCOREBOARD_OPEN -> 0.99f
                screenState == ScreenState.SHOP_OPEN -> 0.99f
                parsedTimeSec != null -> 0.98f
                isMatchActive -> 0.90f
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
                currentShopGold = parsedShopGold ?: cachedGameState.currentShopGold,
                allyTotalGold = allyTotalGold ?: cachedGameState.allyTotalGold,
                enemyTotalGold = enemyTotalGold ?: cachedGameState.enemyTotalGold,
                allyRoster = if (allyRoster.isNotEmpty()) allyRoster.toList() else cachedGameState.allyRoster,
                enemyRoster = if (enemyRoster.isNotEmpty()) enemyRoster.toList() else cachedGameState.enemyRoster,
                scoreboardOpen = (screenState == ScreenState.SCOREBOARD_OPEN),
                shopOpen = (screenState == ScreenState.SHOP_OPEN),
                overallConfidence = confidence,
                frameProcessingTimeMs = processingTime,
                captureIntervalMs = captureIntervalMs,
                frameChangedPercent = changeResult.differencePercent,
                detectedComponents = detectedComponents,
                rawOcrSummary = finalOcrText.take(250).replace("\n", " ")
            )

            cachedGameState = newGameState
            newGameState
        } catch (e: Exception) {
            Log.e("VisionEngine", "GameState analysis failed", e)
            cachedGameState
        }
    }

    private fun parseAoVTime(text: String): Int? {
        if (text.isBlank()) return null
        val lines = text.lines()
        for (rawLine in lines) {
            val line = rawLine.trim()
            if (line.isEmpty()) continue

            // 1. Remove ping e.g. "24ms", "24 ms", "60fps"
            val lineCleaned = line
                .replace(Regex("""\b\d{1,3}\s*(ms|fps)\b""", RegexOption.IGNORE_CASE), " ")
                .replace(Regex("""\b(ms|fps)\b""", RegexOption.IGNORE_CASE), " ")

            // 2. Normalize common OCR letter mistakes for digital LCD clocks
            val normalized = lineCleaned
                .replace("O", "0")
                .replace("o", "0")
                .replace("D", "0")
                .replace("Q", "0")

            // Priority 1: Clear mm:ss pattern with explicit separator (:, ., -, ', ;, |, /)
            // Example: 00:48, 0:48, 01:30, 15:42, 00.48, 00-48, 00'48, 00;48
            val explicitTimerRegex = Regex("""\b([0-5]?[0-9])\s*[:.;'\-_|/]\s*([0-5][0-9])\b""")
            val explicitMatches = explicitTimerRegex.findAll(normalized)
            for (m in explicitMatches) {
                val min = m.groupValues[1].toIntOrNull()
                val sec = m.groupValues[2].toIntOrNull()
                if (min != null && sec != null && min in 0..59 && sec in 0..59) {
                    val total = min * 60 + sec
                    if (total in 0..3600) {
                        return total
                    }
                }
            }

            // Priority 2: Also handle digit substitutions like B -> 8, S -> 5
            val digitNormalized = normalized
                .replace("B", "8")
                .replace("S", "5")
                .replace("s", "5")
                .replace("l", "1")
                .replace("I", "1")
                .replace("Z", "2")

            val fallbackMatches = explicitTimerRegex.findAll(digitNormalized)
            for (m in fallbackMatches) {
                val min = m.groupValues[1].toIntOrNull()
                val sec = m.groupValues[2].toIntOrNull()
                if (min != null && sec != null && min in 0..59 && sec in 0..59) {
                    val total = min * 60 + sec
                    if (total in 0..3600) {
                        return total
                    }
                }
            }
        }
        return null
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

    private fun parseTeamRoster(text: String, isAlly: Boolean): MutableList<HeroScoreboardEntry> {
        val list = mutableListOf<HeroScoreboardEntry>()
        val lines = text.lines()
        var currentHero = ""
        var currentKda = ""
        var currentGold = 0

        for (rawLine in lines) {
            val line = rawLine.trim()
            if (line.isEmpty()) continue

            // Check if line contains hero names
            val knownHeroes = listOf(
                "Nakroth", "Valhein", "Natalya", "Krixi", "Raz", "Florentino", "Flowborn", "Zuka", "Arum", "Arthur",
                "Murad", "Tulen", "Hayate", "Capheny", "Violet", "Elsu", "Aoi", "Richter", "Yorn", "Tel'Annas",
                "Maloch", "Thane", "Baldum", "Toro", "Mina", "Veera", "Liliana", "Lauriel", "Dirak", "Yue",
                "Keera", "Yan", "Bijan", "Stuart", "Helen", "Aya", "Zip", "Rouie", "TeeMee", "Gildur", "Omen",
                "Qi", "Lubu", "Kil'Groth", "Astrid", "Ryoma", "Skud", "Taara", "Allain", "Laville", "Iggy"
            )

            for (h in knownHeroes) {
                if (line.contains(h, ignoreCase = true)) {
                    if (currentHero.isNotEmpty()) {
                        list.add(HeroScoreboardEntry(currentHero, "", currentKda, currentGold, 1, isAlly))
                    }
                    currentHero = if (h.equals("Flowborn", ignoreCase = true)) "Florentino" else h
                    currentKda = ""
                    currentGold = 0
                    break
                }
            }

            // Check KDA pattern e.g. 12 / 3 / 4 or 9 / 3 / 5 or 12/3/4
            val kdaRegex = Regex("""(\d{1,2})\s*[/I|]\s*(\d{1,2})\s*[/I|]\s*(\d{1,2})""")
            val kdaMatch = kdaRegex.find(line)
            if (kdaMatch != null) {
                currentKda = "${kdaMatch.groupValues[1]} / ${kdaMatch.groupValues[2]} / ${kdaMatch.groupValues[3]}"
            }

            // Check gold pattern e.g. 10020, 8218, 6996, 5515
            val goldRegex = Regex("""\b(\d{4,5})\b""")
            val goldMatch = goldRegex.find(line)
            if (goldMatch != null) {
                val g = goldMatch.groupValues[1].toIntOrNull() ?: 0
                if (g in 1500..35000) {
                    currentGold = g
                }
            }
        }

        if (currentHero.isNotEmpty()) {
            list.add(HeroScoreboardEntry(currentHero, "", currentKda, currentGold, 1, isAlly))
        }
        return list
    }

    override fun close() {
        try {
            textRecognizer.close()
        } catch (_: Exception) {}
    }
}
