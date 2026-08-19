package com.example.model

enum class CoachStatus(val displayName: String, val detailText: String) {
    OUTSIDE_MATCH("TRỢ LÝ SẴN SÀNG", "Chưa phát hiện trận đấu. Vào Liên Quân để bắt đầu phân tích"),
    DETECTING_MATCH("ĐANG NHẬN DIỆN TRẬN", "Đang quét màn hình Liên Quân..."),
    IN_MATCH_ANALYZING("ĐANG THU THẬP DỮ LIỆU", "Đang phân tích chỉ số trận đấu..."),
    IN_MATCH_READY("ĐANG PHÂN TÍCH TRỰC TIẾP", "Đang phân tích chiến thuật thực chiến"),
    MATCH_ENDED("TRẬN ĐẤU KẾT THÚC", "Trận đấu đã hoàn thành")
}

enum class ObjectiveTarget(val displayName: String, val minGameTimeSec: Int, val maxGameTimeSec: Int) {
    SPIRIT_SENTINEL("Dơi Thủ Vệ (Bảo Kê Đường)", 70, 480),
    ABYSSAL_DRAGON("Rồng Krayg / Rồng Ánh Sáng", 120, 900),
    DARK_SLAYER("Tà Thần Caesar (Cấp 2)", 480, 900),
    ENRAGED_CAESAR("Caesar Bạo Chúa (Cấp 3)", 900, 3600),
    MID_TOWER("Ép Trụ Đường Giữa", 180, 1200),
    HIGH_GROUND_DEFENSE("Thủ Trụ Siêu Cấp & Nhà Chính", 600, 3600),
    INVADE_ENEMY_BUFF("Cướp Bùa Xanh / Đỏ Địch", 60, 600),
    FARM_SAFE("Farm Rừng An Toàn & Ôm Trụ", 0, 3600)
}

enum class DangerLevel(val labelVi: String) {
    SAFE("An Toàn"),
    LOW("Thấp"),
    MEDIUM("Cảnh Giác"),
    HIGH("Nguy Hiểm Cao"),
    CRITICAL("Báo Động Đỏ")
}

enum class DetectedScreenMode {
    IDLE,
    COMBAT,
    SCOREBOARD_OPEN,
    SHOP_OPEN,
    MINIMAP_ALERT
}

data class ThreatPlayer(
    val heroName: String,
    val role: String,
    val kda: String,
    val currentGold: Int,
    val threatLevel: String, // "CAO", "TRUNG BÌNH", "THẤP"
    val counterTip: String
)

data class ItemRecommendation(
    val itemName: String,
    val reason: String,
    val category: String, // "Lên Đồ Khắc Chế", "Trang Bị Cốt Lõi", "Tình Huống"
    val costGold: Int,
    val isCounter: Boolean = false
)

data class TacticalState(
    val coachStatus: CoachStatus = CoachStatus.OUTSIDE_MATCH,
    val gameDetected: Boolean = false,
    val matchStarted: Boolean = false,
    val gameDataValid: Boolean = false,
    val analysisReady: Boolean = false,
    val matchTimeSeconds: Int = 0,
    val winProbability: Int? = null,
    val currentObjective: ObjectiveTarget? = null,
    val dangerWarning: String = "",
    val dangerLevel: DangerLevel = DangerLevel.SAFE,
    val teamGoldDiff: Int = 0, // + is ally lead, - is deficit
    val allyKills: Int = 0,
    val enemyKills: Int = 0,
    val allyTowers: Int = 0,
    val enemyTowers: Int = 0,
    val teamfightAdvice: String = "",
    val splitPushAdvice: String = "",
    val carryTarget: String = "",
    val detectedUIMode: DetectedScreenMode = DetectedScreenMode.IDLE,
    val isVoiceMuted: Boolean = false,
    val topThreats: List<ThreatPlayer> = emptyList(),
    val itemRecommendations: List<ItemRecommendation> = emptyList(),
    val lastOcrText: String = "",
    val captureFpsMode: String = "Nghỉ (Chờ trận)"
) {
    val formattedTime: String
        get() {
            if (!gameDataValid || matchTimeSeconds <= 0) return "--:--"
            val min = matchTimeSeconds / 60
            val sec = matchTimeSeconds % 60
            return "%02d:%02d".format(min, sec)
        }

    val formattedWinRate: String
        get() = if (analysisReady && winProbability != null) "${winProbability}%" else "--%"

    val formattedGoldDiff: String
        get() {
            if (!gameDataValid) return "-- Vàng"
            val prefix = if (teamGoldDiff >= 0) "+ " else "- "
            val absVal = kotlin.math.abs(teamGoldDiff)
            return if (absVal >= 1000) {
                "${prefix}%.1fk Vàng".format(absVal / 1000f)
            } else {
                "${prefix}${absVal} Vàng"
            }
        }
}
