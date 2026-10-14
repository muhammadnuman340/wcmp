package com.numan.wcmp.core.permissions

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

class CapabilityRepository(
    private val capabilityManager: CapabilityManager
) {
    private val _capabilities = MutableStateFlow<List<CapabilityState>>(emptyList())
    val capabilities: StateFlow<List<CapabilityState>> = _capabilities.asStateFlow()

    init {
        refreshCapabilities()
    }

    fun refreshCapabilities() {
        _capabilities.update { capabilityManager.getAllCapabilityStates() }
    }

    fun getCapabilityState(capability: AndroidCapability): CapabilityState {
        return capabilityManager.getCapabilityState(capability)
    }
}
