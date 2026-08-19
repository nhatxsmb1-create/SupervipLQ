package com.example.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.model.MatchRecord
import com.example.model.TacticalLogEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface MatchDao {
    @Query("SELECT * FROM matches ORDER BY dateTimestamp DESC")
    fun getAllMatches(): Flow<List<MatchRecord>>

    @Query("SELECT * FROM matches WHERE id = :id LIMIT 1")
    suspend fun getMatchById(id: Long): MatchRecord?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMatch(match: MatchRecord): Long

    @Query("DELETE FROM matches WHERE id = :id")
    suspend fun deleteMatchById(id: Long)

    @Query("DELETE FROM matches")
    suspend fun deleteAllMatches()

    @Query("SELECT * FROM tactical_logs WHERE matchId = :matchId ORDER BY timestampSeconds ASC")
    fun getLogsForMatch(matchId: Long): Flow<List<TacticalLogEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTacticalLog(log: TacticalLogEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTacticalLogs(logs: List<TacticalLogEntity>)

    @Query("SELECT COUNT(*) FROM matches")
    fun getMatchCount(): Flow<Int>

    @Query("SELECT COUNT(*) FROM matches WHERE result = 'VICTORY'")
    fun getVictoryCount(): Flow<Int>
}
