package com.numan.wcmp.core.background

import android.content.Context
import androidx.work.Constraints
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.ExistingWorkPolicy
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import com.numan.wcmp.core.background.workers.DiagnosticsRefreshWorker
import com.numan.wcmp.core.background.workers.InstalledAppsSyncWorker
import com.numan.wcmp.core.background.workers.RepositoryMaintenanceWorker
import java.util.concurrent.TimeUnit

class WcmpWorkManager(context: Context) : WcmpWorkScheduler {

    private val workManager = WorkManager.getInstance(context)

    companion object {
        const val SYNC_WORK_NAME = "WCMP_INSTALLED_APPS_SYNC"
        const val MAINTENANCE_WORK_NAME = "WCMP_REPOSITORY_MAINTENANCE"
        const val DIAGNOSTICS_WORK_NAME = "WCMP_DIAGNOSTICS_REFRESH"
    }

    override fun scheduleInstalledAppsSync() {
        // Periodic sync to detect new apps, but not aggressive polling.
        // Once a day is reasonable for background discovery.
        val constraints = Constraints.Builder()
            .setRequiresBatteryNotLow(true)
            .build()

        val request = PeriodicWorkRequestBuilder<InstalledAppsSyncWorker>(24, TimeUnit.HOURS)
            .setConstraints(constraints)
            .build()

        workManager.enqueueUniquePeriodicWork(
            SYNC_WORK_NAME,
            ExistingPeriodicWorkPolicy.KEEP,
            request
        )
    }

    override fun scheduleRepositoryMaintenance() {
        // Weekly maintenance for database cleanup
        val constraints = Constraints.Builder()
            .setRequiresDeviceIdle(true)
            .setRequiresCharging(true)
            .build()

        val request = PeriodicWorkRequestBuilder<RepositoryMaintenanceWorker>(7, TimeUnit.DAYS)
            .setConstraints(constraints)
            .build()

        workManager.enqueueUniquePeriodicWork(
            MAINTENANCE_WORK_NAME,
            ExistingPeriodicWorkPolicy.KEEP,
            request
        )
    }

    override fun scheduleDiagnosticsRefresh() {
        // Diagnostics refresh can be one-time since it's on-demand typically.
        val request = OneTimeWorkRequestBuilder<DiagnosticsRefreshWorker>().build()

        workManager.enqueueUniqueWork(
            DIAGNOSTICS_WORK_NAME,
            ExistingWorkPolicy.REPLACE, // Replace if already running since it's on-demand
            request
        )
    }

    override fun cancelAllWcmpWork() {
        workManager.cancelUniqueWork(SYNC_WORK_NAME)
        workManager.cancelUniqueWork(MAINTENANCE_WORK_NAME)
        workManager.cancelUniqueWork(DIAGNOSTICS_WORK_NAME)
    }
}
