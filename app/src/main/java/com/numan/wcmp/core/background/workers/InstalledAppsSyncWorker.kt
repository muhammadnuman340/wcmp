package com.numan.wcmp.core.background.workers

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.numan.wcmp.data.WcmpRepositoryProvider
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class InstalledAppsSyncWorker(
    appContext: Context,
    workerParams: WorkerParameters
) : CoroutineWorker(appContext, workerParams) {

    override suspend fun doWork(): Result = withContext(Dispatchers.IO) {
        try {
            val repository = WcmpRepositoryProvider.getProtectedAppsRepository(applicationContext)
            
            // Delegate discovery and merge logic directly to the repository
            repository.refreshInstalledApps()
            
            Result.success()
        } catch (e: Exception) {
            // Log this securely in a real environment
            // Return retry for transient failures during sync
            Result.retry()
        }
    }
}
