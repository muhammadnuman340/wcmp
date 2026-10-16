package com.numan.wcmp.domain.evidence.health

import com.numan.wcmp.core.security.evidence.EvidenceCrypto
import com.numan.wcmp.core.security.evidence.EvidenceKeyManager
import com.numan.wcmp.feature.vault.model.VaultEvidence
import java.io.File
import java.util.UUID

class ControlledDecryptionService(
    private val directoryProvider: EvidenceDirectoryProvider,
    private val keyManager: EvidenceKeyManager
) : EvidenceDecryptionService {
    /**
     * Decrypts the evidence file into a short-lived, app-private cache file.
     * Returns the File if successful, or throws an exception on integrity/key errors.
     */
    override fun decryptForViewing(evidence: VaultEvidence): File {
        val encryptedFileName = "evidence_${evidence.id}.enc"
        val encryptedFile = File(directoryProvider.getEvidenceDirectory(), encryptedFileName)
        
        if (!encryptedFile.exists()) {
            throw IllegalStateException("File is missing")
        }

        val cacheDir = directoryProvider.getControlledViewingDirectory()
        if (!cacheDir.exists()) {
            cacheDir.mkdirs()
        }

        // Generate a random temporary filename to avoid collisions
        val ext = when (evidence.type) {
            com.numan.wcmp.feature.vault.model.VaultEvidenceType.PHOTO -> ".jpg"
            com.numan.wcmp.feature.vault.model.VaultEvidenceType.AUDIO -> ".m4a"
            else -> ".bin"
        }
        val tempFile = File(cacheDir, "view_${UUID.randomUUID()}$ext")

        EvidenceCrypto.decryptFile(keyManager, encryptedFile, tempFile)
        
        return tempFile
    }

    /**
     * Cleans up any decrypted cache files securely with zero-fill overwrite.
     */
    override fun cleanupCache() {
        val cacheDir = directoryProvider.getControlledViewingDirectory()
        if (cacheDir.exists()) {
            cacheDir.listFiles()?.forEach { file ->
                EvidenceCrypto.secureDelete(file)
            }
        }
    }
}
