package com.example.data.repository

import com.example.data.local.MatchDao
import com.example.model.MatchRecord
import com.example.model.MatchResult
import com.example.model.TacticalLogEntity
import kotlinx.coroutines.flow.Flow

class MatchRepository(private val matchDao: MatchDao) {

    val allMatches: Flow<List<MatchRecord>> = matchDao.getAllMatches()
    val totalMatchesCount: Flow<Int> = matchDao.getMatchCount()
    val totalVictoriesCount: Flow<Int> = matchDao.getVictoryCount()

    fun getLogsForMatch(matchId: Long): Flow<List<TacticalLogEntity>> =
        matchDao.getLogsForMatch(matchId)

    suspend fun getMatchById(id: Long): MatchRecord? = matchDao.getMatchById(id)

    suspend fun insertMatch(match: MatchRecord): Long = matchDao.insertMatch(match)

    suspend fun insertTacticalLogs(logs: List<TacticalLogEntity>) =
        matchDao.insertTacticalLogs(logs)

    suspend fun deleteMatch(id: Long) = matchDao.deleteMatchById(id)

    suspend fun clearHistory() = matchDao.deleteAllMatches()

    suspend fun seedSampleMatchesIfEmpty() {
        val sampleMatches = listOf(
            MatchRecord(
                heroUsed = "Florentino",
                heroRole = "Đấu Sĩ / Đường Tà Thần",
                result = MatchResult.VICTORY,
                durationSeconds = 942, // ~15:42
                finalKDA = "8/2/6",
                finalGoldDiff = 4800,
                recommendationsCount = 14,
                topObjectiveContested = "Tà Thần Caesar & Rồng Krayg",
                coachScore = 94,
                tacticalNotes = "Lên Đao Truy Hồn khắc chế hồi máu của Taara và ăn Caesar Bạo Chúa định đoạt trận đấu."
            ),
            MatchRecord(
                heroUsed = "Violet",
                heroRole = "Xạ Thủ (AD) / Đường Rồng",
                result = MatchResult.VICTORY,
                durationSeconds = 810, // 13:30
                finalKDA = "11/1/8",
                finalGoldDiff = 6200,
                recommendationsCount = 18,
                topObjectiveContested = "Trụ Đường Giữa & Dơi Thủ Vệ",
                coachScore = 96,
                tacticalNotes = "Giữ vị trí xuất sắc sau Đỡ Đòn, theo dõi cảnh báo Sát Thủ địch đảo đường."
            ),
            MatchRecord(
                heroUsed = "Nakroth",
                heroRole = "Sát Thủ / Đi Rừng",
                result = MatchResult.DEFEAT,
                durationSeconds = 1120, // 18:40
                finalKDA = "6/5/4",
                finalGoldDiff = -3200,
                recommendationsCount = 21,
                topObjectiveContested = "Caesar Bạo Chúa",
                coachScore = 78,
                tacticalNotes = "Dâng cao cướp rừng khi toàn đội đang thua 3.5k Vàng. Cần chú ý cảnh báo rút lui."
            ),
            MatchRecord(
                heroUsed = "Tulen",
                heroRole = "Pháp Sư (Mid) / Đi Rừng",
                result = MatchResult.VICTORY,
                durationSeconds = 760, // 12:40
                finalKDA = "9/0/11",
                finalGoldDiff = 5100,
                recommendationsCount = 12,
                topObjectiveContested = "Rồng Ánh Sáng Đầu Trận",
                coachScore = 98,
                tacticalNotes = "Đảo đường gank chuẩn xác theo tín hiệu radar kẻ địch biến mất ở sông."
            )
        )

        for (m in sampleMatches) {
            val id = matchDao.insertMatch(m)
            val sampleLogs = listOf(
                TacticalLogEntity(
                    matchId = id,
                    timestampSeconds = 120,
                    eventType = "MỤC TIÊU",
                    calloutText = "Dơi Thủ Vệ xuất hiện. Đấu Sĩ chú ý tranh chấp.",
                    priorityLevel = 1
                ),
                TacticalLogEntity(
                    matchId = id,
                    timestampSeconds = 240,
                    eventType = "CẢNH BÁO",
                    calloutText = "Rừng địch di chuyển xuống bụi cỏ Sông gần đường Rồng.",
                    priorityLevel = 2
                ),
                TacticalLogEntity(
                    matchId = id,
                    timestampSeconds = 480,
                    eventType = "TRANG BỊ",
                    calloutText = "Khuyến nghị lên Huân Chương Troy chống sốc phép từ 2 Pháp Sư địch.",
                    priorityLevel = 2
                ),
                TacticalLogEntity(
                    matchId = id,
                    timestampSeconds = 720,
                    eventType = "GIAO TRANH",
                    calloutText = "Cửa thắng 5v4! Xạ Thủ địch đã đếm số. Ép thẳng Trụ Đường Giữa!",
                    priorityLevel = 3
                )
            )
            matchDao.insertTacticalLogs(sampleLogs)
        }
    }
}
