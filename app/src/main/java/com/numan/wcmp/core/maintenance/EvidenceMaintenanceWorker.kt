package com.numan.wcmp.core.maintenance

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.numan.wcmp.data.WcmpRepositoryProvider
import com.numan.wcmp.domain.evidence.health.EvidenceHealth
import com.numan.wcmp.domain.evidence.health.EvidenceHealthEvaluator
import com.numan.wcmp.core.security.evidence.DefaultEvidenceKeyManager
import kotlinx.coroutines.flow.first
import java.io.File

class EvidenceMaintenanceWorker(
    appContext: Context,
    workerParams: WorkerParameters
) : CoroutineWorker(appContext, workerParams) {

    override suspend fun doWork(): Result {
        val context = applicationContext
        val repository = WcmpRepositoryProvider.getSecurityRepository(context)
        val keyManager = DefaultEvidenceKeyManager()
        val evaluator = EvidenceHealthEvaluator(context.filesDir, keyManager)
        
        // 1. Audit Storage Directory
        val evidenceDir = File(context.filesDir, "evidence")
        val filesOnDisk = evidenceDir.listFiles()?.toList() ?: emptyList()
        
        // 2. Audit Database Records
        val records = repository.observeVaultEvidence().first()
        val recordMap = records.associateBy { "evidence_${it.id}.enc" }
        
        var orphansDetected = 0
        var missingFilesDetected = 0
        
        // 3. Orphan Encrypted File Detection
        for (file in filesOnDisk) {
            if (file.extension == "enc" && !recordMap.containsKey(file.name)) {
                // Orphaned file found! (No DB record)
                // We do NOT blindly delete unknown files. We report them for diagnostics.
                orphansDetected++
            }
        }
        
        // 4. Missing File Detection
        for (record in records) {
            val health = evaluator.evaluateHealth(record)
            if (health == EvidenceHealth.MISSING_FILE) {
                missingFilesDetected++
                // We do NOT delete the DB record automatically. 
                // It's kept as UNAVAILABLE so the user knows an event occurred but evidence was lost.
            }
        }
        
        // Return success with diagnostic output
        val outputData = androidx.work.workDataOf(
            "orphans_detected" to orphansDetected,
            "missing_detected" to missingFilesDetected
        )
        
        return Result.success(outputData)
    }
}
