package com.example.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.ArenaCoachApp
import com.example.model.MatchRecord
import com.example.model.MatchResult
import com.example.model.TacticalLogEntity
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

data class MatchStats(
    val totalMatches: Int = 0,
    val totalWins: Int = 0,
    val winRatePercent: Int = 0,
    val avgScore: Int = 0
)

class HistoryViewModel(application: Application) : AndroidViewModel(application) {

    private val matchRepository = (application as ArenaCoachApp).matchRepository

    val matches: StateFlow<List<MatchRecord>> = matchRepository.allMatches.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    private val _selectedMatch = MutableStateFlow<MatchRecord?>(null)
    val selectedMatch: StateFlow<MatchRecord?> = _selectedMatch.asStateFlow()

    private val _selectedMatchLogs = MutableStateFlow<List<TacticalLogEntity>>(emptyList())
    val selectedMatchLogs: StateFlow<List<TacticalLogEntity>> = _selectedMatchLogs.asStateFlow()

    val totalMatches: StateFlow<Int> = matches.map { it.size }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = 0
    )

    val winRate: StateFlow<Int> = matches.map { list ->
        if (list.isEmpty()) 0
        else {
            val wins = list.count { it.result == MatchResult.VICTORY }
            (wins * 100) / list.size
        }
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = 0
    )

    val averageScore: StateFlow<Int> = matches.map { list ->
        if (list.isEmpty()) 0
        else list.map { it.coachScore }.average().toInt()
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = 0
    )

    val stats: StateFlow<MatchStats> = combine(
        matchRepository.totalMatchesCount,
        matchRepository.totalVictoriesCount,
        matches
    ) { total, wins, list ->
        val winRate = if (total > 0) (wins * 100) / total else 0
        val avgScore = if (list.isNotEmpty()) list.map { it.coachScore }.average().toInt() else 0
        MatchStats(
            totalMatches = total,
            totalWins = wins,
            winRatePercent = winRate,
            avgScore = avgScore
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = MatchStats()
    )

    fun selectMatch(match: MatchRecord) {
        _selectedMatch.value = match
        viewModelScope.launch {
            matchRepository.getLogsForMatch(match.id).collect { logs ->
                _selectedMatchLogs.value = logs
            }
        }
    }

    fun clearSelectedMatch() {
        _selectedMatch.value = null
        _selectedMatchLogs.value = emptyList()
    }

    fun deleteMatch(id: Long) {
        viewModelScope.launch {
            matchRepository.deleteMatch(id)
            if (_selectedMatch.value?.id == id) {
                clearSelectedMatch()
            }
        }
    }

    fun clearHistory() {
        viewModelScope.launch {
            matchRepository.clearHistory()
            clearSelectedMatch()
        }
    }

    fun resetDemoData() {
        viewModelScope.launch {
            matchRepository.clearHistory()
            matchRepository.seedSampleMatchesIfEmpty()
        }
    }
}
