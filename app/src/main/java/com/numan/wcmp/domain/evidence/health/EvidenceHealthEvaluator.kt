package com.numan.wcmp.domain.evidence.health

import com.numan.wcmp.core.security.evidence.EvidenceCrypto
import com.numan.wcmp.core.security.evidence.EvidenceKeyManager
import com.numan.wcmp.feature.vault.model.VaultEvidence
import java.io.File
import java.io.FileInputStream

class EvidenceHealthEvaluator(
    private val filesDir: File,
    private val keyManager: EvidenceKeyManager
) {
    /**
     * Evaluates the health state of a piece of VaultEvidence.
     * This avoids a full decryption, preferring to quickly verify file and key presence.
     */
    fun evaluateHealth(evidence: VaultEvidence): EvidenceHealth {
        // Legacy unencrypted
        if (!evidence.isEncrypted) {
            return EvidenceHealth.LEGACY
        }
        
        // Location evidence has no file, it's just encrypted metadata
        // For our current implementation, we just check if it's marked encrypted.
        if (evidence.type == com.numan.wcmp.feature.vault.model.VaultEvidenceType.LOCATION) {
            return EvidenceHealth.HEALTHY
        }
        
        // Ensure the file exists
        val encryptedFileName = "evidence_${evidence.id}.enc"
        val encryptedFile = File(filesDir, "evidence/$encryptedFileName")
        
        if (!encryptedFile.exists()) {
            return EvidenceHealth.MISSING_FILE
        }

        // Try getting the key — do NOT create a new one for health checks
        val key = try {
            keyManager.getExistingKey() ?: return EvidenceHealth.KEY_UNAVAILABLE
        } catch (e: Exception) {
            return EvidenceHealth.KEY_UNAVAILABLE
        }

        // Attempt a quick read of the header (version, IV length) to catch basic corruption
        // We do not do a full GCM tag check here because it requires fully reading and decrypting the file.
        // Full INTEGRITY_ERROR will be discovered during actual decryption attempt (Controlled Decryption).
        try {
            FileInputStream(encryptedFile).use { input ->
                val version = input.read()
                if (version != 1) return EvidenceHealth.MIGRATION_REQUIRED
                
                val ivLength = input.read()
                if (ivLength != 12) return EvidenceHealth.INTEGRITY_ERROR
            }
        } catch (e: Exception) {
            return EvidenceHealth.INTEGRITY_ERROR
        }

        return EvidenceHealth.HEALTHY
    }
}
