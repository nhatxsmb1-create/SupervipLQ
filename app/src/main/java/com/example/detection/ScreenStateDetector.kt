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
        val hasScoreboardKeywords = ocrText.contains("Thông số tướng", ignoreCase = true) ||
                ocrText.contains("Thuộc tính tướng", ignoreCase = true) ||
                ocrText.contains("View Defeat Recap", ignoreCase = true) ||
                ocrText.contains("Defeat Recap", ignoreCase = true) ||
                ocrText.contains("Tố cáo chatvoice", ignoreCase = true) ||
                ocrText.contains("chatvoice", ignoreCase = true) ||
                ocrText.contains("KDA", ignoreCase = true) ||
                (ocrText.contains("Xếp", ignoreCase = true) && ocrText.contains("Mặc", ignoreCase = true))

        val hasScoreboardComponent = detectedComponents.any { it.componentName == "Scoreboard" && it.confidence > 0.4f }
        if (hasScoreboardComponent || hasScoreboardKeywords) {
            return ScreenState.SCOREBOARD_OPEN
        }

        val hasShopKeywords = ocrText.contains("Shop", ignoreCase = true) ||
                ocrText.contains("Đề cử", ignoreCase = true) ||
                ocrText.contains("Giữ mạng", ignoreCase = true) ||
                ocrText.contains("Tên bộ trang", ignoreCase = true) ||
                ocrText.contains("trang bị tiến cử", ignoreCase = true) ||
                ocrText.contains("Cửa Hàng", ignoreCase = true) ||
                (ocrText.contains("Công", ignoreCase = true) && ocrText.contains("Phép", ignoreCase = true)) ||
                (ocrText.contains("Thủ", ignoreCase = true) && ocrText.contains("Tốc chạy", ignoreCase = true))

        val hasShopComponent = detectedComponents.any { it.componentName == "ShopUI" && it.confidence > 0.4f }
        if (hasShopComponent || hasShopKeywords) {
            return ScreenState.SHOP_OPEN
        }

        // Timer regex matching (e.g., 00:48, 00:24, 0:24, 00.24, 00 48, 15:30, 02:00)
        val hasTimerPattern = ocrText.contains(Regex("""\b([0-5]?[0-9])\s*[:.\s;,|I!]\s*([0-5][0-9])\b""")) ||
                ocrText.contains(Regex("""\d{1,2}:\d{2}"""))

        val hasScorePattern = ocrText.contains(Regex("""\d{1,2}\s*(?:vs|v|VS|Vs|[-:])\s*\d{1,2}"""))

        val hasInGameSpells = ocrText.contains("Biến về", ignoreCase = true) ||
                ocrText.contains("Hồi máu", ignoreCase = true) ||
                ocrText.contains("Tốc biến", ignoreCase = true) ||
                ocrText.contains("Trừng trị", ignoreCase = true) ||
                ocrText.contains("Bộc phá", ignoreCase = true) ||
                ocrText.contains("Tốc hành", ignoreCase = true) ||
                ocrText.contains("Gầm thét", ignoreCase = true) ||
                ocrText.contains("Cấp cứu", ignoreCase = true) ||
                ocrText.contains("Thanh tẩy", ignoreCase = true)

        val hasPingOrFps = ocrText.contains(Regex("""\b\d{1,3}\s*ms\b""", RegexOption.IGNORE_CASE)) ||
                ocrText.contains(Regex("""\b\d{1,3}\s*fps\b""", RegexOption.IGNORE_CASE))

        val hasHeroSelectionText = ocrText.contains("Chọn tướng", ignoreCase = true) ||
                ocrText.contains("Khóa", ignoreCase = true) ||
                ocrText.contains("Cấm", ignoreCase = true) ||
                ocrText.contains("Ban", ignoreCase = true) ||
                ocrText.contains("Pick", ignoreCase = true) ||
                ocrText.contains("Đội Xanh", ignoreCase = true) ||
                ocrText.contains("Đội Đỏ", ignoreCase = true) ||
                ocrText.contains("Phép bổ trợ", ignoreCase = true) ||
                ocrText.contains("Ngọc bổ trợ", ignoreCase = true)

        if (hasHeroSelectionText && !hasTimerPattern && !hasInGameSpells) {
            return ScreenState.HERO_SELECTION
        }

        val hasStrictLobbyText = ocrText.contains("Bắt Đầu Tìm Trận", ignoreCase = true) ||
                ocrText.contains("Mời Bè Bạn", ignoreCase = true) ||
                ocrText.contains("Sảnh Chờ", ignoreCase = true) ||
                ocrText.contains("Gia Nhập Phòng", ignoreCase = true) ||
                ocrText.contains("Đang tìm trận", ignoreCase = true) ||
                ocrText.contains("Đấu Đỉnh Cao", ignoreCase = true) ||
                ocrText.contains("Đấu Hạng", ignoreCase = true)

        if (hasStrictLobbyText && !hasTimerPattern && !hasInGameSpells && !hasScorePattern) {
            return ScreenState.GAME_MENU
        }

        // When in landscape and timer, spells, ping, or score is found -> 100% IN_MATCH
        if (hasTimerPattern || hasInGameSpells || hasScorePattern || hasPingOrFps) {
            return ScreenState.IN_MATCH
        }

        val hasGeneralInGameKeywords = ocrText.contains("Caesar", ignoreCase = true) ||
                ocrText.contains("Rồng", ignoreCase = true) ||
                ocrText.contains("Trụ", ignoreCase = true) ||
                ocrText.contains("Chiến", ignoreCase = true) ||
                ocrText.contains("16+", ignoreCase = true)

        if (hasGeneralInGameKeywords) {
            return ScreenState.IN_MATCH
        }

        return ScreenState.GAME_MENU
    }
}
