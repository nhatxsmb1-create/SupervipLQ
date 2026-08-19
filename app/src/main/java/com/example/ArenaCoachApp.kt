package com.example

import android.app.Application
import com.example.data.local.AppDatabase
import com.example.data.repository.MatchRepository
import com.example.data.repository.UserPreferencesRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class ArenaCoachApp : Application() {

    lateinit var database: AppDatabase
        private set

    lateinit var matchRepository: MatchRepository
        private set

    lateinit var userPreferencesRepository: UserPreferencesRepository
        private set

    override fun onCreate() {
        super.onCreate()
        database = AppDatabase.getDatabase(this)
        matchRepository = MatchRepository(database.matchDao())
        userPreferencesRepository = UserPreferencesRepository(this)

        // Preload sample match data if empty for instant interactive stats & testing
        CoroutineScope(Dispatchers.IO).launch {
            try {
                matchRepository.seedSampleMatchesIfEmpty()
            } catch (_: Exception) {}
        }
    }
}
