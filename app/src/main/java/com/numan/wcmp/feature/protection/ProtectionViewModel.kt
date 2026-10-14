package com.numan.wcmp.feature.protection

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.numan.wcmp.core.permissions.AndroidCapability
import com.numan.wcmp.core.permissions.CapabilityRepository
import com.numan.wcmp.core.permissions.CapabilityState
import com.numan.wcmp.data.WcmpRepositoryProvider
import com.numan.wcmp.data.repository.ProtectedAppsRepository
import com.numan.wcmp.domain.security.SecurityEngine
import com.numan.wcmp.domain.security.SecurityEngineState
import com.numan.wcmp.domain.security.SecuritySession
import com.numan.wcmp.domain.security.SecuritySessionManager
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

enum class ProtectionMode {
    QUICK_PROTECT, ALWAYS_PROTECT, SCHEDULED_PROTECT, SMART_PROTECT, CUSTOM_RULES
}

data class ProtectionUiState(
    val engineState: SecurityEngineState = SecurityEngineState.DISABLED,
    val activeSession: SecuritySession? = null,
    val selectedMode: ProtectionMode = ProtectionMode.QUICK_PROTECT,
    val protectedAppCount: Int = 0,
    val capabilities: List<CapabilityState> = emptyList()
) {
    val isProtectionActive: Boolean
        get() = engineState == SecurityEngineState.RUNNING
        
    val requiresAccess: Boolean
        get() = engineState == SecurityEngineState.WAITING_FOR_ACCESS
        
    fun getCapabilityState(capability: AndroidCapability): CapabilityState? {
        return capabilities.find { it.capability == capability }
    }
}

class ProtectionViewModel(
    private val securityEngine: SecurityEngine,
    private val sessionManager: SecuritySessionManager,
    private val capabilityRepository: CapabilityRepository,
    private val protectedAppsRepository: ProtectedAppsRepository
) : ViewModel() {

    private val _selectedMode = MutableStateFlow(ProtectionMode.QUICK_PROTECT)

    val uiState: StateFlow<ProtectionUiState> = combine(
        securityEngine.state,
        sessionManager.currentSession,
        _selectedMode,
        protectedAppsRepository.observeProtectedAppsOnly().map { it.size },
        capabilityRepository.capabilities
    ) { engineState, session, mode, appCount, capabilities ->
        ProtectionUiState(
            engineState = engineState,
            activeSession = session,
            selectedMode = mode,
            protectedAppCount = appCount,
            capabilities = capabilities
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = ProtectionUiState()
    )

    init {
        refreshCapabilities()
    }

    fun startProtection() {
        securityEngine.start()
    }

    fun stopProtection() {
        securityEngine.stop()
    }

    fun setMode(mode: ProtectionMode) {
        _selectedMode.value = mode
    }

    fun refreshCapabilities() {
        viewModelScope.launch {
            capabilityRepository.refreshCapabilities()
        }
    }

    class Factory(private val context: Context) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            val appCtx = context.applicationContext
            return ProtectionViewModel(
                WcmpRepositoryProvider.getSecurityEngine(appCtx),
                WcmpRepositoryProvider.getSecuritySessionManager(),
                WcmpRepositoryProvider.getCapabilityRepository(appCtx),
                WcmpRepositoryProvider.getProtectedAppsRepository(appCtx)
            ) as T
        }
    }
}
