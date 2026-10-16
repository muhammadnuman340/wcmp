package com.numan.wcmp.domain.evidence.health

import com.numan.wcmp.core.security.evidence.EvidenceCrypto
import com.numan.wcmp.core.security.evidence.EvidenceKeyManager
import com.numan.wcmp.feature.vault.model.VaultEvidence
import com.numan.wcmp.feature.vault.model.VaultEvidenceType
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File

interface EvidenceIntegrityService {
    suspend fun inspect(evidence: VaultEvidence): EvidenceHealth
    fun inspectFast(evidence: VaultEvidence): EvidenceHealth
    suspend fun scanForOrphans(existingEvidence: List<VaultEvidence>): List<String>
}

class DefaultEvidenceIntegrityService(
    private val directoryProvider: EvidenceDirectoryProvider,
    private val keyManager: EvidenceKeyManager,
    private val healthEvaluator: EvidenceHealthEvaluator
) : EvidenceIntegrityService {

    override fun inspectFast(evidence: VaultEvidence): EvidenceHealth {
        return healthEvaluator.evaluateHealth(evidence)
    }

    override suspend fun inspect(evidence: VaultEvidence): EvidenceHealth = withContext(Dispatchers.IO) {
        val fastHealth = healthEvaluator.evaluateHealth(evidence)
        if (fastHealth != EvidenceHealth.HEALTHY) {
            return@withContext fastHealth
        }

        if (evidence.type == VaultEvidenceType.LOCATION) {
            return@withContext EvidenceHealth.HEALTHY
        }

        val encryptedFileName = "evidence_${evidence.id}.enc"
        val encryptedFile = File(directoryProvider.getEvidenceDirectory(), encryptedFileName)

        if (!encryptedFile.exists()) {
            return@withContext EvidenceHealth.MISSING_FILE
        }

        try {
            EvidenceCrypto.validateIntegrity(keyManager, encryptedFile)
            EvidenceHealth.HEALTHY
        } catch (e: Exception) {
            EvidenceHealth.INTEGRITY_ERROR
        }
    }

    override suspend fun scanForOrphans(existingEvidence: List<VaultEvidence>): List<String> =
        withContext(Dispatchers.IO) {
            val evidenceDir = directoryProvider.getEvidenceDirectory()
            val allFiles = evidenceDir.listFiles { file -> file.isFile }?.map { it.name } ?: emptyList()
            val expectedFiles = existingEvidence
                .filter { it.type != VaultEvidenceType.LOCATION }
                .map { "evidence_${it.id}.enc" }
                .toSet()

            allFiles.filter { !expectedFiles.contains(it) }
        }
}
