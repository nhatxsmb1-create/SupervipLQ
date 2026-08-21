package com.example.tactical

import com.example.model.CoachStatus
import com.example.model.DangerLevel
import com.example.model.DetectedScreenMode
import com.example.model.GameState
import com.example.model.HeroDatabase
import com.example.model.ItemDatabase
import com.example.model.ItemRecommendation
import com.example.model.ObjectiveTarget
import com.example.model.ScreenState
import com.example.model.TacticalState
import com.example.model.FeatureBasedWinProbabilityModel

data class TacticalEvaluationResult(
    val newState: TacticalState,
    val voiceCallout: String?,
    val calloutTag: String,
    val calloutPriority: Int // 1: Low, 2: Medium, 3: High
)

class TacticalEngine {

    private val winProbabilityModel = FeatureBasedWinProbabilityModel()

    fun evaluateGameState(gameState: GameState, currentState: TacticalState): TacticalEvaluationResult {
        val confidence = gameState.overallConfidence

        val coachStatus = when (gameState.screenState) {
            ScreenState.HERO_SELECTION -> CoachStatus.IN_HERO_SELECTION
            ScreenState.LOADING -> CoachStatus.LOADING_MATCH
            ScreenState.MATCH_END -> CoachStatus.MATCH_ENDED
            ScreenState.GAME_MENU -> CoachStatus.DETECTING_MATCH
            ScreenState.IN_MATCH,
            ScreenState.SCOREBOARD_OPEN,
            ScreenState.SHOP_OPEN,
            ScreenState.COMBAT -> CoachStatus.IN_MATCH_READY
            ScreenState.OUTSIDE_GAME,
            ScreenState.UNKNOWN -> CoachStatus.OUTSIDE_MATCH
        }

        // 1. Handle Ban-Pick Phase (Hero Selection)
        if (coachStatus == CoachStatus.IN_HERO_SELECTION) {
            val heroState = currentState.copy(
                coachStatus = coachStatus,
                gameDetected = true,
                matchStarted = false,
                gameDataValid = true,
                analysisReady = true,
                matchTimeSeconds = 0,
                winProbability = null,
                currentObjective = null,
                dangerWarning = "Ban-Pick: Ưu tiên cấm tướng khống chế cứng (Arum, Aleister) & sát thương cơ động (Aoi, Biron).",
                dangerLevel = DangerLevel.MEDIUM,
                teamGoldDiff = 0,
                teamfightAdvice = "Gợi ý Phép Bổ Trợ: Tốc Biến / Trừng Trị. Phù Hiệu: Ma Tính / Ma Mũi Tên.",
                splitPushAdvice = "Đảm bảo đội hình đủ 5 vị trí: Đỡ Đòn, Đi Rừng, Pháp Sư, Xạ Thủ & Trợ Thủ.",
                carryTarget = "Giai Đoạn Ban-Pick",
                itemRecommendations = listOf(
                    ItemRecommendation("Giày Kiên Cường", "Kháng hiệu ứng khống chế", "Đề Xuất Ban-Pick", 700),
                    ItemRecommendation("Đao Truy Hồn", "Lên sớm nếu đối phương có hồi phục", "Khắc Chế", 2000, isCounter = true)
                )
            )
            return TacticalEvaluationResult(
                newState = heroState,
                voiceCallout = "Giai đoạn cấm chọn tướng. Chú ý phép bổ trợ và ngọc",
                calloutTag = "hero_selection",
                calloutPriority = 2
            )
        }

        // 2. Handle Loading Screen
        if (coachStatus == CoachStatus.LOADING_MATCH) {
            val loadingState = currentState.copy(
                coachStatus = coachStatus,
                gameDetected = true,
                matchStarted = false,
                gameDataValid = true,
                analysisReady = true,
                matchTimeSeconds = 0,
                winProbability = null,
                currentObjective = null,
                dangerWarning = "Đang nạp trận... Sẵn sàng mua trang bị khởi đầu và ra canh bùa rừng!",
                dangerLevel = DangerLevel.SAFE,
                teamfightAdvice = "Mua trang bị khởi đầu (Kiếm/Nhẫn/Rừng) ngay khi xuất hiện ở Tế Đàn.",
                splitPushAdvice = "Di chuyển ra bụi cỏ bảo vệ bùa rừng cùng đồng đội.",
                carryTarget = "Đang Nạp Trận..."
            )
            return TacticalEvaluationResult(
                newState = loadingState,
                voiceCallout = null,
                calloutTag = "loading",
                calloutPriority = 1
            )
        }

        // 3. Handle Outside Match / Lobby Menu
        if (coachStatus == CoachStatus.OUTSIDE_MATCH || coachStatus == CoachStatus.DETECTING_MATCH || coachStatus == CoachStatus.MATCH_ENDED) {
            val emptyState = currentState.copy(
                coachStatus = coachStatus,
                gameDetected = (coachStatus != CoachStatus.OUTSIDE_MATCH),
                matchStarted = false,
                gameDataValid = false,
                analysisReady = false,
                matchTimeSeconds = 0,
                winProbability = null,
                currentObjective = null,
                dangerWarning = coachStatus.detailText,
                dangerLevel = DangerLevel.SAFE,
                teamGoldDiff = 0,
                allyKills = 0,
                enemyKills = 0,
                teamfightAdvice = "",
                splitPushAdvice = "",
                carryTarget = "",
                itemRecommendations = emptyList()
            )
            return TacticalEvaluationResult(
                newState = emptyState,
                voiceCallout = null,
                calloutTag = "none",
                calloutPriority = 1
            )
        }

        // 4. In Active Match: High-Accuracy Tactical Evaluation
        val winProbResult = winProbabilityModel.calculateWinProbability(gameState)
        val goldDiff = gameState.goldDifference ?: 0
        val matchTimeSeconds = gameState.matchTimeSeconds ?: 0
        val allyKills = gameState.allyKills ?: 0
        val enemyKills = gameState.enemyKills ?: 0

        // Objective target selection based on latest Arena of Valor Esports Meta
        val objective = when {
            matchTimeSeconds >= 900 && goldDiff >= 0 -> ObjectiveTarget.ENRAGED_CAESAR // 15:00+ Caesar Bạo Chúa Cấp 3
            matchTimeSeconds >= 900 && goldDiff < -5000 -> ObjectiveTarget.HIGH_GROUND_DEFENSE
            matchTimeSeconds in 480 until 900 && goldDiff >= 1000 -> ObjectiveTarget.DARK_SLAYER // 08:00+ Caesar Hắc Ám Cấp 2
            matchTimeSeconds in 120 until 480 && goldDiff >= -1500 -> ObjectiveTarget.ABYSSAL_DRAGON // 02:00+ Rồng Ánh Sáng Krayg
            matchTimeSeconds in 70 until 120 -> ObjectiveTarget.SPIRIT_SENTINEL // Dơi Thủ Vệ
            goldDiff <= -6000 -> ObjectiveTarget.HIGH_GROUND_DEFENSE
            goldDiff >= 4000 -> ObjectiveTarget.MID_TOWER
            else -> ObjectiveTarget.FARM_SAFE
        }

        var dangerWarning = "Tình hình ổn định: Duy trì farm lính & kiểm soát tầm nhìn bản đồ."
        var dangerLevel = DangerLevel.SAFE
        var voiceCallout: String? = null
        var calloutTag = "none"
        var calloutPriority = 1

        when {
            goldDiff <= -5000 -> {
                dangerWarning = "Đang thua ${kotlin.math.abs(goldDiff)} Vàng! Né giao tranh 5v5, ôm sát trụ thủ nhà."
                dangerLevel = DangerLevel.CRITICAL
                voiceCallout = "Cẩn thận - Đang thua tiền, lùi về ôm trụ"
                calloutTag = "danger_gold_deficit"
                calloutPriority = 3
            }
            matchTimeSeconds in 80..115 -> {
                dangerWarning = "Mốc 01:30: Rừng đối phương đạt Cấp 4! Chú ý kiểm tra bụi sông đề phòng gank bất ngờ!"
                dangerLevel = DangerLevel.HIGH
                voiceCallout = "Cảnh giác gank - Rừng đối phương đã Cấp 4"
                calloutTag = "danger_gank_early"
                calloutPriority = 2
            }
            matchTimeSeconds in 116..150 -> {
                dangerWarning = "Mốc 02:00: Rồng Krayg & Caesar Cấp 1 xuất hiện! Tập trung kiểm soát hang Rồng."
                dangerLevel = DangerLevel.MEDIUM
                voiceCallout = "Tập trung kiểm soát Rồng Krayg"
                calloutTag = "objective_dragon"
                calloutPriority = 2
            }
            matchTimeSeconds in 230..255 -> {
                dangerWarning = "Mốc 04:00: Mất giáp bảo vệ Trụ ngoài! Tận dụng ép Trụ hoặc lùi sâu bảo vệ."
                dangerLevel = DangerLevel.MEDIUM
                voiceCallout = "Hết khiên Trụ 4 phút - Chú ý ép Trụ"
                calloutTag = "tower_shield_expired"
                calloutPriority = 2
            }
            matchTimeSeconds in 475..520 -> {
                dangerWarning = "Mốc 08:00: Tà Thần Caesar Hắc Ám Cấp 2 xuất hiện! Ăn Caesar thả Rồng đẩy Trụ."
                dangerLevel = DangerLevel.HIGH
                voiceCallout = "Mốc 8 phút - Tập trung ăn Caesar Hắc Ám"
                calloutTag = "objective_slayer_tier2"
                calloutPriority = 2
            }
            matchTimeSeconds in 890..940 -> {
                dangerWarning = "Mốc 15:00: Caesar Bạo Chúa xuất hiện! Mục tiêu sinh tử quyết định toàn bộ ván đấu!"
                dangerLevel = DangerLevel.CRITICAL
                voiceCallout = "Báo động mốc 15 phút - Kiểm soát Caesar Bạo Chúa"
                calloutTag = "objective_enraged_slayer"
                calloutPriority = 3
            }
            gameState.screenState == ScreenState.COMBAT && goldDiff < -2000 -> {
                dangerWarning = "Giao tranh bất lợi! Giữ cự ly và lùi về trụ."
                dangerLevel = DangerLevel.HIGH
                voiceCallout = "Lùi về giữ vị trí"
                calloutTag = "combat_disadvantage"
                calloutPriority = 3
            }
            goldDiff >= 3500 -> {
                dangerWarning = "Dẫn trước ${goldDiff} Vàng! Ép giao tranh lấy Trụ Đường Giữa & cướp Bùa Rừng địch."
                dangerLevel = DangerLevel.SAFE
                voiceCallout = "Dẫn trước tiền - Ép Trụ đường giữa"
                calloutTag = "lead_advantage"
                calloutPriority = 2
            }
        }

        val teamfightAdvice = when {
            goldDiff <= -4000 -> "Né combat 5v5. Nhường rừng ngoài, Pháp Sư & Xạ Thủ xả chiêu dọn lính từ trong Trụ."
            goldDiff in -3999..2000 -> "Giao tranh cân bằng: Trợ Thủ mở giao tranh/bảo kê, Sát Thủ vòng sau bắt Chủ Lực địch."
            else -> "Đang có lợi thế Vàng: Đỡ Đòn tràn lên kiểm soát bụi cỏ rừng địch, ép Trụ Đường Giữa."
        }

        val splitPushAdvice = when {
            goldDiff <= -3000 -> "Đấu Sĩ cơ động đẩy lính đường xa để kéo giãn đội hình đối phương."
            goldDiff >= 4000 -> "Đẩy đồng loạt 3 đường lính cao cùng lúc để ép đối phương mất Trụ siêu cấp."
            else -> "Dọn sạch lính qua sông trước khi tập trung giao tranh mục tiêu lớn."
        }

        // Dynamic counter items recommendation
        val itemRecommendations = listOf(
            ItemRecommendation("Đao Truy Hồn / Sách Truy Hồn", "Khắc chế tướng hồi phục (Biron, Helen, Taara)", "Khắc Chế", 2000, isCounter = true),
            ItemRecommendation("Phụ Kiện Ma Nhãn", "Soi tàng hình Kaine / Aoi / Elsu", "Trợ Thủ", 1400, isCounter = true),
            ItemRecommendation("Quả Cầu Băng Sương / Liềm Đoạt Mệnh", "Tránh bị sốc sát thương đột tử", "Sinh Tồn", 2000)
        )

        val newState = currentState.copy(
            coachStatus = CoachStatus.IN_MATCH_READY,
            gameDetected = true,
            matchStarted = true,
            gameDataValid = true,
            analysisReady = true,
            matchTimeSeconds = matchTimeSeconds,
            winProbability = winProbResult.percentage,
            currentObjective = objective,
            dangerWarning = dangerWarning,
            dangerLevel = dangerLevel,
            teamGoldDiff = goldDiff,
            allyKills = allyKills,
            enemyKills = enemyKills,
            teamfightAdvice = teamfightAdvice,
            splitPushAdvice = splitPushAdvice,
            carryTarget = if (enemyKills >= 8) "Xạ Thủ / Pháp Sư đối phương (Đang xanh)" else "Chủ Lực Địch",
            detectedUIMode = when (gameState.screenState) {
                ScreenState.SCOREBOARD_OPEN -> DetectedScreenMode.SCOREBOARD_OPEN
                ScreenState.SHOP_OPEN -> DetectedScreenMode.SHOP_OPEN
                ScreenState.COMBAT -> DetectedScreenMode.COMBAT
                else -> DetectedScreenMode.IDLE
            },
            itemRecommendations = itemRecommendations
        )

        return TacticalEvaluationResult(
            newState = newState,
            voiceCallout = voiceCallout,
            calloutTag = calloutTag,
            calloutPriority = calloutPriority
        )
    }

    fun evaluate(
        currentState: TacticalState,
        matchTimeSeconds: Int,
        goldDiff: Int,
        allyKills: Int,
        enemyKills: Int,
        allyTowers: Int,
        enemyTowers: Int,
        detectedMode: DetectedScreenMode,
        forceValid: Boolean = false
    ): TacticalEvaluationResult {
        val simulatedGameState = GameState(
            matchActive = forceValid || matchTimeSeconds > 0 || detectedMode != DetectedScreenMode.IDLE,
            screenState = if (forceValid || matchTimeSeconds > 0) ScreenState.IN_MATCH else ScreenState.OUTSIDE_GAME,
            matchTimeSeconds = matchTimeSeconds,
            goldDifference = goldDiff,
            allyKills = allyKills,
            enemyKills = enemyKills,
            overallConfidence = if (forceValid) 0.95f else 0.40f
        )
        return evaluateGameState(simulatedGameState, currentState)
    }
}
