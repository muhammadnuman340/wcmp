package com.numan.wcmp.feature.settings.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.numan.wcmp.core.permissions.CapabilityRepository
import com.numan.wcmp.core.permissions.CapabilityState
import com.numan.wcmp.data.repository.SecurityRepository
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

data class SettingsState(
    val protectionMode: String = "Unknown",
    val autoProtectionEnabled: Boolean = false,
    val motionDetectionEnabled: Boolean = false,
    val notificationAlertsEnabled: Boolean = false,
    val animationsEnabled: Boolean = true,
    val cameraEvidenceEnabled: Boolean = false,
    val audioEvidenceEnabled: Boolean = false
)

class SettingsViewModel(
    private val securityRepository: SecurityRepository,
    private val capabilityRepository: CapabilityRepository
) : ViewModel() {

    val capabilities: StateFlow<List<CapabilityState>> = capabilityRepository.capabilities
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    val settingsState: StateFlow<SettingsState> = combine(
        securityRepository.protectionMode,
        securityRepository.autoProtectionEnabled,
        securityRepository.motionDetectionEnabled,
        securityRepository.notificationAlertsEnabled,
        securityRepository.animationsEnabled
    ) { mode, auto, motion, notif, anim ->
        SettingsState(
            protectionMode = mode,
            autoProtectionEnabled = auto,
            motionDetectionEnabled = motion,
            notificationAlertsEnabled = notif,
            animationsEnabled = anim,
            cameraEvidenceEnabled = false,
            audioEvidenceEnabled = false
        )
    }.combine(securityRepository.cameraEvidenceEnabled) { state, cam ->
        state.copy(cameraEvidenceEnabled = cam)
    }.combine(securityRepository.audioEvidenceEnabled) { state, aud ->
        state.copy(audioEvidenceEnabled = aud)
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = SettingsState()
    )

    fun refreshCapabilities() {
        capabilityRepository.refreshCapabilities()
    }
    
    fun setProtectionMode(mode: String) {
        viewModelScope.launch { securityRepository.setProtectionMode(mode) }
    }
    
    fun setAutoProtection(enabled: Boolean) {
        viewModelScope.launch { securityRepository.setAutoProtection(enabled) }
    }
    
    fun setNotificationAlerts(enabled: Boolean) {
        viewModelScope.launch { securityRepository.setNotificationAlerts(enabled) }
    }
    
    fun setAnimations(enabled: Boolean) {
        viewModelScope.launch { securityRepository.setAnimations(enabled) }
    }

    class Factory(
        private val securityRepository: SecurityRepository,
        private val capabilityRepository: CapabilityRepository
    ) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return SettingsViewModel(securityRepository, capabilityRepository) as T
        }
    }
}
