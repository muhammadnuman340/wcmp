package com.numan.wcmp.domain.evidence.health

import com.numan.wcmp.core.security.evidence.EvidenceCrypto
import com.numan.wcmp.core.security.evidence.EvidenceKeyManager
import com.numan.wcmp.feature.vault.model.VaultEvidence
import java.io.File

interface EvidenceIntegrityService {
    suspend fun inspect(evidence: VaultEvidence): EvidenceHealth
    fun inspectFast(evidence: VaultEvidence): EvidenceHealth
    
    /**
     * Finds orphan encrypted files that don't match any VaultEvidence entity.
     * Returns a list of filenames.
     */
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

    override suspend fun inspect(evidence: VaultEvidence): EvidenceHealth {
        // First fast check
        val fastHealth = healthEvaluator.evaluateHealth(evidence)
        if (fastHealth != EvidenceHealth.HEALTHY) {
            return fastHealth
        }
        
        val encryptedFileName = "evidence_${evidence.id}.enc"
        val encryptedFile = File(directoryProvider.getEvidenceDirectory(), encryptedFileName)
        
        if (!encryptedFile.exists()) {
            return EvidenceHealth.MISSING_FILE
        }

        return try {
            EvidenceCrypto.validateIntegrity(keyManager, encryptedFile)
            EvidenceHealth.HEALTHY
        } catch (e: Exception) {
            // Note: If the exception indicates KEY_UNAVAILABLE, the fast check would have caught it.
            // If it failed here, it's either an invalid tag or the key was suddenly invalid.
            EvidenceHealth.INTEGRITY_ERROR
        }
    }

    override suspend fun scanForOrphans(existingEvidence: List<VaultEvidence>): List<String> {
        val evidenceDir = directoryProvider.getEvidenceDirectory()
        val allEncryptedFiles = evidenceDir.listFiles { _, name -> name.startsWith("evidence_") && name.endsWith(".enc") }
            ?.map { it.name } ?: emptyList()

        val expectedFiles = existingEvidence.map { "evidence_${it.id}.enc" }.toSet()

        return allEncryptedFiles.filter { !expectedFiles.contains(it) }
    }
}

