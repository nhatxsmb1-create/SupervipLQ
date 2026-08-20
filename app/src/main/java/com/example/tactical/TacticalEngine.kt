package com.example.tactical

import com.example.model.CoachStatus
import com.example.model.DangerLevel
import com.example.model.DetectedScreenMode
import com.example.model.GameState
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
        val isMatchActive = gameState.matchActive == true
        val confidence = gameState.overallConfidence

        val coachStatus = when {
            gameState.screenState == ScreenState.OUTSIDE_GAME -> CoachStatus.OUTSIDE_MATCH
            gameState.screenState == ScreenState.GAME_MENU -> CoachStatus.DETECTING_MATCH
            gameState.screenState == ScreenState.HERO_SELECTION -> CoachStatus.IN_HERO_SELECTION
            gameState.screenState == ScreenState.LOADING -> CoachStatus.LOADING_MATCH
            gameState.screenState in listOf(ScreenState.IN_MATCH, ScreenState.SCOREBOARD_OPEN, ScreenState.SHOP_OPEN, ScreenState.COMBAT) && confidence < 0.35f -> CoachStatus.IN_MATCH_ANALYZING
            gameState.screenState in listOf(ScreenState.IN_MATCH, ScreenState.SCOREBOARD_OPEN, ScreenState.SHOP_OPEN, ScreenState.COMBAT) -> CoachStatus.IN_MATCH_READY
            gameState.screenState == ScreenState.MATCH_END -> CoachStatus.MATCH_ENDED
            else -> CoachStatus.OUTSIDE_MATCH
        }

        // Handle Hero Selection (Ban-Pick phase)
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
                dangerWarning = "Đang Ban-Pick: Ưu tiên cấm chọn tướng khống chế cứng hoặc có khả năng hồi phục mạnh.",
                dangerLevel = DangerLevel.MEDIUM,
                teamGoldDiff = 0,
                teamfightAdvice = "Gợi ý Phép Bổ Trợ: Tốc Biến / Tê Tái. Phù Hiệu: Ma Tính / Ma Mũi Tên.",
                splitPushAdvice = "Đảm bảo đội hình đủ 5 vị trí: Đỡ Đòn, Đi Rừng, Pháp Sư, Xạ Thủ & Trợ Thủ.",
                carryTarget = "Giai Đoạn Cấm / Chọn",
                itemRecommendations = listOf(
                    ItemRecommendation("Giày Kiên Cường", "Kháng hiệu ứng khống chế", "Khuyến Nghiệu Đội Hình", 700),
                    ItemRecommendation("Đao Truy Hồn", "Sẵn sàng lên nếu địch có hồi máu mạnh", "Khắc Che", 2000, isCounter = true)
                )
            )
            return TacticalEvaluationResult(
                newState = heroState,
                voiceCallout = "Đang giai đoạn cấm chọn tướng. Chú ý phép bổ trợ và ngọc",
                calloutTag = "hero_selection",
                calloutPriority = 2
            )
        }

        // Handle Loading Screen
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
                dangerWarning = "Đang nạp trận... Sẵn sàng di chuyển mua đồ đầu trận!",
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

        // If not in active match or low data confidence, return clean idle/waiting state
        if (coachStatus != CoachStatus.IN_MATCH_READY && coachStatus != CoachStatus.IN_MATCH_ANALYZING) {
            val emptyState = currentState.copy(
                coachStatus = coachStatus,
                gameDetected = (coachStatus != CoachStatus.OUTSIDE_MATCH),
                matchStarted = (coachStatus == CoachStatus.IN_MATCH_ANALYZING || coachStatus == CoachStatus.IN_MATCH_READY),
                gameDataValid = false,
                analysisReady = false,
                matchTimeSeconds = gameState.matchTimeSeconds ?: 0,
                winProbability = null,
                currentObjective = null,
                dangerWarning = coachStatus.detailText,
                dangerLevel = DangerLevel.SAFE,
                teamGoldDiff = 0,
                allyKills = gameState.allyKills ?: 0,
                enemyKills = gameState.enemyKills ?: 0,
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

        val winProbResult = winProbabilityModel.calculateWinProbability(gameState)
        val goldDiff = gameState.goldDifference ?: 0
        val matchTimeSeconds = gameState.matchTimeSeconds ?: 0
        val allyKills = gameState.allyKills ?: 0
        val enemyKills = gameState.enemyKills ?: 0

        // Objective target selection based on game state
        val objective = when {
            matchTimeSeconds >= 900 && goldDiff >= 0 -> ObjectiveTarget.ENRAGED_CAESAR
            matchTimeSeconds >= 900 && goldDiff < -5000 -> ObjectiveTarget.HIGH_GROUND_DEFENSE
            matchTimeSeconds in 480 until 900 && goldDiff >= 1000 -> ObjectiveTarget.DARK_SLAYER
            matchTimeSeconds in 120 until 480 && goldDiff >= -1500 -> ObjectiveTarget.ABYSSAL_DRAGON
            matchTimeSeconds in 70 until 120 -> ObjectiveTarget.SPIRIT_SENTINEL
            goldDiff <= -6000 -> ObjectiveTarget.HIGH_GROUND_DEFENSE
            goldDiff >= 4000 -> ObjectiveTarget.MID_TOWER
            else -> ObjectiveTarget.FARM_SAFE
        }

        // Confidence-aware phrasing (Requirement 12)
        val isLowConfidence = confidence < 0.70f
        val prefix = if (isLowConfidence) "Có khả năng " else ""

        var dangerWarning = "${prefix}An toàn, giữ nhịp farm"
        var dangerLevel = DangerLevel.SAFE
        var voiceCallout: String? = null
        var calloutTag = "none"
        var calloutPriority = 1

        when {
            goldDiff <= -5000 -> {
                dangerWarning = "${prefix}đang thua ${kotlin.math.abs(goldDiff)} Vàng! Tránh giao tranh 5v5 trực diện."
                dangerLevel = DangerLevel.CRITICAL
                voiceCallout = "Tránh giao tranh - Đang thua tiền"
                calloutTag = "danger_gold"
                calloutPriority = 3
            }
            matchTimeSeconds in 75..110 -> {
                dangerWarning = "${prefix}rừng địch đạt Cấp 4 — Cảnh giác bị gank tại bụi cỏ Sông!"
                dangerLevel = DangerLevel.HIGH
                voiceCallout = "Kẻ địch biến mất ở Sông"
                calloutTag = "danger_gank_early"
                calloutPriority = 2
            }
            gameState.screenState == ScreenState.COMBAT && goldDiff < -2000 -> {
                dangerWarning = "Giao tranh thế bất lợi! Rút lui giữ mạng bảo vệ Trụ."
                dangerLevel = DangerLevel.HIGH
                voiceCallout = "Rút lui ngay"
                calloutTag = "combat_disadvantage"
                calloutPriority = 3
            }
            goldDiff >= 4500 && matchTimeSeconds in 480..900 -> {
                dangerWarning = "${prefix}đội đang dẫn trước ${goldDiff} Vàng. Ưu tiên kiểm soát Rồng & Caesar!"
                dangerLevel = DangerLevel.SAFE
                voiceCallout = "Ăn Tà Thần Caesar"
                calloutTag = "objective_slayer"
                calloutPriority = 2
            }
            matchTimeSeconds in 120..150 -> {
                dangerWarning = "${prefix}Rồng Ánh Sáng đã xuất hiện. Kiểm soát bụi cỏ lấy tầm nhìn."
                dangerLevel = DangerLevel.MEDIUM
                voiceCallout = "Ăn Rồng Ánh Sáng"
                calloutTag = "objective_dragon"
                calloutPriority = 2
            }
        }

        // Tactical advice
        val teamfightAdvice = when {
            goldDiff <= -4000 -> "Tránh giao tranh. Đội đang thiếu tiền, nhường rừng ngoài và dọn lính trong Trụ."
            goldDiff in -3999..2000 -> "Giao tranh cân bằng: Chờ Đỡ Đòn mở combat trước, Sát Thủ vòng sau bắt Xạ Thủ."
            else -> "Đang dẫn vàng, không cần mạo hiểm. Tập trung 5 người ép Trụ Đường Giữa."
        }

        val splitPushAdvice = when {
            goldDiff <= -3000 -> "Đấu Sĩ đẩy lẻ đường xa nhất để kéo giãn đội hình đối phương."
            goldDiff >= 4000 -> "Đẩy đều 3 đường lính cao cùng lúc để tạo áp lực."
            else -> "Giữ lính qua nửa sông trước khi di chuyển hỗ trợ giao tranh rồng."
        }

        val itemRecommendations = listOf(
            ItemRecommendation("Đao Truy Hồn", "Khắc chế hồi máu", "Lên Đồ Khắc Chế", 2000, isCounter = true),
            ItemRecommendation("Huân Chương Troy", "Kháng sốc sát thương phép", "Lên Đồ Khắc Chế", 2220, isCounter = true),
            ItemRecommendation("Thương Longinus", "Xuyên giáp và giảm hồi chiêu", "Trang Bị Cốt Lõi", 2060)
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
            carryTarget = if (enemyKills >= 10) "Violet / Elsu (Xạ Thủ địch đang xanh)" else "Chủ Lực Địch",
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
            overallConfidence = if (forceValid) 0.90f else 0.40f
        )
        return evaluateGameState(simulatedGameState, currentState)
    }
}
