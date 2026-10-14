package com.numan.wcmp.core.background.workers

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.numan.wcmp.data.WcmpRepositoryProvider
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class DiagnosticsRefreshWorker(
    appContext: Context,
    workerParams: WorkerParameters
) : CoroutineWorker(appContext, workerParams) {

    override suspend fun doWork(): Result = withContext(Dispatchers.IO) {
        try {
            val capabilityRepository = WcmpRepositoryProvider.getCapabilityRepository(applicationContext)
            
            // Just refresh local capabilities cleanly in the background without launching prompts
            capabilityRepository.refreshCapabilities()
            
            Result.success()
        } catch (e: Exception) {
            Result.retry()
        }
    }
}
