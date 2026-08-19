package com.example.model

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
    val matchTimeSeconds: Int = 0,
    val winProbability: Int = 50, // 0 to 100%
    val currentObjective: ObjectiveTarget = ObjectiveTarget.FARM_SAFE,
    val dangerWarning: String = "Không có nguy hiểm cận kề",
    val dangerLevel: DangerLevel = DangerLevel.SAFE,
    val teamGoldDiff: Int = 0, // + is ally lead, - is deficit
    val allyKills: Int = 0,
    val enemyKills: Int = 0,
    val allyTowers: Int = 0,
    val enemyTowers: Int = 0,
    val teamfightAdvice: String = "Farm an toàn và giữ vị trí sau Đỡ Đòn",
    val splitPushAdvice: String = "Giữ thế lính cân bằng ở các đường",
    val carryTarget: String = "Xạ Thủ Chủ Lực",
    val detectedUIMode: DetectedScreenMode = DetectedScreenMode.IDLE,
    val isVoiceMuted: Boolean = false,
    val topThreats: List<ThreatPlayer> = emptyList(),
    val itemRecommendations: List<ItemRecommendation> = listOf(
        ItemRecommendation("Đao Truy Hồn", "Khắc chế tướng hồi máu (Veres, Florentino, Taara)", "Lên Đồ Khắc Chế", 2000, isCounter = true),
        ItemRecommendation("Huân Chương Troy", "Kháng sốc sát thương phép từ Pháp Sư địch", "Lên Đồ Khắc Chế", 2220, isCounter = true),
        ItemRecommendation("Thương Longinus", "Xuyên giáp Đỡ Đòn và giảm hồi chiêu", "Trang Bị Cốt Lõi", 2060)
    ),
    val lastOcrText: String = "",
    val captureFpsMode: String = "Nghỉ (1 khung hình / 5 giây)"
) {
    val formattedTime: String
        get() {
            val min = matchTimeSeconds / 60
            val sec = matchTimeSeconds % 60
            return "%02d:%02d".format(min, sec)
        }

    val formattedGoldDiff: String
        get() {
            val prefix = if (teamGoldDiff >= 0) "+ " else "- "
            val absVal = kotlin.math.abs(teamGoldDiff)
            return if (absVal >= 1000) {
                "${prefix}%.1fk Vàng".format(absVal / 1000f)
            } else {
                "${prefix}${absVal} Vàng"
            }
        }
}
