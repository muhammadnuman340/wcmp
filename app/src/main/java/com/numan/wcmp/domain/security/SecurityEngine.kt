package com.numan.wcmp.domain.security

import com.numan.wcmp.data.repository.ProtectedAppsRepository
import com.numan.wcmp.domain.protection.AppLockController
import com.numan.wcmp.domain.protection.AppLockState
import com.numan.wcmp.feature.protectedapps.model.ProtectedApp
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock

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

    private val transitionMutex = Mutex()
    private val lifecycleLock = Any()

    private var monitorJob: Job? = null
    private var statusJob: Job? = null
    private var protectedAppsCacheJob: Job? = null

    @Volatile
    private var cachedProtectedApps: Map<String, ProtectedApp> = emptyMap()

    @Volatile
    private var currentProtectedSessionPackage: String? = null

    @Volatile
    var engineArmedAtTimestamp: Long? = null
        private set

    fun start() {
        synchronized(lifecycleLock) {
            if (monitorJob?.isActive == true && statusJob?.isActive == true) {
                foregroundMonitor.start()
                return
            }

            monitorJob?.cancel()
            statusJob?.cancel()
            protectedAppsCacheJob?.cancel()

            _state.update { SecurityEngineState.STARTING }
            if (engineArmedAtTimestamp == null) {
                engineArmedAtTimestamp = System.currentTimeMillis()
            }
            foregroundMonitor.start()

            protectedAppsCacheJob = coroutineScope.launch {
                protectedAppsRepository.observeProtectedAppsOnly().collect { list ->
                    cachedProtectedApps = list.associateBy { it.packageName }
                }
            }

            statusJob = coroutineScope.launch {
                foregroundMonitor.status.collect { status ->
                    when (status) {
                        ForegroundMonitorStatus.RUNNING -> _state.update {
                            SecurityEngineState.RUNNING
                        }
                        ForegroundMonitorStatus.REQUIRES_ACCESS -> _state.update {
                            SecurityEngineState.WAITING_FOR_ACCESS
                        }
                        ForegroundMonitorStatus.ERROR -> _state.update {
                            SecurityEngineState.ERROR
                        }
                        ForegroundMonitorStatus.STOPPED,
                        ForegroundMonitorStatus.NOT_AVAILABLE -> {
                            _state.update { SecurityEngineState.DISABLED }
                        }
                        else -> {}
                    }
                }
            }

            monitorJob = coroutineScope.launch {
                foregroundMonitor.foregroundApp.collect { app ->
                    transitionMutex.withLock {
                        handleForegroundTransition(app)
                    }
                }
            }
        }
    }

    fun stop() {
        synchronized(lifecycleLock) {
            _state.update { SecurityEngineState.DISABLED }
            engineArmedAtTimestamp = null
            foregroundMonitor.stop()
            monitorJob?.cancel()
            statusJob?.cancel()
            protectedAppsCacheJob?.cancel()
            monitorJob = null
            statusJob = null
            protectedAppsCacheJob = null
            currentProtectedSessionPackage = null
            sessionManager?.endSession(SecuritySessionStatus.CANCELLED)
        }
    }

    private suspend fun handleForegroundTransition(app: ForegroundApp?) {
        if (app == null) {
            endActiveProtectedSession()
            return
        }

        // If WCMP itself is in the foreground, check if AppLockController is actively locking/authenticating.
        // Do NOT tear down the active protected session while AppLockActivity is prompting for biometrics.
        if (app.packageName == "com.numan.wcmp") {
            val lockState = appLockController?.state?.value
            if (lockState == AppLockState.LOCK_REQUIRED || lockState == AppLockState.AUTHENTICATING) {
                return
            }
            endActiveProtectedSession()
            return
        }

        val matchedApp = cachedProtectedApps[app.packageName]
        if (matchedApp == null) {
            endActiveProtectedSession()
            return
        }

        if (currentProtectedSessionPackage == app.packageName) {
            return
        }

        // If transitioning directly from Protected App A to Protected App B, close Session A first
        if (currentProtectedSessionPackage != null && currentProtectedSessionPackage != app.packageName) {
            endActiveProtectedSession()
        }

        currentProtectedSessionPackage = app.packageName
        val enrichedApp = app.copy(applicationName = matchedApp.name)

        sessionManager?.startSession(
            trigger = "Protected App Access",
            protectedPackage = app.packageName
        )
        eventProcessor.processProtectedAppAccess(enrichedApp)
        appLockController?.onProtectedAppAccessed(enrichedApp)
    }

    private fun endActiveProtectedSession() {
        val prevSession = currentProtectedSessionPackage
        currentProtectedSessionPackage = null
        if (prevSession != null) {
            appLockController?.onProtectedAppLeft(prevSession)
            sessionManager?.endSession()
        }
    }
}
