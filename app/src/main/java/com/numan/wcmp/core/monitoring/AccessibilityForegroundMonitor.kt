package com.numan.wcmp.core.monitoring

import com.numan.wcmp.domain.security.ForegroundApp
import com.numan.wcmp.domain.security.ForegroundMonitor
import com.numan.wcmp.domain.security.ForegroundMonitorStatus
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

object AccessibilityForegroundMonitor : ForegroundMonitor {
    private val lock = Any()

    private val _status = MutableStateFlow(ForegroundMonitorStatus.STOPPED)
    override val status: StateFlow<ForegroundMonitorStatus> = _status.asStateFlow()

    private val _foregroundApp = MutableStateFlow<ForegroundApp?>(null)
    override val foregroundApp: StateFlow<ForegroundApp?> = _foregroundApp.asStateFlow()

    private var isRequestedToRun = false
    private var isServiceBound = false

    override fun start() {
        synchronized(lock) {
            isRequestedToRun = true
            updateStatusLocked()
        }
    }

    override fun stop() {
        synchronized(lock) {
            isRequestedToRun = false
            _foregroundApp.value = null
            updateStatusLocked()
        }
    }

    fun onServiceConnected() {
        synchronized(lock) {
            isServiceBound = true
            updateStatusLocked()
        }
    }

    fun onServiceDisconnected() {
        synchronized(lock) {
            isServiceBound = false
            _foregroundApp.value = null
            updateStatusLocked()
        }
    }

    fun onForegroundPackageChanged(packageName: CharSequence?, className: String? = null) {
        val pkg = packageName?.toString()?.takeIf { it.isNotBlank() } ?: return
        synchronized(lock) {
            if (!isRequestedToRun || _status.value != ForegroundMonitorStatus.RUNNING) return
            _foregroundApp.value = ForegroundApp(
                packageName = pkg,
                observedAt = System.currentTimeMillis()
            )
        }
    }

    private fun updateStatusLocked() {
        _status.value = when {
            !isRequestedToRun -> ForegroundMonitorStatus.STOPPED
            !isServiceBound -> ForegroundMonitorStatus.REQUIRES_ACCESS
            else -> ForegroundMonitorStatus.RUNNING
        }
    }
}
