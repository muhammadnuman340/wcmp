package com.numan.wcmp.domain.security

import com.numan.wcmp.data.repository.ProtectedAppsRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

import com.numan.wcmp.domain.protection.AppLockController

class SecurityEngine(
    private val foregroundMonitor: ForegroundMonitor,
    private val protectedAppsRepository: ProtectedAppsRepository,
    private val eventProcessor: SecurityEventProcessor,
    private val appLockController: AppLockController?,
    private val coroutineScope: CoroutineScope,
    private val sessionManager: SecuritySessionManager? = null
) {
    private val _state = MutableStateFlow(SecurityEngineState.DISABLED)
    val state: StateFlow<SecurityEngineState> = _state.asStateFlow()

    private var monitorJob: Job? = null
    private var statusJob: Job? = null

    // Session-based deduplication
    private var currentProtectedSessionPackage: String? = null

    fun start() {
        if (_state.value == SecurityEngineState.RUNNING || _state.value == SecurityEngineState.STARTING) return

        _state.update { SecurityEngineState.STARTING }
        foregroundMonitor.start()
        
        statusJob = coroutineScope.launch {
            foregroundMonitor.status.collect { status ->
                when (status) {
                    ForegroundMonitorStatus.RUNNING -> _state.update { SecurityEngineState.RUNNING }
                    ForegroundMonitorStatus.REQUIRES_ACCESS -> _state.update { SecurityEngineState.WAITING_FOR_ACCESS }
                    ForegroundMonitorStatus.ERROR -> _state.update { SecurityEngineState.ERROR }
                    ForegroundMonitorStatus.STOPPED, ForegroundMonitorStatus.NOT_AVAILABLE -> _state.update { SecurityEngineState.DISABLED }
                    else -> {}
                }
            }
        }

        monitorJob = coroutineScope.launch {
            foregroundMonitor.foregroundApp.collect { app ->
                handleForegroundTransition(app)
            }
        }
    }

    fun stop() {
        _state.update { SecurityEngineState.DISABLED }
        foregroundMonitor.stop()
        monitorJob?.cancel()
        statusJob?.cancel()
        currentProtectedSessionPackage = null
    }

    private suspend fun handleForegroundTransition(app: ForegroundApp?) {
        if (app == null) {
            // User went to launcher or unknown
            val prevSession = currentProtectedSessionPackage
            currentProtectedSessionPackage = null
            if (prevSession != null) {
                appLockController?.onProtectedAppLeft(prevSession)
                sessionManager?.endSession()
            }
            return
        }
        
        // Ignore WCMP itself
        if (app.packageName == "com.numan.wcmp") {
            val prevSession = currentProtectedSessionPackage
            currentProtectedSessionPackage = null
            if (prevSession != null) {
                appLockController?.onProtectedAppLeft(prevSession)
                sessionManager?.endSession()
            }
            return
        }

        // Check if package is protected
        // We use first() to get the current list. In a highly optimized engine we'd cache this.
        val protectedApps = protectedAppsRepository.observeProtectedAppsOnly().first()
        val isProtected = protectedApps.any { it.packageName == app.packageName }

        if (!isProtected) {
            // Unprotected app
            val prevSession = currentProtectedSessionPackage
            currentProtectedSessionPackage = null
            if (prevSession != null) {
                appLockController?.onProtectedAppLeft(prevSession)
                sessionManager?.endSession()
            }
            return
        }

        // Protected app accessed. Check deduplication
        if (currentProtectedSessionPackage == app.packageName) {
            // Already the current protected session, ignore duplicate
            return
        }

        // New protected session!
        currentProtectedSessionPackage = app.packageName
        
        // Populate applicationName if we found it in the repository
        val matchedApp = protectedApps.find { it.packageName == app.packageName }
        val enrichedApp = app.copy(applicationName = matchedApp?.name ?: app.applicationName)
        
        sessionManager?.startSession(
            trigger = "Protected App Access",
            protectedPackage = app.packageName
        )
        
        eventProcessor.processProtectedAppAccess(enrichedApp)
        appLockController?.onProtectedAppAccessed(enrichedApp)
    }
}
