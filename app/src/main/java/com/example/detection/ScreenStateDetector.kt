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

        // Timer regex matching (e.g., 00:24, 0:24, 00.24, 00 24, 15:30)
        val hasTimerPattern = ocrText.contains(Regex("(\\d{1,2})\\s*[:.bB1lI-]\\s*(\\d{2})")) ||
                ocrText.contains(Regex("\\d{1,2}:\\d{2}"))

        val hasHeroSelectionText = ocrText.contains("Chọn tướng", ignoreCase = true) ||
                ocrText.contains("Khóa", ignoreCase = true) ||
                ocrText.contains("Cấm", ignoreCase = true) ||
                ocrText.contains("Ban", ignoreCase = true) ||
                ocrText.contains("Pick", ignoreCase = true) ||
                ocrText.contains("Đội Xanh", ignoreCase = true) ||
                ocrText.contains("Đội Đỏ", ignoreCase = true) ||
                ocrText.contains("Phép bổ trợ", ignoreCase = true) ||
                ocrText.contains("Ngọc bổ trợ", ignoreCase = true)

        val hasInGameKeywords = ocrText.contains("vs", ignoreCase = true) ||
                ocrText.contains("k", ignoreCase = true) ||
                ocrText.contains("Vàng", ignoreCase = true) ||
                ocrText.contains("Caesar", ignoreCase = true) ||
                ocrText.contains("Rồng", ignoreCase = true) ||
                ocrText.contains("Trụ", ignoreCase = true) ||
                ocrText.contains("Chiến", ignoreCase = true) ||
                ocrText.contains("Biến về", ignoreCase = true) ||
                ocrText.contains("Hồi máu", ignoreCase = true) ||
                ocrText.contains("Trừng trị", ignoreCase = true) ||
                ocrText.contains("Tốc biến", ignoreCase = true) ||
                ocrText.contains("Bộc phá", ignoreCase = true) ||
                ocrText.contains("ms", ignoreCase = true) ||
                ocrText.contains("fps", ignoreCase = true) ||
                ocrText.contains("UID", ignoreCase = true)

        if (hasHeroSelectionText && !hasTimerPattern && !hasInGameKeywords) {
            return ScreenState.HERO_SELECTION
        }

        val hasStrictLobbyText = ocrText.contains("Bắt Đầu Tìm Trận", ignoreCase = true) ||
                ocrText.contains("Mời Bè Bạn", ignoreCase = true) ||
                ocrText.contains("Sảnh Chờ", ignoreCase = true) ||
                ocrText.contains("Gia Nhập Phòng", ignoreCase = true) ||
                ocrText.contains("Tìm Trận", ignoreCase = true)

        if (hasStrictLobbyText && !hasTimerPattern && !hasInGameKeywords && !hasHeroSelectionText) {
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
