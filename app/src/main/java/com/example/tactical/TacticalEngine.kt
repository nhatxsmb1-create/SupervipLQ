package com.example.tactical

import com.example.model.DangerLevel
import com.example.model.DetectedScreenMode
import com.example.model.ItemRecommendation
import com.example.model.ObjectiveTarget
import com.example.model.TacticalState
import com.example.model.ThreatPlayer
import kotlin.math.max
import kotlin.math.min

data class TacticalEvaluationResult(
    val newState: TacticalState,
    val voiceCallout: String?,
    val calloutTag: String,
    val calloutPriority: Int // 1: Low, 2: Medium, 3: High
)

class TacticalEngine {

    fun evaluate(
        matchTimeSeconds: Int,
        goldDiff: Int,
        allyKills: Int,
        enemyKills: Int,
        allyTowers: Int,
        enemyTowers: Int,
        detectedMode: DetectedScreenMode
    ): TacticalEvaluationResult {

        // 1. Calculate Estimated Win Probability
        val baseWin = 50
        val goldImpact = (goldDiff / 400).coerceIn(-35, 35)
        val killImpact = ((allyKills - enemyKills) * 1.5f).toInt().coerceIn(-15, 15)
        val towerImpact = ((allyTowers - enemyTowers) * 4).coerceIn(-20, 20)
        val computedWin = (baseWin + goldImpact + killImpact + towerImpact).coerceIn(5, 95)

        // 2. Determine Macro Objective Target based on Match Timeline & Gold State
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

        // 3. Determine Danger Warning and Danger Level
        var dangerWarning = "Không có nguy hiểm cận kề"
        var dangerLevel = DangerLevel.SAFE
        var voiceCallout: String? = null
        var calloutTag = "none"
        var calloutPriority = 1

        when {
            goldDiff <= -5000 -> {
                dangerWarning = "Đang thua ${kotlin.math.abs(goldDiff)} Vàng! Tuyệt đối không giao tranh 5v5 trực diện."
                dangerLevel = DangerLevel.CRITICAL
                voiceCallout = "Tránh giao tranh - Đang thua tiền"
                calloutTag = "danger_gold"
                calloutPriority = 3
            }
            matchTimeSeconds in 75..110 -> {
                dangerWarning = "Rừng địch đã đạt Cấp 4 — Cảnh giác bị gank tại bụi cỏ Sông!"
                dangerLevel = DangerLevel.HIGH
                voiceCallout = "Kẻ địch biến mất ở Sông"
                calloutTag = "danger_gank_early"
                calloutPriority = 2
            }
            detectedMode == DetectedScreenMode.COMBAT && goldDiff < -2000 -> {
                dangerWarning = "Giao tranh thế bất lợi! Rút lui giữ mạng bảo vệ Trụ."
                dangerLevel = DangerLevel.HIGH
                voiceCallout = "Rút lui ngay"
                calloutTag = "combat_disadvantage"
                calloutPriority = 3
            }
            goldDiff >= 4500 && matchTimeSeconds in 480..900 -> {
                dangerWarning = "Đội đang dẫn trước ${goldDiff} Vàng. Tập trung 5 người ép Tà Thần Caesar!"
                dangerLevel = DangerLevel.SAFE
                voiceCallout = "Ăn Tà Thần Caesar"
                calloutTag = "objective_slayer"
                calloutPriority = 2
            }
            matchTimeSeconds in 120..150 -> {
                dangerWarning = "Rồng Ánh Sáng đầu trận đã xuất hiện. Kiểm soát bụi cỏ lấy tầm nhìn."
                dangerLevel = DangerLevel.MEDIUM
                voiceCallout = "Ăn Rồng Ánh Sáng"
                calloutTag = "objective_dragon"
                calloutPriority = 2
            }
        }

        // 4. Teamfight and Split-push Advice
        val teamfightAdvice = when {
            goldDiff <= -4000 -> "Tránh giao tranh lớn. Đỡ Đòn cắm mắt bụi cỏ, nhường rừng ngoài và dọn lính trong Trụ."
            goldDiff in -3999..2000 -> "Giao tranh cân bằng: Chờ Đỡ Đòn mở combat trước, Sát Thủ vòng sau bắt Xạ Thủ và Pháp Sư địch."
            else -> "Thế trận áp đảo: Tập trung 5 người ép Trụ Đường Giữa hoặc bắt lẻ khi địch ra bãi bùa."
        }

        val splitPushAdvice = when {
            goldDiff <= -3000 -> "Đấu Sĩ đẩy lẻ đường xa nhất để kéo giãn đội hình đối phương."
            goldDiff >= 4000 -> "Đẩy đều 3 đường lính cao cùng lúc để tạo áp lực phá Trụ Siêu Cấp."
            else -> "Giữ lính qua nửa sông trước khi di chuyển hỗ trợ giao tranh rồng."
        }

        val carryTarget = if (enemyKills >= 10) "Violet / Elsu (Xạ Thủ địch đang xanh)" else "Chủ Lực Địch"

        val itemRecommendations = listOf(
            ItemRecommendation("Đao Truy Hồn", "Khắc chế hồi máu (Veres, Florentino, Taara)", "Lên Đồ Khắc Chế", 2000, isCounter = true),
            ItemRecommendation("Huân Chương Troy", "Kháng sốc sát thương phép từ Pháp Sư địch", "Lên Đồ Khắc Chế", 2220, isCounter = true),
            ItemRecommendation("Thương Longinus", "Tăng xuyên giáp và giảm 15% hồi chiêu", "Trang Bị Cốt Lõi", 2060)
        )

        val newState = TacticalState(
            matchTimeSeconds = matchTimeSeconds,
            winProbability = computedWin,
            currentObjective = objective,
            dangerWarning = dangerWarning,
            dangerLevel = dangerLevel,
            teamGoldDiff = goldDiff,
            allyKills = allyKills,
            enemyKills = enemyKills,
            allyTowers = allyTowers,
            enemyTowers = enemyTowers,
            teamfightAdvice = teamfightAdvice,
            splitPushAdvice = splitPushAdvice,
            carryTarget = carryTarget,
            detectedUIMode = detectedMode,
            itemRecommendations = itemRecommendations
        )

        return TacticalEvaluationResult(
            newState = newState,
            voiceCallout = voiceCallout,
            calloutTag = calloutTag,
            calloutPriority = calloutPriority
        )
    }
}
