package com.example.model

import androidx.room.Entity
import androidx.room.PrimaryKey

enum class MatchResult(val labelVi: String) {
    VICTORY("CHIẾN THẮNG"),
    DEFEAT("THẤT BẠI")
}

@Entity(tableName = "matches")
data class MatchRecord(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val dateTimestamp: Long = System.currentTimeMillis(),
    val heroUsed: String,
    val heroRole: String,
    val result: MatchResult,
    val durationSeconds: Int,
    val finalKDA: String,
    val finalGoldDiff: Int,
    val recommendationsCount: Int,
    val topObjectiveContested: String,
    val coachScore: Int, // 0 - 100
    val tacticalNotes: String
) {
    val formattedDuration: String
        get() {
            val min = durationSeconds / 60
            val sec = durationSeconds % 60
            return "%02d:%02d".format(min, sec)
        }
}

@Entity(tableName = "tactical_logs")
data class TacticalLogEntity(
    @PrimaryKey(autoGenerate = true)
    val logId: Long = 0,
    val matchId: Long,
    val timestampSeconds: Int,
    val eventType: String, // "OBJECTIVE", "DANGER", "ITEM", "GENERAL"
    val calloutText: String,
    val priorityLevel: Int
)
