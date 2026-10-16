package com.numan.wcmp.domain.evidence.health

import com.numan.wcmp.core.security.evidence.EvidenceCrypto
import com.numan.wcmp.core.security.evidence.EvidenceKeyManager
import com.numan.wcmp.feature.vault.model.VaultEvidence
import com.numan.wcmp.feature.vault.model.VaultEvidenceType
import java.io.File
import java.io.FileInputStream

class EvidenceHealthEvaluator(
    private val filesDir: File,
    private val keyManager: EvidenceKeyManager
) {
    fun evaluateHealth(evidence: VaultEvidence): EvidenceHealth {
        // Location evidence is metadata stored in Room without an external .enc file
        if (evidence.type == VaultEvidenceType.LOCATION) {
            return if (evidence.latitude != null && evidence.longitude != null) {
                EvidenceHealth.HEALTHY
            } else {
                EvidenceHealth.INTEGRITY_ERROR
            }
        }

        if (!evidence.isEncrypted) {
            return EvidenceHealth.LEGACY
        }

        val encryptedFileName = "evidence_${evidence.id}.enc"
        val encryptedFile = File(filesDir, "evidence/$encryptedFileName")

        if (!encryptedFile.exists()) {
            return EvidenceHealth.MISSING_FILE
        }

        try {
            keyManager.getExistingKey() ?: return EvidenceHealth.KEY_UNAVAILABLE
        } catch (e: Exception) {
            return EvidenceHealth.KEY_UNAVAILABLE
        }

        if (encryptedFile.length() < 14) { // Header (version + ivLength) + 12-byte IV
            return EvidenceHealth.INTEGRITY_ERROR
        }

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
