package com.numan.wcmp.core.monitoring

import android.content.ComponentName
import com.numan.wcmp.domain.security.ForegroundApp
import com.numan.wcmp.domain.security.ForegroundMonitor
import com.numan.wcmp.domain.security.ForegroundMonitorStatus
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

/**
 * Singleton object bridging the Android AccessibilityService lifecycle 
 * to the generic ForegroundMonitor abstraction.
 */
object AccessibilityForegroundMonitor : ForegroundMonitor {
    private val _status = MutableStateFlow(ForegroundMonitorStatus.STOPPED)
    override val status: StateFlow<ForegroundMonitorStatus> = _status.asStateFlow()

    private val _foregroundApp = MutableStateFlow<ForegroundApp?>(null)
    override val foregroundApp: StateFlow<ForegroundApp?> = _foregroundApp.asStateFlow()

    private var isRequestedToRun = false

    override fun start() {
        isRequestedToRun = true
        updateStatus()
    }

    override fun stop() {
        isRequestedToRun = false
        updateStatus()
    }

    fun onServiceConnected() {
        updateStatus(true)
    }

    fun onServiceDisconnected() {
        updateStatus(false)
        _foregroundApp.value = null
    }

    fun onForegroundPackageChanged(packageName: CharSequence?) {
        if (!isRequestedToRun || _status.value != ForegroundMonitorStatus.RUNNING) return
        
        val pkg = packageName?.toString()
        if (pkg != null) {
            _foregroundApp.value = ForegroundApp(packageName = pkg)
        }
    }

    private var isServiceBound = false
    
    private fun updateStatus(bound: Boolean = isServiceBound) {
        isServiceBound = bound
        if (!isRequestedToRun) {
            _status.value = ForegroundMonitorStatus.STOPPED
        } else if (!isServiceBound) {
            _status.value = ForegroundMonitorStatus.REQUIRES_ACCESS
        } else {
            _status.value = ForegroundMonitorStatus.RUNNING
        }
    }
}
