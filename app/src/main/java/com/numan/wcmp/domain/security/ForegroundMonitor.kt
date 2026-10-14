package com.numan.wcmp.domain.security

import kotlinx.coroutines.flow.Flow

interface ForegroundMonitor {
    val status: Flow<ForegroundMonitorStatus>
    val foregroundApp: Flow<ForegroundApp?>

    fun start()
    fun stop()
}
