package com.numan.wcmp.feature.protectedapps

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.numan.wcmp.data.repository.ProtectedAppsRepository
import com.numan.wcmp.feature.protectedapps.model.ProtectedApp
import com.numan.wcmp.feature.protectedapps.model.ProtectedAppCategory
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class ProtectedAppsViewModel(
    private val repository: ProtectedAppsRepository
) : ViewModel() {

    private val _installedApps = repository.observeInstalledApps()

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    private val _selectedCategory = MutableStateFlow(ProtectedAppCategory.ALL)
    val selectedCategory: StateFlow<ProtectedAppCategory> = _selectedCategory.asStateFlow()

    val filteredApps: StateFlow<List<ProtectedApp>> = combine(
        _installedApps,
        _searchQuery,
        _selectedCategory
    ) { apps, query, category ->
        var result = apps

        // Filter by Category
        if (category != ProtectedAppCategory.ALL) {
            result = result.filter { app ->
                when (category) {
                    ProtectedAppCategory.PROTECTED -> app.isProtected
                    ProtectedAppCategory.UNPROTECTED -> !app.isProtected
                    else -> app.category == category
                }
            }
        }

        // Filter by Search Query
        if (query.isNotBlank()) {
            val lowercaseQuery = query.lowercase()
            result = result.filter { app ->
                app.name.lowercase().contains(lowercaseQuery) || 
                app.packageName.lowercase().contains(lowercaseQuery)
            }
        }

        result.sortedBy { it.name.lowercase() }
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )
    
    val protectedAppCount: StateFlow<Int> = _installedApps.map { apps ->
        apps.count { it.isProtected }
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = 0
    )

    val totalInstalledCount: StateFlow<Int> = _installedApps.map { apps ->
        apps.size
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = 0
    )

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error.asStateFlow()

    init {
        refreshApps()
    }

    fun refreshApps() {
        viewModelScope.launch {
            _isLoading.update { true }
            _error.update { null }
            try {
                repository.refreshInstalledApps()
            } catch (e: Exception) {
                _error.update { "Unable to load installed applications. Please try again." }
            } finally {
                _isLoading.update { false }
            }
        }
    }

    fun setSearchQuery(query: String) {
        _searchQuery.value = query
    }

    fun setCategory(category: ProtectedAppCategory) {
        _selectedCategory.value = category
    }

    fun setProtected(packageName: String, isProtected: Boolean) {
        viewModelScope.launch {
            repository.setProtected(packageName, isProtected)
        }
    }

    class Factory(
        private val repository: ProtectedAppsRepository
    ) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return ProtectedAppsViewModel(repository) as T
        }
    }
}
