package com.numan.wcmp.core.security.evidence

import com.numan.wcmp.data.repository.SecurityRepository
import com.numan.wcmp.domain.evidence.EvidenceRecord
import com.numan.wcmp.domain.evidence.EvidenceStorage
import com.numan.wcmp.domain.evidence.EvidenceType
import com.numan.wcmp.feature.vault.model.VaultCategory
import com.numan.wcmp.feature.vault.model.VaultEvidence
import com.numan.wcmp.feature.vault.model.VaultEvidenceType
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class EncryptedEvidenceStorage(
    private val filesDir: File,
    private val securityRepository: SecurityRepository,
    private val keyManager: EvidenceKeyManager = DefaultEvidenceKeyManager()
) : EvidenceStorage {

    override suspend fun saveEvidenceRecord(
        record: EvidenceRecord,
        relatedEventId: String?,
        title: String,
        category: String,
        sessionId: String?
    ) {
        var finalLocalUri = record.localUri
        var isEncrypted = false
        var encryptionVersion: Int? = null

        // Encrypt file if it's a valid local URI starting with file://
        if (record.localUri.startsWith("file://")) {
            val path = record.localUri.removePrefix("file://")
                val plaintextFile = File(path)
                if (plaintextFile.exists()) {
                    val encryptedFileName = "evidence_${record.id}.enc"
                    val encryptedFile = File(filesDir, "evidence/$encryptedFileName")
                    
                    encryptedFile.parentFile?.mkdirs()

                    try {
                        EvidenceCrypto.encryptFile(
                            keyManager = keyManager,
                            plaintextFile = plaintextFile,
                            encryptedFile = encryptedFile,
                            version = EvidenceEncryptionVersion.V1
                        )
                        
                        finalLocalUri = "file://${encryptedFile.absolutePath}"
                        isEncrypted = true
                        encryptionVersion = EvidenceEncryptionVersion.V1
                    } catch (e: Exception) {
                        // If encryption fails, clean up the plaintext file so it isn't left exposed
                        if (plaintextFile.exists()) {
                            plaintextFile.delete()
                        }
                        if (encryptedFile.exists()) {
                            encryptedFile.delete()
                        }
                        throw EvidenceEncryptionException("Failed to encrypt evidence file", e)
                    }
                }
        } else if (record.type == EvidenceType.LOCATION_STAMP) {
            // Location is pure metadata, so we consider it "encrypted" implicitly by the Vault schema
            isEncrypted = true
            encryptionVersion = EvidenceEncryptionVersion.V1
        }

        val vaultType = when (record.type) {
            EvidenceType.FRONT_CAMERA_PHOTO -> VaultEvidenceType.PHOTO
            EvidenceType.AUDIO_CLIP -> VaultEvidenceType.AUDIO
            EvidenceType.LOCATION_STAMP -> VaultEvidenceType.LOCATION
            EvidenceType.AUDIT_LOG_SNAPSHOT -> VaultEvidenceType.EVENT
        }

        val vaultEvidence = VaultEvidence(
            id = record.id,
            title = title,
            type = vaultType,
            relatedEvent = relatedEventId ?: category,
            timestamp = SimpleDateFormat("MMM dd, HH:mm", Locale.getDefault()).format(Date(record.capturedAtTimestamp)),
            size = "${record.fileSizeInBytes / 1024} KB",
            category = VaultCategory.OTHER,
            latitude = record.latitude,
            longitude = record.longitude,
            accuracyMeters = record.accuracyMeters,
            altitude = record.altitude,
            isEncrypted = isEncrypted,
            encryptionVersion = encryptionVersion,
            sessionId = sessionId
        )
        
        securityRepository.insertVaultEvidence(vaultEvidence)
    }

    override suspend fun deleteEvidenceRecord(id: String) {
        // Retrieve the record to know the file path
        // For simplicity, we just delete the standard format file if it exists
        val encryptedFileName = "evidence_$id.enc"
        val encryptedFile = File(filesDir, "evidence/$encryptedFileName")
        if (encryptedFile.exists()) {
            encryptedFile.delete()
        }
        securityRepository.deleteVaultEvidence(id)
    }
}
