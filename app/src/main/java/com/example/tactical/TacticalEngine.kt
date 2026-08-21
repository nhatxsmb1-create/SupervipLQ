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
            gameState.screenState == ScreenState.HERO_SELECTION -> CoachStatus.IN_HERO_SELECTION
            gameState.screenState == ScreenState.LOADING -> CoachStatus.LOADING_MATCH
            gameState.screenState == ScreenState.MATCH_END -> CoachStatus.MATCH_ENDED
            gameState.screenState == ScreenState.GAME_MENU -> CoachStatus.DETECTING_MATCH
            else -> CoachStatus.IN_MATCH_READY
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

        // Objective target selection based on latest Arena of Valor Meta
        val objective = when {
            matchTimeSeconds >= 900 && goldDiff >= 0 -> ObjectiveTarget.ENRAGED_CAESAR // 15:00+ Caesar Bạo Chúa Cấp 3
            matchTimeSeconds >= 900 && goldDiff < -5000 -> ObjectiveTarget.HIGH_GROUND_DEFENSE
            matchTimeSeconds in 480 until 900 && goldDiff >= 1000 -> ObjectiveTarget.DARK_SLAYER // 08:00+ Caesar Hắc Ám Cấp 2
            matchTimeSeconds in 120 until 480 && goldDiff >= -1500 -> ObjectiveTarget.ABYSSAL_DRAGON // 02:00+ Rồng Ánh Sáng
            matchTimeSeconds in 70 until 120 -> ObjectiveTarget.SPIRIT_SENTINEL // Dơi Thủ Vệ
            goldDiff <= -6000 -> ObjectiveTarget.HIGH_GROUND_DEFENSE
            goldDiff >= 4000 -> ObjectiveTarget.MID_TOWER
            else -> ObjectiveTarget.FARM_SAFE
        }

        // Confidence-aware phrasing
        val isLowConfidence = confidence < 0.70f
        val prefix = if (isLowConfidence) "Có khả năng " else ""

        var dangerWarning = "${prefix}An toàn, duy trì nhịp farm & tích Vàng"
        var dangerLevel = DangerLevel.SAFE
        var voiceCallout: String? = null
        var calloutTag = "none"
        var calloutPriority = 1

        when {
            goldDiff <= -5000 -> {
                dangerWarning = "${prefix}đang thua ${kotlin.math.abs(goldDiff)} Vàng! Né giao tranh 5v5, dọn lính sát Trụ."
                dangerLevel = DangerLevel.CRITICAL
                voiceCallout = "Tránh giao tranh - Đang thua tiền sâu"
                calloutTag = "danger_gold"
                calloutPriority = 3
            }
            matchTimeSeconds in 80..115 -> {
                dangerWarning = "${prefix}Rừng địch hoàn thành vòng Rừng 1 (Cấp 4)! Chú ý gank tại bụi Sông!"
                dangerLevel = DangerLevel.HIGH
                voiceCallout = "Cảnh giác gank - Rừng địch đã Cấp 4"
                calloutTag = "danger_gank_early"
                calloutPriority = 2
            }
            matchTimeSeconds in 230..255 -> {
                dangerWarning = "${prefix}Mốc 4 phút: Mất khiên bảo vệ Trụ ngoài! Tập trung bảo vệ hoặc ép Trụ Đường Giữa!"
                dangerLevel = DangerLevel.MEDIUM
                voiceCallout = "Chú ý ép Trụ - Hết khiên Trụ 4 phút"
                calloutTag = "tower_shield_expired"
                calloutPriority = 2
            }
            gameState.screenState == ScreenState.COMBAT && goldDiff < -2000 -> {
                dangerWarning = "Giao tranh bất lợi! Lùi về giữ mạng bảo vệ Trụ!"
                dangerLevel = DangerLevel.HIGH
                voiceCallout = "Rút lui ngay"
                calloutTag = "combat_disadvantage"
                calloutPriority = 3
            }
            goldDiff >= 4000 && matchTimeSeconds >= 480 -> {
                dangerWarning = "${prefix}đang dẫn trước ${goldDiff} Vàng. Kiểm soát Bùa Caesar Hắc Ám ép đường!"
                dangerLevel = DangerLevel.SAFE
                voiceCallout = "Ăn Caesar Hắc Ám ép đường"
                calloutTag = "objective_slayer"
                calloutPriority = 2
            }
            matchTimeSeconds in 120..150 -> {
                dangerWarning = "${prefix}Rồng Ánh Sáng (2:00) xuất hiện. Kiểm soát bụi lấy tầm nhìn ăn Rồng."
                dangerLevel = DangerLevel.MEDIUM
                voiceCallout = "Tập trung ăn Rồng Ánh Sáng"
                calloutTag = "objective_dragon"
                calloutPriority = 2
            }
            matchTimeSeconds in 890..930 -> {
                dangerWarning = "Mốc 15 phút: Caesar Bạo Chúa xuất hiện! Mục tiêu sinh tử quyết định trận đấu!"
                dangerLevel = DangerLevel.HIGH
                voiceCallout = "Tập trung kiểm soát Caesar Bạo Chúa"
                calloutTag = "objective_enraged_slayer"
                calloutPriority = 3
            }
        }

        // Tactical advice according to Meta
        val teamfightAdvice = when {
            goldDiff <= -4000 -> "Tránh giao tranh 5v5. Nhường rừng ngoài, Pháp Sư & Xạ Thủ dọn sóng lính trong Trụ."
            goldDiff in -3999..2000 -> "Giao tranh cân bằng: Trợ Thủ mở combat / bọc lót, Sát Thủ chờ bắt Xạ Thủ / Pháp Sư địch."
            else -> "Đang dẫn Vàng: Trợ Thủ / Đỡ Đòn kiểm soát tàn bạo tầm nhìn Rừng địch, ép Trụ Đường Giữa."
        }

        val splitPushAdvice = when {
            goldDiff <= -3000 -> "Đấu Sĩ cơ động (Biron / Florentino) đẩy lẻ đường xa để kéo giãn đội hình địch."
            goldDiff >= 4000 -> "Đẩy đồng loạt 3 đường lính cao cùng lúc, không để đối phương thủ Trụ dễ dàng."
            else -> "Giữ lính qua nửa sông trước khi di chuyển hỗ trợ ăn Rồng hoặc Caesar."
        }

        val itemRecommendations = listOf(
            ItemRecommendation("Đao Truy Hồn / Sách Truy Hồn", "BẮT BỘC nếu địch có Helen, Biron, Florentino", "Khắc Che", 2000, isCounter = true),
            ItemRecommendation("Phụ Kiện Ma Nhãn", "Soi tàng hình Kaine / Aoi / Elsu", "Trợ Thủ Meta", 1400, isCounter = true),
            ItemRecommendation("Quả Cầu Băng Sương / Liềm Đoạt Mệnh", "Né sốc sát thương late game", "Sinh Tồn", 2000)
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
