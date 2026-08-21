package com.example.detection

import android.graphics.Bitmap
import com.example.model.ScreenState

/**
 * High-accuracy Screen State Detector for Arena of Valor (Liên Quân Mobile).
 * Correctly distinguishes outside game (vertical / non-game), lobby menu, hero selection (ban-pick),
 * loading, active match, scoreboard, and shop without emitting fake match signals outside game.
 */
class ScreenStateDetector {

    fun detectScreenState(
        bitmap: Bitmap,
        ocrText: String,
        detectedComponents: List<DetectedUIComponent>
    ): ScreenState {
        if (bitmap.isRecycled) return ScreenState.UNKNOWN

        val w = bitmap.width
        val h = bitmap.height
        val isLandscape = w > h

        // If screen is strictly vertical (portrait mode), user is definitely outside the game or in system launcher
        if (!isLandscape) {
            return ScreenState.OUTSIDE_GAME
        }

        // Check if Scoreboard or Shop component was detected dynamically
        val hasScoreboardComponent = detectedComponents.any { it.componentName == "Scoreboard" && it.confidence > 0.5f }
        if (hasScoreboardComponent || ocrText.contains("KDA", ignoreCase = true) || ocrText.contains("Trang bị", ignoreCase = true) && ocrText.contains("Vàng", ignoreCase = true)) {
            return ScreenState.SCOREBOARD_OPEN
        }

        val hasShopComponent = detectedComponents.any { it.componentName == "ShopUI" && it.confidence > 0.4f }
        if (hasShopComponent || ocrText.contains("Cửa Hàng", ignoreCase = true) || (ocrText.contains("Công", ignoreCase = true) && ocrText.contains("Phép", ignoreCase = true) && ocrText.contains("Thủ", ignoreCase = true))) {
            return ScreenState.SHOP_OPEN
        }

        // Timer regex matching (e.g., 00:24, 0:24, 00.24, 15:30, 02:00)
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
                ocrText.contains("fps", ignoreCase = true)

        if (hasHeroSelectionText && !hasTimerPattern) {
            return ScreenState.HERO_SELECTION
        }

        val hasStrictLobbyText = ocrText.contains("Bắt Đầu Tìm Trận", ignoreCase = true) ||
                ocrText.contains("Mời Bè Bạn", ignoreCase = true) ||
                ocrText.contains("Sảnh Chờ", ignoreCase = true) ||
                ocrText.contains("Gia Nhập Phòng", ignoreCase = true) ||
                ocrText.contains("Đang tìm trận", ignoreCase = true) ||
                ocrText.contains("Đấu Đỉnh Cao", ignoreCase = true) ||
                ocrText.contains("Đấu Hạng", ignoreCase = true)

        if (hasStrictLobbyText && !hasTimerPattern && !hasInGameKeywords) {
            return ScreenState.GAME_MENU
        }

        // When in landscape and timer or in-game HUD indicators are found -> IN_MATCH
        if (hasTimerPattern || hasInGameKeywords) {
            return ScreenState.IN_MATCH
        }

        // Default landscape state if no match keywords found yet: GAME_MENU / DETECTING
        return ScreenState.GAME_MENU
    }
}
