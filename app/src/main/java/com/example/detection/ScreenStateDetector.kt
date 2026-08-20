package com.example.detection

import android.graphics.Bitmap
import android.graphics.Color
import com.example.model.ScreenState

/**
 * Screen State Detector that inspects visible screen information dynamically to classify screen mode.
 * Zero user intervention required.
 */
class ScreenStateDetector {

    fun detectScreenState(
        bitmap: Bitmap,
        ocrText: String,
        detectedComponents: List<DetectedUIComponent>
    ): ScreenState {
        if (bitmap.isRecycled) return ScreenState.UNKNOWN

        // Check if Scoreboard or Shop component was detected dynamically
        val hasScoreboardComponent = detectedComponents.any { it.componentName == "Scoreboard" && it.confidence > 0.5f }
        if (hasScoreboardComponent) {
            return ScreenState.SCOREBOARD_OPEN
        }

        val hasShopComponent = detectedComponents.any { it.componentName == "ShopUI" && it.confidence > 0.4f }
        if (hasShopComponent) {
            return ScreenState.SHOP_OPEN
        }

        // Check OCR text hints for game screen or match timer pattern
        val hasTimerPattern = ocrText.contains(Regex("(\\d{1,2})[:.](\\d{2})"))
        val hasInGameKeywords = ocrText.contains("vs", ignoreCase = true) ||
                ocrText.contains("k", ignoreCase = true) ||
                ocrText.contains("Vàng", ignoreCase = true) ||
                ocrText.contains("Caesar", ignoreCase = true) ||
                ocrText.contains("Rồng", ignoreCase = true) ||
                ocrText.contains("Trụ", ignoreCase = true) ||
                ocrText.contains("Chiến", ignoreCase = true)

        val hasLobbyText = ocrText.contains("Đấu Luyện", ignoreCase = true) ||
                ocrText.contains("Đấu Hạng", ignoreCase = true) ||
                ocrText.contains("Bắt Đầu", ignoreCase = true) ||
                ocrText.contains("Sảnh", ignoreCase = true) ||
                ocrText.contains("Lobby", ignoreCase = true)

        if (hasLobbyText && !hasTimerPattern && !hasInGameKeywords) {
            return ScreenState.GAME_MENU
        }

        val w = bitmap.width
        val h = bitmap.height

        // Check color distribution for dark loading screen or active match
        var darkPixels = 0
        var brightSkillPixels = 0
        var nonBlackPixels = 0
        val samples = 25

        for (i in 0 until samples) {
            val cx = (w * (0.15f + (i % 5) * 0.17f)).toInt().coerceIn(0, w - 1)
            val cy = (h * (0.15f + (i / 5) * 0.15f)).toInt().coerceIn(0, h - 1)
            val pixel = bitmap.getPixel(cx, cy)
            val luma = (Color.red(pixel) + Color.green(pixel) + Color.blue(pixel)) / 3

            if (luma < 25) darkPixels++
            if (luma > 200) brightSkillPixels++
            if (luma > 10) nonBlackPixels++
        }

        if (brightSkillPixels > 6) {
            return ScreenState.COMBAT
        }

        if (hasTimerPattern || hasInGameKeywords || darkPixels < 22) {
            return ScreenState.IN_MATCH
        }

        if (darkPixels >= 23 && nonBlackPixels < 5) {
            return ScreenState.LOADING
        }

        return if (nonBlackPixels > 10) ScreenState.IN_MATCH else ScreenState.OUTSIDE_GAME
    }
}
