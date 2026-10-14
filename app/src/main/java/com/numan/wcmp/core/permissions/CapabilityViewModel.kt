package com.numan.wcmp.core.permissions

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn

class CapabilityViewModel(private val repository: CapabilityRepository) : ViewModel() {

    val capabilities: StateFlow<List<CapabilityState>> = repository.capabilities
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    fun refreshCapabilities() {
        repository.refreshCapabilities()
    }

    class Factory(private val repository: CapabilityRepository) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return CapabilityViewModel(repository) as T
        }
    }
}
