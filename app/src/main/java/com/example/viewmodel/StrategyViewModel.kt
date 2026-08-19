package com.example.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.model.GameItem
import com.example.model.HeroDatabase
import com.example.model.HeroInfo
import com.example.model.ItemDatabase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn

class StrategyViewModel : ViewModel() {

    private val _selectedTab = MutableStateFlow(0)
    val selectedTab: StateFlow<Int> = _selectedTab.asStateFlow()

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    private val _selectedRole = MutableStateFlow("All")
    val selectedRole: StateFlow<String> = _selectedRole.asStateFlow()

    private val _selectedHero = MutableStateFlow<HeroInfo?>(null)
    val selectedHero: StateFlow<HeroInfo?> = _selectedHero.asStateFlow()

    private val _selectedItemCategory = MutableStateFlow("All")
    val selectedItemCategory: StateFlow<String> = _selectedItemCategory.asStateFlow()

    val filteredHeroes: StateFlow<List<HeroInfo>> = combine(
        _searchQuery,
        _selectedRole
    ) { query, role ->
        HeroDatabase.heroes.filter { hero ->
            val matchesQuery = query.isEmpty() ||
                    hero.name.contains(query, ignoreCase = true) ||
                    hero.role.contains(query, ignoreCase = true) ||
                    hero.counterStrategy.contains(query, ignoreCase = true)
            val matchesRole = role == "All" || hero.role.contains(role, ignoreCase = true)
            matchesQuery && matchesRole
        }
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = HeroDatabase.heroes
    )

    val filteredItems: StateFlow<List<GameItem>> = combine(
        _searchQuery,
        _selectedItemCategory
    ) { query, category ->
        ItemDatabase.items.filter { item ->
            val matchesQuery = query.isEmpty() ||
                    item.name.contains(query, ignoreCase = true) ||
                    item.counterPurpose.contains(query, ignoreCase = true) ||
                    item.category.contains(query, ignoreCase = true)
            val matchesCat = category == "All" || item.category.contains(category, ignoreCase = true)
            matchesQuery && matchesCat
        }
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = ItemDatabase.items
    )

    fun setTab(tabIndex: Int) {
        _selectedTab.value = tabIndex
        _searchQuery.value = ""
    }

    fun setSearchQuery(query: String) {
        _searchQuery.value = query
    }

    fun setSelectedRole(role: String) {
        _selectedRole.value = role
    }

    fun selectHero(hero: HeroInfo?) {
        _selectedHero.value = hero
    }

    fun setSelectedItemCategory(category: String) {
        _selectedItemCategory.value = category
    }
}
