package com.numan.wcmp.core.security.evidence

import com.numan.wcmp.data.repository.SecurityRepository
import com.numan.wcmp.domain.evidence.EvidenceRecord
import com.numan.wcmp.domain.evidence.EvidenceStorage
import com.numan.wcmp.domain.evidence.EvidenceType
import com.numan.wcmp.feature.vault.model.VaultCategory
import com.numan.wcmp.feature.vault.model.VaultEvidence
import com.numan.wcmp.feature.vault.model.VaultEvidenceType
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
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
    ) = withContext(Dispatchers.IO) {
        var isEncrypted = false
        var encryptionVersion: Int? = null
        var actualSizeBytes = record.fileSizeInBytes

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

                    isEncrypted = true
                    encryptionVersion = EvidenceEncryptionVersion.V1
                    actualSizeBytes = encryptedFile.length()
                } catch (e: Exception) {
                    EvidenceCrypto.secureDelete(plaintextFile)
                    EvidenceCrypto.secureDelete(encryptedFile)
                    throw EvidenceEncryptionException("Failed to encrypt evidence file", e)
                }
            }
        } else if (record.type == EvidenceType.LOCATION_STAMP) {
            isEncrypted = true
            encryptionVersion = EvidenceEncryptionVersion.V1
        }

        val (vaultType, vaultCategory) = when (record.type) {
            EvidenceType.FRONT_CAMERA_PHOTO -> VaultEvidenceType.PHOTO to VaultCategory.PHOTOS
            EvidenceType.AUDIO_CLIP -> VaultEvidenceType.AUDIO to VaultCategory.AUDIO
            EvidenceType.LOCATION_STAMP -> VaultEvidenceType.LOCATION to VaultCategory.OTHER
            EvidenceType.AUDIT_LOG_SNAPSHOT -> VaultEvidenceType.EVENT to VaultCategory.EVENTS
        }

        val formattedDate = SimpleDateFormat("MMM dd, HH:mm", Locale.getDefault())
            .format(Date(record.capturedAtTimestamp))

        val sizeLabel = if (record.type == EvidenceType.LOCATION_STAMP) {
            "1 KB"
        } else {
            "${(actualSizeBytes / 1024L).coerceAtLeast(1L)} KB"
        }

        val vaultEvidence = VaultEvidence(
            id = record.id,
            title = title,
            type = vaultType,
            relatedEvent = relatedEventId ?: category,
            timestamp = formattedDate,
            size = sizeLabel,
            category = vaultCategory,
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

    override suspend fun deleteEvidenceRecord(id: String) = withContext(Dispatchers.IO) {
        val encryptedFileName = "evidence_$id.enc"
        val encryptedFile = File(filesDir, "evidence/$encryptedFileName")
        if (encryptedFile.exists()) {
            EvidenceCrypto.secureDelete(encryptedFile)
        }
        securityRepository.deleteVaultEvidence(id)
    }
}
