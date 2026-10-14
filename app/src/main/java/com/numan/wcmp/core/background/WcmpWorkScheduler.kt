package com.numan.wcmp.core.background

interface WcmpWorkScheduler {
    fun scheduleInstalledAppsSync()
    fun scheduleRepositoryMaintenance()
    fun scheduleDiagnosticsRefresh()
    fun cancelAllWcmpWork()
}
