package com.example.service

import com.example.debug.DebugState
import com.example.model.CoachStatus
import com.example.model.DangerLevel
import com.example.model.DetectedScreenMode
import com.example.model.GameState
import com.example.model.ItemRecommendation
import com.example.model.ObjectiveTarget
import com.example.model.TacticalState
import com.example.model.ThreatPlayer
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

object CoachStateHub {

    private val _tacticalState = MutableStateFlow(
        TacticalState(
            coachStatus = CoachStatus.IN_MATCH_READY,
            gameDetected = true,
            matchStarted = true,
            gameDataValid = true,
            analysisReady = true,
            matchTimeSeconds = 120,
            winProbability = 55,
            currentObjective = ObjectiveTarget.ABYSSAL_DRAGON,
            dangerWarning = "Chú ý Rừng đối phương gank đường ở mốc 01:30 - 02:00. Kiểm tra bụi cỏ!",
            dangerLevel = DangerLevel.MEDIUM,
            teamGoldDiff = 300,
            teamfightAdvice = "Tập trung giao tranh quanh hang Rồng Krayg ở phút 02:00. Khép góc Xạ Thủ đối phương!",
            itemRecommendations = listOf(
                ItemRecommendation("Giày Kiên Cường", "Kháng hiệu ứng khống chế của team bạn", "Khắc Chế", 700, isCounter = true),
                ItemRecommendation("Đao Truy Hồn", "Khắc chế hồi máu", "Khắc Chế", 2000, isCounter = true)
            )
        )
    )
    val tacticalState: StateFlow<TacticalState> = _tacticalState.asStateFlow()

    private val _gameState = MutableStateFlow(GameState())
    val gameState: StateFlow<GameState> = _gameState.asStateFlow()

    private val _debugState = MutableStateFlow(DebugState())
    val debugState: StateFlow<DebugState> = _debugState.asStateFlow()

    private val _isServiceRunning = MutableStateFlow(false)
    val isServiceRunning: StateFlow<Boolean> = _isServiceRunning.asStateFlow()

    private val _isOverlayExpanded = MutableStateFlow(true)
    val isOverlayExpanded: StateFlow<Boolean> = _isOverlayExpanded.asStateFlow()

    fun updateState(newState: TacticalState) {
        _tacticalState.value = newState
    }

    fun updateGameState(newGameState: GameState) {
        _gameState.value = newGameState
        _debugState.value = _debugState.value.copy(gameState = newGameState)
    }

    fun setDebugHudEnabled(enabled: Boolean) {
        _debugState.value = _debugState.value.copy(isEnabled = enabled)
    }

    fun setServiceRunning(running: Boolean) {
        _isServiceRunning.value = running
    }

    fun toggleOverlayExpanded() {
        _isOverlayExpanded.value = !_isOverlayExpanded.value
    }

    fun setOverlayExpanded(expanded: Boolean) {
        _isOverlayExpanded.value = expanded
    }

    fun toggleVoiceMute() {
        val current = _tacticalState.value
        _tacticalState.value = current.copy(isVoiceMuted = !current.isVoiceMuted)
    }

    fun setVoiceMuted(muted: Boolean) {
        val current = _tacticalState.value
        _tacticalState.value = current.copy(isVoiceMuted = muted)
    }

    fun resetToIdle() {
        val current = _tacticalState.value
        _tacticalState.value = current.copy(
            coachStatus = CoachStatus.IN_MATCH_READY,
            gameDetected = true,
            matchStarted = true,
            gameDataValid = true,
            analysisReady = true,
            matchTimeSeconds = 0,
            winProbability = 52,
            currentObjective = ObjectiveTarget.ABYSSAL_DRAGON,
            dangerWarning = "Đã đặt lại đồng hồ trận đấu (00:00). Chuẩn bị farm đợt lính đầu tiên!",
            dangerLevel = DangerLevel.SAFE,
            teamGoldDiff = 0,
            teamfightAdvice = "Kiểm soát đường và theo dõi bước di chuyển của Rừng team bạn.",
            itemRecommendations = listOf(
                ItemRecommendation("Giày Kiên Cường", "Kháng hiệu ứng khống chế", "Khắc Chế", 700, isCounter = true)
            )
        )
    }

    private var currentScenarioIndex = 0

    fun advanceSimulationScenario() {
        applyPresetScenario(currentScenarioIndex)
        currentScenarioIndex = (currentScenarioIndex + 1) % 5
    }

    /**
     * Các tình huống mô phỏng thực chiến mẫu
     */
    fun applyPresetScenario(scenarioIndex: Int) {
        when (scenarioIndex) {
            0 -> { // Đầu trận & Cảnh báo bị gank
                _tacticalState.value = TacticalState(
                    coachStatus = CoachStatus.IN_MATCH_READY,
                    gameDetected = true,
                    matchStarted = true,
                    gameDataValid = true,
                    analysisReady = true,
                    matchTimeSeconds = 115,
                    winProbability = 52,
                    currentObjective = ObjectiveTarget.SPIRIT_SENTINEL,
                    dangerWarning = "Rừng địch vừa đạt Cấp 4 — Nguy hiểm ở bụi cỏ Sông!",
                    dangerLevel = DangerLevel.HIGH,
                    teamGoldDiff = 300,
                    allyKills = 2,
                    enemyKills = 1,
                    allyTowers = 0,
                    enemyTowers = 0,
                    teamfightAdvice = "Không dâng cao qua sông khi Trợ Thủ chưa có tầm nhìn",
                    splitPushAdvice = "Dọn nhanh đợt lính rồi đảo đường hỗ trợ Mid",
                    carryTarget = "Nakroth (Cấp 4 - Đang rình gank)",
                    detectedUIMode = DetectedScreenMode.IDLE,
                    captureFpsMode = "Mô phỏng (1 frame / 5s)",
                    lastOcrText = "Thời gian: 01:55 | Vàng: 3.2k vs 2.9k"
                )
            }
            1 -> { // Giữa trận tranh chấp Rồng (Kém tiền)
                _tacticalState.value = TacticalState(
                    coachStatus = CoachStatus.IN_MATCH_READY,
                    gameDetected = true,
                    matchStarted = true,
                    gameDataValid = true,
                    analysisReady = true,
                    matchTimeSeconds = 380,
                    winProbability = 41,
                    currentObjective = ObjectiveTarget.ABYSSAL_DRAGON,
                    dangerWarning = "Đang kém 4,200 Vàng! TUYỆT ĐỐI KHÔNG ép combat 5v5 tại hang Rồng",
                    dangerLevel = DangerLevel.CRITICAL,
                    teamGoldDiff = -4200,
                    allyKills = 4,
                    enemyKills = 10,
                    allyTowers = 1,
                    enemyTowers = 3,
                    teamfightAdvice = "Tránh giao tranh - Bỏ Rồng và farm an toàn bãi lính gần Trụ",
                    splitPushAdvice = "Đường trên đẩy lẻ tạo áp lực buộc địch về thủ",
                    carryTarget = "Violet (Xạ Thủ 6/0/2 - Sát thương cực lớn)",
                    detectedUIMode = DetectedScreenMode.COMBAT,
                    captureFpsMode = "Giao Tranh (1 frame / 1s)",
                    lastOcrText = "Thời gian: 06:20 | Vàng: 14.8k vs 19.0k"
                )
            }
            2 -> { // Mở Cửa Hàng Phân Tích Đồ Khắc Chế
                _tacticalState.value = TacticalState(
                    coachStatus = CoachStatus.IN_MATCH_READY,
                    gameDetected = true,
                    matchStarted = true,
                    gameDataValid = true,
                    analysisReady = true,
                    matchTimeSeconds = 450,
                    winProbability = 58,
                    currentObjective = ObjectiveTarget.MID_TOWER,
                    dangerWarning = "Phát hiện Cửa Hàng: Khuyến nghị lên Huân Chương Troy chống sốc phép",
                    dangerLevel = DangerLevel.SAFE,
                    teamGoldDiff = 1800,
                    allyKills = 8,
                    enemyKills = 6,
                    allyTowers = 2,
                    enemyTowers = 1,
                    teamfightAdvice = "Lên Huân Chương Troy sớm để chịu Lôi Quang từ Tulen",
                    splitPushAdvice = "Ép lính Đường Giữa vào Trụ 1",
                    carryTarget = "Tulen (Pháp Sư sốc điện)",
                    detectedUIMode = DetectedScreenMode.SHOP_OPEN,
                    captureFpsMode = "Cửa Hàng (Phân tích ngay)",
                    itemRecommendations = listOf(
                        ItemRecommendation("Huân Chương Troy", "Kháng sốc sát thương phép từ Tulen / Veera", "Lên Đồ Khắc Chế", 2220, isCounter = true),
                        ItemRecommendation("Đao Truy Hồn", "Giảm 50% hồi máu của Veres và Taara", "Lên Đồ Khắc Chế", 2000, isCounter = true),
                        ItemRecommendation("Thánh Kiếm", "Tăng sát thương chí mạng bùng nổ", "Trang Bị Cốt Lõi", 2000)
                    ),
                    lastOcrText = "Cửa hàng phát hiện: Vàng hiện có 2,340 Vàng"
                )
            }
            3 -> { // Bảng Điểm Hơn Người 5v4
                _tacticalState.value = TacticalState(
                    coachStatus = CoachStatus.IN_MATCH_READY,
                    gameDetected = true,
                    matchStarted = true,
                    gameDataValid = true,
                    analysisReady = true,
                    matchTimeSeconds = 620,
                    winProbability = 68,
                    currentObjective = ObjectiveTarget.DARK_SLAYER,
                    dangerWarning = "Phân tích Bảng Điểm: Cửa thắng 5v4 (Xạ Thủ địch đang đếm số 30s)",
                    dangerLevel = DangerLevel.SAFE,
                    teamGoldDiff = 5400,
                    allyKills = 16,
                    enemyKills = 9,
                    allyTowers = 4,
                    enemyTowers = 2,
                    teamfightAdvice = "Khởi động Tà Thần Caesar ngay! Mina khống chế chặn đường Rừng địch cướp",
                    splitPushAdvice = "Đường Rồng có lính cao ép vào Trụ trong",
                    carryTarget = "Nakroth (Mục tiêu cần khống chế)",
                    detectedUIMode = DetectedScreenMode.SCOREBOARD_OPEN,
                    captureFpsMode = "Bảng Điểm (Phân tích ngay)",
                    topThreats = listOf(
                        ThreatPlayer("Violet", "Xạ Thủ (AD)", "6/3/4", 7100, "CAO", "Hồi sinh sau 28s - Ăn Caesar NGAY"),
                        ThreatPlayer("Nakroth", "Đi Rừng", "4/4/2", 6800, "TRUNG BÌNH", "Cảnh giác Trừng Trị cướp bùa")
                    ),
                    lastOcrText = "Bảng điểm: Tổng vàng 38.2k vs 32.8k"
                )
            }
            4 -> { // Cuối Trận Thủ Trụ Siêu Cấp
                _tacticalState.value = TacticalState(
                    coachStatus = CoachStatus.IN_MATCH_READY,
                    gameDetected = true,
                    matchStarted = true,
                    gameDataValid = true,
                    analysisReady = true,
                    matchTimeSeconds = 1040,
                    winProbability = 36,
                    currentObjective = ObjectiveTarget.HIGH_GROUND_DEFENSE,
                    dangerWarning = "Caesar Bạo Chúa đang tiến vào Trụ Siêu Cấp Đường Giữa!",
                    dangerLevel = DangerLevel.CRITICAL,
                    teamGoldDiff = -6800,
                    allyKills = 14,
                    enemyKills = 22,
                    allyTowers = 2,
                    enemyTowers = 7,
                    teamfightAdvice = "Tập trung toàn lực dọn Rồng Caesar trước! Không tràn ra ngoài Trụ",
                    splitPushAdvice = "Không đi lẻ - Bắt buộc tụ hợp 5 người thủ nhà",
                    carryTarget = "Violet & Elsu (Đứng sau cấu rỉa)",
                    detectedUIMode = DetectedScreenMode.COMBAT,
                    captureFpsMode = "Giao Tranh (1 frame / 1s)",
                    itemRecommendations = listOf(
                        ItemRecommendation("Giáp Hộ Mệnh", "Bắt buộc hồi sinh để thủ nhà chính", "Tình Huống", 2080, isCounter = true),
                        ItemRecommendation("Nham Thuẫn", "Tạo lá chắn lớn sống sót khi bị ép Trụ", "Tình Huống", 1980)
                    ),
                    lastOcrText = "Thời gian: 17:20 | Vàng: 54k vs 60.8k"
                )
            }
        }
    }
}
