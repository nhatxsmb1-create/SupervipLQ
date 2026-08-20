package com.example.model

data class WinProbabilityResult(
    val percentage: Int?, // null if insufficient data
    val statusText: String,
    val confidence: Float
)

interface WinProbabilityModel {
    fun calculateWinProbability(gameState: GameState): WinProbabilityResult
}

/**
 * Real feature-based Win Probability model implementation.
 * Returns null / "Tỷ lệ thắng: Chưa đủ dữ liệu" when confidence is insufficient or match is inactive.
 */
class FeatureBasedWinProbabilityModel : WinProbabilityModel {

    override fun calculateWinProbability(gameState: GameState): WinProbabilityResult {
        if (gameState.matchActive != true || gameState.overallConfidence < 0.50f) {
            return WinProbabilityResult(
                percentage = null,
                statusText = "Tỷ lệ thắng: Chưa đủ dữ liệu",
                confidence = 0f
            )
        }

        val goldDiff = gameState.goldDifference ?: 0
        val allyKills = gameState.allyKills ?: 0
        val enemyKills = gameState.enemyKills ?: 0
        val timeSec = gameState.matchTimeSeconds ?: 0

        // Base 50% shift according to gold advantage and kills ratio
        val goldWeight = (goldDiff / 1000f) * 2.5f // Each 1k gold = 2.5%
        val killDiff = allyKills - enemyKills
        val killWeight = killDiff * 1.5f

        val calculatedRate = (50f + goldWeight + killWeight).toInt().coerceIn(10, 95)

        return WinProbabilityResult(
            percentage = calculatedRate,
            statusText = "Tỷ lệ thắng: $calculatedRate%",
            confidence = gameState.overallConfidence
        )
    }
}
